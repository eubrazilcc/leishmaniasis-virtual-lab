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
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.storage.dao.LvlInstanceDAO.GEOLOCATION_KEY;
import static eu.eubrazilcc.lvl.storage.dao.LvlInstanceDAO.INSTANCE_DAO;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.LvlInstance;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;

/**
 * Tests {@link LvlInstance} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LvlInstanceCollectionTest {

	@Test
	public void test() {
		System.out.println("LvlInstanceCollectionTest.test()");
		try {
			// insert
			final Date heartbeat = new Date();
			final LvlInstance instance = LvlInstance.builder()
					.instanceId("instanceId")
					.roles(newHashSet("shard"))
					.heartbeat(heartbeat)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.0d, 2.0d).build()).build())					
					.build();
			WriteResult<LvlInstance> writeResult = INSTANCE_DAO.insert(instance);
			assertThat("insert write result is not null", writeResult, notNullValue());
			assertThat("insert write result Id is not null", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty", isNotBlank(writeResult.getId()), equalTo(true));
			final String dbId = writeResult.getId();

			// insert ignoring duplicates
			writeResult = INSTANCE_DAO.insert(instance, true);
			assertThat("insert write result is not null after duplicate insertion", writeResult, notNullValue());
			assertThat("insert write result Id is not null after duplicate insertion", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty after duplicate insertion", isNotBlank(writeResult.getId()), equalTo(true));
			assertThat("insert write result Id coincides with expected after duplicate insertion", writeResult.getId(), equalTo(dbId));

			try {
				INSTANCE_DAO.insert(instance, false);
				fail("Expected Exception due to duplicate insertion");
			} catch (RuntimeException expected) {
				System.out.println("Expected exception caught: " + expected.getClass());
			}

			// find
			LvlInstance instance2 = INSTANCE_DAO.find(instance.getInstanceId());
			assertThat("instance is not null", instance2, notNullValue());
			assertThat("instance coincides with original", instance2, equalTo(instance));
			System.out.println(instance2.toString());

			// list with projection
			List<LvlInstance> instances = INSTANCE_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(GEOLOCATION_KEY, false), null);
			assertThat("projected instances is not null", instances, notNullValue());
			assertThat("number of projected instances coincides with expected", instances.size(), equalTo(1));
			assertThat("location was filtered from database response", instances.get(0).getLocation(), nullValue());

			// update
			instance.setRoles(newHashSet("working_node"));
			INSTANCE_DAO.update(instance);

			// find after update
			instance2 = INSTANCE_DAO.find(instance.getInstanceId());
			assertThat("instance is not null", instance2, notNullValue());
			assertThat("instance coincides with original", instance2, equalTo(instance));
			System.out.println(instance2.toString());

			// remove
			INSTANCE_DAO.delete(instance.getInstanceId());
			final long numRecords = INSTANCE_DAO.count();
			assertThat("number of instances stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {				
				final LvlInstance instance3 = LvlInstance.builder()
						.instanceId("Instance " + i)
						.build();
				ids.add(instance3.getInstanceId());
				INSTANCE_DAO.insert(instance3);
			}
			final int size = 3;
			int start = 0;
			instances = null;
			final MutableLong count = new MutableLong(0l);
			do {
				instances = INSTANCE_DAO.list(start, size, null, null, null, count);
				if (instances.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + instances.size() + " of " + count.getValue() + " items");
				}
				start += instances.size();
			} while (!instances.isEmpty());
			for (final String id2 : ids) {			
				INSTANCE_DAO.delete(id2);
			}
			INSTANCE_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LvlInstanceCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("LvlInstanceCollectionTest.test() has finished");
		}
	}

}