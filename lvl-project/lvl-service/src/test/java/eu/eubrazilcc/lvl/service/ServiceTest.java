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

package eu.eubrazilcc.lvl.service;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.bearerHeader;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.Sequences;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.geospatial.FeatureCollection;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.service.Task.TaskType;
import eu.eubrazilcc.lvl.service.rest.SequenceResource;
import eu.eubrazilcc.lvl.service.rest.TaskResource;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common;
import eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager;

/**
 * Tests REST Web service.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ServiceTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			"lvl-service-test-Hf330xKUcsn7vnlKQXFndptow52MvZNKWxxpbnVqAA"));

	private static final String BASE_URI = "https://localhost:8443/lvl-service/rest/v1";

	private WebTarget target;
	private static final String token = "1234567890abcdEFGhiJKlMnOpqrstUVWxyZ";

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
		// load test configuration
		final ImmutableList.Builder<URL> builder = new ImmutableList.Builder<URL>();
		final ImmutableList<URL> defaultUrls = ConfigurationManager.getDefaultConfiguration();
		for (final URL url : defaultUrls) {
			if (!url.toString().endsWith(ConfigurationManager.REST_SERVICE_CONFIG)) {
				builder.add(url);
			} else {
				builder.add(this.getClass().getResource("/config/lvl-service.xml"));
			}
		}
		CONFIG_MANAGER.setup(builder.build());
		CONFIG_MANAGER.preload();
		// setup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);		
		// prepare client
		final Client client = ClientBuilder.newBuilder()
				.register(MoxyJsonFeature.class)
				.register(SseFeature.class)
				.build();
		// configure Web target
		target = client.target(BASE_URI);
		// insert valid token in the database
		TOKEN_DAO.insert(AccessToken.builder()
				.token(token)
				.issuedAt(System.currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.scope(ScopeManager.asList(ScopeManager.all()))
				.build());
	}

	@After
	public void cleanUp() {
		// cleanup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	@Test
	public void test() {
		System.out.println("ServiceTest.test()");
		try {
			// test import sequences task
			Path path = TaskResource.class.getAnnotation(Path.class);			
			final Task task = Task.builder()
					.type(TaskType.IMPORT_SEQUENCES)
					.ids(newArrayList("353470160", "353483325", "384562886"))
					.build();
			Response response = target.path(path.value()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.post(Entity.entity(task, MediaType.APPLICATION_JSON_TYPE));

			assertThat("Create import sequences task response is not null", response, notNullValue());
			assertThat("Create import sequences task response is CREATED", response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
			assertThat("Create import sequences task response is not empty", response.getEntity(), notNullValue());
			String payload = response.readEntity(String.class);
			assertThat("Create import sequences task response entity is not null", payload, notNullValue());
			assertThat("Create import sequences task response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println("Create import sequences task response body (JSON), empty is OK: " + payload);
			System.out.println("Create import sequences task response JAX-RS object: " + response);
			System.out.println("Create import sequences task HTTP headers: " + response.getStringHeaders());
			final URI location = new URI((String)response.getHeaders().get("Location").get(0));

			// test import sequences task progress
			boolean hasErrors = false;
			final EventInput eventInput = target.path(path.value())
					.path("progress")
					.path(getName(location.getPath()))
					.queryParam("refresh", 1)
					.request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.get(EventInput.class);
			while (!eventInput.isClosed()) {
				final InboundEvent inboundEvent = eventInput.read();
				if (inboundEvent == null) {
					// connection has been closed
					break;
				}
				final String name = inboundEvent.getName();
				assertThat("Progress event name is not null", name, notNullValue());
				assertThat("Progress event name is not empty", isNotBlank(name));
				final String data = inboundEvent.readData(String.class);
				assertThat("Progress event data is not null", data, notNullValue());
				assertThat("Progress event data is not empty", isNotBlank(data));
				final Progress progress = JSON_MAPPER.readValue(data, Progress.class);
				assertThat("Progress event decoded object is not null", progress, notNullValue());
				assertThat("Import sequences task does not have errors", !hasErrors);				
				/* uncomment for additional output */				
				System.out.println(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: name=" 
						+ name + "; data=" + data + "; object=" + progress);
			}

			// test create new sequence
			path = SequenceResource.class.getAnnotation(Path.class);
			final Sequence sequence = Sequence.builder()
					.dataSource(DataSource.GENBANK)
					.definition("Example sequence")
					.accession("LVL00000")
					.version("0.0")
					.organism("Example organism")
					.location(Point.builder().coordinate(1.2d, 3.4d).build())
					.build();
			final SequenceKey sequenceKey = SequenceKey.builder()
					.dataSource(sequence.getDataSource())
					.accession(sequence.getAccession())
					.build();
			response = target.path(path.value()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.post(Entity.entity(sequence, MediaType.APPLICATION_JSON_TYPE));			
			assertThat("Create new sequence response is not null", response, notNullValue());
			assertThat("Create new sequence response is CREATED", response.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
			assertThat("Create new sequence response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new sequence response entity is not null", payload, notNullValue());
			assertThat("Create new sequence response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("Create new sequence response body (JSON), empty is OK: " + payload);
			System.out.println("Create new sequence response JAX-RS object: " + response);
			System.out.println("Create new sequence HTTP headers: " + response.getStringHeaders());

			// test get sequences (JSON encoded)
			response = target.path(path.value()).request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.get();
			assertThat("Get sequences response is not null", response, notNullValue());
			assertThat("Get sequences response is OK", response.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
			assertThat("Get sequences response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get sequences response entity is not null", payload, notNullValue());
			assertThat("Get sequences response entity is not empty", isNotBlank(payload));
			/* uncomment for additional output */			
			System.out.println("Get sequences response body (JSON): " + payload);
			System.out.println("Get sequences response JAX-RS object: " + response);
			System.out.println("Get sequences HTTP headers: " + response.getStringHeaders());

			// test get sequences (Java object)
			final Sequences sequences = target.path(path.value()).request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.get(Sequences.class);
			assertThat("Get sequences result is not null", sequences, notNullValue());
			assertThat("Get sequences list is not null", sequences.getSequences(), notNullValue());
			assertThat("Get sequences list is not empty", !sequences.getSequences().isEmpty());
			/* uncomment for additional output */			
			System.out.println("Get sequences result: " + sequences.toString());

			// test get sequence by data source + accession number
			Sequence sequence2 = target.path(path.value()).path(sequenceKey.toId(SequenceResource.ID_SEPARATOR))
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.get(Sequence.class);
			assertThat("Get sequence by accession number result is not null", sequence2, notNullValue());
			assertThat("Get sequence by accession number coincides with expected", sequence2.equalsIgnoreLink(sequence));
			/* uncomment for additional output */
			System.out.println("Get sequence by accession number result: " + sequence2.toString());

			// test update sequence
			sequence.setDefinition("Modified example sequence");
			response = target.path(path.value()).path(sequenceKey.toId(SequenceResource.ID_SEPARATOR))
					.request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.put(Entity.entity(sequence, MediaType.APPLICATION_JSON_TYPE));
			assertThat("Update sequence response is not null", response, notNullValue());
			assertThat("Update sequence response is NO_CONTENT", response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
			assertThat("Update sequence response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update sequence response entity is not null", payload, notNullValue());
			assertThat("Update sequence response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("Update sequence response body (JSON), empty is OK: " + payload);
			System.out.println("Update sequence response JAX-RS object: " + response);
			System.out.println("Update sequence HTTP headers: " + response.getStringHeaders());

			// test get sequence by accession number after update
			sequence2 = target.path(path.value()).path(sequenceKey.toId(SequenceResource.ID_SEPARATOR))
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.get(Sequence.class);
			assertThat("Get sequence by accession number after update result is not null", sequence2, notNullValue());
			assertThat("Get sequence by accession number after update coincides with expected", sequence2.equalsIgnoreLink(sequence));
			/* uncomment for additional output */
			System.out.println("Get sequence by accession number after update result: " + sequence2.toString());			

			// test find sequences near to a location
			final FeatureCollection featCol = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d)
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.get(FeatureCollection.class);
			assertThat("Get nearby sequences result is not null", featCol, notNullValue());
			assertThat("Get nearby sequences list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby sequences list is not empty", featCol.getFeatures().length > 0);
			/* uncomment for additional output */			
			System.out.println("Get nearby sequences result: " + featCol.toString());

			// test delete sequence
			response = target.path(path.value()).path(sequenceKey.toId(SequenceResource.ID_SEPARATOR))
					.request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(token))
					.delete();
			assertThat("Delete sequence response is not null", response, notNullValue());
			assertThat("Delete sequence response is NO_CONTENT", response.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
			assertThat("Delete sequence response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete sequence response entity is not null", payload, notNullValue());
			assertThat("Delete sequence response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("Delete sequence response body (JSON), empty is OK: " + payload);
			System.out.println("Delete sequence response JAX-RS object: " + response);
			System.out.println("Delete sequence HTTP headers: " + response.getStringHeaders());			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ServiceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ServiceTest.test() has finished");
		}
	}

}