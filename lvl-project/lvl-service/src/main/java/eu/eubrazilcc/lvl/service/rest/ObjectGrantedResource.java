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
import static eu.eubrazilcc.lvl.core.mail.EmailSender.EMAIL_SENDER;
import static eu.eubrazilcc.lvl.core.servlet.ServletUtils.getPortalEndpoint;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.SharedObjectDAO.SHARED_OBJECT_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
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
import org.apache.commons.validator.routines.EmailValidator;
import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.FormattedQueryParam;
import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Notification.Priority;
import eu.eubrazilcc.lvl.core.ObjectGranted;
import eu.eubrazilcc.lvl.core.PaginableWithNamespace;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link ObjectGranted} resource. No especial permissions are required to list user's own shared objects. All active users can share 
 * objects with other users. To create/modify/delete a shared object, specific permissions are required on the kind of object shared.
 * Shared objects are linked to an email address.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/shares/granted")
public class ObjectGrantedResource {

	protected final static Logger LOGGER = getLogger(ObjectGrantedResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Shared Objects (Granted) Resource";

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public ObjectsGranted getObjectsGranted(final @PathParam("namespace") String namespace, 
			final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("users:*:*:" + ns2permission(namespace2) + ":view") // check permissions to access user's profile
				.getPrincipal();
		final ObjectsGranted paginable = ObjectsGranted.start()
				.namespace(ownerid)
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
		final List<ObjectGranted> objGranteds = SHARED_OBJECT_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count, ownerid);
		paginable.setElements(objGranteds);
		// set total count and return to the caller
		final int totalEntries = objGranteds.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace:" + URL_FRAGMENT_PATTERN + "}/{id:" + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public ObjectGranted getObjectGranted(final @PathParam("namespace") String namespace, final @PathParam("id") String id, 
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("users:*:*:" + ns2permission(namespace2) + ":view") // check permissions to access user's profile
				.getPrincipal();
		// get from database
		final ObjectGranted objGranted = SHARED_OBJECT_DAO.find(id2, ownerid);
		if (objGranted == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return objGranted;
	}

	@POST
	@Path("{namespace:" + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createObjectGranted(final @PathParam("namespace") String namespace, final ObjectGranted objGranted, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		if (objGranted == null || isBlank(trimToNull(objGranted.getUser())) || isBlank(trimToNull(objGranted.getCollection())) 
				|| isBlank(trimToNull(objGranted.getItemId())) || !EmailValidator.getInstance(false).isValid(objGranted.getUser())) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions(permission(objGranted, namespace2, "create"))
				.getPrincipal();
		// complete required fields
		objGranted.setId(compactRandomUUID());		
		objGranted.setOwner(ownerid);
		objGranted.setSharedDate(new Date());
		// create entry in the database
		SHARED_OBJECT_DAO.insert(objGranted);
		// notify user
		final ResourceOwner addresse = RESOURCE_OWNER_DAO.findByEmail(objGranted.getUser());
		if (addresse != null) {
			NOTIFICATION_MANAGER.send(Notification.builder()
					.newId()
					.priority(Priority.NORMAL)
					.addressee(addresse.getOwnerId())
					.message(String.format("%s shared a new %s with you", objGranted.getOwner(), objGranted.getCollection()))
					.build());
		} else {
			final URI baseUri = uriInfo.getBaseUriBuilder().clone().build();
			EMAIL_SENDER.sendTextEmail(objGranted.getUser(), "a LeishVL user share a dataset with you", 
					String.format("%s shared a new %s with you. Join now the LeishVL at %s to gain access to your shared data.", 
							objGranted.getOwner(), objGranted.getCollection(), getPortalEndpoint(baseUri)));
		}
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(objGranted.getUrlSafeId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace:" + URL_FRAGMENT_PATTERN + "}/{id:" + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateObjectGranted(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final ObjectGranted update, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null || isBlank(trimToNull(update.getUser())) || isBlank(trimToNull(update.getCollection())) 
				|| isBlank(trimToNull(update.getItemId()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions(permission(update, namespace2, "edit"))
				.getPrincipal();
		// get from database
		final ObjectGranted objGranted = SHARED_OBJECT_DAO.find(id2, ownerid);
		if (objGranted == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		SHARED_OBJECT_DAO.update(update);
	}

	@DELETE
	@Path("{namespace:" + URL_FRAGMENT_PATTERN + "}/{id:" + URL_FRAGMENT_PATTERN + "}")
	public void deleteObjectGranted(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		// get from database
		final ObjectGranted objGranted = SHARED_OBJECT_DAO.find(id2);
		if (objGranted == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// check user permissions
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions(permission(objGranted, namespace2, "edit"));
		// delete
		SHARED_OBJECT_DAO.delete(id2);	
	}

	private String permission(final ObjectGranted objGranted, final String namespace, final String type) {
		final String collection = objGranted.getCollection().trim(), itemId = objGranted.getItemId().trim();
		String ptoken1 = null, ptoken2 = null;
		switch (collection) {
		case LEISHMANIA_PENDING_COLLECTION:
		case SANDFLY_PENDING_COLLECTION:
			ptoken1 = "sequences";
			ptoken2 = "pending";
			break;
		default:
			throw new WebApplicationException(String.format("Unsupported collection: ", collection), BAD_REQUEST);
		}
		return String.format("%s:%s:%s:%s:%s", ptoken1, ptoken2, ns2permission(namespace), itemId, type);
	}

	/**
	 * Wraps a collection of {@link ObjectGranted}.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class ObjectsGranted extends PaginableWithNamespace<ObjectGranted> {		

		@InjectLinks({
			@InjectLink(resource=ObjectGrantedResource.class, method="getObjectsGranted", bindings={
					@Binding(name="page", value="${instance.page - 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=ObjectGrantedResource.class, method="getObjectsGranted", bindings={
					@Binding(name="page", value="${0}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
			@InjectLink(resource=ObjectGrantedResource.class, method="getObjectsGranted", bindings={
					@Binding(name="page", value="${instance.page + 1}"),
					@Binding(name="per_page", value="${instance.perPage}"),
					@Binding(name="sort", value="${instance.sort}"),
					@Binding(name="order", value="${instance.order}"),
					@Binding(name="q", value="${instance.query}")
			}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
			@InjectLink(resource=ObjectGrantedResource.class, method="getObjectsGranted", bindings={
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

		public static ObjectsGrantedBuilder start() {
			return new ObjectsGrantedBuilder();
		}

		public static class ObjectsGrantedBuilder {

			private final ObjectsGranted instance = new ObjectsGranted();

			public ObjectsGrantedBuilder namespace(final String namespace) {
				instance.setNamespace(trimToEmpty(namespace));
				return this;
			}

			public ObjectsGrantedBuilder page(final int page) {
				instance.setPage(page);
				return this;
			}

			public ObjectsGrantedBuilder perPage(final int perPage) {
				instance.setPerPage(perPage);
				return this;
			}

			public ObjectsGrantedBuilder sort(final String sort) {
				instance.setSort(sort);
				return this;
			}

			public ObjectsGrantedBuilder order(final String order) {
				instance.setOrder(order);
				return this;
			}

			public ObjectsGrantedBuilder query(final String query) {
				instance.setQuery(query);
				return this;
			}

			public ObjectsGrantedBuilder formattedQuery(final List<FormattedQueryParam> formattedQuery) {
				instance.setFormattedQuery(formattedQuery);
				return this;
			}

			public ObjectsGrantedBuilder totalCount(final int totalCount) {
				instance.setTotalCount(totalCount);
				return this;
			}

			public ObjectsGrantedBuilder hash(final String hash) {
				instance.setHash(hash);
				return this;
			}

			public ObjectsGrantedBuilder objGrantederence(final List<ObjectGranted> objGranted) {
				instance.setElements(objGranted);
				return this;
			}

			public ObjectsGranted build() {
				return instance;
			}

		}

	}

}