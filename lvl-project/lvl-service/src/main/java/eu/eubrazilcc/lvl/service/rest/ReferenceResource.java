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

import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.REFERENCE_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isBlank;

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

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.References;

/**
 * References resource.
 * @author Erik Torres <ertorser@upv.es>
 * @see {@link Reference} class
 */
@Path("/references")
public class ReferenceResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Reference Resource";
	public static final String RESOURCE_SCOPE = resourceScope(ReferenceResource.class);

	public static final String REF_ID_PATTERN = "[0-9]+";

	@GET
	@Produces(APPLICATION_JSON)
	public References getReferences(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		final References paginable = References.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.build();
		// get references from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<Reference> references = REFERENCE_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, count);
		paginable.setElements(references);
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id: " + REF_ID_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Reference getReference(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final Reference reference = REFERENCE_DAO.find(id);
		if (reference == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return reference;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createReference(final Reference reference, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, false, RESOURCE_NAME);
		if (reference == null || isBlank(reference.getPubmedId())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// create reference in the database
		REFERENCE_DAO.insert(reference);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(reference.getPubmedId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: " + REF_ID_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateReference(final @PathParam("id") String id, final Reference update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}		
		// get from database
		final Reference current = REFERENCE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		REFERENCE_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + REF_ID_PATTERN + "}")
	public void deleteReference(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final Reference current = REFERENCE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		REFERENCE_DAO.delete(id);
	}

	@GET
	@Path("nearby/{longitude}/{latitude}")
	@Produces(APPLICATION_JSON)
	public FeatureCollection findNearbyReferences(final @PathParam("longitude") double longitude, 
			final @PathParam("latitude") double latitude, 
			final @QueryParam("maxDistance") @DefaultValue("1000.0d") double maxDistance, 
			final @QueryParam("group") @DefaultValue("true") boolean group,
			final @QueryParam("heatmap") @DefaultValue("false") boolean heatmap,
			final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		// get from database
		final List<Localizable<Point>> localizables = SEQUENCE_DAO.getReferenceLocations(Point.builder()
				.coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build())
				.build(), maxDistance);
		// transform to improve visualization
		return LocalizableAnalyzer.toFeatureCollection(localizables, Crs.builder().wgs84().build(), group, heatmap);		
	}

}