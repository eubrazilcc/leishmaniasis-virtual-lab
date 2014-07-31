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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.json.client.FormValidationHelper.readValid;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.PendingUserDAO.PENDING_USER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper.bearerHeader;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.all;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asList;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.user;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.http.client.fluent.Form.form;
import static org.hamcrest.CoreMatchers.containsString;
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

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.fluent.Request;
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
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.oauth2.rest.IdentityProvider;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2AuthzServer;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2Registration;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2Token;
import eu.eubrazilcc.lvl.oauth2.rest.OAuth2TokenRevocation;
import eu.eubrazilcc.lvl.oauth2.rest.UserRegistration;
import eu.eubrazilcc.lvl.storage.oauth2.PendingUser;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.User;
import eu.eubrazilcc.lvl.storage.oauth2.Users;
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
		CONFIG_MANAGER.setup(builder.build());
		CONFIG_MANAGER.preload();
		// setup test file-system environment
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);
		// prepare client
		final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
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
			final ResourceOwner resourceOwner = ResourceOwner.builder()
					.id("test_username")
					.user(User.builder()
							.username("test_username")
							.password("test_password")
							.email("username@example.com")
							.fullname("Fullname")
							.scopes(asList(scope))
							.build()).build();			
			RESOURCE_OWNER_DAO.insert(resourceOwner);

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
					.setUsername(resourceOwner.getUser().getUsername())
					.setPassword(resourceOwner.getUser().getPassword())
					.buildBodyMessage();
			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());			
			OAuthJSONAccessTokenResponse response2 = oAuthClient.accessToken(request);
			assertThat("Access token is valid", response2.getAccessToken(), notNullValue());
			assertThat("Access token expiration is not null", response2.getExpiresIn(), notNullValue());
			assertThat("Access token scope is not null", response2.getScope(), notNullValue());
			String accessToken = response2.getAccessToken();
			/* uncomment the following lines for additional output */
			System.out.println("     >> Access token: " + response2.getAccessToken());
			System.out.println("     >> Expires in: " + response2.getExpiresIn() + " seconds");
			System.out.println("     >> Scope: " + response2.getScope());

			// test access token (email & password), this test uses an additional parameter in the request
			uri = UriBuilder.fromUri(BASE_URI).path(OAuth2Token.class).build();
			System.out.println(" >> Access token (email & password): " + uri.toString());
			request = OAuthClientRequest
					.tokenLocation(uri.toString())
					.setGrantType(GrantType.PASSWORD)
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setUsername(resourceOwner.getUser().getEmail())
					.setPassword(resourceOwner.getUser().getPassword())
					.setParameter(OAuth2Token.USE_EMAIL, "true") // additional parameter
					.buildBodyMessage();
			oAuthClient = new OAuthClient(new URLConnectionClient());			
			response2 = oAuthClient.accessToken(request);
			assertThat("Access token is valid (using email address)", response2.getAccessToken(), notNullValue());
			assertThat("Access token expiration is not null (using email address)", response2.getExpiresIn(), notNullValue());
			assertThat("Access token scope is not null (using email address)", response2.getScope(), notNullValue());
			accessToken = response2.getAccessToken();
			/* uncomment the following lines for additional output */
			System.out.println("     >> Access token (using email address): " + response2.getAccessToken());
			System.out.println("     >> Expires in (using email address): " + response2.getExpiresIn() + " seconds");
			System.out.println("     >> Scope (using email address): " + response2.getScope());

			// test token revocation
			request = OAuthClientRequest
					.tokenLocation(uri.toString())
					.setGrantType(GrantType.PASSWORD)
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setUsername(resourceOwner.getUser().getUsername())
					.setPassword(resourceOwner.getUser().getPassword())
					.buildBodyMessage();
			oAuthClient = new OAuthClient(new URLConnectionClient());
			response2 = oAuthClient.accessToken(request);
			final String accessToken2 = response2.getAccessToken();

			Path path = OAuth2TokenRevocation.class.getAnnotation(Path.class);
			System.out.println(" >> Token revocation: " + target.path(path.value()).getUri().toString());			
			Form form = new Form();
			form.param("token", accessToken2);
			form.param("client_id", clientId);
			form.param("client_secret", clientSecret);					
			Response response3 = target.path(path.value()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken2))
					.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			assertThat("Revoke token response is not null", response3, notNullValue());
			assertThat("Revoke token response is OK", response3.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
			assertThat("Revoke token response is not empty", response3.getEntity(), notNullValue());
			String payload = response3.readEntity(String.class);
			assertThat("Revoke token response entity is not null", payload, notNullValue());
			assertThat("Revoke token response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Revoke token response body (JSON), empty is OK: " + payload);
			System.out.println("     >> Revoke token response JAX-RS object: " + response3);
			System.out.println("     >> Revoke token HTTP headers: " + response3.getStringHeaders());

			// test identity provider (IdP) create new user
			path = IdentityProvider.class.getAnnotation(Path.class);
			final User user = User.builder()
					.username("username2")
					.password("password2")
					.email("username2@example.com")
					.fullname("Fullname2")
					.scopes(asList(user("username2")))
					.build();
			System.out.println(" >> IdP resource server: " + target.path(path.value()).getUri().toString());
			response3 = target.path(path.value()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
			assertThat("Create new user response is not null", response3, notNullValue());
			assertThat("Create new user response is CREATED", response3.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
			assertThat("Create new user response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
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
			assertThat("Get users response is OK", response3.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
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
			assertThat("Get user by username coincides with expected", user2.equalsIgnoreVolatile(user));
			/* uncomment for additional output */
			System.out.println("     >> Get user by username result: " + user2.toString());

			// test identity provider (IdP) get user by email address
			user2 = target.path(path.value()).path(user.getEmail()).queryParam("use_email", true).queryParam("plain", true)
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.get(User.class);
			assertThat("Get user by email address result is not null", user2, notNullValue());
			assertThat("Get user by email address coincides with expected", user2.equalsIgnoreVolatile(user));
			/* uncomment for additional output */
			System.out.println("     >> Get user by email address result: " + user2.toString());			

			// test identity provider (IdP) update user
			user.setPassword("updated_password2");
			response3 = target.path(path.value()).path(user.getUsername()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.put(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
			assertThat("Update user response is not null", response3, notNullValue());
			assertThat("Update user response is OK", response3.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
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
			assertThat("Get user by username after update coincides with expected", user2.equalsIgnoreVolatile(user));
			/* uncomment for additional output */
			System.out.println("     >> Get user by username after update result: " + user2.toString());

			// test identity provider (IdP) get user by username with revoked token
			try {
				target.path(path.value()).path(user.getUsername())
				.request(MediaType.APPLICATION_JSON)
				.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken2))
				.get(User.class);
				fail("Should have thrown an NotAuthorizedException because access token is revoked");
			} catch (NotAuthorizedException e) {
				assertThat(e.getMessage(), containsString("HTTP 401 Unauthorized"));
			}

			// test identity provider (IdP) delete user
			response3 = target.path(path.value()).path(user.getUsername()).request()
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.delete();
			assertThat("Delete user response is not null", response3, notNullValue());
			assertThat("Delete user response is OK", response3.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
			assertThat("Delete user response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("Delete user response entity is not null", payload, notNullValue());
			assertThat("Delete user response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Delete user response body (JSON), empty is OK: " + payload);
			System.out.println("     >> Delete user response JAX-RS object: " + response3);
			System.out.println("     >> Delete user HTTP headers: " + response3.getStringHeaders());

			// test user registration
			path = UserRegistration.class.getAnnotation(Path.class);
			System.out.println(" >> User registration resource server: " + target.path(path.value()).getUri().toString());
			response3 = target.path(path.value()).queryParam("skip_activation", true).request()
					.post(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));
			assertThat("Create pending user response is not null", response3, notNullValue());
			assertThat("Create pending user response is CREATED", response3.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
			assertThat("Create pending user response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("Create pending user response entity is not null", payload, notNullValue());
			assertThat("Create pending user response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> Create pending user response body (JSON), empty is OK: " + payload);
			System.out.println("     >> Create pending user response JAX-RS object: " + response3);
			System.out.println("     >> Create pending user HTTP headers: " + response3.getStringHeaders());

			// test user registration new user activation
			final PendingUser pendingUser = PENDING_USER_DAO.findByEmail(user.getEmail());
			final PendingUser pendingUser2 = PendingUser.builder()
					.user(User.builder().email(user.getEmail()).build())
					.activationCode(pendingUser.getActivationCode())
					.build();
			response3 = target.path(path.value()).path(user.getEmail()).request()
					.put(Entity.entity(pendingUser2, MediaType.APPLICATION_JSON_TYPE));
			assertThat("New user activation response is not null", response3, notNullValue());			
			assertThat("New user activation response is OK", response3.getStatus(), equalTo(Response.Status.NO_CONTENT.getStatusCode()));
			assertThat("New user activation response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("New user activation response entity is not null", payload, notNullValue());
			assertThat("New user activation response entity is empty", isBlank(payload));
			/* uncomment for additional output */			
			System.out.println("     >> New user activation response body (JSON), empty is OK: " + payload);
			System.out.println("     >> New user activation response JAX-RS object: " + response3);
			System.out.println("     >> New user activation HTTP headers: " + response3.getStringHeaders());

			// test identity provider (IdP) get user by username after activation
			path = IdentityProvider.class.getAnnotation(Path.class);
			System.out.println(" >> IdP resource server: " + target.path(path.value()).getUri().toString());
			user2 = target.path(path.value()).path(user.getUsername()).queryParam("plain", true)
					.request(MediaType.APPLICATION_JSON)
					.header(OAuth2Common.HEADER_AUTHORIZATION, bearerHeader(accessToken))
					.get(User.class);
			assertThat("Get user by username after validation result is not null", user2, notNullValue());
			assertThat("Get user by username after validation scopes is not null", user2.getScopes(), notNullValue());
			assertThat("Get user by username after validation scopes is not empty", !user2.getScopes().isEmpty());
			assertThat("Get user by username after validation coincides with expected", user2.equalsIgnoreVolatile(user));
			/* uncomment for additional output */
			System.out.println("     >> Get user by username after validation result: " + user2.toString());

			// test check user availability with form field: username, expected response: user unavailable
			path = UserRegistration.class.getAnnotation(Path.class);
			final Path innerPath = UserRegistration.class.getMethod("checkUserAvailability", new Class<?>[]{ MultivaluedMap.class }).getAnnotation(Path.class);
			System.out.println(" >> Check user availability: " + target.path(path.value()).path(innerPath.value()).getUri().toString());
			form = new Form();
			form.param("type", "username");
			form.param("username", user.getUsername());
			response3 = target.path(path.value()).path(innerPath.value()).request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
			assertThat("Check user availability response is not null", response3, notNullValue());
			assertThat("Check user availability response is OK", response3.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
			assertThat("Check user availability response is not empty", response3.getEntity(), notNullValue());
			payload = response3.readEntity(String.class);
			assertThat("Check user availability response entity is not null", payload, notNullValue());
			assertThat("Check user availability response entity is not empty", !isBlank(payload));
			assertThat("Check user availability coincides with expected", !readValid(payload));
			/* uncomment for additional output */
			System.out.println("     >> Check user availability response body (JSON): " + payload);
			System.out.println("     >> Check user availability response JAX-RS object: " + response3);			

			// test check user availability with form field: email, expected response: user available (using plain REST, no Jersey client)
			uri = target.path(path.value()).path(innerPath.value()).getUri();
			System.out.println(" >> Check user availability (plain): " + uri.toString());
			final String response4 = Request.Post(uri)
					.addHeader("Accept", "text/javascript") // also supports: application/json
					.bodyForm(form().add("email", "not_existing_email@example.org").add("type", "email").build())
					.execute()
					.returnContent()
					.asString();
			assertThat("Check user availability (plain) is not null", response4, notNullValue());
			assertThat("Check user availability (plain) is not empty", isNotBlank(response4));
			/* uncomment for additional output */
			System.out.println("Check user availability (plain): " + response4);
			final boolean valid = readValid(response4);
			assertThat("Check user availability (plain) concides with expected", valid, equalTo(true));	
			/* uncomment for additional output */
			System.out.println("Check user availability (plain) returns: " + valid);
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