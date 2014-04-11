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

package eu.eubrazilcc.lvl.oauth2;

import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.bearerHeader;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.all;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asList;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.user;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.ext.dynamicreg.client.OAuthRegistrationClient;
import org.apache.oltu.oauth2.ext.dynamicreg.client.request.OAuthClientRegistrationRequest;
import org.apache.oltu.oauth2.ext.dynamicreg.client.response.OAuthClientRegistrationResponse;
import org.apache.oltu.oauth2.ext.dynamicreg.common.OAuthRegistration;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.oauth2.rest.IdentityProvider;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2AuthzServer;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2Registration;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2Token;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.User;
import eu.eubrazilcc.lvl.storage.oauth2.Users;
import eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common;
import eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider;

/**
 * Integration test.
 * @author Erik Torres <ertorser@upv.es>
 */
public class AuthTest {

	private static final File TEST_OUTPUT_DIR = new File(FilenameUtils.concat(System.getProperty("java.io.tmpdir"),
			"lvl-authserver-test-Hf330xKUcsn7vnlKQXFndptow52MvZNKWxxpbnVqAA"));

	private static final String BASE_URI = "https://localhost:8443/lvl-auth/oauth2/v1";

	private WebTarget target;

	@Before
	public void setUp() throws Exception {
		// load test configuration
		final ImmutableList.Builder<URL> builder = new ImmutableList.Builder<URL>();
		final ImmutableList<URL> defaultUrls = ConfigurationManager.getDefaultConfiguration();
		for (final URL url : defaultUrls) {
			if (!url.toString().endsWith(ConfigurationManager.AUTHZ_SERVER_CONFIG)) {
				builder.add(url);
			} else {
				builder.add(this.getClass().getResource("/config/lvl-auth.xml"));
			}
		}
		ConfigurationManager.INSTANCE.setup(builder.build());
		ConfigurationManager.INSTANCE.preload();
		// setup test file-system environment
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);
		// prepare client
		final Client client = ClientBuilder.newBuilder().register(MoxyJsonFeature.class).build();
		// configure Web target
		target = client.target(BASE_URI);
	}

	@After
	public void cleanUp() {
		// cleanup test file-system environment
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);
	}

	@Test
	public void test() {
		System.out.println("AuthTest.test()");
		try {
			final String redirectURI = "https://localhost:8443/redirect";
			final String state = SecretProvider.generateFastUrlSafeSecret();
			final String scope = all();
			final String username = "test_username";
			final String password = "test_password";
			ResourceOwnerDAO.INSTANCE.insert(ResourceOwner.builder()
					.id(username)
					.user(User.builder()
							.username(username)
							.password(password)
							.email("username@example.com")
							.fullname("Fullname")
							.scope(asList(scope))
							.build()).build());

			// test client registration
			URI uri = UriBuilder.fromUri(BASE_URI).path(OAuth2Registration.class).build();
			System.out.println(" >> Client registration: " + uri.toString());
			OAuthClientRequest request = OAuthClientRegistrationRequest
					.location(uri.toString(), OAuthRegistration.Type.PUSH)
					.setName("Sample Application")
					.setUrl("http://www.example.com")
					.setDescription("Description of a Sample App")
					.setIcon("http://www.example.com/app.ico")
					.setRedirectURL(redirectURI)
					.buildJSONMessage();			
			OAuthRegistrationClient oauthclient = new OAuthRegistrationClient(new URLConnectionClient());
			OAuthClientRegistrationResponse response = oauthclient.clientInfo(request);
			assertThat("Registration response is not null", response, notNullValue());
			assertThat("Registration client Id is valid", StringUtils.isNotBlank(response.getClientId()));
			assertThat("Registration client secret is valid", StringUtils.isNotBlank(response.getClientSecret()));
			assertThat("Registration expiration is valid", response.getExpiresIn(), notNullValue());
			assertThat("Registration issued at is valid", StringUtils.isNotBlank(response.getIssuedAt()));
			final String clientId = response.getClientId();
			final String clientSecret = response.getClientSecret();
			/* uncomment the following lines for additional output */
			System.out.println("     >> Client Id: " + response.getClientId());
			System.out.println("     >> Client secret: " + response.getClientSecret());
			System.out.println("     >> Expires in: " + response.getExpiresIn() + " seconds");
			System.out.println("     >> Issued at: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(response.getIssuedAt()) * 1000l)));

			// test client authorization (end user)
			uri = UriBuilder.fromUri(BASE_URI).path(OAuth2AuthzServer.class).build();
			System.out.println(" >> Client (end user) authorization: " + uri.toString());
			request = OAuthClientRequest
					.authorizationLocation(uri.toString())
					.setClientId(clientId)
					.setRedirectURI(redirectURI)
					.setResponseType(ResponseType.CODE.toString())
					.setState(state)
					.buildQueryMessage();
			HttpURLConnection conn = doRequest(request);
			String queryString = conn.getURL().toURI().getQuery();
			Map<String, Object> map = OAuthUtils.decodeForm(queryString);
			assertThat("End user authorization code is not null", map.get(OAuth.OAUTH_CODE), notNullValue());
			assertThat("End user authorization state coincides with original", (String)map.get(OAuth.OAUTH_STATE), equalTo(state));
			/* uncomment the following lines for additional output */
			for (final Map.Entry<String, Object> entry : map.entrySet()) {
				System.out.println("     >> " + entry.getKey() + " -> " + entry.getValue());
			}

			// test client authorization (token)
			uri = UriBuilder.fromUri(BASE_URI).path(OAuth2AuthzServer.class).build();
			System.out.println(" >> Client (token) authorization: " + uri.toString());
			request = OAuthClientRequest
					.authorizationLocation(uri.toString())
					.setClientId(clientId)
					.setRedirectURI(redirectURI)
					.setResponseType(ResponseType.TOKEN.toString())
					.setState(state)
					.buildQueryMessage();
			conn = doRequest(request);
			String fragment = conn.getURL().toURI().getFragment();
			map = OAuthUtils.decodeForm(fragment);
			assertThat("Token authorization expiration is not null", map.get(OAuth.OAUTH_EXPIRES_IN), notNullValue());
			assertThat("Token authorization token is not null", map.get(OAuth.OAUTH_ACCESS_TOKEN), notNullValue());
			/* uncomment the following lines for additional output */
			for (final Map.Entry<String, Object> entry : map.entrySet()) {
				System.out.println("     >> " + entry.getKey() + " -> " + entry.getValue());
			}
			assertThat("Token authorization state coincides with original", (String)map.get(OAuth.OAUTH_STATE), equalTo(state));

			// test access token (user & password)
			uri = UriBuilder.fromUri(BASE_URI).path(OAuth2Token.class).build();
			System.out.println(" >> Access token (user & password): " + uri.toString());
			request = OAuthClientRequest
					.tokenLocation(uri.toString())
					.setGrantType(GrantType.PASSWORD)
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setUsername(username)
					.setPassword(password)
					.buildBodyMessage();
			final OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());			
			final OAuthJSONAccessTokenResponse response2 = oAuthClient.accessToken(request);
			assertThat("Access token is valid", response2.getAccessToken(), notNullValue());
			assertThat("Access token expiration is not null", response2.getExpiresIn(), notNullValue());
			assertThat("Access token scope is not null", response2.getScope(), notNullValue());
			final String accessToken = response2.getAccessToken();
			/* uncomment the following lines for additional output */
			System.out.println("     >> Access token: " + response2.getAccessToken());
			System.out.println("     >> Expires in: " + response2.getExpiresIn() + " seconds");
			System.out.println("     >> Scope: " + response2.getScope());

			// test identity provider (IdP) create new user
			final Path path = IdentityProvider.class.getAnnotation(Path.class);
			final User user = User.builder()
					.username("username2")
					.password("password2")
					.email("username2@example.com")
					.fullname("Fullname2")
					.scope(user("username2"))
					.build();
			System.out.println(" >> IdP resource server: " + target.path(path.value()).getUri().toString());
			Response response3 = target.path(path.value()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
			assertThat("Create new user response is not null", response3, notNullValue());
			assertThat("Create new user response is CREATED", response3.getStatus() == Response.Status.CREATED.getStatusCode());
			assertThat("Create new user response is not empty", response3.getEntity(), notNullValue());
			String payload = response3.readEntity(String.class);
			assertThat("Create new user response entity is not null", payload, notNullValue());
			assertThat("Create new user response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Create new user response body (JSON), empty is OK: " + payload);
			System.out.println("     >> Create new user response JAX-RS object: " + response3);
			System.out.println("     >> Create new user HTTP headers: " + response3.getStringHeaders());

			// test identity provider (IdP) get users (JSON encoded)
			response3 = target.path(path.value()).request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.get();
			assertThat("Get users response is not null", response3, notNullValue());
			assertThat("Get users response is OK", response3.getStatus() == Response.Status.OK.getStatusCode());
			assertThat("Get users response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("Get users response entity is not null", payload, notNullValue());
			assertThat("Get users response entity is not empty", isNotBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Get users response body (JSON): " + payload);
			System.out.println("     >> Get users response JAX-RS object: " + response3);
			System.out.println("     >> Get users HTTP headers: " + response3.getStringHeaders());

			// test identity provider (IdP) get users (Java object)
			final Users users = target.path(path.value()).request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.get(Users.class);
			assertThat("Get users result is not null", users, notNullValue());
			assertThat("Get users list is not null", users.getUsers(), notNullValue());
			assertThat("Get users list is not empty", !users.getUsers().isEmpty());
			/* uncomment for additional output */
			System.out.println("     >> Get users result: " + users.toString());

			// test identity provider (IdP) get user by username
			User user2 = target.path(path.value()).path(user.getUsername()).queryParam("plain", true)
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.get(User.class);
			assertThat("Get user by username result is not null", user2, notNullValue());
			assertThat("Get user by username coincides with expected", user2.equalsIgnoreLink(user));
			/* uncomment for additional output */
			System.out.println("     >> Get user by username result: " + user2.toString());

			// test identity provider (IdP) update user
			user.setPassword("updated_password2");
			response3 = target.path(path.value()).path(user.getUsername()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.put(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
			assertThat("Update user response is not null", response3, notNullValue());
			assertThat("Update user response is OK", response3.getStatus() == Response.Status.NO_CONTENT.getStatusCode());
			assertThat("Update user response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("Update user response entity is not null", payload, notNullValue());
			assertThat("Update user response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Update user response body (JSON), empty is OK: " + payload);
			System.out.println("     >> Update user response JAX-RS object: " + response3);
			System.out.println("     >> Update user HTTP headers: " + response3.getStringHeaders());

			// test identity provider (IdP) get user by username after update
			user2 = target.path(path.value()).path(user.getUsername()).queryParam("plain", true)
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.get(User.class);
			assertThat("Get user by username after update result is not null", user2, notNullValue());
			assertThat("Get user by username after update coincides with expected", user2.equalsIgnoreLink(user));
			/* uncomment for additional output */
			System.out.println("     >> Get user by username after update result: " + user2.toString());			

			// test identity provider (IdP) delete user
			response3 = target.path(path.value()).path(user.getUsername()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.delete();
			assertThat("Delete user response is not null", response3, notNullValue());
			assertThat("Delete user response is OK", response3.getStatus() == Response.Status.NO_CONTENT.getStatusCode());
			assertThat("Delete user response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("Delete user response entity is not null", payload, notNullValue());
			assertThat("Delete user response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Delete user response body (JSON), empty is OK: " + payload);
			System.out.println("     >> Delete user response JAX-RS object: " + response3);
			System.out.println("     >> Delete user HTTP headers: " + response3.getStringHeaders());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("AuthTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("AuthTest.test() has finished");
		}
	}

	private static HttpURLConnection doRequest(final OAuthClientRequest request) throws IOException {
		final URL url = new URL(request.getLocationUri());
		final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setInstanceFollowRedirects(true);
		conn.connect();
		conn.getResponseCode();
		return conn;
	}

}