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

package eu.eubrazilcc.lvl.storage.oauth2.linkedin;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Objects;

/**
 * Provides a challenge that is hard to guess. Used by LinkedIn as part of the access token negotiation to prevent CSRF.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://developer.linkedin.com/docs/oauth2">LinkedIn: Authenticating with OAuth 2.0</a>
 */
public class LinkedInState {

	private String state;
	private long expiresIn;
	private long issuedAt;
	private String redirectUri;
	private String callback;

	public String getState() {
		return state;
	}
	public void setState(final String state) {
		this.state = state;
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
	public String getRedirectUri() {
		return redirectUri;
	}
	public void setRedirectUri(final String redirectUri) {
		this.redirectUri = redirectUri;
	}	
	public String getCallback() {
		return callback;
	}
	public void setCallback(final String callback) {
		this.callback = callback;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof LinkedInState)) {
			return false;
		}
		final LinkedInState other = LinkedInState.class.cast(obj);
		return Objects.equals(state, other.state)				
				&& Objects.equals(expiresIn, other.expiresIn)
				&& Objects.equals(issuedAt, other.issuedAt)
				&& Objects.equals(redirectUri, other.redirectUri)
				&& Objects.equals(callback, other.callback);
	}

	@Override
	public int hashCode() {
		return Objects.hash(state, expiresIn, issuedAt, redirectUri, callback);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("state", state)				
				.add("expiresIn", expiresIn)
				.add("issuedAt", issuedAt)
				.add("redirectUri", redirectUri)
				.add("callback", callback)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final LinkedInState instance = new LinkedInState();

		public Builder state(final String state) {
			checkArgument(isNotBlank(state), "Uninitialized or invalid state");
			instance.setState(state);
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

		public Builder redirectUri(final String redirectUri) {
			checkArgument(isNotBlank(redirectUri), "Uninitialized or invalid redirect URI");
			instance.setRedirectUri(redirectUri);
			return this;
		}
		
		public Builder callback(final String callback) {
			checkArgument(isNotBlank(callback), "Uninitialized or invalid callback");
			instance.setCallback(callback);
			return this;
		}

		public LinkedInState build() {
			return instance;
		}

	}

}