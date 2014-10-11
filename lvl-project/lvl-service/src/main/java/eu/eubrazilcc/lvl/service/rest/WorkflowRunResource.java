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
import static eu.eubrazilcc.lvl.core.util.NamingUtils.parsePublicLinkId;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static eu.eubrazilcc.lvl.storage.dao.WorkflowRunDAO.WORKFLOW_RUN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.ACCESS_TYPE;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.OWNER_ID;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.RESOURCE_WIDE_ACCESS;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

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
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.workflow.WorkflowRun;
import eu.eubrazilcc.lvl.service.WorkflowRuns;

/**
 * Workflow runs resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pipeline_runs")
public class WorkflowRunResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Pipeline Runs Resource";
	public static final String RESOURCE_SCOPE = resourceScope(WorkflowRunResource.class);

	private final static Logger LOGGER = getLogger(WorkflowRunResource.class);

	@GET
	@Produces(APPLICATION_JSON)
	public WorkflowRuns getWorkflowRuns(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final ImmutableMap<String, String> access = authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		final WorkflowRuns paginable = WorkflowRuns.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get public links from database
		final MutableLong count = new MutableLong(0l);
		final List<WorkflowRun> publicLinks = WORKFLOW_RUN_DAO.list(paginable.getPageFirstEntry(), per_page, null, null, count, getOwnerId(access));
		paginable.setElements(publicLinks);
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id}")
	@Produces(APPLICATION_JSON)
	public WorkflowRun getWorkflowRun(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final ImmutableMap<String, String> access = authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id, getOwnerId(access));
		if (run == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return run;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createWorkflowRun(final WorkflowRun run, final @Context UriInfo uriInfo, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final ImmutableMap<String, String> access = authorize(request, null, headers, RESOURCE_SCOPE, true, true, RESOURCE_NAME);
		if (isBlank(run.getWorkflowId())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}		
		// submit
		final String invocationId = ESCENTRAL_CONN.executeWorkflow(run.getWorkflowId(), run.getParameters());
		run.setId(randomUUID().toString());
		run.setInvocationId(invocationId);
		run.setSubmitter(access.get(OWNER_ID));
		run.setSubmitted(new Date());		
		// create entry in the database
		WORKFLOW_RUN_DAO.insert(run);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(run.getId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id}")
	@Consumes(APPLICATION_JSON)
	public void updateWorkflowRun(final @PathParam("id") String id, final WorkflowRun update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final ImmutableMap<String, String> access = authorize(request, null, headers, RESOURCE_SCOPE, true, true, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String[] splitted = parsePublicLinkId(id);
		if (splitted == null || splitted.length != 2) {
			throw new WebApplicationException("Invalid parameters", Response.Status.BAD_REQUEST);
		}		
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id);
		if (run == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// check permissions
		if (!isAllowed(access, run)) {
			throw new WebApplicationException("Operation not permitted", Response.Status.BAD_REQUEST);
		}
		// update
		WORKFLOW_RUN_DAO.update(update);
	}

	@DELETE
	@Path("{id}")
	public void deleteWorkflowRun(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final ImmutableMap<String, String> access = authorize(request, null, headers, RESOURCE_SCOPE, true, true, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}		
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id);
		if (run == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		// check permissions
		if (!isAllowed(access, run)) {
			throw new WebApplicationException("Operation not permitted", Response.Status.BAD_REQUEST);
		}
		// delete
		WORKFLOW_RUN_DAO.delete(id);
		// cancel the execution in the remote workflow service
		try {
			ESCENTRAL_CONN.cancelExecution(run.getInvocationId());
		} catch (Exception e) {
			LOGGER.warn("Invocation cannot be terminated in the remote workflow service", e);
		}
	}

	private static String getOwnerId(final ImmutableMap<String, String> access) {
		return !RESOURCE_WIDE_ACCESS.equals(access.get(ACCESS_TYPE)) ? access.get(OWNER_ID) : null;
	}

	private static boolean isAllowed(final ImmutableMap<String, String> access, final WorkflowRun run) {
		return RESOURCE_WIDE_ACCESS.equals(access.get(ACCESS_TYPE)) || access.get(OWNER_ID).equals(run.getSubmitter());
	}

}