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

import static eu.eubrazilcc.lvl.core.entrez.GenBankSequenceAnalizer.inferCountry;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGenBankFiles;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;

import eu.eubrazilcc.lvl.core.entrez.GenBankSequenceAnalizer.GenBankField;
import eu.eubrazilcc.lvl.core.xml.NCBIXmlBindingHelper;
import eu.eubrazilcc.lvl.core.xml.ncbi.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.GBSet;

/**
 * Tests biological sequences analysis.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceAnalysisTest {

	private static final File TEST_OUTPUT_DIR = new File(FilenameUtils.concat(System.getProperty("java.io.tmpdir"),
			SequenceAnalysisTest.class.getSimpleName() + "_" + RandomStringUtils.random(8, true, true)));

	@Before
	public void setUp() {
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("SequenceAnalysisTest.test()");
		try {			
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
					
					// TODO
					
					/* Uncomment for additional output */
					System.out.println(" >> Accession  : " + seq.getGBSeqPrimaryAccession());
					System.out.println(" >> Definition : " + seq.getGBSeqDefinition());
					System.out.println(" >> Version    : " + seq.getGBSeqAccessionVersion());
					System.out.println(" >> Organism   : " + seq.getGBSeqOrganism());
					
					// TODO
				}
				
				
				
			}


			/*
			// TODO
			final File dir = new File("/home/etorres/KK/entrez/phlebotomus");
			final Collection<File> files2 = FileUtils.listFiles(dir, new String[] { "gb" }, false);
			for (final File file : files2) {
				System.out.println(" >> Sequence file: " + file.getCanonicalPath());
				final ImmutableList<Locale> possibleCountries = inferCountry(file);

				// TODO
				System.err.println("\n\n" + Iterables.getFirst(possibleCountries, null).getDisplayCountry() + "\n\n");
				// TODO

			}
			if (true) {
				return;
			}
			// TODO
			 */

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SequenceAnalysisTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SequenceAnalysisTest.test() has finished");
		}
	}

	@After
	public void cleanUp() {
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);
	}

}