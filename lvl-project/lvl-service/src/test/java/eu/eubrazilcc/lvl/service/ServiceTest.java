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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.service.Task.TaskType.IMPORT_SEQUENCES;
import static eu.eubrazilcc.lvl.service.io.PublicLinkWriter.unsetPublicLink;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.bearerHeader;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.all;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asList;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.user;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.getPathNoEndSeparator;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import org.apache.http.client.fluent.Request;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.PublicLink;
import eu.eubrazilcc.lvl.core.PublicLink.Target;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.rest.PublicLinkResource;
import eu.eubrazilcc.lvl.service.rest.SequenceResource;
import eu.eubrazilcc.lvl.service.rest.TaskResource;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;

/**
 * Tests REST Web service.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ServiceTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			"lvl-service-test-Hf330xKUcsn7vnlKQXFndptow52MvZNKWxxpbnVqAA"));

	private static final String HOST = "https://localhost:8443";
	private static final String SERVICE = "/lvl-service/rest/v1";
	private static final String BASE_URI = HOST + SERVICE;

	private WebTarget target;
	private static final String TOKEN_ROOT = "1234567890abcdEFGhiJKlMnOpqrstUVWxyZ";
	private static final String TOKEN_USER = "0987654321zYXwvuTSRQPoNmLkjIHgfeDCBA";

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private static final List<String> PUBLIC_LINKS = newArrayList();

	@Before
	public void setUp() throws Exception {
		// load test configuration
		final ImmutableList.Builder<URL> builder = new ImmutableList.Builder<URL>();
		final ImmutableList<URL> defaultUrls = getDefaultConfiguration();
		for (final URL url : defaultUrls) {
			if (!url.toString().endsWith(REST_SERVICE_CONFIG)) {
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
				.register(JacksonFeature.class)
				.register(SseFeature.class)
				.build();
		// configure Web target
		target = client.target(BASE_URI);
		// insert valid tokens in the database
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_ROOT)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.scope(asList(all()))
				.build());
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_USER)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId("user1")
				.scope(asList(user("user1")))
				.build());
	}

	@After
	public void cleanUp() {
		// cleanup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);
		for (final String link : PUBLIC_LINKS) {
			try {
				unsetPublicLink(new File(link));
			} catch (Exception ignore) { }
		}
	}

	@Test
	public void test() {
		System.out.println("ServiceTest.test()");
		try {
			// test import sequences task
			Path path = TaskResource.class.getAnnotation(Path.class);
			Task task = Task.builder()
					.type(IMPORT_SEQUENCES)
					.ids(newArrayList("353470160", "353483325", "384562886"))
					.build();

			Response response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(task, APPLICATION_JSON_TYPE));
			assertThat("Create import sequences task response is not null", response, notNullValue());
			assertThat("Create import sequences task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create import sequences task response is not empty", response.getEntity(), notNullValue());
			String payload = response.readEntity(String.class);
			assertThat("Create import sequences task response entity is not null", payload, notNullValue());
			assertThat("Create import sequences task response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println(" >> Create import sequences task response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create import sequences task response JAX-RS object: " + response);
			System.out.println(" >> Create import sequences task HTTP headers: " + response.getStringHeaders());
			URI location = new URI((String)response.getHeaders().get("Location").get(0));

			// test import sequences task progress
			EventInput eventInput = target.path(path.value())
					.path("progress")
					.path(getName(location.getPath()))
					.queryParam("refresh", 1)
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
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
				final Progress progress = JSON_MAPPER.readValue(data, Progress.class);				
				assertThat("Progress event decoded object is not null", progress, notNullValue());
				assertThat("Import sequences task does not have errors", !progress.isHasErrors());
				/* uncomment for additional output */				
				System.out.println(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: id=" 
						+ id + "; name=" + name + "; data=" + data + "; object=" + progress);
			}

			// repeat the import new sequences task to test how the subscription is made from a client using the JavaScript EventSource interface
			task = Task.builder()
					.type(IMPORT_SEQUENCES)
					.ids(newArrayList("430902590"))
					.build();
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(task, APPLICATION_JSON_TYPE));
			assertThat("Create import sequences task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			/* uncomment for additional output */
			System.out.println(" >> Create import sequences task response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create import sequences task response JAX-RS object: " + response);
			System.out.println(" >> Create import sequences task HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));

			eventInput = target.path(path.value())
					.path("progress")
					.path(getName(location.getPath()))
					.queryParam("refresh", 1)
					.queryParam("token", TOKEN_ROOT) // token attribute replaces HTTP header to overcome this EventSource limitation
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
				final Progress progress = JSON_MAPPER.readValue(data, Progress.class);
				assertThat("Progress event decoded object is not null", progress, notNullValue());
				assertThat("Import sequences task does not have errors", !progress.isHasErrors());
				/* uncomment for additional output */				
				System.out.println(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: object=" + progress);
			}			

			// test create new sequence
			path = SequenceResource.class.getAnnotation(Path.class);
			final Sequence sequence = Sequence.builder()
					.dataSource(DataSource.GENBANK)
					.definition("Example sequence")
					.accession("LVL00000")
					.version("0.0")
					.organism("Example organism")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.2d, 3.4d).build()).build())
					.build();
			final SequenceKey sequenceKey = SequenceKey.builder()
					.dataSource(sequence.getDataSource())
					.accession(sequence.getAccession())
					.build();			
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(sequence, APPLICATION_JSON_TYPE));			
			assertThat("Create new sequence response is not null", response, notNullValue());
			assertThat("Create new sequence response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new sequence response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new sequence response entity is not null", payload, notNullValue());
			assertThat("Create new sequence response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println(" >> Create new sequence response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new sequence response JAX-RS object: " + response);
			System.out.println(" >> Create new sequence HTTP headers: " + response.getStringHeaders());

			// test get sequences (JSON encoded)
			response = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Get sequences response is not null", response, notNullValue());
			assertThat("Get sequences response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Get sequences response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get sequences response entity is not null", payload, notNullValue());
			assertThat("Get sequences response entity is not empty", isNotBlank(payload));
			/* uncomment for additional output */			
			System.out.println(" >> Get sequences response body (JSON): " + payload);
			System.out.println(" >> Get sequences response JAX-RS object: " + response);
			System.out.println(" >> Get sequences HTTP headers: " + response.getStringHeaders());

			// test get sequences (Java object)
			Sequences sequences = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Get sequences result is not null", sequences, notNullValue());
			assertThat("Get sequences list is not null", sequences.getElements(), notNullValue());
			assertThat("Get sequences list is not empty", !sequences.getElements().isEmpty());
			assertThat("Get sequences items count coincide with list size", sequences.getElements().size(), equalTo(sequences.getTotalCount()));
			/* uncomment for additional output */			
			System.out.println(" >> Get sequences result: " + sequences.toString());

			// test sequence pagination (JSON encoded)
			final int perPage = 2;
			response = target.path(path.value())
					.queryParam("per_page", perPage)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Paginate sequences first page response is not null", response, notNullValue());
			assertThat("Paginate sequences first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Paginate sequences first page response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Paginate sequences first page response entity is not null", payload, notNullValue());
			assertThat("Paginate sequences first page response entity is not empty", isNotBlank(payload));			
			sequences = JSON_MAPPER.readValue(payload, Sequences.class);			
			assertThat("Paginate sequences first page result is not null", sequences, notNullValue());
			assertThat("Paginate sequences first page list is not null", sequences.getElements(), notNullValue());
			assertThat("Paginate sequences first page list is not empty", !sequences.getElements().isEmpty());
			assertThat("Paginate sequences first page items count coincide with page size", sequences.getElements().size(), 
					equalTo(min(perPage, sequences.getTotalCount())));
			/* uncomment for additional output */			
			System.out.println(" >> Paginate sequences first page response body (JSON): " + payload);

			assertThat("Paginate sequences first page links is not null", sequences.getLinks(), notNullValue());
			assertThat("Paginate sequences first page links is not empty", !sequences.getLinks().isEmpty());
			assertThat("Paginate sequences first page links count coincide with expected", sequences.getLinks().size(), equalTo(2));
			Link lastLink = null;
			for (int i = 0; i < sequences.getLinks().size() && lastLink == null; i++) {
				final Link link = sequences.getLinks().get(i);
				if (LAST.equalsIgnoreCase(link.getRel())) {
					lastLink = link;
				}
			}
			assertThat("Paginate sequences first page link to last page is not null", lastLink, notNullValue());

			response = target.path(getPath(lastLink).substring(SERVICE.length()))
					.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
					.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Paginate sequences last page response is not null", response, notNullValue());
			assertThat("Paginate sequences last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Paginate sequences last page response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Paginate sequences last page response entity is not null", payload, notNullValue());
			assertThat("Paginate sequences last page response entity is not empty", isNotBlank(payload));			
			sequences = JSON_MAPPER.readValue(payload, Sequences.class);			
			assertThat("Paginate sequences last page result is not null", sequences, notNullValue());
			assertThat("Paginate sequences last page list is not null", sequences.getElements(), notNullValue());
			assertThat("Paginate sequences last page list is not empty", !sequences.getElements().isEmpty());
			/* uncomment for additional output */			
			System.out.println(" >> Paginate sequences last page response body (JSON): " + payload);

			assertThat("Paginate sequences last page links is not null", sequences.getLinks(), notNullValue());
			assertThat("Paginate sequences last page links is not empty", !sequences.getLinks().isEmpty());
			assertThat("Paginate sequences last page links count coincide with expected", sequences.getLinks().size(), equalTo(2));

			// test get sequences pagination (Java object)
			sequences = target.path(path.value())
					.queryParam("per_page", perPage)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Paginate sequences first page result is not null", sequences, notNullValue());
			assertThat("Paginate sequences first page list is not null", sequences.getElements(), notNullValue());
			assertThat("Paginate sequences first page list is not empty", !sequences.getElements().isEmpty());
			assertThat("Paginate sequences first page items count coincide with list size", sequences.getElements().size(), 
					equalTo(min(perPage, sequences.getTotalCount())));
			/* uncomment for additional output */			
			System.out.println(" >> Paginate sequences first page result: " + sequences.toString());

			assertThat("Paginate sequences first page links is not null", sequences.getLinks(), notNullValue());
			assertThat("Paginate sequences first page links is not empty", !sequences.getLinks().isEmpty());
			assertThat("Paginate sequences first page links count coincide with expected", sequences.getLinks().size(), equalTo(2));
			lastLink = null;
			for (int i = 0; i < sequences.getLinks().size() && lastLink == null; i++) {
				final Link link = sequences.getLinks().get(i);
				if (LAST.equalsIgnoreCase(link.getRel())) {
					lastLink = link;
				}
			}
			assertThat("Paginate sequences first page link to last page is not null", lastLink, notNullValue());

			sequences = target.path(getPath(lastLink).substring(SERVICE.length()))
					.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
					.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Paginate sequences last page result is not null", sequences, notNullValue());
			assertThat("Paginate sequences last page list is not null", sequences.getElements(), notNullValue());
			assertThat("Paginate sequences last page list is not empty", !sequences.getElements().isEmpty());
			/* uncomment for additional output */			
			System.out.println(" >> Paginate sequences last page result: " + sequences.toString());

			assertThat("Paginate sequences last page links is not null", sequences.getLinks(), notNullValue());
			assertThat("Paginate sequences last page links is not empty", !sequences.getLinks().isEmpty());
			assertThat("Paginate sequences last page links count coincide with expected", sequences.getLinks().size(), equalTo(2));

			// test get sequences applying a full-text search filter
			sequences = target.path(path.value())
					.queryParam("q", "papatasi")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Search sequences result is not null", sequences, notNullValue());
			assertThat("Search sequences list is not null", sequences.getElements(), notNullValue());
			assertThat("Search sequences list is not empty", !sequences.getElements().isEmpty());
			assertThat("Search sequences items count coincide with list size", sequences.getElements().size(), equalTo(sequences.getTotalCount()));
			assertThat("Search sequences coincides result with expected", sequences.getElements().size(), equalTo(3));
			/* uncomment for additional output */			
			System.out.println(" >> Search sequences result: " + sequences.toString());

			// test get sequences applying a keyword matching filter
			sequences = target.path(path.value())
					.queryParam("q", "accession:JP553239")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Search sequences result is not null", sequences, notNullValue());
			assertThat("Search sequences list is not null", sequences.getElements(), notNullValue());
			assertThat("Search sequences list is not empty", !sequences.getElements().isEmpty());
			assertThat("Search sequences items count coincide with list size", sequences.getElements().size(), equalTo(sequences.getTotalCount()));
			assertThat("Search sequences coincides result with expected", sequences.getElements().size(), equalTo(1));
			/* uncomment for additional output */			
			System.out.println(" >> Search sequences result: " + sequences.toString());

			// test get sequences applying a full-text search combined with a keyword matching filter
			sequences = target.path(path.value())
					.queryParam("q", "source:GenBank Phlebotomus")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Search sequences result is not null", sequences, notNullValue());
			assertThat("Search sequences list is not null", sequences.getElements(), notNullValue());
			assertThat("Search sequences list is not empty", !sequences.getElements().isEmpty());
			assertThat("Search sequences items count coincide with list size", sequences.getElements().size(), equalTo(sequences.getTotalCount()));
			assertThat("Search sequences coincides result with expected", sequences.getElements().size(), equalTo(4));
			/* uncomment for additional output */			
			System.out.println(" >> Search sequences result: " + sequences.toString());

			// test get sequences sorted by accession number
			sequences = target.path(path.value())
					.queryParam("sort", "accession")
					.queryParam("order", "asc")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Sorted sequences result is not null", sequences, notNullValue());
			assertThat("Sorted sequences list is not null", sequences.getElements(), notNullValue());
			assertThat("Sorted sequences list is not empty", !sequences.getElements().isEmpty());
			assertThat("Sorted sequences items count coincide with list size", sequences.getElements().size(), equalTo(sequences.getTotalCount()));
			String last = "-1";
			for (final Sequence seq : sequences.getElements()) {
				assertThat("Sequences are properly sorted", seq.getAccession().compareTo(last) > 0);
				last = seq.getAccession();
			}
			/* uncomment for additional output */			
			System.out.println(" >> Sorted sequences result: " + sequences.toString());

			// test get sequence by data source + accession number
			Sequence sequence2 = target.path(path.value()).path(sequenceKey.toId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequence.class);
			assertThat("Get sequence by accession number result is not null", sequence2, notNullValue());
			assertThat("Get sequence by accession number coincides with expected", sequence2.equalsIgnoringVolatile(sequence));
			/* uncomment for additional output */
			System.out.println(" >> Get sequence by accession number result: " + sequence2.toString());

			// test update sequence
			sequence.setDefinition("Modified example sequence");
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.put(entity(sequence, APPLICATION_JSON_TYPE));
			assertThat("Update sequence response is not null", response, notNullValue());
			assertThat("Update sequence response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update sequence response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update sequence response entity is not null", payload, notNullValue());
			assertThat("Update sequence response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println(" >> Update sequence response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update sequence response JAX-RS object: " + response);
			System.out.println(" >> Update sequence HTTP headers: " + response.getStringHeaders());

			// test get sequence by accession number after update
			sequence2 = target.path(path.value()).path(sequenceKey.toId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequence.class);
			assertThat("Get sequence by accession number after update result is not null", sequence2, notNullValue());
			assertThat("Get sequence by accession number after update coincides with expected", sequence2.equalsIgnoringVolatile(sequence));
			/* uncomment for additional output */
			System.out.println(" >> Get sequence by accession number after update result: " + sequence2.toString());			

			// test find sequences near to a location
			FeatureCollection featCol = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(FeatureCollection.class);
			assertThat("Get nearby sequences result is not null", featCol, notNullValue());
			assertThat("Get nearby sequences list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby sequences list is not empty", featCol.getFeatures().size() > 0);
			/* uncomment for additional output */			
			System.out.println(" >> Get nearby sequences result: " + featCol.toString());

			// test find sequences near to a location (using plain REST, no Jersey client)
			final URI uri = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d).getUri();
			final String response2 = Request.Get(uri)
					.addHeader("Accept", "application/json")
					.addHeader(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.execute()
					.returnContent()
					.asString();
			assertThat("Get nearby sequences result (plain) is not null", response2, notNullValue());
			assertThat("Get nearby sequences result (plain) is not empty", isNotBlank(response2));
			/* uncomment for additional output */
			System.out.println(" >> Get nearby sequences result (plain): " + response2);
			featCol = JSON_MAPPER.readValue(response2, FeatureCollection.class);
			assertThat("Get nearby sequences result (plain) is not null", featCol, notNullValue());
			assertThat("Get nearby sequences (plain) list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby sequences (plain) list is not empty", featCol.getFeatures().size() > 0);
			/* uncomment for additional output */
			System.out.println(" >> Get nearby sequences result (plain): " + featCol.toString());

			// test delete sequence
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.delete();
			assertThat("Delete sequence response is not null", response, notNullValue());
			assertThat("Delete sequence response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete sequence response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete sequence response entity is not null", payload, notNullValue());
			assertThat("Delete sequence response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println(" >> Delete sequence response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete sequence response JAX-RS object: " + response);
			System.out.println(" >> Delete sequence HTTP headers: " + response.getStringHeaders());

			// test create public link (GZIP compressed FASTA sequence)
			path = PublicLinkResource.class.getAnnotation(Path.class);
			PublicLink publicLink = PublicLink.builder()
					.target(Target.builder().type("sequence").id("gb:JP540074").filter("export_fasta").compression("gzip").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (FASTA.GZIP sequence) response is not null", response, notNullValue());
			assertThat("Create public link (FASTA.GZIP sequence) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (FASTA.GZIP sequence) is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (FASTA.GZIP sequence) response entity is not null", payload, notNullValue());
			assertThat("Create public link (FASTA.GZIP sequence) response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println(" >> Create public link (FASTA.GZIP sequence) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (FASTA.GZIP sequence) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (FASTA.GZIP sequence) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));
			publicLink.setPath(getName(getPathNoEndSeparator(location.getPath())) + "/" + getName(location.getPath()));
			publicLink.setMime("application/gzip");
			// TODO : add link to PUBLIC_LINKS

			// test get public links
			final PublicLinks publicLinks = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(PublicLinks.class);
			assertThat("Get public links result is not null", publicLinks, notNullValue());
			assertThat("Get public links list is not null", publicLinks.getElements(), notNullValue());
			assertThat("Get public links list is not empty", !publicLinks.getElements().isEmpty());
			assertThat("Get public links items count coincide with list size", publicLinks.getElements().size(), equalTo(publicLinks.getTotalCount()));
			/* uncomment for additional output */			
			System.out.println(" >> Get public links result: " + publicLinks.toString());

			// test get public link
			final PublicLink publicLink2 = target.path(path.value()).path(publicLinks.getElements().get(0).getPath())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(PublicLink.class);
			assertThat("Get public link result is not null", publicLink2, notNullValue());
			assertThat("Get public link coincides with expected", publicLink2.equalsIgnoringVolatile(publicLink), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Get public link result: " + publicLink2.toString());

			// test update public link
			publicLink.setDescription("Different description");
			response = target.path(path.value()).path(publicLinks.getElements().get(0).getPath())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.put(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Update public link response is not null", response, notNullValue());
			assertThat("Update public link response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update public link response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update public link response entity is not null", payload, notNullValue());
			assertThat("Update public link response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println(" >> Update public link response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update public link response JAX-RS object: " + response);
			System.out.println(" >> Update public link HTTP headers: " + response.getStringHeaders());

			// test delete public link
			response = target.path(path.value()).path(publicLinks.getElements().get(0).getPath())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.delete();
			assertThat("Delete public link response is not null", response, notNullValue());
			assertThat("Delete public link response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete public link response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete public link response entity is not null", payload, notNullValue());
			assertThat("Delete public link response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println(" >> Delete public link response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete public link response JAX-RS object: " + response);
			System.out.println(" >> Delete public link HTTP headers: " + response.getStringHeaders());

			// test create public link (GZIP compressed NCBI sequence)
			publicLink = PublicLink.builder()
					.target(Target.builder().type("sequence").id("gb:JP540074").filter("export").compression("gzip").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (NCBI.GZIP sequence) response is not null", response, notNullValue());
			assertThat("Create public link (NCBI.GZIP sequence) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (NCBI.GZIP sequence) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (NCBI.GZIP sequence) response entity is not null", payload, notNullValue());
			assertThat("Create public link (NCBI.GZIP sequence) response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println(" >> Create public link (NCBI.GZIP sequence) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (NCBI.GZIP sequence) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (NCBI.GZIP sequence) HTTP headers: " + response.getStringHeaders());
			// TODO : add link to PUBLIC_LINKS

			// test create public link (uncompressed FASTA sequence)
			publicLink = PublicLink.builder()
					.target(Target.builder().type("sequence").id("gb:JP540074").filter("export_fasta").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (FASTA sequence) response is not null", response, notNullValue());
			assertThat("Create public link (FASTA sequence) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (FASTA sequence) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (FASTA sequence) response entity is not null", payload, notNullValue());
			assertThat("Create public link (FASTA sequence) response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println(" >> Create public link (FASTA sequence) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (FASTA sequence) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (FASTA sequence) HTTP headers: " + response.getStringHeaders());
			// TODO : add link to PUBLIC_LINKS

			// test create public link (uncompressed NCBI sequence)
			publicLink = PublicLink.builder()
					.target(Target.builder().type("sequence").id("gb:JP540074").filter("export").compression("none").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (NCBI sequence) response is not null", response, notNullValue());
			assertThat("Create public link (NCBI sequence) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (NCBI sequence) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (NCBI sequence) response entity is not null", payload, notNullValue());
			assertThat("Create public link (NCBI sequence) response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println(" >> Create public link (NCBI sequence) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (NCBI sequence) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (NCBI sequence) HTTP headers: " + response.getStringHeaders());
			// TODO : add link to PUBLIC_LINKS

			// test create public link (GZIP compressed NCBI bulk of sequences)
			publicLink = PublicLink.builder()
					.target(Target.builder().type("sequence").ids(newArrayList("gb:JP540074", "gb:JP553239")).filter("export").compression("gzip").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (NCBI.GZIP sequences bulk) response is not null", response, notNullValue());
			assertThat("Create public link (NCBI.GZIP sequences bulk) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (NCBI.GZIP sequences bulk) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (NCBI.GZIP sequences bulk) response entity is not null", payload, notNullValue());
			assertThat("Create public link (NCBI.GZIP sequences bulk) response entity is empty", isBlank(payload));
			/* uncomment for additional output */
			System.out.println(" >> Create public link (NCBI.GZIP sequences bulk) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (NCBI.GZIP sequences bulk) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (NCBI.GZIP sequence) HTTP headers: " + response.getStringHeaders());
			// TODO : add link to PUBLIC_LINKS

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ServiceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ServiceTest.test() has finished");
		}
	}

}