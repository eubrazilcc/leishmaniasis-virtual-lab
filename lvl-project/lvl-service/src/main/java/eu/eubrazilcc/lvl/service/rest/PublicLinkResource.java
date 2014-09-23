package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.service.io.PublicLinkWriter.unsetPublicLink;
import static eu.eubrazilcc.lvl.service.io.PublicLinkWriter.writePublicLink;
import static eu.eubrazilcc.lvl.storage.dao.PublicLinkDAO.PUBLIC_LINK_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.File;
import java.net.URI;
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

import static javax.ws.rs.core.UriBuilder.fromUri;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.mutable.MutableLong;

import eu.eubrazilcc.lvl.core.PublicLink;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.servlet.ServletUtils;
import eu.eubrazilcc.lvl.service.PublicLinks;

/**
 * Public link resources.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/public_links")
public class PublicLinkResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Shared Object Resource";
	public static final String RESOURCE_SCOPE = resourceScope(PublicLinkResource.class);

	public static final String PATH_PATTERN = "[a-zA-Z_0-9\\.-]+";
	public static final String NAME_PATTERN = "[a-zA-Z_0-9\\.-]+";

	// TODO : add owner to all operations
	
	@GET
	@Produces(APPLICATION_JSON)
	public PublicLinks getPublicLinks(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		final PublicLinks paginable = PublicLinks.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get public links from database
		final MutableLong count = new MutableLong(0l);
		final List<PublicLink> publicLinks = PUBLIC_LINK_DAO.downloadBaseUri(getDownloadBaseUri(uriInfo))
				.list(paginable.getPageFirstEntry(), per_page, null, null, count);
		paginable.setElements(publicLinks);
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{path :" + PATH_PATTERN + "}/{name: " + NAME_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public PublicLink getPublicLink(final @PathParam("path") String path, final @PathParam("name") String name,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		if (isBlank(path) || isBlank(name)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final PublicLink publicLink = PUBLIC_LINK_DAO.downloadBaseUri(getDownloadBaseUri(uriInfo)).find(path + "/" + name);
		if (publicLink == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return publicLink;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createPublicLink(final PublicLink publicLink, final @Context UriInfo uriInfo, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, true, RESOURCE_NAME);
		final String key = randomAlphanumeric(16).toLowerCase();
		final File outputDir = new File(CONFIG_MANAGER.getSharedDir(), key);
		final String fullpath = writePublicLink(publicLink, outputDir);
		publicLink.setPath(key + "/" + getName(fullpath));
		publicLink.setMime(mimeType(new File(fullpath)));
		// TODO : insert owner here!
		// create entry in the database
		PUBLIC_LINK_DAO.insert(publicLink);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(publicLink.getPath());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{path :" + PATH_PATTERN + "}/{name: " + NAME_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updatePublicLink(final @PathParam("path") String path, final @PathParam("name") String name, 
			final PublicLink update, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, true, RESOURCE_NAME);
		if (isBlank(path) || isBlank(name)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		final String fullpath = path + "/" + name;
		if (update == null || !update.getPath().equals(fullpath)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from database
		final PublicLink publicLink = PUBLIC_LINK_DAO.find(fullpath);
		if (publicLink == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// update
		PUBLIC_LINK_DAO.update(update);
	}

	@DELETE
	@Path("{path :" + PATH_PATTERN + "}/{name: " + NAME_PATTERN + "}")
	public void deletePublicLink(final @PathParam("path") String path, final @PathParam("name") String name,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, true, RESOURCE_NAME);
		if (isBlank(path) || isBlank(name)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		final String fullpath = path + "/" + name;
		// get from database
		final PublicLink publicLink = PUBLIC_LINK_DAO.find(fullpath);
		if (publicLink == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		// delete
		PUBLIC_LINK_DAO.delete(fullpath);
		unsetPublicLink(publicLink, CONFIG_MANAGER.getSharedDir());
	}
	
	public static URI getDownloadBaseUri(final UriInfo uriInfo) {
		final URI thisResourceEndpoint = uriInfo.getBaseUriBuilder().clone().build();
		final URI portalEndpoint = ServletUtils.getPortalEndpoint(thisResourceEndpoint);		
		return fromUri(portalEndpoint).path(CONFIG_MANAGER.getPublicLocation()).build();
	}

}