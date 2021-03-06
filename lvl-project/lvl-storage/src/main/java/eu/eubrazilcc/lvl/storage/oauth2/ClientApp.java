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

package eu.eubrazilcc.lvl.storage.oauth2;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Client application. Provides a validating, fluent API for object initialization.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ClientApp implements Serializable {

	private static final long serialVersionUID = -6814304647527533515L;

	private String name;
	private URI url;
	private String description;
	private URI icon;
	private URI redirectURL;
	private String clientId;
	private String clientSecret;
	private long expiresIn;
	private long issuedAt;

	public ClientApp() { }

	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public URI getUrl() {
		return url;
	}
	public void setUrl(final URI url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(final String description) {
		this.description = description;
	}
	public URI getIcon() {
		return icon;
	}
	public void setIcon(final URI icon) {
		this.icon = icon;
	}
	public URI getRedirectURL() {
		return redirectURL;
	}
	public void setRedirectURL(final URI redirectURL) {
		this.redirectURL = redirectURL;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(final String clientSecret) {
		this.clientSecret = clientSecret;
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
		if (obj == null || !(obj instanceof ClientApp)) {
			return false;
		}
		final ClientApp other = ClientApp.class.cast(obj);
		return Objects.equals(name, other.name)
				&& Objects.equals(url, other.url)
				&& Objects.equals(description, other.description)
				&& Objects.equals(icon, other.icon)
				&& Objects.equals(redirectURL, other.redirectURL)
				&& Objects.equals(clientId, other.clientId)				
				&& Objects.equals(clientSecret, other.clientSecret)
				&& Objects.equals(expiresIn, other.expiresIn)
				&& Objects.equals(issuedAt, other.issuedAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, url, description, icon, redirectURL, clientId,
				clientSecret, expiresIn, issuedAt);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("name", name)
				.add("url", url)
				.add("description", description)
				.add("icon", icon)
				.add("redirectURL", redirectURL)
				.add("clientId", clientId)
				.add("clientSecret", clientSecret)
				.add("expiresIn", expiresIn)
				.add("issuedAt", issuedAt)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ClientApp instance = new ClientApp();

		public Builder name(final String name) {
			checkArgument(isNotBlank(name), "Uninitialized or invalid name");
			instance.setName(name);
			return this;
		}

		public Builder url(final String url) {
			checkArgument(isNotBlank(url), "Uninitialized or invalid URL");
			try {				
				instance.setUrl(new URI(url));
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Invalid URL", e);
			}
			return this;
		}

		public Builder description(final String description) {
			checkArgument(isNotBlank(description), "Uninitialized or invalid description");
			instance.setDescription(description);
			return this;
		}

		public Builder icon(final String icon) {
			checkArgument(isNotBlank(icon), "Uninitialized or invalid icon");
			try {
				instance.setIcon(new URI(icon));
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Invalid icon URL", e);
			}
			return this;
		}

		public Builder redirectURL(final String redirectURL) {
			checkArgument(isNotBlank(redirectURL), "Uninitialized or invalid redirect URL");
			try {
				instance.setRedirectURL(new URI(redirectURL));
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException("Invalid icon redirect URL", e);
			}
			return this;
		}

		public Builder clientId(final String clientId) {
			checkArgument(isNotBlank(clientId), "Uninitialized or invalid client Id");
			instance.setClientId(clientId);
			return this;
		}

		public Builder clientSecret(final String clientSecret) {
			checkArgument(isNotBlank(clientSecret), "Uninitialized or invalid client secret");
			instance.setClientSecret(clientSecret);
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

		public ClientApp build() {
			return instance;
		}

	}

}