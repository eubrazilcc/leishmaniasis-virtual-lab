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
import static eu.eubrazilcc.lvl.storage.oauth2.dao.LinkedInStateDAO.LINKEDIN_STATE_DAO;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.linkedin.LinkedInState;

/**
 * Tests {@link LinkedInState} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LinkedInStateCollectionTest {

	@Test
	public void test() {
		System.out.println("LinkedInStateCollectionTest.test()");
		try {
			// insert
			final long issuedAt = currentTimeMillis() / 1000l;
			final LinkedInState state = LinkedInState.builder()
					.state("1234567890abcdEFGhiJKlMnOpqrstUVWxyZ")
					.issuedAt(issuedAt)
					.expiresIn(23l)
					.build();
			LINKEDIN_STATE_DAO.insert(state);

			// find
			LinkedInState state2 = LINKEDIN_STATE_DAO.find(state.getState());
			assertThat("linkedin state is not null", state2, notNullValue());
			assertThat("linkedin state coincides with original", state2, equalTo(state));
			System.out.println(state2.toString());

			// update
			state.setExpiresIn(604800l);
			LINKEDIN_STATE_DAO.update(state);

			// find after update
			state2 = LINKEDIN_STATE_DAO.find(state.getState());
			assertThat("linkedin state is not null", state2, notNullValue());
			assertThat("linkedin state coincides with original", state2, equalTo(state));
			System.out.println(state2.toString());

			// list by issued date
			List<LinkedInState> states = LINKEDIN_STATE_DAO.listByIssuedDate(new Date(issuedAt));
			assertThat("linkedin states are not null", states, notNullValue());
			assertThat("linkedin states are not empty", !states.isEmpty(), equalTo(true));
			assertThat("number of linkedin states coincides with expected", states.size(), equalTo(1));
			assertThat("linkedin states coincide with original", states.get(0), equalTo(state));			

			// check validity			
			boolean validity = LINKEDIN_STATE_DAO.isValid(state.getState());
			assertThat("linkedin state is valid", validity, equalTo(true));

			// remove
			LINKEDIN_STATE_DAO.delete(state.getState());
			final long numRecords = LINKEDIN_STATE_DAO.count();
			assertThat("number of linkedin states stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final LinkedInState state3 = LinkedInState.builder()
						.state(Integer.toString(i)).build();
				ids.add(state3.getState());
				LINKEDIN_STATE_DAO.insert(state3);
			}
			final int size = 3;
			int start = 0;
			states = null;
			final MutableLong count = new MutableLong(0l);
			do {
				states = LINKEDIN_STATE_DAO.list(start, size, null, null, null, count);
				if (states.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + states.size() + " of " + count.getValue() + " items");
				}
				start += states.size();
			} while (!states.isEmpty());
			for (final String id2 : ids) {			
				LINKEDIN_STATE_DAO.delete(id2);
			}
			LINKEDIN_STATE_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LinkedInStateCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("LinkedInStateCollectionTest.test() has finished");
		}
	}

}