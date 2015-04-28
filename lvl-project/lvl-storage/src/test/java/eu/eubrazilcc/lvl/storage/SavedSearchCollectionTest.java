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
import static eu.eubrazilcc.lvl.storage.dao.SavedSearchDAO.SAVED_SEARCH_DAO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.FormattedQueryParam;
import eu.eubrazilcc.lvl.core.SavedSearch;

/**
 * Tests saved searches collection in the database.
 * @author Erik Torres <ertorser@upv.es> 
 */
public class SavedSearchCollectionTest {

	@Test
	public void test() {
		System.out.println("SavedSearchCollectionTest.test()");
		try {
			// insert
			final SavedSearch search = SavedSearch.builder()
					.id("abc123")
					.namespace("username")
					.type("sequence ; sandflies")
					.saved(new Date())
					.search(newHashSet(FormattedQueryParam.builder().term("country:spain").valid(true).build(),
							FormattedQueryParam.builder().term("sequence").build()))
							.build();			
			SAVED_SEARCH_DAO.insert(search);

			// find
			SavedSearch search2 = SAVED_SEARCH_DAO.find(search.getId());
			assertThat("saved search is not null", search2, notNullValue());
			assertThat("saved search coincides with original", search2, equalTo(search));
			System.out.println(search2.toString());

			// update
			search.setDescription("This record now includes a new description of the saved item");
			SAVED_SEARCH_DAO.update(search);

			// find after update
			search2 = SAVED_SEARCH_DAO.find(search.getId());
			assertThat("saved search is not null", search2, notNullValue());
			assertThat("saved search coincides with original", search2, equalTo(search));
			System.out.println(search2.toString());

			// list all by owner
			List<SavedSearch> searchs = SAVED_SEARCH_DAO.findAll(search.getNamespace());
			assertThat("saved searches are not null", searchs, notNullValue());
			assertThat("saved searches are not empty", !searchs.isEmpty(), equalTo(true));
			assertThat("number of saved searches coincides with expected", searchs.size(), equalTo(1));
			assertThat("saved searches coincide with original", searchs.get(0), equalTo(search));

			// remove
			SAVED_SEARCH_DAO.delete(search.getId());
			final long numRecords = SAVED_SEARCH_DAO.count();
			assertThat("number of saved searches stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final SavedSearch search3 = SavedSearch.builder()
						.id(Integer.toString(i)).type("type").build();
				ids.add(search3.getId());
				SAVED_SEARCH_DAO.insert(search3);
			}
			final int size = 3;
			int start = 0;
			searchs = null;
			final MutableLong count = new MutableLong(0l);
			do {
				searchs = SAVED_SEARCH_DAO.list(start, size, null, null, null, count);
				if (searchs.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + searchs.size() + " of " + count.getValue() + " items");
				}
				start += searchs.size();
			} while (!searchs.isEmpty());
			for (final String id2 : ids) {			
				SAVED_SEARCH_DAO.delete(id2);
			}
			SAVED_SEARCH_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SavedSearchCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SavedSearchCollectionTest.test() has finished");
		}
	}
	
}