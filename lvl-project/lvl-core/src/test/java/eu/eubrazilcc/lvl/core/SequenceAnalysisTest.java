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
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGenBankFiles;
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


			// test country inference
			final Collection<File> files = getGenBankFiles();
			for (final File file : files) {
				System.out.println(" >> Sequence file: " + file.getCanonicalPath());
				final ImmutableMultimap<GenBankField, Locale> possibleCountries = inferCountry(file);				

				// TODO
				System.err.println("\n\n");
				for (final GenBankField field : possibleCountries.keySet()) {
					final ImmutableCollection<Locale> locales = possibleCountries.get(field);
					for (final Locale locale : locales) {
						System.err.println(" >> " + field.toString() + "=" + locale.getDisplayCountry());
					}					
				}
				System.err.println("\n\n");
				// TODO

			}
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