/*
 * Copyright 2014 EUBrazilCC (EU‐Brazil Cloud Connect)
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by 
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *   http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */

package eu.eubrazilcc.lvl.storage.mongodb;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.toMap;
import static com.mongodb.MapReduceCommand.OutputType.REDUCE;
import static com.mongodb.MongoCredential.createMongoCRCredential;
import static com.mongodb.util.JSON.parse;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.BaseFile;
import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.Versionable;
import eu.eubrazilcc.lvl.core.geojson.Polygon;

/**
 * Data connector based on mongoDB. Access to file collections is provided through the GridFS specification. While other objects
 * rely only on the final version, previous file versions are kept in the database. The additional attribute {@link #IS_LATEST_VERSION_ATTR isLastestVersion} 
 * of the {@link BaseFile base file class} is used to create a sparse index with a unique constraint, enforcing the integrity of 
 * the files within a collection (only one version of a file can be labeled as the latest version of the file).
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://www.mongodb.org/">mongoDB</a>
 * @see <a href="http://docs.mongodb.org/manual/core/gridfs/">GridFS</a>
 */
public enum MongoDBConnector implements Closeable2 {

	MONGODB_CONN;

	private static final Logger LOGGER = getLogger(MongoDBConnector.class);

	public static final String TMP_COLLECTION_PREFIX = "tmp_";

	/**
	 * The name of the MongoDB GridFS files collection.
	 * @see <a href="http://docs.mongodb.org/manual/core/gridfs/#gridfs-collections">GridFS Collections</a>
	 */
	private static final String GRIDFS_FILES_COLLECTION = "files";

	public static final String METADATA_ATTR = "metadata";
	public static final String IS_LATEST_VERSION_ATTR = "isLastestVersion";
	public static final String OPEN_ACCESS_LINK_ATTR = "openAccessLink";
	public static final String OPEN_ACCESS_DATE_ATTR = "openAccessDate";
	public static final String FILE_VERSION_PROP = METADATA_ATTR + "." + IS_LATEST_VERSION_ATTR;
	public static final String FILE_OPEN_ACCESS_LINK_PROP = METADATA_ATTR + "." + OPEN_ACCESS_LINK_ATTR;
	public static final String FILE_OPEN_ACCESS_DATE_PROP = METADATA_ATTR + "." + OPEN_ACCESS_DATE_ATTR;

	private Lock mutex = new ReentrantLock();
	private MongoClient __client = null;	

	private MongoClient client() {
		mutex.lock();
		try {
			if (__client == null) {
				final MongoClientOptions options = MongoClientOptions.builder()
						.readPreference(ReadPreference.nearest())
						.writeConcern(WriteConcern.ACKNOWLEDGED).build();
				final Splitter splitter = on(':').trimResults().omitEmptyStrings().limit(2);
				final List<ServerAddress> seeds = from(CONFIG_MANAGER.getDbHosts()).transform(new Function<String, ServerAddress>() {						
					@Override
					public ServerAddress apply(final String entry) {
						ServerAddress seed = null;						
						try {
							final List<String> tokens = splitter.splitToList(entry);
							switch (tokens.size()) {
							case 1:
								seed = new ServerAddress(tokens.get(0), 27017);
								break;
							case 2:
								int port = parseInt(tokens.get(1));
								seed = new ServerAddress(tokens.get(0), port);
								break;
							default:
								break;
							}
						} catch (Exception e) {
							LOGGER.error("Invalid host", e);
						}
						return seed;
					}						
				}).filter(notNull()).toList();
				// enable/disable authentication (requires MongoDB server to be configured to support authentication)
				final List<MongoCredential> credentials = newArrayList();
				if (!CONFIG_MANAGER.isAnonymousDbAccess()) {
					credentials.add(createMongoCRCredential(CONFIG_MANAGER.getDbUsername(), 
							CONFIG_MANAGER.getDbName(), CONFIG_MANAGER.getDbPassword().toCharArray()));
				}
				__client = new MongoClient(seeds, credentials, options);
				// check class attributes accessed by reflection
				try {
					final Field metadataField = BaseFile.class.getDeclaredField(METADATA_ATTR);					
					checkNotNull(metadataField, "Metadata property (" + METADATA_ATTR + ") not found in file base: " 
							+ BaseFile.class.getCanonicalName());
					final Class<?> metadataType = metadataField.getType();
					checkState(Versionable.class.isAssignableFrom(metadataType), "Metadata does not implements versionable: " 
							+ metadataType.getCanonicalName());
					checkNotNull(Versionable.class.getDeclaredField(IS_LATEST_VERSION_ATTR), 
							"Version property (" + IS_LATEST_VERSION_ATTR + ") not found in versionable: "
									+ Versionable.class.getCanonicalName());					
					checkNotNull(metadataType.getDeclaredField(OPEN_ACCESS_LINK_ATTR), 
							"Open access link property (" + OPEN_ACCESS_LINK_ATTR + ") not found in metadata: " + metadataType.getCanonicalName());					
					checkNotNull(metadataType.getDeclaredField(OPEN_ACCESS_DATE_ATTR), 
							"Open access date property (" + OPEN_ACCESS_DATE_ATTR + ") not found in metadata: " + metadataType.getCanonicalName());					
				} catch (Exception e) {
					throw new IllegalStateException("Object versioning needs a compatible version of the LVL core library, but none is available", e);
				}
			}
			return __client;
		} finally {
			mutex.unlock();
		}
	}

