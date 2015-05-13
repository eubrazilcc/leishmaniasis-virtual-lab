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

import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLSetFiles;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getPubMedXMLFiles;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getPubMedXMLSetFiles;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getTaxonomyXMLSetFiles;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getGenInfoIdentifier;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getGeneNames;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getPubMedIds;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getPubMedReferences;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.inferCountry;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.parseSequence;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XMLB;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.parseArticle;
import static eu.eubrazilcc.lvl.core.xml.TaxonomyXmlBinder.TAXONOMY_XMLB;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;

import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticleSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy.TaxaSet;

/**
 * Test XML to/from NCBI Java object binding.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NCBIXmlBindingTest {

	@Test
	public void test() {
		System.out.println("NCBIXmlBindingTest.test()");
		try {
			// test parsing GenInfo identifier
			final String[] ids = { "gb|JQ790522.1|", "gi|384562886", "gi|", "gi|JQ790522", "gi" };
			final Integer[] gis = { null, 384562886, null, null, null };
			final GBSeq gbSeq = GBSEQ_XML_FACTORY.createGBSeq();
			gbSeq.setGBSeqOtherSeqids(GBSEQ_XML_FACTORY.createGBSeqOtherSeqids());
			gbSeq.getGBSeqOtherSeqids().getGBSeqid().add(GBSEQ_XML_FACTORY.createGBSeqid());
			for (int i = 0; i < ids.length; i++) {
				gbSeq.getGBSeqOtherSeqids().getGBSeqid().get(0).setvalue(ids[i]);		
				final Integer gi = getGenInfoIdentifier(gbSeq);
				assertThat("gi coincides with expected", gi, equalTo(gis[i]));
			}

			// test inferring location from the country feature stored in GenBank records
			final String[] features = { "Italy", "Spain:Almeria", "Sudan: Sirougia, Khartoum State" };
			final Locale[] countries = { getLocale("Italy"), getLocale("Spain"), getLocale("Sudan") };
			gbSeq.setGBSeqFeatureTable(GBSEQ_XML_FACTORY.createGBSeqFeatureTable());
			gbSeq.getGBSeqFeatureTable().getGBFeature().add(GBSEQ_XML_FACTORY.createGBFeature());
			gbSeq.getGBSeqFeatureTable().getGBFeature().get(0).setGBFeatureQuals(GBSEQ_XML_FACTORY.createGBFeatureQuals());
			gbSeq.getGBSeqFeatureTable().getGBFeature().get(0).getGBFeatureQuals().getGBQualifier().add(GBSEQ_XML_FACTORY.createGBQualifier());
			gbSeq.getGBSeqFeatureTable().getGBFeature().get(0).getGBFeatureQuals().getGBQualifier().get(0).setGBQualifierName("country");
			for (int i = 0; i < features.length; i++) {	
				gbSeq.getGBSeqFeatureTable().getGBFeature().get(0).getGBFeatureQuals().getGBQualifier().get(0).setGBQualifierValue(features[i]);
				final ImmutableMultimap<String, Locale> countries2 = inferCountry(gbSeq);
				assertThat("inferred countries is not null", countries2, notNullValue());
				assertThat("inferred countries is not empty", !countries2.isEmpty());				
				for (final String key : countries2.keySet()) {
					for (final Locale locale : countries2.get(key)) {
						assertThat("inferred country coincides with expected", locale, equalTo(countries[i]));
						/* uncomment to display additional output */
						System.out.println("Inferred country: field=" + key + ", country=" + locale.getDisplayCountry());
					}
				}
			}

			// test parsing GenBank XML records
			Collection<File> files = getGBSeqXMLSetFiles();
			for (final File file : files) {
				System.out.println(" >> GenBank sequence set XML file: " + file.getCanonicalPath());
				final GBSet gbSet = GBSEQ_XMLB.typeFromFile(file);
				assertThat("GenBank XML set is not null", gbSet, notNullValue());
				assertThat("GenBank XML sequences is not null", gbSet.getGBSeq(), notNullValue());
				assertThat("GenBank XML sequences is not empty", !gbSet.getGBSeq().isEmpty());				
				for (final GBSeq seq : gbSet.getGBSeq()) {
					assertThat("GenBank XML sequence accession is not empty", isNotBlank(seq.getGBSeqPrimaryAccession()));
					assertThat("GenBank XML sequence definition is not empty", isNotBlank(seq.getGBSeqDefinition()));
					assertThat("GenBank XML sequence version is not empty", isNotBlank(seq.getGBSeqAccessionVersion()));
					assertThat("GenBank XML sequence organism is not empty", isNotBlank(seq.getGBSeqOrganism()));
					/* Uncomment for additional output */
					System.out.println(" >> Accession  : " + seq.getGBSeqPrimaryAccession());
					System.out.println(" >> Definition : " + seq.getGBSeqDefinition());
					System.out.println(" >> Version    : " + seq.getGBSeqAccessionVersion());
					System.out.println(" >> Organism   : " + seq.getGBSeqOrganism());
					System.out.println(" >> Length     : " + seq.getGBSeqLength());

					final Set<String> gene = getGeneNames(seq);
					if (gene != null) {
						/* Uncomment for additional output */
						System.out.println(" >> Gene names : " + gene);
					}

					final List<Reference> references = getPubMedReferences(seq);
					assertThat("References is not null", references, notNullValue());
					assertThat("References is not empty", references.isEmpty(), equalTo(false));
					/* Uncomment for additional output */
					System.out.println(" >> References : " + references);

					final Set<String> pmids = getPubMedIds(seq);
					assertThat("References PMIDS are not null", pmids, notNullValue());
					assertThat("References PMIDS are not empty", pmids.isEmpty(), equalTo(false));
					/* Uncomment for additional output */
					System.out.println(" >> Reference PMIDS : " + pmids);

					final Sequence sequence = parseSequence(seq, Sandfly.builder());
					assertThat("Sequence is not null", sequence, notNullValue());
					assertThat("Sequence data source is not empty", isNotBlank(sequence.getDataSource()));
					assertThat("Sequence accession is not empty", isNotBlank(sequence.getAccession()));
					assertThat("Sequence definition is not empty", isNotBlank(sequence.getDefinition()));
					assertThat("Sequence version is not empty", isNotBlank(sequence.getVersion()));
					assertThat("Sequence organism is not empty", isNotBlank(sequence.getOrganism()));
					/* Uncomment for additional output */
					System.out.println(" >> Sequence  : " + sequence.toString());
				}
			}

			// test parsing GenBank isolated sequences
			files = getGBSeqXMLFiles();
			for (final File file : files) {
				System.out.println(" >> GenBank sequence XML file: " + file.getCanonicalPath());
				final GBSeq seq = GBSEQ_XMLB.typeFromFile(file);
				assertThat("GenBank XML sequence is not null", seq, notNullValue());				
				assertThat("GenBank XML sequence accession is not empty", isNotBlank(seq.getGBSeqPrimaryAccession()));
				assertThat("GenBank XML sequence definition is not empty", isNotBlank(seq.getGBSeqDefinition()));
				assertThat("GenBank XML sequence version is not empty", isNotBlank(seq.getGBSeqAccessionVersion()));
				assertThat("GenBank XML sequence organism is not empty", isNotBlank(seq.getGBSeqOrganism()));
				/* Uncomment for additional output */
				System.out.println(" >> Accession  : " + seq.getGBSeqPrimaryAccession());
				System.out.println(" >> Definition : " + seq.getGBSeqDefinition());
				System.out.println(" >> Version    : " + seq.getGBSeqAccessionVersion());
				System.out.println(" >> Organism   : " + seq.getGBSeqOrganism());
				System.out.println(" >> Length     : " + seq.getGBSeqLength());

				final Set<String> gene = getGeneNames(seq);
				if (gene != null) {
					/* Uncomment for additional output */
					System.out.println(" >> Gene names : " + gene);
				}

				final List<Reference> references = getPubMedReferences(seq);
				assertThat("References is not null", references, notNullValue());
				assertThat("References is not empty", references.isEmpty(), equalTo(false));
				/* Uncomment for additional output */
				System.out.println(" >> References : " + references);

				final Set<String> pmids = getPubMedIds(seq);
				assertThat("References PMIDS are not null", pmids, notNullValue());
				assertThat("References PMIDS are not empty", pmids.isEmpty(), equalTo(false));
				/* Uncomment for additional output */
				System.out.println(" >> Reference PMIDS : " + pmids);

				final Sequence sequence = parseSequence(seq, Sandfly.builder());
				assertThat("Sequence is not null", sequence, notNullValue());
				assertThat("Sequence data source is not empty", isNotBlank(sequence.getDataSource()));
				assertThat("Sequence accession is not empty", isNotBlank(sequence.getAccession()));
				assertThat("Sequence definition is not empty", isNotBlank(sequence.getDefinition()));
				assertThat("Sequence version is not empty", isNotBlank(sequence.getVersion()));
				assertThat("Sequence organism is not empty", isNotBlank(sequence.getOrganism()));
				/* Uncomment for additional output */
				System.out.println(" >> Sequence  : " + sequence.toString());
			}

			// test parsing PubMed XML records
			files = getPubMedXMLSetFiles();
			for (final File file : files) {
				System.out.println(" >> PubMed article set XML file: " + file.getCanonicalPath());
				final PubmedArticleSet articleSet = PUBMED_XMLB.typeFromFile(file);
				assertThat("PubMed XML set is not null", articleSet, notNullValue());
				assertThat("PubMed XML articles is not null", articleSet.getPubmedArticle(), notNullValue());
				assertThat("PubMed XML articles is not empty", !articleSet.getPubmedArticle().isEmpty());				
				for (final PubmedArticle article : articleSet.getPubmedArticle()) {
					assertThat("PubMed XML article MEDLINE citation is not null", article.getMedlineCitation(), notNullValue());
					assertThat("PubMed XML article is not null", article.getMedlineCitation().getArticle(), notNullValue());
					assertThat("PubMed XML article title is not empty", isNotBlank(article.getMedlineCitation().getArticle().getArticleTitle()));					
					assertThat("PubMed XML article PMID is not null", article.getMedlineCitation().getPMID(), notNullValue());
					assertThat("PubMed XML article PMID is not empty", isNotBlank(article.getMedlineCitation().getPMID().getvalue()));					
					assertThat("PubMed XML article journal is not null", article.getMedlineCitation().getArticle().getJournal(), notNullValue());
					assertThat("PubMed XML article journal issue is not null", article.getMedlineCitation().getArticle().getJournal()
							.getJournalIssue(), notNullValue());
					assertThat("PubMed XML article journal publication date is not null", article.getMedlineCitation().getArticle().getJournal()
							.getJournalIssue().getPubDate(), notNullValue());
					assertThat("PubMed XML article journal publication year is not null", article.getMedlineCitation().getArticle().getJournal()
							.getJournalIssue().getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate(), notNullValue());					
					assertThat("PubMed XML article journal publication year is not empty", article.getMedlineCitation().getArticle().getJournal()
							.getJournalIssue().getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty(), equalTo(false));
					/* Uncomment for additional output */
					System.out.println(" >> Title : " + article.getMedlineCitation().getArticle().getArticleTitle());
					System.out.println(" >> PMID  : " + article.getMedlineCitation().getPMID().getvalue());

					final Reference reference = parseArticle(article);
					assertThat("Reference is not null", reference, notNullValue());
					assertThat("Reference title is not empty", isNotBlank(reference.getTitle()));
					assertThat("Reference PMID is not empty", isNotBlank(reference.getPubmedId()));
					assertThat("Reference publication year coincides with expected", reference.getPublicationYear() > 1900, equalTo(true));
					/* Uncomment for additional output */
					System.out.println(" >> Reference  : " + reference.toString());
				}
			}

			// test parsing GenBank isolated articles
			files = getPubMedXMLFiles();
			for (final File file : files) {
				System.out.println(" >> PubMed article XML file: " + file.getCanonicalPath());
				final PubmedArticle article = PUBMED_XMLB.typeFromFile(file);				
				assertThat("PubMed XML article MEDLINE citation is not null", article.getMedlineCitation(), notNullValue());
				assertThat("PubMed XML article is not null", article.getMedlineCitation().getArticle(), notNullValue());
				assertThat("PubMed XML article title is not empty", isNotBlank(article.getMedlineCitation().getArticle().getArticleTitle()));					
				assertThat("PubMed XML article PMID is not null", article.getMedlineCitation().getPMID(), notNullValue());
				assertThat("PubMed XML article PMID is not empty", isNotBlank(article.getMedlineCitation().getPMID().getvalue()));					
				assertThat("PubMed XML article journal is not null", article.getMedlineCitation().getArticle().getJournal(), notNullValue());
				assertThat("PubMed XML article journal issue is not null", article.getMedlineCitation().getArticle().getJournal()
						.getJournalIssue(), notNullValue());
				assertThat("PubMed XML article journal publication date is not null", article.getMedlineCitation().getArticle().getJournal()
						.getJournalIssue().getPubDate(), notNullValue());
				assertThat("PubMed XML article journal publication year is not null", article.getMedlineCitation().getArticle().getJournal()
						.getJournalIssue().getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate(), notNullValue());					
				assertThat("PubMed XML article journal publication year is not empty", article.getMedlineCitation().getArticle().getJournal()
						.getJournalIssue().getPubDate().getYearOrMonthOrDayOrSeasonOrMedlineDate().isEmpty(), equalTo(false));
				/* Uncomment for additional output */
				System.out.println(" >> Title : " + article.getMedlineCitation().getArticle().getArticleTitle());
				System.out.println(" >> PMID  : " + article.getMedlineCitation().getPMID().getvalue());

				final Reference reference = parseArticle(article);
				assertThat("Reference is not null", reference, notNullValue());
				assertThat("Reference title is not empty", isNotBlank(reference.getTitle()));
				assertThat("Reference PMID is not empty", isNotBlank(reference.getPubmedId()));
				assertThat("Reference publication year coincides with expected", reference.getPublicationYear() > 1900, equalTo(true));
				/* Uncomment for additional output */
				System.out.println(" >> Reference  : " + reference.toString());
			}

			// test parsing Taxonomy XML records
			files = getTaxonomyXMLSetFiles();
			for (final File file : files) {
				System.out.println(" >> Taxonomy set XML file: " + file.getCanonicalPath());
				final TaxaSet taxaSet = TAXONOMY_XMLB.typeFromFile(file);
				assertThat("Taxonomy XML set is not null", taxaSet, notNullValue());
				assertThat("Taxonomy XML taxons is not null", taxaSet.getTaxon(), notNullValue());
				assertThat("Taxonomy XML taxons is not empty", !taxaSet.getTaxon().isEmpty());
				// TODO : complete
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NCBIXmlBindingTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("NCBIXmlBindingTest.test() has finished");
		}
	}

}