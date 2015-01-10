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

import static eu.eubrazilcc.lvl.core.util.NamingUtils.toAsciiSafeName;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ClientAppDAO.CLIENT_APP_DAO;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.generateSecret;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.ext.dynamicreg.server.request.JSONHttpServletRequestWrapper;
import org.apache.oltu.oauth2.ext.dynamicreg.server.request.OAuthServerRegistrationRequest;
import org.apache.oltu.oauth2.ext.dynamicreg.server.response.OAuthServerRegistrationResponse;

import eu.eubrazilcc.lvl.storage.oauth2.ClientApp;

/**
 * Implements the OAuth 2.0 Registration Endpoint using Apache Oltu.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://oltu.apache.org/">Apache Oltu</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
@Path("/register")
public class OAuth2Registration {

	/**
	 * The lifetime in seconds of the registration.
	 */
	public static final long REGISTRATION_EXPIRATION_SECONDS = 946708560l; // 30 years

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response register(final @Context HttpServletRequest request) throws OAuthSystemException {
		try {
			final OAuthServerRegistrationRequest oauthRequest = new OAuthServerRegistrationRequest(
					new JSONHttpServletRequestWrapper(request));
			oauthRequest.discover();

			final ClientApp clientApp = ClientApp.builder()
					.name(oauthRequest.getClientName())
					.url(oauthRequest.getClientUrl())
					.description(oauthRequest.getClientDescription())
					.icon(oauthRequest.getClientIcon())
					.redirectURL(oauthRequest.getRedirectURI())
					.clientId(toAsciiSafeName(oauthRequest.getClientName()))
					.clientSecret(generateSecret())
					.issuedAt(System.currentTimeMillis() / 1000l)
					.expiresIn(REGISTRATION_EXPIRATION_SECONDS)
					.build();
			CLIENT_APP_DAO.insert(clientApp);

			final OAuthResponse response = OAuthServerRegistrationResponse
					.status(HttpServletResponse.SC_OK)
					.setClientId(clientApp.getClientId())
					.setClientSecret(clientApp.getClientSecret())
					.setIssuedAt(Long.toString(clientApp.getIssuedAt()))
					.setExpiresIn(clientApp.getExpiresIn())
					.buildJSONMessage();
			return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

		} catch (OAuthProblemException e) {
			final OAuthResponse response = OAuthServerRegistrationResponse
					.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
					.error(e)
					.buildJSONMessage();
			return Response.status(response.getResponseStatus()).entity(response.getBody()).build();		
		}
	}

}