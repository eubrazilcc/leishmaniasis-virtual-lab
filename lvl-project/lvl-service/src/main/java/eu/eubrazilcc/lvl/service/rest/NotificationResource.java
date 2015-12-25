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

import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.NotificationDAO.NOTIFICATION_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang3.StringUtils.isBlank;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Notification.Priority;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.service.Notifications;
import eu.eubrazilcc.lvl.service.TotalCount;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link Notification} resources. Users are allowed to view and edit their own notifications, but only administrators are 
 * allowed to create new notifications. User permissions in role notation:<br />
 * <tt>notifications:*: + ownerid + :*:view,edit</tt>
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/notifications")
public class NotificationResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Notification Resource";

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Notifications getNotifications(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("notifications:*:" + ns2permission(namespace2) + ":*:view")
				.getPrincipal();
		final Notifications paginable = Notifications.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.hash(computeHash(q, null))
				.build();
		// get notifications from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<Notification> notifications = NOTIFICATION_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count, ownerid);
		paginable.setElements(notifications);
		// set total count and return to the caller
		final int totalEntries = notifications.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Produces(APPLICATION_JSON)
	public Notification getNotification(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("notifications:*:" + ns2permission(namespace2) + ":" + id2 + ":view")
				.getPrincipal();		
		// get from database
		final Notification notification = NOTIFICATION_DAO.find(id2, ownerid);
		if (notification == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return notification;
	}
	
	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/total/count")
	@Produces(APPLICATION_JSON)
	public TotalCount getTotalCount(final @PathParam("namespace") String namespace, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("notifications:*:" + ns2permission(namespace2) + ":*:view")
				.getPrincipal();		
		// get from database		
		final long totalCount = NOTIFICATION_DAO.count(ownerid);		
		return new TotalCount(totalCount);				
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createNotification(final @PathParam("namespace") String namespace, final Notification notification, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		parseParam(namespace);
		if (notification == null || (isBlank(notification.getAddressee()) && isBlank(notification.getScope())) 
				|| isBlank(notification.getMessage())) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("notifications:*:*:*:create");
		// update required fields
		notification.setId(compactRandomUUID());
		notification.setIssuedAt(new Date());
		if (notification.getPriority() == null) notification.setPriority(Priority.NORMAL);
		// create notification in the database
		NOTIFICATION_MANAGER.send(notification);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(notification.getId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	@Consumes(APPLICATION_JSON)
	public void updateNotification(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final Notification update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("notifications:*:" + ns2permission(namespace2) + ":" + id2 + ":edit")
				.getPrincipal();		
		// get from database
		final Notification current = NOTIFICATION_DAO.find(id2, ownerid);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		NOTIFICATION_DAO.update(update);			
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id}")
	public void deleteNotification(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		final String ownerid = OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME)
				.requiresPermissions("notifications:*:" + ns2permission(namespace2) + ":" + id2 + ":edit")
				.getPrincipal();		
		// get from database
		final Notification current = NOTIFICATION_DAO.find(id2, ownerid);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		NOTIFICATION_DAO.delete(id2);
	}

}