	/**
	 * Creates a new index in a collection, sorting the elements in ascending order.
	 * @param field - field that is used to index the elements
	 * @param collection - collection where the index is created
	 */
	public void createIndex(final String field, final String collection) {
		createIndex(field, collection, false);
	}

	/**
	 * Creates an index on a field, if one does not already exist on the specified collection. Indexes 
	 * created with this method are created in the background and stores unique elements.
	 * @param field - field that is used to index the elements
	 * @param collection - collection where the index is created
	 * @param descending - (optional) sort the elements of the index in descending order
	 */
	public void createIndex(final String field, final String collection, final boolean descending) {
		checkArgument(isNotBlank(field), "Uninitialized or invalid field");
		createIndex(ImmutableList.of(field), collection, true, descending);
	}

	/**
	 * Creates an index on a set of fields, if one does not already exist on the specified collection, 
	 * sorting the elements in ascending order. Indexes created with this method are created in the 
	 * background and stores unique elements.
	 * @param fields - fields that are used to index the elements
	 * @param collection - collection where the index is created
	 */
	public void createIndex(final List<String> fields, final String collection) {
		createIndex(fields, collection, true, false);
	}

	/**
	 * Creates an index on a field, if one does not already exist on the specified collection. Indexes 
	 * created with this method are created in the background and allow storing duplicated elements.
	 * @param field - field that is used to index the elements
	 * @param collection - collection where the index is created
	 * @param descending - (optional) sort the elements of the index in descending order
	 */
	public void createNonUniqueIndex(final String field, final String collection, final boolean descending) {
		createIndex(ImmutableList.of(field), collection, false, descending);
	}

	/**
	 * Creates an index on a set of fields, if one does not already exist on the specified collection.
	 * Indexes created with this method are created in the background and allow storing duplicated 
	 * elements.
	 * @param fields - fields that are used to index the elements
	 * @param collection - collection where the index is created
	 * @param descending - (optional) sort the elements of the index in descending order
	 */
	public void createNonUniqueIndex(final List<String> fields, final String collection, final boolean descending) {
		createIndex(fields, collection, false, descending);
	}

	/**
	 * Creates an index on a set of fields, if one does not already exist on the specified collection.
	 * Indexes created with this method are created in the background and could stores unique elements.
	 * @param fields - fields that are used to index the elements
	 * @param collection - collection where the index is created
	 * @param descending - (optional) sort the elements of the index in descending order
	 */
	public void createIndex(final List<String> fields, final String collection, final boolean unique, final boolean descending) {
		checkArgument(fields != null && !fields.isEmpty(), "Uninitialized or invalid fields");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		dbcol.createIndex(new BasicDBObject(toMap(fields, new Function<String, Integer>() {
			@Override
			public Integer apply(final String field) {
				return descending ? -1 : 1;
			}				
		})), new BasicDBObject(unique ? ImmutableMap.of("unique", true, "background", true) : ImmutableMap.of("background", true)));
	}

	/**
	 * Creates a new geospatial index in a collection. Indexes are created in the background.
	 * @param field - field that is used to index the elements
	 * @param collection - collection where the index is created
	 */
	public void createGeospatialIndex(final String field, final String collection) {
		checkArgument(isNotBlank(field), "Uninitialized or invalid field");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		dbcol.createIndex(new BasicDBObject(field, "2dsphere"), new BasicDBObject("background", true));
	}

	/**
	 * Creates a text index in a collection. Text indexes are created using English as the default language.
	 * @param fields - fields that are used to index the elements
	 * @param collection - collection where the index is created
	 */
	public void createTextIndex(final List<String> fields, final String collection) {
		checkArgument(fields != null && !fields.isEmpty(), "Uninitialized or invalid fields");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		dbcol.createIndex(new BasicDBObject(toMap(fields, new Function<String, String>() {
			@Override
			public String apply(final String field) {
				return "text";
			}
		})), new BasicDBObject("default_language", "english").append("name", collection + ".text_idx"));
	}

