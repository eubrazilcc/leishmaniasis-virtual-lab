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

import static org.apache.commons.io.FileUtils.listFiles;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.entrez.EntrezHelper;

/**
 * Tests Entrez utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class EntrezTest {

	private static final File TEST_OUTPUT_DIR = new File(FilenameUtils.concat(System.getProperty("java.io.tmpdir"),
			EntrezTest.class.getSimpleName() + "_" + RandomStringUtils.random(8, true, true)));

	private static final String SMALL_QUERY = "(phlebotomus[Organism])+AND+(\"Phlebotomus+alexandri\"[porgn:__txid94477])";

	@Before
	public void setUp() {
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("EntrezTest.test()");
		try {
			// test list Ids
			final Set<String> ids = EntrezHelper.listNucleotideIds(SMALL_QUERY);
			assertThat("ids is not null", ids, notNullValue());
			assertThat("ids is not empty", !ids.isEmpty());
			final int count = ids.size();
			
			// test save sequences
			EntrezHelper.saveNucleotides(SMALL_QUERY, TEST_OUTPUT_DIR);
			final Collection<File> files = listFiles(TEST_OUTPUT_DIR, new String[] { "gb" }, false);
			assertThat("GenBank files is not null", files, notNullValue());
			assertThat("GenBank files is not empty", !files.isEmpty());
			assertThat("GenBank files count does not concide", count == files.size());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("EntrezTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("EntrezTest.test() has finished");
		}
	}

	@After
	public void cleanUp() {
		FileUtils.deleteQuietly(TEST_OUTPUT_DIR);
	}

}