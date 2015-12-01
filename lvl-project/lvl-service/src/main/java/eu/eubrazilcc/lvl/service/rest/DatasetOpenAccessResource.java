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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2dbnamespace;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.DatasetDAO.DATASET_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.METHOD_NOT_ALLOWED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
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

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.DatasetOpenAccess;
import eu.eubrazilcc.lvl.service.DatasetOpenAccesses;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link DatasetOpenAccess dataset open access} resource. Provides methods for creating, editing and viewing open access 
 * links that allows downloading datasets without.   
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/datasets/open_access")
public class DatasetOpenAccessResource {

	private final static Logger LOGGER = getLogger(DatasetOpenAccessResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Dataset Open Access Resource";

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public DatasetOpenAccesses getDatasetOpenAccesses(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final OAuth2SecurityManager securityManager = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":*:view");
		final String ownerid = securityManager.getPrincipal();
		final DatasetOpenAccesses paginable = DatasetOpenAccesses.start()
				.namespace(namespace2)
				.page(page)
				.perPage(per_page)
				.build();
		// get datasets from database
		final MutableLong count = new MutableLong(0l);
		final List<DatasetOpenAccess> openaccesses = from(DATASET_DAO.listOpenAccess(ns2dbnamespace(namespace2, ownerid), paginable.getPageFirstEntry(), per_page, null, null, count))
				.transform(new Function<Dataset, DatasetOpenAccess>() {
					@Override
					public DatasetOpenAccess apply(final Dataset dataset) {
						return DatasetOpenAccess.builder()
								.filename(dataset.getFilename())
								.namespace(dataset.getNamespace())
								.openAccessDate(dataset.getMetadata().getOpenAccessDate())
								.openAccessLink(dataset.getMetadata().getOpenAccessLink())
								.build();						
					}

				}).filter(notNull()).toList();
		paginable.setElements(openaccesses);
		// set total count and return to the caller		
		final int totalEntries = openaccesses.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public DatasetOpenAccess getDatasetOpenAccess(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), filename2 = parseParam(filename);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":" + filename2 + ":view")
				.getPrincipal();
		// get from database
		final Dataset dataset = DATASET_DAO.find(ns2dbnamespace(namespace2, ownerid), filename2);
		if (dataset == null || dataset.getOutfile() == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}		
		final DatasetOpenAccess openaccess = DatasetOpenAccess.builder()
				.filename(dataset.getFilename())
				.namespace(dataset.getNamespace())
				.openAccessDate(dataset.getMetadata().getOpenAccessDate())
				.openAccessLink(dataset.getMetadata().getOpenAccessLink())
				.build();
		return openaccess;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createDatasetOpenAccess(final @PathParam("namespace") String namespace, final DatasetOpenAccess openAccess, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		String filename2 = null;
		if (openAccess == null || isBlank(filename2 = trimToNull(openAccess.getFilename()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("datasets:files:" + ns2permission(namespace2) + ":*:create")
				.getPrincipal();
		// update dataset open access
		final String dbns = ns2dbnamespace(namespace2, ownerid);
		openAccess.setFilename(filename2);
		openAccess.setNamespace(namespace2);
		// create new entry in the database
		DATASET_DAO.createOpenAccessLink(dbns, filename2);
		LOGGER.debug("New open access link was created to dataset: ns=" + dbns + ", fn=" + filename2);
		final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
				.path(getClass())
				.path(urlEncodeUtf8(dbns))
				.path(urlEncodeUtf8(filename2));
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateDatasetOpenAccess(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final DatasetOpenAccess update, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		throw new WebApplicationException("Method not allowed for this resource", METHOD_NOT_ALLOWED);
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	public void deleteDatasetOpenAccess(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
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
		DATASET_DAO.removeOpenAccessLink(ns2dbnamespace(namespace2, ownerid), filename2);
	}

}