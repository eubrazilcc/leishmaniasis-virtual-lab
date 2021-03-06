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
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;

import com.google.common.cache.Cache;

import eu.eubrazilcc.lvl.core.Sequence;

/**
 * Stores sequences as files in a temporary directory, which is used as a caching system. A file is deleted when the cache 
 * reference expires. This class uses the {@link Sequence#getId() sequence Id} as the cache key and the 
 * {@link Sequence#getVersion() sequence version} as the versionable field.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequencePersistingCache extends VersionablePersistingCache<Sequence> {

	private static final Cache<String, CachedVersionable> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.expireAfterAccess(CACHE_EXPIRATION_SECONDS, SECONDS)
			.removalListener(new PersistingRemovalListener())
			.build();

	public SequencePersistingCache() {
		super(SequencePersistingCache.class);		
	}	

	@Override
	protected Cache<String, CachedVersionable> cache() {
		return CACHE;
	}

	@Override
	public CachedVersionable put(final String id, final Sequence obj) throws IOException {
		String key = null;
		checkArgument(isNotBlank(key = toKey(id)) && obj != null && isNotBlank(obj.getId()) && isNotBlank(obj.getVersion()) && obj.getSequence() != null,
				"Uninitialized or invalid sequence");		
		File outfile = null;
		try {
			final String uuid = randomUUID().toString();
			outfile = new File(new File(getCacheDir(), uuid.substring(0, 2)), uuid);
			final File parentDir = outfile.getParentFile();
			if (parentDir.exists() || parentDir.mkdirs());
			if ((outfile.isFile() && outfile.canWrite()) || outfile.createNewFile());
			final CachedVersionable cached = CachedVersionable.builder()
					.cachedFilename(outfile.getCanonicalPath())
					.version(obj.getVersion().trim())
					.build();
			GBSEQ_XMLB.typeToFile(obj.getSequence(), outfile);			
			CACHE.put(key, cached);
			return cached;
		} catch (Exception e) {
			deleteQuietly(outfile);
			throw e;
		}
	}

}