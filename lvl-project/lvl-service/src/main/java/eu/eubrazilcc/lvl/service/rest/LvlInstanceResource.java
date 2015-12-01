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

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer.toFeatureCollection;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.cache.StatisticsCache.leishmaniaStats;
import static eu.eubrazilcc.lvl.service.cache.StatisticsCache.sandflyStats;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.US_ASCII_PRINTABLE_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.LvlInstanceDAO.INSTANCE_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;

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
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.LvlInstance;
import eu.eubrazilcc.lvl.core.SimpleStat;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.LvlInstances;

/**
 * {@link LvlInstance} resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/instances")
public class LvlInstanceResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Instances Resource";

	protected final static Logger LOGGER = getLogger(LvlInstanceResource.class);

	@GET
	@Produces(APPLICATION_JSON)
	public LvlInstances getLvlInstances(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final LvlInstances paginable = LvlInstances.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.build();
		// get instances from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<LvlInstance> instances = INSTANCE_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count);
		paginable.setElements(instances);
		// set total count and return to the caller
		final int totalEntries = instances.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public LvlInstance getLvlInstance(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final LvlInstance instance = INSTANCE_DAO.find(id);
		if (instance == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return instance;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createLvlInstance(final LvlInstance instance, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		String instanceId = null;
		if (instance == null || isBlank(instanceId = trimToNull(instance.getInstanceId()))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// create instance in the database
		INSTANCE_DAO.insert(instance);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(instanceId);		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateLvlInstance(final @PathParam("id") String id, final LvlInstance update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final LvlInstance current = INSTANCE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		INSTANCE_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	public void deleteLvlInstance(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final LvlInstance current = INSTANCE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		INSTANCE_DAO.delete(id);
	}

	@GET
	@Path("nearby/{longitude}/{latitude}")
	@Produces(APPLICATION_JSON)
	public FeatureCollection findNearbyLvlInstances(final @PathParam("longitude") double longitude, 
			final @PathParam("latitude") double latitude, 
			final @QueryParam("maxDistance") @DefaultValue("1000.0d") double maxDistance, 
			final @QueryParam("group") @DefaultValue("true") boolean group,
			final @QueryParam("heatmap") @DefaultValue("false") boolean heatmap,
			final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final Crs crs = Crs.builder().wgs84().build();
		final Point location = Point.builder().coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build()).build();
		final List<LvlInstance> instances = INSTANCE_DAO.getNear(location, maxDistance);
		// transform to improve visualization
		return fromNullable(toFeatureCollection(from(instances).transform(new Function<LvlInstance, Localizable<Point>>() {
			@Override
			public Localizable<Point> apply(final LvlInstance instance) {
				return instance;
			}			
		}).filter(notNull()).toList(), crs, group, heatmap)).or(FeatureCollection.builder().build());		
	}

	@GET
	@Path("stats/collection")
	@Produces(APPLICATION_JSON)
	public Map<String, List<SimpleStat>> getCollectionStatistics() {
		final Map<String, List<SimpleStat>> stats = leishmaniaStats();
		stats.putAll(sandflyStats());
		return stats;
	}

}