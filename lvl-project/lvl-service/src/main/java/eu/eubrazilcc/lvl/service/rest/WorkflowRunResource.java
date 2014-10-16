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
import static eu.eubrazilcc.lvl.core.util.NamingUtils.parsePublicLinkId;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static eu.eubrazilcc.lvl.storage.dao.WorkflowRunDAO.WORKFLOW_RUN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.ACCESS_TYPE;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.OWNER_ID;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.RESOURCE_WIDE_ACCESS;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.workflow.WorkflowProduct;
import eu.eubrazilcc.lvl.core.workflow.WorkflowRun;
import eu.eubrazilcc.lvl.core.workflow.WorkflowStatus;
import eu.eubrazilcc.lvl.service.WorkflowRuns;

/**
 * Workflow runs resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pipeline_runs")
public class WorkflowRunResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Pipeline Runs Resource";
	public static final String RESOURCE_SCOPE = resourceScope(WorkflowDefinitionResource.class);

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
		// update status (when needed)
		if (run.getStatus() == null || !run.getStatus().isCompleted()) {
			final WorkflowStatus status = ESCENTRAL_CONN.getStatus(run.getInvocationId());
			// retrieve products
			if (status != null && status.isCompleted()) {
				final ImmutableList<WorkflowProduct> products = ESCENTRAL_CONN.saveProducts(run.getInvocationId(), 
						new File(CONFIG_MANAGER.getProductsDir(), run.getId()));
				run.setProducts(products);
			}
			// update the entry in the database
			run.setStatus(status);
			WORKFLOW_RUN_DAO.update(run);
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
		deleteQuietly(new File(CONFIG_MANAGER.getProductsDir(), run.getId()));		
		// cancel the execution in the remote workflow service
		try {
			ESCENTRAL_CONN.cancelExecution(run.getInvocationId());
		} catch (Exception e) {
			LOGGER.warn("Invocation cannot be terminated in the remote workflow service", e);
		}
	}

	/**
	 * It uses the solution #1 described here: 
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WindowBase64/Base64_encoding_and_decoding#The_.22Unicode_Problem.22">Base64 encoding and decoding</a>
	 */
	@GET
	@Path("text_product/{id}/{path}")
	@Produces(TEXT_PLAIN)
	public String getTextProduct(final @PathParam("id") String id, final @PathParam("path") String path,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final ImmutableMap<String, String> access = authorize(request, null, headers, RESOURCE_SCOPE, false, true, RESOURCE_NAME);
		if (isBlank(id) || isBlank(path)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id, getOwnerId(access));
		if (run == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		final File workflowRunBaseDir = new File(CONFIG_MANAGER.getProductsDir(), run.getId());
		String content = null;
		try {			
			final File file = new File(workflowRunBaseDir, decode(new String(decodeBase64(path)), UTF_8.name()));
			if (!file.canRead()) {
				throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
			}
			content = readFileToString(file);
		} catch (WebApplicationException wap) {
			throw wap;
		} catch (IOException e) {
			LOGGER.error("Failed to read product text file", e);
		}		
		return content;
	}	

	private static String getOwnerId(final ImmutableMap<String, String> access) {
		return !RESOURCE_WIDE_ACCESS.equals(access.get(ACCESS_TYPE)) ? access.get(OWNER_ID) : null;
	}

	private static boolean isAllowed(final ImmutableMap<String, String> access, final WorkflowRun run) {
		return RESOURCE_WIDE_ACCESS.equals(access.get(ACCESS_TYPE)) || access.get(OWNER_ID).equals(run.getSubmitter());
	}

}