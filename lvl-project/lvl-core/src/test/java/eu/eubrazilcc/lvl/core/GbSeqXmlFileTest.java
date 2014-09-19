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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.toFasta;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper;

/**
 * Tests GenBank sequence XML file helper class {@link GbSeqXmlHelper}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GbSeqXmlFileTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			GbSeqXmlFileTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("GbSeqXmlFileTest.test()");
		try {
			File output = null;
			String payload = null;

			// test writing individual FASTA files (uncompressed)
			final Collection<File> files = getGBSeqXMLFiles();
			for (final File file : files) {
				System.out.println(" >> Sequence file: " + file.getCanonicalPath());
				output = new File(TEST_OUTPUT_DIR, "sequence.fasta");
				toFasta(file, output.getCanonicalPath(), false);
				assertThat("fasta sequence file exists: " + output.getCanonicalPath(), output.canRead(), equalTo(true));
				payload = readFileToString(output);
				assertThat("fasta sequence is not null", payload, notNullValue());
				assertThat("fasta sequence is not empty", isNotBlank(payload), equalTo(true));
				/* uncomment for additional output */
				System.out.println(" >> FASTA sequence \n" + payload);
			}

			// test writing a single FASTA file (uncompressed)
			output = new File(TEST_OUTPUT_DIR, "sequences.fasta");
			toFasta(files, output.getCanonicalPath(), false);
			assertThat("fasta sequences file exists: " + output.getCanonicalPath(), output.canRead(), equalTo(true));
			payload = readFileToString(output);
			assertThat("fasta sequences is not null", payload, notNullValue());
			assertThat("fasta sequences is not empty", isNotBlank(payload), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> FASTA sequences \n" + payload);

			// test writing a FASTA file (compressed)
			final List<File> list = newArrayList(files);
			Collections.shuffle(list);
			final File file = list.get(0);
			output = new File(TEST_OUTPUT_DIR, "sequence.fasta.gz");
			toFasta(file, output.getCanonicalPath(), false);
			assertThat("fasta GZIP compressed file exists: " + output.getCanonicalPath(), output.canRead(), equalTo(true));
			assertThat("fasta GZIP compressed file is not empty", output.length() > 0l, equalTo(true));			

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("GbSeqXmlFileTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("GbSeqXmlFileTest.test() has finished");
		}
	}

	@After
	public void cleanUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
	}

}