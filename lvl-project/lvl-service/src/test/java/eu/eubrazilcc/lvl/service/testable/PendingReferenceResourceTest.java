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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.PendingReference;
import eu.eubrazilcc.lvl.service.rest.PendingReferenceResource;
import eu.eubrazilcc.lvl.service.rest.PendingReferenceResource.PendingReferences;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to pending references collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingReferenceResourceTest extends Testable {

	public PendingReferenceResourceTest(final TestContext testCtxt) {
		super(testCtxt, PendingReferenceResourceTest.class);		
	}

	@Override
	public void test() throws Exception {
		// test create new pending reference
		final Path path = PendingReferenceResource.class.getAnnotation(Path.class);
		final PendingReference pendingRef = PendingReference.builder()
				.pubmedId("ADGJ87950")
				.seqids(newHashSet("gb:ABC12345678"))
				.sampleids(newHashSet("colfleb:123", "isciii:456"))
				.build();		
		Response response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(pendingRef, APPLICATION_JSON));			
		assertThat("Create new pending reference response is not null", response, notNullValue());
		assertThat("Create new pending reference response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new pending reference response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new pending reference response entity is not null", payload, notNullValue());
		assertThat("Create new pending reference response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Create new pending reference response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create new pending reference response JAX-RS object: " + response);
		printMsg(" >> Create new pending reference HTTP headers: " + response.getStringHeaders());

		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create pending response location is not null", location, notNullValue());
		assertThat("Create pending response path is not empty", isNotBlank(location.getPath()), equalTo(true));

		final String pendingRefId = getName(location.toURL().getPath());
		assertThat("Created pending response Id is not null", pendingRefId, notNullValue());
		assertThat("Created pending response Id is not empty", isNotBlank(pendingRefId), equalTo(true));
		pendingRef.setId(pendingRefId);
		pendingRef.setNamespace(testCtxt.ownerid("user1"));		

		// test get pending reference by Id (Java object)
		PendingReference pendingRef2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingRef.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReference.class);
		assertThat("Get pending reference by Id result is not null", pendingRef2, notNullValue());
		assertThat("Get pending reference by Id date is not null", pendingRef2.getModified(), notNullValue());
		pendingRef.setModified(pendingRef2.getModified());
		assertThat("Get pending reference by Id coincides with expected", pendingRef2.equalsIgnoringVolatile(pendingRef));
		// uncomment for additional output
		printMsg(" >> Get pending reference by Id result: " + toJson(pendingRef2, JSON_PRETTY_PRINTER));

		// create a larger dataset to test complex operations
		final int numItems = 3;
		for (int i = 0; i < numItems; i++) {								
			final PendingReference pendingRef3 = PendingReference.builder()
					.pubmedId("TEFGR87950")
					.sampleids(newHashSet("isciii:789"))
					.build();
			response = testCtxt.target().path(path.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
					.post(entity(pendingRef3, APPLICATION_JSON));
			assertThat("Create new pending reference response is not null", response, notNullValue());
			assertThat("Create new pending reference response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		}

		// test list all pending references (JSON encoded)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Get pending references response is not null", response, notNullValue());
		assertThat("Get pending references response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get pending references response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get pending references response entity is not null", payload, notNullValue());
		assertThat("Get pending references response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Get pending references response body (JSON): " + payload);
		printMsg(" >> Get pending references response JAX-RS object: " + response);
		printMsg(" >> Get pending references HTTP headers: " + response.getStringHeaders());

		// test list all pending references (Java object)
		PendingReferences pendingRefs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReferences.class);
		assertThat("Get pending references result is not null", pendingRefs, notNullValue());
		assertThat("Get pending references list coincides with expected", pendingRefs.getElements(), allOf(notNullValue(), not(empty()), hasSize(pendingRefs.getTotalCount())));
		// uncomment for additional output
		printMsg(" >> Get pending references result: " + toJson(pendingRefs, JSON_PRETTY_PRINTER));

		// access from an unauthorized user must fail
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Unauthorized get pending reference response is not null", response, notNullValue());
		assertThat("Unauthorized get pending reference response is UNAUTHORIZED", response.getStatus(), equalTo(UNAUTHORIZED.getStatusCode()));		

		// test pending reference pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Paginate pending references first page response is not null", response, notNullValue());
		assertThat("Paginate pending references first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate pending references first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate pending references first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		pendingRefs = testCtxt.jsonMapper().readValue(payload, PendingReferences.class);
		assertThat("Paginate pending references first page result is not null", pendingRefs, notNullValue());
		assertThat("Paginate pending references first page list coincides with expected", pendingRefs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, pendingRefs.getTotalCount()))));		
		// uncomment for additional output			
		printMsg(" >> Paginate pending references first page response body (JSON): " + payload);

		assertThat("Paginate pending references first page links coincides with expected", pendingRefs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));		
		Link lastLink = null;
		for (int i = 0; i < pendingRefs.getLinks().size() && lastLink == null; i++) {
			final Link link = pendingRefs.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate pending references first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Paginate pending references last page response is not null", response, notNullValue());
		assertThat("Paginate pending references last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate pending references last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate pending references last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		pendingRefs = testCtxt.jsonMapper().readValue(payload, PendingReferences.class);
		assertThat("Paginate pending references last page result is not null", pendingRefs, notNullValue());
		assertThat("Paginate pending references last page list is not empty", pendingRefs.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		printMsg(" >> Paginate pending references last page response body (JSON): " + payload);

		assertThat("Paginate pending references last page links coincide with expected", pendingRefs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get pending references pagination (Java object)
		pendingRefs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReferences.class);
		assertThat("Paginate pending references first page result is not null", pendingRefs, notNullValue());
		assertThat("Paginate pending references first page list coincides with expected", pendingRefs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, pendingRefs.getTotalCount()))));		
		// uncomment for additional output
		printMsg(" >> Paginate pending references first page result: " + toJson(pendingRefs, JSON_PRETTY_PRINTER));

		assertThat("Paginate pending references first page links coincide with expected", pendingRefs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < pendingRefs.getLinks().size() && lastLink == null; i++) {
			final Link link = pendingRefs.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate pending references first page link to last page is not null", lastLink, notNullValue());

		pendingRefs = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReferences.class);
		assertThat("Paginate pending references last page result is not null", pendingRefs, notNullValue());
		assertThat("Paginate pending references last page list is not empty", pendingRefs.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		printMsg(" >> Paginate pending references last page result: " + toJson(pendingRefs, JSON_PRETTY_PRINTER));

		assertThat("Paginate pending references last page links coincide with expected", pendingRefs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get pending references applying a keyword matching filter
		pendingRefs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("q", "pmid:\"" + pendingRef.getPubmedId() + "\"")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReferences.class);
		assertThat("Search pending references result is not null", pendingRefs, notNullValue());
		assertThat("Search pending references list coincides with expected", pendingRefs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(pendingRefs.getTotalCount()), hasSize(1)));
		// uncomment for additional output
		printMsg(" >> Search pending references result: " + toJson(pendingRefs, JSON_PRETTY_PRINTER));

		// test get pending references applying a keyword matching filter (JSON encoded)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.queryParam("q", "pmid:\"" + pendingRef.getPubmedId() + "\"")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Search pending references (JSON encoded) response is not null", response, notNullValue());
		assertThat("Search pending references (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Search pending references (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Search pending references (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		pendingRefs = testCtxt.jsonMapper().readValue(payload, PendingReferences.class);
		assertThat("Search pending references (JSON encoded) result is not null", pendingRefs, notNullValue());
		assertThat("Search pending references (JSON encoded) items coincide with expected", pendingRefs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, pendingRefs.getTotalCount()))));
		// uncomment for additional output			
		printMsg(" >> Search pending references response body (JSON): " + payload);

		// test get pending references sorted by PMID
		pendingRefs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("sort", "pmid")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReferences.class);
		assertThat("Sorted pending references result is not null", pendingRefs, notNullValue());
		assertThat("Sorted pending references items coincide with expected", pendingRefs.getElements(), allOf(notNullValue(), not(empty()), hasSize(pendingRefs.getTotalCount())));		
		Date last = new Date(0);
		for (final PendingReference s : pendingRefs.getElements()) {
			assertThat("PendingSeqs are properly sorted", s.getModified().compareTo(last) > 0);
			last = s.getModified();
		}
		// uncomment for additional output			
		printMsg(" >> Sorted pending references result: " + toJson(pendingRefs, JSON_PRETTY_PRINTER));

		// test get pending reference by Id
		pendingRef2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS)).path(pendingRef.getId())	 			
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReference.class);
		assertThat("Get pending reference by catalog number result is not null", pendingRef2, notNullValue());
		assertThat("Get pending reference by catalog number coincides with expected", pendingRef2.equalsIgnoringVolatile(pendingRef));
		// uncomment for additional output
		printMsg(" >> Get pending reference by catalog number result: " + toJson(pendingRef2, JSON_PRETTY_PRINTER));

		// test update pending reference
		pendingRef.getSeqids().add("gb:DEF09876");
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingRef.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(pendingRef, APPLICATION_JSON));
		assertThat("Update pending reference response is not null", response, notNullValue());
		assertThat("Update pending reference response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update pending reference response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update pending reference response entity is not null", payload, notNullValue());
		assertThat("Update pending reference response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Update pending reference response body (JSON), empty is OK: " + payload);
		printMsg(" >> Update pending reference response JAX-RS object: " + response);
		printMsg(" >> Update pending reference HTTP headers: " + response.getStringHeaders());

		// test get pending reference by Id after update
		pendingRef2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingRef.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingReference.class);
		assertThat("Get pending reference by Id after update result is not null", pendingRef2, notNullValue());
		assertThat("Get pending reference by Id after update coincides with expected", pendingRef2.equalsIgnoringVolatile(pendingRef));
		// uncomment for additional output
		printMsg(" >> Get pending reference by Id after update result: " + toJson(pendingRef2, JSON_PRETTY_PRINTER));

		// test delete pending reference
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingRef.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete pending reference response is not null", response, notNullValue());
		assertThat("Delete pending reference response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete pending reference response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete pending reference response entity is not null", payload, notNullValue());
		assertThat("Delete pending reference response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Delete pending reference response body (JSON), empty is OK: " + payload);
		printMsg(" >> Delete pending reference response JAX-RS object: " + response);
		printMsg(" >> Delete pending reference HTTP headers: " + response.getStringHeaders());
	}

}