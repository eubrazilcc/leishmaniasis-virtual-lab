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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;

/**
 * Tests OAuth2 access token collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class TokenCollectionTest {

	@Test
	public void test() {
		System.out.println("TokenCollectionTest.test()");
		try {
			final Collection<String> scopes = newArrayList("scope1", "scope2", "scope2/username1", "scope3,a");
			// insert
			final AccessToken accessToken = AccessToken.builder()
					.token("1234567890abcdEFGhiJKlMnOpqrstUVWxyZ")
					.issuedAt(currentTimeMillis() / 1000l)
					.expiresIn(23l)
					.ownerId("username1")
					.scope(scopes)
					.build();
			TOKEN_DAO.insert(accessToken);

			// find
			AccessToken accessToken2 = TOKEN_DAO.find(accessToken.getToken());
			assertThat("access token is not null", accessToken2, notNullValue());
			assertThat("access token coincides with original", accessToken2, equalTo(accessToken));
			System.out.println(accessToken2.toString());

			// update
			accessToken.setExpiresIn(604800l);
			TOKEN_DAO.update(accessToken);

			// find after update
			accessToken2 = TOKEN_DAO.find(accessToken.getToken());
			assertThat("access token is not null", accessToken2, notNullValue());
			assertThat("access token coincides with original", accessToken2, equalTo(accessToken));
			System.out.println(accessToken2.toString());

			// list by owner Id
			List<AccessToken> accessTokens = TOKEN_DAO.listByOwnerId(accessToken.getOwnerId());
			assertThat("access tokens are not null", accessTokens, notNullValue());
			assertThat("access tokens are not empty", !accessTokens.isEmpty(), equalTo(true));
			assertThat("number of access tokens coincides with expected", accessTokens.size(), equalTo(1));
			assertThat("access tokens coincide with original", accessTokens.get(0), equalTo(accessToken));			

			// check validity			
			boolean validity = TOKEN_DAO.isValid(accessToken.getToken());
			assertThat("access token is valid", validity, equalTo(true));
			
			final AtomicReference<String> ownerIdRef = new AtomicReference<String>();
			validity = TOKEN_DAO.isValid(accessToken.getToken(), ownerIdRef);
			assertThat("access token is valid using target scope", validity, equalTo(true));
			assertThat("owner id coincides is not null", ownerIdRef.get(), notNullValue());
			assertThat("owner id coincides with expected", ownerIdRef.get(), equalTo(accessToken.getOwnerId()));
			
			// remove
			TOKEN_DAO.delete(accessToken.getToken());
			final long numRecords = TOKEN_DAO.count();
			assertThat("number of access tokens stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final AccessToken accessToken3 = AccessToken.builder()
						.token(Integer.toString(i)).build();
				ids.add(accessToken3.getToken());
				TOKEN_DAO.insert(accessToken3);
			}
			final int size = 3;
			int start = 0;
			accessTokens = null;
			final MutableLong count = new MutableLong(0l);
			do {
				accessTokens = TOKEN_DAO.list(start, size, null, null, null, count);
				if (accessTokens.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + accessTokens.size() + " of " + count.getValue() + " items");
				}
				start += accessTokens.size();
			} while (!accessTokens.isEmpty());
			for (final String id2 : ids) {			
				TOKEN_DAO.delete(id2);
			}
			TOKEN_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("TokenCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("TokenCollectionTest.test() has finished");
		}
	}

}