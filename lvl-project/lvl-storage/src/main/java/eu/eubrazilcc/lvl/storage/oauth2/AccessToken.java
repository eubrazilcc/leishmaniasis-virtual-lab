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
import static com.google.common.collect.Iterables.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Objects;

/**
 * OAuth2 access token.
 * @author Erik Torres <ertorser@upv.es>
 */
public class AccessToken implements Serializable {

	private static final long serialVersionUID = -6366221908943426735L;

	private String token;
	private long expiresIn;
	private long issuedAt;
	private Set<String> scopes;

	public AccessToken() { }

	public String getToken() {
		return token;
	}
	public void setToken(final String token) {
		this.token = token;
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
	public Set<String> getScopes() {
		return scopes;
	}
	public void setScopes(final Set<String> scopes) {
		this.scopes = scopes;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof AccessToken)) {
			return false;
		}
		final AccessToken other = AccessToken.class.cast(obj);
		return Objects.equal(token, other.token)				
				&& Objects.equal(expiresIn, other.expiresIn)
				&& Objects.equal(issuedAt, other.issuedAt)
				&& Objects.equal(scopes, other.scopes);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(token, expiresIn, issuedAt, scopes);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("token", token)				
				.add("expiresIn", expiresIn)
				.add("issuedAt", issuedAt)
				.add("scopes", scopes)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final AccessToken accessToken = new AccessToken();

		public Builder() {
			accessToken.setScopes(new HashSet<String>());
		}

		public Builder token(final String token) {
			checkArgument(StringUtils.isNotBlank(token), "Uninitialized or invalid token");
			accessToken.setToken(token);
			return this;
		}

		public Builder expiresIn(final long expiresIn) {
			accessToken.setExpiresIn(expiresIn);
			return this;
		}

		public Builder issuedAt(final long issuedAt) {
			checkArgument(issuedAt >= 0l, "Invalid issued at");
			accessToken.setIssuedAt(issuedAt);
			return this;
		}
		
		public Builder scope(final String scope) {
			checkArgument(StringUtils.isNotBlank(scope), "Uninitialized or invalid scope");
			accessToken.getScopes().add(scope);
			return this;
		}
		
		public Builder scope(final Collection<String> scopes) {
			checkArgument(scopes != null && !isEmpty(scopes), "Uninitialized scopes");
			accessToken.getScopes().addAll(scopes);
			return this;
		}

		public AccessToken build() {
			return accessToken;
		}

	}

}