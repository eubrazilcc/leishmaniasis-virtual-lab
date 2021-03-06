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

package eu.eubrazilcc.lvl.oauth2.rest;

import static eu.eubrazilcc.lvl.storage.oauth2.dao.AuthCodeDAO.AUTH_CODE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ClientAppDAO.CLIENT_APP_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.defaultPermissions;
import static java.lang.System.currentTimeMillis;

import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import eu.eubrazilcc.lvl.core.servlet.OAuth2RequestWrapper;
import eu.eubrazilcc.lvl.oauth2.security.OAuth2TokenGenerator;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;

/**
 * Implements the OAuth 2.0 Token Endpoint using Apache Oltu.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server">OAuth 2.0 Authorization Server</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
@Path("/token")
public class OAuth2Token {

	/**
	 * The lifetime in seconds of the access token.
	 */
	public static final long TOKEN_EXPIRATION_SECONDS = 604800l; // 1 week

	public static final String USE_EMAIL = "use_email";

	public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method)";	

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response authorize(final @Context HttpServletRequest request, final MultivaluedMap<String, String> form) throws OAuthSystemException {
		try {
			final OAuthTokenRequest oauthRequest = new OAuthTokenRequest(new OAuth2RequestWrapper(request, form, null));
			final OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new OAuth2TokenGenerator());			
			String scope = null, ownerId = null;

			// check if client id is valid
			if (!CLIENT_APP_DAO.isValid(oauthRequest.getClientId())) {
				final OAuthResponse response = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
						.setError(OAuthError.TokenResponse.INVALID_CLIENT)
						.setErrorDescription(INVALID_CLIENT_DESCRIPTION)
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			}			

			// check if client secret is valid
			if (!CLIENT_APP_DAO.isValid(oauthRequest.getClientId(), oauthRequest.getClientSecret())) {
				final OAuthResponse response = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
						.setErrorDescription(INVALID_CLIENT_DESCRIPTION)
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			}

			// do checking for different grant types
			if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
				if (!AUTH_CODE_DAO.isValid(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
					final OAuthResponse response = OAuthASResponse
							.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
							.setError(OAuthError.TokenResponse.INVALID_GRANT)
							.setErrorDescription("invalid authorization code")
							.buildJSONMessage();
					return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
				}
			} else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
				final AtomicReference<String> scopeRef = new AtomicReference<String>(), ownerIdRef = new AtomicReference<String>();
				if (!RESOURCE_OWNER_DAO.isValid(toResourceOwnerId(oauthRequest.getUsername()), oauthRequest.getUsername(), oauthRequest.getPassword(), 
						"true".equalsIgnoreCase(oauthRequest.getParam(USE_EMAIL)), scopeRef, ownerIdRef)) {
					final OAuthResponse response = OAuthASResponse
							.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
							.setError(OAuthError.TokenResponse.INVALID_GRANT)
							.setErrorDescription("invalid username or password")
							.buildJSONMessage();
					return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
				}
				scope = scopeRef.get();
				ownerId = ownerIdRef.get();
			} else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
				// refresh token is not supported in this implementation
				final OAuthResponse response = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
						.setError(OAuthError.TokenResponse.INVALID_GRANT)
						.setErrorDescription("refresh token is not supported in this implementation")
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			} else {
				final OAuthResponse response = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
						.setError(OAuthError.TokenResponse.INVALID_GRANT)
						.setErrorDescription("unsupported grant type: " + oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			}

			// set scope, depending on user or default scope if grant type is code
			scope = (scope != null ? scope : defaultPermissions());

			final AccessToken accessToken = AccessToken.builder()
					.token(oauthIssuerImpl.accessToken())
					.issuedAt(currentTimeMillis() / 1000l)
					.expiresIn(TOKEN_EXPIRATION_SECONDS)
					.scope(asPermissionList(scope))
					.ownerId(ownerId)
					.build();
			TOKEN_DAO.insert(accessToken);

			final OAuthResponse response = OAuthASResponse
					.tokenResponse(HttpServletResponse.SC_OK)
					.setAccessToken(accessToken.getToken())
					.setExpiresIn(Long.toString(accessToken.getExpiresIn()))
					.setScope(scope)
					.buildJSONMessage();
			return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

		} catch (OAuthProblemException e) {
			final OAuthResponse res = OAuthASResponse
					.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
					.error(e)
					.buildJSONMessage();
			return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
		}
	}

}