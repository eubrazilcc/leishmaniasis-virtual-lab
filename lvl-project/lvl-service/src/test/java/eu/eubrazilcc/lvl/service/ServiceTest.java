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

package eu.eubrazilcc.lvl.service;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationFinder.DEFAULT_LOCATION;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.USER_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.userPermissions;
import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.toURLs;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.service.rest.jackson.MapperProvider;
import eu.eubrazilcc.lvl.service.testable.CitationResourceTest;
import eu.eubrazilcc.lvl.service.testable.DatasetOpenAccessResourceTest;
import eu.eubrazilcc.lvl.service.testable.DatasetResourceTest;
import eu.eubrazilcc.lvl.service.testable.DatasetShareResourceTest;
import eu.eubrazilcc.lvl.service.testable.IssuesResourceTest;
import eu.eubrazilcc.lvl.service.testable.LeishmaniaResourceTest;
import eu.eubrazilcc.lvl.service.testable.LeishmaniaSampleResourceTest;
import eu.eubrazilcc.lvl.service.testable.LvlInstanceResourceTest;
import eu.eubrazilcc.lvl.service.testable.PostResourceTest;
import eu.eubrazilcc.lvl.service.testable.SandflyPendingResourceTest;
import eu.eubrazilcc.lvl.service.testable.SandflyResourceTest;
import eu.eubrazilcc.lvl.service.testable.SandflySampleResourceTest;
import eu.eubrazilcc.lvl.service.testable.SavedSearchResourceTest;
import eu.eubrazilcc.lvl.service.testable.SubscriptionRequestResourceTest;
import eu.eubrazilcc.lvl.service.testable.TaskResourceTest;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.security.User;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.TestContext.TestCredential;

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
	private String ownerIdRoot;
	private String ownerId1;
	private String ownerId2;
	private String ownerId3;

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
		// load test configuration
		final File file = new File(concat(DEFAULT_LOCATION, "test" + separator + "etc" + separator + REST_SERVICE_CONFIG));
		if (file.canRead()) {
			final List<URL> urls = newArrayList(getDefaultConfiguration());
			for (final ListIterator<URL> it = urls.listIterator(); it.hasNext();) {
				final URL url = it.next();
				if (url.getPath().endsWith(REST_SERVICE_CONFIG)) {
					it.remove();
					it.add(toURLs(new File[]{ file })[0]);
				}
			}
			CONFIG_MANAGER.setup(urls);
		}
		CONFIG_MANAGER.preload();
		// setup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);		
		TEST_OUTPUT_DIR.mkdirs();
		// prepare client
		final Client client = ClientBuilder.newBuilder()
				.register(MapperProvider.class)
				.register(JacksonFeature.class)
				.register(SseFeature.class)
				.register(MultiPartFeature.class)
				.build();
		// configure Web target
		target = client.target(BASE_URI);
		target.property(ClientProperties.FOLLOW_REDIRECTS, true);
		// insert valid users in the database (they are needed for properly authentication/authorization)
		ownerIdRoot = toResourceOwnerId("root");
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
				.ownerId(ownerIdRoot)
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
			// create test context
			final TestContext testCtxt = new TestContext(TEST_OUTPUT_DIR, SERVICE, target, JSON_MAPPER, ImmutableMap.of(
					"root", new TestCredential(ownerIdRoot, TOKEN_ROOT), 
					"user1", new TestCredential(ownerId1, TOKEN_USER1), 
					"user2", new TestCredential(ownerId2, TOKEN_USER2), 
					"user3", new TestCredential(ownerId3, TOKEN_USER3)));
			
			// test task resource
			new TaskResourceTest(testCtxt).runTest();

			// test sand-flies resource
			new SandflyResourceTest(testCtxt).runTest();

			// test leishmania resource
			new LeishmaniaResourceTest(testCtxt).runTest();

			// test dataset resource
			new DatasetResourceTest(testCtxt).runTest();

			// test sand-fly samples resource
			new SandflySampleResourceTest(testCtxt).runTest();

			// test leishmania samples resource
			new LeishmaniaSampleResourceTest(testCtxt).runTest();

			// test pending sand-fly sequences resource
			new SandflyPendingResourceTest(testCtxt).runTest();

			// test pending leishmania sequences resource
			new LeishmaniaResourceTest(testCtxt).runTest();

			// test shared dataset resource
			new DatasetShareResourceTest(testCtxt).runTest();

			// test dataset open access resource
			new DatasetOpenAccessResourceTest(testCtxt).runTest();

			// test citation resource
			new CitationResourceTest(testCtxt).runTest();

			// test instance resource
			new LvlInstanceResourceTest(testCtxt).runTest();

			// test saved search resource
			new SavedSearchResourceTest(testCtxt).runTest();
			
			// test post resource
			new PostResourceTest(testCtxt).runTest();

			// test issues resource
			new IssuesResourceTest(testCtxt).runTest();

			// test subscription request resource
			new SubscriptionRequestResourceTest(testCtxt).runTest();

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