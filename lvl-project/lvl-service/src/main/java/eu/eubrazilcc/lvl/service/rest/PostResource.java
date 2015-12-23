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

import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.ns2permission;
import static eu.eubrazilcc.lvl.service.rest.QueryParamHelper.parseParam;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.US_ASCII_PRINTABLE_PATTERN;
import static eu.eubrazilcc.lvl.storage.dao.PostDAO.POST_DAO;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
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

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.community.Post;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.service.Posts;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * {@link Post} resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/community/posts")
public class PostResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Posts Resource";

	protected final static Logger LOGGER = getLogger(PostResource.class);

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}") // maintains coherence with create/update operations of this API
	@Produces(APPLICATION_JSON)
	public Posts getPosts(final @PathParam("namespace") String namespace, final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("posts:*:*:*:view");
		final Posts paginable = Posts.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.hash(computeHash(q, null))
				.build();
		// get posts from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		
		// TODO
		System.err.println("\n\n >> QUERY: " + filter + ", SORT: " + sorting + "\n");
		// TODO
		
		final List<Post> posts = POST_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count);
		paginable.setElements(posts);
		// set total count and return to the caller
		final int totalEntries = posts.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);		
		return paginable;
	}

	@GET
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id: " + US_ASCII_PRINTABLE_PATTERN + "}") // maintains coherence with create/update operations of this API
	@Produces(APPLICATION_JSON)
	public Post getPost(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String id2 = parseParam(id);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("posts:*:*:*:view");
		// get from database
		final Post post = POST_DAO.find(id2);
		if (post == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		return post;
	}

	@POST
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public Response createPost(final @PathParam("namespace") String namespace, final Post post, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("posts:*:" + ns2permission(namespace2) + ":*:create");
		if (post == null || isBlank(trimToNull(post.getAuthor())) || post.getCategory() == null || post.getLevel() == null || isBlank(trimToNull(post.getBody()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		// update the required fields
		post.setId(randomUUID().toString());
		post.setCreated(new Date());
		// save to the database
		POST_DAO.insert(post);		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(post.getId());		
		return created(uriBuilder.build()).build();
	}

	@PUT
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updatePost(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final Post update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		if (update == null || isBlank(trimToNull(update.getAuthor())) || update.getCategory() == null || update.getLevel() == null 
				|| isBlank(trimToNull(update.getBody()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("posts:*:" + ns2permission(namespace2) + ":" + id2 + ":edit");
		// get from database
		final Post current = POST_DAO.find(id2);
		if (current == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// update
		POST_DAO.update(update);			
	}

	@DELETE
	@Path("{namespace: " + URL_FRAGMENT_PATTERN + "}/{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	public void deletePost(final @PathParam("namespace") String namespace, final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		final String namespace2 = parseParam(namespace), id2 = parseParam(id);
		OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("posts:*:" + ns2permission(namespace2) + ":" + id2 + ":edit");
		// get from database
		final Post current = POST_DAO.find(id2);
		if (current == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// delete
		POST_DAO.delete(id2);
	}

}