/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
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
import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.successfulAsList;
import static com.mongodb.MongoCredential.createMongoCRCredential;
import static com.mongodb.ReadPreference.nearest;
import static com.mongodb.WriteConcern.ACKNOWLEDGED;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Filters.text;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.storage.Filters.LogicalType.LOGICAL_AND;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_ACTIVE_VERSION_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_GUID_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_LAST_MODIFIED_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_LOCATION_FIELD;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.JSON_MAPPER;
import static java.lang.Integer.parseInt;
import static org.apache.commons.beanutils.PropertyUtils.getSimpleProperty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.mongodb.Block;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.AggregateIterable;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.connection.ClusterSettings;

import eu.eubrazilcc.lvl.core.BaseFile;
import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.Feature;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.Filter;
import eu.eubrazilcc.lvl.storage.Filters;
import eu.eubrazilcc.lvl.storage.LvlObjectNotFoundException;
import eu.eubrazilcc.lvl.storage.LvlObjectWriteException;
import eu.eubrazilcc.lvl.storage.base.LvlCollection;
import eu.eubrazilcc.lvl.storage.base.LvlObject;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDateDeserializer;

/**
 * Data connector based on mongoDB. Access to file collections is provided through the GridFS specification. While other objects
 * rely only on the final version, previous file versions are kept in the database. The additional attribute {@link #IS_LATEST_VERSION_ATTR isLastestVersion} 
 * of the {@link BaseFile base file class} is used to create a sparse index with a unique constraint, enforcing the integrity of 
 * the files within a collection (only one version of a file can be labeled as the latest version of the file).
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://www.mongodb.org/">mongoDB</a>
 * @see <a href="http://docs.mongodb.org/manual/core/gridfs/">GridFS</a>
 */
public enum MongoConnector implements Closeable2 {

	MONGODB_CONN;

	private static final Logger LOGGER = getLogger(MongoConnector.class);

	private Lock mutex = new ReentrantLock();
	private MongoClient __client = null;

	private MongoClient client() {
		mutex.lock();
		try {
			if (__client == null) {
				// cluster settings
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
				final ClusterSettings clusterSettings = ClusterSettings.builder()
						.hosts(seeds)
						.build();
				// enable/disable authentication (requires MongoDB server to be configured to support authentication)
				final List<MongoCredential> credentialList = newArrayList();
				if (!CONFIG_MANAGER.isAnonymousDbAccess()) {
					credentialList.add(createMongoCRCredential(CONFIG_MANAGER.getDbUsername(), 
							CONFIG_MANAGER.getDbName(), CONFIG_MANAGER.getDbPassword().toCharArray()));
				}
				// create client
				final MongoClientSettings settings = MongoClientSettings.builder()
						.readPreference(nearest())
						.writeConcern(ACKNOWLEDGED)
						.clusterSettings(clusterSettings)
						.credentialList(credentialList)
						.build();
				__client = MongoClients.create(settings);				
			}
			return __client;
		} finally {
			mutex.unlock();
		}
	}

	public ListenableFuture<List<String>> createIndexes(final String collection, final List<IndexModel> indexes) {
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		checkArgument(indexes != null, "Uninitialized or invalid indexes");
		final SettableFuture<List<String>> future = SettableFuture.create();
		final MongoDatabase db = client().getDatabase(CONFIG_MANAGER.getDbName());
		final MongoCollection<Document> dbcol = db.getCollection(collection);
		dbcol.createIndexes(indexes, new SingleResultCallback<List<String>>() {
			@Override
			public void onResult(final List<String> result, final Throwable t) {
				if (t == null) future.set(result); else future.setException(t);
			}
		});		
		return future;
	}

	/**
	 * Saves the specified object to the database, overriding any previous entry or creating a new one in case that there are no matches 
	 * for the object GUID. The saved object will be the new active version, but in the case that other entry is active and cannot be 
	 * overridden, then this method will fail.
	 * @param obj - object to save
	 * @return a future which result indicates that the operation is successfully completed, or an exception when the method fails.
	 */
	public <T extends LvlObject> ListenableFuture<Void> save(final T obj) {
		final SettableFuture<Void> future = SettableFuture.create();
		final MongoCollection<Document> dbcol = getCollection(obj);
		final Document update = new Document(ImmutableMap.<String, Object>of("$currentDate", new Document(LVL_LAST_MODIFIED_FIELD, true),
				"$set", parseObject(obj, true, true)));
		final FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().projection(fields(include(LVL_GUID_FIELD)))
				.returnDocument(AFTER)
				.upsert(true);
		dbcol.findOneAndUpdate(eq(LVL_GUID_FIELD, obj.getLvlId()), update, options, new SingleResultCallback<Document>() {
			@Override
			public void onResult(final Document result, final Throwable t) {
				if (t == null) {
					if (result != null) {
						obj.setDbId(result.get("_id", ObjectId.class).toHexString());
						future.set(null);
					} else future.setException(new LvlObjectWriteException("No new records were inserted, no existing records were modified by the operation"));					
				} else future.setException(t);
			}
		});
		return future;
	}

