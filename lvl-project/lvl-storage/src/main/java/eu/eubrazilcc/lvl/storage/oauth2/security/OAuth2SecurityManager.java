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

package eu.eubrazilcc.lvl.storage.oauth2.security;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.servlet.ServletUtils.getClientAddress;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.AUTHORIZATION_HEADER_OAUTH2;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.AUTHORIZATION_QUERY_OAUTH2;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.oltu.oauth2.common.OAuth.HeaderType.WWW_AUTHENTICATE;
import static org.apache.oltu.oauth2.common.error.OAuthError.ResourceResponse.INVALID_TOKEN;
import static org.apache.oltu.oauth2.common.message.types.ParameterStyle.HEADER;
import static org.apache.oltu.oauth2.common.message.types.ParameterStyle.QUERY;
import static org.apache.shiro.SecurityUtils.getSubject;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.servlet.OAuth2RequestWrapper;
import eu.eubrazilcc.lvl.storage.security.BaseSecurityManager;

/**
 * Extends the security manager with methods that throws Web exceptions when user authentication/authorization fails.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class OAuth2SecurityManager extends BaseSecurityManager {

	private static final Logger LOGGER = getLogger(OAuth2SecurityManager.class);

	private final String resourceName;
	private final String clientAddress;

	public OAuth2SecurityManager(final String resourceName, final String clientAddress) {
		super(getSubject());
		this.resourceName = resourceName;
		this.clientAddress = clientAddress;
	}

	public static final OAuth2SecurityManager login(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers,
			final String resourceName) {		
		try {
			final String clientAddress = getClientAddress(request);
			final OAuth2SecurityManager instance = new OAuth2SecurityManager(resourceName, clientAddress);
			try {
				instance.login(getOAuth2AccessToken(request, form, headers, resourceName));
			} catch (AuthenticationException ae) {
				LOGGER.error("Authentication failed", ae);
				invalidCredentialError(resourceName, clientAddress);
			}
			return instance;		
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(status(INTERNAL_SERVER_ERROR)
					.header("WWW-Authenticate", "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	public OAuth2SecurityManager requiresAuthentication() {
		try {
			if (!isAuthenticated()) {
				invalidCredentialError(resourceName, clientAddress);
			}
			return this;
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(status(INTERNAL_SERVER_ERROR)
					.header("WWW-Authenticate", "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	public OAuth2SecurityManager requiresRoles(final Collection<String> roleIdentifiers) {
		try {
			if (!hasAllRoles(roleIdentifiers)) {
				invalidCredentialError(resourceName, clientAddress);
			}
			return this;
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(status(INTERNAL_SERVER_ERROR)
					.header("WWW-Authenticate", "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	public OAuth2SecurityManager requiresPermissions(final String... permissions) {
		try {
			if (!isPermittedAll(permissions)) {
				invalidCredentialError(resourceName, clientAddress);
			}
			return this;
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(status(INTERNAL_SERVER_ERROR)
					.header("WWW-Authenticate", "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	private static final String getOAuth2AccessToken(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers,
			final String resourceName) throws OAuthSystemException {
		try {
			// make the OAuth request out of this request
			final OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(new OAuth2RequestWrapper(request, form, headers), 
					isNotBlank(request.getParameter(AUTHORIZATION_QUERY_OAUTH2)) ? QUERY : HEADER);
			// get the access token
			return oauthRequest.getAccessToken();
		} catch (OAuthProblemException e) {
			// check if the error code has been set
			final String errorCode = e.getError();
			if (OAuthUtils.isEmpty(errorCode)) {
				// setup the OAuth error message
				final OAuthResponse oauthResponse = OAuthRSResponse
						.errorResponse(SC_UNAUTHORIZED)
						.setRealm(resourceName)
						.buildHeaderMessage();
				LOGGER.warn("Access from " + getClientAddress(request) + " is denied due to: bad request", e);
				// if no error code then throw a standard 401 unauthorized error
				throw new WebApplicationException(status(UNAUTHORIZED)
						.header(WWW_AUTHENTICATE, oauthResponse.getHeader(WWW_AUTHENTICATE))
						.build());
			}
			final OAuthResponse oauthResponse = OAuthRSResponse
					.errorResponse(SC_UNAUTHORIZED)
					.setRealm(resourceName)
					.setError(e.getError())
					.setErrorDescription(e.getDescription())
					.setErrorUri(e.getUri())
					.buildHeaderMessage();
			LOGGER.warn("Access from " + getClientAddress(request) + " is denied due to: " + e.getError() + "(" + e.getDescription() + ")");
			throw new WebApplicationException(status(BAD_REQUEST)
					.header(WWW_AUTHENTICATE, oauthResponse.getHeader(WWW_AUTHENTICATE))
					.build());
		}
	}

	private static final void invalidCredentialError(final String resourceName, final String clientAddress) throws OAuthSystemException {
		// setup the OAuth error message
		final OAuthResponse oauthResponse = OAuthRSResponse
				.errorResponse(SC_UNAUTHORIZED)
				.setRealm(resourceName)
				.setError(INVALID_TOKEN)
				.buildHeaderMessage();
		LOGGER.warn("Access from " + clientAddress + " is denied due to: invalid credentials");
		throw new WebApplicationException(status(UNAUTHORIZED)
				.header(WWW_AUTHENTICATE, oauthResponse.getHeader(WWW_AUTHENTICATE))
				.build());
	}

	public static final String bearerHeader(final String token) {
		checkArgument(isNotBlank(token), "Uninitialized or invalid token");
		return AUTHORIZATION_HEADER_OAUTH2 + token;
	}

	public static MultivaluedMap<String, String> authzHeader(final String token) {
		checkArgument(isNotBlank(token), "Uninitialized or invalid token");
		final MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
		map.put(HEADER_AUTHORIZATION, newArrayList(bearerHeader(token)));
		return map;
	}	

}