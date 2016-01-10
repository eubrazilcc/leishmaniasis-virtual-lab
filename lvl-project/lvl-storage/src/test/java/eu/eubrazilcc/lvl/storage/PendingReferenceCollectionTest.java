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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.dao.PendingReferenceDAO.DB_PREFIX;
import static eu.eubrazilcc.lvl.storage.dao.PendingReferenceDAO.PENDING_REFERENCE_DAO;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.PendingReference;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.SubmissionRequest.SubmissionResolution;
import eu.eubrazilcc.lvl.core.SubmissionRequest.SubmissionStatus;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests pending references collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingReferenceCollectionTest extends LeishvlTestCase {

	public PendingReferenceCollectionTest() {
		super(false);
	}

	@Test
	public void test() {
		printMsg("PendingReferenceCollectionTest.test()");
		try {
			// insert
			final PendingReference pendingRef = PendingReference.builder()
					.id("LVLREF0001")
					.namespace("username")
					.pubmedId("ADGJ87950")
					.seqids(newHashSet("gb:ABC12345678"))
					.sampleids(newHashSet("colfleb:123", "isciii:456"))
					.build();
			final WriteResult<PendingReference> ack = PENDING_REFERENCE_DAO.insert(pendingRef);
			assertThat("write ack is not null", ack, notNullValue());
			assertThat("database id is not empty", trim(ack.getId()), allOf(notNullValue(), not(equalTo(""))));
			printMsg(" >> New record inserted: id=" + ack.getId() + ", record=" + toJson(pendingRef, JSON_PRETTY_PRINTER));			

			// find
			PendingReference pendingRef2 = PENDING_REFERENCE_DAO.find(pendingRef.getId());
			assertThat("pendingRef is not null", pendingRef2, notNullValue());
			assertThat("pendingRef coincides with original", pendingRef2, equalTo(pendingRef));
			printMsg(" >> Found:\n" + toJson(pendingRef2, JSON_PRETTY_PRINTER));

			// duplicates are not allowed
			try {
				PENDING_REFERENCE_DAO.insert(pendingRef2);
				fail("Duplicate pendingRefs are not allowed");
			} catch (Exception e) {
				printMsg("Exception caught while trying to insert a duplicate pendingRef");
			}

			// insert element with hard link			
			final PendingReference pendingRef1 = PendingReference.builder()
					.links(newArrayList(Link.fromUri("http://example.com/pending/citation/lvl0002").rel(SELF).type(APPLICATION_JSON).build()))
					.id("LVLREF0002")
					.namespace("username")
					.pubmedId("TEFGR87950")
					.sampleids(newHashSet("isciii:789"))
					.build();
			PENDING_REFERENCE_DAO.insert(pendingRef1);
			pendingRef1.setLinks(null);			

			// find element after insertion (hard link should be removed)
			pendingRef2 = PENDING_REFERENCE_DAO.find(pendingRef1.getId());
			assertThat("pendingRef inserted with hard link is not null", pendingRef2, notNullValue());
			assertThat("pendingRef inserted with hard link coincides with expected", pendingRef2, equalTo(pendingRef1));
			printMsg(" >> Found element inserted with links:\n" + toJson(pendingRef2, JSON_PRETTY_PRINTER));

			// find all
			final List<PendingReference> all = PENDING_REFERENCE_DAO.findAll();
			printMsg(" >> List all:\n" + toJson(all, JSON_PRETTY_PRINTER));

			// delete
			PENDING_REFERENCE_DAO.delete(pendingRef1.getId());

			// update
			pendingRef.getSeqids().add("gb:DEF09876");
			pendingRef.setAssignedTo("curator@lvl");
			pendingRef.setResolution(SubmissionResolution.ACCEPTED);
			pendingRef.setStatus(SubmissionStatus.CLOSED);
			pendingRef.setAllocatedCollection("pendingReference");
			pendingRef.setAllocatedId("123");
			PENDING_REFERENCE_DAO.update(pendingRef);

			// find after update
			pendingRef2 = PENDING_REFERENCE_DAO.find(pendingRef.getId());
			assertThat("pendingRef is not null", pendingRef2, notNullValue());
			assertThat("pendingRef coincides with original", pendingRef2, equalTo(pendingRef));
			printMsg(" >> Found element after update:\n" + toJson(pendingRef2, JSON_PRETTY_PRINTER));

			// remove
			PENDING_REFERENCE_DAO.delete(pendingRef.getId());
			final long numRecords = PENDING_REFERENCE_DAO.count();
			assertThat("number of pendingRef stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random rand = new Random();
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {				
				final PendingReference pendingRef3 = PendingReference.builder()
						.namespace("username")
						.id("LVLREF000" + Integer.toString(i))
						.pubmedId("XYZ" + i)						
						.build();
				if (i%2 == 0) pendingRef3.setStatus(SubmissionStatus.values()[rand.nextInt(SubmissionStatus.values().length)]);
				ids.add(pendingRef3.getId());
				PENDING_REFERENCE_DAO.insert(pendingRef3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			List<PendingReference> pendingRefs = null;
			final MutableLong count = new MutableLong(0l);
			do {
				pendingRefs = PENDING_REFERENCE_DAO.list(start, size, null, null, null, count);
				if (pendingRefs.size() != 0) {
					printMsg("Paging: first item " + start + ", showing " + pendingRefs.size() + " of " + count.getValue() + " items");					
				}
				start += pendingRefs.size();
			} while (!pendingRefs.isEmpty());
			
			// find submitted records only
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, null, null, null, null, "username", true);
			assertThat("submitted records only pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of submitted records only pendingRef coincides with expected", pendingRefs.size(), equalTo(6));
			for (final PendingReference pr : pendingRefs) {
				assertThat("submitted records contains a valid status", pr.getStatus(), notNullValue());
			}

			// filter: PMID search
			ImmutableMap<String, String> filter = of("pmid", "XYZ1");
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of filtered pendingRef coincides with expected", pendingRefs.size(), equalTo(1));

			// invalid filter
			filter = of("filter_name", "filter_content");
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of filtered pendingRef coincides with expected", pendingRefs.size(), equalTo(0));

			// sorting by PMID in ascending order
			Sorting sorting = Sorting.builder()
					.field("pmid")
					.order(Order.ASC)
					.build();
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of sorted pendingRef coincides with expected", pendingRefs.size(), equalTo(numItems));
			String last = "A";
			for (final PendingReference cs : pendingRefs) {
				assertThat("pendingRef are properly sorted", cs.getPubmedId().compareTo(last) >= 0);
				last = cs.getPubmedId();
			}

			// sorting by PMID in descending order
			sorting = Sorting.builder()
					.field("pmid")
					.order(Order.DESC)
					.build();
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of sorted pendingRef coincides with expected", pendingRefs.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final PendingReference cs : pendingRefs) {
				assertThat("pendingRef are properly sorted", cs.getPubmedId().compareTo(last) <= 0);
				last = cs.getPubmedId();
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of sorted pendingRef coincides with expected", pendingRefs.size(), equalTo(0));

			// projection
			pendingRefs = PENDING_REFERENCE_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(String.format("%s%s", DB_PREFIX, "seqids"), false), null);
			assertThat("projected pendingRef is not null", pendingRefs, notNullValue());
			assertThat("number of projected pendingRef coincides with expected", pendingRefs.size(), equalTo(numItems));
			assertThat("sample was filtered from database response", pendingRefs.get((new Random()).nextInt(numItems)).getSeqids(), nullValue());

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				PENDING_REFERENCE_DAO.delete(id2);
			}
			PENDING_REFERENCE_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PendingReferenceCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("PendingReferenceCollectionTest.test() has finished");
		}
	}

}