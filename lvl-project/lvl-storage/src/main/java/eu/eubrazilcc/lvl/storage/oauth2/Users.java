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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;

import java.util.List;

import eu.eubrazilcc.lvl.core.Paginable;

/**
 * Wraps a collection of {@link User}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Users extends Paginable {

	private List<User> users = newArrayList();	

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(final List<User> users) {
		this.users = newArrayList(users);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("paginable", super.toString())
				.add("users", collectionToString(users))
				.toString();
	}

	/* Fluent API */
	
	public static UsersBuilder start() {
		return new UsersBuilder();
	}

	public static class UsersBuilder {

		private final Users instance;

		public UsersBuilder() {
			instance = new Users();
		}

		public UsersBuilder paginable(final Paginable paginable) {
			instance.push(paginable);
			return this;
		}

		public UsersBuilder users(final List<User> userList) {
			instance.setUsers(userList);
			return this;			
		}

		public Users build() {
			return instance;
		}

	}

}