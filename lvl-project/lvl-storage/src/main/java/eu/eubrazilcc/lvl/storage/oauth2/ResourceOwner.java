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

	@Override
	public int hashCode() {
		return Objects.hashCode(ownerId, user);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("ownerId", ownerId)
				.add("user", user)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ResourceOwner resourceOwner = new ResourceOwner();

		public Builder id(final String ownerId) {
			checkArgument(isNotBlank(ownerId), "Uninitialized or invalid owner Id");
			resourceOwner.setOwnerId(ownerId);
			return this;
		}

		public Builder user(final User user) {
			checkArgument(user != null, "Uninitialized user");
			resourceOwner.setUser(user);
			return this;
		}

		public ResourceOwner build() {
			return resourceOwner;
		}

	}

}