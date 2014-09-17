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
 * OAuth2 access code.
 * @author Erik Torres <ertorser@upv.es>
 */
public class AuthCode implements Serializable {
	
	private static final long serialVersionUID = -5271391065299414494L;
	
	private String code;
	private long expiresIn;
	private long issuedAt;

	public AuthCode() { }

	public String getCode() {
		return code;
	}
	public void setCode(final String code) {
		this.code = code;
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
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof AuthCode)) {
			return false;
		}
		final AuthCode other = AuthCode.class.cast(obj);
		return Objects.equal(code, other.code)				
				&& Objects.equal(expiresIn, other.expiresIn)
				&& Objects.equal(issuedAt, other.issuedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(code, expiresIn, issuedAt);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("code", code)				
				.add("expiresIn", expiresIn)
				.add("issuedAt", issuedAt)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final AuthCode instance = new AuthCode();

		public Builder code(final String code) {
			checkArgument(isNotBlank(code), "Uninitialized or invalid code");
			instance.setCode(code);
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

		public AuthCode build() {
			return instance;
		}

	}
	
}