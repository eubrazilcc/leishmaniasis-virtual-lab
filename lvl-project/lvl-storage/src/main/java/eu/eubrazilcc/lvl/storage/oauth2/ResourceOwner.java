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
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * Simple implementation of the OAuth 2.0 resource owner using user+password.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ResourceOwner implements Serializable {

	private static final long serialVersionUID = -6701870173207375701L;

	private String ownerId;
	private User user;

	public ResourceOwner() { }

	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(final String ownerId) {
		this.ownerId = ownerId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(final User user) {
		this.user = user;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof ResourceOwner)) {
			return false;
		}
		final ResourceOwner other = ResourceOwner.class.cast(obj);
		return Objects.equal(ownerId, other.ownerId)
				&& Objects.equal(user, other.user);
	}

	/**
	 * Ignores password and salt fields when comparing two instances of this class. Use this method when comparing an instance of this class that contains
	 * an unprotected password (password in plain text and no salt) with a protected one (hashed password with a valid salt).
	 * @param other - the instance to be compared to.
	 * @return {@code true} if all the attributes of both instances coincide in value with the sole exception of those considered part of the password. 
	 *        Otherwise, {@code false}.
	 */
	public boolean equalsToUnprotected(final ResourceOwner other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(ownerId, other.ownerId)
				&& ((user == null && other.user == null) || (user.equalsToUnprotected(other.user)));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(ownerId, user);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("ownerId", ownerId)
				.add("user", user)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ResourceOwner instance = new ResourceOwner();

		public Builder id(final String ownerId) {
			checkArgument(isNotBlank(ownerId), "Uninitialized or invalid owner Id");
			instance.setOwnerId(ownerId);
			return this;
		}

		public Builder user(final User user) {
			checkArgument(user != null, "Uninitialized user");
			instance.setUser(user);
			return this;
		}

		public ResourceOwner build() {
			return instance;
		}

	}

	/**
	 * Performs a deep copy of the input instance.
	 * @param original - the original instance to be copied.
	 * @return a deep copy of the input instance.
	 */
	public static final ResourceOwner copyOf(final ResourceOwner original) {
		ResourceOwner copy = null;
		if (original != null) {
			return builder()
					.id(original.ownerId)
					.user(User.copyOf(original.user))
					.build();
		}
		return copy;
	}

}