	/**
	 * Finds the active version of the specified object. In case that any of the object's versions are active, this method will return
	 * <tt>null</tt> to the caller.
	 * @param obj - the object to search for
	 * @param type - the object type
	 * @return a future which result contains the found object, or an exception when the method fails.
	 */
	public <T extends LvlObject> ListenableFuture<LvlObject> find(final LvlObject obj, final Class<T> type) {
		checkArgument(obj != null && obj.getClass().isAssignableFrom(type), "Uninitialized or invalid object type");
		checkArgument(isNotBlank(obj.getLvlId()), "Uninitialized or invalid primary key value");
		final SettableFuture<LvlObject> future = SettableFuture.create();
		final MongoCollection<Document> dbcol = getCollection(obj);
		dbcol.find(eq(LVL_GUID_FIELD, obj.getLvlId())).modifiers(ACTIVE_VERSION_HINT).sort(LAST_MODIFIED_SORT_DESC).first(new SingleResultCallback<Document>() {
			@Override
			public void onResult(final Document result, final Throwable t) {
				if (t == null) {					
					try {
						if (result == null) throw new LvlObjectNotFoundException("Object not found");
						future.set(parseDocument(result, type));
					} catch (Exception e) {
						future.setException(e);
					}
				} else future.setException(t);
			}
		});
		return future;
	}

