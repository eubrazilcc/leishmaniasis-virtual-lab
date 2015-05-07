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

import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static eu.eubrazilcc.lvl.storage.ResourceIdPattern.US_ASCII_PRINTABLE_PATTERN;
import static eu.eubrazilcc.lvl.storage.support.dao.IssueDAO.ISSUE_DAO;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.apache.commons.lang.mutable.MutableLong;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.support.Issue;
import eu.eubrazilcc.lvl.core.support.IssueStatus;
import eu.eubrazilcc.lvl.service.Issues;

/**
 * {@link Issue} resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/support/issues")
public class IssueResource {

	// TODO : restrict access to users with admin role (except creation which is open)
	
	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Issues Resource";

	protected final static Logger LOGGER = getLogger(IssueResource.class);

	@GET
	@Produces(APPLICATION_JSON)
	public Issues getIssues(final @QueryParam("page") @DefaultValue("0") int page,
			final @QueryParam("per_page") @DefaultValue("100") int per_page,
			final @QueryParam("q") @DefaultValue("") String q,			
			final @QueryParam("sort") @DefaultValue("") String sort,
			final @QueryParam("order") @DefaultValue("asc") String order,
			final @Context UriInfo uriInfo, final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		final Issues paginable = Issues.start()
				.page(page)
				.perPage(per_page)
				.sort(sort)
				.order(order)
				.query(q)
				.build();
		// get issues from database
		final MutableLong count = new MutableLong(0l);
		final ImmutableMap<String, String> filter = parseQuery(q);
		final Sorting sorting = parseSorting(sort, order);
		final List<Issue> issues = ISSUE_DAO.list(paginable.getPageFirstEntry(), per_page, filter, sorting, null, count);
		paginable.setElements(issues);
		// set total count and return to the caller
		final int totalEntries = issues.size() > 0 ? ((Long)count.getValue()).intValue() : 0;
		paginable.setTotalCount(totalEntries);
		return paginable;
	}

	@GET
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Produces(APPLICATION_JSON)
	public Issue getIssue(final @PathParam("id") String id, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		// get from database
		final Issue issue = ISSUE_DAO.find(id);
		if (issue == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		return issue;
	}

	@POST
	@Consumes(APPLICATION_JSON)
	public Response createIssue(final Issue issue, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (issue == null || isBlank(trimToNull(issue.getEmail())) || isBlank(trimToNull(issue.getDescription()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		saveItem(issue, null);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(issue.getId());		
		return created(uriBuilder.build()).build();
	}

	@POST
	@Path("with-attachment")
	@Consumes(MULTIPART_FORM_DATA)
	public Response createIssue(final @FormDataParam("issue") Issue issue, final @FormDataParam("file") InputStream is, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (issue == null || isBlank(trimToNull(issue.getEmail())) || isBlank(trimToNull(issue.getDescription()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		saveItem(issue, is);
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(issue.getId());		
		return created(uriBuilder.build()).build();
	}

	private void saveItem(final Issue issue, final InputStream is) {
		// save the attachment
		if (is != null) {
			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();

			String line;
			try {

				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			System.err.println("\n\n >> HERE IS OK: " + sb.toString() + "\n");
			
			// TODO
		}
		// complete required fields
		issue.setId(randomUUID().toString());
		issue.setOpened(new Date());
		issue.setStatus(IssueStatus.NEW);		
		issue.setClosed(null);
		issue.getFollowUp().clear();
		// create issue in the database
		ISSUE_DAO.insert(issue);
	}

	@PUT
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	@Consumes(APPLICATION_JSON)
	public void updateIssue(final @PathParam("id") String id, final Issue update,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		if (isBlank(id) || update == null || update.getOpened() == null || (update.getClosed() != null && update.getOpened().after(update.getClosed()))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		// get from database
		final Issue current = ISSUE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// update
		ISSUE_DAO.update(update);			
	}

	@DELETE
	@Path("{id: " + US_ASCII_PRINTABLE_PATTERN + "}")
	public void deleteIssue(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id)) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		// get from database
		final Issue current = ISSUE_DAO.find(id);
		if (current == null) {
			throw new WebApplicationException("Element not found", NOT_FOUND);
		}
		// delete
		ISSUE_DAO.delete(id);
	}

}