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

import static eu.eubrazilcc.lvl.core.io.FastaReader.readFasta;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getFastaFiles;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.io.FastaReader;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests FASTA format reader helper class {@link FastaReader}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class FastaReaderTest extends LeishvlTestCase {

	public FastaReaderTest() {
		super(false);
	}

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			FastaReaderTest.class.getSimpleName() + "_" + random(8, true, true)));

	private final Map<String, Integer> dataset = ImmutableMap.of("fasta_250.fasta", 251, "its_gb.fasta", 595);

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		printMsg("FastaReaderTest.test()");
		try {
			final Collection<File> files = getFastaFiles();
			for (final File file : files) {
				printMsg(" >> FASTA file: " + file.getCanonicalPath());
				String[] sequences = readFasta(file);
				assertThat("sequences are not null", sequences, notNullValue());
				assertThat("number of sequences coincides with expected", sequences.length, equalTo(dataset.get(file.getName())));
				for (final String sequence : sequences) {
					assertThat("sequence is not empty", isNotBlank(sequence), equalTo(true)); 
				}
				// uncomment for additional output
				printMsg(" >> Read sequences \n" + Arrays.toString(sequences));				
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("FastaReaderTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("FastaReaderTest.test() has finished");
		}
	}

	@After
	public void cleanUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
	}

}