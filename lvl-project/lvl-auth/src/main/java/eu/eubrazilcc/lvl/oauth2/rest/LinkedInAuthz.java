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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.oauth2.rest.jackson.LinkedInMapper.createLinkedInMapper;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.LinkedInStateDAO.LINKEDIN_STATE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.oauthScope;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.LINKEDIN_IDENTITY_PROVIDER;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.USER_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionSet;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.userPermissions;
import static java.lang.Long.parseLong;
import static java.lang.System.currentTimeMillis;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.apache.http.client.fluent.Form.form;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.oauth2.rest.jackson.LinkedInMapper;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.linkedin.LinkedInState;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Provides an authorization service using LinkedIn. 
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/linkedin")
public class LinkedInAuthz {

	/**
	 * The lifetime in seconds of the state.
	 */
	public static final long STATE_EXPIRATION_SECONDS = 1800l; // half an hour	

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " LinkedIn AuthZ Resource";

	protected final static Logger LOGGER = getLogger(LinkedInAuthz.class);

	@POST
	@Path("state")
	@Consumes(APPLICATION_FORM_URLENCODED)
	public Response saveState(final @Context UriInfo uriInfo, final MultivaluedMap<String, String> form) {
		final String secret = parseForm(form, "state");
		final String redirect_uri = parseForm(form, "redirect_uri");
		final String callback = parseForm(form, "callback");
		final LinkedInState state = LinkedInState.builder()
				.state(secret)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(STATE_EXPIRATION_SECONDS)
				.redirectUri(redirect_uri)
				.callback(callback)
				.build();
		LINKEDIN_STATE_DAO.insert(state);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(secret);
		return Response.created(uriBuilder.build()).build();
	}

	@GET
	@Path("callback")
	public Response authorize(final @Context HttpServletRequest request) {
		final String code = parseQuery(request, "code");
		final String state = parseQuery(request, "state");
		final AtomicReference<String> redirectUriRef = new AtomicReference<String>(), callbackRef = new AtomicReference<String>();		
		if (!LINKEDIN_STATE_DAO.isValid(state, redirectUriRef, callbackRef)) {
			throw new NotAuthorizedException(status(UNAUTHORIZED).build());
		}
		URI callback_uri = null;
		Map<String, String> map = null;
		try {
			final List<NameValuePair> form = form()
					.add("grant_type", "authorization_code")
					.add("code", code)
					.add("redirect_uri", redirectUriRef.get())
					.add("client_id", CONFIG_MANAGER.getLinkedInAPIKey())
					.add("client_secret", CONFIG_MANAGER.getLinkedInSecretKey())
					.build();
			// exchange authorization code for a request token
			final long issuedAt = currentTimeMillis() / 1000l;
			String response = Request.Post("https://www.linkedin.com/uas/oauth2/accessToken")
					.addHeader("Accept", "application/json")
					.bodyForm(form)
					.execute()
					.returnContent()
					.asString();
			map = JSON_MAPPER.readValue(response, new TypeReference<HashMap<String,String>>() {});
			final String accessToken = map.get("access_token");
			final long expiresIn = parseLong(map.get("expires_in"));
			checkState(isNotBlank(accessToken), "Uninitialized or invalid access token: " + accessToken);
			checkState(expiresIn > 0l, "Uninitialized or invalid expiresIn: " + expiresIn);
			// retrieve user profile data
			final URIBuilder uriBuilder = new URIBuilder("https://api.linkedin.com/v1/people/~:(id,first-name,last-name,industry,positions,email-address)");
			uriBuilder.addParameter("format", "json");
			response = Request.Get(uriBuilder.build())
					.addHeader("Authorization", "Bearer " + accessToken)
					.execute()
					.returnContent()
					.asString();
			final LinkedInMapper linkedInMapper = createLinkedInMapper().readObject(response);
			final String userId = linkedInMapper.getUserId();
			final String emailAddress = linkedInMapper.getEmailAddress();
			// register or update user in the database
			final String ownerid = toResourceOwnerId(LINKEDIN_IDENTITY_PROVIDER, userId);
			ResourceOwner owner = RESOURCE_OWNER_DAO.find(ownerid);
			if (owner == null) {
				final User user = User.builder()
						.userid(userId)
						.provider(LINKEDIN_IDENTITY_PROVIDER)
						.email(emailAddress)
						.password("password")
						.firstname(linkedInMapper.getFirstName())
						.lastname(linkedInMapper.getLastName())
						.industry(linkedInMapper.getIndustry().orNull())
						.positions(linkedInMapper.getPositions().orNull())
						.roles(newHashSet(USER_ROLE))
						.permissions(asPermissionSet(userPermissions(ownerid)))
						.build();
				owner = ResourceOwner.builder()
						.user(user)
						.build();
				RESOURCE_OWNER_DAO.insert(owner);
			} else {
				owner.getUser().setEmail(emailAddress);
				owner.getUser().setFirstname(linkedInMapper.getFirstName());
				owner.getUser().setLastname(linkedInMapper.getLastName());
				owner.getUser().setIndustry(linkedInMapper.getIndustry().orNull());
				owner.getUser().setPositions(linkedInMapper.getPositions().orNull());
				RESOURCE_OWNER_DAO.update(owner);
			}
			// register access token in the database
			final AccessToken accessToken2 = AccessToken.builder()
					.token(accessToken)
					.issuedAt(issuedAt)
					.expiresIn(expiresIn)
					.scope(asPermissionList(oauthScope(owner, false)))
					.ownerId(ownerid)
					.build();
			TOKEN_DAO.insert(accessToken2);
			// redirect to default portal endpoint
			callback_uri = new URI(callbackRef.get() + "?email=" + urlEncodeUtf8(emailAddress) + "&access_token=" + urlEncodeUtf8(accessToken));			
		} catch (Exception e) {
			String errorCode = null, message = null, status = null;
			if (e instanceof IllegalStateException && map != null) {
				errorCode = map.get("errorCode");
				message = map.get("message");
				status = map.get("status");
			}
			LOGGER.error("Failed to authorize LinkedIn user [errorCode=" + errorCode + ", status=" + status 
					+ ", message=" + message + "]", e);
			throw new WebApplicationException(status(INTERNAL_SERVER_ERROR)
					.header("WWW-Authenticate", "Bearer realm='" + RESOURCE_NAME + "', error='invalid-code'")
					.build());
		} finally {
			LINKEDIN_STATE_DAO.delete(state);
		}
		return Response.seeOther(callback_uri).build();
	}

	private static String parseForm(final MultivaluedMap<String, String> form, final String field) {
		String value = null;
		if (form == null || form.get(field) == null || form.get(field).size() != 1 || isBlank(trimToNull(value = form.getFirst(field)))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		return value;
	}

	private static String parseQuery(final HttpServletRequest request, final String param) {
		String value = null;
		if (request == null || request.getParameterValues(param) == null || request.getParameterValues(param).length != 1 
				|| isBlank(trimToNull(value = request.getParameter(param)))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		return value;
	}	

}