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

import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2dbnamespace;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.DatasetDAO.DATASET_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.convertToValidResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.datasetSharePermission;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

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

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.service.DatasetShares;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link Dataset} resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/datasets/shares")
public class DatasetShareResource {

	private final static Logger LOGGER = getLogger(DatasetShareResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Dataset Shares Resource";

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public DatasetShares getDatasetShares(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename,
			final @QueryParam("page") @DefaultValue("0") int page, final @QueryParam("per_page") @DefaultValue("100") int per_page, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":view")
				.getPrincipal();
		final DatasetShares paginable = DatasetShares.start()
				.namespace(namespace2)
				.page(page)
				.perPage(per_page)
				.build();
		// get datasets from database
		final MutableLong count = new MutableLong(0l);
		final List<DatasetShare> datashares = RESOURCE_OWNER_DAO.listDatashares(ns2dbnamespace(namespace2, ownerid), filename2, 
				paginable.getPageFirstEntry(), per_page, null, null, count);
		paginable.setElements(datashares);
		// set total count and return to the caller
		final int totalEntries = datashares.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/{subject: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public DatasetShare getDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @PathParam("subject") String subject, final @Context UriInfo uriInfo, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename),
				subject2 = convertToValidResourceOwnerId(parseParam(subject), true);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":view")
				.getPrincipal();
		// get from database
		final DatasetShare share = RESOURCE_OWNER_DAO.findDatashare(ns2dbnamespace(namespace2, ownerid), filename2, subject2);
		if (share == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		return share;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final DatasetShare datasetShare, final @Context UriInfo uriInfo,  final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		String subject2 = null;
		if (datasetShare == null || isBlank(subject2 = convertToValidResourceOwnerId(parseParam(datasetShare.getSubject()), true))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":edit")
				.getPrincipal();
		// check the target dataset exists in the database
		final String dbns = ns2dbnamespace(namespace2, ownerid);
		if (!DATASET_DAO.fileExists(dbns, filename2)) {
			throw new WebApplicationException("Dataset not found", NOT_FOUND);
		}		
		// check the subject exists in the database
		if (!RESOURCE_OWNER_DAO.exist(subject2)) {
			throw new WebApplicationException("Subject not found", NOT_FOUND);
		}		
		// prepare dataset share from validated parameters
		final DatasetShare datasetShare2 = DatasetShare.builder()
				.accessType(datasetShare.getAccessType() != null ? datasetShare.getAccessType() : VIEW_SHARE)
				.filename(filename2)
				.namespace(dbns)
				.sharedNow()
				.subject(subject2)
				.build();
		// assign permissions to the subject
		RESOURCE_OWNER_DAO.addPermissions(subject2, datasetShare2);
		LOGGER.debug("New dataset share created: ns=" + dbns + ", fn=" + filename2 + ", subject=" + subject2);
		final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
				.path(getClass())
				.path(datasetShare2.getUrlSafeNamespace())
				.path(datasetShare2.getUrlSafeFilename())
				.path(datasetShare2.getUrlSafeSubject());
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/{subject: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @PathParam("subject") String subject, final DatasetShare update, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename), 
				subject2 = convertToValidResourceOwnerId(parseParam(subject), true);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":edit")
				.getPrincipal();
		LOGGER.trace("Unsupported PUT operation: ownerid=" + ownerid + ", subject=" + subject2);
		throw new WebApplicationException("Method not allowed for this resource", METHOD_NOT_ALLOWED);
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/{subject: " + URL_FRAGMENT_PATTERN + "}")
	public void deleteDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @PathParam("subject") String subject, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename), 
				subject2 = convertToValidResourceOwnerId(parseParam(subject), true);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("datasets:files:" 
				+ ns2permission(namespace2) + ":" + filename2 + ":edit");
		// check the subject exists in the database
		if (!RESOURCE_OWNER_DAO.exist(subject2)) {
			throw new WebApplicationException("Subject not found", NOT_FOUND);
		}
		// delete		
		final DatasetShare share = DatasetShare.builder()
				.accessType(EDIT_SHARE)
				.namespace(namespace2)
				.filename(filename2)
				.subject(subject2)
				.build();		
		final String rw = datasetSharePermission(share);
		share.setAccessType(VIEW_SHARE);
		final String ro = datasetSharePermission(share);
		RESOURCE_OWNER_DAO.removePermissions(subject2, rw, ro);
	}

}