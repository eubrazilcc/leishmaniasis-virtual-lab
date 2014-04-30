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

import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBindingHelper.getGenInfoIdentifier;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.xml.NCBIXmlBindingHelper;
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
			// test parse GenInfo identifier
			final String[] ids = { "gb|JQ790522.1|", "gi|384562886", "gi|", "gi|JQ790522", "gi" };
			final Integer[] gis = { null, 384562886, null, null, null };
			final GBSeq gbSeq = NCBIXmlBindingHelper.FACTORY.createGBSeq();
			gbSeq.setGBSeqOtherSeqids(NCBIXmlBindingHelper.FACTORY.createGBSeqOtherSeqids());
			gbSeq.getGBSeqOtherSeqids().getGBSeqid().add(NCBIXmlBindingHelper.FACTORY.createGBSeqid());
			for (int i = 0; i < ids.length; i++) {				
				gbSeq.getGBSeqOtherSeqids().getGBSeqid().get(0).setvalue(ids[i]);		
				final Integer gi = getGenInfoIdentifier(gbSeq);
				assertThat("gi coincides with expected", gi, equalTo(gis[i]));
			}

			// test import GenBank XML
			final Collection<File> files2 = getGBSeqXMLFiles();
			for (final File file : files2) {
				System.out.println(" >> GenBank sequence XML file: " + file.getCanonicalPath());
				final GBSet gbSet = NCBIXmlBindingHelper.typeFromFile(file);
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