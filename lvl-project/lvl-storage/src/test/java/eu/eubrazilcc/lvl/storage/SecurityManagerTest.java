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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.OWNERID_EL_TEMPLATE;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.ADMIN_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.USER_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.defaultPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.userPermissions;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.generateSecret;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.hashAndSaltPassword;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.security.SimpleSecurityManager;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Tests security components.<br>
 * <br>
 * Roles and permissions:<br>
 * <br>
 * Permission template: <tt>[scope]:[collection]:[sub-collection]:[item]:[permission]</tt><br>
 * <br>
 * For example, the following permission allows granted users to view all the items stored in the 
 * public sub-collection of Leishmania sequences:<br>
 * <br>
 * <tt>sequences:leishmania:public:*:view</tt><br>
 * <br>
 * Role: <tt>user</tt>
 * <ul>
 * <li><tt>sequences:leishmania:public:*:view</tt> (public collection of Leishmania sequences)</li>
 * <li><tt>sequences:sandflies:public:*:view</tt> (public collection of Sandfly sequences)</li>
 * <li><tt>sequences:*:username@provider:*:view,edit,create</tt> (collection of sequences private to 
 *     the user)</li>
 * </ul>
 * <br>
 * Role: <tt>leishmania_curator</tt><br>
 * <tt>sequences:leishmania:*:*:view,edit,create</tt><br>
 * <br>
 * Role: <tt>sandfly_curator</tt><br>
 * <tt>sequences:sandfly:*:*:view,edit,create</tt><br>
 * <br>
 * Shared item permissions:<br>
 * <br>
 * For example, user1 shares the Sandfly sequence1 with user2, allowing him/her to edit the sequence.
 * User2 receives the following permission:<br>
 * <br>
 * <tt>sequences:sandflies:user1@provider:sequence1:view,edit</tt><br>
 * <br>
 * @author Erik Torres <ertorser@upv.es>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityManagerTest {

	private static String adminOwnerid;
	private static String meremortalOwnerid;
	private static String poormanOwnerid;
	private static String accessToken;

	@BeforeClass
	public static void setUp() throws Exception {		
		// insert users, permissions and roles in the database (passwords can be provided )
		adminOwnerid = toResourceOwnerId("adminuser");
		final String[] hashed = hashAndSaltPassword("password"); 
		Collection<String> roles = newArrayList(ADMIN_ROLE);
		Collection<String> permissions = newArrayList(asPermissionList(allPermissions()));
		createResourceOwner(User.builder()
				.userid("adminuser")
				.password(hashed[1])
				.salt(hashed[0]) // hashed password and salt are provided
				.email("adminuser@example.com")
				.fullname("Admin User Fullname")
				.roles(roles)
				.permissions(permissions)
				.build());
		meremortalOwnerid = toResourceOwnerId("meremortal");
		roles = newArrayList(USER_ROLE);
		permissions = newArrayList(asPermissionList(userPermissions(meremortalOwnerid)));
		createResourceOwner(User.builder()
				.userid("meremortal")
				.password("password2") // clear-text password is provided, the DAO is responsible for protecting the password before storing it
				.email("meremortal@example.com")
				.fullname("Mere Mortal User Fullname")
				.roles(roles)
				.permissions(permissions)
				.build());
		poormanOwnerid = toResourceOwnerId("poorman");
		permissions = newArrayList(asPermissionList(defaultPermissions()));
		createResourceOwner(User.builder()
				.userid("poorman")
				.password("password3") // clear-text password is provided, the DAO is responsible for protecting the password before storing it
				.email("poorman@example.com")
				.fullname("Poor man User Fullname")
				.permissions(permissions)
				.build());
		// insert access tokens in the database
		accessToken = createToken(meremortalOwnerid, permissions);
	}

	@AfterClass
	public static void cleanUp() {
		RESOURCE_OWNER_DAO.delete(adminOwnerid);
		RESOURCE_OWNER_DAO.delete(meremortalOwnerid);
		RESOURCE_OWNER_DAO.delete(poormanOwnerid);
		TOKEN_DAO.delete(accessToken);
	}

	@Test
	public void test01AuthX() {
		System.out.println("SecurityManagerTest.test01AuthX()");
		try {
			// test LVL-based authentication using username/password
			SimpleSecurityManager currentUser = SimpleSecurityManager.builder().build();			
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));
			currentUser.login("adminuser", "password");
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));

			String principal = currentUser.getPrincipal();
			assertThat("principal is not null", principal, notNullValue());
			assertThat("principal is not empty", isNotBlank(principal));
			assertThat("principal coincides with expected", principal, equalTo(adminOwnerid));
			/* uncomment for additional output */
			System.out.println(" >> Currently executing user, principal: " + principal);

			assertThat("expected role is present", currentUser.hasRole("admin"), equalTo(true));
			assertThat("expected permission is present", currentUser.isPermitted("lightsaber:weild"), equalTo(true));
			assertThat("expected permissions are present", currentUser.isPermittedAll("lightsaber:weild"), equalTo(true));

			currentUser.logout();
			assertThat("user logged out successfully", currentUser.isAuthenticated(), equalTo(false));

			// mere mortal account
			currentUser = SimpleSecurityManager.builder().build();
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));
			currentUser.login("meremortal", "password2");			      
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));

			principal = currentUser.getPrincipal();
			assertThat("principal is not null", principal, notNullValue());
			assertThat("principal is not empty", isNotBlank(principal));
			assertThat("principal coincides with expected", principal, equalTo(meremortalOwnerid));
			/* uncomment for additional output */
			System.out.println(" >> Currently executing user, principal: " + principal);

			assertThat("expected role is present", currentUser.hasRole("user"), equalTo(true));
			assertThat("unexpected role is not present", currentUser.hasRole("admin"), equalTo(false));
			assertThat("expected permission is present", currentUser.isPermitted("sequences:leishmania:public:item1:view"), equalTo(true));
			assertThat("expected permissions are present", currentUser.isPermittedAll("sequences:sandflies:meremortal@lvl:*:create", "sequences:sandflies:meremortal@lvl:item34:edit"), equalTo(true));
			assertThat("expected permissions (using EL expressions) are present", currentUser
					.isPermittedAll("sequences:sandflies:" + OWNERID_EL_TEMPLATE + ":*:create", "sequences:sandflies:" + OWNERID_EL_TEMPLATE + ":item34:edit"), equalTo(true));
			assertThat("unexpected permission is not present", currentUser.isPermitted("sequences:sandflies:meremortal2@lvl:item1:view"), equalTo(false));
			assertThat("unexpected permissions are not present", currentUser.isPermittedAll("lightsaber:weild"), equalTo(false));

			currentUser.logout();
			assertThat("user logged out successfully", currentUser.isAuthenticated(), equalTo(false));

			// test LVL-based authentication using access token
			currentUser = SimpleSecurityManager.builder().build();
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));

			currentUser.login(accessToken);
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));

			principal = currentUser.getPrincipal();
			assertThat("principal is not null", principal, notNullValue());
			assertThat("principal is not empty", isNotBlank(principal));
			assertThat("principal coincides with expected", principal, equalTo(meremortalOwnerid));
			/* uncomment for additional output */
			System.out.println(" >> Currently executing user, principal: " + principal);

			assertThat("expected role is present", currentUser.hasRole("user"), equalTo(true));
			assertThat("unexpected role is not present", currentUser.hasRole("admin"), equalTo(false));
			assertThat("expected permission is present", currentUser.isPermitted("sequences:leishmania:public:item1:view"), equalTo(true));
			assertThat("expected permissions are present", currentUser.isPermittedAll("sequences:sandflies:meremortal@lvl:*:create", "sequences:sandflies:meremortal@lvl:item34:edit"), equalTo(true));
			assertThat("unexpected permission is not present", currentUser.isPermitted("sequences:sandflies:meremortal2@lvl:item1:view"), equalTo(false));
			assertThat("unexpected permissions are not present", currentUser.isPermittedAll("lightsaber:weild"), equalTo(false));

			currentUser.logout();
			assertThat("user logged out successfully", currentUser.isAuthenticated(), equalTo(false));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SecurityManagerTest.test01AuthX() failed: " + e.getMessage());
		} finally {			
			System.out.println("SecurityManagerTest.test01AuthX() has finished");
		}
	}

	@Test
	public void test02Permissions() {
		try {
			System.out.println("SecurityManagerTest.test02Permissions()");

			// test access from full access profile
			SimpleSecurityManager currentUser = SimpleSecurityManager.builder().build();
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));
			currentUser.login("adminuser", "password");
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));
			String principal = currentUser.getPrincipal();
			assertThat("principal is not null", principal, notNullValue());
			assertThat("principal is not empty", isNotBlank(principal));
			assertThat("principal coincides with expected", principal, equalTo(adminOwnerid));

			assertThat("Users are fully accessible from full access profile", 
					currentUser.isPermittedAll("users:*:*:*:view,edit,create"), equalTo(true));
			assertThat("Specific users are fully accessible from full access profile", 
					currentUser.isPermittedAll("users:*:*:username@lvl:view,edit,create"), equalTo(true));
			assertThat("Sequences are fully accessible from full access profile", 
					currentUser.isPermittedAll("sequences:*:*:*:view,edit,create"), equalTo(true));
			assertThat("Publications are fully accessible from full access profile", 
					currentUser.isPermittedAll("citations:*:*:*:view,edit,create"), equalTo(true));
			assertThat("Pipelines are fully accessible from full access profile", 
					currentUser.isPermittedAll("pipelines:*:*:*:view,edit,create"), equalTo(true));
			assertThat("Pipeline runs are fully accessible from full access profile", 
					currentUser.isPermittedAll("pipelines:runs:*:*:view,edit,create"), equalTo(true));
			assertThat("Datasets are fully accessible from full access profile", 
					currentUser.isPermittedAll("datasets:*:*:*:view,edit,create"), equalTo(true));
			assertThat("Shared datasets are fully accessible from full access profile", 
					currentUser.isPermittedAll("datasets:shared:*:*:view,edit,create"), equalTo(true));

			currentUser.logout();

			// test access from plain user profile
			currentUser = SimpleSecurityManager.builder().build();
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));
			currentUser.login(accessToken);
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));
			principal = currentUser.getPrincipal();
			assertThat("principal is not null", principal, notNullValue());
			assertThat("principal is not empty", isNotBlank(principal));
			assertThat("principal coincides with expected", principal, equalTo(meremortalOwnerid));

			assertThat("Users are not accessible for viewing from user profile", 
					currentUser.isPermitted("users:*:*:*:view"), equalTo(false));
			assertThat("Users are not accessible for editing from user profile", 
					currentUser.isPermitted("users:*:*:*:edit"), equalTo(false));
			assertThat("Users are not accessible for creation from user profile", 
					currentUser.isPermitted("users:*:*:*:create"), equalTo(false));

			assertThat("User own information is accessible for viewing from user profile", 
					currentUser.isPermitted("users:*:*:" + meremortalOwnerid + ":view"), equalTo(true));
			assertThat("User own profile is accessible for viewing and editing from user profile", 
					currentUser.isPermittedAll("users:active:profile:" + meremortalOwnerid + ":view,edit"), equalTo(true));
			assertThat("User profiles are accessible for viewing from user profile", 
					currentUser.isPermitted("users:active:profile:*:view"), equalTo(true));

			assertThat("Sequences are not accessible for reading from user profile", 
					currentUser.isPermitted("sequences:*:*:*:view"), equalTo(false));
			assertThat("Sequences are not accessible for editing from user profile", 
					currentUser.isPermitted("sequences:*:*:*:edit"), equalTo(false));
			assertThat("Sequences are not accessible for creation from user profile", 
					currentUser.isPermitted("sequences:*:*:*:create"), equalTo(false));

			assertThat("Public sequences are accessible for reading from user profile", 
					currentUser.isPermitted("sequences:*:public:*:view"), equalTo(true));
			assertThat("User own sequences are fully accessible from user profile", 
					currentUser.isPermittedAll("sequences:*:" + meremortalOwnerid + ":*:view,edit,create"), equalTo(true));

			assertThat("Publications are not accessible for reading from user profile", 
					currentUser.isPermitted("citations:*:*:*:view"), equalTo(false));
			assertThat("Publications are not accessible for editing from user profile", 
					currentUser.isPermitted("citations:*:*:*:edit"), equalTo(false));
			assertThat("Publications are not accessible for creation from user profile", 
					currentUser.isPermitted("citations:*:*:*:create"), equalTo(false));

			assertThat("Public publications are accessible for reading from user profile", 
					currentUser.isPermitted("citations:*:public:*:view"), equalTo(true));
			assertThat("User own publications are fully accessible from user profile", 
					currentUser.isPermittedAll("citations:*:" + meremortalOwnerid + ":*:view,edit,create"), equalTo(true));

			assertThat("Pipelines are not accessible for reading from user profile", 
					currentUser.isPermitted("pipelines:*:*:*:view"), equalTo(false));
			assertThat("Pipelines are not accessible for editing from user profile", 
					currentUser.isPermitted("pipelines:*:*:*:edit"), equalTo(false));
			assertThat("Pipelines are not accessible for creation from user profile", 
					currentUser.isPermitted("pipelines:*:*:*:create"), equalTo(false));

			assertThat("Public pipelines are accessible for reading from user profile", 
					currentUser.isPermitted("pipelines:*:public:*:view"), equalTo(true));
			assertThat("User own pipeline runs are fully accessible from user profile", 
					currentUser.isPermittedAll("pipelines:runs:" + meremortalOwnerid + ":*:view,edit,create"), equalTo(true));

			assertThat("Datasets are not accessible for reading from user profile", 
					currentUser.isPermitted("datasets:*:*:*:view"), equalTo(false));
			assertThat("Datasets are not accessible for editing from user profile", 
					currentUser.isPermitted("datasets:*:*:*:edit"), equalTo(false));
			assertThat("Datasets are not accessible for creation from user profile", 
					currentUser.isPermitted("datasets:*:*:*:create"), equalTo(false));

			assertThat("Public datasets are accessible for reading from user profile", 
					currentUser.isPermitted("datasets:*:public:*:view"), equalTo(true));
			assertThat("User own datasets are fully accessible from user profile", 
					currentUser.isPermittedAll("datasets:files:" + meremortalOwnerid + ":*:view,edit,create"), equalTo(true));			

			currentUser.logout();

			// test access from guest account
			currentUser = SimpleSecurityManager.builder().build();
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));
			currentUser.login("poorman", "password3");
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));
			principal = currentUser.getPrincipal();
			assertThat("principal is not null", principal, notNullValue());
			assertThat("principal is not empty", isNotBlank(principal));
			assertThat("principal coincides with expected", principal, equalTo(poormanOwnerid));

			assertThat("Users are not accessible for viewing from guest profile", 
					currentUser.isPermitted("users:*:*:*:view"), equalTo(false));
			assertThat("Users are not accessible for editing from guest profile", 
					currentUser.isPermitted("users:*:*:*:edit"), equalTo(false));
			assertThat("Users are not accessible for creation from guest profile", 
					currentUser.isPermitted("users:*:*:*:create"), equalTo(false));

			assertThat("User own information is not accessible for viewing from guest profile", 
					currentUser.isPermitted("users:*:*:" + meremortalOwnerid + ":view"), equalTo(false));
			assertThat("User own profile is not accessible for viewing or editing from guest profile", 
					currentUser.isPermitted("users:active:profile:" + poormanOwnerid + ":view,edit"), equalTo(false));
			assertThat("User profiles are not accessible for viewing from guest profile", 
					currentUser.isPermitted("users:active:profile:*:view"), equalTo(false));

			assertThat("Sequences are not accessible for reading from guest profile", 
					currentUser.isPermitted("sequences:*:*:*:view"), equalTo(false));
			assertThat("Sequences are not accessible for editing from guest profile", 
					currentUser.isPermitted("sequences:*:*:*:edit"), equalTo(false));
			assertThat("Sequences are not accessible for creation from guest profile", 
					currentUser.isPermitted("sequences:*:*:*:create"), equalTo(false));

			assertThat("Public sequences are accessible for reading from guest profile", 
					currentUser.isPermitted("sequences:*:public:*:view"), equalTo(true));
			assertThat("User own sequences are not accessible from guest profile", 
					currentUser.isPermitted("sequences:*:" + poormanOwnerid + ":*:view,edit,create"), equalTo(false));

			assertThat("Publications are not accessible for reading from guest profile", 
					currentUser.isPermitted("citations:*:*:*:view"), equalTo(false));
			assertThat("Publications are not accessible for editing from guest profile", 
					currentUser.isPermitted("citations:*:*:*:edit"), equalTo(false));
			assertThat("Publications are not accessible for creation from guest profile", 
					currentUser.isPermitted("citations:*:*:*:create"), equalTo(false));

			assertThat("Public publications are accessible for reading from guest profile", 
					currentUser.isPermitted("citations:*:public:*:view"), equalTo(true));
			assertThat("User own publications are not accessible from guest profile", 
					currentUser.isPermitted("citations:*:" + poormanOwnerid + ":*:view,edit,create"), equalTo(false));

			assertThat("Pipelines are not accessible for reading from guest profile", 
					currentUser.isPermitted("pipelines:*:*:*:view"), equalTo(false));
			assertThat("Pipelines are not accessible for editing from guest profile", 
					currentUser.isPermitted("pipelines:*:*:*:edit"), equalTo(false));
			assertThat("Pipelines are not accessible for creation from guest profile", 
					currentUser.isPermitted("pipelines:*:*:*:create"), equalTo(false));

			assertThat("Public pipelines are accessible for reading from user profile", 
					currentUser.isPermitted("pipelines:*:public:*:view"), equalTo(true));
			assertThat("User own pipeline runs are not accessible from guest profile", 
					currentUser.isPermittedAll("pipelines:runs:" + poormanOwnerid + ":*:view,edit,create"), equalTo(false));

			assertThat("Datasets are not accessible for reading from guest profile", 
					currentUser.isPermitted("datasets:*:*:*:view"), equalTo(false));
			assertThat("Datasets are not accessible for editing from guest profile", 
					currentUser.isPermitted("datasets:*:*:*:edit"), equalTo(false));
			assertThat("Datasets are not accessible for creation from guest profile", 
					currentUser.isPermitted("datasets:*:*:*:create"), equalTo(false));

			assertThat("Public datasets are accessible for reading from user profile", 
					currentUser.isPermitted("datasets:*:public:*:view"), equalTo(true));
			assertThat("User own datasets are not accessible from guest profile", 
					currentUser.isPermitted("datasets:shared:" + poormanOwnerid + ":*:view,edit,create"), equalTo(false));

			currentUser.logout();

			// test access to shared dataset
			currentUser = SimpleSecurityManager.builder().build();
			assertThat("currently executing user is not null", currentUser, notNullValue());
			assertThat("currently executing user is not authenticated", currentUser.isAuthenticated(), equalTo(false));

			currentUser.login(accessToken);
			assertThat("user logged in successfully", currentUser.isAuthenticated(), equalTo(true));

			assertThat("dataset share permission is not present", currentUser.isPermitted("datasets:files:" + poormanOwnerid + ":mysequences.xml:view"), 
					equalTo(false));
			RESOURCE_OWNER_DAO.addPermissions(meremortalOwnerid, DatasetShare.builder()
					.namespace(poormanOwnerid)
					.filename("mysequences.xml")
					.sharedNow()
					.subject(meremortalOwnerid)
					.accessType(VIEW_SHARE)
					.build());
			Thread.sleep(2000l); // allows notifications to be processed avoiding race conditions
			assertThat("dataset share viewing permission is present", currentUser.isPermitted("datasets:files:" + poormanOwnerid + ":mysequences.xml:view"), 
					equalTo(true));
			assertThat("dataset share editing permission is not present", currentUser.isPermitted("datasets:files:" + poormanOwnerid + ":mysequences.xml:view,edit"), 
					equalTo(false));

			currentUser.logout();
			assertThat("user logged out successfully", currentUser.isAuthenticated(), equalTo(false));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SecurityManagerTest.test02Permissions() failed: " + e.getMessage());
		} finally {			
			System.out.println("SecurityManagerTest.test02Permissions() has finished");
		}
	}

	@Test
	public void test03LinkedIn() {
		System.out.println("SecurityManagerTest.test03LinkedIn()");
		try {
			// test LinkedId-based authentication using access token			
			// TODO
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SecurityManagerTest.test03LinkedIn() failed: " + e.getMessage());
		} finally {			
			System.out.println("SecurityManagerTest.test03LinkedIn() has finished");
		}
	}

	private static void createResourceOwner(final User user) {
		final ResourceOwner resourceOwner = ResourceOwner.builder()
				.user(user).build();
		final WriteResult<ResourceOwner> result = RESOURCE_OWNER_DAO.insert(resourceOwner);
		assertThat("insert resource owner result is not null", result, notNullValue());
		assertThat("insert resource owner result id is not null", result.getId(), notNullValue());
		assertThat("insert resource owner result id is not empty", isNotBlank(result.getId()), equalTo(true));
		final ResourceOwner owner = RESOURCE_OWNER_DAO.useGravatar(false)
				.find(toResourceOwnerId(user.getProvider(), user.getUserid()));
		assertThat("resource owner is not null", owner, notNullValue());		
		/* uncomment for additional output */
		System.out.println(" >> Resource owner: " + owner.toString());
	}

	private static String createToken(final String ownerid, final Collection<String> scopes) {
		final AccessToken accessToken = AccessToken.builder()
				.token(generateSecret())
				.issuedAt(currentTimeMillis() / 1000l)
				.expiresIn(23l)
				.ownerId(ownerid)
				.scope(scopes)
				.build();
		TOKEN_DAO.insert(accessToken);
		final AccessToken accessToken2 = TOKEN_DAO.find(accessToken.getToken());
		assertThat("access token is not null", accessToken2, notNullValue());
		assertThat("access token coincides with original", accessToken2, equalTo(accessToken));
		/* uncomment for additional output */
		System.out.println(accessToken2.toString());
		return accessToken2.getToken();
	}	

}