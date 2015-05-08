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

import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
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
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.FormattedQueryParam;
import eu.eubrazilcc.lvl.core.SavedSearch;
import eu.eubrazilcc.lvl.service.rest.SavedSearchResource;
import eu.eubrazilcc.lvl.service.rest.SavedSearchResource.SavedSearches;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to saved searches in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SavedSearchResourceTest extends Testable {

	public SavedSearchResourceTest(final TestContext testCtxt) {
		super(testCtxt, SavedSearchResourceTest.class);
	}

	@Override
	public void test() throws Exception {
		// test create new saved search
		final Path path = SavedSearchResource.class.getAnnotation(Path.class);
		final SavedSearch search = SavedSearch.builder()
				.type("sequence ; sandflies")
				.search(newHashSet(FormattedQueryParam.builder().term("country:spain").valid(true).build(),
						FormattedQueryParam.builder().term("sequence").build()))
						.build();			
		Response response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(search, APPLICATION_JSON));			
		assertThat("Create new search response is not null", response, notNullValue());
		assertThat("Create new search response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new search response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new search response entity is not null", payload, notNullValue());
		assertThat("Create new search response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Create new search response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new search response JAX-RS object: " + response);
		System.out.println(" >> Create new search HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create search location is not null", location, notNullValue());
		assertThat("Create search path is not empty", isNotBlank(location.getPath()), equalTo(true));

		final String searchId = getName(location.toURL().getPath());
		assertThat("Created search Id is not null", searchId, notNullValue());
		assertThat("Created search Id is not empty", isNotBlank(searchId), equalTo(true));
		search.setId(searchId);
		search.setNamespace(testCtxt.ownerid("user1"));		

		// test get saved search by Id (Java object)
		SavedSearch search2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(search.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(SavedSearch.class);
		assertThat("Get saved search by Id result is not null", search2, notNullValue());
		assertThat("Get saved search by Id date is not null", search2.getSaved(), notNullValue());
		search.setSaved(search2.getSaved());
		assertThat("Get saved search by Id coincides with expected", search2.equalsIgnoringVolatile(search));
		// uncomment for additional output
		System.out.println(" >> Get saved search by Id result: " + search2.toString());

		// test list all saved searches (JSON encoded)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Get saved searches response is not null", response, notNullValue());
		assertThat("Get saved searches response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get saved searches response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get saved searches response entity is not null", payload, notNullValue());
		assertThat("Get saved searches response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Get saved searches response body (JSON): " + payload);
		System.out.println(" >> Get saved searches response JAX-RS object: " + response);
		System.out.println(" >> Get saved searches HTTP headers: " + response.getStringHeaders());			

		// test list all saved searches (Java object)
		SavedSearches searches = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(SavedSearches.class);
		assertThat("Get saved searches result is not null", searches, notNullValue());
		assertThat("Get saved searches list is not null", searches.getElements(), notNullValue());
		assertThat("Get saved searches list is not empty", !searches.getElements().isEmpty());
		assertThat("Get saved searches items count coincide with list size", searches.getElements().size(), equalTo(searches.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get saved searches result: " + searches.toString());			

		// test update saved search
		search.setDescription("This record now includes a new description of the saved item");
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(search.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(search, APPLICATION_JSON));
		assertThat("Update saved search response is not null", response, notNullValue());
		assertThat("Update saved search response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update saved search response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update saved search response entity is not null", payload, notNullValue());
		assertThat("Update saved search response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update saved search response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update saved search response JAX-RS object: " + response);
		System.out.println(" >> Update saved search HTTP headers: " + response.getStringHeaders());

		// test get saved search by Id after update
		search2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(search.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(SavedSearch.class);
		assertThat("Get saved search by Id after update result is not null", search2, notNullValue());
		assertThat("Get saved search by Id after update coincides with expected", search2.equalsIgnoringVolatile(search));
		// uncomment for additional output
		System.out.println(" >> Get saved search by Id after update result: " + search2.toString());

		// test delete saved search
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(search.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete saved search response is not null", response, notNullValue());
		assertThat("Delete saved search response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete saved search response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete saved search response entity is not null", payload, notNullValue());
		assertThat("Delete saved search response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete saved search response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete saved search response JAX-RS object: " + response);
		System.out.println(" >> Delete saved search HTTP headers: " + response.getStringHeaders());
	}

}