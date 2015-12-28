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

import static eu.eubrazilcc.lvl.core.CollectionNames.LEISHMANIA_PENDING_COLLECTION;
import static eu.eubrazilcc.lvl.core.CollectionNames.SANDFLY_PENDING_COLLECTION;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
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
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.ObjectAccepted;
import eu.eubrazilcc.lvl.core.ObjectGranted;
import eu.eubrazilcc.lvl.service.rest.ObjectAcceptedResource;
import eu.eubrazilcc.lvl.service.rest.ObjectAcceptedResource.ObjectsAccepted;
import eu.eubrazilcc.lvl.service.rest.ObjectGrantedResource;
import eu.eubrazilcc.lvl.service.rest.ObjectGrantedResource.ObjectsGranted;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to shared objects in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SharedObjectResourcesTest extends Testable {

	public SharedObjectResourcesTest(final TestContext testCtxt) {
		super(testCtxt, SharedObjectResourcesTest.class, true);
	}

	@Override
	protected void test() throws Exception {
		// test granting access to shared object
		final Path gPath = ObjectGrantedResource.class.getAnnotation(Path.class);
		final ObjectGranted objGranted = ObjectGranted.builder()
				.user(testCtxt.credentials().get("user2").getEmail())
				.collection(LEISHMANIA_PENDING_COLLECTION)
				.itemId("LeishVL123")
				.sharedNow()
				.accessType(EDIT_SHARE)
				.build();		
		Response response = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(objGranted, APPLICATION_JSON));
		assertThat("Create new object granted response is not null", response, notNullValue());
		assertThat("Create new object granted response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new object granted response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new object granted response entity is not null", payload, notNullValue());
		assertThat("Create new object granted response entity is empty", isBlank(payload));
		// conditional output			
		printMsg(" >> Create new object granted response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create new object granted response JAX-RS object: " + response);
		printMsg(" >> Create new object granted HTTP headers: " + response.getStringHeaders());

		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create pending response location is not null", location, notNullValue());
		assertThat("Create pending response path is not empty", isNotBlank(location.getPath()), equalTo(true));

		final String objGrantedId = getName(location.toURL().getPath());
		assertThat("Created object granted response Id is not empty", trim(objGrantedId), allOf(notNullValue(), not(equalTo(""))));
		objGranted.setId(objGrantedId);
		objGranted.setOwner(testCtxt.ownerid("user1"));

		// test get shared object by Id as the object's owner (Java object)
		ObjectGranted objGranted2 = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(objGranted.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(ObjectGranted.class);
		assertThat("Get object granted by Id result is not null", objGranted2, notNullValue());
		assertThat("Get object granted by Id date is not null", objGranted2.getSharedDate(), notNullValue());
		objGranted.setSharedDate(objGranted2.getSharedDate());
		assertThat("Get object granted by Id coincides with expected", objGranted2.equalsIgnoringVolatile(objGranted));
		// conditional output
		printMsg(" >> Get object granted by Id result: " + toJson(objGranted2, JSON_PRETTY_PRINTER));

		// test get shared object by Id as the granted user (Java object)
		final Path aPath = ObjectAcceptedResource.class.getAnnotation(Path.class);
		final ObjectAccepted objAccepted = ObjectAccepted.builder()
				.id(objGranted.getId())
				.owner(objGranted.getOwner())
				.user(objGranted.getUser())
				.collection(objGranted.getCollection())
				.itemId(objGranted.getItemId())
				.sharedDate(objGranted.getSharedDate())
				.accessType(objGranted.getAccessType())
				.build();

		ObjectAccepted objAccepted2 = testCtxt.target().path(aPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.path(objGranted.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(ObjectAccepted.class);
		assertThat("Get object accepted by Id result is not null", objAccepted2, notNullValue());
		assertThat("Get object accepted by Id date is not null", objAccepted2.getSharedDate(), notNullValue());
		assertThat("Get object accepted by Id coincides with expected", objAccepted2.equalsIgnoringVolatile(objAccepted));
		// conditional output
		printMsg(" >> Get object accepted by Id result: " + toJson(objAccepted2, JSON_PRETTY_PRINTER));
		
		// test shared object retrieving
		
		
		
		// TODO

		// create a larger dataset to test complex operations
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		final int numItems = 3;
		for (int i = 0; i < numItems; i++) {								
			final ObjectGranted objGranted3 = ObjectGranted.builder()
					.user(testCtxt.credentials().get("user3").getEmail())
					.collection(SANDFLY_PENDING_COLLECTION)
					.itemId("XYZ" + i)
					.sharedDate(calendar.getTime())
					.accessType(i%2 == 0 ? EDIT_SHARE : VIEW_SHARE)					
					.build();
			response = testCtxt.target().path(gPath.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
					.post(entity(objGranted3, APPLICATION_JSON));
			assertThat("Create new object granted response is not null", response, notNullValue());
			assertThat("Create new object granted response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		}

		// test list all shared objects as the object's owner (JSON encoded)
		response = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Get objects granted response is not null", response, notNullValue());
		assertThat("Get objects granted response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get objects granted response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get objects granted response entity is not null", payload, notNullValue());
		assertThat("Get objects granted response entity is not empty", isNotBlank(payload));
		// conditional output
		printMsg(" >> Get objects granted response body (JSON): " + payload);
		printMsg(" >> Get objects granted response JAX-RS object: " + response);
		printMsg(" >> Get objects granted HTTP headers: " + response.getStringHeaders());
		ObjectsGranted objsGranted = testCtxt.jsonMapper().readValue(payload, ObjectsGranted.class);
		assertThat("Get objects granted (JSON encoded) result is not null", objsGranted, notNullValue());
		assertThat("Get objects granted (JSON encoded) items coincide with expected", objsGranted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(objsGranted.getTotalCount()), hasSize(numItems)));
		// conditional output
		printMsg(" >> Get object accepted by Id result: " + toJson(objsGranted, JSON_PRETTY_PRINTER));

		// test list all shared objects as the granted user (JSON encoded)
		response = testCtxt.target().path(aPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user3")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get();
		assertThat("Get objects accepted response is not null", response, notNullValue());
		assertThat("Get objects accepted response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get objects accepted response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get objects accepted response entity is not null", payload, notNullValue());
		assertThat("Get objects accepted response entity is not empty", isNotBlank(payload));
		// conditional output
		printMsg(" >> Get objects accepted response body (JSON): " + payload);
		printMsg(" >> Get objects accepted response JAX-RS object: " + response);
		printMsg(" >> Get objects accepted HTTP headers: " + response.getStringHeaders());
		ObjectsAccepted objsAccepted = testCtxt.jsonMapper().readValue(payload, ObjectsAccepted.class);
		assertThat("Get objects accepted (JSON encoded) result is not null", objsAccepted, notNullValue());
		assertThat("Get objects accepted (JSON encoded) items coincide with expected", objsAccepted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(objsAccepted.getTotalCount()), hasSize(numItems)));
		// conditional output
		printMsg(" >> Get object accepted by Id result: " + toJson(objsAccepted, JSON_PRETTY_PRINTER));

		// test list all shared objects as the object's owner (Java object)
		objsGranted = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(ObjectsGranted.class);
		assertThat("Get objects granted result is not null", objsGranted, notNullValue());
		assertThat("Get objects granted list coincides with expected", objsGranted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(objsGranted.getTotalCount()), hasSize(numItems)));
		// conditional output
		printMsg(" >> Get objects granted result: " + toJson(objsGranted, JSON_PRETTY_PRINTER));

		// access from an unauthorized user must fail
		response = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user2")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Unauthorized get object granted response is not null", response, notNullValue());
		assertThat("Unauthorized get object granted response is UNAUTHORIZED", response.getStatus(), equalTo(UNAUTHORIZED.getStatusCode()));

		// test shared object pagination as the object's owner (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(gPath.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Paginate objects granted first page response is not null", response, notNullValue());
		assertThat("Paginate objects granted first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate objects granted first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate objects granted first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		objsGranted = testCtxt.jsonMapper().readValue(payload, ObjectsGranted.class);
		assertThat("Paginate objects granted first page result is not null", objsGranted, notNullValue());
		assertThat("Paginate objects granted first page list coincides with expected", objsGranted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(min(perPage, objsGranted.getTotalCount()))));		
		// conditional output
		printMsg(" >> Paginate objects granted first page response body (JSON): " + payload);

		assertThat("Paginate objects granted first page links coincides with expected", objsGranted.getLinks(), 
				allOf(notNullValue(), not(empty()), hasSize(2)));
		Link lastLink = null;
		for (int i = 0; i < objsGranted.getLinks().size() && lastLink == null; i++) {
			final Link link = objsGranted.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate objects granted first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Paginate objects granted last page response is not null", response, notNullValue());
		assertThat("Paginate objects granted last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate objects granted last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate objects granted last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		objsGranted = testCtxt.jsonMapper().readValue(payload, ObjectsGranted.class);
		assertThat("Paginate objects granted last page result is not null", objsGranted, notNullValue());
		assertThat("Paginate objects granted last page list is not empty", objsGranted.getElements(), allOf(notNullValue(), not(empty())));		
		// conditional output			
		printMsg(" >> Paginate objects granted last page response body (JSON): " + payload);

		assertThat("Paginate objects granted last page links coincide with expected", objsGranted.getLinks(), 
				allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get shared objects pagination as the granted user (Java object)
		objsAccepted = testCtxt.target().path(aPath.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get(ObjectsAccepted.class);
		assertThat("Paginate objects granted first page result is not null", objsAccepted, notNullValue());
		assertThat("Paginate objects granted first page list coincides with expected", objsAccepted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(min(perPage, objsAccepted.getTotalCount()))));		
		// conditional output
		printMsg(" >> Paginate objects granted first page result: " + toJson(objsAccepted, JSON_PRETTY_PRINTER));

		assertThat("Paginate objects granted first page links coincide with expected", objsAccepted.getLinks(), 
				allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < objsAccepted.getLinks().size() && lastLink == null; i++) {
			final Link link = objsAccepted.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate objects granted first page link to last page is not null", lastLink, notNullValue());

		objsAccepted = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get(ObjectsAccepted.class);
		assertThat("Paginate objects granted last page result is not null", objsAccepted, notNullValue());
		assertThat("Paginate objects granted last page list is not empty", objsAccepted.getElements(), allOf(notNullValue(), not(empty())));		
		// conditional output
		printMsg(" >> Paginate objects granted last page result: " + toJson(objsAccepted, JSON_PRETTY_PRINTER));

		assertThat("Paginate objects granted last page links coincide with expected", objsAccepted.getLinks(), 
				allOf(notNullValue(), not(empty()), hasSize(2)));

		// filter: collection+itemId search as the object's owner (Java object)
		objsGranted = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("q", String.format("collection:\"%s\" itemId:\"%s\"", SANDFLY_PENDING_COLLECTION, "XYZ1"))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(ObjectsGranted.class);
		assertThat("Search objects granted result is not null", objsGranted, notNullValue());
		assertThat("Search objects granted list coincides with expected", objsGranted.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(objsGranted.getTotalCount()), hasSize(1)));
		// conditional output
		printMsg(" >> Search objects granted result: " + toJson(objsGranted, JSON_PRETTY_PRINTER));

		// filter: collection search as the object's owner (JSON encoded)
		response = testCtxt.target().path(aPath.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.queryParam("q", String.format("collection:\"%s\"", SANDFLY_PENDING_COLLECTION))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get();
		assertThat("Search objects granted (JSON encoded) response is not null", response, notNullValue());
		assertThat("Search objects granted (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Search objects granted (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Search objects granted (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		objsGranted = testCtxt.jsonMapper().readValue(payload, ObjectsGranted.class);
		assertThat("Search objects granted (JSON encoded) result is not null", objsGranted, notNullValue());
		assertThat("Search objects granted (JSON encoded) items coincide with expected", objsGranted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(min(perPage, objsGranted.getTotalCount()))));
		// conditional output
		printMsg(" >> Search objects granted response body (JSON): " + payload);

		// sorting by shared date in ascending order (as the object's owner)
		objsGranted = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("sort", "sharedDate")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(ObjectsGranted.class);
		assertThat("Sorted objects granted result is not null", objsGranted, notNullValue());
		assertThat("Sorted objects granted items coincide with expected", objsGranted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(objsGranted.getTotalCount())));		
		Date last = new Date(0);
		for (final ObjectGranted s : objsGranted.getElements()) {
			assertThat("objects granted are properly sorted", s.getSharedDate().after(last));
			last = s.getSharedDate();
		}
		// conditional output			
		printMsg(" >> Sorted objects granted result: " + toJson(objsGranted, JSON_PRETTY_PRINTER));

		// sorting by shared date in descending order (as the object's owner)
		objsAccepted = testCtxt.target().path(aPath.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("sort", "sharedDate")
				.queryParam("order", "desc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
				.get(ObjectsAccepted.class);
		assertThat("Sorted objects accepted result is not null", objsAccepted, notNullValue());
		assertThat("Sorted objects accepted items coincide with expected", objsAccepted.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(objsAccepted.getTotalCount())));		
		calendar.add(Calendar.MINUTE, 1);
		last = calendar.getTime();
		for (final ObjectAccepted s : objsAccepted.getElements()) {
			assertThat("objects accepted are properly sorted", s.getSharedDate().before(last));
			last = s.getSharedDate();
		}
		// conditional output
		printMsg(" >> Sorted objects accepted result: " + toJson(objsAccepted, JSON_PRETTY_PRINTER));

		// test update object granted
		objGranted.setAccessType(VIEW_SHARE);
		response = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(objGranted.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(objGranted, APPLICATION_JSON));
		assertThat("Update object granted response is not null", response, notNullValue());
		assertThat("Update object granted response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update object granted response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update object granted response entity is not null", payload, notNullValue());
		assertThat("Update object granted response entity is empty", isBlank(payload));
		// conditional output			
		printMsg(" >> Update object granted response body (JSON), empty is OK: " + payload);
		printMsg(" >> Update object granted response JAX-RS object: " + response);
		printMsg(" >> Update object granted HTTP headers: " + response.getStringHeaders());

		// test get object granted by Id after update
		objGranted2 = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(objGranted.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(ObjectGranted.class);
		assertThat("Get object granted by Id after update result is not null", objGranted2, notNullValue());
		assertThat("Get object granted by Id after update coincides with expected", objGranted2.equalsIgnoringVolatile(objGranted));
		// conditional output
		printMsg(" >> Get object granted by Id after update result: " + toJson(objGranted2, JSON_PRETTY_PRINTER));

		// test delete object granted
		response = testCtxt.target().path(gPath.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(objGranted.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete object granted response is not null", response, notNullValue());
		assertThat("Delete object granted response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete object granted response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete object granted response entity is not null", payload, notNullValue());
		assertThat("Delete object granted response entity is empty", isBlank(payload));
		// conditional output			
		printMsg(" >> Delete object granted response body (JSON), empty is OK: " + payload);
		printMsg(" >> Delete object granted response JAX-RS object: " + response);
		printMsg(" >> Delete object granted HTTP headers: " + response.getStringHeaders());
	}

}