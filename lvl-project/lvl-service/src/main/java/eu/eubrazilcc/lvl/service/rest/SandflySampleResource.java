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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.unescapeUrlPathSegment;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlDecodeUtf8;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.formattedQuery;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.cache.GeolocationCache.findNearbySandflySamples;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.US_ASCII_PRINTABLE_PATTERN;
import static eu.eubrazilcc.lvl.storage.SampleKey.Builder.FLEXIBLE_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.SandflySampleDAO.ORIGINAL_SAMPLE_KEY;
import static eu.eubrazilcc.lvl.storage.dao.SandflySampleDAO.SANDFLY_SAMPLE_DAO;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
import eu.eubrazilcc.lvl.core.Identifiers;
import eu.eubrazilcc.lvl.core.Paginable;
import eu.eubrazilcc.lvl.core.Sample;
import eu.eubrazilcc.lvl.core.SandflySample;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.storage.SampleKey;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Sandfly samples resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/samples/sandflies")
public class SandflySampleResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Sample (sandflies) Resource";

	protected final static Logger LOGGER = getLogger(SandflySampleResource.class);

	@GET
	@Produces(APPLICATION_JSON)
	public Samples getSamples(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:public:*:view");
		final Samples paginable = Samples.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.hash(computeHash(q, null))
				.build();
		// get samples from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<SandflySample> samples = SANDFLY_SAMPLE_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting,
				null, count);
				/* ImmutableMap.of(ORIGINAL_SAMPLE_KEY, false), count); */
		paginable.setElements(samples);
		/* paginable.getExcludedFields().add(ORIGINAL_SAMPLE_KEY); */
		// set additional output and return to the caller
		final int totalEntries = samples.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		final List<FormattedQueryParam> formattedQuery = formattedQuery(filter, Sample.class);
		paginable.setFormattedQuery(formattedQuery);
		return paginable;
	}

	@GET
	@Produces(APPLICATION_JSON)
	@Path("project/identifiers")
	public Identifiers getIdentifiers(final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:public:*:view");
		// get samples from database
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);		
		final List<SandflySample> samples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, sorting, 
				ImmutableMap.of("sandflySample.collectionId", true, "sandflySample.catalogNumber", true), null);
		// process and return to the caller
		final Set<String> ids = ofNullable(samples).orElse(Collections.<SandflySample>emptyList()).stream().map(s -> {
			return s != null ? s.getId() : null;
		}).filter(Objects::nonNull).collect(Collectors.toSet());
		return Identifiers.builder().hash(computeHash(q, null)).identifiers(ids).build();
	}

	@GET
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public SandflySample getSample(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String id2 = urlDecodeUtf8(unescapeUrlPathSegment(id));
		final SampleKey sampleKey = SampleKey.builder().parse(id2, ID_FRAGMENT_SEPARATOR, FLEXIBLE_PATTERN, NOTATION_LONG);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:public:*:view");
		// get from database
		final SandflySample sample = SANDFLY_SAMPLE_DAO.find(sampleKey);
		if (sample == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return sample;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createSample(final SandflySample sample, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:*:*:create");
		if (sample == null || isBlank(sample.getCatalogNumber())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// annotate the sample with possible missing fields
		sample.setCollectionId(COLFLEB);
		// create sample in the database
		SANDFLY_SAMPLE_DAO.insert(sample);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(sample.getUrlSafeId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateSample(final @PathParam("id") String id, final SandflySample update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String id2 = urlDecodeUtf8(unescapeUrlPathSegment(id));
		final SampleKey sampleKey = SampleKey.builder().parse(id2, ID_FRAGMENT_SEPARATOR, FLEXIBLE_PATTERN, NOTATION_LONG);
		if (sampleKey == null || !sampleKey.getCollectionId().equals(update.getCollectionId()) 
				|| !sampleKey.getCatalogNumber().equals(update.getCatalogNumber())) {
			throw new WebApplicationException("Parameters do not match", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:*:*:edit");
		// get from database
		final SandflySample current = SANDFLY_SAMPLE_DAO.find(sampleKey);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		SANDFLY_SAMPLE_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	public void deleteSample(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {		
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String id2 = urlDecodeUtf8(unescapeUrlPathSegment(id));
		final SampleKey sampleKey = SampleKey.builder().parse(id2, ID_FRAGMENT_SEPARATOR, FLEXIBLE_PATTERN, NOTATION_LONG);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:*:*:edit");
		// get from database
		final SandflySample current = SANDFLY_SAMPLE_DAO.find(sampleKey);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		SANDFLY_SAMPLE_DAO.delete(sampleKey);
	}

	@GET
	@Path("nearby/{longitude}/{latitude}")
	@Produces(APPLICATION_JSON)
	public FeatureCollection findNearbySamples(final @PathParam("longitude") double longitude, 
			final @PathParam("latitude") double latitude, 
			final @QueryParam("maxDistance") @DefaultValue("1000.0d") double maxDistance, 
			final @QueryParam("group") @DefaultValue("true") boolean group,
			final @QueryParam("heatmap") @DefaultValue("false") boolean heatmap,
			final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:public:*:view");
		return findNearbySandflySamples(Point.builder().coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build()).build(), 
				maxDistance, group, heatmap);
	}

	@GET
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}/export/dwc/xml")
	@Produces(APPLICATION_JSON)
	public SimpleDarwinRecord exportSampleXml(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String id2 = urlDecodeUtf8(unescapeUrlPathSegment(id));
		final SampleKey sampleKey = SampleKey.builder().parse(id2, ID_FRAGMENT_SEPARATOR, FLEXIBLE_PATTERN, NOTATION_LONG);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("samples:sandflies:public:*:view");
		// get from database
		final Sample sample = SANDFLY_SAMPLE_DAO.find(sampleKey);
		if (sample == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		final SimpleDarwinRecord dwcSample = sample.getSample();
		if (dwcSample == null) {
			throw new WebApplicationException("Unable to complete the operation", Response.Status.INTERNAL_SERVER_ERROR);
		}
		return dwcSample;
	}

	/**
	 * Wraps a collection of {@link SandflySample}.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class Samples extends Paginable<SandflySample> {		

		@InjectLinks({
			@InjectLink(resource=SandflySampleResource.class, method="getSamples", bindings={
					@Binding(name="page", value="${instance.page - 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=SandflySampleResource.class, method="getSamples", bindings={
					@Binding(name="page", value="${0}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=SandflySampleResource.class, method="getSamples", bindings={
					@Binding(name="page", value="${instance.page + 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=SandflySampleResource.class, method="getSamples", bindings={
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

		public static SamplesBuilder start() {
			return new SamplesBuilder();
		}

		public static class SamplesBuilder {

			private final Samples instance = new Samples();

			public SamplesBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public SamplesBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public SamplesBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public SamplesBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public SamplesBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public SamplesBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public SamplesBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public SamplesBuilder hash(final String hash) {
				instance.setHash(hash);
				return this;
			}

			public SamplesBuilder samples(final List<SandflySample> samples) {
				instance.setElements(samples);
				return this;			
			}

			public Samples build() {
				return instance;
			}

		}

	}

}