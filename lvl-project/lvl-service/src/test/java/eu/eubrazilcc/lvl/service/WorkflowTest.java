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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.service.rest.WorkflowDefinitionResource;
import eu.eubrazilcc.lvl.service.rest.jackson.MapperProvider;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;

/**
 * Tests Workflow REST Web service.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			ServiceTest.class.getSimpleName() + "_" + random(8, true, true)));

	private static final String HOST = "https://localhost:8443";
	private static final String SERVICE = "/lvl-service/rest/v1";
	private static final String BASE_URI = HOST + SERVICE;

	private WebTarget target;
	private static final String TOKEN_ROOT = "1234567890abcdEFGhiJKlMnOpqrstUVWxyZ";

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
		// insert valid tokens in the database
		TOKEN_DAO.insert(AccessToken.builder()
				.token(TOKEN_ROOT)
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(604800l)
				.ownerId(toResourceOwnerId("root"))
				.scope(asPermissionList(allPermissions()))
				.build());		
	}

	@After
	public void cleanUp() {
		// cleanup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("WorkflowTest.test()");
		try {
			Path path = null;
			Response response = null;
			String payload, response2 = null;
			URI location = null, uri = null;

			// test list workflow definitions
			path = WorkflowDefinitionResource.class.getAnnotation(Path.class);
			WorkflowDefinitions definitions = target.path(path.value())
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(TOKEN_ROOT))
					.get(WorkflowDefinitions.class);
			assertThat("Get definitions result is not null", definitions, notNullValue());
			assertThat("Get definitions list is not null", definitions.getElements(), notNullValue());
			assertThat("Get definitions list is not empty", !definitions.getElements().isEmpty());
			assertThat("Get definitions items count coincide with list size", definitions.getElements().size(), equalTo(definitions.getTotalCount()));
			// uncomment for additional output			
			System.out.println(" >> Get definitions result: " + definitions.toString());



			// TODO
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("WorkflowTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("WorkflowTest.test() has finished");
		}
	}

}