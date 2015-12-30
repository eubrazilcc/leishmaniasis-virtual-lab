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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.service.Task.TaskType.IMPORT_LEISHMANIA_SAMPLES;
import static eu.eubrazilcc.lvl.service.Task.TaskType.IMPORT_SANDFLY_SEQ;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;

import eu.eubrazilcc.lvl.service.Progress;
import eu.eubrazilcc.lvl.service.Task;
import eu.eubrazilcc.lvl.service.rest.TaskResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests task execution in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class TaskResourceTest extends Testable {

	public TaskResourceTest(final TestContext testCtxt) {
		super(testCtxt, TaskResourceTest.class, false);
	}

	public void test() throws Exception {
		// test import sandfly sequences from GenBank
		final Path path = TaskResource.class.getAnnotation(Path.class);
		Task task = Task.builder()
				.type(IMPORT_SANDFLY_SEQ)
				.ids(newArrayList("353470160", "353483325", "384562886"))
				.build();

		Response response = testCtxt.target().path(path.value())					
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(task, APPLICATION_JSON));
		assertThat("Create import sandflies task response is not null", response, notNullValue());
		assertThat("Create import sandflies task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create import sandflies task response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create import sandflies task response entity is not null", payload, notNullValue());
		assertThat("Create import sandflies task response entity is empty", isBlank(payload));
		// conditional output
		printMsg(" >> Create import sandflies task response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create import sandflies task response JAX-RS object: " + response);
		printMsg(" >> Create import sandflies task HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));

		// test import sandflies task progress
		EventInput eventInput = testCtxt.target().path(path.value())
				.path("progress")
				.path(getName(location.getPath()))
				.queryParam("refresh", 1)
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(EventInput.class);
		while (!eventInput.isClosed()) {
			final InboundEvent inboundEvent = eventInput.read();
			if (inboundEvent == null) {
				// connection has been closed
				break;
			}
			final String id = trimToEmpty(inboundEvent.getId()); // Last-Event-ID is optional
			final String name = inboundEvent.getName();
			assertThat("Progress event name is not null", name, notNullValue());
			assertThat("Progress event name is not empty", isNotBlank(name));
			final String data = inboundEvent.readData(String.class);
			assertThat("Progress event data is not null", data, notNullValue());
			assertThat("Progress event data is not empty", isNotBlank(data));
			final Progress progress = testCtxt.jsonMapper().readValue(data, Progress.class);				
			assertThat("Progress event decoded object is not null", progress, notNullValue());
			assertThat("Import sandflies task does not have errors", !progress.isHasErrors());
			// conditional output				
			printMsg(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: id=" 
					+ id + "; name=" + name + "; data=" + data + "; object=" + progress);
		}

		// repeat the import new sandflies task to test how the subscription is made from a client using the JavaScript EventSource interface
		task = Task.builder()
				.type(IMPORT_SANDFLY_SEQ)
				.ids(newArrayList("430902590"))
				.build();
		response = testCtxt.target().path(path.value()).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(task, APPLICATION_JSON));
		assertThat("Create import sandflies task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create import sandflies task response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		// conditional output
		printMsg(" >> Create import sandflies task response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create import sandflies task response JAX-RS object: " + response);
		printMsg(" >> Create import sandflies task HTTP headers: " + response.getStringHeaders());
		location = new URI((String)response.getHeaders().get("Location").get(0));

		eventInput = testCtxt.target().path(path.value())
				.path("progress")
				.path(getName(location.getPath()))
				.queryParam("refresh", 1)
				.queryParam("token", testCtxt.token("root")) // token attribute replaces HTTP header to overcome this EventSource limitation
				.request()
				.get(EventInput.class);
		while (!eventInput.isClosed()) {
			final InboundEvent inboundEvent = eventInput.read();
			if (inboundEvent == null) {
				// connection has been closed
				break;
			}
			final String data = inboundEvent.readData(String.class);
			assertThat("Progress event data is not empty", isNotBlank(data));
			final Progress progress = testCtxt.jsonMapper().readValue(data, Progress.class);
			assertThat("Progress event decoded object is not null", progress, notNullValue());
			assertThat("Import sandflies task does not have errors", !progress.isHasErrors());
			// conditional output				
			printMsg(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: object=" + progress);
		}

		// test import samples from speciesLink
		task = Task.builder()
				.type(IMPORT_LEISHMANIA_SAMPLES)
				.ids(newArrayList("IOCL 0001"))
				.build();

		response = testCtxt.target().path(path.value())					
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(task, APPLICATION_JSON));
		assertThat("Create import leishmania task response is not null", response, notNullValue());
		assertThat("Create import leishmania task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create import leishmania task response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Create import leishmania task response entity is not null", payload, notNullValue());
		assertThat("Create import leishmania task response entity is empty", isBlank(payload));
		// conditional output
		printMsg(" >> Create import leishmania task response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create import leishmania task response JAX-RS object: " + response);
		printMsg(" >> Create import leishmania task HTTP headers: " + response.getStringHeaders());
		location = new URI((String)response.getHeaders().get("Location").get(0));		

		eventInput = testCtxt.target().path(path.value())
				.path("progress")
				.path(getName(location.getPath()))
				.queryParam("refresh", 1)
				.queryParam("token", testCtxt.token("root")) // token attribute replaces HTTP header to overcome this EventSource limitation
				.request()
				.get(EventInput.class);
		while (!eventInput.isClosed()) {
			final InboundEvent inboundEvent = eventInput.read();
			if (inboundEvent == null) {
				// connection has been closed
				break;
			}
			final String data = inboundEvent.readData(String.class);
			assertThat("Progress event data is not empty", isNotBlank(data));
			final Progress progress = testCtxt.jsonMapper().readValue(data, Progress.class);
			assertThat("Progress event decoded object is not null", progress, notNullValue());
			assertThat("Import leishmania task does not have errors", !progress.isHasErrors());
			// conditional output				
			printMsg(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: object=" + progress);
		}
	}

}