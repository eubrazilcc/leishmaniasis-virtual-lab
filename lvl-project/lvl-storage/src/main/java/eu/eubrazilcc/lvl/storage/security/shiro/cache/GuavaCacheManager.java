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

package eu.eubrazilcc.lvl.storage.security.shiro.cache;

import static com.google.common.primitives.Ints.checkedCast;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Set;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.slf4j.Logger;

import com.google.common.cache.CacheBuilder;

/**
 * Implementation of {@link org.apache.shiro.cache.CacheManager} that uses the Guava caching API. This class was originally released 
 * under the Apache License, Version 2.0.<br>
 * <br>
 * This cache manager may be configured giving a cacheBuilderSpecification (as a string) by calling {@link #setCacheBuilderSpecification(String)} 
 * or in <tt>shiro.ini</tt>:
 * <pre>
 * guavaCacheManager = eu.eubrazilcc.lvl.storage.security.shiro.cache.GuavaCacheManager
 * guavaCacheManager.cacheBuilderSpecification = maximumSize=10000,expireAfterWrite=60m
 * securityManager.cacheManager = $guavaCacheManager
 * </pre>
 * For available configuration options (expiration times, using soft or weak references, maximum sizes etc.) 
 * see <a href="http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilderSpec.html">CacheBuilderSpec</a>.
 * @author Brendan Le Ny <bleny@codelutin.com>
 * <br>
 * Changes (major) by Erik Torres <ertorser@upv.es>:
 * <ul>
 * <li>Adapt to project's standards</li>
 * <li>Limit {@link #MAX_CACHED_ELEMENTS maximum cached elements} to avoid memory overflow</li>
 * <li>Add synchronization to {@link #getCacheBuilder() cache builder} method in order to avoid race conditions when used concurrently</li>
 * <li>Check <tt>null</tt> keys in {@link ShiroCacheToGuavaCacheAdapter} to avoid silent exceptions to be raised, breaking multiple-realm 
 *     authentication strategies</li>
 * </ul>
 * @see <a href="https://issues.apache.org/jira/browse/SHIRO-481">Provide CacheManager implementation based on Guava caching API</a>
 */
public class GuavaCacheManager extends AbstractCacheManager {

	private static final Logger LOGGER = getLogger(GuavaCacheManager.class);

	public static final int MAX_CACHED_ELEMENTS = 10000;

	/**
	 * Specification to use when creating cacheBuilder. It may be <tt>null</tt> if user didn't gave any configuration.
	 */
	protected String cacheBuilderSpecification = null;

	/**
	 * CacheBuilder to create new caches according to {@link #cacheBuilderSpecification} if provided.
	 */
	protected CacheBuilder<Object, Object> cacheBuilder = null;

	public void setCacheBuilderSpecification(final String cacheBuilderSpecification) {
		this.cacheBuilderSpecification = cacheBuilderSpecification;
	}

	protected CacheBuilder<Object, Object> getCacheBuilder() {
		if (cacheBuilder == null) {
			synchronized (CacheBuilder.class) {
				if (cacheBuilder == null) {
					if (cacheBuilderSpecification == null) {
						LOGGER.info("creating default cache builder");
						cacheBuilder = CacheBuilder.newBuilder().maximumSize(MAX_CACHED_ELEMENTS);
					} else {
						LOGGER.info("creating cache builder using spec " + cacheBuilderSpecification);
						cacheBuilder = CacheBuilder.from(cacheBuilderSpecification);
					}
				}
			}
		}
		return cacheBuilder;
	}

	@Override
	protected Cache<Object, Object> createCache(final String name) {
		final com.google.common.cache.Cache<Object, Object> guavaCache = getCacheBuilder().build();
		return new ShiroCacheToGuavaCacheAdapter<Object, Object>(name, guavaCache);
	}

	/**
	 * Adapts a {@link com.google.common.cache.Cache} to a {@link org.apache.shiro.cache.Cache} implementation.
	 * @author Brendan Le Ny <bleny@codelutin.com>
	 * <br>
	 * Changes (major) by Erik Torres <ertorser@upv.es>:
	 * <ul>
	 * <li>Adapt to project's standards</li>
	 * <li>Check <tt>null</tt> keys to avoid silent exceptions to be raised, breaking multiple-realm authentication strategies</li>
	 * </ul>
	 */
	protected static class ShiroCacheToGuavaCacheAdapter<K, V> implements Cache<K, V> {

		protected final String name;
		protected final com.google.common.cache.Cache<K, V> adapted;

		public ShiroCacheToGuavaCacheAdapter(final String name, final com.google.common.cache.Cache<K, V> adapted) {
			this.name = name;
			this.adapted = adapted;
		}

		@Override
		public V get(final K key) {
			V value = null;
			if (key != null) {
				value = adapted.getIfPresent(key);
			}
			return value;
		}

		@Override
		public V put(final K key, final V value) {
			V lastValue = null;
			if (key != null) {
				lastValue = get(key);
				adapted.put(key, value);
			}
			return lastValue;
		}

		@Override
		public V remove(final K key) {
			V lastValue = null;
			if (key != null) {
				lastValue = get(key);
				adapted.invalidate(key);
			}
			return lastValue;
		}

		@Override
		public void clear() {
			adapted.invalidateAll();
		}

		@Override
		public int size() {
			return checkedCast(adapted.size());
		}

		@Override
		public Set<K> keys() {
			return adapted.asMap().keySet();
		}

		@Override
		public Collection<V> values() {
			return adapted.asMap().values();
		}

		/**
		 * Gets the name of this cache. This method will not be used by Shiro but is provided for developer
		 * convenience (testing, debugging).
		 * @return the name given when {@link #createCache(String)} was called.
		 */
		public String getName() {
			return name;
		}

		/** 
		 * Gets the adapted Guava cache. This method will not be used by Shiro but is provided for developer 
		 * convenience (testing, debugging).
		 * @return the underlying, adapted Guava cache.
		 */
		public com.google.common.cache.Cache<K, V> getAdapted() {
			return adapted;
		}

		@Override
		public String toString() {
			return "GuavaCache [" + adapted + "]";
		}
	}

}