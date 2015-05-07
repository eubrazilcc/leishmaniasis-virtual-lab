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

package eu.eubrazilcc.lvl.service.testable;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import eu.eubrazilcc.lvl.core.support.Issue;
import eu.eubrazilcc.lvl.core.support.IssueStatus;
import eu.eubrazilcc.lvl.service.Issues;
import eu.eubrazilcc.lvl.service.rest.IssueResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to issues collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class IssuesResourceTest extends Testable {

	public IssuesResourceTest(final TestContext testCtxt) {
		super(testCtxt);
	}

	@Override
	public void test() throws Exception {
		printTestStart(IssuesResourceTest.class.getSimpleName(), "test");

		// test create new issue
		final Path path = IssueResource.class.getAnnotation(Path.class);
		final Issue issue = Issue.builder()
				.email("username@example.com")
				.browser("Google Chrome 42")
				.system("Ubuntu 14.04")
				.description("Problem description")
				.build();
		Response response = testCtxt.target().path(path.value()).request()
				.post(entity(issue, APPLICATION_JSON));			
		assertThat("Create new issue response is not null", response, notNullValue());
		assertThat("Create new issue response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new issue response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new issue response entity is not null", payload, notNullValue());
		assertThat("Create new issue response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Create new issue response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new issue response JAX-RS object: " + response);
		System.out.println(" >> Create new issue HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create issue location is not null", location, notNullValue());
		assertThat("Create issue path is not empty", isNotBlank(location.getPath()), equalTo(true));

		String issueId = getName(location.toURL().getPath());
		assertThat("Created issue Id is not null", issueId, notNullValue());
		assertThat("Created issue Id is not empty", isNotBlank(issueId), equalTo(true));
		issue.setId(issueId);

		// test get issue by Id (Java object)
		Issue issue2 = testCtxt.target().path(path.value()).path(issue.getId())
				.request(APPLICATION_JSON)
				.get(Issue.class);
		assertThat("Get issue by Id result is not null", issue2, notNullValue());
		assertThat("Get issue by Id opened date is not null", issue2.getOpened(), notNullValue());
		issue.setOpened(issue2.getOpened());
		assertThat("Get issue by Id status is new", issue2.getStatus(), equalTo(IssueStatus.NEW));
		issue.setStatus(issue2.getStatus());
		assertThat("Get issue by Id cosed date is null", issue2.getClosed(), nullValue());
		assertThat("Get issue by Id follow-up is not empty", issue2.getFollowUp().isEmpty(), equalTo(true));		
		assertThat("Get issue by Id coincides with expected", issue2.equalsIgnoringVolatile(issue));
		// uncomment for additional output
		System.out.println(" >> Get issue by Id result: " + issue2.toString());

		// test create new issue (multipart with attachment)
		final InputStream is = new ByteArrayInputStream("This is a test".getBytes());
		try (final FormDataMultiPart multipart = new FormDataMultiPart()) {
			multipart.field("issue", issue, APPLICATION_JSON_TYPE).bodyPart(new StreamDataBodyPart("file", is));
			response = testCtxt.target()
					.path(path.value())
					.path("with-attachment")
					.request()
					.post(entity(multipart, multipart.getMediaType()));
			assertThat("Create new issue (multipart with attachment) response is not null", response, notNullValue());
			assertThat("Create new issue (multipart with attachment) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new issue (multipart with attachment) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new issue (multipart with attachment) response entity is not null", payload, notNullValue());
			assertThat("Create new issue (multipart with attachment) response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Create new issue (multipart with attachment) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new issue (multipart with attachment) response JAX-RS object: " + response);
			System.out.println(" >> Create new issue (multipart with attachment) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));
			assertThat("Create issue (multipart with attachment) location is not null", location, notNullValue());
			assertThat("Create issue (multipart with attachment) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			issueId = getName(location.toURL().getPath());
			assertThat("Created issue (multipart with attachment) Id is not null", issueId, notNullValue());
			assertThat("Created issue (multipart with attachment) Id is not empty", isNotBlank(issueId), equalTo(true));
			issue.setId(issueId);
		}

		// test get issue by Id (multipart with attachment as a Java object)
		issue2 = testCtxt.target().path(path.value()).path(issue.getId())
				.request(APPLICATION_JSON)
				.get(Issue.class);
		assertThat("Get issue (multipart with attachment) by Id result is not null", issue2, notNullValue());
		assertThat("Get issue (multipart with attachment) by Id opened date is not null", issue2.getOpened(), notNullValue());
		issue.setOpened(issue2.getOpened());
		assertThat("Get issue (multipart with attachment) by Id status is new", issue2.getStatus(), equalTo(IssueStatus.NEW));
		issue.setStatus(issue2.getStatus());
		assertThat("Get issue (multipart with attachment) by Id cosed date is null", issue2.getClosed(), nullValue());
		assertThat("Get issue (multipart with attachment) by Id follow-up is not empty", issue2.getFollowUp().isEmpty(), equalTo(true));		
		assertThat("Get issue (multipart with attachment) by Id coincides with expected", issue2.equalsIgnoringVolatile(issue));
		// uncomment for additional output
		System.out.println(" >> Get issue (multipart with attachment) by Id result: " + issue2.toString());

		// TODO
		// get the attachment
		// TODO

		// test create new issue (multipart no attachment)
		try (final FormDataMultiPart multipart = new FormDataMultiPart()) {
			multipart.field("issue", issue, APPLICATION_JSON_TYPE);
			response = testCtxt.target()
					.path(path.value())
					.path("with-attachment")
					.request()
					.post(entity(multipart, multipart.getMediaType()));
			assertThat("Create new issue (multipart no attachment) response is not null", response, notNullValue());
			assertThat("Create new issue (multipart no attachment) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new issue (multipart no attachment) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new issue (multipart no attachment) response entity is not null", payload, notNullValue());
			assertThat("Create new issue (multipart no attachment) response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Create new issue (multipart no attachment) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new issue (multipart no attachment) response JAX-RS object: " + response);
			System.out.println(" >> Create new issue (multipart no attachment) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));
			assertThat("Create issue (multipart no attachment) location is not null", location, notNullValue());
			assertThat("Create issue (multipart no attachment) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			issueId = getName(location.toURL().getPath());
			assertThat("Created issue (multipart no attachment) Id is not null", issueId, notNullValue());
			assertThat("Created issue (multipart no attachment) Id is not empty", isNotBlank(issueId), equalTo(true));
			issue.setId(issueId);
		}

		// test get issue by Id (multipart no attachment as a Java object)
		issue2 = testCtxt.target().path(path.value()).path(issue.getId())
				.request(APPLICATION_JSON)
				.get(Issue.class);
		assertThat("Get issue (multipart no attachment) by Id result is not null", issue2, notNullValue());
		assertThat("Get issue (multipart no attachment) by Id opened date is not null", issue2.getOpened(), notNullValue());
		issue.setOpened(issue2.getOpened());
		assertThat("Get issue (multipart no attachment) by Id status is new", issue2.getStatus(), equalTo(IssueStatus.NEW));
		issue.setStatus(issue2.getStatus());
		assertThat("Get issue (multipart no attachment) by Id cosed date is null", issue2.getClosed(), nullValue());
		assertThat("Get issue (multipart no attachment) by Id follow-up is not empty", issue2.getFollowUp().isEmpty(), equalTo(true));		
		assertThat("Get issue (multipart no attachment) by Id coincides with expected", issue2.equalsIgnoringVolatile(issue));
		// uncomment for additional output
		System.out.println(" >> Get issue (multipart no attachment) by Id result: " + issue2.toString());

		// test list all issues (JSON encoded)
		response = testCtxt.target()
				.path(path.value())
				.request(APPLICATION_JSON)
				.get();
		assertThat("Get issues response is not null", response, notNullValue());
		assertThat("Get issues response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get issues response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get issues response entity is not null", payload, notNullValue());
		assertThat("Get issues response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Get issues response body (JSON): " + payload);
		System.out.println(" >> Get issues response JAX-RS object: " + response);
		System.out.println(" >> Get issues HTTP headers: " + response.getStringHeaders());			

		// test list all issues (Java object)
		Issues issues = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.get(Issues.class);
		assertThat("Get issues result is not null", issues, notNullValue());
		assertThat("Get issues list is not null", issues.getElements(), notNullValue());
		assertThat("Get issues list is not empty", !issues.getElements().isEmpty());
		assertThat("Get issues items count coincide with list size", issues.getElements().size(), equalTo(issues.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get issues result: " + issues.toString());			

		// test update issue
		issue.setClosed(new Date());
		issue.getFollowUp().put(new Date().getTime(), "Something new");
		response = testCtxt.target().path(path.value()).path(issue.getId())
				.request()
				.put(entity(issue, APPLICATION_JSON));
		assertThat("Update issue response is not null", response, notNullValue());
		assertThat("Update issue response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update issue response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update issue response entity is not null", payload, notNullValue());
		assertThat("Update issue response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update issue response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update issue response JAX-RS object: " + response);
		System.out.println(" >> Update issue HTTP headers: " + response.getStringHeaders());

		// test get issue by Id after update
		issue2 = testCtxt.target().path(path.value()).path(issue.getId())
				.request(APPLICATION_JSON)
				.get(Issue.class);
		assertThat("Get issue by Id after update result is not null", issue2, notNullValue());
		assertThat("Get issue by Id after update coincides with expected", issue2.equalsIgnoringVolatile(issue));
		// uncomment for additional output
		System.out.println(" >> Get issue by Id after update result: " + issue2.toString());

		// test delete issue
		response = testCtxt.target().path(path.value()).path(issue.getId())
				.request()
				.delete();
		assertThat("Delete issue response is not null", response, notNullValue());
		assertThat("Delete issue response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete issue response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete issue response entity is not null", payload, notNullValue());
		assertThat("Delete issue response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete issue response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete issue response JAX-RS object: " + response);
		System.out.println(" >> Delete issue HTTP headers: " + response.getStringHeaders());

		printTestEnd(IssuesResourceTest.class.getSimpleName(), "test");
	}

}