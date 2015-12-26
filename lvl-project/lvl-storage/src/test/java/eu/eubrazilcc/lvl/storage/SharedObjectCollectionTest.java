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
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Shareable.SharedAccess;
import eu.eubrazilcc.lvl.core.SharedObject;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests pending references collection in the database.
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
			final SharedObject sharedObj = SharedObject.builder()
					.subject("user1@example.com")
					.sharedNow()
					.accessType(EDIT_SHARE)
					.newId()
					.collection("datasets")
					.objectId("LeishVL123")
					.build();
			final WriteResult<SharedObject> ack = SHARED_OBJECT_DAO.insert(sharedObj);
			assertThat("write ack is not null", ack, notNullValue());
			assertThat("database id is not empty", trim(ack.getId()), allOf(notNullValue(), not(equalTo(""))));
			printMsg(" >> New record inserted: id=" + ack.getId() + ", record=" + toJson(sharedObj, JSON_PRETTY_PRINTER));			

			// find
			SharedObject sharedObj2 = SHARED_OBJECT_DAO.find(sharedObj.getId());
			assertThat("sharedObj is not null", sharedObj2, notNullValue());
			assertThat("sharedObj coincides with original", sharedObj2, equalTo(sharedObj));
			printMsg(" >> Found:\n" + toJson(sharedObj2, JSON_PRETTY_PRINTER));

			// duplicates are not allowed
			try {
				SHARED_OBJECT_DAO.insert(sharedObj2);
				fail("Duplicate sharedObjs are not allowed");
			} catch (Exception e) {
				printMsg("Exception caught while trying to insert a duplicate sharedObj");
			}

			// insert element with hard link			
			final SharedObject sharedObj1 = SharedObject.builder()
					.links(newArrayList(Link.fromUri("http://example.com/shares/user1@example.com/lvl0002").rel(SELF).type(APPLICATION_JSON).build()))
					.subject("user1@example.com")
					.sharedNow()
					.accessType(EDIT_SHARE)
					.newId()
					.collection("datasets")
					.objectId("LeishVL456")
					.build();
			SHARED_OBJECT_DAO.insert(sharedObj1);
			sharedObj1.setLinks(null);			

			// find element after insertion (hard link should be removed)
			sharedObj2 = SHARED_OBJECT_DAO.find(sharedObj1.getId());
			assertThat("sharedObj inserted with hard link is not null", sharedObj2, notNullValue());
			assertThat("sharedObj inserted with hard link coincides with expected", sharedObj2, equalTo(sharedObj1));
			printMsg(" >> Found element inserted with links:\n" + toJson(sharedObj2, JSON_PRETTY_PRINTER));

			// find all
			final List<SharedObject> all = SHARED_OBJECT_DAO.findAll();
			printMsg(" >> List all:\n" + toJson(all, JSON_PRETTY_PRINTER));

			// delete
			SHARED_OBJECT_DAO.delete(sharedObj1.getId());

			// update
			sharedObj.setAccessType(SharedAccess.VIEW_SHARE);
			SHARED_OBJECT_DAO.update(sharedObj);

			// find after update
			sharedObj2 = SHARED_OBJECT_DAO.find(sharedObj.getId());
			assertThat("sharedObj is not null", sharedObj2, notNullValue());
			assertThat("sharedObj coincides with original", sharedObj2, equalTo(sharedObj));
			printMsg(" >> Found element after update:\n" + toJson(sharedObj2, JSON_PRETTY_PRINTER));

			// remove
			SHARED_OBJECT_DAO.delete(sharedObj.getId());
			final long numRecords = SHARED_OBJECT_DAO.count();
			assertThat("number of sharedObj stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());			
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				calendar.add(Calendar.MINUTE, 1);
				final SharedObject sharedObj3 = SharedObject.builder()
						.subject("user1@example.com")
						.sharedDate(calendar.getTime())
						.accessType(i%2 == 0 ? SharedAccess.EDIT_SHARE : SharedAccess.VIEW_SHARE)
						.id("LVLREF000" + Integer.toString(i))
						.collection("pendingSequences")
						.objectId("XYZ" + i)
						.build();
				ids.add(sharedObj3.getId());
				SHARED_OBJECT_DAO.insert(sharedObj3);				
			}

			// pagination
			final int size = 3;
			int start = 0;
			List<SharedObject> sharedObjs = null;
			final MutableLong count = new MutableLong(0l);
			do {
				sharedObjs = SHARED_OBJECT_DAO.list(start, size, null, null, null, count);
				if (sharedObjs.size() != 0) {
					printMsg("Paging: first item " + start + ", showing " + sharedObjs.size() + " of " + count.getValue() + " items");					
				}
				start += sharedObjs.size();
			} while (!sharedObjs.isEmpty());

			// filter: collection+objectId search
			ImmutableMap<String, String> filter = of("collection", "pendingSequences", "objectId", "XYZ1");
			sharedObjs = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sharedObj is not null", sharedObjs, notNullValue());
			assertThat("number of filtered sharedObj coincides with expected", sharedObjs.size(), equalTo(1));

			// invalid filter
			filter = of("filter_name", "filter_content");
			sharedObjs = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sharedObj is not null", sharedObjs, notNullValue());
			assertThat("number of filtered sharedObj coincides with expected", sharedObjs.size(), equalTo(0));

			// sorting by shared date in ascending order
			Sorting sorting = Sorting.builder()
					.field("sharedDate")
					.order(Order.ASC)
					.build();
			sharedObjs = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted sharedObj is not null", sharedObjs, notNullValue());
			assertThat("number of sorted sharedObj coincides with expected", sharedObjs.size(), equalTo(numItems));
			Date last = new Date(0);
			for (final SharedObject cs : sharedObjs) {
				assertThat("sharedObj are properly sorted", cs.getSharedDate().after(last));
				last = cs.getSharedDate();
			}

			// sorting by shared date in descending order
			sorting = Sorting.builder()
					.field("sharedDate")
					.order(Order.DESC)
					.build();
			sharedObjs = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted sharedObj is not null", sharedObjs, notNullValue());
			assertThat("number of sorted sharedObj coincides with expected", sharedObjs.size(), equalTo(numItems));
			calendar.add(Calendar.MINUTE, 1);
			last = calendar.getTime();
			for (final SharedObject cs : sharedObjs) {
				assertThat("sharedObj are properly sorted", cs.getSharedDate().before(last));
				last = cs.getSharedDate();
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			sharedObjs = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted sharedObj is not null", sharedObjs, notNullValue());
			assertThat("number of sorted sharedObj coincides with expected", sharedObjs.size(), equalTo(0));

			// projection
			sharedObjs = SHARED_OBJECT_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(String.format("%s%s", DB_PREFIX, "accessType"), false), null);
			assertThat("projected sharedObj is not null", sharedObjs, notNullValue());
			assertThat("number of projected sharedObj coincides with expected", sharedObjs.size(), equalTo(numItems));
			assertThat("field was filtered from database response", sharedObjs.get((new Random()).nextInt(numItems)).getAccessType(), nullValue());

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