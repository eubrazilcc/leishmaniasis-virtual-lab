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

package eu.eubrazilcc.lvl.core;

import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.mapToString;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.computeHash;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.formattedQuery;
import static eu.eubrazilcc.lvl.core.util.QueryUtils.parseQuery;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.util.QueryUtils;

/**
 * Tests {@link QueryUtils} utility class.
 * @author Erik Torres <ertorser@upv.es>
 */
public class QueryUtilsTest {

	@Test
	public void test() {
		System.out.println("QueryUtilsTest.test()");
		try {
			// full-text search
			String query = "full-text search term";
			ImmutableMap<String, String> filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(1));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term"));			
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// keyword matching search
			Map<String, Pair<String, String>> queries = new ImmutableMap.Builder<String, Pair<String, String>>()
					.put("keyword:value", Pair.of("keyword", "value"))
					.put("keyword:\"value\"", Pair.of("keyword", "value"))
					.put("catalogNumber:\"9f9c5951-d147-461d-9dc9-469443023d65\"", Pair.of("catalogNumber", "9f9c5951-d147-461d-9dc9-469443023d65"))
					.put("author:\"!user1@lvl\"", Pair.of("author", "!user1@lvl"))
					.put("author:!user1@lvl", Pair.of("author", "!user1@lvl"))
					.build();
			for (final Map.Entry<String, Pair<String, String>> e : queries.entrySet()) {
				filter = parseQuery(e.getKey());
				assertThat("filter is not null", filter, notNullValue());
				assertThat("number of search terms coincides with expected", filter.size(), equalTo(1));
				assertThat("keyword search term is not null", filter.get(e.getValue().key), notNullValue());
				assertThat("keyword search term coincides with expected", filter.get(e.getValue().key), equalTo(e.getValue().value));			
				/* uncomment for additional output */
				System.out.println(" >> QUERY -->" + e.getKey() + "<--");
				System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));
			}

			// combined full-text and keyword matching search
			query = "full-text search term keyword:value";
			filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(2));
			assertThat("keyword search term is not null", filter.get("keyword"), notNullValue());
			assertThat("keyword search term coincides with expected", filter.get("keyword"), equalTo("value"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term"));			
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// complex, combined full-text and keyword matching search
			query = "full-text search term keyword1:value1 keyword2:\"value2\"";
			filter = parseQuery(query);			
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(3));
			assertThat("keyword search term 1 is not null", filter.get("keyword1"), notNullValue());
			assertThat("keyword search term 1 coincides with expected", filter.get("keyword1"), equalTo("value1"));
			assertThat("keyword search term 2 is not null", filter.get("keyword2"), notNullValue());
			assertThat("keyword search term 2 coincides with expected", filter.get("keyword2"), equalTo("value2"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term"));			
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// complex, combined full-text and keyword matching search (alternative ordering)
			query = "keyword1:\"value1 contains spaces\" keyword2:value2 \"full-text search term\"";
			filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(3));
			assertThat("keyword search term 1 is not null", filter.get("keyword1"), notNullValue());
			assertThat("keyword search term 1 coincides with expected", filter.get("keyword1"), equalTo("value1 contains spaces"));
			assertThat("keyword search term 2 is not null", filter.get("keyword2"), notNullValue());
			assertThat("keyword search term 2 coincides with expected", filter.get("keyword2"), equalTo("value2"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term"));
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// complex, combined full-text and keyword matching search (alternative ordering)
			query = "keyword1:value1 full-text search term keyword2:value2";
			filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(3));
			assertThat("keyword search term 1 is not null", filter.get("keyword1"), notNullValue());
			assertThat("keyword search term 1 coincides with expected", filter.get("keyword1"), equalTo("value1"));
			assertThat("keyword search term 2 is not null", filter.get("keyword2"), notNullValue());
			assertThat("keyword search term 2 coincides with expected", filter.get("keyword2"), equalTo("value2"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term"));
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// complex, combined full-text and keyword matching search (alternative ordering)
			query = "keyword1:value1 full-text search term keyword2:value2 another full-text search with duplicated values";
			filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(3));
			assertThat("keyword search term 1 is not null", filter.get("keyword1"), notNullValue());
			assertThat("keyword search term 1 coincides with expected", filter.get("keyword1"), equalTo("value1"));
			assertThat("keyword search term 2 is not null", filter.get("keyword2"), notNullValue());
			assertThat("keyword search term 2 coincides with expected", filter.get("keyword2"), equalTo("value2"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term another full-text search with duplicated values"));
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// complex, combined full-text and keyword matching search (alternative ordering, remove duplicated terms from full-text)
			query = "keyword1:value1 full-text search term keyword2:value2 another full-text search with duplicated values";
			filter = parseQuery(query, true);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(3));
			assertThat("keyword search term 1 is not null", filter.get("keyword1"), notNullValue());
			assertThat("keyword search term 1 coincides with expected", filter.get("keyword1"), equalTo("value1"));
			assertThat("keyword search term 2 is not null", filter.get("keyword2"), notNullValue());
			assertThat("keyword search term 2 coincides with expected", filter.get("keyword2"), equalTo("value2"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term another with duplicated values"));
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

			// complex, combined full-text and keyword matching search (using a sloppy spacing format)
			query = " keyword1:value1   full-text   search term   countryfeature:value2  ";
			filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(3));
			assertThat("keyword search term 1 is not null", filter.get("keyword1"), notNullValue());
			assertThat("keyword search term 1 coincides with expected", filter.get("keyword1"), equalTo("value1"));
			assertThat("keyword search term 2 is not null", filter.get("countryfeature"), notNullValue());
			assertThat("keyword search term 2 coincides with expected", filter.get("countryfeature"), equalTo("value2"));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("full-text search term"));
			List<FormattedQueryParam> formatted = formattedQuery(filter, Sequence.class);
			assertThat("formatted query is not empty", formatted, allOf(notNullValue(), hasSize(3)));
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));
			System.out.println(" >> FORMATTED QUERY: " + collectionToString(formatted));

			// invalid placement
			query = "keyword : value";
			filter = parseQuery(query);
			assertThat("filter is not null", filter, notNullValue());
			assertThat("number of search terms coincides with expected", filter.size(), equalTo(1));
			assertThat("full-text search term is not null", filter.get("text"), notNullValue());
			assertThat("full-text search term coincides with expected", filter.get("text"), equalTo("keyword : value"));
			/* uncomment for additional output */
			System.out.println(" >> QUERY -->" + query + "<--");
			System.out.println(" >> COMPUTED FILTER: " + mapToString(filter));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("QueryUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("QueryUtilsTest.test() has finished");
		}
	}

	@Test
	public void testHash() {
		// test hash computation using query and sort
		String query = "full-text search term keyword1:value1 keyword2:\"value2\"";
		String sort = "field1=asc";
		String hash = computeHash(query, sort);
		assertThat("hash is not empty", trim(hash), allOf(notNullValue(), not(equalTo(""))));
		/* uncomment for additional output */
		System.out.println(" >> INPUT  --> query: " + query + ", sort: " + sort + "<--");
		System.out.println(" >> OUTPUT --> hash: " + hash + "<--");

		// test hash computation using query
		query = "full-text search term keyword1:value1 keyword2:\"value2\"";
		sort = null;
		hash = computeHash(query, sort);
		assertThat("hash is not empty", trim(hash), allOf(notNullValue(), not(equalTo(""))));
		/* uncomment for additional output */
		System.out.println(" >> INPUT  --> query: " + query + ", sort: NULL<--");
		System.out.println(" >> OUTPUT --> hash: " + hash + "<--");

		// test hash computation with empty query
		query = "";
		sort = null;
		hash = computeHash(query, sort);
		assertThat("hash is not empty", trim(hash), allOf(notNullValue(), not(equalTo(""))));
		/* uncomment for additional output */
		System.out.println(" >> INPUT  --> query: " + query + ", sort: NULL<--");
		System.out.println(" >> OUTPUT --> hash: " + hash + "<--");
	}

}