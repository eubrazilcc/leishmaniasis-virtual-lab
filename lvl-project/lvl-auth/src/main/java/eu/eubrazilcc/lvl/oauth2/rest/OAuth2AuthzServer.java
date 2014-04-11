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

package eu.eubrazilcc.lvl.oauth2.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;

import eu.eubrazilcc.lvl.oauth2.security.OAuth2TokenGenerator;
import eu.eubrazilcc.lvl.storage.oauth2.AuthCode;
import eu.eubrazilcc.lvl.storage.oauth2.dao.AuthCodeDAO;

/**
 * Implements the OAuth 2.0 End User Authorization Endpoint using Apache Oltu.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server">OAuth 2.0 Authorization Server</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
@Path("/authz")
public class OAuth2AuthzServer {

	/**
	 * The lifetime in seconds of the access token.
	 */
	public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600l; // 1 hour

	@GET
	public Response authorize(final @Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {		
		try {
			final OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
			final OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new OAuth2TokenGenerator());

			// build response according to response_type
			final String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

			final OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
					.authorizationResponse(request, HttpServletResponse.SC_FOUND);

			if (responseType.equals(ResponseType.CODE.toString())) {
				final AuthCode authCode = AuthCode.builder()
						.code(oauthIssuerImpl.authorizationCode())
						.build();
				AuthCodeDAO.INSTANCE.insert(authCode);
				builder.setCode(authCode.getCode());
			}
			if (responseType.equals(ResponseType.TOKEN.toString())) {
				final AuthCode accessToken = AuthCode.builder()
						.code(oauthIssuerImpl.accessToken())
						.issuedAt(System.currentTimeMillis() / 1000l)
						.expiresIn(ACCESS_TOKEN_EXPIRATION_SECONDS)
						.build();
				AuthCodeDAO.INSTANCE.insert(accessToken);
				builder.setAccessToken(accessToken.getCode());
				builder.setExpiresIn(accessToken.getExpiresIn());
			}

			final String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

			final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
			final URI url = new URI(response.getLocationUri());

			return Response.status(response.getResponseStatus()).location(url).build();
		} catch (OAuthProblemException e) {
			final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

			final String redirectUri = e.getRedirectUri();

			if (OAuthUtils.isEmpty(redirectUri)) {
				throw new WebApplicationException(responseBuilder.entity("OAuth2 callback URL needs to be provided by client").build());
			}
			final OAuthResponse response = OAuthASResponse
					.errorResponse(HttpServletResponse.SC_FOUND)
					.error(e)
					.location(redirectUri).buildQueryMessage();
			final URI location = new URI(response.getLocationUri());
			return responseBuilder.location(location).build();
		}
	}

}