	/**
	 * Creates a sparse index with a unique constraint on a field, if one does not already exist on the specified collection.
	 * Indexes created with this method are created in the background. <strong>Note:</strong> Do NOT use compound indexes to
	 * create an sparse index, since the results are unexpected.
	 * @param field - field that is used to index the elements
	 * @param collection - collection where the index is created
	 * @param descending - (optional) sort the elements of the index in descending order
	 * @see <a href="http://docs.mongodb.org/manual/core/index-sparse/">MongoDB: Sparse Indexes</a>
	 */
	public void createSparseIndexWithUniqueConstraint(final String field, final String collection, final boolean descending) {
		checkArgument(isNotBlank(field), "Uninitialized or invalid field");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");		
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		dbcol.createIndex(new BasicDBObject(field, descending ? -1 : 1),
				new BasicDBObject(ImmutableMap.of("unique", true, "background", true, "sparse", true)));				
	}

	/**
	 * Inserts an object into the specified collection.
	 * @param obj - object to be inserted in the collection
	 * @param collection - collection where the object is inserted
	 * @return the id associated to the object in the collection
	 */
	public String insert(final DBObject obj, final String collection) {
		checkArgument(obj != null, "Uninitialized object");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		try {
			dbcol.insert(obj);
		} catch (DuplicateKeyException dke) {
			throw new MongoDBDuplicateKeyException(dke.getMessage());
		}
		return ObjectId.class.cast(obj.get("_id")).toString();
	}

	/**
	 * Retrieves an object from a collection.
	 * @param query - statement that is used to find the object in the collection
	 * @param collection - collection where the object is searched
	 * @return the object retrieved from the database or {@code null}
	 */
	public BasicDBObject get(final DBObject query, final String collection) {
		checkArgument(query != null, "Uninitialized query");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		return (BasicDBObject) dbcol.findOne(query);			
	}

	/**
	 * Returns a view of the objects in the collection that contains the specified range. The objects are sorted by the key in ascending order. 
	 * Optionally, the number of objects found in the collection is returned to the caller. Also, the returned fields can be filtered.
	 * @param sortCriteria - objects in the collection are sorted with this criteria
	 * @param collection - collection where the objects are searched
	 * @param start - starting index
	 * @param size - maximum number of objects returned
	 * @param query - the expression to be used to query the collection
	 * @param projection - (optional) Specifies the fields to return using projection operators. To return all fields in the matching document, 
	 *                     omit this parameter
	 * @param count - (optional) is updated with the number of objects in the database
	 * @return a view of the objects in the collection that contains the specified range
	 */
	public List<BasicDBObject> list(final DBObject sortCriteria, final String collection, final int start, final int size, 
			final @Nullable DBObject query, final @Nullable DBObject projection, final @Nullable MutableLong count) {
		checkArgument(sortCriteria != null, "Uninitialized sort criteria");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final List<BasicDBObject> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		final DBCursor cursor = query != null ? dbcol.find(query) : dbcol.find(); // TODO : projection
		cursor.sort(sortCriteria);
		cursor.skip(start).limit(size);
		try {
			while (cursor.hasNext()) {
				list.add((BasicDBObject) cursor.next());
			}
		} finally {
			cursor.close();
		}
		if (count != null) {				
			count.setValue(cursor.count());
		}
		return list;
	}

	/**
	 * Counts the objects stored in a collection.
	 * @param collection - collection whose objects are counted
	 * @return the number of objects stored in the collection
	 */
	public long count(final String collection) {
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		return dbcol.getCount();
	}

	/**
	 * Returns the objects in the collection that are within the specified distance (in meters) 
	 * from the center point specified by the coordinates (in WGS84). The objects are sorted 
	 * from nearest to farthest.
	 * @param collection - collection whose objects are counted
	 * @param longitude - longitude in WGS84 coordinate reference system (CRS)
	 * @param latitude - latitude in WGS84 coordinate reference system (CRS)
	 * @param maxDistance - limits the results to those objects that fall within the specified 
	 *        distance (in meters) from the center point
	 * @return the objects that are within the specified distance from the center point, sorted
	 *        from nearest to farthest
	 */
	public BasicDBList geoNear(final String collection, final double longitude, final double latitude, 
			final double maxDistance) {
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final BasicDBObject query = new BasicDBObject("geoNear", collection)
		.append("near", new BasicDBObject("type", "Point").append("coordinates", new double[]{ longitude, latitude }))
		.append("maxDistance", maxDistance)
		.append("spherical", true)			
		.append("uniqueDocs", true)
		.append("num", Integer.MAX_VALUE);
		LOGGER.trace("geoNear query: " + JSON.serialize(query));
		final CommandResult cmdResult = db.command(query);
		checkState(cmdResult.ok(), "geoNear search failed");			
		return (BasicDBList) cmdResult.get("results");
	}

