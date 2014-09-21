package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.checkAuthenticateAccess;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.SharedObject;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.service.SharedObjects;

/**
 * Shared object resources.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/shared_objects")
public class SharedObjectResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Shared Object Resource";
	public static final String RESOURCE_SCOPE = resourceScope(SharedObjectResource.class);
	public static final String PATH_PATTERN = "[a-zA-Z_0-9\\.-]+";
	public static final String NAME_PATTERN = "[a-zA-Z_0-9\\.-]+";

	@GET
	@Produces(APPLICATION_JSON)
	public SharedObjects getSharedObjects(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		checkAuthenticateAccess(request, null, headers, RESOURCE_NAME);

		// TODO
		return null;
	}

	@GET
	@Path("{path :" + PATH_PATTERN + "}/{name: " + NAME_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public SharedObject getSharedObject(final @PathParam("path") String path, final @PathParam("name") String name,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		checkAuthenticateAccess(request, null, headers, RESOURCE_NAME);

		// TODO
		System.err.println("\n\n HERE : " + path + "/" + name + "\n");
		// TODO

		// TODO
		return null;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createSharedObject(final SharedObject sharedObject, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		checkAuthenticateAccess(request, null, headers, RESOURCE_NAME);

		// TODO
		return null;
	}

	@PUT
	@Path("{path :" + PATH_PATTERN + "}/{name: " + NAME_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateSharedObject(final @PathParam("path") String path, final @PathParam("name") String name, 
			final SharedObject update, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		checkAuthenticateAccess(request, null, headers, RESOURCE_NAME);

		// TODO
	}

	@DELETE
	@Path("{path :" + PATH_PATTERN + "}/{name: " + NAME_PATTERN + "}")
	public void deleteSharedObject(final @PathParam("path") String path, final @PathParam("name") String name,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		checkAuthenticateAccess(request, null, headers, RESOURCE_NAME);

		// TODO
	}

}