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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static eu.eubrazilcc.lvl.core.CollectionNames.LEISHMANIA_COLLECTION;
import static eu.eubrazilcc.lvl.core.CollectionNames.SANDFLY_COLLECTION;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.LEISHMANIA_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.SimpleStat;

/**
 * Stores calls to the application database that includes a collection statistics query.
 * @author Erik Torres <ertorser@upv.es>
 */
public class StatisticsCache {

	private static final Logger LOGGER = getLogger(StatisticsCache.class);

	public static final int MAX_CACHED_ELEMENTS = 2;
	public static final int CACHE_EXPIRATION_SECONDS = 180; // three hour

	private static final LoadingCache<String, Optional<Map<String, List<SimpleStat>>>> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.refreshAfterWrite(CACHE_EXPIRATION_SECONDS, SECONDS)
			.build(new CacheLoader<String, Optional<Map<String, List<SimpleStat>>>>() {
				@Override
				public Optional<Map<String, List<SimpleStat>>> load(final String key) throws ExecutionException {
					return collectionStats(key);
				}
				@Override
				public ListenableFuture<Optional<Map<String, List<SimpleStat>>>> reload(final String key, final Optional<Map<String, List<SimpleStat>>> old) throws Exception {
					return TASK_RUNNER.submit(new Callable<Optional<Map<String, List<SimpleStat>>>>() {
						public Optional<Map<String, List<SimpleStat>>> call() {
							return collectionStats(key);
						}
					});
				}
			});

	public static Map<String, List<SimpleStat>> sandflyStats() {
		Optional<Map<String, List<SimpleStat>>> stats = absent();
		try {
			stats = CACHE.get(SANDFLY_COLLECTION);
		} catch (ExecutionException e) {
			LOGGER.error("Failed to get collection statistics from cache", e);
		}
		return stats.or(new Hashtable<String, List<SimpleStat>>());
	}

	public static Map<String, List<SimpleStat>> leishmaniaStats() {
		Optional<Map<String, List<SimpleStat>>> stats = absent();
		try {
			stats = CACHE.get(LEISHMANIA_COLLECTION);
		} catch (ExecutionException e) {
			LOGGER.error("Failed to get collection statistics from cache", e);
		}
		return stats.or(new Hashtable<String, List<SimpleStat>>());
	}	

	private static Optional<Map<String, List<SimpleStat>>> collectionStats(final String collection) {
		checkArgument(isNotBlank(collection), "Uninitialized or invalid collection");
		LOGGER.trace("Colelction: " + collection);
		// get from database
		Optional<Map<String, List<SimpleStat>>> stats = absent();
		if (SANDFLY_COLLECTION.equals(collection)) {
			stats = fromNullable(SANDFLY_DAO.collectionStats());
		} else if (LEISHMANIA_COLLECTION.equals(collection)) {
			stats = fromNullable(LEISHMANIA_DAO.collectionStats());
		}
		return stats;
	}

}