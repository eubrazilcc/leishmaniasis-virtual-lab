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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.CollectionNames.SANDFLY_COLLECTION;
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getDarwinCoreSets;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getPubMedXMLFiles;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XMLB;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XMLB;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.support.Issue;
import eu.eubrazilcc.lvl.core.support.IssueStatus;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecordSet;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests JSON mapping capabilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class JsonMappingTest extends LeishvlTestCase {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	
	public JsonMappingTest() {
		super(false);		
	}

	@Test
	public void test() {
		printMsg("JsonMappingTest.test()");
		try {
			// create test dataset
			final Link seqLink = Link.fromUri(UriBuilder.fromUri("http://localhost/sanfly").path("gb:ABC12345678").build())
					.rel(SELF).type(APPLICATION_JSON).build();
			final Link refLink = Link.fromUri(UriBuilder.fromUri("http://localhost/paper").path("ADGJ87950").build())
					.rel(SELF).type(APPLICATION_JSON).build();
			final Link sampleLink = Link.fromUri(UriBuilder.fromUri("http://localhost/sample").path("sample:IOCL 123").build())
					.rel(SELF).type(APPLICATION_JSON).build();

			final Point point = Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build();

			final GBSeq gbSeq = GBSEQ_XMLB.typeFromFile(getGBSeqXMLFiles().iterator().next());
			assertThat("GenBank sequence is not null", gbSeq, notNullValue());

			final PubmedArticle article = PUBMED_XMLB.typeFromFile(getPubMedXMLFiles().iterator().next());
			assertThat("PubMed article is not null", article, notNullValue());

			final Sandfly sandfly = Sandfly.builder()
					.dataSource(GENBANK)
					.accession("ABC12345678")
					.version("3.0")
					.gi(1239841)
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(point)
					.locale(new Locale("es", "ES"))
					.pmids(newHashSet("1234R", "AV99O0"))
					.sequence(gbSeq)
					.build();
			assertThat("sanfly is not null", sandfly, notNullValue());

			final Leishmania leishmania = Leishmania.builder()
					.dataSource(GENBANK)
					.accession("ABC12345678")
					.version("3.0")
					.gi(1239841)
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(point)
					.locale(new Locale("es", "ES"))
					.pmids(newHashSet("1234R", "AV99O0"))
					.build();
			assertThat("leishmania is not null", leishmania, notNullValue());

			final Reference reference = Reference.builder()
					.pubmedId("ADGJ87950")
					.title("The best paper in the world!")
					.publicationYear(1984)
					.seqids(newHashSet("gb:ABC12345678"))
					.article(article)
					.build();
			assertThat("reference is not null", reference, notNullValue());

			File sampleFile = getDarwinCoreSets().stream().filter(file -> {
				return file.getAbsolutePath().contains("colfleb");
			}).findFirst().get();
			assertThat("COLFLEB file is not null", sampleFile, notNullValue());
			SimpleDarwinRecordSet dwcSet = DWC_XMLB.typeFromFile(sampleFile);
			assertThat("COLFLEB DwC set is not null", dwcSet, notNullValue());
			assertThat("COLFLEB DwC records are not empty", dwcSet.getSimpleDarwinRecord(), allOf(notNullValue(), not(empty())));
			assertThat("COLFLEB DwC contains at least one record", dwcSet.getSimpleDarwinRecord().size(), greaterThan(0));
			SimpleDarwinRecord sample = dwcSet.getSimpleDarwinRecord().iterator().next();
			assertThat("COLFLEB sample is not null", sample, notNullValue());

			final SandflySample colflebSample = SandflySample.builder()
					.collectionId(COLFLEB)
					.catalogNumber(trim(sample.getCatalogNumber()))
					.sample(sample)
					.build();

			sampleFile = getDarwinCoreSets().stream().filter(file -> {
				return file.getAbsolutePath().contains("clioc");
			}).findFirst().get();
			assertThat("CLIOC file is not null", sampleFile, notNullValue());
			dwcSet = DWC_XMLB.typeFromFile(sampleFile);
			assertThat("CLIOC DwC set is not null", dwcSet, notNullValue());
			assertThat("CLIOC DwC records are not empty", dwcSet.getSimpleDarwinRecord(), allOf(notNullValue(), not(empty())));
			assertThat("CLIOC DwC contains at least one record", dwcSet.getSimpleDarwinRecord().size(), greaterThan(0));
			sample = dwcSet.getSimpleDarwinRecord().iterator().next();
			assertThat("CLIOC sample is not null", sample, notNullValue());

			final LeishmaniaSample cliocSample = LeishmaniaSample.builder()
					.collectionId(CLIOC)
					.catalogNumber(trim(sample.getCatalogNumber()))
					.sample(sample)
					.build();

			final PendingSequence pendingSeq = PendingSequence.builder()
					.sample(DWC_XML_FACTORY.createSimpleDarwinRecord()
							.withInstitutionCode("UPVLC")
							.withCollectionCode("UPVLC-collection")
							.withCountry("Spain")
							.withStateProvince("Comunitat Valenciana")
							.withCounty("Valencia")
							.withLocality("Valencia")
							.withScientificName("Nyssomyia whitmani"))
					.sequence("AAAAAAAAAAAAAAAAAACCCCCCCCCCCCCCCCGGGGGGGGGGGGGGGGTTTTTTTTAAAAAAAAAAAA")
					.preparation(SamplePreparation.builder()
							.sex("Female")
							.individualCount(23)
							.collectingMethod("CDC")
							.preparationType("Mata")
							.materialType("Parátipo")
							.build())
					.build();

			final Target target = Target.builder()
					.type("sequence")
					.collection(SANDFLY_COLLECTION)
					.id("gb:JP540074")
					.filter("export_fasta")
					.compression("gzip").build();
			assertThat("target is not null", target, notNullValue());
			final Metadata metadata = Metadata.builder()
					.description("Optional description")
					.editor("ownerid")
					.isLastestVersion("my_fasta_sequences.zip")
					.openAccessLink("publicLink")
					.tags(newHashSet("tag1", "tag2", "tag3"))
					.target(target)
					.build();
			assertThat("dataset metadata is not null", metadata, notNullValue());
			final Dataset dataset = Dataset.builder()
					.aliases(newArrayList("alias1", "alias2"))
					.chunkSize(20l)
					.contentType("application/gzip")
					.filename("my_fasta_sequences.zip")
					.id("abcd123")
					.length(230l)
					.md5("iour83ytrdh")
					.metadata(metadata)
					.uploadDate(new Date())
					.build();
			assertThat("dataset is not null", dataset, notNullValue());

			final DatasetShare datasetShare = DatasetShare.builder()
					.accessType(EDIT_SHARE)
					.filename("filename")
					.namespace("namespace")
					.sharedNow()
					.subject("user@idp")
					.build();
			assertThat("dataset share is not null", datasetShare, notNullValue());

			final LvlInstance instance = LvlInstance.builder()
					.instanceId("instanceId")
					.roles(newHashSet("shard"))
					.heartbeat(new Date())
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(3.7036d, 40.4169d).build()).build())					
					.build();
			assertThat("instance is not null", instance, notNullValue());

			final SavedSearch savedSearch = SavedSearch.builder()
					.id("abc123")
					.type("sequence ; sandflies")
					.saved(new Date())
					.search(newHashSet(FormattedQueryParam.builder().term("country:spain").valid(true).build(),
							FormattedQueryParam.builder().term("sequence").build()))
					.build();
			assertThat("saved search is not null", savedSearch, notNullValue());

			final Issue issue = Issue.builder()
					.newId()
					.status(IssueStatus.NEW)
					.email("username@example.com")
					.browser("Google Chrome 42")
					.system("Ubuntu 14.04")
					.description("Problem description")
					.opened(new Date())
					.build();
			assertThat("issue is not null", issue, notNullValue());			

			// test GenBank sequence
			testGenBankSequence(gbSeq);

			// test PubMed article
			testPubMedArticle(article);

			// test sanfly with no links
			testSandfly(sandfly);

			// test sanfly with links
			sandfly.setLinks(newArrayList(seqLink));
			testSandfly(sandfly);

			// test leishmania with no links
			testLeishmania(leishmania);

			// test leishmania with links
			leishmania.setLinks(newArrayList(seqLink));
			testLeishmania(leishmania);

			// test references with no links
			testReference(reference);

			// test references with links
			reference.setLinks(newArrayList(refLink));
			testReference(reference);

			// test COLFLEB samples with no links
			testColflebSample(colflebSample);

			// test COLFLEB samples with links
			colflebSample.setLinks(newArrayList(sampleLink));
			testColflebSample(colflebSample);

			// test CLIOC samples with no links
			testCliocSample(cliocSample);

			// test CLIOC samples with links
			cliocSample.setLinks(newArrayList(sampleLink));
			testCliocSample(cliocSample);

			// test pending sequence with no links
			testPendingSequence(pendingSeq);

			// test pending sequence with links
			pendingSeq.setLinks(newArrayList(sampleLink));
			testPendingSequence(pendingSeq);

			// test dataset with no links
			testDataset(dataset);

			// test dataset with links
			dataset.setLinks(newArrayList(refLink));
			testDataset(dataset);

			// test dataset share with no links
			testDatasetShare(datasetShare);

			// test dataset share with links
			datasetShare.setLinks(newArrayList(refLink));
			testDatasetShare(datasetShare);

			// test instance with no links
			testInstance(instance);

			// test instance with links
			instance.setLinks(newArrayList(refLink));
			testInstance(instance);

			// test saved searches
			testSavedSearch(savedSearch);

			// test saved searches with links
			savedSearch.setLinks(newArrayList(refLink));
			testSavedSearch(savedSearch);

			// test issues
			testIssue(issue);

			// test issue with links
			issue.setLinks(newArrayList(refLink));
			testIssue(issue);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("JsonMappingTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("JsonMappingTest.test() has finished");
		}
	}

	private void testGenBankSequence(final GBSeq gbSeq) throws IOException {
		// test GenBank sequence JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(gbSeq);
		assertThat("serialized GenBank sequence is not null", payload, notNullValue());
		assertThat("serialized GenBank sequence is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized GenBank sequence (JSON): " + payload);

		// test GenBank sequence JSON deserialization
		final GBSeq gbSeq2 = JSON_MAPPER.readValue(payload, GBSeq.class);
		assertThat("deserialized GenBank sequence is not null", gbSeq2, notNullValue());
		// assertThat("deserialized GenBank sequence coincides with expected", gbSeq2, equalTo(gbSeq));
	}

	private void testPubMedArticle(final PubmedArticle article) throws IOException {
		// test PubMed article JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(article);
		assertThat("serialized PubMed article is not null", payload, notNullValue());
		assertThat("serialized PubMed article is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized PubMed article (JSON): " + payload);

		// test PubMed article JSON deserialization
		final PubmedArticle article2 = JSON_MAPPER.readValue(payload, PubmedArticle.class);
		assertThat("deserialized PubMed article is not null", article2, notNullValue());
		// assertThat("deserialized PubMed article coincides with expected", article2, equalTo(article));
	}

	private void testSandfly(final Sandfly sanfly) throws IOException {
		// test sanfly JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(sanfly);
		assertThat("serialized sanfly is not null", payload, notNullValue());
		assertThat("serialized sanfly is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized sanfly (JSON): " + payload);

		// test sanfly JSON deserialization
		final Sandfly sanfly2 = JSON_MAPPER.readValue(payload, Sandfly.class);
		assertThat("deserialized sanfly is not null", sanfly2, notNullValue());
		assertThat("deserialized sanfly coincides with expected", sanfly2, equalTo(sanfly));
	}

	private void testLeishmania(final Leishmania leishmania) throws IOException {
		// test leishmania JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(leishmania);
		assertThat("serialized leishmania is not null", payload, notNullValue());
		assertThat("serialized leishmania is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized leishmania (JSON): " + payload);

		// test leishmania JSON deserialization
		final Leishmania leishmania2 = JSON_MAPPER.readValue(payload, Leishmania.class);
		assertThat("deserialized sanfly is not null", leishmania2, notNullValue());
		assertThat("deserialized sanfly coincides with expected", leishmania2, equalTo(leishmania));
	}

	private void testReference(final Reference reference) throws IOException {
		// test reference JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(reference);
		assertThat("serialized reference is not null", payload, notNullValue());
		assertThat("serialized reference is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized reference (JSON): " + payload);

		// test reference JSON deserialization
		final Reference reference2 = JSON_MAPPER.readValue(payload, Reference.class);
		assertThat("deserialized reference is not null", reference2, notNullValue());
		assertThat("deserialized reference coincides with expected", reference2, equalTo(reference));
	}

	private void testColflebSample(final SandflySample sample) throws IOException {
		// test sandfly sample JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(sample);
		assertThat("serialized sample is not null", payload, notNullValue());
		assertThat("serialized sample is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized sample (JSON): " + payload);

		// test sandfly sample JSON deserialization
		final SandflySample sample2 = JSON_MAPPER.readValue(payload, SandflySample.class);
		assertThat("deserialized sample is not null", sample2, notNullValue());
		assertThat("deserialized sample coincides with expected", sample2, equalTo(sample));
	}

	private void testCliocSample(final LeishmaniaSample sample) throws IOException {
		// test leishmania sample JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(sample);
		assertThat("serialized sample is not null", payload, notNullValue());
		assertThat("serialized sample is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized sample (JSON): " + payload);

		// test leishmania sample JSON deserialization
		final LeishmaniaSample sample2 = JSON_MAPPER.readValue(payload, LeishmaniaSample.class);
		assertThat("deserialized sample is not null", sample2, notNullValue());
		assertThat("deserialized sample coincides with expected", sample2, equalTo(sample));
	}

	private void testPendingSequence(final PendingSequence pendingSeq) throws IOException {
		// test pending sequence JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(pendingSeq);
		assertThat("serialized pending sequence is not null", payload, notNullValue());
		assertThat("serialized pending sequence is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized pending sequence (JSON): " + payload);

		// test pending sequence JSON deserialization
		final PendingSequence pendingSeq2 = JSON_MAPPER.readValue(payload, PendingSequence.class);
		assertThat("deserialized pending sequence is not null", pendingSeq2, notNullValue());
		assertThat("deserialized pending sequence coincides with expected", pendingSeq2, equalTo(pendingSeq));
	}

	private void testDataset(final Dataset dataset) throws IOException {
		// test dataset JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(dataset);
		assertThat("serialized dataset is not null", payload, notNullValue());
		assertThat("serialized dataset is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized dataset (JSON): " + payload);

		// test dataset JSON deserialization
		final Dataset dataset2 = JSON_MAPPER.readValue(payload, Dataset.class);
		assertThat("deserialized dataset is not null", dataset2, notNullValue());
		assertThat("deserialized dataset coincides with expected", dataset2, equalTo(dataset));
	}

	private void testDatasetShare(final DatasetShare datasetShare) throws IOException {
		// test dataset share JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(datasetShare);
		assertThat("serialized dataset share is not null", payload, notNullValue());
		assertThat("serialized dataset share is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized dataset share (JSON): " + payload);

		// test dataset share JSON deserialization
		final DatasetShare datasetShare2 = JSON_MAPPER.readValue(payload, DatasetShare.class);
		assertThat("deserialized dataset share is not null", datasetShare2, notNullValue());
		assertThat("deserialized dataset share coincides with expected", datasetShare2, equalTo(datasetShare));
	}

	private void testInstance(final LvlInstance instance) throws IOException {
		// test instance JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(instance);
		assertThat("serialized instance is not null", payload, notNullValue());
		assertThat("serialized instance is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized instance (JSON): " + payload);

		// test instance JSON deserialization
		final LvlInstance instance2 = JSON_MAPPER.readValue(payload, LvlInstance.class);
		assertThat("deserialized instance is not null", instance2, notNullValue());
		assertThat("deserialized instance coincides with expected", instance2, equalTo(instance));
	}

	private void testSavedSearch(final SavedSearch savedSearch) throws IOException {
		// test instance JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(savedSearch);
		assertThat("serialized saved search is not null", payload, notNullValue());
		assertThat("serialized saved search is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized saved search (JSON): " + payload);

		// test instance JSON deserialization
		final SavedSearch savedSearch2 = JSON_MAPPER.readValue(payload, SavedSearch.class);
		assertThat("deserialized saved search is not null", savedSearch2, notNullValue());
		assertThat("deserialized saved search coincides with expected", savedSearch2, equalTo(savedSearch));
	}

	private void testIssue(final Issue issue) throws IOException {
		// test instance JSON serialization
		final String payload = JSON_MAPPER.writeValueAsString(issue);		
		assertThat("serialized issue is not null", payload, notNullValue());
		assertThat("serialized issue is not empty", isNotBlank(payload), equalTo(true));
		/* uncomment for additional output */
		printMsg(" >> Serialized issue (JSON): " + payload);

		// test instance JSON deserialization
		final Issue issue2 = JSON_MAPPER.readValue(payload, Issue.class);
		assertThat("deserialized issue is not null", issue2, notNullValue());
		assertThat("deserialized issue coincides with expected", issue2, equalTo(issue));
	}

}