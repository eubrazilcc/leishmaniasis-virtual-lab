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

package eu.eubrazilcc.lvl.core.util;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner.MapJoiner;

/**
 * Collection utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class CollectionUtils {

	public static <E> String collectionToString(final Collection<E> collection) {
		return Arrays.toString(from(collection != null ? collection : new ArrayList<E>()).transform(new Function<E, String>() {
			@Override
			public String apply(final E item) {
				return item != null ? item.toString() : null;
			}
		}).filter(notNull()).toArray(String.class));		
	}

	public static <K, V> String mapToString(final Map<K, V> map) {	
		final MapJoiner mapJoiner = on(',').withKeyValueSeparator("=");
		return mapJoiner.join(map != null ? map : new HashMap<K, V>());
	}

}