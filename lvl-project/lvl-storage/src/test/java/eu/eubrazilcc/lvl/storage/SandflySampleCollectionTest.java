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
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.dao.SandflySampleDAO.ORIGINAL_SAMPLE_KEY;
import static eu.eubrazilcc.lvl.storage.dao.SandflySampleDAO.SANDFLY_SAMPLE_DAO;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.Link;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.SandflySample;
import eu.eubrazilcc.lvl.core.SimpleStat;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests sandfly samples collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SandflySampleCollectionTest extends LeishvlTestCase {

	private static DatatypeFactory dtf;	

	public SandflySampleCollectionTest() {
		super(true);
	}

	@BeforeClass
	public static void setup() throws Exception {
		dtf = DatatypeFactory.newInstance();
	}

	@Test
	public void test() {
		System.out.println("SandflySampleCollectionTest.test()");
		try {
			// create sample
			final SimpleDarwinRecord sample = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-09-28T11:42:11"))
					.withCollectionID("221")
					.withInstitutionCode("Fiocruz")
					.withCollectionCode("Fiocruz-COLFLEB")
					.withBasisOfRecord("S")
					.withOccurrenceID("524447531")
					.withCatalogNumber("051/75")
					.withRecordNumber("051/75")
					.withIndividualCount(BigInteger.valueOf(19))
					.withOccurrenceRemarks("COLETA: Hora Ini: 18:00:00 Hora Fim: 08:20:00 ;")
					.withYear(yearAsXMLGregorianCalendar(1975))
					.withContinent("América do Sul")
					.withCountry("Brasil")
					.withStateProvince("Minas Gerais")
					.withCounty("Caratinga")
					.withLocality("Córrego Barracão")
					.withDecimalLatitude(38.081473d)
					.withDecimalLongitude(-122.913837d)
					.withScientificName("Nyssomyia whitmani")
					.withKingdom("Animalia")
					.withPhylum("Arthropoda")
					.withClazz("Insecta")
					.withOrder("Diptera")
					.withFamily("Psychodidae")
					.withGenus("Nyssomyia")
					.withSpecificEpithet("whitmani");

			// insert
			final SandflySample sandflySample = SandflySample.builder()
					.collectionId(COLFLEB)
					.catalogNumber(sample.getCatalogNumber())											
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(sample.getDecimalLongitude(), sample.getDecimalLatitude()).build()).build())
					.locale(new Locale("pt", "BR"))
					.sample(sample)
					.build();
			final SampleKey sandflySampleKey = SampleKey.builder()
					.collectionId(sandflySample.getCollectionId())
					.catalogNumber(sandflySample.getCatalogNumber())
					.build();
			final WriteResult<SandflySample> ack = SANDFLY_SAMPLE_DAO.insert(sandflySample);
			assertThat("write ack is not null", ack, notNullValue());
			assertThat("database id is not empty", trim(ack.getId()), allOf(notNullValue(), not(equalTo(""))));
			printMsg(" >> New record inserted: key=" + sandflySampleKey.toString() + ", id=" + ack.getId() 
			+ ", record=" + toJson(sandflySample, JSON_PRETTY_PRINTER));

			// find all
			final List<SandflySample> all = SANDFLY_SAMPLE_DAO.findAll();
			printMsg(" >> ALL\n" + toJson(all, JSON_PRETTY_PRINTER));

			// find
			SandflySample sandflySample2 = SANDFLY_SAMPLE_DAO.find(sandflySampleKey);
			assertThat("sandflySample is not null", sandflySample2, notNullValue());
			assertThat("sandflySample coincides with original", sandflySample2, equalTo(sandflySample));
			assertThat("sandflySample contains original sample", sandflySample2.getSample(), notNullValue());
			System.out.println(sandflySample2.toString());

			// duplicates are not allowed
			try {
				SANDFLY_SAMPLE_DAO.insert(sandflySample2);
				fail("Duplicate sandflySamples are not allowed");
			} catch (Exception e) {
				System.out.println("Exception caught while trying to insert a duplicate sandflySample");
			}

			// insert element with hard link
			final SimpleDarwinRecord sample1 = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-09-28T11:42:11"))
					.withCollectionID("221")
					.withInstitutionCode("Fiocruz")
					.withCollectionCode("Fiocruz-COLFLEB")
					.withBasisOfRecord("S")
					.withOccurrenceID("123")
					.withCatalogNumber("123/15")
					.withRecordNumber("123/15")
					.withIndividualCount(BigInteger.valueOf(19))
					.withOccurrenceRemarks("COLETA: Hora Ini: 18:00:00 Hora Fim: 08:20:00 ;")
					.withYear(yearAsXMLGregorianCalendar(1975))
					.withContinent("América do Sul")
					.withCountry("Brasil")
					.withStateProvince("Minas Gerais")
					.withCounty("Caratinga")
					.withLocality("Córrego Barracão")
					.withDecimalLatitude(38.081473d)
					.withDecimalLongitude(-122.913837d)
					.withScientificName("Nyssomyia whitmani")
					.withKingdom("Animalia")
					.withPhylum("Arthropoda")
					.withClazz("Insecta")
					.withOrder("Diptera")
					.withFamily("Psychodidae")
					.withGenus("Nyssomyia")
					.withSpecificEpithet("whitmani");
			final SandflySample sandflySample1 = SandflySample.builder()
					.links(newArrayList(Link.fromUri("http://example.com/sandflySample/fcf:123%2F15").rel(SELF).type(APPLICATION_JSON).build()))
					.collectionId(COLFLEB)
					.catalogNumber(sample1.getCatalogNumber())
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(sample1.getDecimalLongitude(), sample1.getDecimalLatitude()).build()).build())
					.locale(new Locale("pt", "BR"))
					.sample(sample1)
					.build();
			final SampleKey sandflySampleKey1 = SampleKey.builder()
					.collectionId(sandflySample1.getCollectionId())
					.catalogNumber(sandflySample1.getCatalogNumber())
					.build();

			SANDFLY_SAMPLE_DAO.insert(sandflySample1);
			sandflySample1.setLinks(null);

			// find element after insertion (hard link should be removed)
			sandflySample2 = SANDFLY_SAMPLE_DAO.find(sandflySampleKey1);
			assertThat("sandflySample inserted with hard link is not null", sandflySample2, notNullValue());
			assertThat("sandflySample inserted with hard link coincides with expected", sandflySample2, equalTo(sandflySample1));
			System.out.println(sandflySample2.toString());

			SANDFLY_SAMPLE_DAO.delete(sandflySampleKey1);

			// update
			sandflySample.getSample().setModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T11:42:11"));
			SANDFLY_SAMPLE_DAO.update(sandflySample);

			// find after update
			sandflySample2 = SANDFLY_SAMPLE_DAO.find(sandflySampleKey);
			assertThat("sandflySample is not null", sandflySample2, notNullValue());
			assertThat("sandflySample coincides with original", sandflySample2, equalTo(sandflySample));
			System.out.println(sandflySample2.toString());

			// search sandflySample near a point and within a maximum distance
			List<SandflySample> sandflySamples = SANDFLY_SAMPLE_DAO.getNear(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build(), 
					10000.0d);
			assertThat("sandflySample is not null", sandflySamples, notNullValue());
			assertThat("ids is not empty", !sandflySamples.isEmpty());

			// search sandflySample within an area
			sandflySamples = SANDFLY_SAMPLE_DAO.geoWithin(Polygon.builder()
					.exteriorRing(LngLatAlt.builder().coordinates(-140.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 50.0d).build(),
							LngLatAlt.builder().coordinates(-140.0d, 30.0d).build()).build());
			assertThat("sandflySample is not null", sandflySamples, notNullValue());
			assertThat("ids is not empty", !sandflySamples.isEmpty());

			// remove
			SANDFLY_SAMPLE_DAO.delete(sandflySampleKey);
			final long numRecords = SANDFLY_SAMPLE_DAO.count();
			assertThat("number of sandflySample stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random random = new Random();
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final SimpleDarwinRecord sample3 = DWC_XML_FACTORY.createSimpleDarwinRecord()
						.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-09-28T11:42:11"))
						.withCollectionID("221")
						.withInstitutionCode("Fiocruz")
						.withCollectionCode("Fiocruz-COLFLEB")
						.withBasisOfRecord("S")
						.withOccurrenceID(Integer.toString(i))
						.withCatalogNumber(Integer.toString(i) + "/15")
						.withYear(yearAsXMLGregorianCalendar(1975 + i))
						.withRecordNumber(Integer.toString(i) + "/15")
						.withStateProvince("This is an example");
				final Point location = i%2 == 0 ? Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build() : null;
				final SandflySample sandflySample3 = SandflySample.builder()
						.collectionId(COLFLEB)
						.catalogNumber(sample3.getCatalogNumber())													
						.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
						.location(location)
						.sample(sample3)
						.build();
				ids.add(sandflySample3.getCatalogNumber());
				SANDFLY_SAMPLE_DAO.insert(sandflySample3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			sandflySamples = null;
			final MutableLong count = new MutableLong(0l);
			do {
				sandflySamples = SANDFLY_SAMPLE_DAO.list(start, size, null, null, null, count);
				if (sandflySamples.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + sandflySamples.size() + " of " + count.getValue() + " items");
				}
				start += sandflySamples.size();
			} while (!sandflySamples.isEmpty());

			// collection statistics
			final Map<String, List<SimpleStat>> stats = SANDFLY_SAMPLE_DAO.collectionStats();
			assertThat("sandflySample collection stats is not null", stats, notNullValue());
			// uncomment for additional output
			for (final Map.Entry<String, List<SimpleStat>> entry : stats.entrySet()) {
				System.err.println(" >> Field: " + entry.getKey());
				for (final SimpleStat stat : entry.getValue()) {
					System.err.println("   >> " + stat);
				}				
			}

			// filter: keyword matching search			
			ImmutableMap<String, String> filter = of("collection", COLFLEB);
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems));			

			// filter: keyword matching search
			filter = of("catalogNumber", Integer.toString(random.nextInt(numItems)) + "/15");
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(1));

			// filter: keyword matching search
			filter = of("locale", Locale.ENGLISH.toString()); // language
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems / 2));

			filter = of("locale", "_" + Locale.FRANCE.getCountry()); // country
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), 
					anyOf(equalTo(numItems / 2), equalTo((numItems / 2) + 1)));

			filter = of("locale", Locale.FRANCE.toString()); // exact match
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), 
					anyOf(equalTo(numItems / 2), equalTo((numItems / 2) + 1)));

			// filter: combined keyword matching search
			filter = of("collection", COLFLEB, "catalogNumber", Integer.toString(random.nextInt(numItems)) + "/15");
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(1));
			
			// filter: full-text search
			filter = of("text", "an example");
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems));

			// filter: combined full-text search with keyword matching search
			filter = of("collection", COLFLEB, "locale", Locale.ENGLISH.toString(), "text", "example");
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems / 2));

			// invalid filter
			filter = of("filter_name", "filter_content");
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of filtered sandflySample coincides with expected", sandflySamples.size(), equalTo(0));

			// sorting by catalogNumber in ascending order
			Sorting sorting = Sorting.builder()
					.field("catalogNumber")
					.order(Order.ASC)
					.build();
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of sorted sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems));
			String last = "-1";
			for (final SandflySample cs : sandflySamples) {
				assertThat("sandflySample are properly sorted", cs.getCatalogNumber().compareTo(last) > 0);
				last = cs.getCatalogNumber();
			}

			// sorting by year in descending order
			sorting = Sorting.builder()
					.field("year")
					.order(Order.DESC)
					.build();
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of sorted sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final SandflySample cs : sandflySamples) {
				assertThat("sandflySample are properly sorted", Integer.toString(cs.getSample().getYear().getYear()).compareTo(last) <= 0);
				last = Integer.toString(cs.getSample().getYear().getYear());
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of sorted sandflySample coincides with expected", sandflySamples.size(), equalTo(0));

			// projection
			sandflySamples = SANDFLY_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(ORIGINAL_SAMPLE_KEY, false), null);
			assertThat("projected sandflySample is not null", sandflySamples, notNullValue());
			assertThat("number of projected sandflySample coincides with expected", sandflySamples.size(), equalTo(numItems));
			assertThat("sample was filtered from database response", sandflySamples.get((new Random()).nextInt(numItems)).getSample(), nullValue());

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				SANDFLY_SAMPLE_DAO.delete(SampleKey.builder()
						.collectionId(COLFLEB)
						.catalogNumber(id2)
						.build());
			}
			SANDFLY_SAMPLE_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SandflySampleCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SandflySampleCollectionTest.test() has finished");
		}
	}

	private static XMLGregorianCalendar yearAsXMLGregorianCalendar(final int year) {
		final GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.YEAR, year);
		return dtf.newXMLGregorianCalendar(gc);
	}

}