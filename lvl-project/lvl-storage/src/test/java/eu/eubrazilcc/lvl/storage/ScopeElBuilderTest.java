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

import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.PATH_SEPARATOR;
import static eu.eubrazilcc.lvl.storage.oauth2.security.el.ScopeElBuilder.buildScope;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.User;

/**
 * Tests scope expression build and parsing from templates using {@link ScopeElBuilderTest}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ScopeElBuilderTest {

	@Test
	public void test() {
		System.out.println("ScopeElBuilderTest.test()");
		try {
			// create test dataset
			final User user = User.builder()
					.username("uname1")
					.password("123456xyz")
					.email("email@example.org")
					.fullname("Name Middlename Lastname")
					.build();
			assertThat("user is not null", user, notNullValue());
			final String scope = "target_scope";

			// test create scope with username
			String scope2 = buildScope(scope, user);
			assertThat("scope built from username is not null", scope2, notNullValue());
			assertThat("scope built from username is not empty", isNotBlank(scope2), equalTo(true));
			assertThat("scope built from username coincides with expected", scope2, equalTo(scope + PATH_SEPARATOR + user.getUsername()));			
			/* uncomment for additional output */
			System.out.println(" >> Scope built from username: " + scope2);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ScopeElBuilderTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ScopeElBuilderTest.test() has finished");
		}
	}

}