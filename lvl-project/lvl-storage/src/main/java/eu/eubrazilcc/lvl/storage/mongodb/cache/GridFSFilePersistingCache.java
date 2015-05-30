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

package eu.eubrazilcc.lvl.storage.mongodb.cache;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static java.lang.System.getProperty;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mongodb.gridfs.GridFSDBFile;

/**
 * Stores MongoDB GridFS files in a temporary directory, which is used as a caching system. A file is deleted
 * when the cache reference expires.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://docs.mongodb.org/manual/core/gridfs/">GridFS</a>
 */
public class GridFSFilePersistingCache {

	private static final Logger LOGGER = getLogger(GridFSFilePersistingCache.class);

	public static final int MAX_CACHED_ELEMENTS = 100;
	public static final int CACHE_EXPIRATION_SECONDS = 86400; // one day

	private static final Cache<String, CachedGridFSFile> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.expireAfterAccess(CACHE_EXPIRATION_SECONDS, SECONDS)
			.removalListener(new PersistingRemovalListener())
			.build();

	private final String cacheName;
	private final File cacheDir;

	public GridFSFilePersistingCache() {
		this.cacheName = GridFSFilePersistingCache.class.getSimpleName() + "_" + random(8, true, true);
		File localCacheDir;
		try {
			localCacheDir = new File(CONFIG_MANAGER.getLocalCacheDir(), this.cacheName);
		} catch (IOException e) {
			localCacheDir = new File(concat(getProperty("java.io.tmpdir"), this.cacheName));
		}
		this.cacheDir = localCacheDir;
		if ((this.cacheDir.isDirectory() && this.cacheDir.canWrite()) || this.cacheDir.mkdirs());
		LOGGER.info("GridFS persistent cache: " + this.cacheDir.getAbsolutePath());
	}

	public CachedGridFSFile getIfPresent(final @Nullable String namespace, final String filename) {
		return CACHE.getIfPresent(toKey(namespace, filename));
	}

	public CachedGridFSFile put(final @Nullable String namespace, final GridFSDBFile gfsFile) throws IOException {		
		checkArgument(gfsFile != null && isNotBlank(gfsFile.getFilename()) && isNotBlank(gfsFile.getMD5()), 
				"Uninitialized or invalid file");
		File outfile = null;
		try {
			final String uuid = randomUUID().toString();
			outfile = new File(new File(cacheDir, uuid.substring(0, 2)), uuid);
			final File parentDir = outfile.getParentFile();
			if (parentDir.exists() || parentDir.mkdirs());
			if ((outfile.isFile() && outfile.canWrite()) || outfile.createNewFile());
			final CachedGridFSFile cachedFile = CachedGridFSFile.builder()
					.cachedFilename(outfile.getCanonicalPath())
					.md5(gfsFile.getMD5())
					.build();
			gfsFile.writeTo(outfile);
			CACHE.put(toKey(namespace, gfsFile.getFilename()), cachedFile);
			return cachedFile;
		} catch (Exception e) {
			deleteQuietly(outfile);
			throw e;
		}
	}

	public CachedGridFSFile update(final @Nullable String namespace, final GridFSDBFile gfsFile) throws IOException {
		checkArgument(gfsFile != null && isNotBlank(gfsFile.getFilename()) && isNotBlank(gfsFile.getMD5()), 
				"Uninitialized or invalid file");
		invalidate(namespace, gfsFile.getFilename());
		return put(namespace, gfsFile);
	}

	public void invalidate(final @Nullable String namespace, final String filename) {
		CACHE.invalidate(toKey(namespace, filename));
	}
	
	public void invalidateAll() {
		CACHE.invalidateAll();
	}

	private static String toKey(final @Nullable String namespace, final String filename) {
		checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
		return trimToEmpty(namespace) + "/" + filename.trim();
	}

	/**
	 * Removes files from the filesystem when the corresponding cache reference expires.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PersistingRemovalListener implements RemovalListener<String, CachedGridFSFile> {

		@Override
		public void onRemoval(final RemovalNotification<String, CachedGridFSFile> notification) {
			if (notification != null && notification.getValue() != null 
					&& isNotBlank(notification.getValue().getCachedFilename())) {
				final File cachedFile = new File(notification.getValue().getCachedFilename());
				deleteQuietly(cachedFile);
				final File parentDir = cachedFile.getParentFile();
				if (parentDir.isDirectory() && parentDir.list().length == 0) {
					deleteQuietly(parentDir);	
				}
				LOGGER.trace("Cached file deleted: " + notification.getValue().getCachedFilename());
			}
		}

	}

}