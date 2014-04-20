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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.PendingUser;
import eu.eubrazilcc.lvl.storage.oauth2.User;
import eu.eubrazilcc.lvl.storage.oauth2.dao.PendingUserDAO;

/**
 * Tests pending user collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingUserCollectionTest {

	@Test
	public void test() {
		System.out.println("PendingUserCollectionTest.test()");
		try {
			final Collection<String> scopes = newArrayList("scope1", "scope2");
			// insert
			final PendingUser pendingUser = PendingUser.builder()
					.id("username")
					.expiresIn(1000l)
					.issuedAt(2000l)
					.activationCode("1234567890abcDEF")
					.user(User.builder()
							.username("username")
							.password("password")
							.email("username@example.com")
							.fullname("Fullname")
							.scopes(scopes)
							.build()).build();
			PendingUserDAO.INSTANCE.insert(pendingUser);
			
			// find
			PendingUser pendingUser2 = PendingUserDAO.INSTANCE.find(pendingUser.getPendingUserId());
			assertThat("pending user is not null", pendingUser2, notNullValue());
			assertThat("pending user coincides with original", pendingUser2, equalTo(pendingUser));
			System.out.println(pendingUser2.toString());
			
			// update
			pendingUser.getUser().setPassword("new_password");
			PendingUserDAO.INSTANCE.update(pendingUser);
			
			// find after update
			pendingUser2 = PendingUserDAO.INSTANCE.find(pendingUser.getPendingUserId());
			assertThat("pending user is not null", pendingUser2, notNullValue());
			assertThat("pending user coincides with original", pendingUser2, equalTo(pendingUser));
			System.out.println(pendingUser2.toString());
			
			// check validity using pending user Id and username
			boolean validity = PendingUserDAO.INSTANCE.isValid(pendingUser.getPendingUserId(), 
					pendingUser.getUser().getUsername(), 
					pendingUser.getActivationCode(), 
					false);
			assertThat("pending user is valid (using owner Id & username)", validity);

			// check validity using email address
			validity = PendingUserDAO.INSTANCE.isValid(null, 
					pendingUser.getUser().getEmail(), 
					pendingUser.getActivationCode(), 
					true);
			assertThat("pending user is valid (using email)", validity);			

			// remove
			PendingUserDAO.INSTANCE.delete(pendingUser.getPendingUserId());

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final PendingUser pendingUser3 = PendingUser.builder()
						.id(Integer.toString(i))
						.expiresIn(1000l)
						.issuedAt(2000l)
						.activationCode("1234567890abcDEF")
						.user(User.builder()
								.username(Integer.toString(i))
								.password("password")
								.email("username" + i + "@example.com")
								.fullname("Fullname")
								.scopes(scopes)
								.build()).build();								
				ids.add(pendingUser3.getPendingUserId());
				PendingUserDAO.INSTANCE.insert(pendingUser3);
			}
			final int size = 3;
			int start = 0;
			List<PendingUser> pendingUsers = null;
			final MutableLong count = new MutableLong(0l);
			do {
				pendingUsers = PendingUserDAO.INSTANCE.list(start, size, count);
				if (pendingUsers.size() != 0) {
					System.out.println("Paging " + start + " - " + pendingUsers.size() + " of " + count.getValue());
				}
				start += pendingUsers.size();
			} while (!pendingUsers.isEmpty());
			for (final String id2 : ids) {			
				PendingUserDAO.INSTANCE.delete(id2);
			}
			PendingUserDAO.INSTANCE.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PendingUserCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PendingUserCollectionTest.test() has finished");
		}
	}

}