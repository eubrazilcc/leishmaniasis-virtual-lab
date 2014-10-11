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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import eu.eubrazilcc.lvl.core.workflow.WorkflowDataObject;
import eu.eubrazilcc.lvl.service.WorkflowDataObjects;

/**
 * Workflows data resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pipelines_data")
public class WorkflowDataResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Pipeline Data Resource";
	public static final String RESOURCE_SCOPE = resourceScope(PublicLinkResource.class);
	
	@GET
	@Produces(APPLICATION_JSON)
	public WorkflowDataObjects getWorkflowDataObjects(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		final WorkflowDataObjects paginable = WorkflowDataObjects.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get workflow data objects from e-SC		
		final List<WorkflowDataObject> dataObjects = ESCENTRAL_CONN.listFiles();
		// TODO : implement pagination
		paginable.setElements(dataObjects);
		// set total count and return to the caller
		final int totalEntries = dataObjects.size();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
	public WorkflowDataObject getWorkflowDataObject(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}			
		// get workflow data objects from e-SC		
		final List<WorkflowDataObject> dataObjects = ESCENTRAL_CONN.listFiles();
		WorkflowDataObject dataObject = null;
		for (int i = 0; i < dataObjects.size() && dataObject == null; i++) {
			if (dataObjects.get(i).getId().equals(id)) {
				dataObject = dataObjects.get(i);
			}
		}
		if (dataObject == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return dataObject;
	}
	
}