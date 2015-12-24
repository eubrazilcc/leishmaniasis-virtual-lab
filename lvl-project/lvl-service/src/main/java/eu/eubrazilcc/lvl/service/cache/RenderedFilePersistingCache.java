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

package eu.eubrazilcc.lvl.service.cache;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static java.lang.System.getProperty;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Stores files in a temporary directory, which is used as a caching system. A file is deleted
 * when the cache reference expires.
 * @author Erik Torres <ertorser@upv.es>
 */
public class RenderedFilePersistingCache {

	private static final Logger LOGGER = getLogger(RenderedFilePersistingCache.class);

	public static final int MAX_CACHED_ELEMENTS = 100;
	public static final int CACHE_EXPIRATION_SECONDS = 86400; // one day

	private static final Cache<String, CachedFile> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.expireAfterAccess(CACHE_EXPIRATION_SECONDS, SECONDS)
			.removalListener(new PersistingRemovalListener())
			.build();

	private final String cacheName;
	private final File cacheDir;

	public RenderedFilePersistingCache() {
		this.cacheName = RenderedFilePersistingCache.class.getSimpleName() + "_" + random(8, true, true);
		File localCacheDir;
		try {
			localCacheDir = new File(CONFIG_MANAGER.getLocalCacheDir(), this.cacheName);
		} catch (IOException e) {
			localCacheDir = new File(concat(getProperty("java.io.tmpdir"), this.cacheName));
		}
		this.cacheDir = localCacheDir;
		if ((this.cacheDir.isDirectory() && this.cacheDir.canWrite()) || this.cacheDir.mkdirs());
		LOGGER.info("File persistent cache: " + this.cacheDir.getAbsolutePath());
	}

	public CachedFile getIfPresent(final String key) {
		String key2 = null;
		checkArgument(isNotBlank(key2 = trimToNull(key)), "Uninitialized or invalid key");
		return CACHE.getIfPresent(key2);
	}

	public CachedFile put(final String key, final File file) throws IOException {
		String key2 = null;
		checkArgument(isNotBlank(key2 = trimToNull(key)), "Uninitialized or invalid key");
		checkArgument(file != null && file.canRead(), "Uninitialized or invalid file");
		File renderedFile = null;
		try {
			final String uuid = randomUUID().toString();
			renderedFile = new File(new File(cacheDir, uuid.substring(0, 2)), uuid);
			final File parentDir = renderedFile.getParentFile();
			if (parentDir.exists() || parentDir.mkdirs());
			if ((renderedFile.isFile() && renderedFile.canWrite()) || renderedFile.createNewFile());
			final CachedFile cachedFile = CachedFile.builder()
					.cachedFilename(renderedFile.getCanonicalPath())
					.build();
			move(file.toPath(), renderedFile.toPath(), REPLACE_EXISTING);			
			CACHE.put(key2, cachedFile);
			return cachedFile;
		} catch (Exception e) {
			deleteQuietly(renderedFile);
			throw e;
		}
	}

	public CachedFile update(final String key, final File file) throws IOException {		
		invalidate(key);
		return put(key, file);
	}

	public void invalidate(final String key) {
		String key2 = null;
		checkArgument(isNotBlank(key2 = trimToNull(key)), "Uninitialized or invalid key");
		CACHE.invalidate(key2);
	}

	public void invalidateAll() {
		CACHE.invalidateAll();
	}

	/**
	 * Removes files from the filesystem when the corresponding cache reference expires.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PersistingRemovalListener implements RemovalListener<String, CachedFile> {
		@Override
		public void onRemoval(final RemovalNotification<String, CachedFile> notification) {
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