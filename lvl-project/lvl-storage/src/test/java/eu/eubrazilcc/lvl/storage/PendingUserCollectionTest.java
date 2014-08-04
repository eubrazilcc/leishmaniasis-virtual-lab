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
import static eu.eubrazilcc.lvl.storage.oauth2.dao.PendingUserDAO.PENDING_USER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.PendingUserDAO.updatePassword;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.oauth2.PendingUser;
import eu.eubrazilcc.lvl.storage.oauth2.User;

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

			// insert (no salt)
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
			WriteResult<PendingUser> result = PENDING_USER_DAO.insert(pendingUser);
			assertThat("insert pending user result is not null", result, notNullValue());
			assertThat("insert pending user result id is not null", result.getId(), notNullValue());
			assertThat("insert pending user result id is not empty", isNotBlank(result.getId()), equalTo(true));
			assertThat("insert pending user result element is not null", result.getElement(), notNullValue());
			assertThat("insert pending user result user is not null", result.getElement().getUser(), notNullValue());
			assertThat("insert pending user result hashed password", result.getElement().getUser().getPassword(), notNullValue());
			assertThat("insert pending user result hashed password", isNotBlank(result.getElement().getUser().getPassword()), equalTo(true));			
			assertThat("insert pending user result salt", result.getElement().getUser().getSalt(), notNullValue());
			assertThat("insert pending user result salt", isNotBlank(result.getElement().getUser().getSalt()), equalTo(true));
			assertThat("inserted pending user coincides with original (ignoring password & salt)", 
					pendingUser.equalsToUnprotected(result.getElement()), equalTo(true));
			final PendingUser hashed = result.getElement();

			// find (no salt)
			PendingUser pendingUser2 = PENDING_USER_DAO.find(pendingUser.getPendingUserId());
			assertThat("pending user is not null", pendingUser2, notNullValue());
			assertThat("pending user coincides with original", pendingUser2, equalTo(hashed));
			System.out.println(pendingUser2.toString());

			// update
			updatePassword(hashed, "new_password");
			PENDING_USER_DAO.update(hashed);

			// find after update
			pendingUser2 = PENDING_USER_DAO.find(pendingUser.getPendingUserId());
			assertThat("pending user is not null", pendingUser2, notNullValue());
			assertThat("pending user coincides with original", pendingUser2, equalTo(hashed));
			System.out.println(pendingUser2.toString());

			// check validity using pending user Id and username
			boolean validity = PENDING_USER_DAO.isValid(pendingUser.getPendingUserId(), 
					pendingUser.getUser().getUsername(), 
					pendingUser.getActivationCode(), 
					false);
			assertThat("pending user is valid (using owner Id & username)", validity);

			// check validity using email address
			validity = PENDING_USER_DAO.isValid(null, 
					pendingUser.getUser().getEmail(), 
					pendingUser.getActivationCode(), 
					true);
			assertThat("pending user is valid (using email)", validity);			

			// remove
			PENDING_USER_DAO.delete(pendingUser.getPendingUserId());

			// insert (with salt)
			result = PENDING_USER_DAO.insert(hashed);
			assertThat("insert pending user result (with salt) is not null", result, notNullValue());
			assertThat("insert pending user result (with salt) id is not null", result.getId(), notNullValue());
			assertThat("insert pending user result (with salt) id is not empty", isNotBlank(result.getId()), equalTo(true));

			// find (with salt)
			pendingUser2 = PENDING_USER_DAO.find(hashed.getPendingUserId());
			assertThat("pending user (with salt) is not null", pendingUser2, notNullValue());
			assertThat("pending user (with salt) coincides with original", pendingUser2, equalTo(hashed));
			System.out.println(pendingUser2.toString());

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final PendingUser pendingUser4 = PendingUser.builder()
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
				ids.add(pendingUser4.getPendingUserId());
				PENDING_USER_DAO.insert(pendingUser4);
			}
			final int size = 3;
			int start = 0;
			List<PendingUser> pendingUsers = null;
			final MutableLong count = new MutableLong(0l);
			do {
				pendingUsers = PENDING_USER_DAO.list(start, size, count);
				if (pendingUsers.size() != 0) {
					System.out.println("Paging " + start + " - " + pendingUsers.size() + " of " + count.getValue());
				}
				start += pendingUsers.size();
			} while (!pendingUsers.isEmpty());
			for (final String id2 : ids) {			
				PENDING_USER_DAO.delete(id2);
			}
			PENDING_USER_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PendingUserCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PendingUserCollectionTest.test() has finished");
		}
	}

}