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
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Date;
import java.util.Objects;

/**
 * Any identifiable item capable of being shared between its owner and another user.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class Shareable {

	private String owner;
	private String user;

	private String collection;
	private String itemId;

	private Date sharedDate;
	private SharedAccess accessType;

	/**
	 * Gets who is sharing access to the item being shared.
	 * @return identity of the user who is sharing access to the item being shared.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Sets who is sharing access to the item being shared.
	 * @param owner - the identity of the user who is sharing access to the item being shared
	 */
	public void setOwner(final String owner) {
		this.owner = owner;
	}

	/**
	 * Gets who is receiving access to the item being shared.
	 * @return identity of the user who is receiving access to the item being shared.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets who is receiving access to the item being shared.
	 * @param user - the identity of the user who is receiving access to the item being shared
	 */
	public void setUser(final String user) {
		this.user = user;
	}

	/**
	 * Gets the collection where the item is stored.
	 * @return the collection where the item is stored.
	 */
	public String getCollection() {
		return collection;
	}

	/**
	 * Sets the collection where the item is stored.
	 * @param collection - the collection where the item is stored
	 */
	public void setCollection(final String collection) {
		this.collection = collection;
	}

	/**
	 * Gets the identifier of the shared item.
	 * @return the identifier of the shared item.
	 */
	public String getItemId() {
		return itemId;
	}

	/**
	 * Sets the identifier of the shared item.
	 * @param itemId - the identifier of the shared item
	 */
	public void setItemId(final String itemId) {
		this.itemId = itemId;
	}

	/**
	 * Gets the moment when the item was shared.
	 * @return the moment when the item was shared.
	 */
	public Date getSharedDate() {
		return sharedDate;
	}

	/**
	 * Sets the moment when the item was shared.
	 * @param sharedDate - the moment when the item was shared
	 */
	public void setSharedDate(final Date sharedDate) {
		this.sharedDate = sharedDate;
	}

	/**
	 * Gets the type of access granted over a shared item.
	 * @return the type of access granted over a shared item.
	 */
	public SharedAccess getAccessType() {
		return accessType;
	}

	/**
	 * Sets the type of access granted over a shared item.
	 * @param accessType - the type of access granted over a shared item
	 */
	public void setAccessType(final SharedAccess accessType) {
		this.accessType = accessType;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Shareable)) {
			return false;
		}
		final Shareable other = Shareable.class.cast(obj);
		return  Objects.equals(owner, other.owner)
				&& Objects.equals(user, other.user)
				&& Objects.equals(collection, other.collection)
				&& Objects.equals(itemId, other.itemId)
				&& Objects.equals(sharedDate, other.sharedDate)
				&& Objects.equals(accessType, other.accessType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(owner, user, collection, itemId, sharedDate, accessType);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("owner", owner)
				.add("user", user)
				.add("collection", collection)
				.add("itemId", itemId)
				.add("sharedDate", sharedDate)
				.add("accessType", accessType)
				.toString();
	}	

	/**
	 * Defines the type of access granted over a shared object.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static enum SharedAccess {
		/**
		 * Grants access to view the shared object.
		 */
		VIEW_SHARE,
		/**
		 * Grants access to view and modify the shared object.
		 */
		EDIT_SHARE
	}

	/* Fluent API */

	public static class Builder<T extends Shareable> {

		protected final T instance;

		public Builder(final Class<T> clazz) {
			T tmp = null;
			try {
				tmp = clazz.newInstance();
			} catch (Exception ignore) { }
			instance = tmp;
		}

		public Builder<T> owner(final String owner) {
			String owner2 = null;
			checkArgument(isNotBlank(owner2 = trimToNull(owner)), "Uninitialized or invalid owner");
			instance.setOwner(owner2);
			return this;
		}

		public Builder<T> user(final String user) {
			String user2 = null;
			checkArgument(isNotBlank(user2 = trimToNull(user)), "Uninitialized or invalid user");
			instance.setUser(user2);
			return this;
		}

		public Builder<T> collection(final String collection) {
			String collection2 = null;
			checkArgument(isNotBlank(collection2 = trimToNull(collection)), "Uninitialized or invalid collection");
			instance.setCollection(collection2);
			return this;
		}

		public Builder<T> itemId(final String itemId) {
			String itemId2 = null;
			checkArgument(isNotBlank(itemId2 = trimToNull(itemId)), "Uninitialized or invalid item Id");
			instance.setItemId(itemId2);
			return this;
		}

		public Builder<T> sharedDate(final Date sharedDate) {
			instance.setSharedDate(requireNonNull(sharedDate, "Uninitialized shared date"));
			return this;
		}

		public Builder<T> sharedNow() {
			instance.setSharedDate(new Date());
			return this;
		}

		public Builder<T> accessType(final SharedAccess accessType) {
			instance.setAccessType(requireNonNull(accessType, "Uninitialized access type"));			
			return this;
		}

		public T build() {
			return instance;
		}

	}

}