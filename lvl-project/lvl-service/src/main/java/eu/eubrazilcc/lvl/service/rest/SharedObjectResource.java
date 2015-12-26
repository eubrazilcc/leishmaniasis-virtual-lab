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
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.SharedObjectDAO.DB_PREFIX;
import static eu.eubrazilcc.lvl.storage.dao.SharedObjectDAO.SHARED_OBJECT_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.SimpleDateFormat;
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
import eu.eubrazilcc.lvl.core.PaginableWithNamespace;
import eu.eubrazilcc.lvl.core.SharedObject;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link SharedObject} resource. 
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/shares")
public class SharedObjectResource {

	/* protected final static Logger LOGGER = getLogger(SharedObjectResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Pending Citations (references) Resource";

	// 2015-12-01T13:50:08
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public SharedObjects getSharedObjects(final @PathParam("namespace") String namespace, 
			final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("citations:pending:" + ns2permission(namespace2) + ":*:view")
				.getPrincipal();
		final SharedObjects paginable = SharedObjects.start()
				.namespace(namespace2)
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.hash(computeHash(q, null))
				.build();
		// get pending citations from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);		
		final Sorting sorting = parseSorting(sort, order);
		final List<SharedObject> pendingRefs = SHARED_OBJECT_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, 
				ImmutableMap.of(DB_PREFIX + "sequence", false), count, ownerid);
		paginable.setElements(pendingRefs);
		paginable.getExcludedFields().add(DB_PREFIX + "sequence");
		// set total count and return to the caller
		final int totalEntries = pendingRefs.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Produces(APPLICATION_JSON)
	public SharedObject getSharedObject(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("citations:pending:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();
		// get from database
		final SharedObject pendingRef = SHARED_OBJECT_DAO.find(id2, ownerid);
		if (pendingRef == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return pendingRef;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createSharedObject(final @PathParam("namespace") String namespace, final SharedObject pendingRef, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		if (pendingRef == null || isBlank(trimToNull(pendingRef.getPubmedId()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("citations:pending:" + ns2permission(namespace2) + ":*:create")
				.getPrincipal();
		// complete required fields
		pendingRef.setId(compactRandomUUID());
		pendingRef.setNamespace(ownerid);
		pendingRef.setModified(new Date());		
		// create entry in the database
		SHARED_OBJECT_DAO.insert(pendingRef);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(pendingRef.getUrlSafeId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Consumes(APPLICATION_JSON)
	public void updateSharedObject(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final SharedObject update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null || isBlank(trimToNull(update.getPubmedId()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("citations:pending:" + ns2permission(namespace2) + ":" + id2 + ":edit");		
		// get from database
		final SharedObject pendingRef = SHARED_OBJECT_DAO.find(id);
		if (pendingRef == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		SHARED_OBJECT_DAO.update(update);
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	public void deleteSharedObject(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
		.requiresPermissions("citations:pending:" + ns2permission(namespace2) + ":" + id2 + ":edit")
		.getPrincipal();
		// get from database
		final SharedObject pendingRef = SHARED_OBJECT_DAO.find(id2);
		if (pendingRef == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}		
		// delete
		SHARED_OBJECT_DAO.delete(id2);	
	}

	/**
	 * Wraps a collection of {@link SharedObject}.
	 * @author Erik Torres <ertorser@upv.es>
	 *
	public static class SharedObjects extends PaginableWithNamespace<SharedObject> {		

		@InjectLinks({
			@InjectLink(resource=SharedObjectResource.class, method="getSharedObjects", bindings={
					@Binding(name="page", value="${instance.page - 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=SharedObjectResource.class, method="getSharedObjects", bindings={
					@Binding(name="page", value="${0}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=SharedObjectResource.class, method="getSharedObjects", bindings={
					@Binding(name="page", value="${instance.page + 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=SharedObjectResource.class, method="getSharedObjects", bindings={
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

		public static SharedObjectsBuilder start() {
			return new SharedObjectsBuilder();
		}

		public static class SharedObjectsBuilder {

			private final SharedObjects instance = new SharedObjects();

			public SharedObjectsBuilder namespace(final String namespace) {
				instance.setNamespace(trimToEmpty(namespace));
				return this;
			}

			public SharedObjectsBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public SharedObjectsBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public SharedObjectsBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public SharedObjectsBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public SharedObjectsBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public SharedObjectsBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public SharedObjectsBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public SharedObjectsBuilder hash(final String hash) {
				instance.setHash(hash);
				return this;
			}

			public SharedObjectsBuilder pendingReference(final List<SharedObject> pendingReference) {
				instance.setElements(pendingReference);
				return this;
			}

			public SharedObjects build() {
				return instance;
			}

		}

	} */

}