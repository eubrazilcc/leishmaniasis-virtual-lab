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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.SavedSearchDAO.SAVED_SEARCH_DAO;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
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
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.FormattedQueryParam;
import eu.eubrazilcc.lvl.core.PaginableWithNamespace;
import eu.eubrazilcc.lvl.core.SavedSearch;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Saved searches resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/saved/searches")
public final class SavedSearchResource {

	public static final String RESOURCE_NAME = LVL_NAME + " Saved Searches Resource";

	protected final static Logger LOGGER = getLogger(SavedSearchResource.class);

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public SavedSearches getSavedSearches(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("saved:searches:" + ns2permission(namespace2) + ":*:view")
				.getPrincipal();
		final SavedSearches paginable = SavedSearches.start()
				.namespace(namespace2)
				.page(page)
				.perPage(per_page)
				.build();
		// get public links from database
		final MutableLong count = new MutableLong(0l);
		final List<SavedSearch> publicLinks = SAVED_SEARCH_DAO.list(paginable.getPageFirstEntry(), per_page, null, null, null, count, ownerid);
		paginable.setElements(publicLinks);
		// set total count and return to the caller
		final int totalEntries = publicLinks.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Produces(APPLICATION_JSON)
	public SavedSearch getSavedSearch(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("saved:searches:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();
		// get from database
		final SavedSearch search = SAVED_SEARCH_DAO.find(id2, ownerid);
		if (search == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return search;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createSavedSearch(final @PathParam("namespace") String namespace, final SavedSearch search, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		if (search == null || isBlank(trimToNull(search.getType())) || search.getSearch() == null || search.getSearch().isEmpty()) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}		
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("saved:searches:" + ns2permission(namespace2) + ":*:create")
				.getPrincipal();		
		// complete required fields
		search.setId(randomUUID().toString());
		search.setNamespace(ownerid);
		search.setSaved(new Date());		
		// create entry in the database
		SAVED_SEARCH_DAO.insert(search);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(search.getId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Consumes(APPLICATION_JSON)
	public void updateSavedSearch(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final SavedSearch update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("saved:searches:" + ns2permission(namespace2) + ":" + id2 + ":edit");		
		// get from database
		final SavedSearch search = SAVED_SEARCH_DAO.find(id);
		if (search == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		SAVED_SEARCH_DAO.update(update);
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	public void deleteSavedSearch(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
		.requiresPermissions("saved:searches:" + ns2permission(namespace2) + ":" + id2 + ":edit")
		.getPrincipal();
		// get from database
		final SavedSearch search = SAVED_SEARCH_DAO.find(id2);
		if (search == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		// delete
		SAVED_SEARCH_DAO.delete(id2);	
	}

	/**
	 * Wraps a collection of {@link SavedSearch}.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class SavedSearches extends PaginableWithNamespace<SavedSearch> {		

		@InjectLinks({
			@InjectLink(resource=SavedSearchResource.class, method="getSavedSearches", bindings={
				@Binding(name="page", value="${instance.page - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=SavedSearchResource.class, method="getSavedSearches", bindings={
				@Binding(name="page", value="${0}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=SavedSearchResource.class, method="getSavedSearches", bindings={
				@Binding(name="page", value="${instance.page + 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=SavedSearchResource.class, method="getSavedSearches", bindings={
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

		public static SavedSearchesBuilder start() {
			return new SavedSearchesBuilder();
		}

		public static class SavedSearchesBuilder {

			private final SavedSearches instance = new SavedSearches();

			public SavedSearchesBuilder namespace(final String namespace) {
				instance.setNamespace(trimToEmpty(namespace));
				return this;
			}

			public SavedSearchesBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public SavedSearchesBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public SavedSearchesBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public SavedSearchesBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public SavedSearchesBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public SavedSearchesBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public SavedSearchesBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public SavedSearchesBuilder searches(final List<SavedSearch> searches) {
				instance.setElements(searches);
				return this;			
			}

			public SavedSearches build() {
				return instance;
			}

		}

	}

}