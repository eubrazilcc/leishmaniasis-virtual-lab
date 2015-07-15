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
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.hash2bucket;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import eu.eubrazilcc.lvl.storage.LvlObjectNotFoundException;
import eu.eubrazilcc.lvl.storage.base.LvlFile;
import eu.eubrazilcc.lvl.storage.base.Metadata;
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
		final ListenableFuture<Void> deleteFuture = removeFile(obj);
		addCallback(deleteFuture, new FutureCallback<Void>() {
			@Override
			public void onSuccess(final Void result) {				
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

	/**
	 * Removes a file from the database.
	 * @param namespace - (optional) name space where the file was stored under. When nothing specified, the default bucket is used
	 * @param filename - filename to be removed from the database
	 */
	public <T extends LvlFile> ListenableFuture<Void> removeFile(final T obj) {
		final CancellableTask<Void> task = new MongoTask<Void>(ListenableFutureTask.create(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final Connection conn = Connection.newBucket(client(), obj);
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				gridFS.remove(conn.filename);
				return null;
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

	public <T extends LvlFile> ListenableFuture<List<String>> typeaheadFile(final T obj, final String query, final int size) {
		final CancellableTask<List<String>> task = new MongoTask<List<String>>(ListenableFutureTask.create(new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				final Connection conn = Connection.newBucketNoFilename(client(), obj);		
				checkArgument(isNotBlank(query), "Uninitialized or invalid query");
				final String query2 = query.trim();
				final List<String> list = newArrayList();
				final GridFS gridFS = (conn.bucket != null ? new GridFS(conn.db, conn.bucket) : new GridFS(conn.db));
				final DBCursor cursor = gridFS.getFileList(new BasicDBObject("filename", new BasicDBObject("$regex", query2).append("$options", "i")), 
						new BasicDBObject("filename", 1));
				cursor.skip(0).limit(size);
				try {
					while (cursor.hasNext()) {
						list.add(((GridFSDBFile) cursor.next()).getFilename());
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

	/* TODO

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
			} *
	}

	/**
	 * Reads a file object from the current database. The file is identified by the original filename stored in the database and the 
	 * name space under the file was stored. When several versions exist of the same file, the latest version will be retrieved.
	 * @param namespace - (optional) name space to be searched for in the database. When nothing specified, the default bucket is used
	 * @param filename - filename to be searched for in the database
	 * @param output - file where the output will be saved, this file must exists and must be writable
	 *
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
	 * Lists all the files in the specified name space. Only latest versions are included in the list.
	 * @param namespace - (optional) name space to be searched for files. When nothing specified, the default bucket is used
	 * @param sortCriteria - objects in the collection are sorted with this criteria
	 * @param start - starting index
	 * @param size - maximum number of objects returned
	 * @param count - (optional) is updated with the number of objects in the database
	 * @return a view of the files stored under the specified name space that contains the specified range.
	 *
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
	 * Counts the files stored in the specified name space.
	 * @param namespace - (optional) name space whose files are counted
	 * @return the number of objects stored in the collection
	 *
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
	 *
	public void statsFiles(final OutputStream os, final @Nullable String namespace) throws IOException {
		String namespace2 = trimToNull(namespace);
		if (namespace2 == null) {
			final DB db = client().getDB(CONFIG_MANAGER.getDbName());
			final GridFS gfsNs = new GridFS(db);
			namespace2 = gfsNs.getBucketName();
		}
		stats(os, namespace2 + "." + GRIDFS_FILES_COLLECTION);
	} */

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
		public Connection(final DB db, final String bucket, final String filename) {
			this.db = db;
			this.bucket = bucket;
			this.filename = filename;
		}
		public static <T extends LvlFile> Connection newBucket(final MongoClient client, final T obj) {
			final DB db = client.getDB(CONFIG_MANAGER.getDbName());
			final String bucket = getBucket(obj).orNull();
			final String filename = getBucketFilename(obj);
			return new Connection(db, bucket, filename);
		}
		public static <T extends LvlFile> Connection newBucketNoFilename(final MongoClient client, final T obj) {
			final DB db = client.getDB(CONFIG_MANAGER.getDbName());
			final String bucket = getBucket(obj).orNull();
			return new Connection(db, bucket, null);
		}
		private static <T extends LvlFile> Optional<String> getBucket(final T obj) {
			checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
			String bucket = null, namespace = null;
			if (isNotBlank(namespace = trimToNull(obj.getMetadata().getNamespace()))) {
				bucket = "fs_" + String.format(BUCKETS_PADDING, hash2bucket(namespace, NUM_BUCKETS));
			}
			return fromNullable(bucket);
		}
		private static <T extends LvlFile> String getBucketFilename(final T obj) {
			checkArgument(obj != null && obj.getMetadata() != null, "Uninitialized or invalid file object");
			String filename = null;
			checkArgument(isNotBlank(filename = trimToNull(obj.getMetadata().getFilename())), "Uninitialized or invalid file object");
			String namespace = null;
			if (isNotBlank(namespace = trimToNull(obj.getMetadata().getNamespace()))) {
				filename = namespace + ":" + filename;
			}
			return filename;
		}
	}

	private static class MongoTask<T> extends CancellableTask<T> {
		public MongoTask(final ListenableFutureTask<T> task) {
			super(null);
			this.task = task;
		}
	}

}