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

package eu.eubrazilcc.lvl.storage.mongodb;

import static com.google.common.base.MoreObjects.toStringHelper;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBMapKey.escapeMapKey;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Provides a map that features a key compatible with mongoDB field names. Internally this class wraps a {@link Hashtable} with a key restriction.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://docs.mongodb.org/manual/reference/limits/#Restrictions-on-Field-Names">mongoDB Restrictions on Field Names</a>
 */
public class MongoDBMap<K extends MongoDBMapKey, V> implements Map<K, V> {

	private Map<K, V> __map = new Hashtable<>();

	public MongoDBMap() {		
	}

	public MongoDBMap(final Map<? extends K, ? extends V> initial) {
		super();
		this.putAll(initial);
	}

	@Override
	public void clear() {
		__map.clear();
	}

	@Override
	public boolean containsKey(final Object key) {
		return __map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return __map.containsValue(value);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return __map.entrySet();
	}

	@Override
	public V get(final Object key) {
		return __map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return __map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return __map.keySet();
	}

	@Override
	public V put(final K key, final V value) {
		return __map.put(key, value);
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		__map.putAll(m);
	}

	@Override
	public V remove(final Object key) {
		return __map.remove(key);
	}

	@Override
	public int size() {
		return __map.size();
	}

	@Override
	public Collection<V> values() {
		return __map.values();
	}

	public V getUnescaped(final String key) {		
		return __map.get(escapeMapKey(key));
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof MongoDBMap)) {
			return false;
		}
		final MongoDBMap<?, ?> other = MongoDBMap.class.cast(obj);
		return Objects.equals(__map, other.__map);
	}

	@Override
	public int hashCode() {
		return Objects.hash(__map);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("__map", __map != null ? __map.toString() : null)
				.toString();
	}

}