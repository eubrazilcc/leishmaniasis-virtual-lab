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

import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.FLAT_FILE;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.GB_SEQ_XML;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.PUBMED_XML;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getPubMedIds;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.entrez.EntrezHelper;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests Entrez utilities provided by the class {@link EntrezHelper}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class EntrezTest extends LeishvlTestCase {

	public EntrezTest() {
		super(false);
	}

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			EntrezTest.class.getSimpleName() + "_" + random(8, true, true)));

	private static final String SMALL_QUERY = "(phlebotomus[Organism])+AND+(\"Phlebotomus+alexandri\"[porgn:__txid94477])";	

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
		TASK_RUNNER.preload();
	}

	@After
	public void cleanUp() throws IOException {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		printMsg("EntrezTest.test()");
		try (final EntrezHelper entrez = EntrezHelper.create()) {
			assertThat("Entrez helper is not null", entrez, notNullValue());

			// test list Ids
			final Set<String> ids = entrez.listNucleotides(SMALL_QUERY);
			assertThat("ids is not null", ids, notNullValue());
			assertThat("ids is not empty", !ids.isEmpty());
			int count = ids.size();

			// test save sequences in flat file format
			File outputDir = new File(TEST_OUTPUT_DIR, "gb-flat");
			entrez.saveNucleotides(ids, outputDir, FLAT_FILE);
			Collection<File> files = listFiles(outputDir, new String[] { "gb" }, false);
			assertThat("GenBank flat files is not null", files, notNullValue());
			assertThat("GenBank flat files is not empty", !files.isEmpty());
			assertThat("GenBank flat files count does not concide", count, equalTo(files.size()));

			// test save sequences in GenBank XML format
			outputDir = new File(TEST_OUTPUT_DIR, "gb-xml");
			entrez.saveNucleotides(ids, outputDir, GB_SEQ_XML);
			files = listFiles(outputDir, new String[] { "xml" }, false);
			assertThat("GenBank XML files is not null", files, notNullValue());
			assertThat("GenBank XML files is not empty", !files.isEmpty());
			assertThat("GenBank XML files count does not concide", count, equalTo(files.size()));

			// test save publications in PubMed XML format
			final GBSeq gbSeq = GBSEQ_XMLB.typeFromFile(new File(TEST_OUTPUT_DIR, "gb-xml/9931364.xml"));
			assertThat("GenBank XML sequence is not null", gbSeq, notNullValue());
			final Set<String> pmids = getPubMedIds(gbSeq);
			assertThat("GenBank XML PMIDs is not null", pmids, notNullValue());
			assertThat("GenBank XML PMIDs is not empty", !pmids.isEmpty());
			count = pmids.size();

			outputDir = new File(TEST_OUTPUT_DIR, "pubmed-xml");
			entrez.savePublications(pmids, outputDir, PUBMED_XML);
			files = listFiles(outputDir, new String[] { "xml" }, false);
			assertThat("PubMed XML files is not null", files, notNullValue());
			assertThat("PubMed XML files is not empty", !files.isEmpty());
			assertThat("PubMed XML files count does not concide", count, equalTo(files.size()));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("EntrezTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("EntrezTest.test() has finished");
		}
	}	

}