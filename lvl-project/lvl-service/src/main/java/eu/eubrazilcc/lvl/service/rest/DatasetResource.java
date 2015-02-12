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

import static eu.eubrazilcc.lvl.core.Dataset.DATASET_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlDecodeUtf8;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.service.io.DatasetWriter.writeDataset;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.DatasetDAO.DATASET_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.OWNERID_EL_TEMPLATE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Dataset.DatasetMetadata;
import eu.eubrazilcc.lvl.service.Datasets;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link Dataset} resource. Provides methods for creating, editing, viewing and downloading datasets. Other resources provide additional 
 * operations, such as sharing a dataset with other users {@link DatasetShareResource}.  
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/datasets/objects")
public class DatasetResource {

	private final static Logger LOGGER = getLogger(DatasetResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Dataset Resource";

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Datasets getDatasets(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final OAuth2SecurityManager securityManager = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":*:view");
		final String ownerid = securityManager.getPrincipal();
		final Datasets paginable = Datasets.start()
				.page(page)
				.perPage(per_page)
				.build();
		// get datasets from database
		final MutableLong count = new MutableLong(0l);
		final List<Dataset> datasets = DATASET_DAO.list(ns2dbnamespace(namespace2, ownerid), paginable.getPageFirstEntry(), per_page, null, null, count);
		paginable.setElements(datasets);
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Dataset getDataset(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":view")
				.getPrincipal();
		// get from database
		final Dataset dataset = DATASET_DAO.find(ns2dbnamespace(namespace2, ownerid), filename2);		
		if (dataset == null || dataset.getOutfile() == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		return dataset;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createDataset(final @PathParam("namespace") String namespace, final Dataset dataset, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		String filename2 = null;
		if (dataset == null || isBlank(filename2 = trimToNull(dataset.getFilename()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":*:create")
				.getPrincipal();
		// write to temporary file
		writeDataset(dataset);
		if (dataset.getOutfile() == null) {
			throw new WebApplicationException("Failed to write dataset to file", INTERNAL_SERVER_ERROR);			
		}
		// update dataset, setting ownerid as dataset editor
		final String dbns = ns2dbnamespace(namespace2, ownerid);
		dataset.setNamespace(dbns);
		dataset.setFilename(filename2);		
		((DatasetMetadata)dataset.getMetadata()).setEditor(ownerid);
		// create new entry in the database		
		final WriteResult<Dataset> result = DATASET_DAO.insert(dbns, filename2, dataset.getOutfile(), 
				dataset.getMetadata());
		LOGGER.debug("New dataset created: ns=" + dbns + ", fn=" + filename2 + ", id=" + result.getId());
		final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
				.path(getClass())
				.path(urlEncodeUtf8(dbns))
				.path(dataset.getUrlSafeFilename());
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateDataset(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, final Dataset update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		if (update == null) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":edit")
				.getPrincipal();
		// check the record exists in the database
		if (!DATASET_DAO.fileExists(ns2dbnamespace(namespace2, ownerid), filename2)) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// write to temporary file
		writeDataset(update);
		if (update.getOutfile() == null) {
			throw new WebApplicationException("Failed to write dataset to file", INTERNAL_SERVER_ERROR);			
		}
		// update dataset, setting ownerid as dataset editor
		final String dbns = ns2dbnamespace(namespace2, ownerid);
		update.setNamespace(dbns);
		update.setFilename(filename2);		
		((DatasetMetadata)update.getMetadata()).setEditor(ownerid);
		// (update) insert new version in the database
		final WriteResult<Dataset> result = DATASET_DAO.insert(dbns, filename2, update.getOutfile(), update.getMetadata());
		LOGGER.debug("New version created: ns=" + dbns + ", fn=" + filename2 + ", id=" + result.getId());
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	public void deleteDataset(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {		
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":edit")
				.getPrincipal();
		// check the record exists in the database
		if (!DATASET_DAO.fileExists(ns2dbnamespace(namespace2, ownerid), filename2)) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// delete
		DATASET_DAO.delete(ns2dbnamespace(namespace2, ownerid), filename2);
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/download")
	@Produces(APPLICATION_OCTET_STREAM)
	public Response downloadDataset(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":view")
				.getPrincipal();
		// get from database
		final Dataset dataset = DATASET_DAO.find(ns2dbnamespace(namespace2, ownerid), filename2);		
		if (dataset == null || dataset.getOutfile() == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// attach file to response
		final StreamingOutput stream = new StreamingOutput() {
			@Override
			public void write(final OutputStream os) throws IOException {
				try (final FileInputStream is = new FileInputStream(dataset.getOutfile())) {
					final byte[] buffer = new byte[1024];
					int noOfBytes = 0;
					while ((noOfBytes = is.read(buffer)) != -1) {
						os.write(buffer, 0, noOfBytes);
					}
				} catch (Exception e) {
					throw new WebApplicationException("Failed to write file to output", INTERNAL_SERVER_ERROR);
				}
			}
		};
		return Response.ok(stream, isNotBlank(dataset.getContentType()) ? dataset.getContentType(): APPLICATION_OCTET_STREAM)
				.header("content-disposition", "attachment; filename = " + dataset.getFilename())
				.build();
	}

	public static final String parseParam(final String param) {
		String param2 = null;
		if (isBlank(param) || isBlank(param2 = trimToNull(urlDecodeUtf8(param)))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		return param2;
	}

	public static final String ns2permission(final String namespace) {
		return DATASET_DEFAULT_NS.equals(namespace) ? OWNERID_EL_TEMPLATE : namespace;
	}

	public static final String ns2dbnamespace(final String namespace, final String ownerid) {
		return DATASET_DEFAULT_NS.equals(namespace) ? ownerid : namespace;
	}

}