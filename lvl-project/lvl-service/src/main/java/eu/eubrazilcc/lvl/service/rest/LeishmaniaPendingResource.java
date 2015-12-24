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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.XmlHelper.yearAsXMLGregorianCalendar;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaPendingDAO.DB_PREFIX;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaPendingDAO.LEISHMANIA_PENDING_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import eu.eubrazilcc.lvl.core.LeishmaniaPending;
import eu.eubrazilcc.lvl.core.PaginableWithNamespace;
import eu.eubrazilcc.lvl.core.PendingSequence;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link LeishmaniaPending} resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/pending/leishmania")
public class LeishmaniaPendingResource {

	protected final static Logger LOGGER = getLogger(LeishmaniaPendingResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Pending Sequences (leishmania) Resource";

	// 2015-12-01T13:50:08
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public LeishmaniaPendings getLeishmaniaPendings(final @PathParam("namespace") String namespace, 
			final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("sequences:pending:" + ns2permission(namespace2) + ":*:view")
				.getPrincipal();
		final LeishmaniaPendings paginable = LeishmaniaPendings.start()
				.namespace(namespace2)
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.hash(computeHash(q, null))
				.build();
		// get pending sequences from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);		
		final Sorting sorting = parseSorting(sort, order);
		final List<LeishmaniaPending> pendingSeqs = LEISHMANIA_PENDING_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, 
				ImmutableMap.of(DB_PREFIX + "sequence", false), count, ownerid);
		paginable.setElements(pendingSeqs);
		paginable.getExcludedFields().add(DB_PREFIX + "sequence");
		// set total count and return to the caller
		final int totalEntries = pendingSeqs.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Produces(APPLICATION_JSON)
	public LeishmaniaPending getPendingSequence(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("sequences:pending:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();
		// get from database
		final LeishmaniaPending pendingSeq = LEISHMANIA_PENDING_DAO.find(id2, ownerid);
		if (pendingSeq == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return pendingSeq;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createPendingSequence(final @PathParam("namespace") String namespace, final LeishmaniaPending pendingSeq, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		if (pendingSeq == null || pendingSeq.getSample() == null 
				|| isBlank(trimToNull(pendingSeq.getSample().getInstitutionCode())) 
				|| isBlank(trimToNull(pendingSeq.getSample().getCollectionCode()))
				|| isBlank(trimToNull(pendingSeq.getSample().getCountry()))
				|| isBlank(trimToNull(pendingSeq.getSample().getScientificName()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("sequences:pending:" + ns2permission(namespace2) + ":*:create")
				.getPrincipal();
		// complete required fields
		final Calendar cal = Calendar.getInstance();
		pendingSeq.setId(compactRandomUUID());
		pendingSeq.setNamespace(ownerid);
		pendingSeq.getSample().setModified(DWC_XML_FACTORY.createSimpleLiteral().withContent(DATE_FORMAT.format(cal.getTime())));
		if (pendingSeq.getSample().getYear() == null) pendingSeq.getSample().setYear(yearAsXMLGregorianCalendar(cal.get(Calendar.YEAR)));
		pendingSeq.getSample().setCatalogNumber(pendingSeq.getId());
		pendingSeq.getSample().setRecordedBy(pendingSeq.getNamespace());
		// create entry in the database
		LEISHMANIA_PENDING_DAO.insert(pendingSeq);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pendingSeq.getUrlSafeId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Consumes(APPLICATION_JSON)
	public void updatePendingSequence(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final LeishmaniaPending update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("sequences:pending:" + ns2permission(namespace2) + ":" + id2 + ":edit");		
		// get from database
		final LeishmaniaPending pendingSeq = LEISHMANIA_PENDING_DAO.find(id);
		if (pendingSeq == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		LEISHMANIA_PENDING_DAO.update(update);
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	public void deletePendingSequence(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
		.requiresPermissions("sequences:pending:" + ns2permission(namespace2) + ":" + id2 + ":edit")
		.getPrincipal();
		// get from database
		final LeishmaniaPending pendingSeq = LEISHMANIA_PENDING_DAO.find(id2);
		if (pendingSeq == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		// delete
		LEISHMANIA_PENDING_DAO.delete(id2);	
	}

	/**
	 * Wraps a collection of {@link PendingSequence}.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class LeishmaniaPendings extends PaginableWithNamespace<LeishmaniaPending> {

		@InjectLinks({
			@InjectLink(resource=LeishmaniaPendingResource.class, method="getLeishmaniaPendings", bindings={
					@Binding(name="page", value="${instance.page - 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=LeishmaniaPendingResource.class, method="getLeishmaniaPendings", bindings={
					@Binding(name="page", value="${0}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=LeishmaniaPendingResource.class, method="getLeishmaniaPendings", bindings={
					@Binding(name="page", value="${instance.page + 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=LeishmaniaPendingResource.class, method="getLeishmaniaPendings", bindings={
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

		public static LeishmaniaPendingsBuilder start() {
			return new LeishmaniaPendingsBuilder();
		}

		public static class LeishmaniaPendingsBuilder {

			private final LeishmaniaPendings instance = new LeishmaniaPendings();

			public LeishmaniaPendingsBuilder namespace(final String namespace) {
				instance.setNamespace(trimToEmpty(namespace));
				return this;
			}

			public LeishmaniaPendingsBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public LeishmaniaPendingsBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public LeishmaniaPendingsBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public LeishmaniaPendingsBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public LeishmaniaPendingsBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public LeishmaniaPendingsBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public LeishmaniaPendingsBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public LeishmaniaPendingsBuilder hash(final String hash) {
				instance.setHash(hash);
				return this;
			}

			public LeishmaniaPendingsBuilder pendingSequence(final List<LeishmaniaPending> pendingSequence) {
				instance.setElements(pendingSequence);
				return this;
			}

			public LeishmaniaPendings build() {
				return instance;
			}

		}

	}

}