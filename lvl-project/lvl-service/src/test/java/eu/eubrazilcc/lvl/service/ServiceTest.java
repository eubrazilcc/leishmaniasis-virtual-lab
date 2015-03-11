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
import static eu.eubrazilcc.lvl.core.SequenceCollection.SANDFLY_COLLECTION;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.getSequence;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XML_FACTORY;
import static eu.eubrazilcc.lvl.service.Task.TaskType.IMPORT_SANDFLY_SEQ;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.AUTHORIZATION_QUERY_OAUTH2;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.USER_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.userPermissions;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
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

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
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

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.DatasetOpenAccess;
import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.LvlInstance;
import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.Target;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.rest.CitationResource;
import eu.eubrazilcc.lvl.service.rest.CitationResource.References;
import eu.eubrazilcc.lvl.service.rest.DatasetOpenAccessResource;
import eu.eubrazilcc.lvl.service.rest.DatasetResource;
import eu.eubrazilcc.lvl.service.rest.DatasetShareResource;
import eu.eubrazilcc.lvl.service.rest.LeishmaniaSequenceResource;
import eu.eubrazilcc.lvl.service.rest.LvlInstanceResource;
import eu.eubrazilcc.lvl.service.rest.PublicResource;
import eu.eubrazilcc.lvl.service.rest.SandflySequenceResource;
import eu.eubrazilcc.lvl.service.rest.SandflySequenceResource.Sequences;
import eu.eubrazilcc.lvl.service.rest.TaskResource;
import eu.eubrazilcc.lvl.service.rest.jackson.MapperProvider;
import eu.eubrazilcc.lvl.storage.SequenceKey;
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
	private static final String TOKEN_USER1 = "0987654321zYXwvuTSRQPoNmLkjIHgfeDCBA";
	private static final String TOKEN_USER2 = "zYXwvuTSRQPoNmLkjIHgfeDCBA1234567890";
	private static final String TOKEN_USER3 = "zYXwvuTSRQ567890PoNmLkjIHgfeDCBA1234";
	private String ownerId1;
	private String ownerId2;
	private String ownerId3;

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

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
				.register(MapperProvider.class)
				.register(JacksonFeature.class)
				.register(SseFeature.class)
				.build();
		// configure Web target
		target = client.target(BASE_URI);
		target.property(ClientProperties.FOLLOW_REDIRECTS, true);
		// insert valid users in the database (they are needed for properly authentication/authorization)
		ownerId1 = toResourceOwnerId("user1");
		ownerId2 = toResourceOwnerId("user2");
		ownerId3 = toResourceOwnerId("user3");
		final User user1 = User.builder()
				.userid("user1")
				.password("password1")
				.email("user1@example.com")
				.firstname("User 1")
				.lastname("LVL User")
				.role(USER_ROLE)
				.permissions(asPermissionList(userPermissions(ownerId1)))
				.build(),
				user2 = User.builder()
				.userid("user2")
				.password("password2")
				.email("user2@example.com")
				.firstname("User 2")
				.lastname("LVL User")
				.role(USER_ROLE)
				.permissions(asPermissionList(userPermissions(ownerId2)))
				.build(),
				user3 = User.builder()
				.userid("user3")
				.password("password3")
				.email("user3@example.com")
				.firstname("User 3")
				.lastname("LVL User")
				.role(USER_ROLE)
				.permissions(asPermissionList(userPermissions(ownerId3)))
				.build();
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder()
				.user(user1).build());
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder()
				.user(user2).build());
		RESOURCE_OWNER_DAO.insert(ResourceOwner.builder()
				.user(user3).build());
		// insert valid tokens in the database
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_ROOT)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId(toResourceOwnerId("root"))
				.scope(asPermissionList(allPermissions()))
				.build());
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_USER1)
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
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_USER3)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId(ownerId3)
				.scope(user3.getPermissions())
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
					.post(entity(task, APPLICATION_JSON));
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
					.post(entity(task, APPLICATION_JSON));
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

			// test create new sandfly
			final GBSeq sandflySeq = GBSEQ_XML_FACTORY.createGBSeq()
					.withGBSeqPrimaryAccession("ABC12345678")
					.withGBSeqAccessionVersion("3.0")
					.withGBSeqOtherSeqids(GBSEQ_XML_FACTORY.createGBSeqOtherSeqids().withGBSeqid(GBSEQ_XML_FACTORY.createGBSeqid().withvalue(Integer.toString(Integer.MAX_VALUE))))
					.withGBSeqOrganism("organism")					
					.withGBSeqLength("850");

			path = SandflySequenceResource.class.getAnnotation(Path.class);
			final Sandfly sandfly = Sandfly.builder()
					.dataSource(DataSource.GENBANK)
					.definition("Example sandfly")
					.accession("LVL00000")
					.version("0.0")
					.organism("Example organism")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.2d, 3.4d).build()).build())
					.sequence(sandflySeq)
					.build();
			SequenceKey sequenceKey = SequenceKey.builder()
					.dataSource(sandfly.getDataSource())
					.accession(sandfly.getAccession())
					.build();			
			response = target.path(path.value()).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.post(entity(sandfly, APPLICATION_JSON));			
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

			// test export sandfly 
			final GBSeq gbSeq = target.path(path.value())
					.path(sequenceKey.toId())
					.path("export/gb/xml")
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(GBSeq.class);
			assertThat("Export sandfly result is not null", gbSeq, notNullValue());
			// uncomment for additional output
			System.out.println(" >> Export sandfly result: " + gbSeq.toString());

			// test update sandfly
			sandfly.setDefinition("Modified example sandfly");
			response = target.path(path.value()).path(sequenceKey.toId())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.put(entity(sandfly, APPLICATION_JSON));
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
					.post(entity(leishmania, APPLICATION_JSON));			
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
					.put(entity(leishmania, APPLICATION_JSON));
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
			System.out.println(" >> Delete leishmania HTTP headers: " + response.getStringHeaders());

			// test create dataset (GZIP compressed FASTA sandfly)
			path = DatasetResource.class.getAnnotation(Path.class);
			Target datasetTarget = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.id("gb:JP540074")
					.filter("export_fasta")
					.compression("gzip").build();
			Metadata datasetMetadata = Metadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			Dataset dataset = Dataset.builder()
					.filename("my_fasta_sequences.zip")
					.metadata(datasetMetadata)
					.build();			
			response = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(dataset, APPLICATION_JSON));
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
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(Datasets.class);
			assertThat("Get datasets (user account) result is not null", datasets, notNullValue());
			assertThat("Get datasets (user account) list is not null", datasets.getElements(), notNullValue());
			assertThat("Get datasets (user account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
			assertThat("Get datasets (user account) items count coincide with list size", datasets.getElements().size(), 
					equalTo(datasets.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get datasets (user account) result: " + datasets.toString());

			// test list datasets (from user account using default namespace)
			datasets = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(Datasets.class);
			assertThat("Get datasets (user account) result is not null", datasets, notNullValue());
			assertThat("Get datasets (user account) list is not null", datasets.getElements(), notNullValue());
			assertThat("Get datasets (user account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
			assertThat("Get datasets (user account) items count coincide with list size", datasets.getElements().size(), 
					equalTo(datasets.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get datasets (user account) result: " + datasets.toString());

			// test get dataset
			datasetMetadata = dataset.getMetadata();
			datasetMetadata.setEditor(ownerId1);
			datasetMetadata.setIsLastestVersion(dataset.getFilename());
			datasetMetadata.getTags().add("fasta");
			Dataset dataset2 = target.path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
					.path(datasets.getElements().get(0).getUrlSafeFilename())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
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
			dataset2 = target.path(path.value()).path(urlEncodeUtf8(defaultIfBlank(datasets.getElements().get(0).getNamespace(), LVL_DEFAULT_NS).trim()))
					.path(urlEncodeUtf8(defaultIfBlank(datasets.getElements().get(0).getFilename(), LVL_DEFAULT_NS).trim()))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(Dataset.class);			
			assertThat("Get dataset (url encoded) result is not null", dataset2, notNullValue());
			assertThat("Get dataset (url encoded) Id coincides with expected", dataset2.getId(), equalTo(datasetId));
			// uncomment for additional output
			System.out.println(" >> Get dataset (url encoded) result: " + dataset2.toString());			

			// test update dataset
			dataset.getMetadata().setDescription("Different description");
			response = target.path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
					.path(datasets.getElements().get(0).getUrlSafeFilename())
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.put(entity(dataset, APPLICATION_JSON));
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
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
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
			datasetMetadata = Metadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_ncbi_sequences.zip")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(dataset, APPLICATION_JSON));
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
			datasetMetadata = Metadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_sequence.fasta")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(dataset, APPLICATION_JSON));
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
			datasetMetadata = Metadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_ncbi_sequence.xml")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(dataset, APPLICATION_JSON));
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
			datasetMetadata = Metadata.builder()
					.target(datasetTarget)
					.description("Optional description")
					.build();
			dataset = Dataset.builder()
					.filename("my_ncbi_sequences.zip")
					.metadata(datasetMetadata)
					.build();
			response = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(dataset, APPLICATION_JSON));			
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
			URI downloadUri = target.path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml")).path("download").getUri();
			org.apache.http.client.fluent.Response response3 = Request.Get(downloadUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", APPLICATION_OCTET_STREAM)
					.addHeader(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.execute();
			assertThat("Download dataset response is not null", response3, notNullValue());
			HttpResponse response4 = response3.returnResponse();
			assertThat("Download dataset HTTP response is not null", response4, notNullValue());
			assertThat("Download dataset status line is not null", response4.getStatusLine(), notNullValue());
			assertThat("Download dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
					equalTo(OK.getStatusCode()));
			Header[] headers = response4.getAllHeaders();
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
			HttpEntity entity = response4.getEntity();
			assertThat("Download dataset entity is not null", entity, notNullValue());
			assertThat("Download dataset content length coincides with expected", entity.getContentLength(), greaterThan(0l));
			File outfile = new File(TEST_OUTPUT_DIR, filename);
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
			GBSeq sequence = getSequence(outfile);
			assertThat("XML parsed from downloaded file is not null", sequence, notNullValue());
			// uncomment for additional output
			System.out.println(" >> Saved file: " + filename);

			// test create a dataset share (user1 grants access to user2)
			path = DatasetShareResource.class.getAnnotation(Path.class);
			final DatasetShare share = DatasetShare.builder()
					.subject(ownerId2)
					.build();
			response = target.path(path.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(share, APPLICATION_JSON));
			assertThat("Create dataset share response is not null", response, notNullValue());
			assertThat("Create dataset share response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset share response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset share response entity is not null", payload, notNullValue());
			assertThat("Create dataset share response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset share response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset share response JAX-RS object: " + response);
			System.out.println(" >> Create dataset share HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			assertThat("Create dataset share location is not null", location, notNullValue());
			assertThat("Create dataset share path is not empty", isNotBlank(location.getPath()), equalTo(true));

			// test listing dataset shares (from super-user account)	
			DatasetShares shares = target.path(path.value())
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
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
				shares = target.path(path.value())
						.path(urlEncodeUtf8(ownerId1))
						.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
						.request(APPLICATION_JSON)
						.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER3))
						.get(DatasetShares.class);
				fail("list dataset shares from unauthorized user account must produce 401 error");
			} catch (NotAuthorizedException e) {
				// uncomment for additional output			
				System.out.println(" >> List dataset shares (unauthorized user account) produced the expected 401 error");
			}			

			// test list dataset shares (from owner)
			shares = target.path(path.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(DatasetShares.class);
			assertThat("Get dataset shares (owner) result is not null", shares, notNullValue());
			assertThat("Get dataset shares (owner) list is not null", shares.getElements(), notNullValue());
			assertThat("Get dataset shares (owner) list is not empty", shares.getElements().isEmpty(), equalTo(false));
			assertThat("Get dataset shares (owner) items count coincide with list size", shares.getElements().size(), 
					equalTo(shares.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get dataset shares (owner) result: " + shares.toString());

			// test list datasets (from granted user)
			shares = target.path(path.value())
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER2))
					.get(DatasetShares.class);
			assertThat("Get dataset shares (granted user) result is not null", shares, notNullValue());
			assertThat("Get dataset shares (granted user) list is not null", shares.getElements(), notNullValue());
			assertThat("Get dataset shares (granted user) list is not empty", shares.getElements().isEmpty(), equalTo(false));
			assertThat("Get dataset shares (granted user) items count coincide with list size", shares.getElements().size(), 
					equalTo(shares.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get dataset shares (granted user) result: " + shares.toString());

			// test getting a dataset share from owner
			DatasetShare share2 = target.path(path.value())				
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.path(urlEncodeUtf8(ownerId2))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(DatasetShare.class);
			assertThat("Get dataset share (from owner) result is not null", share2, notNullValue());					
			assertThat("Get dataset share (from owner) access type is not null", share2.getAccessType(), notNullValue());
			assertThat("Get dataset share (from owner) access type coincides with expected", share2.getAccessType(), equalTo(VIEW_SHARE));
			assertThat("Get dataset share (from owner) filename is not empty", isNotBlank(share2.getFilename()), equalTo(true));
			assertThat("Get dataset share (from owner) filename coincides with expected", share2.getFilename(), equalTo("my_ncbi_sequence.xml"));
			assertThat("Get dataset share (from owner) namespace is not empty", isNotBlank(share2.getNamespace()), equalTo(true));
			assertThat("Get dataset share (from owner) namespace coincides with expected", share2.getNamespace(), equalTo(ownerId1));
			assertThat("Get dataset share (from owner) shared date is not null", share2.getSharedDate(), notNullValue());
			assertThat("Get dataset share (from owner) subject is not empty", isNotBlank(share2.getSubject()), equalTo(true));
			assertThat("Get dataset share (from owner) subject coincides with expected", share2.getSubject(), equalTo(ownerId2));			
			// uncomment for additional output
			System.out.println(" >> Get dataset share (from owner) result: " + share2.toString());

			// test viewing a dataset from an account granted (not from the owner account)
			share2 = target.path(path.value())
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.path(urlEncodeUtf8(ownerId2))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER2))
					.get(DatasetShare.class);
			assertThat("Get dataset share (from granted account) result is not null", share2, notNullValue());					
			assertThat("Get dataset share (from granted account) access type is not null", share2.getAccessType(), notNullValue());
			assertThat("Get dataset share (from granted account) access type coincides with expected", share2.getAccessType(), equalTo(VIEW_SHARE));
			assertThat("Get dataset share (from granted account) filename is not empty", isNotBlank(share2.getFilename()), equalTo(true));
			assertThat("Get dataset share (from granted account) filename coincides with expected", share2.getFilename(), equalTo("my_ncbi_sequence.xml"));
			assertThat("Get dataset share (from granted account) namespace is not empty", isNotBlank(share2.getNamespace()), equalTo(true));
			assertThat("Get dataset share (from granted account) namespace coincides with expected", share2.getNamespace(), equalTo(ownerId1));
			assertThat("Get dataset share (from granted account) shared date is not null", share2.getSharedDate(), notNullValue());
			assertThat("Get dataset share (from granted account) subject is not empty", isNotBlank(share2.getSubject()), equalTo(true));
			assertThat("Get dataset share (from granted account) subject coincides with expected", share2.getSubject(), equalTo(ownerId2));			
			// uncomment for additional output
			System.out.println(" >> Get dataset share (from granted account) result: " + share2.toString());

			// test viewing a dataset share (from user unauthorized user account)
			try {
				share2 = target.path(path.value())
						.path(urlEncodeUtf8(ownerId1))
						.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
						.path(urlEncodeUtf8(ownerId2))
						.request(APPLICATION_JSON)
						.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER3))
						.get(DatasetShare.class);				
				fail("get dataset share from unauthorized user account must produce 401 error");
			} catch (NotAuthorizedException e) {
				// uncomment for additional output			
				System.out.println(" >> Get dataset share (unauthorized user account) produced the expected 401 error");
			}

			// test removing permissions to data share and accessing (not from the owner account)
			try {
				response = target.path(path.value())
						.path(urlEncodeUtf8(ownerId1))
						.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
						.path(urlEncodeUtf8(ownerId2))
						.request()
						.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER2))
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
			response = target.path(path.value())
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.path(urlEncodeUtf8(ownerId2))
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
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

			// test create open access link
			path = DatasetOpenAccessResource.class.getAnnotation(Path.class);
			final DatasetOpenAccess openAccess = DatasetOpenAccess.builder()
					.filename("my_ncbi_sequence.xml")
					.namespace(ownerId1)
					.build();
			response = target.path(path.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS))
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.post(entity(openAccess, APPLICATION_JSON));
			assertThat("Create dataset open access link response is not null", response, notNullValue());
			assertThat("Create dataset open access link response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create dataset open access link response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create dataset open access link response entity is not null", payload, notNullValue());
			assertThat("Create dataset open access link response entity is empty", isBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Create dataset open access link response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create dataset open access link response JAX-RS object: " + response);
			System.out.println(" >> Create dataset open access link HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));			
			assertThat("Create dataset open access link location is not null", location, notNullValue());
			assertThat("Create dataset open access link path is not empty", isNotBlank(location.getPath()), equalTo(true));					

			// test get open access link (from user unauthorized user account)
			DatasetOpenAccess openAccess2 = null;
			try {
				openAccess2 = target.path(path.value())
						.path(urlEncodeUtf8(ownerId1))
						.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
						.request(APPLICATION_JSON)
						.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER3))
						.get(DatasetOpenAccess.class);				
				fail("get dataset open access link from unauthorized user account must produce 401 error");
			} catch (NotAuthorizedException e) {
				// uncomment for additional output		
				System.out.println(" >> Get dataset open access link (unauthorized user account) produced the expected 401 error");
			}

			// test get open access link (from owner account)
			openAccess2 = target.path(path.value())				
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(DatasetOpenAccess.class);
			assertThat("Get dataset open access link (from owner) result is not null", openAccess2, notNullValue());					
			assertThat("Get dataset open access link (from owner) filename is not empty", isNotBlank(openAccess2.getFilename()), equalTo(true));
			assertThat("Get dataset open access link (from owner) filename coincides with expected", openAccess2.getFilename(), equalTo("my_ncbi_sequence.xml"));
			assertThat("Get dataset open access link (from owner) namespace is not empty", isNotBlank(openAccess2.getNamespace()), equalTo(true));
			assertThat("Get dataset open access link (from owner) namespace coincides with expected", openAccess2.getNamespace(), equalTo(ownerId1));
			assertThat("Get dataset open access link (from owner) creation date is not null", openAccess2.getOpenAccessDate(), notNullValue());
			assertThat("Get dataset open access link (from owner) link is not empty", isNotBlank(openAccess2.getOpenAccessLink()), equalTo(true));	
			// uncomment for additional output
			System.out.println(" >> Get dataset open access link (from owner) result: " + openAccess2.toString());			

			// test list open access links (from owner account)
			DatasetOpenAccesses openAccesses = target.path(path.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.get(DatasetOpenAccesses.class);
			assertThat("Get dataset open access links (owner) result is not null", openAccesses, notNullValue());
			assertThat("Get dataset open access links (owner) list is not null", openAccesses.getElements(), notNullValue());
			assertThat("Get dataset open access links (owner) list is not empty", openAccesses.getElements().isEmpty(), equalTo(false));
			assertThat("Get dataset open access links (owner) items count coincide with list size", openAccesses.getElements().size(), 
					equalTo(openAccesses.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get dataset open access links (owner) result: " + openAccesses.toString());			

			// test download from open access link			
			downloadUri = target.path(PublicResource.class.getAnnotation(Path.class).value())
					.path("datasets")
					.path(urlEncodeUtf8(openAccesses.getElements().get(0).getOpenAccessLink()))
					.getUri();
			response3 = Request.Get(downloadUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", APPLICATION_OCTET_STREAM)
					.execute();
			assertThat("Download open access dataset response is not null", response3, notNullValue());
			response4 = response3.returnResponse();
			assertThat("Download open access dataset HTTP response is not null", response4, notNullValue());
			assertThat("Download open access dataset status line is not null", response4.getStatusLine(), notNullValue());
			assertThat("Download open access dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
					equalTo(OK.getStatusCode()));
			headers = response4.getAllHeaders();
			assertThat("Download open access dataset headers is not null", headers, notNullValue());
			assertThat("Download open access dataset headers is not empty", headers.length, greaterThan(0));
			filename = null;
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
			assertThat("Download open access dataset filename is not empty", isNotBlank(filename), equalTo(true));
			entity = response4.getEntity();
			assertThat("Download open access dataset entity is not null", entity, notNullValue());
			assertThat("Download open access dataset content length coincides with expected", entity.getContentLength(), greaterThan(0l));
			outfile = new File(TEST_OUTPUT_DIR, filename);
			deleteQuietly(outfile);
			outfile.createNewFile();
			try (final InputStream inputStream = entity.getContent();
					final FileOutputStream outputStream = new FileOutputStream(outfile)) {
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
			}
			assertThat("Downloaded open access file exists", outfile.exists(), equalTo(true));
			assertThat("Downloaded open access file is not empty", outfile.length(), greaterThan(0L));			
			sequence = getSequence(outfile);
			assertThat("XML parsed from downloaded open access file is not null", sequence, notNullValue());
			// uncomment for additional output
			System.out.println(" >> Saved open access file: " + filename);

			// test shorten URL with valid public resource
			URI endpointUri = target.path(PublicResource.class.getAnnotation(Path.class).value())
					.path("datasets")
					.path(urlEncodeUtf8(openAccesses.getElements().get(0).getOpenAccessLink()))
					.path("shortened_url")
					.getUri();
			response3 = Request.Get(endpointUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", TEXT_PLAIN)
					.execute();
			assertThat("Shorten open access dataset response is not null", response3, notNullValue());
			response4 = response3.returnResponse();
			assertThat("Shorten open access dataset HTTP response is not null", response4, notNullValue());
			assertThat("Shorten open access dataset status line is not null", response4.getStatusLine(), notNullValue());
			assertThat("Shorten open access dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
					equalTo(OK.getStatusCode()));
			entity = response4.getEntity();
			assertThat("Shorten open access dataset response is not empty", entity, notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Shorten open access dataset response entity is not null", payload, notNullValue());
			assertThat("Shorten open access dataset response entity is empty", isNotBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Shortened URL: " + payload);

			// test shorten URL with invalid public resource (should fail with status 404)
			endpointUri = target.path(PublicResource.class.getAnnotation(Path.class).value())
					.path("datasets")
					.path("this_is_an_invalid_secret")
					.path("shortened_url")
					.getUri();
			response3 = Request.Get(endpointUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", TEXT_PLAIN)
					.execute();
			assertThat("Shorten open access dataset response is not null", response3, notNullValue());
			response4 = response3.returnResponse();
			assertThat("Shorten open access dataset HTTP response is not null", response4, notNullValue());
			assertThat("Shorten open access dataset status line is not null", response4.getStatusLine(), notNullValue());
			assertThat("Shorten open access dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
					equalTo(NOT_FOUND.getStatusCode()));
			

			// TODO

			// test remove open access link
			response = target.path(path.value())
					.path(urlEncodeUtf8(ownerId1))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request()
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_USER1))
					.delete();
			assertThat("Delete dataset open access link response is not null", response, notNullValue());
			assertThat("Delete dataset open access link response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete dataset open access link response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete dataset open access link response entity is not null", payload, notNullValue());
			assertThat("Delete dataset open access link response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete dataset open access link response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete dataset open access link response JAX-RS object: " + response);
			System.out.println(" >> Delete dataset open access link HTTP headers: " + response.getStringHeaders());

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
					.post(entity(reference, APPLICATION_JSON));			
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
					.put(entity(reference, APPLICATION_JSON));
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

			// test create new instance
			final String instanceId = "00000001";
			final Date heartbeat = new Date();			

			path = LvlInstanceResource.class.getAnnotation(Path.class);
			final LvlInstance instance = LvlInstance.builder()
					.instanceId(instanceId)
					.roles(newHashSet("shard"))
					.heartbeat(heartbeat)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build())					
					.build();
			response = target.path(path.value()).request()
					.post(entity(instance, APPLICATION_JSON));			
			assertThat("Create new instance response is not null", response, notNullValue());
			assertThat("Create new instance response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Create new instance response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Create new instance response entity is not null", payload, notNullValue());
			assertThat("Create new instance response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Create new instance response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Create new instance response JAX-RS object: " + response);
			System.out.println(" >> Create new instance HTTP headers: " + response.getStringHeaders());			

			// test get instance by Id (Java object)
			LvlInstance instance2 = target.path(path.value()).path(instance.getInstanceId())
					.request(APPLICATION_JSON)
					.get(LvlInstance.class);
			assertThat("Get instance by Id result is not null", instance2, notNullValue());
			assertThat("Get instance by Id coincides with expected", instance2.equalsIgnoringVolatile(instance));
			// uncomment for additional output
			System.out.println(" >> Get instance by Id result: " + instance2.toString());

			// test list all instances (JSON encoded)
			response = target.path(path.value()).request(APPLICATION_JSON)
					.get();
			assertThat("Get instances response is not null", response, notNullValue());
			assertThat("Get instances response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Get instances response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get instances response entity is not null", payload, notNullValue());
			assertThat("Get instances response entity is not empty", isNotBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Get instances response body (JSON): " + payload);
			System.out.println(" >> Get instances response JAX-RS object: " + response);
			System.out.println(" >> Get instances HTTP headers: " + response.getStringHeaders());			

			// test list all instances (Java object)
			LvlInstances instances = target.path(path.value()).request(APPLICATION_JSON)
					.get(LvlInstances.class);
			assertThat("Get instances result is not null", instances, notNullValue());
			assertThat("Get instances list is not null", instances.getElements(), notNullValue());
			assertThat("Get instances list is not empty", !instances.getElements().isEmpty());
			assertThat("Get instances items count coincide with list size", instances.getElements().size(), equalTo(instances.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get instances result: " + instances.toString());			

			// test find instances near to a location (JSON encoded)
			uri = target.path(path.value()).path("nearby").path("-122.90").path("38.08")
					.queryParam("maxDistance", 1000.0d).getUri();
			response2 = Request.Get(uri)
					.addHeader("Accept", "application/json")
					.execute()
					.returnContent()
					.asString();
			assertThat("Get nearby instances result (plain) is not null", response2, notNullValue());
			assertThat("Get nearby instances result (plain) is not empty", isNotBlank(response2));
			// uncomment for additional output
			System.out.println(" >> Get nearby instances result (plain): " + response2);
			featCol = JSON_MAPPER.readValue(response2, FeatureCollection.class);
			assertThat("Get nearby instances result (plain) is not null", featCol, notNullValue());
			assertThat("Get nearby instances (plain) list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby instances (plain) list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output
			System.out.println(" >> Get nearby instances result (plain): " + featCol.toString());			

			// test find instances near to a location (Java object)
			featCol = target.path(path.value()).path("nearby").path("-122.90").path("38.08")
					.queryParam("maxDistance", 1000.0d)
					.request(APPLICATION_JSON)
					.get(FeatureCollection.class);
			assertThat("Get nearby instances result is not null", featCol, notNullValue());
			assertThat("Get nearby instances list is not null", featCol.getFeatures(), notNullValue());
			assertThat("Get nearby instances list is not empty", featCol.getFeatures().size() > 0);
			// uncomment for additional output			
			System.out.println(" >> Get nearby instances result: " + featCol.toString());

			// test update instance
			instance.setRoles(newHashSet("working_node"));
			response = target.path(path.value()).path(instance.getInstanceId())
					.request()
					.put(entity(instance, APPLICATION_JSON));
			assertThat("Update instance response is not null", response, notNullValue());
			assertThat("Update instance response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Update instance response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Update instance response entity is not null", payload, notNullValue());
			assertThat("Update instance response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Update instance response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Update instance response JAX-RS object: " + response);
			System.out.println(" >> Update instance HTTP headers: " + response.getStringHeaders());

			// test get instance by Id after update
			instance2 = target.path(path.value()).path(instance.getInstanceId())
					.request(APPLICATION_JSON)
					.get(LvlInstance.class);
			assertThat("Get instance by Id after update result is not null", instance2, notNullValue());
			assertThat("Get instance by Id after update coincides with expected", instance2.equalsIgnoringVolatile(instance));
			// uncomment for additional output
			System.out.println(" >> Get instance by Id after update result: " + instance2.toString());

			// test delete instance
			response = target.path(path.value()).path(instance.getInstanceId())
					.request()
					.delete();
			assertThat("Delete instance response is not null", response, notNullValue());
			assertThat("Delete instance response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
			assertThat("Delete instance response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Delete instance response entity is not null", payload, notNullValue());
			assertThat("Delete instance response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Delete instance response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Delete instance response JAX-RS object: " + response);
			System.out.println(" >> Delete instance HTTP headers: " + response.getStringHeaders());

			// test get collection statistics
			response = target.path(path.value())
					.path("stats/collection")
					.request()
					.accept(APPLICATION_JSON)
					.get();
			assertThat("Get collection stats is not null", response, notNullValue());
			assertThat("Get collection stats is OK", response.getStatus(), equalTo(OK.getStatusCode()));
			assertThat("Get collection stats is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Get collection stats entity is not null", payload, notNullValue());
			assertThat("Get collection stats entity is not empty", isNotBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Get collection stats body (JSON): " + payload);
			System.out.println(" >> Get collection stats JAX-RS object: " + response);
			System.out.println(" >> Get collection stats HTTP headers: " + response.getStringHeaders());			

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ServiceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ServiceTest.test() has finished");
		}
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

	public static class ExpectedException extends RuntimeException {

		private static final long serialVersionUID = -1985898389956760475L;

		public ExpectedException(final String message) {
			super(message);
		}

	}

}