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
import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.servlet.ServletUtils.getClientAddress;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.AUTHORIZATION_HEADER_OAUTH2;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.servlet.OAuth2RequestWrapper;

/**
 * OAuth2 gatekeeper that validates the access token and grant access to the resource. It receives 
 * the validating token in the header of the request. Tokens sent in the body of the request are silently
 * ignored.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server">OAuth 2.0 Authorization Server</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
public final class OAuth2Gatekeeper {

	private static final Logger LOGGER = getLogger(OAuth2Gatekeeper.class);

	public static final String OWNER_ID = "owner_id";
	public static final String ACCESS_TYPE = "access_type";
	public static final String RESOURCE_WIDE_ACCESS = "resource_access";
	public static final String USER_ACCESS = "user_access";

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

	public static final ImmutableMap<String, String> authorize(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers, 
			final String resourceScope,
			final boolean requestFullAccess,
			final boolean addResourceOwner,
			final String resourceName) {
		try {
			return authorizeInternal(request, form, headers, resourceScope, requestFullAccess, addResourceOwner, resourceName);
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.header(OAuth.HeaderType.WWW_AUTHENTICATE, "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	public static final ImmutableMap<String, String> checkAuthenticateAccess(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers,
			final String resourceName) {
		try {
			return authorizeInternal(request, form, headers, null, false, false, resourceName);
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.header(OAuth.HeaderType.WWW_AUTHENTICATE, "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	private static final ImmutableMap<String, String> authorizeInternal(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers, 
			final @Nullable String resourceScope,
			final boolean requestFullAccess,
			final boolean addResourceOwner,
			final String resourceName) throws OAuthSystemException {
		try {
			ImmutableMap<String, String> access = null;

			// make the OAuth request out of this request
			final OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(
					new OAuth2RequestWrapper(request, form, headers), ParameterStyle.HEADER);		

			// get the access token
			final String accessToken = oauthRequest.getAccessToken();

			// validate the access token
			if (resourceScope != null) {
				// resource that requires special permissions, start by checking if the holder of the token has access to the root of 
				// the resource (regardless of the identity of the resource owner) and then check user-based access
				final AtomicReference<String> ownerIdRef = new AtomicReference<String>();
				if (TOKEN_DAO.isValid(accessToken, resourceScope, requestFullAccess, false, ownerIdRef)) {					
					access = of(OWNER_ID, ownerIdRef.get(), ACCESS_TYPE, RESOURCE_WIDE_ACCESS);
				} else if (addResourceOwner) {
					if (TOKEN_DAO.isValid(accessToken, resourceScope, requestFullAccess, addResourceOwner, ownerIdRef)) {						
						access = of(OWNER_ID, ownerIdRef.get(), ACCESS_TYPE, USER_ACCESS);
					}
				}
				if (access == null || isBlank(access.get(OWNER_ID)) || isBlank(access.get(ACCESS_TYPE))) {
					invalidCredentialError(request, resourceName);
				}
			} else {
				// resource that only requires authenticated access
				if (!TOKEN_DAO.isValid(accessToken)) {
					invalidCredentialError(request, resourceName);
				}
			}
			return access;

		} catch (OAuthProblemException e) {
			// check if the error code has been set
			final String errorCode = e.getError();
			if (OAuthUtils.isEmpty(errorCode)) {
				// setup the OAuth error message
				final OAuthResponse oauthResponse = OAuthRSResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setRealm(resourceName)
						.buildHeaderMessage();
				LOGGER.warn("Access from " + getClientAddress(request) + " is denied due to: bad request");
				// if no error code then throw a standard 401 unauthorized error
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
						.build());
			}

			final OAuthResponse oauthResponse = OAuthRSResponse
					.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
					.setRealm(resourceName)
					.setError(e.getError())
					.setErrorDescription(e.getDescription())
					.setErrorUri(e.getUri())
					.buildHeaderMessage();
			LOGGER.warn("Access from " + getClientAddress(request) + " is denied due to: " + e.getError() + "(" + e.getDescription() + ")");
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
					.build());
		}
	}

	private static final void invalidCredentialError(final HttpServletRequest request, final String resourceName) throws OAuthSystemException {
		// setup the OAuth error message
		final OAuthResponse oauthResponse = OAuthRSResponse
				.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
				.setRealm(resourceName)
				.setError(OAuthError.ResourceResponse.INVALID_TOKEN)
				.buildHeaderMessage();
		LOGGER.warn("Access from " + getClientAddress(request) + " is denied due to: invalid credentials");
		throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
				.header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
				.build());
	}

}