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

import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.all;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asList;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.defaultScope;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.inherit;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.isAccessible;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.user;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager;

/**
 * Tests scope manager.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ScopeManagerTest {

	@Test
	public void test() {
		System.out.println("ScopeManagerTest.test()");
		try {
			final String username = "username1";
			final String fullAccessProfile = all();
			final String userProfile = user(username);
			final String defaultProfile = defaultScope();

			// test access from full access profile
			assertThat("Users are accessible from full access profile (write included)", 
					isAccessible(ScopeManager.USERS, asList(fullAccessProfile), true));
			assertThat("Users are accessible from full access profile (write not included)", 
					isAccessible(ScopeManager.USERS, asList(fullAccessProfile), false));
			
			assertThat("Users are accessible from full access profile (write included)", 
					isAccessible(inherit(ScopeManager.USERS, ScopeManager.ALL), asList(fullAccessProfile), true));
			assertThat("Users are accessible from full access profile (write not included)", 
					isAccessible(inherit(ScopeManager.USERS, ScopeManager.ALL), asList(fullAccessProfile), false));
			
			assertThat("Specific users are accessible from full access profile (write included)", 
					isAccessible(inherit(ScopeManager.USERS, username), asList(fullAccessProfile), true));
			assertThat("Specific users are accessible from full access profile (write not included)", 
					isAccessible(inherit(ScopeManager.USERS, username), asList(fullAccessProfile), false));

			assertThat("Sequences are accessible from full access profile (write included)", 
					isAccessible(ScopeManager.SEQUENCES, asList(fullAccessProfile), true));
			assertThat("Sequences are accessible from full access profile (write not included)", 
					isAccessible(ScopeManager.SEQUENCES, asList(fullAccessProfile), false));

			// test access from user profile
			assertThat("Users are not accessible from user profile (write included)", 
					!isAccessible(ScopeManager.USERS, asList(userProfile), true));
			assertThat("Users are not accessible from user profile (write not included)", 
					!isAccessible(ScopeManager.USERS, asList(userProfile), false));

			assertThat("User own information is accessible from user profile (write included)", 
					isAccessible(inherit(ScopeManager.USERS, username), asList(userProfile), true));
			assertThat("User own information is accessible from user profile (write not included)", 
					isAccessible(inherit(ScopeManager.USERS, username), asList(userProfile), false));

			assertThat("Sequences are not accessible from user profile (write included)", 
					!isAccessible(ScopeManager.SEQUENCES, asList(userProfile), true));
			assertThat("Sequences are accessible from user profile (write not included)", 
					isAccessible(ScopeManager.SEQUENCES, asList(userProfile), false));
			
			// test access from default profile
			assertThat("Users are not accessible from default profile (write included)", 
					!isAccessible(ScopeManager.USERS, asList(defaultProfile), true));
			assertThat("Users are not accessible from default profile (write not included)", 
					!isAccessible(ScopeManager.USERS, asList(defaultProfile), false));

			assertThat("Sequences are not accessible from default profile (write included)", 
					!isAccessible(ScopeManager.SEQUENCES, asList(defaultProfile), true));
			assertThat("Sequences are accessible from default profile (write not included)", 
					isAccessible(ScopeManager.SEQUENCES, asList(defaultProfile), false));
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ScopeManagerTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ScopeManagerTest.test() has finished");
		}
	}

}