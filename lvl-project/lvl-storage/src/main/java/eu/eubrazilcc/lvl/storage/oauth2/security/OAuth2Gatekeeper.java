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
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
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

import eu.eubrazilcc.lvl.core.servlet.OAuth2RequestWrapper;

/**
 * OAuth2 gatekeeper that validates the access token and grant access to the resource. It receives 
 * the validating token in the header of the request.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server">OAuth 2.0 Authorization Server</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
public final class OAuth2Gatekeeper {

	private static final Logger LOGGER = getLogger(OAuth2Gatekeeper.class);

	public static final String bearerHeader(final String token) {
		checkArgument(isNotBlank(token), "Uninitialized or invalid token");
		return OAuth2Common.AUTHORIZATION_HEADER_OAUTH2 + token;
	}

	public static final void authorize(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers, 
			final String resourceScope,
			final boolean requestFullAccess,
			final String resourceName) {
		try {
			authorizeInternal(request, form, headers, resourceScope, requestFullAccess, resourceName);
		} catch (OAuthSystemException e) {
			LOGGER.error("Authorization failed", e);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.header(OAuth.HeaderType.WWW_AUTHENTICATE, "Bearer realm='" + resourceName + "', error='invalid-token'")
					.build());
		}
	}

	private static final void authorizeInternal(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers, 
			final String resourceScope,
			final boolean requestFullAccess,
			final String resourceName) throws OAuthSystemException {
		try {
			// make the OAuth request out of this request
			final OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(
					new OAuth2RequestWrapper(request, form, headers), ParameterStyle.HEADER);		

			// get the access token
			final String accessToken = oauthRequest.getAccessToken();

			// validate the access token
			if (!TOKEN_DAO.isValid(accessToken, resourceScope, requestFullAccess)) {
				// setup the OAuth error message
				final OAuthResponse oauthResponse = OAuthRSResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setRealm(resourceName)
						.setError(OAuthError.ResourceResponse.INVALID_TOKEN)
						.buildHeaderMessage();

				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
						.build());
			}
		} catch (OAuthProblemException e) {
			// check if the error code has been set
			final String errorCode = e.getError();
			if (OAuthUtils.isEmpty(errorCode)) {
				// setup the OAuth error message
				final OAuthResponse oauthResponse = OAuthRSResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setRealm(resourceName)
						.buildHeaderMessage();

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

			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.header(OAuth.HeaderType.WWW_AUTHENTICATE, oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
					.build());
		}
	}

}