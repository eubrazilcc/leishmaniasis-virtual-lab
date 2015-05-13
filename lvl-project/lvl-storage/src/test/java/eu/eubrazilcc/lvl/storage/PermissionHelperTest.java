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

package eu.eubrazilcc.lvl.storage;

import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.NUMBER_OF_PERMISSIONS_GRANTED_TO_REGULAR_USERS;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.PERMISSIONS_SEPARATOR;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asOAuthString;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionSet;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.userPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.datasetSharePermission;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.DatasetShare;

/**
 * Tests scope manager.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PermissionHelperTest {	

	@Test
	public void test() {
		System.out.println("PermissionHelperTest.test()");
		try {
			// create test dataset
			final String ownerid = "username@lvl";
			assertThat("ownerid is not null", ownerid, notNullValue());
			assertThat("ownerid is not empty", isNotBlank(ownerid));

			String permissions = userPermissions(ownerid);
			assertThat("permissions are not null", permissions, notNullValue());
			assertThat("permissions are not empty", isNotBlank(permissions));
			/* uncomment for additional output */
			System.out.println(" >> Permissions: " + permissions);		

			// test role and permission transformation
			final List<String> list = asPermissionList(permissions);
			assertThat("permissions list is not null", list, notNullValue());
			assertThat("permissions list size concides with expected", list.size(), equalTo(NUMBER_OF_PERMISSIONS_GRANTED_TO_REGULAR_USERS));									
			/* uncomment for additional output */
			System.out.println(" >> Permissions list: " + list);

			final Set<String> set = asPermissionSet(permissions);
			assertThat("permissions set is not null", set, notNullValue());
			assertThat("permissions set size concides with expected", set.size(), equalTo(NUMBER_OF_PERMISSIONS_GRANTED_TO_REGULAR_USERS));
			/* uncomment for additional output */
			System.out.println(" >> Permissions set: " + set);

			final String string = asOAuthString(set, true);
			assertThat("permissions string is null", string, notNullValue());
			assertThat("permissions string is not empty", isNotBlank(string));
			assertThat("number of permissions concides with expected", string.split(String.valueOf(PERMISSIONS_SEPARATOR)).length, 
					equalTo(NUMBER_OF_PERMISSIONS_GRANTED_TO_REGULAR_USERS));
			/* uncomment for additional output */
			System.out.println(" >> Permissions string: " + string);

			// test datasets share permission
			final DatasetShare share = DatasetShare.builder()
					.namespace("owner@idp1")
					.filename("filename")
					.subject("username@idp2")
					.sharedNow()
					.accessType(EDIT_SHARE)
					.build();
			permissions = datasetSharePermission(share);
			assertThat("data share permissions are not null", permissions, notNullValue());
			assertThat("data share permissions are not empty", isNotBlank(permissions));
			assertThat("data share permissions coincide with expected", permissions, equalTo("datasets:files:owner@idp1:filename:view,edit"));
			/* uncomment for additional output */
			System.out.println(" >> Data share permissions: " + permissions);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PermissionHelperTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PermissionHelperTest.test() has finished");
		}
	}

}