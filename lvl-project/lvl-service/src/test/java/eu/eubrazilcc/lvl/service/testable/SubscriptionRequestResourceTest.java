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

package eu.eubrazilcc.lvl.service.testable;

import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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

import java.net.URI;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.support.SubscriptionRequest;
import eu.eubrazilcc.lvl.service.SubscriptionRequests;
import eu.eubrazilcc.lvl.service.rest.SubscriptionRequestResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to requests collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SubscriptionRequestResourceTest extends Testable {

	public SubscriptionRequestResourceTest(final TestContext testCtxt) {
		super(testCtxt, SubscriptionRequestResourceTest.class);
	}

	@Override
	public void test() throws Exception {
		// test create new request		
		final Path path = SubscriptionRequestResource.class.getAnnotation(Path.class);
		final SubscriptionRequest request = SubscriptionRequest.builder()
				.email("username@example.com")
				.channels(newHashSet("mailing list"))					
				.build();
		Response response = testCtxt.target().path(path.value()).request()
				.post(entity(request, APPLICATION_JSON));			
		assertThat("Create new request response is not null", response, notNullValue());
		assertThat("Create new request response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new request response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new request response entity is not null", payload, notNullValue());
		assertThat("Create new request response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create new request response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new request response JAX-RS object: " + response);
		System.out.println(" >> Create new request HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create request location is not null", location, notNullValue());
		assertThat("Create request path is not empty", isNotBlank(location.getPath()), equalTo(true));

		final String requestId = getName(location.toURL().getPath());
		assertThat("Created request Id is not null", requestId, notNullValue());
		assertThat("Created request Id is not empty", isNotBlank(requestId), equalTo(true));
		request.setId(requestId);

		// test get request by Id (Java object)
		SubscriptionRequest request2 = testCtxt.target().path(path.value()).path(request.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(SubscriptionRequest.class);
		assertThat("Get request by Id result is not null", request2, notNullValue());
		assertThat("Get request by Id requested date is not null", request2.getRequested(), notNullValue());
		request.setRequested(request2.getRequested());
		assertThat("Get request by Id fullfilled is null", request2.getFulfilled(), nullValue());		
		assertThat("Get request by Id coincides with expected", request2.equalsIgnoringVolatile(request));
		// uncomment for additional output
		System.out.println(" >> Get request by Id result: " + request2.toString());

		// test list all requests (JSON encoded)
		response = testCtxt.target()
				.path(path.value())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Get requests response is not null", response, notNullValue());
		assertThat("Get requests response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get requests response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get requests response entity is not null", payload, notNullValue());
		assertThat("Get requests response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Get requests response body (JSON): " + payload);
		System.out.println(" >> Get requests response JAX-RS object: " + response);
		System.out.println(" >> Get requests HTTP headers: " + response.getStringHeaders());			

		// test list all requests (Java object)
		SubscriptionRequests requests = testCtxt.target()
				.path(path.value())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(SubscriptionRequests.class);
		assertThat("Get requests result is not null", requests, notNullValue());
		assertThat("Get requests list is not null", requests.getElements(), notNullValue());
		assertThat("Get requests list is not empty", !requests.getElements().isEmpty());
		assertThat("Get requests items count coincide with list size", requests.getElements().size(), equalTo(requests.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get requests result: " + requests.toString());			

		// test update request
		request.setFulfilled(new Date());
		response = testCtxt.target()
				.path(path.value())
				.path(request.getId())				
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.put(entity(request, APPLICATION_JSON));
		assertThat("Update request response is not null", response, notNullValue());
		assertThat("Update request response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update request response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update request response entity is not null", payload, notNullValue());
		assertThat("Update request response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update request response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update request response JAX-RS object: " + response);
		System.out.println(" >> Update request HTTP headers: " + response.getStringHeaders());

		// test get request by Id after update
		request2 = testCtxt.target()
				.path(path.value())
				.path(request.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(SubscriptionRequest.class);
		assertThat("Get request by Id after update result is not null", request2, notNullValue());
		assertThat("Get request by Id after update coincides with expected", request2.equalsIgnoringVolatile(request));
		// uncomment for additional output
		System.out.println(" >> Get request by Id after update result: " + request2.toString());

		// test delete request
		response = testCtxt.target().path(path.value()).path(request.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.delete();
		assertThat("Delete request response is not null", response, notNullValue());
		assertThat("Delete request response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete request response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete request response entity is not null", payload, notNullValue());
		assertThat("Delete request response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete request response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete request response JAX-RS object: " + response);
		System.out.println(" >> Delete request HTTP headers: " + response.getStringHeaders());
	}

}