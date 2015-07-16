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
import static com.mongodb.MongoCredential.createMongoCRCredential;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.storage.Filter.FilterType.FILTER_EXISTS;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.hash2bucket;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.SettableFuture;
import com.mongodb.BasicDBObject;
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
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.storage.Filter;
import eu.eubrazilcc.lvl.storage.Filters;
import eu.eubrazilcc.lvl.storage.LvlObjectNotFoundException;
import eu.eubrazilcc.lvl.storage.base.LvlFile;
import eu.eubrazilcc.lvl.storage.base.LvlFiles;
import eu.eubrazilcc.lvl.storage.base.Metadata;
import eu.eubrazilcc.lvl.storage.base.Metadata.OpenAccess;
import eu.eubrazilcc.lvl.storage.mongodb.cache.CachedGridFSFile;
import eu.eubrazilcc.lvl.storage.mongodb.cache.GridFSFilePersistingCache;

/**
 * File connector based on mongoDB. <strong>Note </strong> that this class uses the legacy mongoDB driver, which should be updated when GridFS
 * becomes available in the new driver. 
 * @author Erik Torres <ertorser@upv.es>
 */
@Deprecated
public enum MongoFileConnector implements Closeable2 {

	MONGODB_FILE_CONN;

	private static final Logger LOGGER = getLogger(MongoFileConnector.class);

	public static final int NUM_BUCKETS = 16;
	private static final String BUCKETS_PADDING = "%02d";

