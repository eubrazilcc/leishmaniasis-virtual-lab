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

package eu.eubrazilcc.lvl.core;

import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.GB_SEQXML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.GB_SEQXML;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.getGenInfoIdentifier;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.inferCountry;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.parse;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;

import eu.eubrazilcc.lvl.core.xml.ncbi.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.GBSet;

/**
 * Test XML to/from NCBI Java object binding.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NCBIXmlBindingTest {

	@Test
	public void test() {
		System.out.println("NCBIXmlBindingTest.test()");
		try {
			// test GenInfo identifier parsing
			final String[] ids = { "gb|JQ790522.1|", "gi|384562886", "gi|", "gi|JQ790522", "gi" };
			final Integer[] gis = { null, 384562886, null, null, null };
			final GBSeq gbSeq = GB_SEQXML_FACTORY.createGBSeq();
			gbSeq.setGBSeqOtherSeqids(GB_SEQXML_FACTORY.createGBSeqOtherSeqids());
			gbSeq.getGBSeqOtherSeqids().getGBSeqid().add(GB_SEQXML_FACTORY.createGBSeqid());
			for (int i = 0; i < ids.length; i++) {
				gbSeq.getGBSeqOtherSeqids().getGBSeqid().get(0).setvalue(ids[i]);		
				final Integer gi = getGenInfoIdentifier(gbSeq);
				assertThat("gi coincides with expected", gi, equalTo(gis[i]));
			}

			// test country feature parsing
			final String[] features = { "Italy", "Spain:Almeria", "Sudan: Sirougia, Khartoum State" };
			final Locale[] countries = { getLocale("Italy"), getLocale("Spain"), getLocale("Sudan") };
			gbSeq.setGBSeqFeatureTable(GB_SEQXML_FACTORY.createGBSeqFeatureTable());
			gbSeq.getGBSeqFeatureTable().getGBFeature().add(GB_SEQXML_FACTORY.createGBFeature());
			gbSeq.getGBSeqFeatureTable().getGBFeature().get(0).setGBFeatureQuals(GB_SEQXML_FACTORY.createGBFeatureQuals());
			gbSeq.getGBSeqFeatureTable().getGBFeature().get(0).getGBFeatureQuals().getGBQualifier().add(GB_SEQXML_FACTORY.createGBQualifier());
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

			// test import GenBank XML
			final Collection<File> files2 = getGBSeqXMLFiles();
			for (final File file : files2) {
				System.out.println(" >> GenBank sequence XML file: " + file.getCanonicalPath());
				final GBSet gbSet = GB_SEQXML.typeFromFile(file);
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
					
					final Sequence sequence = parse(seq);
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
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NCBIXmlBindingTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("NCBIXmlBindingTest.test() has finished");
		}
	}

}