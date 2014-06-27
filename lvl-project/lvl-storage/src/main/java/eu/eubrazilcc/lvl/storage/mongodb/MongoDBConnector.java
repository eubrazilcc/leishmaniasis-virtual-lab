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
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.toMap;
import static com.mongodb.MongoCredential.createMongoCRCredential;
import static com.mongodb.util.JSON.parse;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
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
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.geojson.Polygon;

/**
 * Data connector based on mongoDB.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://www.mongodb.org/">mongoDB</a>
 */
public enum MongoDBConnector implements Closeable2 {

	MONGODB_CONN;

	private final static Logger LOGGER = getLogger(MongoDBConnector.class);

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
	 * Indexes created with this method are created in the background and stores unique elements.
	 * @param fields - fields that are used to index the elements
	 * @param collection - collection where the index is created
	 * @param descending - (optional) sort the elements of the index in descending order
	 */
	public void createIndex(final List<String> fields, final String collection, final boolean unique, final boolean descending) {
		checkArgument(fields != null && !fields.isEmpty(), "Uninitialized or invalid fields");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		db.requestStart();
		try {
			db.requestEnsureConnection();
			dbcol.createIndex(new BasicDBObject(toMap(fields, new Function<String, Integer>() {
				@Override
				public Integer apply(final String field) {
					return descending ? -1 : 1;
				}				
			})), new BasicDBObject(unique ? ImmutableMap.of("unique", true, "background", true) : ImmutableMap.of("background", true)));
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			dbcol.createIndex(new BasicDBObject(field, "2dsphere"), new BasicDBObject("background", true));
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			dbcol.insert(obj);
			return ObjectId.class.cast(obj.get("_id")).toString();
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			return (BasicDBObject) dbcol.findOne(query);			
		} finally {
			db.requestDone();
		}
	}

	/**
	 * Returns a view of the objects in the collection that contains the specified
	 * range. The objects are sorted by the key in ascending order. Optionally,
	 * the number of objects found in the collection is returned to the caller.
	 * @param sortCriteria - objects in the collection are sorted with this criteria
	 * @param collection - collection where the objects are searched
	 * @param start - starting index
	 * @param size - maximum number of objects returned
	 * @param count - (optional) is updated with the number of objects in the database
	 * @return a view of the objects in the collection that contains the specified range
	 */
	public List<BasicDBObject> list(final DBObject sortCriteria, final String collection, 
			final int start, final int size, final @Nullable MutableLong count) {
		checkArgument(sortCriteria != null, "Uninitialized sort criteria");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		final List<BasicDBObject> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		db.requestStart();
		try {
			db.requestEnsureConnection();
			final DBCursor cursor = dbcol.find();			 
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
				count.setValue(dbcol.getCount());
			}
			return list;
		} finally {
			db.requestDone();
		}	
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			return dbcol.getCount();
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			final BasicDBObject query = new BasicDBObject("geoNear", collection)
			.append("near", new BasicDBObject("type", "Point").append("coordinates", new double[]{ longitude, latitude }))
			.append("maxDistance", maxDistance)
			.append("spherical", true)			
			.append("uniqueDocs", true)
			.append("num", 100);
			LOGGER.trace("geoNear query: " + JSON.serialize(query));
			final CommandResult cmdResult = db.command(query);
			checkState(cmdResult.ok(), "geoNear search failed");			
			return (BasicDBList) cmdResult.get("results");
		} finally {
			db.requestDone();
		}	
	}

	public List<BasicDBObject> geoWithin(final String locationField, final String collection, final Polygon polygon) {
		checkArgument(isNotBlank(locationField), "Uninitialized or invalid location field");
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		checkArgument(polygon != null, "Uninitialized polygon");
		final List<BasicDBObject> list = newArrayList();
		final DB db = client().getDB(CONFIG_MANAGER.getDbName());
		final DBCollection dbcol = db.getCollection(collection);
		db.requestStart();
		try {
			db.requestEnsureConnection();
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
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			final BasicDBObject current = (BasicDBObject) dbcol.findOne(query);
			checkState(current != null, "Object not found");
			dbcol.save(BasicDBObjectBuilder.start(obj.toMap()).append("_id", current.get("_id")).get());
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			db.requestEnsureConnection();
			dbcol.remove(query);
		} finally {
			db.requestDone();
		}
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
		db.requestStart();
		try {
			os.write((" >> Collection: " + collection + "\n").getBytes());
			db.requestEnsureConnection();			
			final List<DBObject> indexes = dbcol.getIndexInfo();
			os.write("   >> Indexes\n".getBytes());
			for (final DBObject idx : indexes) {
				os.write(("        " + idx.toString() + "\n").getBytes());
			}
			os.write(("   >> Items count: " + dbcol.getCount() + "\n").getBytes());
		} finally {			
			db.requestDone();
			os.flush();
		}	
	}

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

}