	private final GridFSFilePersistingCache gfsPersistingCache = new GridFSFilePersistingCache();

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
				// enable/disable authentication (requires MongoDB server to be configured to support authentication)
				final List<MongoCredential> credentials = newArrayList();
				if (!CONFIG_MANAGER.isAnonymousDbAccess()) {
					credentials.add(createMongoCRCredential(CONFIG_MANAGER.getDbUsername(), 
							CONFIG_MANAGER.getDbName(), CONFIG_MANAGER.getDbPassword().toCharArray()));
				}
				// create client
				final MongoClientOptions options = MongoClientOptions.builder()
						.readPreference(ReadPreference.nearest())
						.writeConcern(WriteConcern.ACKNOWLEDGED).build();
				__client = new MongoClient(seeds, credentials, options);				
			}
			return __client;
		} finally {
			mutex.unlock();
		}
	}

	public <T extends LvlFile> ListenableFuture<Void> saveFile(final T obj, final File srcFile, final boolean override) {
		checkArgument(srcFile != null && srcFile.isFile() && srcFile.canRead(), "Uninitialized or invalid input file");		
		return (override ? deleteAndSave(obj, srcFile) : saveFile(obj, srcFile));		
	}

	private <T extends LvlFile> ListenableFuture<Void> deleteAndSave(final T obj, final File srcFile) {
		final SettableFuture<Void> future = SettableFuture.create();		
		final ListenableFuture<Boolean> deleteFuture = removeFile(obj);
		addCallback(deleteFuture, new FutureCallback<Boolean>() {
			@Override
			public void onSuccess(final Boolean result) {
				final ListenableFuture<Void> saveFuture = saveFile(obj, srcFile);
				addCallback(saveFuture, new FutureCallback<Void>() {
					public void onSuccess(final Void result) {
						future.set(result);
					}
					public void onFailure(final Throwable t) {
						future.setException(t);
					}
				});
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}			
		});
		return future;
	}

	private <T extends LvlFile> ListenableFuture<Void> saveFile(final T obj, final File srcFile) {		
		final CancellableTask<Void> task = new MongoTask<Void>(ListenableFutureTask.create(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final Connection conn = Connection.newBucket(client(), obj);		
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				obj.getConfigurer().prepareFiles(gridFS.getBucketName());
				// save file to the database
				final GridFSInputFile gfsFile = gridFS.createFile(srcFile);
				gfsFile.setFilename(conn.filename);
				gfsFile.setContentType(mimeType(srcFile));				
				gfsFile.setMetaData(fromMetadata(obj.getMetadata()));
				gfsFile.save();
				// update object
				obj.setId(ObjectId.class.cast(gfsFile.getId()).toString());
				obj.setLength(gfsFile.getLength());
				obj.setChunkSize(gfsFile.getChunkSize());
				obj.setUploadDate(gfsFile.getUploadDate());
				obj.setMd5(gfsFile.getMD5());
				obj.setFilename(gfsFile.getFilename());		
				obj.setContentType(gfsFile.getContentType());
				return null;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	public <T extends LvlFile> ListenableFuture<LvlFile> fetchFile(final LvlFile obj, final Class<T> type) {
		checkArgument(obj != null && obj.getClass().isAssignableFrom(type), "Uninitialized or invalid object type");
		final CancellableTask<LvlFile> task = new MongoTask<LvlFile>(ListenableFutureTask.create(new Callable<LvlFile>() {
			@Override
			public LvlFile call() throws Exception {
				final Connection conn = Connection.newBucket(client(), obj);		
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final GridFSDBFile gfsFile = gridFS.findOne(conn.filename);
				if (gfsFile == null) throw new LvlObjectNotFoundException("Object not found");
				T result = null;
				try {
					result = parseGridFSDBFile(gfsFile, type);
					CachedGridFSFile cachedFile = gfsPersistingCache.getIfPresent(gfsFile.getFilename());
					if (cachedFile != null) {
						if (!cachedFile.getMd5().equals(gfsFile.getMD5())) {
							cachedFile = gfsPersistingCache.update(gfsFile);
						}
					} else {
						cachedFile = gfsPersistingCache.put(gfsFile);
					}
					result.setOutfile(new File(cachedFile.getCachedFilename()));
				} catch (Exception e) {
					throw new IllegalStateException("Failed to fetch file [bucket=" + gridFS.getBucketName() + ", filename=" + conn.filename + "]", e);
				}	
				return result;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();		
	}

	/**
	 * Checks whether or not the specified file exists in the database, returning <tt>true</tt> only when the file exists in the 
	 * specified namespace.
	 * @param obj - file object to be searched for in the database
	 * @return
	 */
	public <T extends LvlFile> ListenableFuture<Boolean> fileExists(final T obj) {
		final CancellableTask<Boolean> task = new MongoTask<Boolean>(ListenableFutureTask.create(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				final Connection conn = Connection.newBucket(client(), obj);		
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				return gridFS.findOne(conn.filename) != null;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	public <T extends LvlFile> ListenableFuture<List<T>> fetchFiles(final LvlFiles<T> files, final Class<T> type, final int start, final int size, 
			final @Nullable Filters filters, final @Nullable Map<String, Boolean> sorting, final @Nullable MutableLong totalCount) {
		final SettableFuture<List<T>> future = SettableFuture.create();
		final List<T> list = newArrayList();
		final CancellableTask<Void> task = new MongoTask<Void>(ListenableFutureTask.create(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				// parse input parameters
				final BasicDBObject filterFields = parseFilters(filters);
				final BasicDBObject sortFields = parseSorting(sorting);
				// operate on the bucket
				final Connection conn = Connection.newBucket(client(), files);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final DBCursor cursor = gridFS.getFileList(filterFields, sortFields);
				cursor.skip(start).limit(size);
				try {
					while (cursor.hasNext()) {
						list.add(parseGridFSDBFile((GridFSDBFile) cursor.next(), type));
					}
				} finally {
					cursor.close();
				}
				if (totalCount != null) {				
					totalCount.setValue(cursor.count());
				}
				return null;
			}
		}));
		addCallback(task.getTask(), new FutureCallback<Void>() {
			@Override
			public void onSuccess(final Void result) {
				future.set(list);
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}
		});
		TASK_RUNNER.execute(task);
		return future;
	}

	/**
	 * Removes a file from the database.
	 * @param namespace - (optional) name space where the file was stored under. When nothing specified, the default bucket is used
	 * @param filename - filename to be removed from the database
	 */
	public <T extends LvlFile> ListenableFuture<Boolean> removeFile(final T obj) {
		final CancellableTask<Boolean> task = new MongoTask<Boolean>(ListenableFutureTask.create(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				final Connection conn = Connection.newBucket(client(), obj);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				gridFS.remove(conn.filename);
				return true;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	public <T extends LvlFile> ListenableFuture<Void> updateMetadata(final T obj) {
		final CancellableTask<Void> task = new MongoTask<Void>(ListenableFutureTask.create(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final Connection conn = Connection.newBucket(client(), obj);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final GridFSDBFile gfsFile = gridFS.findOne(conn.filename);
				if (gfsFile == null) throw new LvlObjectNotFoundException("Object not found");
				gfsFile.setMetaData(fromMetadata(obj.getMetadata()));
				gfsFile.save();
				// update object
				obj.setId(ObjectId.class.cast(gfsFile.getId()).toString());
				obj.setLength(gfsFile.getLength());
				obj.setChunkSize(gfsFile.getChunkSize());
				obj.setUploadDate(gfsFile.getUploadDate());
				obj.setMd5(gfsFile.getMD5());
				obj.setFilename(gfsFile.getFilename());		
				obj.setContentType(gfsFile.getContentType());
				return null;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	public <T extends LvlFile> ListenableFuture<Void> createOpenAccessLink(final T obj) {
		checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
		final OpenAccess openAccess = OpenAccess.builder()
				.bucket(Connection.getBucketNumber(obj).orNull())
				.secret(random(16, "abcdefghijklmnopqrstuvwxyz0123456789"))
				.date(new Date())
				.build();
		obj.getMetadata().setOpenAccess(openAccess);
		return updateMetadata(obj);
	}

	public <T extends LvlFile> ListenableFuture<Void> removeOpenAccessLink(final T obj) {
		checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
		obj.getMetadata().setOpenAccess(null);
		return updateMetadata(obj);
	}

	public <T extends LvlFile> ListenableFuture<List<T>> findOpenAccess(final LvlFiles<T> files, final Class<T> type, final int start, 
			final int size, final @Nullable Map<String, Boolean> sorting, final @Nullable MutableLong totalCount) {
		final Filters filters = Filters.builder()
				.filters(newArrayList(Filter.builder().fieldName("metadata.openAccess.secret").type(FILTER_EXISTS).value("true").build()))
				.build();
		return fetchFiles(files, type, start, size, filters, sorting, totalCount);		
	}

	public <T extends LvlFile> ListenableFuture<LvlFile> fetchOpenAccessFile(final LvlFile obj, final Class<T> type) {
		checkArgument(obj != null && obj.getClass().isAssignableFrom(type), "Uninitialized or invalid object type");
		final CancellableTask<LvlFile> task = new MongoTask<LvlFile>(ListenableFutureTask.create(new Callable<LvlFile>() {
			@Override
			public LvlFile call() throws Exception {
				final Connection conn = Connection.newBucketOpenAccess(client(), obj);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final GridFSDBFile gfsFile = gridFS.findOne(new BasicDBObject("metadata.filename", conn.filename).append("metadata.openAccess.secret", conn.secret));
				if (gfsFile == null) throw new LvlObjectNotFoundException("Object not found");
				T result = null;
				try {
					result = parseGridFSDBFile(gfsFile, type);
					CachedGridFSFile cachedFile = gfsPersistingCache.getIfPresent(gfsFile.getFilename());
					if (cachedFile != null) {
						if (!cachedFile.getMd5().equals(gfsFile.getMD5())) {
							cachedFile = gfsPersistingCache.update(gfsFile);
						}
					} else {
						cachedFile = gfsPersistingCache.put(gfsFile);
					}
					result.setOutfile(new File(cachedFile.getCachedFilename()));
				} catch (Exception e) {
					throw new IllegalStateException("Failed to fetch open access file [bucket=" + gridFS.getBucketName() + ", filename=" + conn.filename 
							+ ", secret=" + conn.secret + "]", e);
				}
				return result;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();		
	}

	public <T extends LvlFile> ListenableFuture<List<String>> typeaheadFile(final LvlFiles<T> files, final String query, final int size) {
		final CancellableTask<List<String>> task = new MongoTask<List<String>>(ListenableFutureTask.create(new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				final Connection conn = Connection.newBucket(client(), files);
				checkArgument(isNotBlank(query), "Uninitialized or invalid query");
				final String query2 = query.trim();
				final List<String> list = newArrayList();
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final DBCursor cursor = gridFS.getFileList(new BasicDBObject("metadata.filename", new BasicDBObject("$regex", query2).append("$options", "i")), 
						new BasicDBObject("filename", 1));
				cursor.skip(0).limit(size);
				try {
					while (cursor.hasNext()) {
						list.add((String)((GridFSDBFile) cursor.next()).getMetaData().get("filename"));
					}
				} finally {
					cursor.close();
				}		
				return list;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	public <T extends LvlFile> ListenableFuture<Long> totalCount(final LvlFiles<T> files) {
		final CancellableTask<Long> task = new MongoTask<Long>(ListenableFutureTask.create(new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				final Connection conn = Connection.newBucket(client(), files);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				return conn.db.getCollection(gridFS.getBucketName() + ".files").count();
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	public <T extends LvlFile> ListenableFuture<MongoCollectionStats> statsFiles(final LvlFiles<T> files) {
		final CancellableTask<MongoCollectionStats> task = new MongoTask<MongoCollectionStats>(ListenableFutureTask.create(new Callable<MongoCollectionStats>() {
			@Override
			public MongoCollectionStats call() throws Exception {
				final Connection conn = Connection.newBucket(client(), files);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final DBCollection dbcol = conn.db.getCollection(gridFS.getBucketName() + ".files");
				final MongoCollectionStats stats = new MongoCollectionStats(dbcol.getName());
				final List<DBObject> indexes = dbcol.getIndexInfo();
				for (final DBObject idx : indexes) {
					stats.getIndexes().add(idx.toString());
				}
				stats.setCount(dbcol.getCount());
				return stats;
			}
		}));
		TASK_RUNNER.execute(task);
		return task.getTask();
	}

	/* Helper methods */

	private static <T extends LvlFile> T parseGridFSDBFile(final GridFSDBFile gfsFile, final Class<T> type) throws IOException {
		final ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>()
				.put("id", ObjectId.class.cast(gfsFile.getId()).toString())
				.put("length", gfsFile.getLength())
				.put("chunkSize", gfsFile.getChunkSize())
				.put("uploadDate", gfsFile.getUploadDate())
				.put("md5", gfsFile.getMD5())
				.put("filename", gfsFile.getFilename())
				.put("contentType", gfsFile.getContentType())				
				.put("metadata", toMetadata(gfsFile.getMetaData()));
		if (gfsFile.getAliases() != null) {
			builder.put("aliases", gfsFile.getAliases());
		}
		final T obj = JSON_MAPPER.readValue(objectToJson(builder.build()), type);
		return obj;
	}

	private DBObject fromMetadata(final Metadata metadata) {
		DBObject obj = null;
		if (metadata != null) {
			try {
				obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(metadata));
			} catch (JsonProcessingException e) {
				LOGGER.error("Failed to write dataset to DB object", e);
			}
		}
		return obj;		
	}

	private static Metadata toMetadata(final DBObject obj) {
		Metadata metadata = null;
		if (obj != null) {
			try {
				metadata = JSON_MAPPER.readValue(obj.toString(), Metadata.class);		
			} catch (IOException e) {
				LOGGER.error("Failed to read dataset from DB object", e);
			}
		}
		return metadata;
	}

	private BasicDBObject parseFilters(final @Nullable Filters filters) {
		final ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<>();
		if (filters != null && filters.getFilters() != null && !filters.getFilters().isEmpty()) {
			String field = null;
			for (final Filter filter : filters.getFilters()) {
				if (isNotBlank(field = trimToNull(filter.getFieldName()))) {
					switch (filter.getType()) {
					case FILTER_EXISTS:
						builder.put(field, new BasicDBObject("$exists", parseBoolean(filter.getValue())));
						break;
					default:
						throw new UnsupportedOperationException("Currently unsupported filter type: " + filter.getType());
					}
				}
			}
		}
		return new BasicDBObject(builder.build());
	}

	private BasicDBObject parseSorting(final @Nullable Map<String, Boolean> sorting) {
		final ImmutableMap.Builder<String, Integer> builder = new ImmutableMap.Builder<>();
		if (sorting != null && !sorting.isEmpty()) {
			String field = null;
			boolean descending;
			for (final Map.Entry<String, Boolean> entry : sorting.entrySet()) {				
				if (isNotBlank(field = trimToNull(entry.getKey()))) {
					descending = entry.getValue() != null && entry.getValue().booleanValue();
					builder.put(field, descending ? -1 : 1);
				}
			}
		} else {
			// insertion order
			builder.put("uploadDate", -1);
		}		
		return new BasicDBObject(builder.build());
	}

	/* General methods */

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("mongoDB file connector initialized successfully");
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
			LOGGER.info("mongoDB file connector shutdown successfully");
		}
	}

	/* Inner classes */

	private static class Connection {
		private final DB db;
		private final String bucket;
		private final String filename;
		private final String secret;
		public Connection(final DB db, final String bucket, final String filename, final String secret) {
			this.db = db;
			this.bucket = bucket;
			this.filename = filename;
			this.secret = secret;
		}
		public static <T extends LvlFile> Connection newBucket(final MongoClient client, final T obj) {
			final DB db = client.getDB(CONFIG_MANAGER.getDbName());
			final String bucket = getBucket(obj).orNull();
			final String filename = getBucketFilename(obj);
			return new Connection(db, bucket, filename, null);
		}
		public static <T extends LvlFile> Connection newBucket(final MongoClient client, final LvlFiles<T> files) {
			final DB db = client.getDB(CONFIG_MANAGER.getDbName());
			final String bucket = getBucket(files).orNull();
			return new Connection(db, bucket, null, null);
		}
		public static <T extends LvlFile> Connection newBucketOpenAccess(final MongoClient client, final T obj) {
			final DB db = client.getDB(CONFIG_MANAGER.getDbName());
			final String bucket = getOpenAccessBucket(obj);
			final String filename = getBucketFilename(obj, false);
			final String secret = getOpenAccessSecret(obj);
			return new Connection(db, bucket, filename, secret);
		}
		private static <T extends LvlFile> Optional<Integer> getBucketNumber(final T obj) {
			checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
			Integer bucket = null;
			String namespace = null;
			if (isNotBlank(namespace = trimToNull(obj.getMetadata().getNamespace()))) {
				bucket = hash2bucket(namespace, NUM_BUCKETS);
			}
			return fromNullable(bucket);
		}
		private static <T extends LvlFile> Optional<String> getBucket(final T obj) {
			checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
			String bucket = null, namespace = null;
			if (isNotBlank(namespace = trimToNull(obj.getMetadata().getNamespace()))) {
				bucket = "fs_" + String.format(BUCKETS_PADDING, hash2bucket(namespace, NUM_BUCKETS));
			}
			return fromNullable(bucket);
		}
		private static <T extends LvlFile> Optional<String> getBucket(final LvlFiles<T> files) {			
			checkArgument(files != null, "Uninitialized or invalid files");
			String bucket = null, namespace = null;
			if (isNotBlank(namespace = trimToNull(files.getNamespace()))) {
				bucket = "fs_" + String.format(BUCKETS_PADDING, hash2bucket(namespace, NUM_BUCKETS));
			}
			return fromNullable(bucket);
		}
		private static <T extends LvlFile> String getBucketFilename(final T obj) {
			return getBucketFilename(obj, true);
		}
		private static <T extends LvlFile> String getBucketFilename(final T obj, final boolean addNamespace) {
			checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
			String filename = null;
			checkArgument(isNotBlank(filename = trimToNull(obj.getMetadata().getFilename())), "Uninitialized or invalid file object");
			if (addNamespace) {
				String namespace = null;
				if (isNotBlank(namespace = trimToNull(obj.getMetadata().getNamespace()))) {
					filename = namespace + ":" + filename;
				}
			}
			return filename;
		}
		private static <T extends LvlFile> String getOpenAccessBucket(final T obj) {
			checkArgument(obj != null && obj.getMetadata() != null && obj.getMetadata().getOpenAccess() != null, "Uninitialized or invalid file object");
			Integer bucket = null;
			checkArgument((bucket = obj.getMetadata().getOpenAccess().getBucket()) != null, "Uninitialized or invalid file object");
			return "fs_" + String.format(BUCKETS_PADDING, bucket);
		}
		private static <T extends LvlFile> String getOpenAccessSecret(final T obj) {
			checkArgument(obj != null && obj.getMetadata() != null && obj.getMetadata().getOpenAccess() != null, "Uninitialized or invalid file object");
			String secret = null;
			checkArgument(isNotBlank(secret = trimToNull(obj.getMetadata().getOpenAccess().getSecret())), "Uninitialized or invalid file object");
			return secret;
		}
	}

	private static class MongoTask<T> extends CancellableTask<T> {
		public MongoTask(final ListenableFutureTask<T> task) {
			super(null);
			this.task = task;
		}
	}

}