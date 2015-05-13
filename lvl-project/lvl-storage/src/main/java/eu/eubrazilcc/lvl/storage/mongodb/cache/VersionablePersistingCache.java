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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Stores versionable objects as files in a temporary directory, which is used as a caching system. A 
 * file is deleted when the cache reference expires.
 * @author Erik Torres <ertorser@upv.es>
 * @param <T> the type of versionable object
 */
public abstract class VersionablePersistingCache<T> {

	private static final Logger LOGGER = getLogger(VersionablePersistingCache.class);

	public static final int MAX_CACHED_ELEMENTS = 100;
	public static final int CACHE_EXPIRATION_SECONDS = 86400; // one day

	private final String cacheName;
	private final File cacheDir;

	public VersionablePersistingCache(final Class<?> implementingClass) {
		this.cacheName = implementingClass.getSimpleName() + "_" + random(8, true, true);
		File localCacheDir;
		try {
			localCacheDir = new File(CONFIG_MANAGER.getLocalCacheDir(), this.cacheName);
		} catch (IOException e) {
			localCacheDir = new File(concat(getProperty("java.io.tmpdir"), this.cacheName));
		}
		this.cacheDir = localCacheDir;
		if ((this.cacheDir.isDirectory() && this.cacheDir.canWrite()) || this.cacheDir.mkdirs());
		LOGGER.info(this.cacheName + " persistent cache started in directory: " + this.cacheDir.getAbsolutePath());
	}	

	public String getCacheName() {
		return cacheName;
	}

	public File getCacheDir() {
		return cacheDir;
	}

	protected abstract Cache<String, CachedVersionable> cache();

	public CachedVersionable getIfPresent(final String id) {
		return cache().getIfPresent(toKey(id));
	}

	public abstract CachedVersionable put(final String id, final T obj) throws IOException;

	public CachedVersionable update(final String id, final T obj) throws IOException {		
		checkArgument(obj != null, "Uninitialized or invalid object");
		final String key = toKey(id);
		invalidate(key);
		return put(key, obj);
	}

	public void invalidate(final String id) {
		cache().invalidate(toKey(id));
	}

	public void invalidateAll() {
		cache().invalidateAll();
	}

	protected static String toKey(final String id) {
		String id2 = null;
		checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
		return id2;
	}

	/**
	 * Removes files from the filesystem when the corresponding cache reference expires.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PersistingRemovalListener implements RemovalListener<String, CachedVersionable> {

		@Override
		public void onRemoval(final RemovalNotification<String, CachedVersionable> notification) {
			String cachedFilename = null;
			if (notification != null && notification.getValue() != null 
					&& isNotBlank(cachedFilename = notification.getValue().getCachedFilename())) {
				final File cachedFile = new File(cachedFilename);
				deleteQuietly(cachedFile);
				final File parentDir = cachedFile.getParentFile();
				if (parentDir.isDirectory() && parentDir.list().length == 0) {
					deleteQuietly(parentDir);	
				}
				LOGGER.trace("Cached object deleted: " + cachedFilename);
			}
		}

	}

}