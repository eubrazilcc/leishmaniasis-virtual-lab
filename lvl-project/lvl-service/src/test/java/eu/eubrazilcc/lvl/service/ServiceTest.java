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
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.Dataset.DATASET_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.SequenceCollection.SANDFLY_COLLECTION;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.getSequence;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.decodePublicLinkPath;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.service.Task.TaskType.IMPORT_SANDFLY_SEQ;
import static eu.eubrazilcc.lvl.service.io.PublicLinkWriter.unsetPublicLink;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.USER_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.userPermissions;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Dataset.DatasetMetadata;
import eu.eubrazilcc.lvl.core.PublicLinkOLD;
import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.Target;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.rest.CitationResource;
import eu.eubrazilcc.lvl.service.rest.CitationResource.References;
import eu.eubrazilcc.lvl.service.rest.DatasetResource;
import eu.eubrazilcc.lvl.service.rest.PublicLinkResource;
import eu.eubrazilcc.lvl.service.rest.TaskResource;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Tests REST Web service.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ServiceTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			ServiceTest.class.getSimpleName() + "_" + random(8, true, true)));

	private static final String HOST = "https://localhost:8443";
	private static final String SERVICE = "/lvl-service/rest/v1";
	private static final String BASE_URI = HOST + SERVICE;

	private WebTarget target;
	private static final String TOKEN_ROOT  = "1234567890abcdEFGhiJKlMnOpqrstUVWxyZ";
	private static final String TOKEN_USER  = "0987654321zYXwvuTSRQPoNmLkjIHgfeDCBA";
	private static final String TOKEN_USER2 = "zYXwvuTSRQPoNmLkjIHgfeDCBA1234567890";	
	private String ownerId1;
	private String ownerId2;

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
		TEST_OUTPUT_DIR.mkdirs();
		// prepare client
		final Client client = ClientBuilder.newBuilder()
				.register(JacksonFeature.class)
				.register(SseFeature.class)
				.build();
		// configure Web target
		target = client.target(BASE_URI);
		target.property(ClientProperties.FOLLOW_REDIRECTS, true);
		// insert valid users in the database (they are needed for properly authentication/authorization)
		ownerId1 = toResourceOwnerId("user1");
		ownerId2 = toResourceOwnerId("user2");
		final User user1 = User.builder()
				.userid("user1")
				.password("password1")
				.email("user1@example.com")
				.fullname("User 1")
				.role(USER_ROLE)
				.permissions(asPermissionList(userPermissions(ownerId1)))
				.build(),
				user2 = User.builder()
				.userid("user2")
				.password("password2")
				.email("user2@example.com")
				.fullname("User 2")
				.role(USER_ROLE)
				.permissions(asPermissionList(userPermissions(ownerId2)))
				.build();
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder()
				.user(user1).build());
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder()
				.user(user2).build());
		// insert valid tokens in the database
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_ROOT)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId(toResourceOwnerId("root"))
				.scope(asPermissionList(allPermissions()))
				.build());
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_USER)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId(ownerId1)
				.scope(user1.getPermissions())
				.build());
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_USER2)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId(ownerId2)
				.scope(user2.getPermissions())
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
			Path path = null;
			Response response = null;
			String payload, response2 = null;
			URI location = null, uri = null;
			FeatureCollection featCol = null;

			// test import sandflies task
			path = TaskResource.class.getAnnotation(Path.class);
			Task task = Task.builder()
					.type(IMPORT_SANDFLY_SEQ)
					.ids(newArrayList("353470160", "353483325", "384562886"))
					.build();

			response = target.path(path.value())					
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(task, APPLICATION_JSON_TYPE));
			assertThat("Create import sandflies task response is not null", response, notNullValue());
			assertThat("Create import sandflies task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create import sandflies task response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create import sandflies task response entity is not null", payload, notNullValue());
			assertThat("Create import sandflies task response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create import sandflies task response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create import sandflies task response JAX-RS object: " + response);
			System.out.println(" >> Create import sandflies task HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));

			// test import sandflies task progress
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
				assertThat("Import sandflies task does not have errors", !progress.isHasErrors());
				// uncomment for additional output				
				System.out.println(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: id=" 
						+ id + "; name=" + name + "; data=" + data + "; object=" + progress);
			}

			// repeat the import new sandflies task to test how the subscription is made from a client using the JavaScript EventSource interface
			task = Task.builder()
					.type(IMPORT_SANDFLY_SEQ)
					.ids(newArrayList("430902590"))
					.build();
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(task, APPLICATION_JSON_TYPE));
			assertThat("Create import sandflies task response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create import sandflies task response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			// uncomment for additional output
			System.out.println(" >> Create import sandflies task response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create import sandflies task response JAX-RS object: " + response);
			System.out.println(" >> Create import sandflies task HTTP headers: " + response.getStringHeaders());
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
				assertThat("Import sandflies task does not have errors", !progress.isHasErrors());
				// uncomment for additional output				
				System.out.println(" >> Event [" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S z").format(new Date()) + "]: object=" + progress);
			}

			/* TODO

			// test create new sandfly
			path = SandflySequenceResource.class.getAnnotation(Path.class);
			final Sandfly sandfly = Sandfly.builder()
					.dataSource(DataSource.GENBANK)
					.definition("Example sandfly")
					.accession("LVL00000")
					.version("0.0")
					.organism("Example organism")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.2d, 3.4d).build()).build())
					.build();
			SequenceKey sequenceKey = SequenceKey.builder()
					.dataSource(sandfly.getDataSource())
					.accession(sandfly.getAccession())
					.build();			
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(sandfly, APPLICATION_JSON_TYPE));			
			assertThat("Create new sandfly response is not null", response, notNullValue());
			assertThat("Create new sandfly response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new sandfly response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new sandfly response entity is not null", payload, notNullValue());
			assertThat("Create new sandfly response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Create new sandfly response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new sandfly response JAX-RS object: " + response);
			System.out.println(" >> Create new sandfly HTTP headers: " + response.getStringHeaders());

			// test get sandflies (JSON encoded)
			response = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Get sandflies response is not null", response, notNullValue());
			assertThat("Get sandflies response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Get sandflies response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get sandflies response entity is not null", payload, notNullValue());
			assertThat("Get sandflies response entity is not empty", isNotBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Get sandflies response body (JSON): " + payload);
			System.out.println(" >> Get sandflies response JAX-RS object: " + response);
			System.out.println(" >> Get sandflies HTTP headers: " + response.getStringHeaders());

			// test get sandflies (Java object)
			Sequences sandflies = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Get sandflies result is not null", sandflies, notNullValue());
			assertThat("Get sandflies list is not null", sandflies.getElements(), notNullValue());
			assertThat("Get sandflies list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Get sandflies items count coincide with list size", sandflies.getElements().size(), equalTo(sandflies.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get sandflies result: " + sandflies.toString());

			// test sandfly pagination (JSON encoded)
			final int perPage = 2;
			response = target.path(path.value())
					.queryParam("per_page", perPage)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Paginate sandflies first page response is not null", response, notNullValue());
			assertThat("Paginate sandflies first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Paginate sandflies first page response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Paginate sandflies first page response entity is not null", payload, notNullValue());
			assertThat("Paginate sandflies first page response entity is not empty", isNotBlank(payload));			
			sandflies = JSON_MAPPER.readValue(payload, Sequences.class);			
			assertThat("Paginate sandflies first page result is not null", sandflies, notNullValue());
			assertThat("Paginate sandflies first page list is not null", sandflies.getElements(), notNullValue());
			assertThat("Paginate sandflies first page list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Paginate sandflies first page items count coincide with page size", sandflies.getElements().size(), 
					equalTo(min(perPage, sandflies.getTotalCount())));
			// uncomment for additional output			
			System.out.println(" >> Paginate sandflies first page response body (JSON): " + payload);

			assertThat("Paginate sandflies first page links is not null", sandflies.getLinks(), notNullValue());
			assertThat("Paginate sandflies first page links is not empty", !sandflies.getLinks().isEmpty());
			assertThat("Paginate sandflies first page links count coincide with expected", sandflies.getLinks().size(), equalTo(2));
			Link lastLink = null;
			for (int i = 0; i < sandflies.getLinks().size() && lastLink == null; i++) {
				final Link link = sandflies.getLinks().get(i);
				if (LAST.equalsIgnoreCase(link.getRel())) {
					lastLink = link;
				}
			}
			assertThat("Paginate sandflies first page link to last page is not null", lastLink, notNullValue());

			response = target.path(getPath(lastLink).substring(SERVICE.length()))
					.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
					.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Paginate sandflies last page response is not null", response, notNullValue());
			assertThat("Paginate sandflies last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Paginate sandflies last page response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Paginate sandflies last page response entity is not null", payload, notNullValue());
			assertThat("Paginate sandflies last page response entity is not empty", isNotBlank(payload));			
			sandflies = JSON_MAPPER.readValue(payload, Sequences.class);			
			assertThat("Paginate sandflies last page result is not null", sandflies, notNullValue());
			assertThat("Paginate sandflies last page list is not null", sandflies.getElements(), notNullValue());
			assertThat("Paginate sandflies last page list is not empty", !sandflies.getElements().isEmpty());
			// uncomment for additional output			
			System.out.println(" >> Paginate sandflies last page response body (JSON): " + payload);

			assertThat("Paginate sandflies last page links is not null", sandflies.getLinks(), notNullValue());
			assertThat("Paginate sandflies last page links is not empty", !sandflies.getLinks().isEmpty());
			assertThat("Paginate sandflies last page links count coincide with expected", sandflies.getLinks().size(), equalTo(2));

			// test get sandflies pagination (Java object)
			sandflies = target.path(path.value())
					.queryParam("per_page", perPage)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Paginate sandflies first page result is not null", sandflies, notNullValue());
			assertThat("Paginate sandflies first page list is not null", sandflies.getElements(), notNullValue());
			assertThat("Paginate sandflies first page list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Paginate sandflies first page items count coincide with list size", sandflies.getElements().size(), 
					equalTo(min(perPage, sandflies.getTotalCount())));
			// uncomment for additional output			
			System.out.println(" >> Paginate sandflies first page result: " + sandflies.toString());

			assertThat("Paginate sandflies first page links is not null", sandflies.getLinks(), notNullValue());
			assertThat("Paginate sandflies first page links is not empty", !sandflies.getLinks().isEmpty());
			assertThat("Paginate sandflies first page links count coincide with expected", sandflies.getLinks().size(), equalTo(2));
			lastLink = null;
			for (int i = 0; i < sandflies.getLinks().size() && lastLink == null; i++) {
				final Link link = sandflies.getLinks().get(i);
				if (LAST.equalsIgnoreCase(link.getRel())) {
					lastLink = link;
				}
			}
			assertThat("Paginate sandflies first page link to last page is not null", lastLink, notNullValue());

			sandflies = target.path(getPath(lastLink).substring(SERVICE.length()))
					.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
					.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Paginate sandflies last page result is not null", sandflies, notNullValue());
			assertThat("Paginate sandflies last page list is not null", sandflies.getElements(), notNullValue());
			assertThat("Paginate sandflies last page list is not empty", !sandflies.getElements().isEmpty());
			// uncomment for additional output			
			System.out.println(" >> Paginate sandflies last page result: " + sandflies.toString());

			assertThat("Paginate sandflies last page links is not null", sandflies.getLinks(), notNullValue());
			assertThat("Paginate sandflies last page links is not empty", !sandflies.getLinks().isEmpty());
			assertThat("Paginate sandflies last page links count coincide with expected", sandflies.getLinks().size(), equalTo(2));

			// test get sandflies applying a full-text search filter
			sandflies = target.path(path.value())
					.queryParam("q", "papatasi")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Search sandflies result is not null", sandflies, notNullValue());
			assertThat("Search sandflies list is not null", sandflies.getElements(), notNullValue());
			assertThat("Search sandflies list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Search sandflies items count coincide with list size", sandflies.getElements().size(), equalTo(sandflies.getTotalCount()));
			assertThat("Search sandflies coincides result with expected", sandflies.getElements().size(), equalTo(3));
			// uncomment for additional output			
			System.out.println(" >> Search sandflies result: " + sandflies.toString());

			// test get sandflies applying a keyword matching filter
			sandflies = target.path(path.value())
					.queryParam("q", "accession:JP553239")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Search sandflies result is not null", sandflies, notNullValue());
			assertThat("Search sandflies list is not null", sandflies.getElements(), notNullValue());
			assertThat("Search sandflies list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Search sandflies items count coincide with list size", sandflies.getElements().size(), equalTo(sandflies.getTotalCount()));
			assertThat("Search sandflies coincides result with expected", sandflies.getElements().size(), equalTo(1));
			// uncomment for additional output			
			System.out.println(" >> Search sandflies result: " + sandflies.toString());

			// test get sandflies applying a full-text search combined with a keyword matching filter
			sandflies = target.path(path.value())
					.queryParam("q", "source:GenBank Phlebotomus")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Search sandflies result is not null", sandflies, notNullValue());
			assertThat("Search sandflies list is not null", sandflies.getElements(), notNullValue());
			assertThat("Search sandflies list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Search sandflies items count coincide with list size", sandflies.getElements().size(), equalTo(sandflies.getTotalCount()));
			assertThat("Search sandflies coincides result with expected", sandflies.getElements().size(), equalTo(4));
			// uncomment for additional output			
			System.out.println(" >> Search sandflies result: " + sandflies.toString());

			// test get sandflies sorted by accession number
			sandflies = target.path(path.value())
					.queryParam("sort", "accession")
					.queryParam("order", "asc")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sequences.class);
			assertThat("Sorted sandflies result is not null", sandflies, notNullValue());
			assertThat("Sorted sandflies list is not null", sandflies.getElements(), notNullValue());
			assertThat("Sorted sandflies list is not empty", !sandflies.getElements().isEmpty());
			assertThat("Sorted sandflies items count coincide with list size", sandflies.getElements().size(), equalTo(sandflies.getTotalCount()));
			String last = "-1";
			for (final Sandfly seq : sandflies.getElements()) {
				assertThat("Sandflies are properly sorted", seq.getAccession().compareTo(last) > 0);
				last = seq.getAccession();
			}
			// uncomment for additional output			
			System.out.println(" >> Sorted sandflies result: " + sandflies.toString());

			// test get sandfly by data source + accession number
			Sandfly sandfly2 = target.path(path.value()).path(sequenceKey.toId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sandfly.class);
			assertThat("Get sandfly by accession number result is not null", sandfly2, notNullValue());
			assertThat("Get sandfly by accession number coincides with expected", sandfly2.equalsIgnoringVolatile(sandfly));
			// uncomment for additional output
			System.out.println(" >> Get sandfly by accession number result: " + sandfly2.toString());

			// test update sandfly
			sandfly.setDefinition("Modified example sandfly");
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.put(entity(sandfly, APPLICATION_JSON_TYPE));
			assertThat("Update sandfly response is not null", response, notNullValue());
			assertThat("Update sandfly response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update sandfly response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update sandfly response entity is not null", payload, notNullValue());
			assertThat("Update sandfly response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Update sandfly response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update sandfly response JAX-RS object: " + response);
			System.out.println(" >> Update sandfly HTTP headers: " + response.getStringHeaders());

			// test get sandfly by accession number after update
			sandfly2 = target.path(path.value()).path(sequenceKey.toId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Sandfly.class);
			assertThat("Get sandfly by accession number after update result is not null", sandfly2, notNullValue());
			assertThat("Get sandfly by accession number after update coincides with expected", sandfly2.equalsIgnoringVolatile(sandfly));
			// uncomment for additional output
			System.out.println(" >> Get sandfly by accession number after update result: " + sandfly2.toString());			

			// test find sandflies near to a location
			featCol = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(FeatureCollection.class);
			assertThat("Get nearby sandflies result is not null", featCol, notNullValue());
			assertThat("Get nearby sandflies list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby sandflies list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output			
			System.out.println(" >> Get nearby sandflies result: " + featCol.toString());

			// test find sandflies near to a location (using plain REST, no Jersey client)
			uri = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d).getUri();
			response2 = Request.Get(uri)
					.addHeader("Accept", "application/json")
					.addHeader(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.execute()
					.returnContent()
					.asString();
			assertThat("Get nearby sandflies result (plain) is not null", response2, notNullValue());
			assertThat("Get nearby sandflies result (plain) is not empty", isNotBlank(response2));
			// uncomment for additional output
			System.out.println(" >> Get nearby sandflies result (plain): " + response2);
			featCol = JSON_MAPPER.readValue(response2, FeatureCollection.class);
			assertThat("Get nearby sandflies result (plain) is not null", featCol, notNullValue());
			assertThat("Get nearby sandflies (plain) list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby sandflies (plain) list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output
			System.out.println(" >> Get nearby sandflies result (plain): " + featCol.toString());

			// test find sandflies near to a location (using plain REST, no Jersey client, and query style authz token)
			uri = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d)
					.queryParam(AUTHORIZATION_QUERY_OAUTH2, TOKEN_ROOT)
					.getUri();
			response2 = Request.Get(uri)
					.addHeader("Accept", "application/json")
					.execute()
					.returnContent()
					.asString();
			assertThat("Get nearby sandflies result (plain + query token) is not null", response2, notNullValue());
			assertThat("Get nearby sandflies result (plain + query token) is not empty", isNotBlank(response2));
			// uncomment for additional output
			System.out.println(" >> Get nearby sandflies result (plain + query token): " + response2);
			featCol = JSON_MAPPER.readValue(response2, FeatureCollection.class);
			assertThat("Get nearby sandflies result (plain + query token) is not null", featCol, notNullValue());
			assertThat("Get nearby sandflies (plain + query token) list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby sandflies (plain + query token) list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output
			System.out.println(" >> Get nearby sandflies result (plain + query token): " + featCol.toString());

			// test delete sandfly
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.delete();
			assertThat("Delete sandfly response is not null", response, notNullValue());
			assertThat("Delete sandfly response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete sandfly response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete sandfly response entity is not null", payload, notNullValue());
			assertThat("Delete sandfly response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete sandfly response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete sandfly response JAX-RS object: " + response);
			System.out.println(" >> Delete sandfly HTTP headers: " + response.getStringHeaders());

			// test create new leishmania
			path = LeishmaniaSequenceResource.class.getAnnotation(Path.class);
			final Leishmania leishmania = Leishmania.builder()
					.dataSource(DataSource.GENBANK)
					.definition("Example leishmania")
					.accession("LVL00000")
					.version("0.0")
					.organism("Example organism")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.2d, 3.4d).build()).build())
					.build();
			sequenceKey = SequenceKey.builder()
					.dataSource(leishmania.getDataSource())
					.accession(leishmania.getAccession())
					.build();			
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(leishmania, APPLICATION_JSON_TYPE));			
			assertThat("Create new leishmania response is not null", response, notNullValue());
			assertThat("Create new leishmania response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new leishmania response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new leishmania response entity is not null", payload, notNullValue());
			assertThat("Create new leishmania response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Create new leishmania response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new leishmania response JAX-RS object: " + response);
			System.out.println(" >> Create new leishmania HTTP headers: " + response.getStringHeaders());

			// test get leishmania (JSON encoded)
			response = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Get leishmania response is not null", response, notNullValue());
			assertThat("Get leishmania response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Get leishmania response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get leishmania response entity is not null", payload, notNullValue());
			assertThat("Get leishmania response entity is not empty", isNotBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Get leishmania response body (JSON): " + payload);
			System.out.println(" >> Get leishmania response JAX-RS object: " + response);
			System.out.println(" >> Get leishmania HTTP headers: " + response.getStringHeaders());

			// test leishmania pagination (JSON encoded)
			response = target.path(path.value())
					.queryParam("per_page", perPage)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Paginate leishmania first page response is not null", response, notNullValue());
			assertThat("Paginate leishmania first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Paginate leishmania first page response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Paginate leishmania first page response entity is not null", payload, notNullValue());
			assertThat("Paginate leishmania first page response entity is not empty", isNotBlank(payload));			
			final LeishmaniaSequenceResource.Sequences leishmanias = JSON_MAPPER.readValue(payload, LeishmaniaSequenceResource.Sequences.class);			
			assertThat("Paginate leishmania first page result is not null", leishmanias, notNullValue());
			assertThat("Paginate leishmania first page list is not null", leishmanias.getElements(), notNullValue());
			assertThat("Paginate leishmania first page list is not empty", !leishmanias.getElements().isEmpty());
			assertThat("Paginate leishmania first page items count coincide with page size", leishmanias.getElements().size(), 
					equalTo(min(perPage, leishmanias.getTotalCount())));
			// uncomment for additional output			
			System.out.println(" >> Paginate leishmanias first page response body (JSON): " + payload);

			// test update leishmania
			leishmania.setDefinition("Modified example leishmania");
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.put(entity(leishmania, APPLICATION_JSON_TYPE));
			assertThat("Update leishmania response is not null", response, notNullValue());
			assertThat("Update leishmania response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update leishmania response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update leishmania response entity is not null", payload, notNullValue());
			assertThat("Update leishmania response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Update leishmania response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update leishmania response JAX-RS object: " + response);
			System.out.println(" >> Update leishmania HTTP headers: " + response.getStringHeaders());

			// test get leishmania by accession number after update
			final Leishmania leishmania2 = target.path(path.value()).path(sequenceKey.toId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Leishmania.class);
			assertThat("Get leishmania by accession number after update result is not null", leishmania2, notNullValue());
			assertThat("Get leishmania by accession number after update coincides with expected", leishmania2.equalsIgnoringVolatile(leishmania));
			// uncomment for additional output
			System.out.println(" >> Get leishmania by accession number after update result: " + leishmania2.toString());			

			// test find leishmania near to a location (using plain REST, no Jersey client)
			uri = target.path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
					.queryParam("maxDistance", 4000.0d).getUri();
			response2 = Request.Get(uri)
					.addHeader("Accept", "application/json")
					.addHeader(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.execute()
					.returnContent()
					.asString();
			assertThat("Get nearby leishmania result (plain) is not null", response2, notNullValue());
			assertThat("Get nearby leishmania result (plain) is not empty", isNotBlank(response2));
			// uncomment for additional output
			System.out.println(" >> Get nearby leishmania result (plain): " + response2);
			featCol = JSON_MAPPER.readValue(response2, FeatureCollection.class);
			assertThat("Get nearby leishmania result (plain) is not null", featCol, notNullValue());
			assertThat("Get nearby leishmania (plain) list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby leishmania (plain) list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output
			System.out.println(" >> Get nearby leishmania result (plain): " + featCol.toString());

			// test delete leishmania
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.delete();
			assertThat("Delete leishmania response is not null", response, notNullValue());
			assertThat("Delete leishmania response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete leishmania response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete leishmania response entity is not null", payload, notNullValue());
			assertThat("Delete leishmania response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete leishmania response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete leishmania response JAX-RS object: " + response);
			System.out.println(" >> Delete leishmania HTTP headers: " + response.getStringHeaders()); */

			// TODO
			// test create dataset (GZIP compressed FASTA sandfly)
			path = DatasetResource.class.getAnnotation(Path.class);
			Target datasetTarget = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.id("gb:JP540074")
					.filter("export_fasta")
					.compression("gzip").build();
			DatasetMetadata datasetMetadata = DatasetMetadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			Dataset dataset = Dataset.builder()
					.filename("my_fasta_sequences.zip")
					.metadata(datasetMetadata)
					.build();			
			response = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(dataset, APPLICATION_JSON_TYPE));
			assertThat("Create dataset (FASTA.GZIP sandfly) response is not null", response, notNullValue());
			assertThat("Create dataset (FASTA.GZIP sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset (FASTA.GZIP sandfly) is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset (FASTA.GZIP sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create dataset (FASTA.GZIP sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset (FASTA.GZIP sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset (FASTA.GZIP sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create dataset (FASTA.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));
			assertThat("Create dataset (FASTA.GZIP sandfly) location is not null", location, notNullValue());
			assertThat("Create dataset (FASTA.GZIP sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			// test list datasets (from super-user account)	
			Datasets datasets = target.path(path.value()).path(urlEncodeUtf8(ownerId1)).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Datasets.class);
			assertThat("Get datasets (root account) result is not null", datasets, notNullValue());
			assertThat("Get datasets (root account) list is not null", datasets.getElements(), notNullValue());
			assertThat("Get datasets (root account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
			assertThat("Get datasets (root account) items count coincide with list size", datasets.getElements().size(), 
					equalTo(datasets.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get datasets (root account) result: " + datasets.toString());

			// test list datasets (from user unauthorized user account)
			try {
				datasets = target.path(path.value()).path(urlEncodeUtf8(ownerId1)).request(APPLICATION_JSON)
						.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER2))
						.get(Datasets.class);
				fail("list datasets from user unauthorized user account must produce 401 error");
			} catch (NotAuthorizedException e) {
				// uncomment for additional output			
				System.out.println(" >> Get datasets (unauthorized user account) produced the expected 401 error");
			}			

			// test list datasets (from user account)
			datasets = target.path(path.value()).path(urlEncodeUtf8(ownerId1)).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(Datasets.class);
			assertThat("Get datasets (user account) result is not null", datasets, notNullValue());
			assertThat("Get datasets (user account) list is not null", datasets.getElements(), notNullValue());
			assertThat("Get datasets (user account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
			assertThat("Get datasets (user account) items count coincide with list size", datasets.getElements().size(), 
					equalTo(datasets.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get datasets (user account) result: " + datasets.toString());

			// test list datasets (from user account using default namespace)
			datasets = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS)).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(Datasets.class);
			assertThat("Get datasets (user account) result is not null", datasets, notNullValue());
			assertThat("Get datasets (user account) list is not null", datasets.getElements(), notNullValue());
			assertThat("Get datasets (user account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
			assertThat("Get datasets (user account) items count coincide with list size", datasets.getElements().size(), 
					equalTo(datasets.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get datasets (user account) result: " + datasets.toString());

			// test get dataset
			datasetMetadata = (DatasetMetadata)dataset.getMetadata();
			datasetMetadata.setEditor(ownerId1);
			datasetMetadata.setIsLastestVersion(dataset.getFilename());
			Dataset dataset2 = target.path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
					.path(datasets.getElements().get(0).getUrlSafeFilename())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(Dataset.class);
			assertThat("Get dataset result is not null", dataset2, notNullValue());
			assertThat("Get dataset namespace is not empty", isNotBlank(dataset2.getNamespace()), equalTo(true));
			assertThat("Get dataset namespace coincides with expected", dataset2.getNamespace(), equalTo(ownerId1));			
			assertThat("Get dataset id is not empty", isNotBlank(dataset2.getId()), equalTo(true));
			assertThat("Get dataset length coincides with expected", dataset2.getLength(), greaterThan(0L));
			assertThat("Get dataset chunk size coincides with expected", dataset2.getChunkSize(), greaterThan(0L));
			assertThat("Get dataset creation date is not null", dataset2.getUploadDate(), notNullValue());
			assertThat("Get dataset md5 is not empty", isNotBlank(dataset2.getMd5()), equalTo(true));
			assertThat("Get dataset filename is not empty", isNotBlank(dataset2.getFilename()), equalTo(true));
			assertThat("Get dataset filename coincides with expected", dataset2.getFilename(), equalTo(dataset.getFilename()));
			assertThat("Get dataset content type is not empty", isNotBlank(dataset2.getContentType()), equalTo(true));
			assertThat("Get dataset content type coincides with expected", dataset2.getContentType(), equalTo("application/gzip"));
			assertThat("Get dataset aliases coincides with expected", dataset2.getAliases(), equalTo(dataset.getAliases()));
			assertThat("Get dataset metadata coincides with expected", dataset2.getMetadata(), equalTo(dataset.getMetadata()));
			// uncomment for additional output
			System.out.println(" >> Get dataset result: " + dataset2.toString());

			// test get dataset manually encoding the namespace and filename
			final String datasetId = dataset2.getId();
			dataset2 = target.path(path.value()).path(urlEncodeUtf8(defaultIfBlank(datasets.getElements().get(0).getNamespace(), DATASET_DEFAULT_NS).trim()))
					.path(urlEncodeUtf8(defaultIfBlank(datasets.getElements().get(0).getFilename(), DATASET_DEFAULT_NS).trim()))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(Dataset.class);			
			assertThat("Get dataset (url encoded) result is not null", dataset2, notNullValue());
			assertThat("Get dataset (url encoded) Id coincides with expected", dataset2.getId(), equalTo(datasetId));
			// uncomment for additional output
			System.out.println(" >> Get dataset (url encoded) result: " + dataset2.toString());			

			// test update dataset
			((DatasetMetadata)dataset.getMetadata()).setDescription("Different description");
			response = target.path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
					.path(datasets.getElements().get(0).getUrlSafeFilename())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.put(entity(dataset, APPLICATION_JSON_TYPE));
			assertThat("Update dataset response is not null", response, notNullValue());
			assertThat("Update dataset response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update dataset response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update dataset response entity is not null", payload, notNullValue());
			assertThat("Update dataset response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Update dataset response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update dataset response JAX-RS object: " + response);
			System.out.println(" >> Update dataset HTTP headers: " + response.getStringHeaders());

			// test delete dataset
			response = target.path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
					.path(datasets.getElements().get(0).getUrlSafeFilename())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.delete();
			assertThat("Delete dataset response is not null", response, notNullValue());
			assertThat("Delete dataset response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete dataset response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete dataset response entity is not null", payload, notNullValue());
			assertThat("Delete dataset response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete dataset response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete dataset response JAX-RS object: " + response);
			System.out.println(" >> Delete dataset HTTP headers: " + response.getStringHeaders());

			// test create dataset (GZIP compressed NCBI sandfly)
			datasetTarget = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.id("gb:JP540074")
					.filter("export")
					.compression("gzip")
					.build();
			datasetMetadata = DatasetMetadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_ncbi_sequences.zip")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(dataset, APPLICATION_JSON_TYPE));
			assertThat("Create dataset (NCBI.GZIP sandfly) response is not null", response, notNullValue());
			assertThat("Create dataset (NCBI.GZIP sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset (NCBI.GZIP sandfly) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset (NCBI.GZIP sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create dataset (NCBI.GZIP sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset (NCBI.GZIP sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset (NCBI.GZIP sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create dataset (NCBI.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			assertThat("Create dataset (NCBI.GZIP sandfly) location is not null", location, notNullValue());
			assertThat("Create dataset (NCBI.GZIP sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			// test create dataset (uncompressed FASTA sandfly)
			datasetTarget = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.id("gb:JP540074")
					.filter("export_fasta")
					.build();
			datasetMetadata = DatasetMetadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_sequence.fasta")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(dataset, APPLICATION_JSON_TYPE));
			assertThat("Create dataset (FASTA sandfly) response is not null", response, notNullValue());
			assertThat("Create dataset (FASTA sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset (FASTA sandfly) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset (FASTA sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create dataset (FASTA sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset (FASTA sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset (FASTA sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create dataset (FASTA sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			assertThat("Create dataset (FASTA sandfly) location is not null", location, notNullValue());
			assertThat("Create dataset (FASTA sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			// test create dataset (uncompressed NCBI sandfly)
			datasetTarget = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.id("gb:JP540074")
					.filter("export")
					.compression("none")
					.build();
			datasetMetadata = DatasetMetadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_sequence.xml")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(dataset, APPLICATION_JSON_TYPE));
			assertThat("Create dataset (NCBI sandfly) response is not null", response, notNullValue());
			assertThat("Create dataset (NCBI sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset (NCBI sandfly) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset (NCBI sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create dataset (NCBI sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset (NCBI sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset (NCBI sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create dataset (NCBI sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			assertThat("Create dataset (NCBI sandfly) location is not null", location, notNullValue());
			assertThat("Create dataset (NCBI sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			// test create dataset (GZIP compressed NCBI bulk of sandflies)
			datasetTarget = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.ids(newHashSet("gb:JP540074", "gb:JP553239"))
					.filter("export")
					.compression("gzip")
					.build();
			datasetMetadata = DatasetMetadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_ncbi_sequences.xml")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(dataset, APPLICATION_JSON_TYPE));			
			assertThat("Create dataset (NCBI.GZIP sandflies bulk) response is not null", response, notNullValue());
			assertThat("Create dataset (NCBI.GZIP sandflies bulk) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset (NCBI.GZIP sandflies bulk) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset (NCBI.GZIP sandflies bulk) response entity is not null", payload, notNullValue());
			assertThat("Create dataset (NCBI.GZIP sandflies bulk) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset (NCBI.GZIP sandflies bulk) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset (NCBI.GZIP sandflies bulk) response JAX-RS object: " + response);
			System.out.println(" >> Create dataset (NCBI.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			assertThat("Create dataset (NCBI.GZIP sandfly) location is not null", location, notNullValue());
			assertThat("Create dataset (NCBI.GZIP sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			// test file download
			final URI downloadUri = target.path(path.value()).path(urlEncodeUtf8(DATASET_DEFAULT_NS))
					.path(urlEncodeUtf8(dataset.getFilename())).path("download").getUri();
			final org.apache.http.client.fluent.Response response3 = Request.Get(downloadUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", APPLICATION_OCTET_STREAM)
					.addHeader(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.execute();
			assertThat("Download dataset response is not null", response3, notNullValue());
			final HttpResponse response4 = response3.returnResponse();
			assertThat("Download dataset HTTP response is not null", response4, notNullValue());
			assertThat("Download dataset status line is not null", response4.getStatusLine(), notNullValue());
			assertThat("Download dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
					equalTo(OK.getStatusCode()));
			final Header[] headers = response4.getAllHeaders();
			assertThat("Download dataset headers is not null", headers, notNullValue());
			assertThat("Download dataset headers is not empty", headers.length, greaterThan(0));
			String filename = null;
			for (int i = 0; i < headers.length && filename == null; i++) {
				if ("content-disposition".equalsIgnoreCase(headers[i].getName())) {
					final HeaderElement[] elements = headers[i].getElements();
					if (elements != null) {
						for (int j = 0; j < elements.length && filename == null; j++) {							
							if ("attachment".equalsIgnoreCase(elements[j].getName())) {
								final NameValuePair pair = elements[j].getParameterByName("filename");
								if (pair != null) {
									filename = pair.getValue();
								}
							}
						}
					}
				}
			}
			assertThat("Download dataset filename is not empty", isNotBlank(filename), equalTo(true));
			final HttpEntity entity = response4.getEntity();
			assertThat("Download dataset entity is not null", entity, notNullValue());
			assertThat("Download dataset content length coincides with expected", entity.getContentLength(), greaterThan(0l));
			final File outfile = new File(TEST_OUTPUT_DIR, filename);
			outfile.createNewFile();
			try (final InputStream inputStream = entity.getContent();
					final FileOutputStream outputStream = new FileOutputStream(outfile)) {
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
			}
			assertThat("Downloaded file exists", outfile.exists(), equalTo(true));
			assertThat("Downloaded file is not empty", outfile.length(), greaterThan(0L));
			final GBSeq sequence = getSequence(outfile);
			assertThat("XML parsed from downloaded file is not null", sequence, notNullValue());
			// uncomment for additional output
			System.out.println(" >> Saved file: " + filename);


			// TODO
			// TODO : test share
			// TODO : test public link operations













			if (true) {
				return;
			}

			// TODO

			// test create public link (GZIP compressed FASTA sandfly)
			path = PublicLinkResource.class.getAnnotation(Path.class);
			PublicLinkOLD publicLink = PublicLinkOLD.builder()
					.target(Target.builder().type("sequence").collection(SANDFLY_COLLECTION).id("gb:JP540074").filter("export_fasta").compression("gzip").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (FASTA.GZIP sandfly) response is not null", response, notNullValue());
			assertThat("Create public link (FASTA.GZIP sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (FASTA.GZIP sandfly) is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (FASTA.GZIP sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create public link (FASTA.GZIP sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create public link (FASTA.GZIP sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (FASTA.GZIP sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (FASTA.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));
			final String publicLinkPath = getPathFromLocation(location);
			publicLink.setPath(publicLinkPath);
			publicLink.setMime("application/gzip");
			addPublicLinkForClean(publicLinkPath);

			// test list public links (from super-user account)
			PublicLinks publicLinks = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(PublicLinks.class);
			assertThat("Get public links (root account) result is not null", publicLinks, notNullValue());
			assertThat("Get public links (root account) list is not null", publicLinks.getElements(), notNullValue());
			assertThat("Get public links (root account) list is not empty", publicLinks.getElements().isEmpty(), equalTo(false));
			assertThat("Get public links (root account) items count coincide with list size", publicLinks.getElements().size(), 
					equalTo(publicLinks.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get public links (root account) result: " + publicLinks.toString());

			// test list public links (from user unauthorized user account)
			publicLinks = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER2))
					.get(PublicLinks.class);
			assertThat("Get public links (unauthorized user account) result is not null", publicLinks, notNullValue());
			assertThat("Get public links (unauthorized user account) list is not null", publicLinks.getElements(), notNullValue());
			assertThat("Get public links (unauthorized user account) list is empty", publicLinks.getElements().isEmpty(), equalTo(true));
			assertThat("Get public links (unauthorized user account) items count coincide with list size", publicLinks.getElements().size(), 
					equalTo(publicLinks.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get public links (unauthorized user account) result: " + publicLinks.toString());

			// test list public links (from user account)
			publicLinks = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(PublicLinks.class);
			assertThat("Get public links (user account) result is not null", publicLinks, notNullValue());
			assertThat("Get public links (user account) list is not null", publicLinks.getElements(), notNullValue());
			assertThat("Get public links (user account) list is not empty", publicLinks.getElements().isEmpty(), equalTo(false));
			assertThat("Get public links (user account) items count coincide with list size", publicLinks.getElements().size(), 
					equalTo(publicLinks.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get public links (user account) result: " + publicLinks.toString());

			// test get public link
			publicLink.setOwner(toResourceOwnerId("user1"));
			PublicLinkOLD publicLink2 = target.path(path.value()).path(publicLinks.getElements().get(0).getUrlSafePath())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(PublicLinkOLD.class);
			assertThat("Get public link result is not null", publicLink2, notNullValue());
			assertThat("Get public link creation time is not null", publicLink2.getCreated(), notNullValue());
			publicLink.setCreated(publicLink2.getCreated());
			assertThat("Get public link coincides with expected", publicLink2.equalsIgnoringVolatile(publicLink), equalTo(true));
			// uncomment for additional output
			System.out.println(" >> Get public link result: " + publicLink2.toString());

			// test get public link using encoded identifier
			final String encodedId = encode(publicLinks.getElements().get(0).getUrlSafePath(), UTF_8.name());			
			publicLink2 = target.path(path.value()).path(encodedId)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.get(PublicLinkOLD.class);
			assertThat("Get public link (url encoded Id) result is not null", publicLink2, notNullValue());
			assertThat("Get public link (url encoded Id) creation time is not null", publicLink2.getCreated(), notNullValue());
			publicLink.setCreated(publicLink2.getCreated());
			assertThat("Get public link (url encoded Id) coincides with expected", publicLink2.equalsIgnoringVolatile(publicLink), equalTo(true));
			// uncomment for additional output
			System.out.println(" >> URL encoded Id: " + encodedId);
			System.out.println(" >> Get public link (url encoded Id) result: " + publicLink2.toString());			

			// test update public link
			publicLink.setDescription("Different description");
			response = target.path(path.value()).path(publicLinks.getElements().get(0).getUrlSafePath())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.put(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Update public link response is not null", response, notNullValue());
			assertThat("Update public link response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update public link response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update public link response entity is not null", payload, notNullValue());
			assertThat("Update public link response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Update public link response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update public link response JAX-RS object: " + response);
			System.out.println(" >> Update public link HTTP headers: " + response.getStringHeaders());

			// test delete public link
			response = target.path(path.value()).path(publicLinks.getElements().get(0).getUrlSafePath())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.delete();
			assertThat("Delete public link response is not null", response, notNullValue());
			assertThat("Delete public link response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete public link response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete public link response entity is not null", payload, notNullValue());
			assertThat("Delete public link response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete public link response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete public link response JAX-RS object: " + response);
			System.out.println(" >> Delete public link HTTP headers: " + response.getStringHeaders());

			// test create public link (GZIP compressed NCBI sandfly)
			publicLink = PublicLinkOLD.builder()
					.target(Target.builder().type("sequence").collection(SANDFLY_COLLECTION).id("gb:JP540074").filter("export").compression("gzip").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (NCBI.GZIP sandfly) response is not null", response, notNullValue());
			assertThat("Create public link (NCBI.GZIP sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (NCBI.GZIP sandfly) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (NCBI.GZIP sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create public link (NCBI.GZIP sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create public link (NCBI.GZIP sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (NCBI.GZIP sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (NCBI.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			addPublicLinkForClean(getPathFromLocation(location));

			// test create public link (uncompressed FASTA sandfly)
			publicLink = PublicLinkOLD.builder()
					.target(Target.builder().type("sequence").collection(SANDFLY_COLLECTION).id("gb:JP540074").filter("export_fasta").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (FASTA sandfly) response is not null", response, notNullValue());
			assertThat("Create public link (FASTA sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (FASTA sandfly) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (FASTA sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create public link (FASTA sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create public link (FASTA sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (FASTA sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (FASTA sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			addPublicLinkForClean(getPathFromLocation(location));

			// test create public link (uncompressed NCBI sandfly)
			publicLink = PublicLinkOLD.builder()
					.target(Target.builder().type("sequence").collection(SANDFLY_COLLECTION).id("gb:JP540074").filter("export").compression("none").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (NCBI sandfly) response is not null", response, notNullValue());
			assertThat("Create public link (NCBI sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (NCBI sandfly) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (NCBI sandfly) response entity is not null", payload, notNullValue());
			assertThat("Create public link (NCBI sandfly) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create public link (NCBI sandfly) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (NCBI sandfly) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (NCBI sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			addPublicLinkForClean(getPathFromLocation(location));

			// test create public link (GZIP compressed NCBI bulk of sandflies)
			publicLink = PublicLinkOLD.builder()
					.target(Target.builder().type("sequence").collection(SANDFLY_COLLECTION).ids(newHashSet("gb:JP540074", "gb:JP553239")).filter("export").compression("gzip").build())
					.description("Optional description")
					.build();

			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER))
					.post(entity(publicLink, APPLICATION_JSON_TYPE));
			assertThat("Create public link (NCBI.GZIP sandflies bulk) response is not null", response, notNullValue());
			assertThat("Create public link (NCBI.GZIP sandflies bulk) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create public link (NCBI.GZIP sandflies bulk) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create public link (NCBI.GZIP sandflies bulk) response entity is not null", payload, notNullValue());
			assertThat("Create public link (NCBI.GZIP sandflies bulk) response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create public link (NCBI.GZIP sandflies bulk) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create public link (NCBI.GZIP sandflies bulk) response JAX-RS object: " + response);
			System.out.println(" >> Create public link (NCBI.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			addPublicLinkForClean(getPathFromLocation(location));

			// test create new reference
			final String pmid = "00000000";

			final Sandfly sandfly3 = Sandfly.builder()
					.dataSource(GENBANK)
					.accession("ABC12345678")
					.gi(123)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build())
					.pmids(newHashSet(pmid)).build();
			SANDFLY_DAO.insert(sandfly3);

			path = CitationResource.class.getAnnotation(Path.class);
			final Reference reference = Reference.builder()
					.title("The best paper in the world")
					.pubmedId(pmid)
					.publicationYear(1984)
					.seqids(newHashSet(sandfly3.getId()))
					.build();					
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(reference, APPLICATION_JSON_TYPE));			
			assertThat("Create new reference response is not null", response, notNullValue());
			assertThat("Create new reference response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new reference response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new reference response entity is not null", payload, notNullValue());
			assertThat("Create new reference response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Create new reference response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new reference response JAX-RS object: " + response);
			System.out.println(" >> Create new reference HTTP headers: " + response.getStringHeaders());			

			// test get reference by PMID (Java object)
			Reference reference2 = target.path(path.value()).path(reference.getPubmedId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Reference.class);
			assertThat("Get reference by PMID result is not null", reference2, notNullValue());
			assertThat("Get reference by PMID coincides with expected", reference2.equalsIgnoringVolatile(reference));
			// uncomment for additional output
			System.out.println(" >> Get reference by PMID result: " + reference2.toString());

			// test list all references (JSON encoded)
			response = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get();
			assertThat("Get references response is not null", response, notNullValue());
			assertThat("Get references response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Get references response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get references response entity is not null", payload, notNullValue());
			assertThat("Get references response entity is not empty", isNotBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Get references response body (JSON): " + payload);
			System.out.println(" >> Get references response JAX-RS object: " + response);
			System.out.println(" >> Get references HTTP headers: " + response.getStringHeaders());			

			// test list all references (Java object)
			References references = target.path(path.value()).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(References.class);
			assertThat("Get references result is not null", references, notNullValue());
			assertThat("Get references list is not null", references.getElements(), notNullValue());
			assertThat("Get references list is not empty", !references.getElements().isEmpty());
			assertThat("Get references items count coincide with list size", references.getElements().size(), equalTo(references.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get references result: " + references.toString());			

			// test find references near to a location (JSON encoded)
			uri = target.path(path.value()).path("nearby").path("-122.90").path("38.08")
					.queryParam("maxDistance", 1000.0d).getUri();
			response2 = Request.Get(uri)
					.addHeader("Accept", "application/json")
					.addHeader(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.execute()
					.returnContent()
					.asString();
			assertThat("Get nearby references result (plain) is not null", response2, notNullValue());
			assertThat("Get nearby references result (plain) is not empty", isNotBlank(response2));
			// uncomment for additional output
			System.out.println(" >> Get nearby references result (plain): " + response2);
			featCol = JSON_MAPPER.readValue(response2, FeatureCollection.class);
			assertThat("Get nearby references result (plain) is not null", featCol, notNullValue());
			assertThat("Get nearby references (plain) list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby references (plain) list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output
			System.out.println(" >> Get nearby references result (plain): " + featCol.toString());			

			// test find references near to a location (Java object)
			featCol = target.path(path.value()).path("nearby").path("-122.90").path("38.08")
					.queryParam("maxDistance", 1000.0d)
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(FeatureCollection.class);
			assertThat("Get nearby references result is not null", featCol, notNullValue());
			assertThat("Get nearby references list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby references list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output			
			System.out.println(" >> Get nearby references result: " + featCol.toString());

			// test update reference
			reference.setTitle("A very good paper");
			response = target.path(path.value()).path(reference.getPubmedId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.put(entity(reference, APPLICATION_JSON_TYPE));
			assertThat("Update reference response is not null", response, notNullValue());
			assertThat("Update reference response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update reference response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update reference response entity is not null", payload, notNullValue());
			assertThat("Update reference response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Update reference response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update reference response JAX-RS object: " + response);
			System.out.println(" >> Update reference HTTP headers: " + response.getStringHeaders());

			// test get reference by PMID after update
			reference2 = target.path(path.value()).path(reference.getPubmedId())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(Reference.class);
			assertThat("Get reference by PMID after update result is not null", reference2, notNullValue());
			assertThat("Get reference by PMID after update coincides with expected", reference2.equalsIgnoringVolatile(reference));
			// uncomment for additional output
			System.out.println(" >> Get reference by PMID after update result: " + reference2.toString());

			// test delete reference
			response = target.path(path.value()).path(reference.getPubmedId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.delete();
			assertThat("Delete reference response is not null", response, notNullValue());
			assertThat("Delete reference response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete reference response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete reference response entity is not null", payload, notNullValue());
			assertThat("Delete reference response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete reference response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete reference response JAX-RS object: " + response);
			System.out.println(" >> Delete reference HTTP headers: " + response.getStringHeaders());

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ServiceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ServiceTest.test() has finished");
		}
	}

	private static String getPathFromLocation(final URI location) {
		return decodePublicLinkPath(getName(location.getPath()));
	}

	private static void addPublicLinkForClean(final String path) {
		PUBLIC_LINKS.add(new File(CONFIG_MANAGER.getSharedDir(), path).getAbsolutePath());
	}

	protected static void printWadl(final WebTarget target) {
		final Response response = target.path("application.wadl")					
				.request()
				.get();
		assertThat("Get WADL response is not null", response, notNullValue());
		assertThat("Get WADL response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get WADL response is not empty", response.getEntity(), notNullValue());
		final String payload = response.readEntity(String.class);
		// uncomment for additional output
		System.out.println(" >> Get WADL response body: " + payload);
	}

}