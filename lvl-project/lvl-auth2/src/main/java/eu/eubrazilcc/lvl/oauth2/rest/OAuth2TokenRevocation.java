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

import static eu.eubrazilcc.lvl.storage.oauth2.dao.ClientAppDAO.CLIENT_APP_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import eu.eubrazilcc.lvl.core.servlet.OAuth2RequestWrapper;
import eu.eubrazilcc.lvl.oauth2.revocation.OAuthTokenRevocationRequest;

/**
 * Implements the OAuth 2.0 Token Endpoint. This functionality is not supported natively by 
 * Apache Oltu, but some components of this framework are used in this service.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://tools.ietf.org/html/rfc7009">RFC7009</a> - OAuth 2.0 Token Revocation
 */
@Path("/revoke")
public class OAuth2TokenRevocation {

	public static String UNSUPPORTED_TOKEN_TYPE_ERROR = "unsupported_token_type";

	public static final String INVALID_CLIENT_DESCRIPTION = "Client authentication failed (e.g., unknown client, no client authentication included, or unsupported authentication method)";

	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response revoke(final @Context HttpServletRequest request, final MultivaluedMap<String, String> form) throws OAuthSystemException {
		try {
			final OAuthTokenRevocationRequest oauthRequest = new OAuthTokenRevocationRequest(new OAuth2RequestWrapper(request, form, null));			

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

			// do revoking for different token types
			if (isEmpty(oauthRequest.getParam(OAuthTokenRevocationRequest.TOKEN_TYPE_HINT)) 
					|| oauthRequest.getParam(OAuthTokenRevocationRequest.TOKEN_TYPE_HINT).equals(OAuthTokenRevocationRequest.ACCESS_TOKEN)) {
				TOKEN_DAO.delete(oauthRequest.getParam(OAuthTokenRevocationRequest.TOKEN));
			} else if (oauthRequest.getParam(OAuthTokenRevocationRequest.TOKEN_TYPE_HINT).equals(OAuthTokenRevocationRequest.REFRESH_TOKEN)) {
				// refresh token is not supported in this implementation
				final OAuthResponse response = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
						.setError(UNSUPPORTED_TOKEN_TYPE_ERROR)
						.setErrorDescription("refresh token is not supported in this implementation")
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			} else {
				final OAuthResponse response = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
						.setError(UNSUPPORTED_TOKEN_TYPE_ERROR)
						.setErrorDescription("unsupported token type: " + oauthRequest.getParam(OAuthTokenRevocationRequest.TOKEN_TYPE_HINT))
						.buildJSONMessage();
				return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
			}

			return Response.status(HttpServletResponse.SC_OK).build();
		} catch (OAuthProblemException e) {
			final OAuthResponse res = OAuthASResponse
					.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
					.error(e)
					.buildJSONMessage();
			return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
		}
	}

}