	public List<BasicDBObject> geoWithin(final String locationField, final String collection, final Polygon polygon) {
		checkArgument(isNotBlank(locationField), "Uninitialized or invalid location field");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		checkArgument(polygon != null, "Uninitialized polygon");
		final List<BasicDBObject> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		try {
			final BasicDBObject query = new BasicDBObject(locationField, new BasicDBObject("$geoWithin", 
					new BasicDBObject("$geometry", (DBObject) parse(JSON_MAPPER.writeValueAsString(polygon)))));
			LOGGER.trace("geoWithin query: " + JSON.serialize(query));
			final DBCursor cursor = dbcol.find(query);
			try {
				while (cursor.hasNext()) {
					list.add((BasicDBObject) cursor.next());
				}
			} finally {
				cursor.close();
			}
			return list;
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to parse request parameters", e);			
		}
	}

	/**
	 * Wrapper method to {@link MongoDBConnector#mapReduce(String, String, String, DBObject)} that performs a search by proximity
	 * to select the documents from the collection that will be used as input of the map function.
	 * @param collection - collection whose objects are searched
	 * @param field - geospatial field that defines the 2dsphere index for location data defined as GeoJSON points
	 * @param mapFn - map function
	 * @param reduceFn - reduce function
	 * @param longitude - longitude in WGS84 coordinate reference system (CRS)
	 * @param latitude - latitude in WGS84 coordinate reference system (CRS)
	 * @param maxDistance - limits the results to those objects that fall within the specified 
	 *        distance (in meters) from the center point
	 * @return a list of {@code BasicDBObject} which contains the results of this map reduce operation.
	 */
	public List<BasicDBObject> mapReduce(final String collection, final String field, final String mapFn, final String reduceFn, 
			final double longitude, final double latitude, final double maxDistance) {
		final BasicDBObject query = new BasicDBObject(field, new BasicDBObject("$nearSphere", 
				new BasicDBObject("$geometry", new BasicDBObject("type", "Point").append("coordinates", new double[]{ longitude, latitude }))
		.append("$maxDistance", maxDistance)));
		LOGGER.trace("geoNear query: " + JSON.serialize(query));
		return mapReduce(collection, mapFn, reduceFn, query);
	}

