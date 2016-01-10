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
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.XmlHelper.yearAsXMLGregorianCalendar;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaPendingDAO.LEISHMANIA_PENDING_DAO;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaPendingDAO.ORIGINAL_SAMPLE_KEY;
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

import java.util.List;
import java.util.Random;

import javax.ws.rs.core.Link;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.LeishmaniaPending;
import eu.eubrazilcc.lvl.core.SamplePreparation;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.SubmissionRequest.SubmissionResolution;
import eu.eubrazilcc.lvl.core.SubmissionRequest.SubmissionStatus;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests pending sequences collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishmaniaPendingCollectionTest extends LeishvlTestCase {

	public LeishmaniaPendingCollectionTest() {
		super(true);
	}

	@Test
	public void test() {
		printMsg("LeishmaniaPendingCollectionTest.test()");
		try {
			// create sample
			final SimpleDarwinRecord sample = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-12-01T13:50:08"))
					.withInstitutionCode("ISCIII-WHO-CCL")
					.withCollectionCode("ISCIII-Leishmaniasis-collection")
					.withCatalogNumber("LVLLE0001")
					.withRecordedBy("username")
					.withYear(yearAsXMLGregorianCalendar(2015))
					.withContinent("Europe")
					.withCountry("Spain")
					.withStateProvince("Madrid")
					.withCounty("Madrid")
					.withLocality("Fuenlabrada")
					.withDecimalLatitude(38.081473d)
					.withDecimalLongitude(-122.913837d)
					.withScientificName("Leishmania infantum")
					.withPhylum("Euglenozoa")
					.withClazz("Kinetoplastea")
					.withOrder("Trypanosomatida")
					.withFamily("Trypanosomatidae")
					.withGenus("Leishmania")
					.withSpecificEpithet("infantum");

			// insert
			final LeishmaniaPending pendingSeq = LeishmaniaPending.builder()
					.id("LVLLE0001")
					.namespace("username")
					.sample(sample)
					.sequence("GCGAAGAGGCTGGGCCAGAGAAAGCAAGACACGAGATGAAGCGGAGGGACACACACACACACACACACATACACACACACACACACACCTCCTCTACCAGAAGGAAAACG")
					.preparation(SamplePreparation.builder()
							.sex("Female")
							.individualCount(23)
							.collectingMethod("CDC")
							.preparationType("Mata")
							.materialType("Parátipo")
							.build())
					.build();
			final WriteResult<LeishmaniaPending> ack = LEISHMANIA_PENDING_DAO.insert(pendingSeq);
			assertThat("write ack is not null", ack, notNullValue());
			assertThat("database id is not empty", trim(ack.getId()), allOf(notNullValue(), not(equalTo(""))));
			printMsg(" >> New record inserted: id=" + ack.getId() + ", record=" + toJson(pendingSeq, JSON_PRETTY_PRINTER));			

			// find
			LeishmaniaPending pendingSeq2 = LEISHMANIA_PENDING_DAO.find(pendingSeq.getId());
			assertThat("pendingSeq is not null", pendingSeq2, notNullValue());
			assertThat("pendingSeq coincides with original", pendingSeq2, equalTo(pendingSeq));
			assertThat("pendingSeq contains original sample", pendingSeq2.getSample(), notNullValue());
			printMsg(" >> Found:\n" + toJson(pendingSeq2, JSON_PRETTY_PRINTER));

			// duplicates are not allowed
			try {
				LEISHMANIA_PENDING_DAO.insert(pendingSeq2);
				fail("Duplicate pendingSeqs are not allowed");
			} catch (Exception e) {
				printMsg("Exception caught while trying to insert a duplicate pendingSeq");
			}

			// insert element with hard link
			final SimpleDarwinRecord sample1 = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-12-02T13:50:08"))
					.withInstitutionCode("ISCIII-WHO-CCL")
					.withCollectionCode("ISCIII-Leishmaniasis-collection")
					.withCatalogNumber("LVLLE0002")
					.withRecordedBy("username")
					.withYear(yearAsXMLGregorianCalendar(2015))
					.withContinent("Europe")
					.withCountry("Spain")
					.withStateProvince("Madrid")
					.withCounty("Madrid")
					.withLocality("Fuenlabrada")
					.withDecimalLatitude(38.081473d)
					.withDecimalLongitude(-122.913837d)
					.withScientificName("Leishmania infantum")
					.withPhylum("Euglenozoa")
					.withClazz("Kinetoplastea")
					.withOrder("Trypanosomatida")
					.withFamily("Trypanosomatidae")
					.withGenus("Leishmania")
					.withSpecificEpithet("infantum");
			final LeishmaniaPending pendingSeq1 = LeishmaniaPending.builder()
					.links(newArrayList(Link.fromUri("http://example.com/pending/sequence/lvl0002").rel(SELF).type(APPLICATION_JSON).build()))
					.id("LVLLE0002")
					.namespace("username")
					.sample(sample1)
					.sequence("GCGAAGAGGCTGGGCCAGAGAAAGCAAGACACGAGATGAAGCGGAGGGACACACACACACACACACACATACACACACACACACACACCTCCTCTACCAGAAGGAAAACG")
					.preparation(SamplePreparation.builder()
							.sex("Female")
							.individualCount(23)
							.collectingMethod("CDC")
							.preparationType("Mata")
							.materialType("Parátipo")
							.build())
					.build();
			LEISHMANIA_PENDING_DAO.insert(pendingSeq1);
			pendingSeq1.setLinks(null);			

			// find element after insertion (hard link should be removed)
			pendingSeq2 = LEISHMANIA_PENDING_DAO.find(pendingSeq1.getId());
			assertThat("pendingSeq inserted with hard link is not null", pendingSeq2, notNullValue());
			assertThat("pendingSeq inserted with hard link coincides with expected", pendingSeq2, equalTo(pendingSeq1));
			printMsg(" >> Found element inserted with links:\n" + toJson(pendingSeq2, JSON_PRETTY_PRINTER));

			// find all
			final List<LeishmaniaPending> all = LEISHMANIA_PENDING_DAO.findAll();
			printMsg(" >> List all:\n" + toJson(all, JSON_PRETTY_PRINTER));

			// delete
			LEISHMANIA_PENDING_DAO.delete(pendingSeq1.getId());

			// update
			pendingSeq.getSample().setModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T11:42:11"));
			pendingSeq.setAssignedTo("curator@lvl");
			pendingSeq.setResolution(SubmissionResolution.ACCEPTED);
			pendingSeq.setStatus(SubmissionStatus.CLOSED);
			pendingSeq.setAllocatedCollection("leishmaniaPending");
			pendingSeq.setAllocatedId("123");
			LEISHMANIA_PENDING_DAO.update(pendingSeq);

			// find after update
			pendingSeq2 = LEISHMANIA_PENDING_DAO.find(pendingSeq.getId());
			assertThat("pendingSeq is not null", pendingSeq2, notNullValue());
			assertThat("pendingSeq coincides with original", pendingSeq2, equalTo(pendingSeq));
			printMsg(" >> Found element after update:\n" + toJson(pendingSeq2, JSON_PRETTY_PRINTER));

			// remove
			LEISHMANIA_PENDING_DAO.delete(pendingSeq.getId());
			final long numRecords = LEISHMANIA_PENDING_DAO.count();
			assertThat("number of pendingSeq stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final Random rand = new Random();
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final SimpleDarwinRecord sample3 = DWC_XML_FACTORY.createSimpleDarwinRecord()
						.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T13:50:08"))
						.withInstitutionCode("ISCIII-WHO-CCL")
						.withCollectionCode("ISCIII-Leishmaniasis-collection")
						.withCatalogNumber("LVLLE000" + Integer.toString(i))
						.withYear(yearAsXMLGregorianCalendar(1975 + i))
						.withStateProvince("This is an example");
				final LeishmaniaPending pendingSeq3 = LeishmaniaPending.builder()
						.namespace("username")
						.id(sample3.getCatalogNumber())						
						.sample(sample3)
						.sequence("CCCC")
						.build();
				if (i%2 == 0) pendingSeq3.setStatus(SubmissionStatus.values()[rand.nextInt(SubmissionStatus.values().length)]);
				ids.add(pendingSeq3.getId());
				LEISHMANIA_PENDING_DAO.insert(pendingSeq3);
			}

			// pagination
			final int size = 3;
			int start = 0;
			List<LeishmaniaPending> pendingSeqs = null;
			final MutableLong count = new MutableLong(0l);
			do {
				pendingSeqs = LEISHMANIA_PENDING_DAO.list(start, size, null, null, null, count);
				if (pendingSeqs.size() != 0) {
					printMsg("Paging: first item " + start + ", showing " + pendingSeqs.size() + " of " + count.getValue() + " items");
				}
				start += pendingSeqs.size();
			} while (!pendingSeqs.isEmpty());

			// find submitted records only
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, null, null, null, null, "username", true);
			assertThat("submitted records only pendingRef is not null", pendingSeqs, notNullValue());
			assertThat("number of submitted records only pendingRef coincides with expected", pendingSeqs.size(), equalTo(6));
			for (final LeishmaniaPending pr : pendingSeqs) {
				assertThat("submitted records contains a valid status", pr.getStatus(), notNullValue());
			}

			// filter: full-text search
			ImmutableMap<String, String> filter = of("text", "an example");
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered pendingSeq is not null", pendingSeqs, notNullValue());
			assertThat("number of filtered pendingSeq coincides with expected", pendingSeqs.size(), equalTo(numItems));

			// invalid filter
			filter = of("filter_name", "filter_content");
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, filter, null, null, null);
			assertThat("filtered pendingSeq is not null", pendingSeqs, notNullValue());
			assertThat("number of filtered pendingSeq coincides with expected", pendingSeqs.size(), equalTo(0));

			// sorting by year in ascending order
			Sorting sorting = Sorting.builder()
					.field("year")
					.order(Order.ASC)
					.build();
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted pendingSeq is not null", pendingSeqs, notNullValue());
			assertThat("number of sorted pendingSeq coincides with expected", pendingSeqs.size(), equalTo(numItems));
			String last = "-1";
			for (final LeishmaniaPending cs : pendingSeqs) {
				assertThat("pendingSeq are properly sorted", Integer.toString(cs.getSample().getYear().getYear()).compareTo(last) >= 0);
				last = Integer.toString(cs.getSample().getYear().getYear());
			}

			// sorting by year in descending order
			sorting = Sorting.builder()
					.field("year")
					.order(Order.DESC)
					.build();
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted pendingSeq is not null", pendingSeqs, notNullValue());
			assertThat("number of sorted pendingSeq coincides with expected", pendingSeqs.size(), equalTo(numItems));
			last = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
			for (final LeishmaniaPending cs : pendingSeqs) {
				assertThat("pendingSeq are properly sorted", Integer.toString(cs.getSample().getYear().getYear()).compareTo(last) <= 0);
				last = Integer.toString(cs.getSample().getYear().getYear());
			}

			// invalid sort
			sorting = Sorting.builder()
					.field("sort_name")
					.order(Order.DESC)
					.build();
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, null, sorting, null, null);
			assertThat("sorted pendingSeq is not null", pendingSeqs, notNullValue());
			assertThat("number of sorted pendingSeq coincides with expected", pendingSeqs.size(), equalTo(0));

			// projection
			pendingSeqs = LEISHMANIA_PENDING_DAO.list(0, Integer.MAX_VALUE, null, null, ImmutableMap.of(ORIGINAL_SAMPLE_KEY, false), null);
			assertThat("projected pendingSeq is not null", pendingSeqs, notNullValue());
			assertThat("number of projected pendingSeq coincides with expected", pendingSeqs.size(), equalTo(numItems));
			assertThat("sample was filtered from database response", pendingSeqs.get((new Random()).nextInt(numItems)).getSample(), nullValue());

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				LEISHMANIA_PENDING_DAO.delete(id2);
			}
			LEISHMANIA_PENDING_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LeishmaniaPendingCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("LeishmaniaPendingCollectionTest.test() has finished");
		}
	}

}