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

package eu.eubrazilcc.lvl.core.analysis;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newTreeMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;

import javax.annotation.Nullable;

import com.google.common.base.Function;

/**
 * Provides a disjoint-set data structure with helper methods to operate the data.
 * @author Erik Torres <ertorser@upv.es>
 * @param <T> the type of data stored with this structure
 */
public class DisjointSet<T> {

	private final SortedMap<Integer, Element<T>> map = newTreeMap();

	public DisjointSet(final Collection<T> elements) {		
		if (elements != null) {
			int i = 0;
			for (final T item : elements) {
				this.map.put(i++, new Element<T>(-1, item));
			}
		}
	}

	public int find(final T element) {
		checkArgument(element != null, "Uninitialized input element");
		final Map.Entry<Integer, Element<T>> entry = getEntry(element);
		checkState(entry != null, "The element was not found");
		if (entry.getValue().getId() < 0) {
			return entry.getKey().intValue();
		}
		// apply path compression		
		final int id = find(map.get(entry.getValue().getId()).getElement());
		entry.getValue().setId(id);
		return id;
	}

	public void merge(final T a, final T b) {
		final int idA = find(a), idB = find(b);
		if (idA != idB) {			
			final Element<T> elemA = map.get(idA), elemB = map.get(idB);
			if (elemA.getId() == elemB.getId()) {
				elemA.setId(idB);
				elemB.decrementId();
			} else if (elemA.getId() < elemB.getId()) {
				elemB.setId(idA);
			} else {
				elemA.setId(idB);
			}
		}
	}

	public List<T> rootElements() {		
		return from(map.entrySet()).transform(new Function<Map.Entry<Integer, Element<T>>, T>() {
			@Override
			public T apply(final Entry<Integer, Element<T>> input) {
				final Element<T> element = input.getValue();
				return element.getId() < 0 ? element.getElement() : null;				
			}			
		}).filter(notNull()).toList();
	}

	public List<T> getElementsInSet(final T element) {
		final int id = find(element);
		return from(map.entrySet()).transform(new Function<Map.Entry<Integer, Element<T>>, T>() {
			@Override
			public T apply(final Entry<Integer, Element<T>> input) {
				final T element2 = input.getValue().getElement();
				return find(element2) == id ? element2 : null;
			}
		}).filter(notNull()).toList();
	}

	private @Nullable Map.Entry<Integer, Element<T>> getEntry(final T element) {
		for (final Map.Entry<Integer, Element<T>> entry : map.entrySet()) {
			if (entry.getValue().getElement().equals(element)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		String str = "";
		for (final Map.Entry<Integer, Element<T>> entry : map.entrySet()) {
			final Element<T> element = entry.getValue();
			str += "POS=" + entry.getKey() + ", ID=" + element.getId() + ", VALUE=" + element.getElement().toString() + "\n";
		}
		return str;
	}

	/* Fluent API */

	public static <T> DisjointSet<T> of(final Collection<T> elements) {
		checkArgument(elements != null && !elements.isEmpty(), "Uninitialized or invalid input collection");
		return new DisjointSet<T>(elements);
	}

	/* Auxiliary classes */

	private static final class Element<T> {
		private int id;
		private T element;

		public Element(final int id, final T element) {
			setId(id);
			setElement(element);
		}

		public int getId() {
			return id;
		}
		public void setId(final int id) {
			this.id = id;
		}
		public T getElement() {
			return element;
		}
		public void setElement(final T element) {
			this.element = element;
		}

		public void decrementId() {
			--id;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof Element)) {
				return false;
			}
			final Element<?> other = Element.class.cast(obj);
			return Objects.equals(id, other.id)
					&& Objects.equals(element, other.element);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, element);
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("id", id)
					.add("element", element)					
					.toString();
		}
	}

}