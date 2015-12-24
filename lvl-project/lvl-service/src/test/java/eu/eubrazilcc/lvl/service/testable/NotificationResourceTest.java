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
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.net.URI;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Notification.Action;
import eu.eubrazilcc.lvl.service.Notifications;
import eu.eubrazilcc.lvl.service.rest.NotificationResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to notifications in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NotificationResourceTest extends Testable {

	public NotificationResourceTest(final TestContext testCtxt) {
		super(testCtxt, NotificationResourceTest.class, false);
	}

	@Override
	public void test() throws Exception {
		// test create new notification
		final Path path = NotificationResource.class.getAnnotation(Path.class);
		final Notification notification = Notification.builder()
				.message("This is an example")
				.addressee("user1")
				.build();
		Response response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(notification, APPLICATION_JSON));			
		assertThat("Create new notification response is not null", response, notNullValue());
		assertThat("Create new notification response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new notification response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new notification response entity is not null", payload, notNullValue());
		assertThat("Create new notification response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Create new notification response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create new notification response JAX-RS object: " + response);
		printMsg(" >> Create new notification HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create notification location is not null", location, notNullValue());
		assertThat("Create notification path is not empty", isNotBlank(location.getPath()), equalTo(true));

		String notificationId = getName(location.toURL().getPath());
		assertThat("Created notification Id is not empty", trim(notificationId), allOf(notNullValue(), not(equalTo(""))));
		notification.setId(notificationId);

		// test get notification by Id (Java object)
		Notification notification2 = null;
		int tries = 0;
		do {
			try {
				notification2 = testCtxt.target().path(path.value())
						.path(urlEncodeUtf8(LVL_DEFAULT_NS))
						.path(notification.getId())
						.request(APPLICATION_JSON)
						.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
						.get(Notification.class);
			} catch (NotFoundException ignore) { }
			if (notification2 == null) Thread.sleep(1000l);
		} while (notification2 == null && tries++ < 30);
		assertThat("Get notification by Id result is not null", notification2, notNullValue());
		assertThat("Get notification by Id date is not null", notification2.getIssuedAt(), notNullValue());
		notification.setIssuedAt(notification2.getIssuedAt());
		assertThat("Get notification by Id priority is not null", notification2.getPriority(), notNullValue());
		notification.setPriority(notification2.getPriority());
		assertThat("Get notification by Id addresse coincides with expected", trim(notification2.getAddressee()), 
				allOf(notNullValue(), not(equalTo("")), startsWith("user1@")));
		notification.setAddressee(notification2.getAddressee());
		assertThat("Get notification by Id coincides with expected", notification2.equalsIgnoringVolatile(notification));
		// uncomment for additional output
		printMsg(" >> Get notification by Id result: " + notification2.toString());

		// notify a different user
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(Notification.builder()
						.message("This is an example too")
						.addressee("user2")
						.build(), APPLICATION_JSON));
		assertThat("Create new notification response is not null", response, notNullValue());
		assertThat("Create new notification response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create notification location is not null", location, notNullValue());
		assertThat("Create notification path is not empty", isNotBlank(location.getPath()), equalTo(true));
		notificationId = getName(location.toURL().getPath());

		// test get notification by Id (JSON encoded)
		response = null;
		tries = 0;
		do {
			try {
				response = testCtxt.target().path(path.value())
						.path(urlEncodeUtf8(LVL_DEFAULT_NS))
						.path(notificationId)
						.request(APPLICATION_JSON)
						.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
						.get();
			} catch (NotFoundException ignore) { }
			if (response == null) Thread.sleep(1000l);
		} while (response == null && tries++ < 30);
		assertThat("Get notification response is not null", response, notNullValue());
		assertThat("Get notification response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get notification response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get notification response entity is not null", payload, notNullValue());
		assertThat("Get notification response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Get notification response body (JSON): " + payload);
		printMsg(" >> Get notification response JAX-RS object: " + response);
		printMsg(" >> Get notification HTTP headers: " + response.getStringHeaders());
		notification2 = testCtxt.jsonMapper().readValue(payload, Notification.class);
		assertThat("Get notification by Id result is not null", notification2, notNullValue());
		assertThat("Get notification by Id date is not null", notification2.getIssuedAt(), notNullValue());
		assertThat("Get notification by Id priority is not null", notification2.getPriority(), notNullValue());
		assertThat("Get notification by Id addresse coincides with expected", trim(notification2.getAddressee()), 
				allOf(notNullValue(), not(equalTo("")), startsWith("user2@")));
		// uncomment for additional output
		printMsg(" >> Get notification by Id result: " + notification2.toString());

		// test list all notifications (JSON encoded)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Get notifications response is not null", response, notNullValue());
		assertThat("Get notifications response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get notifications response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get notifications response entity is not null", payload, notNullValue());
		assertThat("Get notifications response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Get notifications response body (JSON): " + payload);
		printMsg(" >> Get notifications response JAX-RS object: " + response);
		printMsg(" >> Get notifications HTTP headers: " + response.getStringHeaders());
		Notifications notifications = testCtxt.jsonMapper().readValue(payload, Notifications.class);
		assertThat("Get notifications result is not null", notifications, notNullValue());
		assertThat("Get notifications elements coincide with expected", notifications.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(1)));
		assertThat("Get notifications items count coincide with list size", notifications.getElements().size(), 
				equalTo(notifications.getTotalCount()));		
		assertThat("Get notifications content coincides with expected", notifications.getElements().get(0)
				.equalsIgnoringVolatile(notification));

		// test list all notifications (Java object)
		notifications = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get(Notifications.class);
		assertThat("Get notifications result is not null", notifications, notNullValue());		
		assertThat("Get notifications elements coincide with expected", notifications.getElements(), 
				allOf(notNullValue(), not(empty()), hasSize(1)));
		assertThat("Get notifications items count coincide with list size", notifications.getElements().size(), 
				equalTo(notifications.getTotalCount()));
		assertThat("Get notifications content coincides with expected", not(notifications.getElements().get(0)
				.equalsIgnoringVolatile(notification)));
		// uncomment for additional output			
		printMsg(" >> Get notifications result: " + notifications.toString());

		// test update notification
		notification.setAction(new Action("/#/action", "This is an action"));
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(notification.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(notification, APPLICATION_JSON));
		assertThat("Update notification response is not null", response, notNullValue());
		assertThat("Update notification response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update notification response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update notification response entity is not null", payload, notNullValue());
		assertThat("Update notification response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Update notification response body (JSON), empty is OK: " + payload);
		printMsg(" >> Update notification response JAX-RS object: " + response);
		printMsg(" >> Update notification HTTP headers: " + response.getStringHeaders());

		// test get notification by Id after update
		notification2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(notification.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(Notification.class);
		assertThat("Get notification by Id after update result is not null", notification2, notNullValue());
		assertThat("Get notification by Id after update coincides with expected", notification2.equalsIgnoringVolatile(notification));
		// uncomment for additional output
		printMsg(" >> Get notification by Id after update result: " + notification2.toString());

		// test delete notification
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(notification.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete notification response is not null", response, notNullValue());
		assertThat("Delete notification response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete notification response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete notification response entity is not null", payload, notNullValue());
		assertThat("Delete notification response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Delete notification response body (JSON), empty is OK: " + payload);
		printMsg(" >> Delete notification response JAX-RS object: " + response);
		printMsg(" >> Delete notification HTTP headers: " + response.getStringHeaders());
	}

}