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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;

/**
 * Tests sequence collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SandflyCollectionTest {

	@Test
	public void test() {
		System.out.println("SandflyCollectionTest.test()");
		try {
			// insert
			final Sandfly sandfly = Sandfly.builder()
					.dataSource(GENBANK)
					.accession("ABC12345678")
					.version("3.0")
					.gi(Integer.MAX_VALUE)
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build())
					.locale(new Locale("es", "ES"))
					.build();
			final SequenceKey sandflyKey = SequenceKey.builder()
					.dataSource(sandfly.getDataSource())
					.accession(sandfly.getAccession())
					.build();
			SANDFLY_DAO.insert(sandfly);

			// find
			Sandfly sandfly2 = SANDFLY_DAO.find(sandflyKey);
			assertThat("sandfly is not null", sandfly2, notNullValue());
			assertThat("sandfly coincides with original", sandfly2, equalTo(sandfly));
			System.out.println(sandfly2.toString());

			// find by GenBank GenInfo Identifier
			sandfly2 = SANDFLY_DAO.find(SequenceGiKey.builder()
					.dataSource(sandfly.getDataSource())
					.gi(sandfly.getGi())
					.build());
			assertThat("sandfly is not null", sandfly2, notNullValue());
			assertThat("sandfly coincides with original", sandfly2, equalTo(sandfly));
			System.out.println(sandfly2.toString());

			// duplicates are not allowed
			try {
				SANDFLY_DAO.insert(sandfly2);
				fail("Duplicate sandflies are not allowed");
			} catch (Exception e) {
				System.out.println("Exception caught while trying to insert a duplicate sandfly");
			}

			// insert element with hard link
			final Sandfly sandfly1 = Sandfly.builder()
					.links(newArrayList(Link.fromUri("http://example.com/sandfly/gb:EFHJ90864").rel(SELF).type(APPLICATION_JSON).build()))
					.dataSource(GENBANK)
					.accession("EFHJ90864")
					.version("3.0")
					.gi(Integer.MAX_VALUE - 1)
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build())
					.locale(new Locale("es", "ES"))
					.pmids(newHashSet("1234R", "AV99O0"))
					.build();
			final SequenceKey sandflyKey1 = SequenceKey.builder()
					.dataSource(sandfly1.getDataSource())
					.accession(sandfly1.getAccession())
					.build();

			SANDFLY_DAO.insert(sandfly1);
			sandfly1.setLinks(null);

			// find element after insertion (hard link should be removed)
			sandfly2 = SANDFLY_DAO.find(sandflyKey1);
			assertThat("sandfly inserted with hard link is not null", sandfly2, notNullValue());
			assertThat("sandfly inserted with hard link coincides with expected", sandfly2, equalTo(sandfly1));
			System.out.println(sandfly2.toString());

			SANDFLY_DAO.delete(sandflyKey1);

			// update
			sandfly.setVersion("4.0");
			SANDFLY_DAO.update(sandfly);

			// find after update
			sandfly2 = SANDFLY_DAO.find(sandflyKey);
			assertThat("sandfly is not null", sandfly2, notNullValue());
			assertThat("sandfly coincides with original", sandfly2, equalTo(sandfly));
			System.out.println(sandfly2.toString());

			// search sandfly near a point and within a maximum distance
			List<Sandfly> sandflies = SANDFLY_DAO.getNear(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build(), 
					10000.0d);
			assertThat("sandfly is not null", sandflies, notNullValue());
			assertThat("ids is not empty", !sandflies.isEmpty());

			// search sandfly within an area
			sandflies = SANDFLY_DAO.geoWithin(Polygon.builder()
					.exteriorRing(LngLatAlt.builder().coordinates(-140.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 50.0d).build(),
							LngLatAlt.builder().coordinates(-140.0d, 30.0d).build()).build());
			assertThat("sandfly is not null", sandflies, notNullValue());
			assertThat("ids is not empty", !sandflies.isEmpty());

			// remove
			SANDFLY_DAO.delete(sandflyKey);
			final long numRecords = SANDFLY_DAO.count();
			assertThat("number of sandfly stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random random = new Random();
			final String[] countries = { "Kenya", "Senegal", "Pakistan", "Italy", "Greece", "France", "Tunisia", "Ethiopia", "Egypt", 
					"Spain", "Lebanon", "Oman", "Syrian Arab Republic", "Cyprus", "Portugal", "Morocco", "Turkey", "Malta", "Madagascar",
					"New Caledonia", "Brazil" };
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final Sandfly sandfly3 = Sandfly.builder()
						.dataSource(GENBANK)
						.accession(Integer.toString(i))
						.definition("This is an example")
						.gi(i)
						.countryFeature(countries[random.nextInt(countries.length)])
						.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
						.build();
				ids.add(sandfly3.getAccession());
				SANDFLY_DAO.insert(sandfly3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			sandflies = null;
			final MutableLong count = new MutableLong(0l);
			do {
				sandflies = SANDFLY_DAO.list(start, size, null, null, count);
				if (sandflies.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + sandflies.size() + " of " + count.getValue() + " items");
				}
				start += sandflies.size();
			} while (!sandflies.isEmpty());

			// filter: keyword matching search			
			ImmutableMap<String, String> filter = of("source", GENBANK);
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(numItems));			

			// filter: keyword matching search
			filter = of("accession", Integer.toString(random.nextInt(numItems)));
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(1));

			// filter: keyword matching search
			filter = of("locale", Locale.ENGLISH.toString());
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(numItems / 2));

			// filter: combined keyword matching search
			filter = of("source", GENBANK, "accession", Integer.toString(random.nextInt(numItems)));
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(1));

			// filter: full-text search
			filter = of("text", "an example");
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(numItems));

			// filter: combined full-text search with keyword matching search
			filter = of("source", GENBANK, "locale", Locale.ENGLISH.toString(), "text", "example");
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(numItems / 2));

			// invalid filter
			filter = of("filter_name", "filter_content");
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sandfly is not null", sandflies, notNullValue());
			assertThat("number of filtered sandfly coincides with expected", sandflies.size(), equalTo(0));

			// sorting by accession in ascending order
			Sorting sorting = Sorting.builder()
					.field("accession")
					.order(Order.ASC)
					.build();
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, null, sorting, null);
			assertThat("sorted sandfly is not null", sandflies, notNullValue());
			assertThat("number of sorted sandfly coincides with expected", sandflies.size(), equalTo(numItems));
			String last = "-1";
			for (final Sandfly seq : sandflies) {
				assertThat("sandfly are properly sorted", seq.getAccession().compareTo(last) > 0);
				last = seq.getAccession();
			}

			// sorting by country in descending order
			sorting = Sorting.builder()
					.field("countryFeature")
					.order(Order.DESC)
					.build();
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, null, sorting, null);
			assertThat("sorted sandfly is not null", sandflies, notNullValue());
			assertThat("number of sorted sandfly coincides with expected", sandflies.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final Sandfly seq : sandflies) {
				assertThat("sandfly are properly sorted", seq.getCountryFeature().compareTo(last) <= 0);
				last = seq.getCountryFeature();
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			sandflies = SANDFLY_DAO.list(0, Integer.MAX_VALUE, null, sorting, null);
			assertThat("sorted sandfly is not null", sandflies, notNullValue());
			assertThat("number of sorted sandfly coincides with expected", sandflies.size(), equalTo(0));

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				SANDFLY_DAO.delete(SequenceKey.builder()
						.dataSource(GENBANK)
						.accession(id2)
						.build());
			}
			SANDFLY_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SandflyCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SandflyCollectionTest.test() has finished");
		}
	}

}