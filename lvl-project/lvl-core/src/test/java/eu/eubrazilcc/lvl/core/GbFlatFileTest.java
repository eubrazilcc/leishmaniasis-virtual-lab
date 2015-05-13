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

import static eu.eubrazilcc.lvl.core.entrez.GbFlatFileHelper.inferCountry;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGenBankFlatFiles;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMultimap;

import eu.eubrazilcc.lvl.core.entrez.GbFlatFileHelper.GenBankField;

/**
 * Tests GenBank flat file helper class {@link GbFlatFileHelper}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GbFlatFileTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			GbFlatFileTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("GbFlatFileTest.test()");
		try {			
			final Collection<File> files = getGenBankFlatFiles();
			for (final File file : files) {
				System.out.println(" >> Sequence file: " + file.getCanonicalPath());
				final ImmutableMultimap<GenBankField, Locale> countries = inferCountry(file);
				assertThat("inferred countries is not null", countries, notNullValue());
				assertThat("inferred countries is not empty", !countries.isEmpty());
				/* uncomment to display additional output */
				System.out.println("Inferred countries: ");
				for (final GenBankField key : countries.keySet()) {
					for (final Locale locale : countries.get(key)) {
						System.out.println("Field=" + key + ", country=" + locale.getDisplayCountry());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("GbFlatFileTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("GbFlatFileTest.test() has finished");
		}
	}

	@After
	public void cleanUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
	}

}