	/**
	 * Runs a map-reduce aggregation over a collection and saves the result to a temporary collection, which is deleted at the end
	 * of the execution. The results of the operation are returned to the caller in a list of {@code BasicDBObject}.
	 * @param collection - collection whose objects are searched
	 * @param mapFn - map function
	 * @param reduceFn - reduce function
	 * @param query - (optional) specifies the selection criteria using query operators for determining the documents input to the 
	 *        map function. Set this parameter to {@code null} to use all documents in the collection
	 * @return a list of {@code BasicDBObject} which contains the results of this map reduce operation.
	 * @see <a href="http://cookbook.mongodb.org/patterns/pivot/">The MongoDB Cookbook - Pivot Data with Map reduce</a>
	 */
	public List<BasicDBObject> mapReduce(final String collection, final String mapFn, final String reduceFn, final @Nullable DBObject query) {
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		checkArgument(isNotBlank(mapFn), "Uninitialized or map function");
		checkArgument(isNotBlank(reduceFn), "Uninitialized or reduce function");
		final List<BasicDBObject> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		final DBCollection tmpCol = tmpCollection();		
		try {
			final MapReduceCommand command = new MapReduceCommand(dbcol, mapFn, reduceFn, tmpCol.getName(), REDUCE, query);
			final MapReduceOutput output = dbcol.mapReduce(command);
			final Iterable<DBObject> results = output.results();
			for (final DBObject result: results) {
				list.add((BasicDBObject) result);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Failed to execute map-reduce operation", e);			
		} finally {
			try {
				tmpCol.drop();
			} catch (Exception mdbe) {
				LOGGER.warn("Failed to drop temporary collection", mdbe);
			}
		}
		return list;
	}

	/**
	 * Updates a object previously stored in a collection.
	 * @param obj - value used to update the object
	 * @param query - statement that is used to find the object in the collection
	 * @param collection - collection where the object is searched
	 */
	public void update(final DBObject obj, final DBObject query, final String collection) {
		checkArgument(obj != null, "Uninitialized object");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		final BasicDBObject current = (BasicDBObject) dbcol.findOne(query);
		checkState(current != null, "Object not found");
		dbcol.save(BasicDBObjectBuilder.start(obj.toMap()).append("_id", current.get("_id")).get());
	}

	/**
	 * Removes an object from a collection.
	 * @param query - statement that is used to find the object in the collection
	 * @param collection - collection from which the object is removed
	 */
	public void remove(final DBObject query, final String collection) {
		checkArgument(query != null, "Uninitialized query");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		dbcol.remove(query);
	}

	/**
	 * Writes statistics about a collection to the specified output stream.
	 * @param os - the output stream to write the statistics to
	 * @param collection - collection from which the statistics are collected
	 * @throws IOException - If an I/O error occurred
	 */
	public void stats(final OutputStream os, final String collection) throws IOException {
		checkArgument(os != null, "Uninitialized output stream");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		try {
			os.write((" >> Collection: " + collection + "\n").getBytes());		
			final List<DBObject> indexes = dbcol.getIndexInfo();
			os.write("   >> Indexes\n".getBytes());
			for (final DBObject idx : indexes) {
				os.write(("        " + idx.toString() + "\n").getBytes());
			}
			os.write(("   >> Items count: " + dbcol.getCount() + "\n").getBytes());
		} finally {
			os.flush();
		}	
	}

	/* GridFS */

	/**
	 * Saves a file into the current database using the specified <tt>namespace</tt> and <tt>filename</tt>. All files sharing the same 
	 * <tt>namespace</tt> and <tt>filename</tt> are considered versions of the same file. So, inserting a new file with an existing 
	 * <tt>namespace</tt> and <tt>filename</tt> will create a new entry in the database. The method {@link #readFile(String, String)} will 
	 * retrieve the latest version of the file and the method {@link #readFile(String, String)} will remove all the versions of the file. 
	 * Other possible options could be to define a unique index in the <tt>files</tt> collection to avoid duplicates (versions) to be 
	 * created: <code>createIndex("filename", namespace + ".files");</code>
	 * @param namespace - (optional) name space under the file is saved. When nothing specified, the default bucket is used
	 * @param filename - filename to be assigned to the file in the database
	 * @param file - file to be saved to the database
	 * @param metadata - optional file metadata
	 * @return the id associated to the file in the collection
	 */
	public String saveFile(final @Nullable String namespace, final String filename, final File file, final @Nullable DBObject metadata) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		checkArgument(file != null && file.canRead() && file.isFile(), "Uninitialized or invalid file");
		String objectId = null;
		final String namespace2 = trimToEmpty(namespace);
		final String filename2 = filename.trim();
		if (metadata != null) {
			metadata.removeField(IS_LATEST_VERSION_ATTR);
		}
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace2) ? new GridFS(db, namespace2) : new GridFS(db);
		// enforce isolation property: each namespace has its own bucket (encompassing 2 collections: files and chunks) and indexes in the database
		createSparseIndexWithUniqueConstraint(FILE_VERSION_PROP, gfsNs.getBucketName() + "." + GRIDFS_FILES_COLLECTION, false);
		// index open access links
		createNonUniqueIndex(FILE_OPEN_ACCESS_LINK_PROP, gfsNs.getBucketName() + "." + GRIDFS_FILES_COLLECTION, false);
		try {				
			// insert new file/version in the database
			final GridFSInputFile gfsFile = gfsNs.createFile(file);
			gfsFile.setFilename(filename2);
			gfsFile.setContentType(mimeType(file));				
			gfsFile.setMetaData(metadata);
			gfsFile.save();
			objectId = ObjectId.class.cast(gfsFile.getId()).toString();
			// unset the latest version in the database
			final GridFSDBFile latestVersion = getLatestVersion(gfsNs, filename2);
			if (latestVersion != null && latestVersion.getMetaData() != null) {
				latestVersion.getMetaData().removeField(IS_LATEST_VERSION_ATTR);
				latestVersion.save();
			}
		} catch (DuplicateKeyException dke) {
			throw new MongoDBDuplicateKeyException(dke.getMessage());
		} catch (IOException ioe) {
			throw new IllegalStateException("Failed to save file", ioe);				
		} finally {
			// enforce versioning property by always restoring the latest version in the database
			restoreLatestVersion(gfsNs, filename2);				
		}
		return objectId;
	}

	/**
	 * Gets the version of the <tt>filename</tt> labeled with the {@link #FILE_VERSION_PROP} property.
	 * @param gridfs - GridFS client
	 * @param filename - filename to be searched for in the database
	 * @return
	 */
	private GridFSDBFile getLatestVersion(final GridFS gridfs, final String filename) {
		return gridfs.findOne(new BasicDBObject(FILE_VERSION_PROP, filename.trim()));
	}

	/**
	 * Gets the version of the <tt>filename</tt> with the latest uploaded date.
	 * @param gridfs - GridFS client
	 * @param filename - filename to be searched for in the database
	 * @return the version of the file identified by the provided filename with the latest uploaded date. 
	 */
	private GridFSDBFile getLatestUploadedFile(final GridFS gridfs, final String filename) {
		return getFirst(gridfs.find(filename.trim(), new BasicDBObject("uploadDate", -1)), null);
	}

	/**
	 * Restores the latest version of a file in the database.
	 * @param gridfs - GridFS client
	 * @param filename - filename to be searched for in the database
	 */
	private void restoreLatestVersion(final GridFS gridfs, final String filename) {
		try {
			final GridFSDBFile latestUploadedVersion = getLatestUploadedFile(gridfs, filename);
			if (latestUploadedVersion != null) {
				if (latestUploadedVersion.getMetaData() == null) {
					latestUploadedVersion.setMetaData(new BasicDBObject());
				}
				latestUploadedVersion.getMetaData().put(IS_LATEST_VERSION_ATTR, filename);				
				latestUploadedVersion.save();
			}
		} catch (Exception ignore) {
			LOGGER.error("Failed to restore latest version namespace=" + gridfs.getBucketName() + ", filename=" + filename);
		}
	}

	public void updateMetadata(final @Nullable String namespace, final String filename, final @Nullable DBObject metadata) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final String namespace2 = trimToEmpty(namespace);
		final String filename2 = filename.trim();
		final DBObject metadata2 = metadata != null ? metadata : new BasicDBObject();
		metadata2.put(IS_LATEST_VERSION_ATTR, filename2);
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace2) ? new GridFS(db, namespace2) : new GridFS(db);
		try {
			final GridFSDBFile latestUploadedVersion = getLatestUploadedFile(gfsNs, filename2);
			checkState(latestUploadedVersion != null, "File not found");
			latestUploadedVersion.setMetaData(metadata2);								
			latestUploadedVersion.save();
		} catch (IllegalStateException ise) {
			throw ise;
		} catch (Exception e) {
			LOGGER.error("Failed to update latest metadata version in namespace=" + gfsNs.getBucketName() 
					+ ", filename=" + filename, e);
		}
	}

	public String createOpenAccessLink(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final String namespace2 = trimToEmpty(namespace);
		final String filename2 = filename.trim();
		final String secret = random(32, "abcdefghijklmnopqrstuvwxyz0123456789");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace2) ? new GridFS(db, namespace2) : new GridFS(db);
		try {
			final GridFSDBFile latestUploadedVersion = getLatestUploadedFile(gfsNs, filename2);
			checkState(latestUploadedVersion != null, "File not found");
			if (latestUploadedVersion.getMetaData() == null) {
				latestUploadedVersion.setMetaData(new BasicDBObject());
			}
			latestUploadedVersion.getMetaData().put(OPEN_ACCESS_LINK_ATTR, secret);
			latestUploadedVersion.getMetaData().put(OPEN_ACCESS_DATE_ATTR, JSON.parse(JSON_MAPPER.writeValueAsString(new Date())));
			latestUploadedVersion.save();
		} catch (IllegalStateException ise) {
			throw ise;
		} catch (Exception e) {
			LOGGER.error("Failed to create open access link in latest file version in namespace=" + gfsNs.getBucketName() 
					+ ", filename=" + filename, e);
		}
		return secret;
	}

	public void removeOpenAccessLink(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final String namespace2 = trimToEmpty(namespace);
		final String filename2 = filename.trim();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace2) ? new GridFS(db, namespace2) : new GridFS(db);			
		final List<GridFSDBFile> files = gfsNs.find(new BasicDBObject("filename", filename2).append(FILE_OPEN_ACCESS_LINK_PROP, 
				new BasicDBObject("$exists", true)), new BasicDBObject("uploadDate", -1));
		for (final GridFSDBFile file : files) {
			if (file.getMetaData() != null) {
				file.getMetaData().removeField(OPEN_ACCESS_LINK_ATTR);
				file.getMetaData().removeField(OPEN_ACCESS_DATE_ATTR);
				file.save();
			}
		}
		/* ideally we want to use a cursor here, but the save() operation fails
			final DBCursor cursor = gfsNs.getFileList(new BasicDBObject("filename", filename2).append(FILE_OPEN_ACCESS_LINK_PROP, 
					new BasicDBObject("$exists", true)));
			try {
				while (cursor.hasNext()) {
					final GridFSDBFile file = (GridFSDBFile) cursor.next();
					if (file.getMetaData() != null) {
						file.getMetaData().removeField(OPEN_ACCESS_LINK_ATTR);
						file.getMetaData().removeField(OPEN_ACCESS_DATE_ATTR); // this fails
						file.save();
					}
				}
			} finally {
				cursor.close();
			} */
	}

	/**
	 * Reads a file object from the current database. The file is identified by the original filename stored in the database and the 
	 * name space under the file was stored. When several versions exist of the same file, the latest version will be retrieved.
	 * @param namespace - (optional) name space to be searched for in the database. When nothing specified, the default bucket is used
	 * @param filename - filename to be searched for in the database
	 * @param output - file where the output will be saved, this file must exists and must be writable
	 */
	public GridFSDBFile readFile(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);			
		return getLatestVersion(gfsNs, filename);
	}

	public GridFSDBFileWrapper readOpenAccessFile(final String secret) {
		checkArgument(isNotBlank(secret), "Uninitialized or invalid secret");
		final String secret2 = secret.trim();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		GridFSDBFileWrapper fileWrapper = null;
		final Set<String> collections = db.getCollectionNames();
		final Iterator<String> it = collections.iterator();
		while (it.hasNext() && fileWrapper == null) {
			final String collection = it.next();
			if (collection.endsWith("." + GRIDFS_FILES_COLLECTION)) {
				final String[] tokens = Splitter.on('.')
						.omitEmptyStrings()
						.trimResults()
						.splitToList(collection)
						.toArray(new String[2]);
				if (tokens.length >= 2) {
					final String namespace = tokens[tokens.length - 2];
					final GridFS gfsNs = new GridFS(db, namespace);
					final GridFSDBFile file = gfsNs.findOne(new BasicDBObject(FILE_OPEN_ACCESS_LINK_PROP, secret2));
					if (file != null) {
						fileWrapper = new GridFSDBFileWrapper(namespace, file);
					}
				}
			}
		}
		return fileWrapper;		
	}

	/**
	 * Checks whether or not the specified file exists in the database, returning <tt>true</tt> only when the file exists in the 
	 * specified namespace.
	 * @param namespace - (optional) name space to be searched for in the database. When nothing specified, the default bucket is used
	 * @param filename - filename to be searched for in the database
	 * @return
	 */
	public boolean fileExists(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);
		return gfsNs.findOne(filename.trim()) != null;
	}

	/**
	 * Lists all the files in the specified name space. Only latest versions are included in the list.
	 * @param namespace - (optional) name space to be searched for files. When nothing specified, the default bucket is used
	 * @param sortCriteria - objects in the collection are sorted with this criteria
	 * @param start - starting index
	 * @param size - maximum number of objects returned
	 * @param count - (optional) is updated with the number of objects in the database
	 * @return a view of the files stored under the specified name space that contains the specified range.
	 */
	public List<GridFSDBFile> listFiles(final @Nullable String namespace, final DBObject sortCriteria, final int start, final int size, 
			final @Nullable MutableLong count) {
		final List<GridFSDBFile> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);
		final DBCursor cursor = gfsNs.getFileList(new BasicDBObject(FILE_VERSION_PROP, new BasicDBObject("$exists", true)), sortCriteria);
		cursor.skip(start).limit(size);
		try {
			while (cursor.hasNext()) {
				list.add((GridFSDBFile) cursor.next());
			}
		} finally {
			cursor.close();
		}
		if (count != null) {				
			count.setValue(cursor.count());
		}
		return list;			
	}

	public List<GridFSDBFile> listFileOpenAccess(final @Nullable String namespace, final DBObject sortCriteria, final int start, final int size, 
			final @Nullable MutableLong count) {
		final List<GridFSDBFile> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);
		final DBCursor cursor = gfsNs.getFileList(new BasicDBObject(FILE_VERSION_PROP, new BasicDBObject("$exists", true)).append(
				FILE_OPEN_ACCESS_LINK_PROP, new BasicDBObject("$exists", true)), sortCriteria);
		cursor.skip(start).limit(size);
		try {
			while (cursor.hasNext()) {
				list.add((GridFSDBFile) cursor.next());
			}
		} finally {
			cursor.close();
		}
		if (count != null) {				
			count.setValue(cursor.count());
		}
		return list;
	}

	/**
	 * Lists all the versions of the specified file.
	 * @param namespace - (optional) name space to be searched for files. When nothing specified, the default bucket is used
	 * @param filename - filename to be searched for in the database
	 * @param sortCriteria - objects in the collection are sorted with this criteria
	 * @param start - starting index
	 * @param size - maximum number of objects returned
	 * @param count - (optional) is updated with the number of objects in the database
	 * @return a view of the versions stored of the specified file that contains the specified range.
	 */
	public List<GridFSDBFile> listFileVersions(final @Nullable String namespace, final String filename, final DBObject sortCriteria, final int start, 
			final int size, final @Nullable MutableLong count) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final List<GridFSDBFile> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);			
		final DBCursor cursor = gfsNs.getFileList(new BasicDBObject("filename", filename.trim()), sortCriteria);
		cursor.skip(start).limit(size);
		try {
			while (cursor.hasNext()) {
				list.add((GridFSDBFile) cursor.next());
			}
		} finally {
			cursor.close();
		}
		if (count != null) {				
			count.setValue(cursor.count());
		}
		return list;		
	}

	/**
	 * Removes a file (with all its versions) from the specified name space.
	 * @param namespace - (optional) name space where the file was stored under. When nothing specified, the default bucket is used
	 * @param filename - filename to be removed from the database
	 */
	public void removeFile(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);
		gfsNs.remove(filename);
	}

	/**
	 * Deletes the latest version from the database. When a previous version exists for the file in the specified namespace, the latest
	 * uploaded version will be the new latest version.
	 * @param namespace - (optional) name space where the file was stored under. When nothing specified, the default bucket is used
	 * @param filename - filename to be removed from the database
	 */
	public void undoLatestVersion(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		final String filename2 = filename.trim();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final GridFS gfsNs = isNotBlank(namespace) ? new GridFS(db, namespace.trim()) : new GridFS(db);
		try {
			// remove latest version from the database				
			gfsNs.remove(new BasicDBObject(FILE_VERSION_PROP, filename2));
		} finally {
			// enforce versioning property by always restoring the latest version in the database
			restoreLatestVersion(gfsNs, filename2);
		}
	}

	/**
	 * Counts the files stored in the specified name space.
	 * @param namespace - (optional) name space whose files are counted
	 * @return the number of objects stored in the collection
	 */
	public long countFiles(final @Nullable String namespace) {
		String namespace2 = trimToNull(namespace);
		if (namespace2 == null) {
			final DB db = client().getDB(CONFIG_MANAGER.getDbName());
			final GridFS gfsNs = new GridFS(db);
			namespace2 = gfsNs.getBucketName();
		}
		return count(namespace2 + "." + GRIDFS_FILES_COLLECTION);		
	}

	/**
	 * Writes statistics about a files name space to the specified output stream.
	 * @param os - the output stream to write the statistics to
	 * @param namespace - (optional) name space from which the statistics are collected
	 * @throws IOException - If an I/O error occurred
	 */
	public void statsFiles(final OutputStream os, final @Nullable String namespace) throws IOException {
		String namespace2 = trimToNull(namespace);
		if (namespace2 == null) {
			final DB db = client().getDB(CONFIG_MANAGER.getDbName());
			final GridFS gfsNs = new GridFS(db);
			namespace2 = gfsNs.getBucketName();
		}
		stats(os, namespace2 + "." + GRIDFS_FILES_COLLECTION);
	}

	/* General methods */

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("MongoDB connector initialized successfully");
	}

	@Override
	public void close() throws IOException {
		mutex.lock();
		try {
			if (__client != null) {
				__client.close();
				__client = null;
			}
		} finally {
			mutex.unlock();
			LOGGER.info("MongoDB connector shutdown successfully");
		}
	}

	/**
	 * Creates an object id that can be used to query a collection by the 
	 * {@code _id} field.
	 * @param id - identifier that is used to create the object id
	 * @return an object id that can be used to query a collection by the 
	 *         {@code _id} field
	 */
	public static BasicDBObject objectId(final String id) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid id");
		return new BasicDBObject("_id", new ObjectId(id));
	}	

	private DBCollection tmpCollection() {
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		return db.getCollection(TMP_COLLECTION_PREFIX + randomAlphanumeric(12));
	}

	/* Inner classes */

	public static class GridFSDBFileWrapper {
		private final String namespace;
		private final GridFSDBFile file;
		public GridFSDBFileWrapper(final String namespace, final GridFSDBFile file) {
			this.namespace = namespace;
			this.file = file;
		}
		public String getNamespace() {
			return namespace;
		}
		public GridFSDBFile getFile() {
			return file;
		}		
	}

}