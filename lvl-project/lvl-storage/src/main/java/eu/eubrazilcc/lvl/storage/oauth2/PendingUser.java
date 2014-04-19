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
 * Pending users are supposed to send a confirmation code.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingUser implements Serializable {

	private static final long serialVersionUID = 4734782376847178866L;

	private String pendingUserId;
	private long expiresIn;
	private long issuedAt;
	private String activationCode;
	private User user;	

	public PendingUser() { }

	public String getPendingUserId() {
		return pendingUserId;
	}
	public void setPendingUserId(final String pendingUserId) {
		this.pendingUserId = pendingUserId;
	}
	public long getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(final long expiresIn) {
		this.expiresIn = expiresIn;
	}
	public long getIssuedAt() {
		return issuedAt;
	}
	public void setIssuedAt(final long issuedAt) {
		this.issuedAt = issuedAt;
	}
	public String getActivationCode() {
		return activationCode;
	}
	public void setActivationCode(final String activationCode) {
		this.activationCode = activationCode;
	}
	public User getUser() {
		return user;
	}
	public void setUser(final User user) {
		this.user = user;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PendingUser)) {
			return false;
		}
		final PendingUser other = PendingUser.class.cast(obj);
		return Objects.equal(pendingUserId, other.pendingUserId)
				&& Objects.equal(expiresIn, other.expiresIn)
				&& Objects.equal(issuedAt, other.issuedAt)
				&& Objects.equal(activationCode, other.activationCode)
				&& Objects.equal(user, other.user);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(pendingUserId, expiresIn, issuedAt, activationCode, user);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("pendingUserId", pendingUserId)
				.add("expiresIn", expiresIn)
				.add("issuedAt", issuedAt)
				.add("confirmationCode", activationCode)
				.add("user", user)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final PendingUser pendingUser = new PendingUser();

		public Builder id(final String pendingUserId) {
			checkArgument(isNotBlank(pendingUserId), "Uninitialized or invalid pending user Id");
			pendingUser.setPendingUserId(pendingUserId);
			return this;
		}

		public Builder expiresIn(final long expiresIn) {
			pendingUser.setExpiresIn(expiresIn);
			return this;
		}

		public Builder issuedAt(final long issuedAt) {
			checkArgument(issuedAt >= 0l, "Invalid issued at");
			pendingUser.setIssuedAt(issuedAt);
			return this;
		}

		public Builder activationCode(final String activationCode) {
			checkArgument(isNotBlank(activationCode), "Uninitialized or invalid activation code");
			pendingUser.setActivationCode(activationCode);
			return this;
		}

		public Builder user(final User user) {
			checkArgument(user != null, "Uninitialized user");
			pendingUser.setUser(user);
			return this;
		}

		public PendingUser build() {
			return pendingUser;
		}

	}

}