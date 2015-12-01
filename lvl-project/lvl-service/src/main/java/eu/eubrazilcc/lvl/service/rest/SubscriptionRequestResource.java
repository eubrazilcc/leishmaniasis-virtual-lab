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

import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.US_ASCII_PRINTABLE_PATTERN;
import static eu.eubrazilcc.lvl.storage.support.dao.SubscriptionRequestDAO.SUBSCRIPTION_REQ_DAO;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.support.SubscriptionRequest;
import eu.eubrazilcc.lvl.service.SubscriptionRequests;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link SubscriptionRequest} resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/support/subscriptions/requests")
public class SubscriptionRequestResource {
	
	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Subscription Requests Resource";

	protected final static Logger LOGGER = getLogger(SubscriptionRequestResource.class);

	@GET
	@Produces(APPLICATION_JSON)
	public SubscriptionRequests getSubscriptionRequests(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresRoles(ImmutableList.of("admin"));
		final SubscriptionRequests paginable = SubscriptionRequests.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.build();
		// get requests from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<SubscriptionRequest> requests = SUBSCRIPTION_REQ_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count);
		paginable.setElements(requests);
		// set total count and return to the caller
		final int totalEntries = requests.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public SubscriptionRequest getSubscriptionRequest(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresRoles(ImmutableList.of("admin"));
		// get from database
		final SubscriptionRequest subscriptionRequest = SUBSCRIPTION_REQ_DAO.find(id);
		if (request == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		return subscriptionRequest;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createSubscriptionRequest(final SubscriptionRequest subscriptionRequest, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (subscriptionRequest == null || isBlank(trimToNull(subscriptionRequest.getEmail()))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		// complete required fields
		subscriptionRequest.setId(randomUUID().toString());
		subscriptionRequest.setRequested(new Date());
		subscriptionRequest.setFulfilled(null);
		// create request in the database
		SUBSCRIPTION_REQ_DAO.insert(subscriptionRequest);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(subscriptionRequest.getId());		
		return Response.created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateSubscriptionRequest(final @PathParam("id") String id, final SubscriptionRequest update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id) || update == null || update.getRequested() == null || (update.getFulfilled() != null && update.getRequested().after(update.getFulfilled()))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresRoles(ImmutableList.of("admin"));
		// get from database
		final SubscriptionRequest current = SUBSCRIPTION_REQ_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// update
		SUBSCRIPTION_REQ_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	public void deleteSubscriptionRequest(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresRoles(ImmutableList.of("admin"));
		// get from database
		final SubscriptionRequest current = SUBSCRIPTION_REQ_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		// delete
		SUBSCRIPTION_REQ_DAO.delete(id);
	}

}