	/**
	 * Finds elements in a collection.
	 * Note: <tt>hint</tt> and <tt>text</tt> are mutually exclusive in a query expression.
	 * @see <a href="http://docs.mongodb.org/manual/reference/method/cursor.hint/#behavior">mongoDB -- cursor.hint()</a>
	 */
	public <T extends LvlObject> ListenableFuture<List<T>> find(final LvlCollection<T> collection, final Class<T> type, final int start, final int size, 
			final @Nullable Filters filters, final Map<String, Boolean> sorting, final @Nullable Map<String, Boolean> projections, 
			final @Nullable MutableLong totalCount) {		
		// parse input parameters
		boolean textFilter = false;
		final List<Bson> filterFields = newArrayList();
		if (filters != null && filters.getFilters() != null && !filters.getFilters().isEmpty()) {
			for (final Filter filter : filters.getFilters()) {
				final String value = trimToEmpty(filter.getValue());
				switch (filter.getType()) {
				case FILTER_NOT:
					throw new UnsupportedOperationException("The filter 'NOT' is currently unsupported.");
				case FILTER_REGEX:
					filterFields.add(regex(filter.getFieldName(), value));
					break;
				case FILTER_TEXT:
					filterFields.add(text(value));
					textFilter = true;
					break;
				case FILTER_COMPARE:
				default:
					if (value.startsWith("<>")) {
						filterFields.add(ne(filter.getFieldName(), value.substring(2).trim()));
					} else if (value.startsWith(">=")) {
						filterFields.add(gte(filter.getFieldName(), value.substring(2).trim()));
					} else if (value.startsWith(">")) {
						filterFields.add(gt(filter.getFieldName(), value.substring(1).trim()));
					} else if (value.startsWith("<=")) {
						filterFields.add(lte(filter.getFieldName(), value.substring(2).trim()));
					} else if (value.startsWith("<")) {
						filterFields.add(lt(filter.getFieldName(), value.substring(1).trim()));
					} else if (value.startsWith("=")) {
						filterFields.add(eq(filter.getFieldName(), value.substring(1).trim()));
					} else {
						throw new UnsupportedOperationException("The expression '" + value + "' is not a recognized filter.");
					}					
					break;
				}
			}
		}
		final Bson filter = (filterFields.isEmpty() ? new Document() : (filters != null && LOGICAL_AND.equals(filters.getType()) 
				? and(filterFields) : or(filterFields)));
		final Bson modifiers = (!textFilter ? ACTIVE_VERSION_HINT : new Document());
		final List<Bson> projectionFields = newArrayList();
		if (projections != null && !projections.isEmpty()) {
			for (final Map.Entry<String, Boolean> projection : projections.entrySet()) {
				projectionFields.add(projection.getValue() ? include(projection.getKey()) : exclude(projection.getKey()));
			}
		}
		final List<Bson> sortingFields = newArrayList();
		if (sorting != null && !sorting.isEmpty()) {
			for (final Map.Entry<String, Boolean> sort : sorting.entrySet()) {
				sortingFields.add(sort.getValue() ? descending(sort.getKey()) : ascending(sort.getKey()));
			}
		}
		// prepare to operate on the collection
		final MongoCollection<Document> dbcol = getCollection(collection);		
		final List<T> page = newArrayList();		
		final MutableLong __totalCount = new MutableLong(0l);
		// get total number of records
		final SettableFuture<Void> countFuture = SettableFuture.create();		
		dbcol.count(filter, new SingleResultCallback<Long>() {
			@Override
			public void onResult(final Long result, final Throwable t) {
				if (t == null) {
					__totalCount.setValue(result);
					countFuture.set(null);
				} else countFuture.setException(t);
			}
		});
		// find records and populate the page
		final SettableFuture<Void> findFuture = SettableFuture.create();
		final FindIterable<Document> iterable = dbcol
				.find(filter)
				.modifiers(modifiers)
				.projection(fields(projectionFields))
				.sort(orderBy(sortingFields))
				.skip(start)
				.limit(size);
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document doc) {
				try {					
					page.add(parseDocument(doc, type));
				} catch (Exception e) {
					LOGGER.error("Failed to parse result, [collection='" + collection.getCollection() + "', objectId='" 
							+ doc.get("_id", ObjectId.class).toHexString() + "']", e);
				}
			}
		}, new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				if (t == null) findFuture.set(null); else findFuture.setException(t);
			}
		});
		// combine results
		final SettableFuture<List<T>> future = SettableFuture.create();
		@SuppressWarnings("unchecked")
		final ListenableFuture<List<Void>> concatenated = allAsList(countFuture, findFuture);
		addCallback(concatenated, new FutureCallback<List<Void>>() {
			@Override
			public void onSuccess(final List<Void> result) {
				if (totalCount != null) totalCount.setValue(__totalCount.getValue());
				future.set(page);
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}			
		});
		return future;
	}

	public <T extends LvlObject> ListenableFuture<Boolean> delete(final T obj, final boolean deleteReferences) { // TODO : add cascading
		checkArgument(obj != null, "Uninitialized or invalid object");
		checkArgument(isNotBlank(obj.getLvlId()), "Uninitialized or invalid primary key value");
		final SettableFuture<Boolean> future = SettableFuture.create();
		final MongoCollection<Document> dbcol = getCollection(obj);
		dbcol.deleteOne(eq(LVL_GUID_FIELD, obj.getLvlId()), new SingleResultCallback<DeleteResult>() {
			@Override
			public void onResult(final DeleteResult result, final Throwable t) {
				if (t == null) {
					if (result.getDeletedCount() == 1l) {			
						future.set(true);
					} else if (result.getDeletedCount() < 1l) {
						future.set(false);
					} else {
						future.setException(new IllegalStateException("Multiple records were deleted: " + result.getDeletedCount()));
					}
				} else future.setException(t);
			}
		});
		return future;
	}

	public <T extends LvlObject> ListenableFuture<Long> totalCount(final LvlCollection<T> collection) {
		final MongoCollection<Document> dbcol = getCollection(collection);
		final SettableFuture<Long> countFuture = SettableFuture.create();		
		dbcol.count(new SingleResultCallback<Long>() {
			@Override
			public void onResult(final Long result, final Throwable t) {
				if (t == null) countFuture.set(result); else countFuture.setException(t);
			}		
		});
		return countFuture;
	}

	public <T extends LvlObject> ListenableFuture<FeatureCollection> fetchNear(final LvlCollection<T> collection, final Class<T> type,
			final double longitude, final double latitude, final double minDistance, final double maxDistance) {
		final SettableFuture<FeatureCollection> future = SettableFuture.create();
		final FeatureCollection features = FeatureCollection.builder().crs(Crs.builder().wgs84().build()).build();
		final MongoCollection<Document> dbcol = getCollection(collection);
		final Document geoNear = new Document("$geoNear", new Document(ImmutableMap.<String, Object>builder()
				.put("spherical", true)
				.put("num", new BsonInt32(Integer.MAX_VALUE))
				.put("maxDistance", maxDistance)
				.put("near", new Document("type", "Point").append("coordinates", new BsonArray(newArrayList(new BsonDouble(longitude), new BsonDouble(latitude)))))
				.put("distanceField", "_dist.calculated")
				.build()));
		final Document project = new Document("$project", new Document(ImmutableMap.<String, Object>builder()
				.put(LVL_GUID_FIELD, true)
				.put(LVL_LOCATION_FIELD, true)
				.put("_dist", true)
				.build()));
		final Document match = new Document("$match", new Document(ImmutableMap.<String, Object>builder()
				.put("_dist.calculated", new Document("$gte", new BsonDouble(minDistance)))
				.build()));
		final AggregateIterable<Document> iterable = dbcol.aggregate(newArrayList(geoNear, project, match));
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document doc) {
				try {
					final T obj = parseDocument(doc, type);
					features.add(Feature.builder()
							.property("name", obj.getLvlId())
							.geometry(obj.getLocation())
							.build());
				} catch (Exception e) {
					LOGGER.error("Failed to parse result, [collection='" + collection.getCollection() + "', objectId='" 
							+ doc.get("_id", ObjectId.class).toHexString() + "']", e);
				}
			}
		}, new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				if (t == null) future.set(features); else future.setException(t);
			}			
		});
		return future;
	}

	public <T extends LvlObject> ListenableFuture<FeatureCollection> fetchWithin(final LvlCollection<T> collection, final Class<T> type,
			final Polygon polygon) {
		checkArgument(polygon != null, "Uninitialized polygon");
		String payload = null;
		try {
			payload = JSON_MAPPER.writeValueAsString(polygon);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to parse polygon");
		}
		final SettableFuture<FeatureCollection> future = SettableFuture.create();
		final FeatureCollection features = FeatureCollection.builder().crs(Crs.builder().wgs84().build()).build();
		final MongoCollection<Document> dbcol = getCollection(collection);
		final Document geoWithin = new Document(LVL_LOCATION_FIELD, new Document(ImmutableMap.<String, Object>builder()
				.put("$geoWithin", new Document("$geometry", Document.parse(payload)))
				.build()));
		final FindIterable<Document> iterable = dbcol
				.find(geoWithin)
				.projection(fields(include(LVL_GUID_FIELD, LVL_LOCATION_FIELD)));
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document doc) {
				try {
					final T obj = parseDocument(doc, type);
					features.add(Feature.builder()
							.property("name", obj.getLvlId())
							.geometry(obj.getLocation())
							.build());
				} catch (Exception e) {
					LOGGER.error("Failed to parse result, [collection='" + collection.getCollection() + "', objectId='" 
							+ doc.get("_id", ObjectId.class).toHexString() + "']", e);
				}
			}
		}, new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				if (t == null) future.set(features); else future.setException(t);
			}
		});
		return future;
	}

	public <T extends LvlObject> ListenableFuture<List<String>> typeahead(final LvlCollection<T> collection, final Class<T> type,
			final String field, final String query, final int size) {
		String field2 = null, query2 = null;
		checkArgument(isNotBlank(field2 = trimToNull(field)), "Uninitialized or invalid field");
		checkArgument(isNotBlank(query2 = trimToNull(query)), "Uninitialized or invalid query");
		final int size2 = size > 0 ? size : 10;
		final SettableFuture<List<String>> future = SettableFuture.create();
		final List<String> values = newArrayList();
		final MongoCollection<Document> dbcol = getCollection(collection);
		final FindIterable<Document> iterable = dbcol
				.find(regex(field2, query2, "i"))
				.projection(fields(include(field2)))
				.sort(orderBy(ascending(field2)))
				.limit(size2);
		final String _field2 = field2;
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(final Document doc) {
				try {
					T obj = parseDocument(doc, type);
					final Object value = getSimpleProperty(obj, _field2);					
					if (value != null) values.add(value.toString());
				} catch (Exception e) {
					LOGGER.error("Failed to parse result, [collection='" + collection.getCollection() + "', objectId='" 
							+ doc.get("_id", ObjectId.class).toHexString() + "']", e);
				}
			}
		}, new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				if (t == null) future.set(values); else future.setException(t);
			}
		});
		return future;
	}

	/**
	 * Collects statistics about the specified collection.
	 * @param collection - collection from which the statistics are collected
	 * @return a listenable future which result is the status of the specified collection.
	 */	
	public <T extends LvlObject> ListenableFuture<MongoCollectionStats> stats(final LvlCollection<T> collection) {		
		final MongoCollection<Document> dbcol = getCollection(collection);
		final MongoCollectionStats stats = new MongoCollectionStats(collection.getCollection());
		// get indexes
		final SettableFuture<Void> indexesFuture = SettableFuture.create();
		dbcol.listIndexes().forEach(new Block<Document>() {
			@Override
			public void apply(final Document document) {
				stats.getIndexes().add(document.toJson());				
			}
		}, new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				if (t == null) indexesFuture.set(null); else indexesFuture.setException(t);
			}
		});
		// count elements
		final SettableFuture<Void> countFuture = SettableFuture.create();
		dbcol.count(new SingleResultCallback<Long>() {
			@Override
			public void onResult(final Long count, final Throwable t) {
				if (t == null) {
					stats.setCount(count);
					countFuture.set(null);
				} else countFuture.setException(t);
			}
		});
		// combine results
		final SettableFuture<MongoCollectionStats> future = SettableFuture.create();
		@SuppressWarnings("unchecked")
		final ListenableFuture<List<Void>> concatenated = successfulAsList(indexesFuture, countFuture);
		addCallback(concatenated, new FutureCallback<List<Void>>() {
			@Override
			public void onSuccess(final List<Void> result) {
				future.set(stats);
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}			
		});
		return future;
	}

	/* Helper methods */

	private <T extends LvlObject> MongoCollection<Document> getCollection(final LvlObject obj) {
		checkArgument(obj != null, "Uninitialized object");
		checkArgument(isNotBlank(obj.getCollection()), "Uninitialized or invalid collection");
		obj.getConfigurer().prepareCollection();
		final MongoDatabase db = client().getDatabase(CONFIG_MANAGER.getDbName());
		return db.getCollection(obj.getCollection());
	}	

	private <T extends LvlObject> MongoCollection<Document> getCollection(final LvlCollection<T> collection) {
		checkArgument(collection != null, "Uninitialized collection");
		checkArgument(isNotBlank(collection.getCollection()), "Uninitialized or invalid collection");
		collection.getConfigurer().prepareCollection();
		final MongoDatabase db = client().getDatabase(CONFIG_MANAGER.getDbName());
		return db.getCollection(collection.getCollection());
	}

	private <T extends LvlObject> Document parseObject(final LvlObject obj, final boolean checkFields, final boolean setDefaults) {
		checkArgument(obj != null, "Uninitialized object");
		if (checkFields) {
			checkArgument(isNotBlank(obj.getLvlId()), "Uninitialized or invalid primary key value");
		}
		if (setDefaults) {
			obj.setLastModified(null);
			obj.setActiveVersion(obj.getLvlId());
		}
		return Document.parse(obj.toJson());
	}

	private static <T extends LvlObject> T parseDocument(final Document doc, final Class<T> type) throws IOException {
		final T obj = JSON_MAPPER_COPY.readValue(doc.toJson(), type);
		obj.setDbId(doc.get("_id", ObjectId.class).toHexString());
		return obj;
	}

	private static final DeserializationProblemHandler DOC_DESERIALIZATION_PROBLEM_HANDLER = new DeserializationProblemHandler() {
		public boolean handleUnknownProperty(final DeserializationContext ctxt, final JsonParser jp, final JsonDeserializer<?> deserializer, 
				final Object beanOrClass, final String propertyName) throws IOException, JsonProcessingException {
			if ("_id".equals(propertyName) || "_dist".equals(propertyName)) {
				LOGGER.trace("Skipping auxiliary property '" + propertyName + ":" + jp.getCurrentToken().toString() + "', in class: " 
						+ beanOrClass.getClass().getCanonicalName());
				jp.skipChildren();
				return true;
			}
			return false;
		};
	};

	private static final SimpleModule JACKSON_MODULE = new SimpleModule("LeishVLModule", new Version(0, 3, 0, null, "eu.eubrazilcc.lvl", "lvl-storage")).
			addDeserializer(Date.class, new MongoDateDeserializer());

	private static final ObjectMapper JSON_MAPPER_COPY = JSON_MAPPER.copy().registerModule(JACKSON_MODULE).addHandler(DOC_DESERIALIZATION_PROBLEM_HANDLER);

	private static final Document ACTIVE_VERSION_HINT = new Document("$hint", new Document(LVL_ACTIVE_VERSION_FIELD, new BsonInt32(1)));
	private static final Bson LAST_MODIFIED_SORT_DESC = orderBy(descending(LVL_LAST_MODIFIED_FIELD));

	/* General methods */

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("mongoDB connector initialized successfully");
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
			LOGGER.info("mongoDB connector shutdown successfully");
		}
	}

}