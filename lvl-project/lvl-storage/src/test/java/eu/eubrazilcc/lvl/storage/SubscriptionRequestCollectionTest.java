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
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.storage.support.dao.SubscriptionRequestDAO.SUBSCRIPTION_REQ_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.support.SubscriptionRequest;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper;

/**
 * Tests {@link SubscriptionRequest} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SubscriptionRequestCollectionTest {

	@Test
	public void test() {
		System.out.println("SubscriptionRequestCollectionTest.test()");
		try {
			// insert
			final SubscriptionRequest request = SubscriptionRequest.builder()
					.newId()
					.email("username@example.com")
					.requested(new SimpleDateFormat("yyyy-mm-dd").parse("2015-01-01"))
					.channels(newHashSet("mailing list"))					
					.build();

			// TODO
			final String payload = MongoDBJsonMapper.JSON_MAPPER.writeValueAsString(request);
			System.err.println("\n\n >> PAYLOAD:\n\n" + payload + "\n");
			// TODO

			WriteResult<SubscriptionRequest> writeResult = SUBSCRIPTION_REQ_DAO.insert(request);
			assertThat("insert write result is not null", writeResult, notNullValue());
			assertThat("insert write result Id is not null", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty", isNotBlank(writeResult.getId()), equalTo(true));

			// insert ignoring duplicates			
			try {
				SUBSCRIPTION_REQ_DAO.insert(request, true);
				fail("Expected Exception due to duplicate insertion");
			} catch (RuntimeException expected) {
				System.out.println("Expected exception caught: " + expected.getClass());
			}

			// find
			SubscriptionRequest request2 = SUBSCRIPTION_REQ_DAO.find(request.getId());
			assertThat("request is not null", request2, notNullValue());
			assertThat("request coincides with original", request2, equalTo(request));
			System.out.println(request2.toString());

			// update
			request.setFulfilled(new SimpleDateFormat("yyyy-mm-dd").parse("2015-01-02"));
			SUBSCRIPTION_REQ_DAO.update(request);

			// find after update
			request2 = SUBSCRIPTION_REQ_DAO.find(request.getId());
			assertThat("request is not null", request2, notNullValue());
			assertThat("request coincides with original", request2, equalTo(request));
			System.out.println(request2.toString());

			// remove
			SUBSCRIPTION_REQ_DAO.delete(request.getId());
			final long numRecords = SUBSCRIPTION_REQ_DAO.count();
			assertThat("number of requests stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {				
				final SubscriptionRequest request3 = SubscriptionRequest.builder()
						.id("request-" + i)
						.email("username" + i + "@example.com")
						.requested(new Date())
						.channels(newHashSet("mailing list"))									
						.build();
				ids.add(request3.getId());
				SUBSCRIPTION_REQ_DAO.insert(request3);
			}
			final int size = 3;
			int start = 0;
			List<SubscriptionRequest> requests = null;
			final MutableLong count = new MutableLong(0l);
			do {
				requests = SUBSCRIPTION_REQ_DAO.list(start, size, null, null, null, count);
				if (requests.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + requests.size() + " of " + count.getValue() + " items");
				}
				start += requests.size();
			} while (!requests.isEmpty());
			for (final String id2 : ids) {			
				SUBSCRIPTION_REQ_DAO.delete(id2);
			}
			SUBSCRIPTION_REQ_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SubscriptionRequestCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SubscriptionRequestCollectionTest.test() has finished");
		}
	}

}