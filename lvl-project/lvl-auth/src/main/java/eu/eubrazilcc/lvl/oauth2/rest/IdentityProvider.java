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
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.UserAnonymizer.AnonymizationLevel.HARD;
import static eu.eubrazilcc.lvl.storage.security.UserAnonymizer.AnonymizationLevel.NONE;
import static org.apache.commons.lang.StringUtils.isBlank;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.mutable.MutableLong;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.oauth2.Users;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;
import eu.eubrazilcc.lvl.storage.security.User;
import eu.eubrazilcc.lvl.storage.security.UserAnonymizer;

/**
 * Implements an identity provider as an OAuth 2.0 Resource Server using Apache Oltu. It receives the 
 * validating token in the header of the request.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://cwiki.apache.org/confluence/display/OLTU/OAuth+2.0+Authorization+Server">OAuth 2.0 Authorization Server</a>
 * @see <a href="http://tools.ietf.org/html/rfc6749">RFC6749</a> - The OAuth 2.0 Authorization Framework
 */
@Path("/users")
public class IdentityProvider {	

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " User Resource (IdP)";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Users getUsers(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("10") int per_page, 
			final @QueryParam("plain") @DefaultValue("false") boolean plain, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (plain) {
			OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("users:*:*:*:view");
		} else {
			OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("users:active:profile:*:view");
		}		
		final Users paginable = Users.start()
				.page(page)
				.perPage(per_page)
				.plain(plain)
				.build();
		// get users from database
		final MutableLong count = new MutableLong(0l);
		final List<ResourceOwner> owners = RESOURCE_OWNER_DAO.useGravatar(true)
				.list(paginable.getPageFirstEntry(), per_page, null, null, null, count);
		paginable.setElements(from(owners).transform(UserAnonymizer.start(plain ? NONE : HARD))
				.filter(notNull()).toList());
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);
		return paginable;		
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public User getUser(final @PathParam("id") String id, 
			final @QueryParam("plain") @DefaultValue("false") boolean plain,
			final @QueryParam("use_email") @DefaultValue("false") boolean useEmail,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {		
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get effective user (this is an exception since we always want to check authorization before)
		final ResourceOwner owner = (!useEmail ? RESOURCE_OWNER_DAO.useGravatar(true).find(id)
				: RESOURCE_OWNER_DAO.useGravatar(true).findByEmail(id));
		if (owner == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// check authorization
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("users:active:profile:" + id + ":view");
		// get from database		
		return UserAnonymizer.start(plain ? NONE : HARD).apply(owner);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(final User user, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (user == null || isBlank(user.getUserid())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("users:*:*:*:create");
		// create user in the database
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder().user(user).build());
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(user.getUserid());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateUser(final @PathParam("id") String id, final User update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id) || update == null || !id.equals(toResourceOwnerId(update))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("users:active:profile:" + id + ":edit");
		// get from database
		final ResourceOwner current = RESOURCE_OWNER_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		RESOURCE_OWNER_DAO.update(ResourceOwner.builder().user(update).build());			
	}

	@DELETE
	@Path("{id}")
	public void deleteUser(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("users:*:*:" + id + ":edit");
		// get from database
		final ResourceOwner current = RESOURCE_OWNER_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		RESOURCE_OWNER_DAO.delete(id);
	}

}