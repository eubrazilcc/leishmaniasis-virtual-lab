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

import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.OWNERID_EL_TEMPLATE;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.el.PermissionElBuilder.buildPermission;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.security.User;
import eu.eubrazilcc.lvl.storage.security.el.PermissionElBuilder;

/**
 * Tests scope expression build and parsing from templates using {@link PermissionElBuilder}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PermissionElBuilderTest {

	@Test
	public void test() {
		System.out.println("PermissionElBuilderTest.test()");
		try {
			// create test dataset
			final User user = User.builder()
					.userid("uname1")
					.password("123456xyz")
					.email("email@example.org")
					.firstname("Name Middlename")
					.lastname("Lastname")
					.build();
			assertThat("user is not null", user, notNullValue());			

			// test create permission including only one user property
			String permission = buildPermission("target:${user.userid}", user);
			assertThat("permission built from username is not null", permission, notNullValue());
			assertThat("permission built from username is not empty", isNotBlank(permission), equalTo(true));
			assertThat("permission built from username coincides with expected", permission, equalTo("target:" + user.getUserid()));			
			/* uncomment for additional output */
			System.out.println(" >> Permission built from only one user property: " + permission);

			// test create permission with repeated user properties
			permission = buildPermission("target:${user.userid}:*:${user.userid}", user);
			assertThat("permission built from username is not null", permission, notNullValue());
			assertThat("permission built from username is not empty", isNotBlank(permission), equalTo(true));
			assertThat("permission built from username coincides with expected", permission, 
					equalTo("target:" + user.getUserid() + ":*:" + user.getUserid()));			
			/* uncomment for additional output */
			System.out.println(" >> Permission built from repeated user properties: " + permission);

			// test create permission including several different user properties
			permission = buildPermission("target:${user.userid}:*:${user.userid}@${user.provider}", user);
			assertThat("permission built from username is not null", permission, notNullValue());
			assertThat("permission built from username is not empty", isNotBlank(permission), equalTo(true));
			assertThat("permission built from username coincides with expected", permission, 
					equalTo("target:" + user.getUserid() + ":*:" + toResourceOwnerId(user)));			
			/* uncomment for additional output */
			System.out.println(" >> Permission built from several different user properties: " + permission);					

			// test create permission inferring owner identifier from user
			permission = buildPermission("level1:" + OWNERID_EL_TEMPLATE + ":level3", user);
			assertThat("permission built from username is not null", permission, notNullValue());
			assertThat("permission built from username is not empty", isNotBlank(permission), equalTo(true));
			assertThat("permission built from username coincides with expected", permission, 
					equalTo("level1:" + toResourceOwnerId(user) + ":level3"));			
			/* uncomment for additional output */
			System.out.println(" >> Permission built from ownerid: " + permission);

			// test create permission from template with no EL parameters
			permission = buildPermission("level1:level2:level3", user);
			assertThat("permission built from username is not null", permission, notNullValue());
			assertThat("permission built from username is not empty", isNotBlank(permission), equalTo(true));
			assertThat("permission built from username coincides with expected", permission, 
					equalTo("level1:level2:level3"));			
			/* uncomment for additional output */
			System.out.println(" >> Permission built from template with no EL params: " + permission);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PermissionElBuilderTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PermissionElBuilderTest.test() has finished");
		}
	}

}