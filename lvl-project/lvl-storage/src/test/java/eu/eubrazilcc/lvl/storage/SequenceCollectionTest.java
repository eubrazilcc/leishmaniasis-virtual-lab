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
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
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

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;

/**
 * Tests sequence collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceCollectionTest {

	@Test
	public void test() {
		System.out.println("SequenceCollectionTest.test()");
		try {
			// insert
			final Sequence sequence = Sequence.builder()
					.dataSource(DataSource.GENBANK)
					.accession("ABC12345678")
					.version("3.0")
					.gi(Integer.MAX_VALUE)
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build())
					.locale(new Locale("es", "ES"))
					.build();
			final SequenceKey sequenceKey = SequenceKey.builder()
					.dataSource(sequence.getDataSource())
					.accession(sequence.getAccession())
					.build();
			SEQUENCE_DAO.insert(sequence);

			// find
			Sequence sequence2 = SEQUENCE_DAO.find(sequenceKey);
			assertThat("sequence is not null", sequence2, notNullValue());
			assertThat("sequence coincides with original", sequence2, equalTo(sequence));
			System.out.println(sequence2.toString());

			// find by GenBank GenInfo Identifier
			sequence2 = SEQUENCE_DAO.find(SequenceGiKey.builder()
					.dataSource(sequence.getDataSource())
					.gi(sequence.getGi())
					.build());
			assertThat("sequence is not null", sequence2, notNullValue());
			assertThat("sequence coincides with original", sequence2, equalTo(sequence));
			System.out.println(sequence2.toString());

			// duplicates are not allowed
			try {
				SEQUENCE_DAO.insert(sequence2);
				fail("Duplicate sequences are not allowed");
			} catch (Exception e) {
				System.out.println("Exception caught while trying to insert a duplicate sequence");
			}

			// insert element with hard link
			final Sequence sequence1 = Sequence.builder()
					.links(newArrayList(Link.fromUri("http://example.com/sequences/gb:EFHJ90864").rel(SELF).type(APPLICATION_JSON).build()))
					.dataSource(DataSource.GENBANK)
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
			final SequenceKey sequenceKey1 = SequenceKey.builder()
					.dataSource(sequence1.getDataSource())
					.accession(sequence1.getAccession())
					.build();

			SEQUENCE_DAO.insert(sequence1);
			sequence1.setLinks(null);

			// find element after insertion (hard link should be removed)
			sequence2 = SEQUENCE_DAO.find(sequenceKey1);
			assertThat("sequence inserted with hard link is not null", sequence2, notNullValue());
			assertThat("sequence inserted with hard link coincides with expected", sequence2, equalTo(sequence1));
			System.out.println(sequence2.toString());
			
			SEQUENCE_DAO.delete(sequenceKey1);

			// update
			sequence.setVersion("4.0");
			SEQUENCE_DAO.update(sequence);

			// find after update
			sequence2 = SEQUENCE_DAO.find(sequenceKey);
			assertThat("sequence is not null", sequence2, notNullValue());
			assertThat("sequence coincides with original", sequence2, equalTo(sequence));
			System.out.println(sequence2.toString());

			// search sequences near a point and within a maximum distance
			List<Sequence> sequences = SEQUENCE_DAO.getNear(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build(), 
					10000.0d);
			assertThat("sequence is not null", sequences, notNullValue());
			assertThat("ids is not empty", !sequences.isEmpty());

			// search sequences within an area
			sequences = SEQUENCE_DAO.geoWithin(Polygon.builder()
					.exteriorRing(LngLatAlt.builder().coordinates(-140.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 50.0d).build(),
							LngLatAlt.builder().coordinates(-140.0d, 30.0d).build()).build());
			assertThat("sequences is not null", sequences, notNullValue());
			assertThat("ids is not empty", !sequences.isEmpty());

			// remove
			SEQUENCE_DAO.delete(sequenceKey);
			final long numRecords = SEQUENCE_DAO.count();
			assertThat("number of sequences stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random random = new Random();
			final String[] countries = { "Kenya", "Senegal", "Pakistan", "Italy", "Greece", "France", "Tunisia", "Ethiopia", "Egypt", 
					"Spain", "Lebanon", "Oman", "Syrian Arab Republic", "Cyprus", "Portugal", "Morocco", "Turkey", "Malta", "Madagascar",
					"New Caledonia", "Brazil" };
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final Sequence sequence3 = Sequence.builder()
						.dataSource(DataSource.GENBANK)
						.accession(Integer.toString(i))
						.definition("This is an example")
						.gi(i)
						.countryFeature(countries[random.nextInt(countries.length)])
						.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
						.build();
				ids.add(sequence3.getAccession());
				SEQUENCE_DAO.insert(sequence3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			sequences = null;
			final MutableLong count = new MutableLong(0l);
			do {
				sequences = SEQUENCE_DAO.list(start, size, null, null, count);
				if (sequences.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + sequences.size() + " of " + count.getValue() + " items");
				}
				start += sequences.size();
			} while (!sequences.isEmpty());

			// filter: keyword matching search			
			ImmutableMap<String, String> filter = of("source", DataSource.GENBANK);
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems));			

			// filter: keyword matching search
			filter = of("accession", Integer.toString(random.nextInt(numItems)));
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(1));

			// filter: keyword matching search
			filter = of("locale", Locale.ENGLISH.toString());
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems / 2));

			// filter: combined keyword matching search
			filter = of("source", DataSource.GENBANK, "accession", Integer.toString(random.nextInt(numItems)));
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(1));

			// filter: full-text search
			filter = of("text", "an example");
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems));

			// filter: combined full-text search with keyword matching search
			filter = of("source", DataSource.GENBANK, "locale", Locale.ENGLISH.toString(), "text", "example");
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems / 2));

			// invalid filter
			filter = of("filter_name", "filter_content");
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(0));

			// sorting by accession in ascending order
			Sorting sorting = Sorting.builder()
					.field("accession")
					.order(Order.ASC)
					.build();
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null);
			assertThat("sorted sequences is not null", sequences, notNullValue());
			assertThat("number of sorted sequences coincides with expected", sequences.size(), equalTo(numItems));
			String last = "-1";
			for (final Sequence seq : sequences) {
				assertThat("sequences are properly sorted", seq.getAccession().compareTo(last) > 0);
				last = seq.getAccession();
			}

			// sorting by country in descending order
			sorting = Sorting.builder()
					.field("countryFeature")
					.order(Order.DESC)
					.build();
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null);
			assertThat("sorted sequences is not null", sequences, notNullValue());
			assertThat("number of sorted sequences coincides with expected", sequences.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final Sequence seq : sequences) {
				assertThat("sequences are properly sorted", seq.getCountryFeature().compareTo(last) <= 0);
				last = seq.getCountryFeature();
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null);
			assertThat("sorted sequences is not null", sequences, notNullValue());
			assertThat("number of sorted sequences coincides with expected", sequences.size(), equalTo(0));

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				SEQUENCE_DAO.delete(SequenceKey.builder()
						.dataSource(DataSource.GENBANK)
						.accession(id2)
						.build());
			}
			SEQUENCE_DAO.stats(System.out);			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SequenceCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SequenceCollectionTest.test() has finished");
		}
	}

}