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
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.URL_FRAGMENT_PATTERN;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

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
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.service.DatasetShares;

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
	public DatasetShares getDatasetShares(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {


		// TODO
		return null;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/{id: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public DatasetShare getDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @PathParam("id") String id, final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {


		// TODO
		return null;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final DatasetShare datasetShare, final @Context UriInfo uriInfo,  final @Context HttpServletRequest request, final @Context HttpHeaders headers) {


		// TODO
		return null;
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/{id: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @PathParam("id") String id, final DatasetShare update, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {


		// TODO
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{filename: " + URL_FRAGMENT_PATTERN + "}/{id: " + URL_FRAGMENT_PATTERN + "}")
	public void deleteDatasetShare(final @PathParam("namespace") String namespace, final @PathParam("filename") String filename, 
			final @PathParam("id") String id, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {


		// TODO
	}

}