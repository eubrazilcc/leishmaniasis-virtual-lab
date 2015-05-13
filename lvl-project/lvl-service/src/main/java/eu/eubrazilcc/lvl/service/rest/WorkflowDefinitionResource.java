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

package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.List;
import java.util.Map;

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

import eu.eubrazilcc.lvl.core.workflow.WorkflowDefinition;
import eu.eubrazilcc.lvl.core.workflow.WorkflowParameters;
import eu.eubrazilcc.lvl.service.WorkflowDefinitions;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Workflows resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pipelines/definitions")
public class WorkflowDefinitionResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Pipeline Resource";

	@GET
	@Produces(APPLICATION_JSON)
	public WorkflowDefinitions getWorkflows(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("pipelines:definitions:public:*:view");
		final WorkflowDefinitions paginable = WorkflowDefinitions.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get workflows from e-SC
		final List<WorkflowDefinition> workflows = ESCENTRAL_CONN.listWorkflows();
		// TODO : implement pagination
		paginable.setElements(workflows);
		// set total count and return to the caller
		final int totalEntries = workflows.size();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
	public WorkflowDefinition getWorkflow(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("pipelines:definitions:public:" + id.trim() + ":view");
		// get workflows from e-SC and find definition
		final List<WorkflowDefinition> workflows = ESCENTRAL_CONN.listWorkflows();
		WorkflowDefinition workflow = null;
		for (int i = 0; i < workflows.size() && workflow == null; i++) {
			if (workflows.get(i).getId().equals(id)) {
				workflow = workflows.get(i);
			}
		}
		if (workflow == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return workflow;
	}
	
	@GET
	@Path("{id}/params")
	@Produces(APPLICATION_JSON)
	public List<Map<String, String>> getWorkflowParams(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("pipelines:definitions:public:" + id.trim() + ":view");
		// get workflows from e-SC		
		final List<WorkflowDefinition> workflows = ESCENTRAL_CONN.listWorkflows();
		WorkflowParameters parameters = null;
		for (int i = 0; i < workflows.size() && parameters == null; i++) {
			if (workflows.get(i).getId().equals(id)) {
				parameters = ESCENTRAL_CONN.getParameters(workflows.get(i).getId(), null);				
			}
		}
		if (parameters == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return parameters.getParameters();
	}

}