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
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.XmlHelper.yearAsXMLGregorianCalendar;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaSampleDAO.LEISHMANIA_SAMPLE_DAO;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaSampleDAO.ORIGINAL_SAMPLE_KEY;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.LeishmaniaSample;
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
 * Tests leishmania samples collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishmaniaSampleCollectionTest extends LeishvlTestCase {

	public LeishmaniaSampleCollectionTest() {
		super(false);
	}

	@Test
	public void test() {
		System.out.println("LeishmaniaSampleCollectionTest.test()");
		try {
			// create sample
			final SimpleDarwinRecord sample = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2010-11-18T13:50:08"))
					.withCollectionID("199")
					.withInstitutionCode("Fiocruz")
					.withCollectionCode("Fiocruz-CLIOC")
					.withBasisOfRecord("L")
					.withOccurrenceID("523692851")
					.withCatalogNumber("IOCL 0001")
					.withRecordedBy("001 - Mauro Célio de Almeida Marzochi")
					.withYear(yearAsXMLGregorianCalendar(1979))
					.withCountry("Brasil")
					.withStateProvince("Rio de Janeiro")
					.withCounty("Rio de Janeiro")
					.withLocality("Jacarepaguá")
					.withDecimalLatitude(38.081473d)
					.withDecimalLongitude(-122.913837d)
					.withIdentifiedBy("Elisa Cupolillo")
					.withScientificName("Leishmania (Viannia) braziliensis")
					.withPhylum("Euglenozoa")
					.withClazz("Kinetoplastea")
					.withOrder("Trypanosomatida")
					.withFamily("Trypanosomatidae")
					.withGenus("Leishmania")
					.withSpecificEpithet("braziliensis");

			// insert
			final LeishmaniaSample leishmaniaSample = LeishmaniaSample.builder()
					.collectionId(CLIOC)
					.catalogNumber(sample.getCatalogNumber())											
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(sample.getDecimalLongitude(), sample.getDecimalLatitude()).build()).build())
					.locale(new Locale("pt", "BR"))
					.sample(sample)
					.build();
			final SampleKey leishmaniaSampleKey = SampleKey.builder()
					.collectionId(leishmaniaSample.getCollectionId())
					.catalogNumber(leishmaniaSample.getCatalogNumber())
					.build();
			final WriteResult<LeishmaniaSample> ack = LEISHMANIA_SAMPLE_DAO.insert(leishmaniaSample);
			assertThat("write ack is not null", ack, notNullValue());
			assertThat("database id is not empty", trim(ack.getId()), allOf(notNullValue(), not(equalTo(""))));
			printMsg(" >> New record inserted: key=" + leishmaniaSampleKey.toString() + ", id=" + ack.getId() 
			+ ", record=" + toJson(leishmaniaSample, JSON_PRETTY_PRINTER));

			// find all
			final List<LeishmaniaSample> all = LEISHMANIA_SAMPLE_DAO.findAll();
			printMsg(" >> ALL\n" + toJson(all, JSON_PRETTY_PRINTER));

			// find
			LeishmaniaSample leishmaniaSample2 = LEISHMANIA_SAMPLE_DAO.find(leishmaniaSampleKey);
			assertThat("leishmaniaSample is not null", leishmaniaSample2, notNullValue());
			assertThat("leishmaniaSample coincides with original", leishmaniaSample2, equalTo(leishmaniaSample));
			assertThat("leishmaniaSample contains original sample", leishmaniaSample2.getSample(), notNullValue());
			System.out.println(leishmaniaSample2.toString());

			// duplicates are not allowed
			try {
				LEISHMANIA_SAMPLE_DAO.insert(leishmaniaSample2);
				fail("Duplicate leishmaniaSamples are not allowed");
			} catch (Exception e) {
				System.out.println("Exception caught while trying to insert a duplicate leishmaniaSample");
			}

			// insert element with hard link
			final SimpleDarwinRecord sample1 = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T13:50:08"))
					.withCollectionID("199")
					.withInstitutionCode("Fiocruz")
					.withCollectionCode("Fiocruz-CLIOC")
					.withBasisOfRecord("L")
					.withOccurrenceID("523692851")
					.withCatalogNumber("IOCL 1234")
					.withRecordedBy("001 - Mauro Célio de Almeida Marzochi")
					.withYear(yearAsXMLGregorianCalendar(1979))
					.withCountry("Brasil")
					.withStateProvince("Rio de Janeiro")
					.withCounty("Rio de Janeiro")
					.withLocality("Jacarepaguá")
					.withDecimalLatitude(38.081473d)
					.withDecimalLongitude(-122.913837d)
					.withIdentifiedBy("Elisa Cupolillo")
					.withScientificName("Leishmania (Viannia) braziliensis")
					.withPhylum("Euglenozoa")
					.withClazz("Kinetoplastea")
					.withOrder("Trypanosomatida")
					.withFamily("Trypanosomatidae")
					.withGenus("Leishmania")
					.withSpecificEpithet("braziliensis");
			final LeishmaniaSample leishmaniaSample1 = LeishmaniaSample.builder()
					.links(newArrayList(Link.fromUri("http://example.com/leishmaniaSample/fcc:IOCL%201234").rel(SELF).type(APPLICATION_JSON).build()))
					.collectionId(CLIOC)
					.catalogNumber(sample1.getCatalogNumber())
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(sample1.getDecimalLongitude(), sample1.getDecimalLatitude()).build()).build())
					.locale(new Locale("pt", "BR"))
					.sample(sample1)
					.build();
			final SampleKey leishmaniaSampleKey1 = SampleKey.builder()
					.collectionId(leishmaniaSample1.getCollectionId())
					.catalogNumber(leishmaniaSample1.getCatalogNumber())
					.build();

			LEISHMANIA_SAMPLE_DAO.insert(leishmaniaSample1);
			leishmaniaSample1.setLinks(null);

			// find element after insertion (hard link should be removed)
			leishmaniaSample2 = LEISHMANIA_SAMPLE_DAO.find(leishmaniaSampleKey1);
			assertThat("leishmaniaSample inserted with hard link is not null", leishmaniaSample2, notNullValue());
			assertThat("leishmaniaSample inserted with hard link coincides with expected", leishmaniaSample2, equalTo(leishmaniaSample1));
			System.out.println(leishmaniaSample2.toString());

			LEISHMANIA_SAMPLE_DAO.delete(leishmaniaSampleKey1);

			// update
			leishmaniaSample.getSample().setModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T11:42:11"));
			LEISHMANIA_SAMPLE_DAO.update(leishmaniaSample);

			// find after update
			leishmaniaSample2 = LEISHMANIA_SAMPLE_DAO.find(leishmaniaSampleKey);
			assertThat("leishmaniaSample is not null", leishmaniaSample2, notNullValue());
			assertThat("leishmaniaSample coincides with original", leishmaniaSample2, equalTo(leishmaniaSample));
			System.out.println(leishmaniaSample2.toString());

			// search leishmaniaSample near a point and within a maximum distance
			List<LeishmaniaSample> leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.getNear(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build(), 
					10000.0d);
			assertThat("leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("ids is not empty", !leishmaniaSamples.isEmpty());

			// search leishmaniaSample within an area
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.geoWithin(Polygon.builder()
					.exteriorRing(LngLatAlt.builder().coordinates(-140.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 30.0d).build(),
							LngLatAlt.builder().coordinates(-110.0d, 50.0d).build(),
							LngLatAlt.builder().coordinates(-140.0d, 30.0d).build()).build());
			assertThat("leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("ids is not empty", !leishmaniaSamples.isEmpty());

			// remove
			LEISHMANIA_SAMPLE_DAO.delete(leishmaniaSampleKey);
			final long numRecords = LEISHMANIA_SAMPLE_DAO.count();
			assertThat("number of leishmaniaSample stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random random = new Random();
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final SimpleDarwinRecord sample3 = DWC_XML_FACTORY.createSimpleDarwinRecord()
						.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T13:50:08"))
						.withCollectionID("199")
						.withInstitutionCode("Fiocruz")
						.withCollectionCode("Fiocruz-CLIOC")
						.withBasisOfRecord("L")
						.withOccurrenceID(Integer.toString(i))
						.withCatalogNumber("IOCL 000" + Integer.toString(i))
						.withYear(yearAsXMLGregorianCalendar(1975 + i))
						.withRecordNumber("IOCL 000" + Integer.toString(i))
						.withStateProvince("This is an example");
				final Point location = i%2 == 0 ? Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build() : null;
				final LeishmaniaSample leishmaniaSample3 = LeishmaniaSample.builder()
						.collectionId(CLIOC)
						.catalogNumber(sample3.getCatalogNumber())													
						.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
						.location(location)
						.sample(sample3)
						.build();
				ids.add(leishmaniaSample3.getCatalogNumber());
				LEISHMANIA_SAMPLE_DAO.insert(leishmaniaSample3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			leishmaniaSamples = null;
			final MutableLong count = new MutableLong(0l);
			do {
				leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(start, size, null, null, null, count);
				if (leishmaniaSamples.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + leishmaniaSamples.size() + " of " + count.getValue() + " items");
				}
				start += leishmaniaSamples.size();
			} while (!leishmaniaSamples.isEmpty());

			// collection statistics
			final Map<String, List<SimpleStat>> stats = LEISHMANIA_SAMPLE_DAO.collectionStats();
			assertThat("leishmaniaSample collection stats is not null", stats, notNullValue());
			// uncomment for additional output
			for (final Map.Entry<String, List<SimpleStat>> entry : stats.entrySet()) {
				System.err.println(" >> Field: " + entry.getKey());
				for (final SimpleStat stat : entry.getValue()) {
					System.err.println("   >> " + stat);
				}				
			}

			// filter: keyword matching search			
			ImmutableMap<String, String> filter = of("collectionId", CLIOC);
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems));			

			// filter: keyword matching search
			filter = of("catalogNumber", "IOCL 000" + Integer.toString(random.nextInt(numItems)));
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(1));

			// filter: keyword matching search
			filter = of("locale", Locale.ENGLISH.toString()); // language
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems / 2));

			filter = of("locale", "_" + Locale.FRANCE.getCountry()); // country
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), 
					anyOf(equalTo(numItems / 2), equalTo((numItems / 2) + 1)));

			filter = of("locale", Locale.FRANCE.toString()); // exact match
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), 
					anyOf(equalTo(numItems / 2), equalTo((numItems / 2) + 1)));

			// filter: combined keyword matching search
			filter = of("collectionId", CLIOC, "catalogNumber", "IOCL 000" + Integer.toString(random.nextInt(numItems)));
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(1));

			// filter: full-text search
			filter = of("text", "an example");
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems));

			// filter: combined full-text search with keyword matching search
			filter = of("collectionId", CLIOC, "locale", Locale.ENGLISH.toString(), "text", "example");
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems / 2));

			// invalid filter
			filter = of("filter_name", "filter_content");
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of filtered leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(0));

			// sorting by catalogNumber in ascending order
			Sorting sorting = Sorting.builder()
					.field("catalogNumber")
					.order(Order.ASC)
					.build();
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of sorted leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems));
			String last = "-1";
			for (final LeishmaniaSample cs : leishmaniaSamples) {
				assertThat("leishmaniaSample are properly sorted", cs.getCatalogNumber().compareTo(last) > 0);
				last = cs.getCatalogNumber();
			}

			// sorting by year in descending order
			sorting = Sorting.builder()
					.field("year")
					.order(Order.DESC)
					.build();
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of sorted leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final LeishmaniaSample cs : leishmaniaSamples) {
				assertThat("leishmaniaSample are properly sorted", Integer.toString(cs.getSample().getYear().getYear()).compareTo(last) <= 0);
				last = Integer.toString(cs.getSample().getYear().getYear());
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of sorted leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(0));

			// projection
			leishmaniaSamples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(ORIGINAL_SAMPLE_KEY, false), null);
			assertThat("projected leishmaniaSample is not null", leishmaniaSamples, notNullValue());
			assertThat("number of projected leishmaniaSample coincides with expected", leishmaniaSamples.size(), equalTo(numItems));
			assertThat("sample was filtered from database response", leishmaniaSamples.get((new Random()).nextInt(numItems)).getSample(), nullValue());

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				LEISHMANIA_SAMPLE_DAO.delete(SampleKey.builder()
						.collectionId(CLIOC)
						.catalogNumber(id2)
						.build());
			}
			LEISHMANIA_SAMPLE_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LeishmaniaSampleCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("LeishmaniaSampleCollectionTest.test() has finished");
		}
	}

}