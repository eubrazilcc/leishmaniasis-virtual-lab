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
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.dao.SharedObjectDAO.DB_PREFIX;
import static eu.eubrazilcc.lvl.storage.dao.SharedObjectDAO.SHARED_OBJECT_DAO;
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
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.ObjectAccepted;
import eu.eubrazilcc.lvl.core.ObjectGranted;
import eu.eubrazilcc.lvl.core.Shareable.SharedAccess;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests shared objects collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SharedObjectCollectionTest extends LeishvlTestCase {

	public SharedObjectCollectionTest() {
		super(true);
	}

	@Test
	public void test() {
		printMsg("SharedObjectCollectionTest.test()");
		try {
			// insert
			final ObjectGranted objGranted = ObjectGranted.builder()
					.id("Shared-A")
					.owner("owner@example.com")
					.user("user@example.com")
					.collection("sequences")
					.itemId("LeishVL123")
					.sharedNow()
					.accessType(EDIT_SHARE)
					.build();
			final WriteResult<ObjectGranted> ack = SHARED_OBJECT_DAO.insert(objGranted);
			assertThat("write ack is not null", ack, notNullValue());
			assertThat("database id is not empty", trim(ack.getId()), allOf(notNullValue(), not(equalTo(""))));
			printMsg(" >> New record inserted: id=" + ack.getId() + ", record=" + toJson(objGranted, JSON_PRETTY_PRINTER));

			// find (as the object's owner)
			ObjectGranted objGranted2 = SHARED_OBJECT_DAO.find(objGranted.getId(), objGranted.getOwner());
			assertThat("objGranted is not null", objGranted2, notNullValue());
			assertThat("objGranted coincides with original", objGranted2, equalTo(objGranted));
			printMsg(" >> Found:\n" + toJson(objGranted2, JSON_PRETTY_PRINTER));

			// find (as the granted user)
			final ObjectAccepted objAccepted = ObjectAccepted.builder()
					.id(objGranted.getId())
					.owner(objGranted.getOwner())
					.user(objGranted.getUser())
					.collection(objGranted.getCollection())
					.itemId(objGranted.getItemId())
					.sharedDate(objGranted.getSharedDate())
					.accessType(objGranted.getAccessType())
					.build();
			ObjectAccepted objAccepted2 = SHARED_OBJECT_DAO.findAccepted(objGranted.getId(), objGranted.getUser()); // owner is not checked
			assertThat("objAccepted is not null", objAccepted2, notNullValue());			
			assertThat("objAccepted coincides with expected", objAccepted2, equalTo(objAccepted));
			printMsg(" >> Found:\n" + toJson(objGranted2, JSON_PRETTY_PRINTER));

			// duplicates are not allowed
			try {
				SHARED_OBJECT_DAO.insert(objGranted2);
				fail("Duplicate objGranteds are not allowed");
			} catch (Exception e) {
				printMsg("Exception caught while trying to insert a duplicate objGranted");
			}

			// insert element with hard link (test that hard link is removed)
			final ObjectGranted objGranted1 = ObjectGranted.builder()
					.links(newArrayList(Link.fromUri("http://example.com/shares/granted/user1@example.com/lvl0002").rel(SELF).type(APPLICATION_JSON).build()))
					.id("Shared-B")
					.owner("owner@example.com")
					.user("user@example.com")
					.collection("sequences")
					.itemId("LeishVL456")
					.sharedNow()
					.accessType(EDIT_SHARE)					
					.build();
			SHARED_OBJECT_DAO.insert(objGranted1);
			objGranted1.setLinks(null);			

			// find element after insertion (hard link should be removed)
			objGranted2 = SHARED_OBJECT_DAO.find(objGranted1.getId());
			assertThat("objGranted inserted with hard link is not null", objGranted2, notNullValue());
			assertThat("objGranted inserted with hard link coincides with expected", objGranted2, equalTo(objGranted1));
			printMsg(" >> Found element inserted with links:\n" + toJson(objGranted2, JSON_PRETTY_PRINTER));

			// find all
			final List<ObjectGranted> all = SHARED_OBJECT_DAO.findAll();
			printMsg(" >> List all:\n" + toJson(all, JSON_PRETTY_PRINTER));			

			// delete
			SHARED_OBJECT_DAO.delete(objGranted1.getId());

			// update
			objGranted.setAccessType(SharedAccess.VIEW_SHARE);
			SHARED_OBJECT_DAO.update(objGranted);

			// find after update
			objGranted2 = SHARED_OBJECT_DAO.find(objGranted.getId());
			assertThat("objGranted is not null", objGranted2, notNullValue());
			assertThat("objGranted coincides with original", objGranted2, equalTo(objGranted));
			printMsg(" >> Found element after update:\n" + toJson(objGranted2, JSON_PRETTY_PRINTER));

			// remove
			SHARED_OBJECT_DAO.delete(objGranted.getId());
			final long numRecords = SHARED_OBJECT_DAO.count();
			assertThat("number of objGranted stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());			
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				calendar.add(Calendar.MINUTE, 1);
				final ObjectGranted objGranted3 = ObjectGranted.builder()
						.id("Shared-" + i)
						.owner("owner@example.com")
						.user("user@example.com")
						.collection("pendingSequences")
						.itemId("XYZ" + i)
						.sharedDate(calendar.getTime())
						.accessType(i%2 == 0 ? SharedAccess.EDIT_SHARE : SharedAccess.VIEW_SHARE)
						.build();				
				ids.add(objGranted3.getId());
				SHARED_OBJECT_DAO.insert(objGranted3);				
			}

			// pagination
			final int size = 3;
			int start = 0;
			List<ObjectGranted> objsGranted = null;
			final MutableLong count = new MutableLong(0l);
			do {
				objsGranted = SHARED_OBJECT_DAO.list(start, size, null, null, null, count);
				if (objsGranted.size() != 0) {
					printMsg("Paging: first item " + start + ", showing " + objsGranted.size() + " of " + count.getValue() + " items");					
				}
				start += objsGranted.size();
			} while (!objsGranted.isEmpty());

			// filter: collection+itemId search (as the object's owner)
			ImmutableMap<String, String> filter = of("collection", "pendingSequences", "itemId", "XYZ1");
			objsGranted = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);			
			assertThat("filtered objGranted coincides with expected", objsGranted, allOf(notNullValue(), not(empty()), hasSize(1)));			

			// invalid filter
			filter = of("filter_name", "filter_content");
			objsGranted = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered objGranted coincides with expected", objsGranted, allOf(notNullValue(), empty()));

			// sorting by shared date in ascending order (as the object's owner)
			Sorting sorting = Sorting.builder()
					.field("sharedDate")
					.order(Order.ASC)
					.build();
			objsGranted = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted objGranted coincides with expected", objsGranted, allOf(notNullValue(), not(empty()), hasSize(numItems)));
			Date last = new Date(0);
			for (final ObjectGranted og : objsGranted) {
				assertThat("objGranted are properly sorted", og.getSharedDate().after(last));
				last = og.getSharedDate();
			}

			// sorting by shared date in descending order (as the object's owner)
			sorting = Sorting.builder()
					.field("sharedDate")
					.order(Order.DESC)
					.build();
			objsGranted = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted objGranted coincides with expected", objsGranted, allOf(notNullValue(), not(empty()), hasSize(numItems)));
			calendar.add(Calendar.MINUTE, 1);
			last = calendar.getTime();
			for (final ObjectGranted og : objsGranted) {
				assertThat("objGranted are properly sorted", og.getSharedDate().before(last));
				last = og.getSharedDate();
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			objsGranted = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted objGranted coincides with expected", objsGranted, allOf(notNullValue(), empty()));

			// projection (as the object's owner)
			objsGranted = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(String.format("%s%s", DB_PREFIX, "accessType"), false), null);			
			assertThat("projected objGranted coincides with expected", objsGranted, allOf(notNullValue(), not(empty()), hasSize(numItems)));
			assertThat("field was filtered from database response", objsGranted.get((new Random()).nextInt(numItems)).getAccessType(), nullValue());

			// filter: collection+itemId search (as the granted user)
			filter = of("collection", "pendingSequences", "itemId", "XYZ1");
			List<ObjectAccepted> objsAccepted = SHARED_OBJECT_DAO.listAccepted(0, Integer.MAX_VALUE, filter, null, null, null, "user@example.com");
			assertThat("filtered objAccepted coincides with expected", objsAccepted, allOf(notNullValue(), not(empty()), hasSize(1)));

			// sorting by shared date in ascending order (as the granted user)
			sorting = Sorting.builder()
					.field("sharedDate")
					.order(Order.ASC)
					.build();
			objsAccepted = SHARED_OBJECT_DAO.listAccepted(0, Integer.MAX_VALUE, null, sorting, null, null, "user@example.com");
			assertThat("sorted objAccepted coincides with expected", objsAccepted, allOf(notNullValue(), not(empty()), hasSize(numItems)));
			last = new Date(0);
			for (final ObjectAccepted oa : objsAccepted) {
				assertThat("objAccepted are properly sorted", oa.getSharedDate().after(last));
				last = oa.getSharedDate();
			}

			// sorting by shared date in descending order (as the granted user)
			sorting = Sorting.builder()
					.field("sharedDate")
					.order(Order.DESC)
					.build();
			objsAccepted = SHARED_OBJECT_DAO.listAccepted(0, Integer.MAX_VALUE, null, sorting, null, null, "user@example.com");
			assertThat("sorted objAccepted coincides with expected", objsAccepted, allOf(notNullValue(), not(empty()), hasSize(numItems)));
			calendar.add(Calendar.MINUTE, 1);
			last = calendar.getTime();
			for (final ObjectAccepted oa : objsAccepted) {
				assertThat("objAccepted are properly sorted", oa.getSharedDate().before(last));
				last = oa.getSharedDate();
			}

			// projection (as the granted user)
			objsAccepted = SHARED_OBJECT_DAO.listAccepted(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(String.format("%s%s", DB_PREFIX, "accessType"), false), 
					null, "user@example.com");
			assertThat("projected objAccepted coincides with expected", objsAccepted, allOf(notNullValue(), not(empty()), hasSize(numItems)));
			assertThat("field was filtered from database response", objsAccepted.get((new Random()).nextInt(numItems)).getAccessType(), nullValue());			

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				SHARED_OBJECT_DAO.delete(id2);
			}
			SHARED_OBJECT_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SharedObjectCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("SharedObjectCollectionTest.test() has finished");
		}
	}

}