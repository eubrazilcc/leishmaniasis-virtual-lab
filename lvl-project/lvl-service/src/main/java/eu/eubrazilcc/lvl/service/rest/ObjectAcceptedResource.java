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
import static eu.eubrazilcc.lvl.core.CollectionNames.LEISHMANIA_PENDING_COLLECTION;
import static eu.eubrazilcc.lvl.core.CollectionNames.SANDFLY_PENDING_COLLECTION;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaPendingDAO.LEISHMANIA_PENDING_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SandflyPendingDAO.SANDFLY_PENDING_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SharedObjectDAO.SHARED_OBJECT_DAO;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
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
import eu.eubrazilcc.lvl.core.ObjectAccepted;
import eu.eubrazilcc.lvl.core.PaginableWithNamespace;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link ObjectAccepted} resource. No especial permissions are required to list the objects shared with the logged user.
 * Shared objects are linked to an email address.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/shares/accepted")
public class ObjectAcceptedResource {

	protected final static Logger LOGGER = getLogger(ObjectAcceptedResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Shared Objects (Accepted) Resource";

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public ObjectsAccepted getObjectsAccepted(final @PathParam("namespace") String namespace, 
			final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);		
		final String userEmail = requireNonNull(OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("users:*:*:" + ns2permission(namespace2) + ":view") // check permissions to access user's profile
				.getEmail(), "User email not found");
		final ObjectsAccepted paginable = ObjectsAccepted.start()
				.namespace(userEmail)
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.hash(computeHash(q, null))
				.build();
		// get from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);		
		final Sorting sorting = parseSorting(sort, order);
		final List<ObjectAccepted> objAccepteds = SHARED_OBJECT_DAO.listAccepted(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count, userEmail);
		paginable.setElements(objAccepteds);
		// set total count and return to the caller
		final int totalEntries = objAccepteds.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace:" + URL_FRAGMENT_PATTERN + "}/{id:" + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public ObjectAccepted getObjectAccepted(final @PathParam("namespace") String namespace, final @PathParam("id") String id, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		// delegate in the private method
		return getObjectAccepted(namespace2, id2, request, headers);
	}

	@GET
	@Path("{namespace:" + URL_FRAGMENT_PATTERN + "}/{id:" + URL_FRAGMENT_PATTERN + "}/fetch")
	@Produces(APPLICATION_JSON)
	public Object getSharedObject(final @PathParam("namespace") String namespace, final @PathParam("id") String id, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		// delegate in private method to load the object's definition
		final ObjectAccepted objAccepted = getObjectAccepted(namespace2, id2, request, headers);
		// load target object from database
		Object obj = null;
		switch (objAccepted.getCollection()) {
		case LEISHMANIA_PENDING_COLLECTION:
			obj = LEISHMANIA_PENDING_DAO.find(objAccepted.getItemId());
			break;
		case SANDFLY_PENDING_COLLECTION:
			obj = SANDFLY_PENDING_DAO.find(objAccepted.getItemId());
			break;
		default:
			throw new WebApplicationException(String.format("Unsupported collection: ", objAccepted.getCollection()), BAD_REQUEST);
		}
		if (obj == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return obj;
	}

	@NotNull
	private ObjectAccepted getObjectAccepted(final String user, final String itemId, final HttpServletRequest request, final HttpHeaders headers) {
		final String userEmail = requireNonNull(OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("users:*:*:" + ns2permission(user) + ":view") // check permissions to access user's profile
				.getEmail(), "User email not found");
		// get from database
		final ObjectAccepted objAccepted = SHARED_OBJECT_DAO.findAccepted(itemId, userEmail);
		if (objAccepted == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// check user permissions
		if (!userEmail.equals(objAccepted.getUser())) {
			throw new WebApplicationException("Access is denied due to invalid credentials", Response.Status.UNAUTHORIZED);
		}
		return objAccepted;
	}

	/**
	 * Wraps a collection of {@link ObjectAccepted}.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class ObjectsAccepted extends PaginableWithNamespace<ObjectAccepted> {		

		@InjectLinks({
			@InjectLink(resource=ObjectAcceptedResource.class, method="getObjectsAccepted", bindings={
					@Binding(name="page", value="${instance.page - 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=ObjectAcceptedResource.class, method="getObjectsAccepted", bindings={
					@Binding(name="page", value="${0}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=ObjectAcceptedResource.class, method="getObjectsAccepted", bindings={
					@Binding(name="page", value="${instance.page + 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=ObjectAcceptedResource.class, method="getObjectsAccepted", bindings={
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

		public static ObjectsAcceptedBuilder start() {
			return new ObjectsAcceptedBuilder();
		}

		public static class ObjectsAcceptedBuilder {

			private final ObjectsAccepted instance = new ObjectsAccepted();

			public ObjectsAcceptedBuilder namespace(final String namespace) {
				instance.setNamespace(trimToEmpty(namespace));
				return this;
			}

			public ObjectsAcceptedBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public ObjectsAcceptedBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public ObjectsAcceptedBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public ObjectsAcceptedBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public ObjectsAcceptedBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public ObjectsAcceptedBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public ObjectsAcceptedBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public ObjectsAcceptedBuilder hash(final String hash) {
				instance.setHash(hash);
				return this;
			}

			public ObjectsAcceptedBuilder objAcceptederence(final List<ObjectAccepted> objAccepted) {
				instance.setElements(objAccepted);
				return this;
			}

			public ObjectsAccepted build() {
				return instance;
			}

		}

	}

}