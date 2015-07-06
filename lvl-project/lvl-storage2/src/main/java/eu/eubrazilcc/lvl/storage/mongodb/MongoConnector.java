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

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.successfulAsList;
import static com.google.common.util.concurrent.Futures.transform;
import static com.mongodb.MongoCredential.createMongoCRCredential;
import static com.mongodb.ReadPreference.nearest;
import static com.mongodb.WriteConcern.ACKNOWLEDGED;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.not;
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
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.storage.Filters.LogicalType.LOGICAL_AND;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_DENSE_IS_ACTIVE_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_GUID_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_LAST_MODIFIED_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_LOCATION_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_SPARSE_IS_ACTIVE_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_STATE_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.LVL_VERSION_FIELD;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.randomVersion;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.JSON_MAPPER;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static org.apache.commons.beanutils.PropertyUtils.getSimpleProperty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonElement;
import org.bson.BsonInt32;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
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
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.SettableFuture;
import com.mongodb.Block;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.AggregateIterable;
import com.mongodb.async.client.FindIterable;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.connection.ClusterSettings;

import eu.eubrazilcc.lvl.core.BaseFile;
import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
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

	public static int TYPEAHEAD_MAX_ITEMS = 10;

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
				// authentication settings (requires MongoDB server to be configured to support authentication)
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

	/**
	 * Creates the specified indexes in the collection.
	 * @param collection - the collection where the indexes will be created
	 * @param indexes - indexes to be created
	 * @return a future which result contains the names of the created indexes, or an exception when the method fails.
	 */
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
	 * Saves the specified object to the database, overriding the active version or creating a new entry in case that there are no matches 
	 * for the object GUID. This method will fail when the active version cannot be overridden.
	 * @param obj - object to save
	 * @param allowedTransitions - transitions between states
	 * @return a future which result indicates that the operation is successfully completed, or an exception when the method fails.
	 */
	public <T extends LvlObject> ListenableFuture<Void> saveActive(final T obj, final String... allowedTransitions) {		
		return save(obj, true, allowedTransitions);
	}

	/**
	 * Saves the specified object to the database, creating a new version. After the object is saved, this method will select the latest
	 * modified record as the new active version. In case that the saved object is not the latest modified, this method will not fail.
	 * @param obj - object to save
	 * @param allowedTransitions - transitions between states
	 * @return a future which result indicates that the operation is successfully completed, or an exception when the method fails.
	 */
	public <T extends LvlObject> ListenableFuture<Void> saveAsVersion(final T obj, final String... allowedTransitions) {
		// insert the object in the collection as a new version
		final ListenableFuture<Void> insertFuture = save(obj, false, allowedTransitions);
		// set the new active version to the latest modified record
		final ListenableFuture<Document> setActiveFuture = transform(insertFuture, new AsyncFunction<Void, Document>() {
			@Override
			public ListenableFuture<Document> apply(final Void insertResult) throws Exception {				
				return activateLastModified(getCollection(obj), obj.getLvlId());
			}			
		}, TASK_RUNNER.executor());
		// create response
		final SettableFuture<Void> future = SettableFuture.create();
		addCallback(setActiveFuture, new FutureCallback<Document>() {
			@Override
			public void onSuccess(final Document result) {
				if (result != null) {
					final String activeVersion = result.getString(LVL_VERSION_FIELD);
					final String thisVersion = obj.getVersion();
					if (!thisVersion.equals(activeVersion)) {
						LOGGER.info("The later modification '" + activeVersion + "' is the new active version instead of this '" + thisVersion 
								+ "' [collection= " + obj.getCollection() + ", lvlId=" + obj.getLvlId() + "]");
					}
					future.set(null);
				} else future.setException(new LvlObjectWriteException("No record was selected as the new active version"));
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}
		});
		return future;
	}

	/**
	 * Base method to save objects to the database.
	 * <p><b>Since the current version of the {@link MongoCollection#findOneAndUpdate(Bson, Bson, FindOneAndUpdateOptions, SingleResultCallback)} method does not
	 * provide support for index hints, this method will always use an index, like in the following example:</b></p>
	 * <p>Using to match the active objects {@link #matchActive(LvlObject)} will not use the sparse index. Therefore, the following filter 
	 * {@link #matchActive(LvlObject)} is preferred which includes a search on the alternative index.</p>
	 * @param obj - object to save
	 * @param overrideActive - set to <tt>true</tt> to override the active version of the object
	 * @param allowedTransitions - transitions between states
	 * @return a future which result indicates that the operation is successfully completed, or an exception when the method fails.
	 */
	private <T extends LvlObject> ListenableFuture<Void> save(final T obj, final boolean overrideActive, final String... allowedTransitions) {
		final SettableFuture<Void> future = SettableFuture.create();
		final MongoCollection<Document> dbcol = getCollection(obj);
		final String version = randomVersion();
		final List<String> states = (allowedTransitions != null ? asList(allowedTransitions) : Collections.<String>emptyList());
		// prepare query statement
		final Bson filter = (overrideActive ? (states.isEmpty() ? matchActive2(obj) : and(matchActive2(obj), matchStates(states))) 
				: (states.isEmpty() ? and(matchGuid(obj), matchVersion(version)) : and(matchGuid(obj), or(matchVersion(version), not(matchStates(states))))));
		final Document update = new Document(ImmutableMap.<String, Object>of("$set", parseObject(obj, overrideActive),
				"$currentDate", new Document(LVL_LAST_MODIFIED_FIELD, true),
				"$setOnInsert", new Document(LVL_VERSION_FIELD, version)));
		final FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().projection(fields(include(LVL_VERSION_FIELD, LVL_LAST_MODIFIED_FIELD)))
				.returnDocument(AFTER)
				.upsert(true);
		// execute the operation in the database
		dbcol.findOneAndUpdate(filter, update, options, new SingleResultCallback<Document>() {
			@Override
			public void onResult(final Document result, final Throwable t) {
				if (t == null) {
					if (result != null) {
						obj.setDbId(result.get("_id", ObjectId.class).toHexString());
						obj.setVersion(result.getString(LVL_VERSION_FIELD));
						obj.setLastModified(result.getDate(LVL_LAST_MODIFIED_FIELD));
						future.set(null);
					} else future.setException(new LvlObjectWriteException("No new records were inserted, no existing records were modified by the operation"));					
				} else if (t instanceof MongoException && ((MongoException)t).getMessage().matches("(?i:.*Cannot update \\'version\\'.*)")) {
					future.setException(new UnsupportedOperationException("Cannot make a transition between invalid states", t));
				} else future.setException(t);
			}
		});
		return future;
	}

	private <T extends LvlObject> ListenableFuture<Document> activateLastModified(final MongoCollection<Document> dbcol, final String lvlId) {
		// found and deactivate all versions of the specified object in the database
		final SettableFuture<Void> deactivateFuture = SettableFuture.create();
		final BsonDocument update = new BsonDocument("$unset", new BsonDocument(newArrayList(
				new BsonElement(LVL_SPARSE_IS_ACTIVE_FIELD, new BsonString("")), 
				new BsonElement(LVL_DENSE_IS_ACTIVE_FIELD, new BsonString("")))));
		final UpdateOptions options = new UpdateOptions().upsert(false);
		dbcol.updateMany(matchGuid(lvlId), update, options, new SingleResultCallback<UpdateResult>() {
			@Override
			public void onResult(final UpdateResult result, final Throwable t) {
				if (t == null) deactivateFuture.set(null); else deactivateFuture.setException(t);
			}
		});
		// set the new active version
		return transform(deactivateFuture, new AsyncFunction<Void, Document>() {
			@Override
			public ListenableFuture<Document> apply(final Void deactivateResult) throws Exception {				
				final SettableFuture<Document> future = SettableFuture.create();
				final BsonDocument update = new BsonDocument("$set", new BsonDocument(newArrayList(
						new BsonElement(LVL_SPARSE_IS_ACTIVE_FIELD, new BsonString(lvlId)),
						new BsonElement(LVL_DENSE_IS_ACTIVE_FIELD, new BsonString(lvlId)))));
				final FindOneAndUpdateOptions options2 = new FindOneAndUpdateOptions().sort(orderBy(descending(LVL_LAST_MODIFIED_FIELD)))
						.returnDocument(AFTER)
						.upsert(false);
				dbcol.findOneAndUpdate(matchGuid(lvlId), update, options2, new SingleResultCallback<Document>() {
					@Override
					public void onResult(final Document result, final Throwable t) {
						if (t == null) future.set(result); else future.setException(t);
					}
				});
				return future;
			}
		}, TASK_RUNNER.executor());		
	}

	/**
	 * Finds the active version of the specified object. In case that any of the object's versions are active, this method will return
	 * <tt>null</tt> to the caller.
	 * @param obj - the object to search for
	 * @param type - the object type
	 * @return a future which result contains the found object, or an exception when the method fails.
	 */
	public <T extends LvlObject> ListenableFuture<LvlObject> findActive(final LvlObject obj, final Class<T> type) {
		checkArgument(obj != null && obj.getClass().isAssignableFrom(type), "Uninitialized or invalid object type");
		checkArgument(isNotBlank(obj.getLvlId()), "Uninitialized or invalid primary key value");
		final SettableFuture<LvlObject> future = SettableFuture.create();
		final MongoCollection<Document> dbcol = getCollection(obj);
		dbcol.find(matchActive(obj)).modifiers(new BsonDocument("$hint", IS_ACTIVE_SPARSE_HINT)).sort(LAST_MODIFIED_SORT_DESC)
		.first(new SingleResultCallback<Document>() {
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
	 * Finds active elements in a collection. Since <tt>hint</tt> and <tt>text</tt> are mutually exclusive in a query expression, the sparse index will be 
	 * included only when no text search is involved in the query. Otherwise, the text index will be used.
	 * @see <a href="http://docs.mongodb.org/manual/reference/method/cursor.hint/#behavior">mongoDB -- cursor.hint()</a>
	 */
	public <T extends LvlObject> ListenableFuture<List<T>> findActive(final LvlCollection<T> collection, final Class<T> type, final int start, 
			final int size, final @Nullable Filters filters, final Map<String, Boolean> sorting, final @Nullable Map<String, Boolean> projections, 
			final @Nullable MutableLong totalCount, final @Nullable List<String> excludedStates) {		
		// parse input parameters
		boolean isText = false;
		final List<Bson> filterFields = newArrayList();
		if (filters != null && filters.getFilters() != null) {
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
					isText = true;
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
		final List<Bson> filterList = newArrayList(not(new BsonDocument(!isText ? LVL_SPARSE_IS_ACTIVE_FIELD : LVL_DENSE_IS_ACTIVE_FIELD, BsonNull.VALUE)));
		if (excludedStates != null && !excludedStates.isEmpty()) filterList.add(not(matchStates(excludedStates)));
		if (!filterFields.isEmpty()) filterList.add(LOGICAL_AND.equals(filters.getType()) ? and(filterFields) : or(filterFields));		
		final List<Bson> projectionFields = newArrayList();
		if (projections != null) {
			for (final Map.Entry<String, Boolean> projection : projections.entrySet()) {
				projectionFields.add(projection.getValue() ? include(projection.getKey()) : exclude(projection.getKey()));
			}
		}
		final List<Bson> sortFields = newArrayList();
		if (sorting != null) {
			for (final Map.Entry<String, Boolean> sort : sorting.entrySet()) {
				sortFields.add(sort.getValue() ? descending(sort.getKey()) : ascending(sort.getKey()));
			}
		}
		// prepare to operate on the collection
		final MongoCollection<Document> dbcol = getCollection(collection);		
		final List<T> page = newArrayList();
		final MutableLong __totalCount = new MutableLong(0l);
		// get total number of records
		final SettableFuture<Void> countFuture = SettableFuture.create();		
		addCallback(totalCount(dbcol, true, excludedStates), new FutureCallback<Long>() {
			@Override
			public void onSuccess(final Long result) {
				__totalCount.setValue(result);
				countFuture.set(null);
			}
			@Override
			public void onFailure(final Throwable t) {
				countFuture.setException(t);
			}			
		});
		// find records and populate the page
		final SettableFuture<Void> findFuture = SettableFuture.create();
		final FindIterable<Document> iterable = dbcol
				.find(and(filterList))
				.modifiers(!isText ? new BsonDocument("$hint", IS_ACTIVE_SPARSE_HINT) : new BsonDocument())
				.projection(fields(projectionFields))
				.sort(orderBy(sortFields))
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
		// combine the results, failing immediately if any of the futures fails
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

	public <T extends LvlObject> ListenableFuture<Boolean> delete(final T obj, final boolean includeVersions, final boolean deleteReferences) {
		checkArgument(obj != null, "Uninitialized or invalid object");
		checkArgument(isNotBlank(obj.getLvlId()), "Uninitialized or invalid primary key value");
		final SettableFuture<Boolean> deleteFuture = SettableFuture.create();
		final MongoCollection<Document> dbcol = getCollection(obj);		
		// find and delete records from the database
		final Bson filter = (includeVersions ? matchGuid(obj) : matchActive2(obj));
		dbcol.deleteMany(filter, new SingleResultCallback<DeleteResult>() {
			@Override
			public void onResult(final DeleteResult result, final Throwable t) {
				if (t == null) {
					if (result.getDeletedCount() < 1l) {
						deleteFuture.set(false);
					} else if (result.getDeletedCount() == 1l || includeVersions) {
						// cascade delete											
						if (deleteReferences) {
							class DeleteTask extends CancellableTask<Void> {
								public DeleteTask() {
									super(null);
									this.task = ListenableFutureTask.create(new Callable<Void>() {
										@Override
										public Void call() throws Exception {
											deleteReferences(obj.getReferences());
											return null;
										}										
									});
								}
							}
							TASK_RUNNER.execute(new DeleteTask()); 
						}
						deleteFuture.set(true);
					} else {
						deleteFuture.setException(new IllegalStateException("Multiple records were deleted: " + result.getDeletedCount()));
					}
				} else deleteFuture.setException(t);
			}
		});
		// set the new active version to the latest modified record		
		return transform(deleteFuture, new AsyncFunction<Boolean, Boolean>() {
			@Override
			public ListenableFuture<Boolean> apply(final Boolean deleteResult) throws Exception {
				final SettableFuture<Boolean> activeFuture = SettableFuture.create();
				if (fromNullable(deleteResult).or(false) && includeVersions) { 
					addCallback(activateLastModified(getCollection(obj), obj.getLvlId()), new FutureCallback<Document>() {
						@Override
						public void onSuccess(final Document result) {
							if (LOGGER.isTraceEnabled()) {
								if (result != null) LOGGER.trace("The new active version is '" + result.getString(LVL_VERSION_FIELD) + "' [collection= " 
										+ obj.getCollection() + ", lvlId=" + obj.getLvlId() + "]");					
								else LOGGER.trace("No record was selected as the new active version [collection= " 
										+ obj.getCollection() + ", lvlId=" + obj.getLvlId() + "]");
							}
							activeFuture.set(true);
						}
						@Override
						public void onFailure(final Throwable t) {
							activeFuture.setException(t);
						}
					});
				} else activeFuture.set(true);
				return activeFuture;
			}			
		}, TASK_RUNNER.executor());			
	}

	private void deleteReferences(final Map<String, List<String>> references) {		
		if (references != null) {
			// TODO : retrieve 
		}
	}

	/**
	 * Counts the total number of active elements in the specified collection.
	 */
	public <T extends LvlObject> ListenableFuture<Long> totalCount(final LvlCollection<T> collection, final @Nullable List<String> excludedStates) {		
		return totalCount(getCollection(collection), true, excludedStates);
	}

	private <T extends LvlObject> ListenableFuture<Long> totalCount(final MongoCollection<Document> dbcol, final boolean onlyActive, 
			final @Nullable List<String> excludedStates) {
		final SettableFuture<Long> countFuture = SettableFuture.create();
		final Bson activeFilter = (onlyActive ? not(new BsonDocument(LVL_SPARSE_IS_ACTIVE_FIELD, BsonNull.VALUE)) : null);
		final Bson statesFilter = (excludedStates != null && !excludedStates.isEmpty()) ? not(matchStates(excludedStates)) : null;
		final Bson filter = (activeFilter != null && statesFilter != null) ? and(activeFilter, statesFilter) 
				: (activeFilter != null ? activeFilter : (statesFilter != null ? statesFilter : new BsonDocument()));
		final CountOptions options = new CountOptions().hint(IS_ACTIVE_SPARSE_HINT);
		dbcol.count(filter, options, new SingleResultCallback<Long>() {
			@Override
			public void onResult(final Long result, final Throwable t) {
				if (t == null) countFuture.set(result); else countFuture.setException(t);
			}
		});
		return countFuture;
	}	

	/**
	 * Fetches the location of the active elements from the database, returning a collection of GeoJSON points to the caller. The points
	 * are annotated with the GUID of the corresponding element.
	 */
	public <T extends LvlObject> ListenableFuture<FeatureCollection> fetchNear(final LvlCollection<T> collection, final Class<T> type, final double longitude, 
			final double latitude, final double minDistance, final double maxDistance, final @Nullable List<String> excludedStates) {
		final SettableFuture<FeatureCollection> future = SettableFuture.create();
		final FeatureCollection features = FeatureCollection.builder().crs(Crs.builder().wgs84().build()).build();
		final MongoCollection<Document> dbcol = getCollection(collection);
		// prepare query
		final List<Document> statements = newArrayList(new Document(LVL_DENSE_IS_ACTIVE_FIELD, new BsonDocument("$ne", BsonNull.VALUE)));
		if ((excludedStates != null && !excludedStates.isEmpty())) statements.add(new Document(LVL_STATE_FIELD, 
				new Document("$not", new Document("$in", excludedStates))));
		final Document query = (statements.size() > 1 ? new Document("$and", statements) : statements.get(0));
		// prepare geolocation query
		final Document geoNear = new Document("$geoNear", new Document(ImmutableMap.<String, Object>builder()
				.put("spherical", true)
				.put("num", new BsonInt32(Integer.MAX_VALUE))
				.put("maxDistance", maxDistance)
				.put("query", query)
				.put("near", new Document("type", "Point").append("coordinates", new BsonArray(newArrayList(new BsonDouble(longitude), new BsonDouble(latitude)))))
				.put("distanceField", "_dist.calculated")
				.build()));
		final Bson project = new SimplePipelineStage("$project", fields(include(LVL_GUID_FIELD, LVL_LOCATION_FIELD, "_dist")));
		final Bson match = new SimplePipelineStage("$match", gte("_dist.calculated", new BsonDouble(minDistance)));
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

	/**
	 * Fetches the location of the active elements from the database, returning a collection of GeoJSON points to the caller. The points are 
	 * annotated with the GUID of the corresponding element.
	 */
	public <T extends LvlObject> ListenableFuture<FeatureCollection> fetchWithin(final LvlCollection<T> collection, final Class<T> type, final Polygon polygon, 
			final @Nullable List<String> excludedStates) {
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
		final Document geoWithin = new Document(LVL_LOCATION_FIELD, new Document(ImmutableMap.<String, Object>of("$geoWithin", 
				new Document("$geometry", Document.parse(payload)))));
		final Bson activeFilter = not(new BsonDocument(LVL_SPARSE_IS_ACTIVE_FIELD, BsonNull.VALUE));
		final Bson statesFilter = (excludedStates != null && !excludedStates.isEmpty()) ? not(matchStates(excludedStates)) : new BsonDocument();		
		final FindIterable<Document> iterable = dbcol
				.find(and(geoWithin, activeFilter, statesFilter))
				.modifiers(new BsonDocument("$hint", IS_ACTIVE_SPARSE_HINT))
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

	/**
	 * Searches for text elements within a collection.
	 */
	public <T extends LvlObject> ListenableFuture<List<String>> typeahead(final LvlCollection<T> collection, final Class<T> type, final String field, 
			final String query, final int size, final @Nullable List<String> excludedStates) {
		String field2 = null, query2 = null;
		checkArgument(isNotBlank(field2 = trimToNull(field)), "Uninitialized or invalid field");
		checkArgument(isNotBlank(query2 = trimToNull(query)), "Uninitialized or invalid query");
		final int size2 = size > 0 ? size : TYPEAHEAD_MAX_ITEMS;
		final SettableFuture<List<String>> future = SettableFuture.create();
		final List<String> values = newArrayList();
		final MongoCollection<Document> dbcol = getCollection(collection);
		final Bson statesFilter = (excludedStates != null && !excludedStates.isEmpty()) ? not(matchStates(excludedStates)) : null;
		final Bson typeaheadFilter = regex(field2, query2, "i");
		final Bson filter = (statesFilter != null ? and(typeaheadFilter, statesFilter) : typeaheadFilter);
		final FindIterable<Document> iterable = dbcol
				.find(filter)
				.modifiers(new BsonDocument("$hint", IS_ACTIVE_SPARSE_HINT))
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
	 * @return a future which result is the status of the specified collection.
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
		// combine the results, ignoring when any of the futures fails (returned statistics can be incomplete)
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

	public <T extends LvlObject> ListenableFuture<Void> drop(final LvlCollection<T> collection) {
		final MongoCollection<Document> dbcol = getCollection(collection);
		final SettableFuture<Void> future = SettableFuture.create();
		dbcol.drop(new SingleResultCallback<Void>() {
			@Override
			public void onResult(final Void result, final Throwable t) {
				if (t == null) {
					collection.getConfigurer().unconfigure();
					future.set(null);
				} else future.setException(t);
			}			
		});
		return future;
	}

	/* Helper methods */

	private Bson matchGuid(final LvlObject obj) {
		return matchGuid(obj.getLvlId());
	}

	private Bson matchGuid(final String lvlId) {
		return eq(LVL_GUID_FIELD, lvlId);
	}

	private Bson matchVersion(final String version) {
		return eq(LVL_VERSION_FIELD, version);
	}

	private Bson matchActive(final LvlObject obj) {
		return eq(LVL_SPARSE_IS_ACTIVE_FIELD, obj.getLvlId());
	}

	private Bson matchActive2(final LvlObject obj) {
		return eq(LVL_DENSE_IS_ACTIVE_FIELD, obj.getLvlId());
	}

	private Bson matchStates(final List<String> states) {
		return in(LVL_STATE_FIELD, states);
	}

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

	private <T extends LvlObject> Document parseObject(final LvlObject obj, final boolean overrideActive) {
		checkArgument(obj != null, "Uninitialized object");
		checkArgument(isNotBlank(obj.getLvlId()), "Uninitialized or invalid primary key value");
		if (overrideActive) {
			obj.setIsActive(obj.getLvlId());
			obj.setIsActive2(obj.getLvlId());
		}
		final Document doc = Document.parse(obj.toJson());
		doc.remove(LVL_LAST_MODIFIED_FIELD);
		if (!overrideActive) {
			doc.remove(LVL_SPARSE_IS_ACTIVE_FIELD);
			doc.remove(LVL_DENSE_IS_ACTIVE_FIELD);
			doc.remove(LVL_VERSION_FIELD);
		}
		return doc;
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

	private static final BsonDocument IS_ACTIVE_SPARSE_HINT = new BsonDocument(LVL_SPARSE_IS_ACTIVE_FIELD, new BsonInt32(1));
	private static final Bson LAST_MODIFIED_SORT_DESC = orderBy(descending(LVL_LAST_MODIFIED_FIELD));

	private static class SimplePipelineStage implements Bson {
		private final String name;
		private final Bson value;

		public SimplePipelineStage(final String name, final Bson value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
			return new BsonDocument(name, value.toBsonDocument(documentClass, codecRegistry));
		}
	}

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