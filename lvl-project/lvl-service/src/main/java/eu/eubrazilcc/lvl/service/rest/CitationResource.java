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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.formattedQuery;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.CITATION_ID_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.LEISHMANIA_DAO;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.ORIGINAL_ARTICLE_KEY;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.REFERENCE_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
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

import org.apache.commons.lang3.mutable.MutableLong;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.FormattedQueryParam;
import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.Paginable;
import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Citations resource. Original article is deliberately excluded from the 
 * {@link #getReferences(int, int, String, String, String, UriInfo, HttpServletRequest, HttpHeaders) listing method}
 * response to reduce document sizes on large fetches, which should decrease memory consumption as well as bandwidth,
 * making document retrieval faster.
 * @author Erik Torres <ertorser@upv.es>
 * @see {@link Reference} class
 */
@Path("/citations")
public final class CitationResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Citation Resource";

	protected final static Logger LOGGER = getLogger(CitationResource.class);

	@GET
	@Produces(APPLICATION_JSON)
	public References getReferences(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:public:*:view");
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
		final List<Reference> references = REFERENCE_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, 
				ImmutableMap.of(ORIGINAL_ARTICLE_KEY, false), count);
		paginable.setElements(references);
		// set additional and return to the caller
		final int totalEntries = references.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		final List<FormattedQueryParam> formattedQuery = formattedQuery(filter, Reference.class);
		paginable.setFormattedQuery(formattedQuery);
		return paginable;
	}

	@GET
	@Path("{id: " + CITATION_ID_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Reference getReference(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:public:" + id.trim() + ":view");
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
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:*:*:create");
		if (reference == null || isBlank(reference.getPubmedId())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// create reference in the database
		REFERENCE_DAO.insert(reference);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(reference.getPubmedId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: " + CITATION_ID_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateReference(final @PathParam("id") String id, final Reference update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:*:" + id.trim() + ":edit");
		// get from database
		final Reference current = REFERENCE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		REFERENCE_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + CITATION_ID_PATTERN + "}")
	public void deleteReference(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:*:" + id.trim() + ":edit");
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
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:public:*:view");
		// get from database
		final Point point = Point.builder().coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build()).build();
		final List<Localizable<Point>> localizables = fromNullable(SANDFLY_DAO.getReferenceLocations(point, maxDistance))
				.or(new ArrayList<Localizable<Point>>());
		localizables.addAll(LEISHMANIA_DAO.getReferenceLocations(point, maxDistance));
		// transform to improve visualization
		return LocalizableAnalyzer.toFeatureCollection(localizables, Crs.builder().wgs84().build(), group, heatmap);		
	}

	@GET
	@Path("{id: " + CITATION_ID_PATTERN + "}/export/pubmed")
	@Produces(APPLICATION_JSON)
	public PubmedArticle exportCitationPubmed(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:public:" + id.trim() + ":view");
		// get from database
		final Reference reference = REFERENCE_DAO.find(id);
		if (reference == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		final PubmedArticle article = reference.getArticle();		
		if (article == null) {
			throw new WebApplicationException("Unable to complete the operation", Response.Status.INTERNAL_SERVER_ERROR);
		}
		return article;
	}

	@GET
	@Path("{id: " + CITATION_ID_PATTERN + "}/export/fulltext")
	@Produces(APPLICATION_JSON)
	public PubmedArticle exportCitationFulltext(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:*:public:" + id.trim() + ":view");
		// TODO
		return null;
	}

	/**
	 * Wraps a collection of PubMed publications.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class References extends Paginable<Reference> {

		@InjectLinks({
			@InjectLink(resource=CitationResource.class, method="getReferences", bindings={
				@Binding(name="page", value="${instance.page - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=CitationResource.class, method="getReferences", bindings={
				@Binding(name="page", value="${0}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=CitationResource.class, method="getReferences", bindings={
				@Binding(name="page", value="${instance.page + 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=CitationResource.class, method="getReferences", bindings={
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

		public static ReferencesBuilder start() {
			return new ReferencesBuilder();
		}

		public static class ReferencesBuilder {

			private final References instance = new References();

			public ReferencesBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public ReferencesBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public ReferencesBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public ReferencesBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public ReferencesBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public ReferencesBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public ReferencesBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public ReferencesBuilder references(final List<Reference> references) {
				instance.setElements(references);
				return this;			
			}

			public References build() {
				return instance;
			}

		}

	}

}