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

import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.service.DatasetShares;
import eu.eubrazilcc.lvl.service.ServiceTest.ExpectedException;
import eu.eubrazilcc.lvl.service.rest.DatasetShareResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to dataset shares in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetShareResourceTest extends Testable {

	public DatasetShareResourceTest(final TestContext testCtxt) {
		super(testCtxt, DatasetShareResourceTest.class);
	}

	@Override
	public void test() throws Exception {
		// test create a dataset share (user1 grants access to user2)
		final Path path = DatasetShareResource.class.getAnnotation(Path.class);
		final DatasetShare share = DatasetShare.builder()
				.subject(testCtxt.ownerid("user2"))
				.build();
		Response response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(share, APPLICATION_JSON));
		assertThat("Create dataset share response is not null", response, notNullValue());
		assertThat("Create dataset share response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset share response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create dataset share response entity is not null", payload, notNullValue());
		assertThat("Create dataset share response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset share response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset share response JAX-RS object: " + response);
		System.out.println(" >> Create dataset share HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));			
		assertThat("Create dataset share location is not null", location, notNullValue());
		assertThat("Create dataset share path is not empty", isNotBlank(location.getPath()), equalTo(true));

		// test listing dataset shares (from super-user account)	
		DatasetShares shares = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(DatasetShares.class);
		assertThat("Get dataset shares (root account) result is not null", shares, notNullValue());
		assertThat("Get dataset shares (root account) list is not null", shares.getElements(), notNullValue());
		assertThat("Get dataset shares (root account) list is not empty", shares.getElements().isEmpty(), equalTo(false));
		assertThat("Get dataset shares (root account) items count coincide with list size", shares.getElements().size(), 
				equalTo(shares.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get dataset shares (root account) result: " + shares.toString());

		// test list dataset shares (from user unauthorized user account)
		try {
			shares = testCtxt.target().path(path.value())
					.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
					.get(DatasetShares.class);
			fail("list dataset shares from unauthorized user account must produce 401 error");
		} catch (NotAuthorizedException e) {
			// uncomment for additional output			
			System.out.println(" >> List dataset shares (unauthorized user account) produced the expected 401 error");
		}			

		// test list dataset shares (from owner)
		shares = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(DatasetShares.class);
		assertThat("Get dataset shares (owner) result is not null", shares, notNullValue());
		assertThat("Get dataset shares (owner) list is not null", shares.getElements(), notNullValue());
		assertThat("Get dataset shares (owner) list is not empty", shares.getElements().isEmpty(), equalTo(false));
		assertThat("Get dataset shares (owner) items count coincide with list size", shares.getElements().size(), 
				equalTo(shares.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get dataset shares (owner) result: " + shares.toString());

		// test list datasets (from granted user)
		shares = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(DatasetShares.class);
		assertThat("Get dataset shares (granted user) result is not null", shares, notNullValue());
		assertThat("Get dataset shares (granted user) list is not null", shares.getElements(), notNullValue());
		assertThat("Get dataset shares (granted user) list is not empty", shares.getElements().isEmpty(), equalTo(false));
		assertThat("Get dataset shares (granted user) items count coincide with list size", shares.getElements().size(), 
				equalTo(shares.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get dataset shares (granted user) result: " + shares.toString());

		// test getting a dataset share from owner
		DatasetShare share2 = testCtxt.target().path(path.value())				
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(DatasetShare.class);
		assertThat("Get dataset share (from owner) result is not null", share2, notNullValue());					
		assertThat("Get dataset share (from owner) access type is not null", share2.getAccessType(), notNullValue());
		assertThat("Get dataset share (from owner) access type coincides with expected", share2.getAccessType(), equalTo(VIEW_SHARE));
		assertThat("Get dataset share (from owner) filename is not empty", isNotBlank(share2.getFilename()), equalTo(true));
		assertThat("Get dataset share (from owner) filename coincides with expected", share2.getFilename(), equalTo("my_ncbi_sequence.xml"));
		assertThat("Get dataset share (from owner) namespace is not empty", isNotBlank(share2.getNamespace()), equalTo(true));
		assertThat("Get dataset share (from owner) namespace coincides with expected", share2.getNamespace(), equalTo(testCtxt.ownerid("user1")));
		assertThat("Get dataset share (from owner) shared date is not null", share2.getSharedDate(), notNullValue());
		assertThat("Get dataset share (from owner) subject is not empty", isNotBlank(share2.getSubject()), equalTo(true));
		assertThat("Get dataset share (from owner) subject coincides with expected", share2.getSubject(), equalTo(testCtxt.ownerid("user2")));			
		// uncomment for additional output
		System.out.println(" >> Get dataset share (from owner) result: " + share2.toString());

		// test viewing a dataset from an account granted (not from the owner account)
		share2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(DatasetShare.class);
		assertThat("Get dataset share (from granted account) result is not null", share2, notNullValue());					
		assertThat("Get dataset share (from granted account) access type is not null", share2.getAccessType(), notNullValue());
		assertThat("Get dataset share (from granted account) access type coincides with expected", share2.getAccessType(), equalTo(VIEW_SHARE));
		assertThat("Get dataset share (from granted account) filename is not empty", isNotBlank(share2.getFilename()), equalTo(true));
		assertThat("Get dataset share (from granted account) filename coincides with expected", share2.getFilename(), equalTo("my_ncbi_sequence.xml"));
		assertThat("Get dataset share (from granted account) namespace is not empty", isNotBlank(share2.getNamespace()), equalTo(true));
		assertThat("Get dataset share (from granted account) namespace coincides with expected", share2.getNamespace(), equalTo(testCtxt.ownerid("user1")));
		assertThat("Get dataset share (from granted account) shared date is not null", share2.getSharedDate(), notNullValue());
		assertThat("Get dataset share (from granted account) subject is not empty", isNotBlank(share2.getSubject()), equalTo(true));
		assertThat("Get dataset share (from granted account) subject coincides with expected", share2.getSubject(), equalTo(testCtxt.ownerid("user2")));			
		// uncomment for additional output
		System.out.println(" >> Get dataset share (from granted account) result: " + share2.toString());

		// test viewing a dataset share (from user unauthorized user account)
		try {
			share2 = testCtxt.target().path(path.value())
					.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
					.get(DatasetShare.class);				
			fail("get dataset share from unauthorized user account must produce 401 error");
		} catch (NotAuthorizedException e) {
			// uncomment for additional output			
			System.out.println(" >> Get dataset share (unauthorized user account) produced the expected 401 error");
		}

		// test removing permissions to data share and accessing (not from the owner account)
		try {
			response = testCtxt.target().path(path.value())
					.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
					.delete();
			if (UNAUTHORIZED.getStatusCode() == response.getStatus()) {
				throw new ExpectedException("No exception was thrown, but status is 401");
			}
			fail("delete dataset share from granted user account must produce 401 error, instead got " + response.getStatus());
		} catch (NotAuthorizedException | ExpectedException e) {
			// uncomment for additional output			
			System.out.println(" >> Delete dataset share (granted user account) produced the expected 401 error");
		}

		// test removing permissions to data share and accessing (from the owner account)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete dataset share response is not null", response, notNullValue());
		assertThat("Delete dataset share response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete dataset share response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete dataset share response entity is not null", payload, notNullValue());
		assertThat("Delete dataset share response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete dataset share response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete dataset share response JAX-RS object: " + response);
		System.out.println(" >> Delete dataset share HTTP headers: " + response.getStringHeaders());
	}

}