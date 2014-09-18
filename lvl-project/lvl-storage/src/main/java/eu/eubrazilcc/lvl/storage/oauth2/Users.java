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

package eu.eubrazilcc.lvl.storage.oauth2;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.List;

import eu.eubrazilcc.lvl.core.Paginable;

/**
 * Wraps a collection of {@link User}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Users extends Paginable<User> {

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("paginable", super.toString())
				.toString();
	}

	/* Fluent API */

	public static UsersBuilder start() {
		return new UsersBuilder();
	}

	public static class UsersBuilder {

		private final Users instance = new Users();

		public UsersBuilder resource(final String resource) {
			instance.setResource(resource);
			return this;
		}

		public UsersBuilder page(final int page) {
			instance.setPage(page);
			return this;
		}

		public UsersBuilder perPage(final int perPage) {
			instance.setPerPage(perPage);
			return this;
		}

		public UsersBuilder sort(final String sort) {
			instance.setSort(sort);
			return this;
		}

		public UsersBuilder order(final String order) {
			instance.setOrder(order);
			return this;
		}

		public UsersBuilder query(final String query) {
			instance.setQuery(query);
			return this;
		}

		public UsersBuilder totalCount(final int totalCount) {
			instance.setTotalCount(totalCount);
			return this;
		}

		public UsersBuilder users(final List<User> users) {
			instance.setElements(users);
			return this;			
		}

		public Users build() {
			return instance;
		}

	}

}