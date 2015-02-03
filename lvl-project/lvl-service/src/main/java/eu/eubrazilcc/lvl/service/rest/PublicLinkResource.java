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

package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.servlet.ServletUtils.getPortalEndpoint;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.parsePublicLinkId;
import static eu.eubrazilcc.lvl.service.io.PublicLinkWriter.unsetPublicLink;
import static eu.eubrazilcc.lvl.service.io.PublicLinkWriter.writePublicLink;
import static eu.eubrazilcc.lvl.storage.dao.PublicLinkDAO.PUBLIC_LINK_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.OWNERID_EL_TEMPLATE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.ADMIN_ROLE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.File;
import java.net.URI;
import java.util.Date;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.mutable.MutableLong;

import eu.eubrazilcc.lvl.core.PublicLinkOLD;
import eu.eubrazilcc.lvl.service.PublicLinks;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Shared data-sources (with a public link) resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/datasets/shared")
public class PublicLinkResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Public Link Resource";

	public static final String PATH_PATTERN = "[a-zA-Z_0-9\\.-]+";
	public static final String NAME_PATTERN = "[a-zA-Z_0-9\\.-]+";

	@GET
	@Produces(APPLICATION_JSON)
	public PublicLinks getPublicLinks(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final OAuth2SecurityManager securityManager = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:shared:" + OWNERID_EL_TEMPLATE + ":*:view");
		final String ownerid = securityManager.getPrincipal();
		final PublicLinks paginable = PublicLinks.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get public links from database
		final MutableLong count = new MutableLong(0l);
		final List<PublicLinkOLD> publicLinks = PUBLIC_LINK_DAO.downloadBaseUri(getDownloadBaseUri(uriInfo))
				.list(paginable.getPageFirstEntry(), per_page, null, null, count, securityManager.hasRole(ADMIN_ROLE) ? null : ownerid);
		paginable.setElements(publicLinks);
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
	public PublicLinkOLD getPublicLink(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String[] splitted = parsePublicLinkId(id);
		if (splitted == null || splitted.length != 2) {
			throw new WebApplicationException("Invalid parameters", Response.Status.BAD_REQUEST);
		}
		final String path = splitted[0], name = splitted[1];
		final String fullpath = path + "/" + name;
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:shared:" + OWNERID_EL_TEMPLATE + ":" + fullpath + ":view")
				.getPrincipal();
		// get from database
		final PublicLinkOLD publicLink = PUBLIC_LINK_DAO.downloadBaseUri(getDownloadBaseUri(uriInfo))
				.find(fullpath, ownerid);
		if (publicLink == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return publicLink;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createPublicLink(final PublicLinkOLD publicLink, final @Context UriInfo uriInfo, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:shared:" + OWNERID_EL_TEMPLATE + ":*:create")
				.getPrincipal();
		final String key = randomAlphanumeric(16).toLowerCase();
		final File outputDir = new File(CONFIG_MANAGER.getSharedDir(), key);
		final String fullpath = writePublicLink(publicLink, outputDir);
		publicLink.setPath(key + "/" + getName(fullpath));
		publicLink.setMime(mimeType(new File(fullpath)));
		publicLink.setOwner(ownerid);
		publicLink.setCreated(new Date());
		// create entry in the database
		PUBLIC_LINK_DAO.insert(publicLink);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(publicLink.getUrlSafePath());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id}")
	@Consumes(APPLICATION_JSON)
	public void updatePublicLink(final @PathParam("id") String id, final PublicLinkOLD update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}		
		final String[] splitted = parsePublicLinkId(id);
		if (splitted == null || splitted.length != 2) {
			throw new WebApplicationException("Invalid parameters", Response.Status.BAD_REQUEST);
		}
		final String path = splitted[0], name = splitted[1];
		final String fullpath = path + "/" + name;
		if (update == null || !update.getPath().equals(fullpath)) {
			throw new WebApplicationException("Parameters do not match", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
		.requiresPermissions("datasets:shared:" + OWNERID_EL_TEMPLATE + ":" + fullpath + ":edit");
		// get from database
		final PublicLinkOLD publicLink = PUBLIC_LINK_DAO.find(fullpath);
		if (publicLink == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		PUBLIC_LINK_DAO.update(update);
	}

	@DELETE
	@Path("{id}")
	public void deletePublicLink(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}		
		final String[] splitted = parsePublicLinkId(id);
		if (splitted == null || splitted.length != 2) {
			throw new WebApplicationException("Invalid parameters", Response.Status.BAD_REQUEST);
		}
		final String path = splitted[0], name = splitted[1];
		final String fullpath = path + "/" + name;
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
		.requiresPermissions("datasets:shared:" + OWNERID_EL_TEMPLATE + ":" + fullpath + ":edit");
		// get from database
		final PublicLinkOLD publicLink = PUBLIC_LINK_DAO.find(fullpath);
		if (publicLink == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		// delete
		PUBLIC_LINK_DAO.delete(fullpath);
		unsetPublicLink(publicLink, CONFIG_MANAGER.getSharedDir());
	}

	public static URI getDownloadBaseUri(final UriInfo uriInfo) {
		final URI thisResourceEndpoint = uriInfo.getBaseUriBuilder().clone().build();
		final URI portalEndpoint = getPortalEndpoint(thisResourceEndpoint);		
		return fromUri(portalEndpoint).path(CONFIG_MANAGER.getPublicLocation()).build();
	}

}