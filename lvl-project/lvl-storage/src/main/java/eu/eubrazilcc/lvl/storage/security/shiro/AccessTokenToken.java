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

package eu.eubrazilcc.lvl.storage.security.shiro;

import static com.google.common.base.MoreObjects.toStringHelper;

import javax.annotation.Nullable;

import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;

/**
 * An access token to support token-based authorization mechanisms, such as OAuth 2.0. This class also implements the {@link RememberMeAuthenticationToken}
 * interface to support "Remember Me" services across user sessions as well as the {@link HostAuthenticationToken} interface to retain the host 
 * name or IP address location from where the authentication attempt is occurring.
 * @author Erik Torres <ertorser@upv.es>
 */
public class AccessTokenToken implements HostAuthenticationToken, RememberMeAuthenticationToken {

	private static final long serialVersionUID = -8799579311590816304L;

	private String token;
	private boolean rememberMe = false;
	private String host;

	/**
	 * JavaBeans compatible no-arguments constructor.
	 */
	public AccessTokenToken() {
	}

	/**
	 * Constructs a new instance encapsulating the token submitted for authentication, with a 
	 * <tt>null</tt> {@link #getHost() host} and the <tt>rememberMe</tt> default of <tt>false</tt>.
	 * @param token
	 */
	public AccessTokenToken(final String token) {
		this(token, false, null);
	}
	
	/**
	 * Constructs a new instance encapsulating the information submitted for authentication.
	 * @param token the access token submitted for authentication
	 * @param rememberMe if the user wishes their identity to be remembered across sessions
	 */
	public AccessTokenToken(final String token, final boolean rememberMe) {
		this(token, rememberMe, null);
	}

	/**
	 * Constructs a new instance encapsulating the information submitted for authentication.
	 * @param token the access token submitted for authentication
	 * @param rememberMe if the user wishes their identity to be remembered across sessions
	 * @param host the host name or IP string from where the attempt is occurring
	 */
	public AccessTokenToken(final String token, final boolean rememberMe, final @Nullable String host) {
		this.token = token;
		this.rememberMe = rememberMe;
		this.host = host;
	}

	/**
	 * Gets the access token submitted during an authentication attempt.
	 * @return the access token submitted during an authentication attempt.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the access token for submission during an authentication attempt.
	 * @param token the access token to be used for submission during an authentication attempt.
	 */
	public void setToken(final String token) {
		this.token = token;
	}

	@Override
	public Object getCredentials() {
		return getToken();
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

	@Override
	public boolean isRememberMe() {
		return rememberMe;		
	}

	public void setRememberMe(final boolean rememberMe) {
		this.rememberMe = rememberMe;
	}

	@Override
	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public void clear() {
		this.token = null;
		this.host = null;
		this.rememberMe = false;
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("token", token)
				.add("rememberMe", rememberMe)
				.add("host", host)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final AccessTokenToken instance = new AccessTokenToken();

		public Builder token(final String token) {
			instance.setToken(token);
			return this;
		}

		public Builder rememberMe(final boolean rememberMe) {
			instance.setRememberMe(rememberMe);
			return this;
		}

		public Builder host(final String host) {
			instance.setHost(host);			
			return this;
		}

		public AccessTokenToken build() {
			return instance;
		}

	}

}