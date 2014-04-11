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

import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.AuthCode;
import eu.eubrazilcc.lvl.storage.oauth2.dao.AuthCodeDAO;

/**
 * Tests OAuth2 authorization code collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class AuthCodeCollectionTest {

	@Test
	public void test() {
		System.out.println("AuthCodeCollectionTest.test()");
		try {
			// insert
			final AuthCode authCode = AuthCode.builder()
					.code("1234567890abcdEFGhiJKlMnOpqrstUVWxyZ")
					.issuedAt(System.currentTimeMillis() / 1000l)
					.expiresIn(23l)
					.build();			
			AuthCodeDAO.INSTANCE.insert(authCode);
			// find
			AuthCode authCode2 = AuthCodeDAO.INSTANCE.find(authCode.getCode());
			assertThat("authorization code is not null", authCode2, notNullValue());
			assertThat("authorization code coincides with original", authCode2, equalTo(authCode));
			System.out.println(authCode2.toString());
			// update
			authCode.setExpiresIn(3600l);
			AuthCodeDAO.INSTANCE.update(authCode);
			// find after update
			authCode2 = AuthCodeDAO.INSTANCE.find(authCode.getCode());
			assertThat("authorization code is not null", authCode2, notNullValue());
			assertThat("authorization code coincides with original", authCode2, equalTo(authCode));
			System.out.println(authCode2.toString());
			// check validity
			final boolean validity = AuthCodeDAO.INSTANCE.isValid(authCode.getCode());
			assertThat("authorization code is valid", validity);
			// remove
			AuthCodeDAO.INSTANCE.delete(authCode.getCode());
			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final AuthCode authCode3 = AuthCode.builder()
						.code(Integer.toString(i)).build();
				ids.add(authCode3.getCode());
				AuthCodeDAO.INSTANCE.insert(authCode3);
			}
			final int size = 3;
			int start = 0;
			List<AuthCode> authCodes = null;
			final MutableLong count = new MutableLong(0l);
			do {
				authCodes = AuthCodeDAO.INSTANCE.list(start, size, count);
				if (authCodes.size() != 0) {
					System.out.println("Paging " + start + " - " + authCodes.size() + " of " + count.getValue());
				}
				start += authCodes.size();
			} while (!authCodes.isEmpty());
			for (final String id2 : ids) {
				AuthCodeDAO.INSTANCE.delete(id2);
			}
			AuthCodeDAO.INSTANCE.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("AuthCodeCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("AuthCodeCollectionTest.test() has finished");
		}
	}

}