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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Objects;
import java.util.Set;

/**
 * Represents the target of any object stored with the filesystem or the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Target {

	private String type;
	private String collection;
	private Set<String> ids;
	private String filter;
	private String compression;

	public Target() { }

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public Set<String> getIds() {
		return ids;
	}

	public void setIds(final Set<String> ids) {
		if (ids != null) {
			this.ids = newHashSet(ids);
		} else {
			this.ids = null;
		}
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public String getCompression() {
		return compression;
	}

	public void setCompression(final String compression) {
		this.compression = compression;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Target)) {
			return false;
		}
		final Target other = Target.class.cast(obj);
		return Objects.equals(type, other.type)
				&& Objects.equals(collection, other.collection)
				&& Objects.equals(ids, other.ids)
				&& Objects.equals(filter, other.filter)
				&& Objects.equals(compression, other.compression);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, collection, ids, filter, compression);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("type", type)
				.add("collection", collection)
				.add("ids", ids)
				.add("filter", filter)
				.add("compression", compression)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Target instance = new Target();

		public Builder type(final String type) {
			instance.setType(type);
			return this;
		}

		public Builder collection(final String collection) {
			instance.setCollection(collection);
			return this;
		}

		public Builder id(final String id) {
			return ids(newHashSet(id));
		}

		public Builder ids(final Set<String> ids) {
			instance.setIds(ids);
			return this;
		}

		public Builder filter(final String filter) {
			instance.setFilter(filter);
			return this;
		}

		public Builder compression(final String compression) {
			instance.setCompression(compression);
			return this;
		}

		public Target build() {
			return instance;
		}

	}

}