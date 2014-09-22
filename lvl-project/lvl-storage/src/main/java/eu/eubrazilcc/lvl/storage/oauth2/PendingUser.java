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
import java.util.Objects;

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
		return Objects.equals(pendingUserId, other.pendingUserId)
				&& Objects.equals(expiresIn, other.expiresIn)
				&& Objects.equals(issuedAt, other.issuedAt)
				&& Objects.equals(activationCode, other.activationCode)
				&& Objects.equals(user, other.user);
	}

	/**
	 * Ignores password and salt fields when comparing two instances of this class. Use this method when comparing an instance of this class that contains
	 * an unprotected password (password in plain text and no salt) with a protected one (hashed password with a valid salt).
	 * @param other - the instance to be compared to.
	 * @return {@code true} if all the attributes of both instances coincide in value with the sole exception of those considered part of the password. 
	 *        Otherwise, {@code false}.
	 */
	public boolean equalsToUnprotected(final PendingUser other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(pendingUserId, other.pendingUserId)
				&& Objects.equals(expiresIn, other.expiresIn)
				&& Objects.equals(issuedAt, other.issuedAt)
				&& Objects.equals(activationCode, other.activationCode)
				&& ((user == null && other.user == null) || (user.equalsToUnprotected(other.user)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(pendingUserId, expiresIn, issuedAt, activationCode, user);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
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

		private final PendingUser instance = new PendingUser();

		public Builder id(final String pendingUserId) {
			checkArgument(isNotBlank(pendingUserId), "Uninitialized or invalid pending user Id");
			instance.setPendingUserId(pendingUserId);
			return this;
		}

		public Builder expiresIn(final long expiresIn) {
			instance.setExpiresIn(expiresIn);
			return this;
		}

		public Builder issuedAt(final long issuedAt) {
			checkArgument(issuedAt >= 0l, "Invalid issued at");
			instance.setIssuedAt(issuedAt);
			return this;
		}

		public Builder activationCode(final String activationCode) {
			checkArgument(isNotBlank(activationCode), "Uninitialized or invalid activation code");
			instance.setActivationCode(activationCode);
			return this;
		}

		public Builder user(final User user) {
			checkArgument(user != null, "Uninitialized user");
			instance.setUser(user);
			return this;
		}

		public PendingUser build() {
			return instance;
		}		

	}

	/**
	 * Performs a deep copy of the input instance.
	 * @param original - the original instance to be copied.
	 * @return a deep copy of the input instance.
	 */
	public static final PendingUser copyOf(final PendingUser original) {
		PendingUser copy = null;
		if (original != null) {
			return builder()
					.id(original.pendingUserId)
					.expiresIn(original.expiresIn)
					.issuedAt(original.issuedAt)
					.activationCode(original.activationCode)
					.user(User.copyOf(original.user))
					.build();
		}
		return copy;
	}

}