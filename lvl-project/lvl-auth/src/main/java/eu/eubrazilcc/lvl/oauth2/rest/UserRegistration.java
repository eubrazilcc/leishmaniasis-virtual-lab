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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.oauth2.mail.EmailSender.EMAIL_SENDER;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.PendingUserDAO.PENDING_USER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asSet;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.user;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.storage.oauth2.PendingUser;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.User;

/**
 * Provides new user sign-up logic to identity provider (IdP).
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pending_users")
public class UserRegistration {

	private final static Logger LOGGER = getLogger(UserRegistration.class);

	/**
	 * The lifetime in seconds of the confirmation code.
	 */
	public static final long CONFIRMATION_CODE_EXPIRATION_SECONDS = 86400l; // 1 day	

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " User Registration";
	public static final String RESOURCE_SCOPE = resourceScope(UserRegistration.class);

	@GET
	@Path("{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public PendingUser getPendingUser(final @PathParam("email") String email, 
			final @QueryParam("send_activation") @DefaultValue("false") boolean sendActivation,
			final @Context UriInfo uriInfo) {
		if (isBlank(email)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final PendingUser pendingUser = PENDING_USER_DAO.findByEmail(email);		
		if (pendingUser == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// re-send confirmation code by email
		if (sendActivation) {
			final URI baseUri = uriInfo.getBaseUriBuilder().clone().build();
			sendActivation(baseUri, pendingUser);
		}
		return PendingUser.builder().user(User.builder().email(email).build()).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createPendingUser(final User user, final @Context UriInfo uriInfo,
			final @QueryParam("skip_activation") @DefaultValue("false") boolean skipActivation) {
		if (user == null || isBlank(user.getUsername()) || isBlank(user.getEmail()) || isBlank(user.getPassword())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// verify that no other user exists in the database with the same username or email address
		if (RESOURCE_OWNER_DAO.find(user.getUsername()) != null || RESOURCE_OWNER_DAO.findByEmail(user.getEmail()) != null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// set the correct scopes that the new user must have
		user.setScopes(asSet(user(user.getUsername())));
		// create pending user in the database		
		final PendingUser pendingUser = PendingUser.builder()
				.activationCode(RandomStringUtils.random(8, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()))
				.issuedAt(System.currentTimeMillis() / 1000l)
				.expiresIn(CONFIRMATION_CODE_EXPIRATION_SECONDS)
				.id(user.getUsername())
				.user(user)
				.build();
		PENDING_USER_DAO.insert(pendingUser);
		// send activation code by email
		if (!skipActivation) {
			final URI baseUri = uriInfo.getBaseUriBuilder().clone().build();
			sendActivation(baseUri, pendingUser);
		}
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pendingUser.getUser().getUsername());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{email}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updatePendingUser(final @PathParam("email") String email, final PendingUser update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(email) || update == null || update.getUser() == null 
				|| !email.equals(update.getUser().getEmail()) || isBlank(update.getActivationCode())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final PendingUser pendingUser = PENDING_USER_DAO.findByEmail(email);		
		if (pendingUser == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		if (!PENDING_USER_DAO.isValid(pendingUser.getPendingUserId(), pendingUser.getUser().getUsername(), 
				update.getActivationCode(), false)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// create regular user in the database		
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder()
				.id(pendingUser.getUser().getUsername())
				.user(pendingUser.getUser())
				.build());
		// delete pending user from database
		PENDING_USER_DAO.delete(pendingUser.getPendingUserId());
	}

	private static final void sendActivation(final URI baseUri, final PendingUser pendingUser) {		
		URI portalUri = null;
		try {
			final String portalEndpoint = CONFIG_MANAGER.getPortalEndpoint();
			portalUri = isNotBlank(portalEndpoint) ? new URI(portalEndpoint.replaceAll("/$", "")) 
			: new URI(baseUri.getScheme(), baseUri.getAuthority(), null, null, null);
		} catch (URISyntaxException e) {
			LOGGER.error("Failed to create LVL portal endpoint", e);
		}
		EMAIL_SENDER.sendTextEmail(pendingUser.getUser().getEmail(), emailActivationSubject(), 
				emailActivationMessage(pendingUser.getUser().getUsername(), pendingUser.getUser().getEmail(), pendingUser.getActivationCode(), portalUri));
	}

	private static final String emailActivationSubject() {
		return "Leish VirtLab";
	}

	private static final String emailActivationMessage(final String username, final String email, final String activationCode, final URI portalUri) {
		return "Dear " + username + ",\n\n"
				+ "Thank you for registering at Leishmaniasis Virtual Laboratory. Please, validate your email address in " 
				+ portalUri.toString() + "/#/user/validate" + " "
				+ "using the following code:\n\n" + activationCode + "\n\n"
				+ "You may also validate your email address by clicking on this link or copying and pasting it in your browser:\n\n"
				+ portalUri.toString() + "/#/user/validate/" + email + "/" + activationCode + "\n\n"
				+ "After validating your email address, you can log in the portal directly using email and password used for account registration.\n\n"
				+ "Leish VirtLab team";

	}

}