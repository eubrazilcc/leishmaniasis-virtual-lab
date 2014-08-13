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
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Sequence;
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

			// find and append link to found records
			final URI baseUri = new URI("https://localhost:8080/service/resource");
			SEQUENCE_DAO.baseUri(baseUri);
			sequence2 = SEQUENCE_DAO.find(sequenceKey);
			assertThat("sequence with link is not null", sequence2, notNullValue());
			assertThat("sequence with link coincides with original", sequence2.equalsIgnoreLink(sequence));
			final Link link = sequence2.getLink();
			assertThat("sequence link is not null", link, notNullValue());
			assertThat("sequence link URI is not null", link.getUri(), notNullValue());
			assertThat("sequence link relation type is not null", link.getRel(), notNullValue());
			assertThat("sequence link relation type is not empty", isNotBlank(link.getRel()));
			assertThat("sequence link type is not null", link.getType(), notNullValue());
			assertThat("sequence link type is not empty", isNotBlank(link.getType()));
			System.out.println(sequence2.toString());
			SEQUENCE_DAO.baseUri(null);			

			// duplicates are not allowed
			try {
				SEQUENCE_DAO.insert(sequence2);
				fail("Duplicate sequences are not allowed");
			} catch (Exception e) {
				System.out.println("Exception caught while trying to insert a duplicate sequence");
			}

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

			// create a large dataset to test complex operations
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final Sequence sequence3 = Sequence.builder()
						.dataSource(DataSource.GENBANK)
						.accession(Integer.toString(i))
						.definition("This is an example")
						.gi(i)
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
				sequences = SEQUENCE_DAO.list(start, size, null, count);
				if (sequences.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + sequences.size() + " of " + count.getValue() + " items");
				}
				start += sequences.size();
			} while (!sequences.isEmpty());

			// filter: keyword matching search
			final Random random = new Random();
			ImmutableMap<String, String> filter = of("source", DataSource.GENBANK);
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems));

			// filter: keyword matching search
			filter = of("accession", Integer.toString(random.nextInt(numItems)));
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(1));
			
			// filter: keyword matching search
			filter = of("locale", Locale.ENGLISH.toString());
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems / 2));

			// filter: combined keyword matching search
			filter = of("source", DataSource.GENBANK, "accession", Integer.toString(random.nextInt(numItems)));
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(1));
			
			// filter: full-text search
			filter = of("text", "an example");
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems));
			
			// filter: combined full-text search with keyword matching search
			filter = of("source", DataSource.GENBANK, "locale", Locale.ENGLISH.toString(), "text", "example");
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(numItems / 2));
			
			// invalid filter
			filter = of("filter_name", "filter_content");
			sequences = SEQUENCE_DAO.list(0, Integer.MAX_VALUE, filter, null);
			assertThat("filtered sequences is not null", sequences, notNullValue());
			assertThat("number of filtered sequences coincides with expected", sequences.size(), equalTo(0));

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