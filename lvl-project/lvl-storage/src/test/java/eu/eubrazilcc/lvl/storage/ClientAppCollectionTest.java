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

import eu.eubrazilcc.lvl.core.util.NamingUtils;
import eu.eubrazilcc.lvl.storage.oauth2.ClientApp;
import eu.eubrazilcc.lvl.storage.oauth2.dao.ClientAppDAO;

/**
 * Tests OAuth2 client application collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ClientAppCollectionTest {

	@Test
	public void test() {
		System.out.println("ClientAppCollectionTest.test()");
		try {
			// insert
			final ClientApp clientApp = ClientApp.builder()
					.name("client_name")
					.url("http://localhost/client")
					.description("Sample client")
					.icon("http://localhost/client/icon")
					.redirectURL("http://localhost/client/redirect")
					.clientId(NamingUtils.toAsciiSafeName("client_name"))
					.clientSecret("1234567890abcdEFGhiJKlMnOpqrstUVWxyZ")
					.issuedAt(1000l)
					.expiresIn(2000l)
					.build();
			ClientAppDAO.INSTANCE.insert(clientApp);
			// find
			ClientApp clientApp2 = ClientAppDAO.INSTANCE.find(clientApp.getClientId());
			assertThat("client application is not null", clientApp2, notNullValue());
			assertThat("client application coincides with original", clientApp2, equalTo(clientApp));
			System.out.println(clientApp2.toString());
			// update
			clientApp.setExpiresIn(4000l);
			ClientAppDAO.INSTANCE.update(clientApp);
			// check validity
			boolean validity = ClientAppDAO.INSTANCE.isValid(clientApp.getClientId());
			assertThat("client application is valid (secret excluded)", validity);
			validity = ClientAppDAO.INSTANCE.isValid(clientApp.getClientId(), clientApp.getClientSecret());
			assertThat("client application is valid (secret included)", validity);
			// find after update
			clientApp2 = ClientAppDAO.INSTANCE.find(clientApp.getClientId());
			assertThat("client application is not null", clientApp2, notNullValue());
			assertThat("client application coincides with original", clientApp2, equalTo(clientApp));
			System.out.println(clientApp2.toString());
			// remove
			ClientAppDAO.INSTANCE.delete(clientApp.getClientId());
			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {				
				final ClientApp clientApp3 = ClientApp.builder()
						.name(Integer.toString(i))
						.url("http://localhost/client")
						.description("Sample client")
						.icon("http://localhost/client/icon")
						.redirectURL("http://localhost/client/redirect")
						.clientId(NamingUtils.toAsciiSafeName((Integer.toString(i))))
						.clientSecret("1234567890abcdEFGhiJKlMnOpqrstUVWxyZ")
						.issuedAt(1000l)
						.expiresIn(2000l)						
						.build();
				ids.add(clientApp3.getClientId());
				ClientAppDAO.INSTANCE.insert(clientApp3);
			}
			final int size = 3;
			int start = 0;
			List<ClientApp> clientApps = null;
			final MutableLong count = new MutableLong(0l);
			do {
				clientApps = ClientAppDAO.INSTANCE.list(start, size, count);
				if (clientApps.size() != 0) {
					System.out.println("Paging " + start + " - " + clientApps.size() + " of " + count.getValue());
				}
				start += clientApps.size();
			} while (!clientApps.isEmpty());
			for (final String id2 : ids) {			
				ClientAppDAO.INSTANCE.delete(id2);
			}
			ClientAppDAO.INSTANCE.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ClientAppCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ClientAppCollectionTest.test() has finished");
		}
	}

}