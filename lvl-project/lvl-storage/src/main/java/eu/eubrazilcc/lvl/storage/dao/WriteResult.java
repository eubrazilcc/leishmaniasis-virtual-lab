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

package eu.eubrazilcc.lvl.storage.dao;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Methods that insert or modify an element in a database may use this method to return a copy of the
 * inserted or changed element to the caller. The id assigned to the element in the database must be
 * always returned within this class, while it's optional to return a copy of the element inserted in 
 * the database and should be done only in those cases when the original element is inserted with
 * modifications in the database (e.g. shaded passwords).
 * @author Erik Torres <ertorser@upv.es>
 * @param <E> - type of the elements
 */
public class WriteResult<E> {

	private String id;
	private @Nullable E element;

	public WriteResult() { }

	/**
	 * The id assigned to the element in the database.
	 * @return the id assigned to the element in the database.
	 */
	public String getId() {
		return id;
	}
	public void setId(final String id) {
		this.id = id;
	}
	/**
	 * A (deep) copy of the element inserted in the database.
	 * @return a (deep) copy of the element inserted in the database, or {@code null} if the original
	 *         element is inserted unmodified.
	 */
	public @Nullable E getElement() {
		return element;
	}
	public void setElement(final @Nullable E element) {
		this.element = element;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WriteResult)) {
			return false;
		}
		final WriteResult<?> other = WriteResult.class.cast(obj);
		return Objects.equal(id, other.id)
				&& Objects.equal(element, other.element);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, element);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)				
				.add("element", element)
				.toString();
	}

	/* Fluent API */

	public static <E> Builder<E> builder() {
		return new Builder<E>();
	}

	public static final class Builder<E> {

		private final WriteResult<E> instance = new WriteResult<E>();

		public Builder<E> id(final String id) {
			instance.setId(id);
			return this;
		}

		public Builder<E> element(final @Nullable E element) {
			instance.setElement(element);
			return this;
		}

		public WriteResult<E> build() {
			return instance;
		}

	}
}