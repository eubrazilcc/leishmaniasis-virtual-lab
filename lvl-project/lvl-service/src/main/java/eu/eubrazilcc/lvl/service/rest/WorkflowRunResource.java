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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.io.PhyloTreeCreator.newickToSvg;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2dbnamespace;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.DatasetDAO.DATASET_DAO;
import static eu.eubrazilcc.lvl.storage.dao.WorkflowRunDAO.WORKFLOW_RUN_DAO;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempDirectory;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_SVG_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Target;
import eu.eubrazilcc.lvl.core.workflow.WorkflowProduct;
import eu.eubrazilcc.lvl.core.workflow.WorkflowRun;
import eu.eubrazilcc.lvl.core.workflow.WorkflowStatus;
import eu.eubrazilcc.lvl.service.WorkflowRuns;
import eu.eubrazilcc.lvl.service.cache.CachedFile;
import eu.eubrazilcc.lvl.service.cache.RenderedFilePersistingCache;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Workflow runs resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pipelines/runs")
public final class WorkflowRunResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Pipeline Runs Resource";

	protected final static Logger LOGGER = getLogger(WorkflowRunResource.class);

	private final RenderedFilePersistingCache renderedFileCache = new RenderedFilePersistingCache();

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public WorkflowRuns getWorkflowRuns(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":*:view")
				.getPrincipal();
		final WorkflowRuns paginable = WorkflowRuns.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get public links from database
		final MutableLong count = new MutableLong(0l);
		final List<WorkflowRun> publicLinks = WORKFLOW_RUN_DAO.list(paginable.getPageFirstEntry(), per_page, null, null, null, count, ownerid);
		paginable.setElements(publicLinks);
		// set total count and return to the caller
		final int totalEntries = publicLinks.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Produces(APPLICATION_JSON)
	public WorkflowRun getWorkflowRun(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id2, ownerid);
		if (run == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// update status (when needed)
		if (run.getStatus() == null || !run.getStatus().isCompleted()) {
			final WorkflowStatus status = ESCENTRAL_CONN.getStatus(run.getInvocationId());
			// retrieve products
			if (status != null && status.isCompleted()) {
				File tmpDir = null;
				try {
					tmpDir = createTempDirectory(WorkflowRunResource.class.getSimpleName()).toFile();
					final File tmpDir2 = tmpDir;
					final String dbns = ns2dbnamespace(LVL_DEFAULT_NS, ownerid);
					final Metadata metadata = Metadata.builder()
							.editor(ownerid)
							.tags(newHashSet("pipeline_product"))
							.target(Target.builder()
									.type("pipeline_product")
									.build()).build();
					final ImmutableList<WorkflowProduct> products = from(ESCENTRAL_CONN.saveProducts(run.getInvocationId(), tmpDir)).transform(new Function<WorkflowProduct, WorkflowProduct>() {
						@Override
						public WorkflowProduct apply(final WorkflowProduct product) {
							final String dbfname = run.getWorkflowId() + "-" + run.getInvocationId() + "-" + getName(product.getPath());													
							final Dataset dataset = Dataset.builder()
									.namespace(dbns)
									.filename(dbfname)
									.metadata(metadata)									
									.build();
							final WriteResult<Dataset> result = DATASET_DAO.insert(dbns, dbfname, new File(tmpDir2, product.getPath()), dataset.getMetadata());
							LOGGER.debug("New dataset created: ns=" + dbns + ", fn=" + dbfname + ", id=" + result.getId());
							return WorkflowProduct.builder().path(dbfname).build();
						}
					}).filter(notNull()).toList();
					run.setProducts(products);
				} catch (IOException e) {
					LOGGER.error("Error saving workflow products", e);
				} finally {
					deleteQuietly(tmpDir);
				}				
			}
			// update the entry in the database
			run.setStatus(status);
			WORKFLOW_RUN_DAO.update(run);
		}
		return run;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createWorkflowRun(final @PathParam("namespace") String namespace, final WorkflowRun run, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		String workflowId2 = null;
		if (run == null || isBlank(workflowId2 = trimToNull(run.getWorkflowId()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}		
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":*:create")
				.getPrincipal();		
		// submit
		final String invocationId = ESCENTRAL_CONN.executeWorkflow(workflowId2, Integer.toString(run.getVersion()), run.getParameters());
		run.setId(randomUUID().toString());
		run.setInvocationId(invocationId);
		run.setSubmitter(ownerid);
		run.setSubmitted(new Date());		
		// create entry in the database
		WORKFLOW_RUN_DAO.insert(run);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(run.getId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Consumes(APPLICATION_JSON)
	public void updateWorkflowRun(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final WorkflowRun update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":" + id2 + ":edit");		
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id2);
		if (run == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// update
		WORKFLOW_RUN_DAO.update(update);
	}
	
	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}/cancel")
	@Consumes(APPLICATION_JSON)
	public void cancelWorkflowRun(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final WorkflowRun update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);		
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":" + id2 + ":edit");		
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id2);
		if (run == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// cancel
		ESCENTRAL_CONN.cancelExecution(run.getInvocationId());
	}	

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	public void deleteWorkflowRun(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":" + id2 + ":edit")
				.getPrincipal();
		// get from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id2);
		if (run == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}		
		// delete
		try {
			final String dbns = ns2dbnamespace(LVL_DEFAULT_NS, ownerid);		
			final List<WorkflowProduct> products = run.getProducts();
			if (products != null) {
				for (final WorkflowProduct product : products) {
					DATASET_DAO.delete(dbns, product.getPath());
				}
			}
		} finally {
			WORKFLOW_RUN_DAO.delete(id2);
		}	
		// cancel the execution in the remote workflow service
		try {
			ESCENTRAL_CONN.cancelExecution(run.getInvocationId());
		} catch (Exception e) {
			LOGGER.warn("Invocation cannot be terminated in the remote workflow service", e);
		}
	}

	/**
	 * It uses the solution #1 described here to encode a path in the URL: 
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WindowBase64/Base64_encoding_and_decoding#The_.22Unicode_Problem.22">Base64 encoding and decoding</a>
	 */
	@GET
	@Path("text_product/{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}/{path}")
	@Produces(TEXT_PLAIN)
	public String getTextProduct(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @PathParam("path") String path,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id), path2 = parseParam(path);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();
		// get run from database
		final WorkflowRun run = WORKFLOW_RUN_DAO.find(id2, ownerid);
		if (run == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// get dataset from database
		String content = null;
		try {
			final String dbns = ns2dbnamespace(LVL_DEFAULT_NS, ownerid);
			final Dataset dataset = DATASET_DAO.find(dbns, decode(new String(decodeBase64(path2)), UTF_8.name()));
			if (dataset == null || dataset.getOutfile() == null || !dataset.getOutfile().canRead()) {
				throw new WebApplicationException("Element not found", NOT_FOUND);				
			}
			content = readFileToString(dataset.getOutfile());			
		} catch (WebApplicationException wap) {
			throw wap;
		} catch (IOException e) {
			LOGGER.error("Failed to read product text file", e);
		}		
		return content;
	}

	/**
	 * It uses the solution #1 described here to encode a path in the URL: 
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/WindowBase64/Base64_encoding_and_decoding#The_.22Unicode_Problem.22">Base64 encoding and decoding</a>
	 */
	@GET
	@Path("svg_product/{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}/{path}")
	@Produces(APPLICATION_SVG_XML)	
	public String getSvgRenderedTree(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @PathParam("path") String path,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id), path2 = parseParam(path);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("pipelines:runs:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();
		// try to get rendered file from cache
		final String cacheKey = namespace2 + "/" + id2 + "/" + path2;
		String svg = null, svgFilename = null;
		File tmpDir = null;
		CachedFile cachedFile = renderedFileCache.getIfPresent(cacheKey);
		if (cachedFile == null) {
			// get run from database
			final WorkflowRun run = WORKFLOW_RUN_DAO.find(id2, ownerid);
			if (run == null) {
				throw new WebApplicationException("Element not found", NOT_FOUND);
			}
			// get dataset from database
			try {
				final String dbns = ns2dbnamespace(LVL_DEFAULT_NS, ownerid);
				final Dataset dataset = DATASET_DAO.find(dbns, decode(new String(decodeBase64(path2)), UTF_8.name()));
				if (dataset == null || dataset.getOutfile() == null || !dataset.getOutfile().canRead()) {
					throw new WebApplicationException("Element not found", NOT_FOUND);
				}
				tmpDir = createTempDirectory(CONFIG_MANAGER.getLocalCacheDir().toPath(), 
						WorkflowRunResource.class.getSimpleName() + "_").toFile();				
				svgFilename = newickToSvg(dataset.getOutfile(), null, tmpDir);				
				cachedFile = renderedFileCache.put(cacheKey, new File(svgFilename));				
			} catch (WebApplicationException wap) {
				throw wap;
			} catch (IOException | InterruptedException e) {
				LOGGER.error("Failed to render SVG from tree file", e);
			} finally {
				deleteQuietly(tmpDir);
			}
		}
		if (cachedFile == null || isBlank(cachedFile.getCachedFilename())) {
			throw new WebApplicationException("SVG not found in cache", INTERNAL_SERVER_ERROR);
		}
		try {
			svg = readFileToString(new File(cachedFile.getCachedFilename()));
		} catch (IOException e) {
			throw new WebApplicationException("Failed to load SVG from cache", e, INTERNAL_SERVER_ERROR);
		}
		return svg;
	}

}