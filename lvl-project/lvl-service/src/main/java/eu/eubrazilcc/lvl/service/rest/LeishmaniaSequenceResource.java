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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.cache.SequenceGeolocationCache.findNearbyLeishmania;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.SEQUENCE_ID_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.LEISHMANIA_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.authorize;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.SEQUENCES;
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
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.mutable.MutableLong;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.Paginable;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.storage.SequenceKey;

/**
 * Leishmania sequences resource. Since a sequence is uniquely identified by the combination of the data source and the accession (i.e. GenBank, U49845), 
 * this class uses the reserved character ':' allowed in an URI segment to delimit or dereference the sequence identifier. Short and long notation
 * of data source are accepted (GenBank or gb). This resource converts the data source to the long notation (used to store the sequence in the
 * database) before calling a method of the database. For example, the following URIs are valid and identifies the same sequence mentioned before:
 * <ul>
 * <li>https://localhost/webapp/sequences/GenBank:U49845</li>
 * <li>https://localhost/webapp/sequences/gb:U49845</li>
 * </ul>
 * Identifiers that don't follow this convention will be rejected by this server with an HTTP Error 400 (Bad request).
 * @author Erik Torres <ertorser@upv.es>
 * @see {@link Leishmania} class
 * @see <a href="https://tools.ietf.org/html/rfc3986#section-3.3">RFC3986 - Uniform Resource Identifier (URI): Generic Syntax; Section 3.3 - Path</a>
 */
@Path("/leishmania")
public class LeishmaniaSequenceResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Sequence (leishmania) Resource";
	public static final String RESOURCE_SCOPE = SEQUENCES;

	@GET
	@Produces(APPLICATION_JSON)
	public Sequences getSequences(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		final Sequences paginable = Sequences.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.build();
		// get sequences from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<Leishmania> sequences = LEISHMANIA_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, count);
		paginable.setElements(sequences);
		// set total count and return to the caller
		final int totalEntries = ((Long)count.getValue()).intValue();
		paginable.setTotalCount(totalEntries);		
		return paginable;
	}

	@GET
	@Path("{id: " + SEQUENCE_ID_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Leishmania getSequence(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// get from database
		final Leishmania sequence = LEISHMANIA_DAO.find(SequenceKey.builder().parse(id, ID_FRAGMENT_SEPARATOR, NOTATION_LONG));
		if (sequence == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return sequence;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createSequence(final Leishmania sequence, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, false, RESOURCE_NAME);
		if (sequence == null || isBlank(sequence.getAccession())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// create sequence in the database
		LEISHMANIA_DAO.insert(sequence);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(sequence.getAccession());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: " + SEQUENCE_ID_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateSequence(final @PathParam("id") String id, final Leishmania update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}		
		final SequenceKey sequenceKey = SequenceKey.builder().parse(id, ID_FRAGMENT_SEPARATOR, NOTATION_LONG);
		if (sequenceKey == null || !sequenceKey.getDataSource().equals(update.getDataSource()) 
				|| !sequenceKey.getAccession().equals(update.getAccession())) {
			throw new WebApplicationException("Parameters do not match", Response.Status.BAD_REQUEST);
		}
		// get from database
		final Leishmania current = LEISHMANIA_DAO.find(sequenceKey);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		LEISHMANIA_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + SEQUENCE_ID_PATTERN + "}")
	public void deleteSequence(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, true, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final SequenceKey sequenceKey = SequenceKey.builder().parse(id, ID_FRAGMENT_SEPARATOR, NOTATION_LONG);
		// get from database
		final Leishmania current = LEISHMANIA_DAO.find(sequenceKey);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		LEISHMANIA_DAO.delete(sequenceKey);
	}

	@GET
	@Path("nearby/{longitude}/{latitude}")
	@Produces(APPLICATION_JSON)
	public FeatureCollection findNearbySequences(final @PathParam("longitude") double longitude, 
			final @PathParam("latitude") double latitude, 
			final @QueryParam("maxDistance") @DefaultValue("1000.0d") double maxDistance, 
			final @QueryParam("group") @DefaultValue("true") boolean group,
			final @QueryParam("heatmap") @DefaultValue("false") boolean heatmap,
			final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		authorize(request, null, headers, RESOURCE_SCOPE, false, false, RESOURCE_NAME);
		return findNearbyLeishmania(Point.builder().coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build()).build(), 
				maxDistance, group, heatmap);
		/* // get from database
		final List<Sequence> sequences = LEISHMANIA_DAO.getNear(Point.builder()
				.coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build())
				.build(), maxDistance);
		// transform to improve visualization
		return SequenceAnalyzer.toFeatureCollection(sequences, Crs.builder().wgs84().build(), group, heatmap); */		
	}

	/**
	 * Wraps a collection of {@link Leishmania}.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class Sequences extends Paginable<Leishmania> {		

		@InjectLinks({
			@InjectLink(resource=LeishmaniaSequenceResource.class, method="getSequences", bindings={
				@Binding(name="page", value="${instance.page - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=LeishmaniaSequenceResource.class, method="getSequences", bindings={
				@Binding(name="page", value="${0}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=LeishmaniaSequenceResource.class, method="getSequences", bindings={
				@Binding(name="page", value="${instance.page + 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=LeishmaniaSequenceResource.class, method="getSequences", bindings={
				@Binding(name="page", value="${instance.totalPages - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=LAST, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}")
		})
		@JsonSerialize(using = LinkListSerializer.class)
		@JsonDeserialize(using = LinkListDeserializer.class)
		@JsonProperty("links")
		private List<Link> links; // HATEOAS links

		@Override
		public List<Link> getLinks() {
			return links;
		}

		@Override
		public void setLinks(final List<Link> links) {
			if (links != null) {
				this.links = newArrayList(links);
			} else {
				this.links = null;
			}
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("paginable", super.toString())
					.toString();
		}

		public static SequencesBuilder start() {
			return new SequencesBuilder();
		}

		public static class SequencesBuilder {

			private final Sequences instance = new Sequences();

			public SequencesBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public SequencesBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public SequencesBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public SequencesBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public SequencesBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public SequencesBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public SequencesBuilder sequences(final List<Leishmania> sequences) {
				instance.setElements(sequences);
				return this;			
			}

			public Sequences build() {
				return instance;
			}

		}

	}

}