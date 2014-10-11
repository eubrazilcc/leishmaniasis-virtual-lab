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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ComparisonChain;

/**
 * Stores a pair consisting of two elements.
 * @author Erik Torres <ertorser@upv.es>
 * @param <K> the key element type
 * @param <V> the value element type
 */
public class Pair<K extends Comparable<K>, V extends Comparable<V>> implements Map.Entry<K, V>, Comparable<Pair<K, V>> {

	public K key;
	public V value;

	public Pair() { }

	public Pair(final K key, final V value) {
		setKey(key);
		setValue(value);		
	}

	@Override
	public K getKey() {
		return key;
	}

	public void setKey(final K key) {
		this.key = key;
	}

	@Override
	public V getValue() {
		return value;
	}

	public V setValue(final V value) {
		this.value = value;
		return this.value;
	}

	@Override
	public int compareTo(final Pair<K, V> other) {
		return ComparisonChain.start()				
				.compare(key, other.key)
				.compare(value, other.value)
				.result();		
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof  Pair)) {
			return false;
		}
		final  Pair<?, ?> other =  Pair.class.cast(obj);
		return Objects.equals(key, other.key)
				&& Objects.equals(value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("key", key)
				.add("value", value)				
				.toString();
	}	

	/* Fluent API */

	public static <K extends Comparable<K>, V extends Comparable<V>>  Pair<K, V> of(final K key, final V value) {
		return new  Pair<K, V>(key, value);
	}

}