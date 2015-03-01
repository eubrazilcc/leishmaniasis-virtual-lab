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
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.LEISHMANIA_DAO;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.ORIGINAL_SEQUENCE_KEY;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.core.Link;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.SimpleStat;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;

/**
 * Tests Leishmania collection in the database.
 * 
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishmaniaCollectionTest {

	@Test
	public void test() {
		System.out.println("LeishmaniaCollectionTest.test()");
		try {
			// create sequence			
			final GBSeq sequence = GBSEQ_XML_FACTORY.createGBSeq()
					.withGBSeqPrimaryAccession("ABC12345678")
					.withGBSeqAccessionVersion("3.0")
					.withGBSeqOtherSeqids(GBSEQ_XML_FACTORY.createGBSeqOtherSeqids().withGBSeqid(GBSEQ_XML_FACTORY.createGBSeqid().withvalue(Integer.toString(Integer.MAX_VALUE))))
					.withGBSeqOrganism("organism")
					.withGBSeqLength("850");

			// insert
			final Leishmania leishmania = Leishmania.builder()
					.dataSource(GENBANK)
					.accession("ABC12345678")
					.version("3.0")
					.gi(Integer.MAX_VALUE)
					.definition("definition")
					.organism("organism")
					.length(850)
					.countryFeature("Spain: Murcia")					
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build())
					.locale(new Locale("es", "ES"))
					.sequence(sequence)
					.build();
			final SequenceKey leishmaniaKey = SequenceKey.builder()
					.dataSource(leishmania.getDataSource())
					.accession(leishmania.getAccession())
					.build();
			LEISHMANIA_DAO.insert(leishmania);

			// find
			Leishmania leishmania2 = LEISHMANIA_DAO.find(leishmaniaKey);
			assertThat("leishmania is not null", leishmania2, notNullValue());
			assertThat("leishmania coincides with original", leishmania2, equalTo(leishmania));
			assertThat("leishmania contains original sequence", leishmania2.getSequence(), notNullValue());
			System.out.println(leishmania2.toString());

			// find by GenBank GenInfo Identifier
			leishmania2 = LEISHMANIA_DAO.find(SequenceGiKey.builder()
					.dataSource(leishmania.getDataSource())
					.gi(leishmania.getGi())
					.build());
			assertThat("leishmania is not null", leishmania2, notNullValue());
			assertThat("leishmania coincides with original", leishmania2, equalTo(leishmania));
			System.out.println(leishmania2.toString());

			// duplicates are not allowed
			try {
				LEISHMANIA_DAO.insert(leishmania2);
				fail("Duplicate leishmanias are not allowed");
			} catch (Exception e) {
				System.out.println("Exception caught while trying to insert a duplicate leishmania");
			}

			// insert element with hard link
			final GBSeq sequence1 = GBSEQ_XML_FACTORY.createGBSeq()
					.withGBSeqPrimaryAccession("EFHJ90864")
					.withGBSeqAccessionVersion("3.0")
					.withGBSeqOtherSeqids(GBSEQ_XML_FACTORY.createGBSeqOtherSeqids().withGBSeqid(GBSEQ_XML_FACTORY.createGBSeqid().withvalue(Integer.toString(Integer.MAX_VALUE - 1))))
					.withGBSeqOrganism("organism")
					.withGBSeqLength("200");
			final Leishmania leishmania1 = Leishmania.builder()
					.links(newArrayList(Link.fromUri("http://example.com/leishmania/gb:EFHJ90864").rel(SELF).type(APPLICATION_JSON).build()))
					.dataSource(GENBANK)
					.accession("EFHJ90864")
					.version("3.0")
					.gi(Integer.MAX_VALUE - 1)
					.definition("definition")
					.organism("organism")
					.length(200)
					.countryFeature("Spain: Murcia")
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build())
					.locale(new Locale("es", "ES"))
					.pmids(newHashSet("1234R", "AV99O0"))
					.sequence(sequence1)
					.build();
			final SequenceKey leishmaniaKey1 = SequenceKey.builder()
					.dataSource(leishmania1.getDataSource())
					.accession(leishmania1.getAccession())
					.build();

			LEISHMANIA_DAO.insert(leishmania1);
			leishmania1.setLinks(null);

			// find element after insertion (hard link should be removed)
			leishmania2 = LEISHMANIA_DAO.find(leishmaniaKey1);
			assertThat("leishmania inserted with hard link is not null", leishmania2, notNullValue());
			assertThat("leishmania inserted with hard link coincides with expected", leishmania2, equalTo(leishmania1));
			System.out.println(leishmania2.toString());

			LEISHMANIA_DAO.delete(leishmaniaKey1);

			// update
			leishmania.setVersion("4.0");
			LEISHMANIA_DAO.update(leishmania);

			// find after update
			leishmania2 = LEISHMANIA_DAO.find(leishmaniaKey);
			assertThat("leishmania is not null", leishmania2, notNullValue());
			assertThat("leishmania coincides with original", leishmania2, equalTo(leishmania));
			System.out.println(leishmania2.toString());

			// search leishmania near a point and within a maximum distance
			List<Leishmania> leishmanias = LEISHMANIA_DAO.getNear(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build(), 
					10000.0d);
			assertThat("leishmania is not null", leishmanias, notNullValue());
			assertThat("ids is not empty", !leishmanias.isEmpty());

			// search leishmania within an area
			leishmanias = LEISHMANIA_DAO.geoWithin(Polygon.builder()
					.exteriorRing(LngLatAlt.builder().coordinates(-140.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 50.0d).build(),
							LngLatAlt.builder().coordinates(-140.0d, 30.0d).build()).build());
			assertThat("leishmania is not null", leishmanias, notNullValue());
			assertThat("ids is not empty", !leishmanias.isEmpty());

			// remove
			LEISHMANIA_DAO.delete(leishmaniaKey);
			final long numRecords = LEISHMANIA_DAO.count();
			assertThat("number of leishmania stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random random = new Random();
			final String[] countries = { "Kenya", "Senegal", "Pakistan", "Italy", "Greece", "France", "Tunisia", "Ethiopia", "Egypt", 
					"Spain", "Lebanon", "Oman", "Syrian Arab Republic", "Cyprus", "Portugal", "Morocco", "Turkey", "Malta", "Madagascar",
					"New Caledonia", "Brazil" };
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final GBSeq sequence3 = GBSEQ_XML_FACTORY.createGBSeq()
						.withGBSeqPrimaryAccession(Integer.toString(i))
						.withGBSeqAccessionVersion("1." + i)
						.withGBSeqOtherSeqids(GBSEQ_XML_FACTORY.createGBSeqOtherSeqids().withGBSeqid(GBSEQ_XML_FACTORY.createGBSeqid().withvalue(Integer.toString(i))))
						.withGBSeqOrganism("organism")
						.withGBSeqLength(Integer.toString(i * 123));
				final Set<String> gene = newHashSet();
				switch (i) {
				case 0:
					gene.add("abc");
					gene.add("def");
					break;
				case 1:
					gene.add("def");
					break;
				case 2:
					gene.add("abc");
					break;
				case 3:
					gene.add("abc");
					break;
				default:
					break;
				}
				final Point location = i%2 == 0 ? Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build() : null;
				final Leishmania leishmania3 = Leishmania.builder()
						.dataSource(GENBANK)
						.accession(Integer.toString(i))
						.version("1." + i)
						.definition("This is an example")
						.gi(i)
						.length(i * 123)
						.countryFeature(countries[random.nextInt(countries.length)])						
						.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
						.location(location)
						.gene(gene)
						.sequence(sequence3)						
						.build();
				ids.add(leishmania3.getAccession());
				LEISHMANIA_DAO.insert(leishmania3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			leishmanias = null;
			final MutableLong count = new MutableLong(0l);
			do {
				leishmanias = LEISHMANIA_DAO.list(start, size, null, null, null, count);
				if (leishmanias.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + leishmanias.size() + " of " + count.getValue() + " items");
				}
				start += leishmanias.size();
			} while (!leishmanias.isEmpty());
			
			// collection statistics
			final Map<String, List<SimpleStat>> stats = LEISHMANIA_DAO.collectionStats();
			assertThat("leishmania collection stats is not null", stats, notNullValue());
			// uncomment for additional output
			for (final Map.Entry<String, List<SimpleStat>> entry : stats.entrySet()) {
				System.err.println(" >> Field: " + entry.getKey());
				for (final SimpleStat stat : entry.getValue()) {
					System.err.println("   >> " + stat);
				}				
			}

			// filter: keyword matching search			
			ImmutableMap<String, String> filter = of("source", GENBANK);
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(numItems));			

			// filter: keyword matching search
			filter = of("accession", Integer.toString(random.nextInt(numItems)));
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(1));

			// filter: keyword matching search
			filter = of("locale", Locale.ENGLISH.toString());
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(numItems / 2));

			// filter: combined keyword matching search
			filter = of("source", GENBANK, "accession", Integer.toString(random.nextInt(numItems)));
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(1));

			// filter: full-text search
			filter = of("text", "an example");
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(numItems));

			// filter: combined full-text search with keyword matching search
			filter = of("source", GENBANK, "locale", Locale.ENGLISH.toString(), "text", "example");
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(numItems / 2));

			// invalid filter
			filter = of("filter_name", "filter_content");
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmania is not null", leishmanias, notNullValue());
			assertThat("number of filtered leishmania coincides with expected", leishmanias.size(), equalTo(0));

			// sorting by accession in ascending order
			Sorting sorting = Sorting.builder()
					.field("accession")
					.order(Order.ASC)
					.build();
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted leishmania is not null", leishmanias, notNullValue());
			assertThat("number of sorted leishmania coincides with expected", leishmanias.size(), equalTo(numItems));
			String last = "-1";
			for (final Leishmania seq : leishmanias) {
				assertThat("leishmania are properly sorted", seq.getAccession().compareTo(last) > 0);
				last = seq.getAccession();
			}

			// sorting by country in descending order
			sorting = Sorting.builder()
					.field("countryFeature")
					.order(Order.DESC)
					.build();
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted leishmania is not null", leishmanias, notNullValue());
			assertThat("number of sorted leishmania coincides with expected", leishmanias.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final Leishmania seq : leishmanias) {
				assertThat("leishmania are properly sorted", seq.getCountryFeature().compareTo(last) <= 0);
				last = seq.getCountryFeature();
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted leishmania is not null", leishmanias, notNullValue());
			assertThat("number of sorted leishmania coincides with expected", leishmanias.size(), equalTo(0));

			// projection
			leishmanias = LEISHMANIA_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(ORIGINAL_SEQUENCE_KEY, false), null);
			assertThat("projected leishmania is not null", leishmanias, notNullValue());
			assertThat("number of projected leishmania coincides with expected", leishmanias.size(), equalTo(numItems));
			assertThat("sequence was filtered from database response", leishmanias.get((new Random()).nextInt(numItems)).getSequence(), nullValue());

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				LEISHMANIA_DAO.delete(SequenceKey.builder()
						.dataSource(GENBANK)
						.accession(id2)
						.build());
			}
			LEISHMANIA_DAO.stats(System.out);			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LeishmaniaCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("LeishmaniaCollectionTest.test() has finished");
		}
	}
}