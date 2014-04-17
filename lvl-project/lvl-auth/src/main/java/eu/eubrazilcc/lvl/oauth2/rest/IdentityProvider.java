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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static eu.eubrazilcc.lvl.core.util.NumberUtils.roundUp;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.inherit;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.mutable.MutableLong;

import eu.eubrazilcc.lvl.core.Paginable;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.http.LinkRelation;
import eu.eubrazilcc.lvl.oauth2.mail.EmailSender;
import eu.eubrazilcc.lvl.storage.oauth2.PendingUser;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.User;
import eu.eubrazilcc.lvl.storage.oauth2.Users;
import eu.eubrazilcc.lvl.storage.oauth2.dao.PendingUserDAO;
import eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper;
import eu.eubrazilcc.lvl.storage.oauth2.security.UserAnonymizer;
import eu.eubrazilcc.lvl.storage.oauth2.security.UserAnonymizer.AnonymizationLevel;

/**
 * Implements an identity provider as an OAuth 2.0 Resource Server using Apache Oltu. It receives the validating token 
 * in the header of the request. In addition, provides new user sign-up logic to the LVL portal.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server">OAuth 2.0 Authorization Server</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
@Path("/users")
public class IdentityProvider {

	/**
	 * The lifetime in seconds of the confirmation code.
	 */
	public static final long CONFIRMATION_CODE_EXPIRATION_SECONDS = 86400l; // 1 day	

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " User Resource (IdP)";
	public static final String RESOURCE_SCOPE = resourceScope(IdentityProvider.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Users getUsers(final @QueryParam("start") @DefaultValue("0") int start,
			final @QueryParam("size") @DefaultValue("10") int size, 
			final @QueryParam("plain") @DefaultValue("false") boolean plain, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, false, RESOURCE_NAME);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder()
				.queryParam("start", "{start}")
				.queryParam("size", "{size}");
		// get sequences from database
		final MutableLong count = new MutableLong(0l);
		final List<ResourceOwner> owners = ResourceOwnerDAO.INSTANCE.baseUri(uriInfo.getAbsolutePath()).list(start, size, count);
		final int total = ((Long)count.getValue()).intValue();
		// previous link
		final Paginable paginable = new Paginable();
		if (start > 0) {
			int previous = start - size;
			if (previous < 0) previous = 0;
			final URI previousUri = uriBuilder.clone().build(previous, size);
			paginable.setPrevious(Link.fromUri(previousUri).rel(LinkRelation.PREVIOUS).type(MediaType.APPLICATION_JSON).build());
			final URI firstUri = uriBuilder.clone().build(0, size);
			paginable.setFirst(Link.fromUri(firstUri).rel(LinkRelation.FIRST).type(MediaType.APPLICATION_JSON).build());
		}
		// next link
		if (start + size < total) {
			int next = start + size;
			final URI nextUri = uriBuilder.clone().build(next, size);
			paginable.setNext(Link.fromUri(nextUri).rel(LinkRelation.NEXT).type(MediaType.APPLICATION_JSON).build());
			final int pages = roundUp(total, size);
			final URI lastUri = uriBuilder.clone().build(pages, size);
			paginable.setLast(Link.fromUri(lastUri).rel(LinkRelation.LAST).type(MediaType.APPLICATION_JSON).build());
		}
		return Users.start().paginable(paginable)
				.users(from(owners).transform(UserAnonymizer.start(plain ? AnonymizationLevel.NONE : AnonymizationLevel.HARD))
						.filter(notNull()).toList()).build();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(final @PathParam("id") String id, 
			final @QueryParam("plain") @DefaultValue("false") boolean plain,
			final @QueryParam("use_email") @DefaultValue("false") boolean useEmail,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {		
		if (isBlank(id)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		OAuth2Gatekeeper.authorize(request, null, headers, inherit(RESOURCE_SCOPE, id), false, RESOURCE_NAME);
		// get from database		
		final ResourceOwner owner = (!useEmail ? ResourceOwnerDAO.INSTANCE.baseUri(uriInfo.getBaseUri()).find(id)
				: ResourceOwnerDAO.INSTANCE.baseUri(uriInfo.getBaseUri()).findByEmail(id));
		if (owner == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return UserAnonymizer.start(plain ? AnonymizationLevel.NONE : AnonymizationLevel.HARD).apply(owner);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(final User user, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {		
		if (user == null || isBlank(user.getUsername())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		OAuth2Gatekeeper.authorize(request, null, headers, inherit(RESOURCE_SCOPE, user.getUsername()), true, RESOURCE_NAME);
		// create user in the database
		ResourceOwnerDAO.INSTANCE.insert(ResourceOwner.builder().id(user.getUsername()).user(user).build());
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(user.getUsername());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUser(final @PathParam("id") String id, final User update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id) || !id.equals(update.getUsername())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		OAuth2Gatekeeper.authorize(request, null, headers, inherit(RESOURCE_SCOPE, id), true, RESOURCE_NAME);
		// get from database
		final ResourceOwner current = ResourceOwnerDAO.INSTANCE.find(id);
		if (current == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// update
		ResourceOwnerDAO.INSTANCE.update(ResourceOwner.builder().id(update.getUsername()).user(update).build());			
	}

	@DELETE
	@Path("{id}")
	public void deleteUser(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		OAuth2Gatekeeper.authorize(request, null, headers, inherit(RESOURCE_SCOPE, id), true, RESOURCE_NAME);
		// get from database
		final ResourceOwner current = ResourceOwnerDAO.INSTANCE.find(id);
		if (current == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// delete
		ResourceOwnerDAO.INSTANCE.delete(id);
	}

	/* New user sign-up with LVL portal */

	@POST
	@Path("signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signUp(final User user, final @Context UriInfo uriInfo,
			final @QueryParam("skip-validation") @DefaultValue("false") boolean skipValidation) {
		if (user == null || isBlank(user.getUsername()) || isBlank(user.getEmail()) || isBlank(user.getPassword())) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// verify that no other user exists in the database with the same username or email address
		if (ResourceOwnerDAO.INSTANCE.find(user.getUsername()) != null || ResourceOwnerDAO.INSTANCE.findByEmail(user.getEmail()) != null) {
			throw new WebApplicationException(Response.Status.FOUND);
		}
		// create pending user in the database
		final PendingUser pendingUser = PendingUser.builder()
				.confirmationCode(RandomStringUtils.random(8, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()))
				.issuedAt(System.currentTimeMillis() / 1000l)
				.expiresIn(CONFIRMATION_CODE_EXPIRATION_SECONDS)
				.id(user.getUsername())
				.user(user)
				.build();
		PendingUserDAO.INSTANCE.insert(pendingUser);
		// send confirmation code by email
		if (!skipValidation) {
			final URI baseUri = uriInfo.getBaseUriBuilder().clone().build();
			sendConfirmation(baseUri, pendingUser);
		}
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pendingUser.getUser().getUsername());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("validate/{email}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void validateUser(final @PathParam("email") String email, final String code) {
		if (isBlank(email) || isBlank(code)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final PendingUser pendingUser = PendingUserDAO.INSTANCE.findByEmail(email);		
		if (pendingUser == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		if (!PendingUserDAO.INSTANCE.isValid(pendingUser.getPendingUserId(), pendingUser.getUser().getUsername(), code, false)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// update
		ResourceOwnerDAO.INSTANCE.insert(ResourceOwner.builder()
				.id(pendingUser.getUser().getUsername())
				.user(pendingUser.getUser())
				.build());
		PendingUserDAO.INSTANCE.delete(pendingUser.getPendingUserId());		
	}

	@PUT
	@Path("resend_confirmation")
	@Consumes(MediaType.APPLICATION_JSON)
	public void resendConfirmationCode(final String email, final @Context UriInfo uriInfo) {
		if (isBlank(email)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final PendingUser pendingUser = PendingUserDAO.INSTANCE.findByEmail(email);		
		if (pendingUser == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// re-send confirmation code by email
		final URI baseUri = uriInfo.getBaseUriBuilder().clone().build();
		sendConfirmation(baseUri, pendingUser);
	}
	
	private static final void sendConfirmation(final URI baseUri, final PendingUser pendingUser) {		
		URI portalUri = null;
		try {
			portalUri = new URI(baseUri.getScheme(), baseUri.getAuthority(), null, null, null);				
		} catch (URISyntaxException e) { }
		EmailSender.INSTANCE.sendTextEmail(pendingUser.getUser().getEmail(), emailValidationSubject(), 
				emailValidationMessage(pendingUser.getUser().getUsername(), pendingUser.getUser().getEmail(), pendingUser.getConfirmationCode(), portalUri));
	}

	private static final String emailValidationSubject() {
		return "Leish VirtLab";
	}

	private static final String emailValidationMessage(final String username, final String email, final String confirmationCode, final URI portalUri) {
		return "Dear " + username + ",\n\n"
				+ "Thank you for registering at Leishmaniasis Virtual Laboratory. Please, validate your email address in " 
				+ portalUri.toString() + "/#/user/validate" + " "
				+ "using the following code:\n\n" + confirmationCode + "\n\n"
				+ "You may also validate your email address by clicking on this link or copying and pasting it in your browser:\n\n"
				+ portalUri.toString() + "/#/user/validate/" + email + "/" + confirmationCode + "\n\n"
				+ "After validating your email address, you can log in the portal directly using email and password used for account registration.\n\n"
				+ "Leish VirtLab team";

	}

}