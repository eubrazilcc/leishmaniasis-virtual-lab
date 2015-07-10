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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.addEditProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.combineProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newCustomObjectProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newGenBankSequence;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newGeocoding;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newObjectImportProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newObsoleteProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newPubMedArticle;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newReleaseProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvWriter.provToFile;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openprovenance.prov.model.Document;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Test support for data provenance tracking.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ProvenanceTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			ProvenanceTest.class.getSimpleName() + random(8, true, true)));

	@BeforeClass
	public static void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
		TEST_OUTPUT_DIR.mkdirs();
	}

	@AfterClass
	public static void cleanUp() throws IOException {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void testCitationProv() {
		try {
			System.out.println("ProvenanceTest.test()");

			// create test dataset
			final User user1 = User.builder().userid("user1").build();
			final User user2 = User.builder().userid("user2").build();
			final List<Document> history = newArrayList();

			// citation imported from external data source (no coordinates provided)
			String testId = "prov-citation-pm-draft1";			
			Document prov = newObjectImportProv(newPubMedArticle("PMID|26148331"), "lvl|ci|pm|26148331", null);
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			File file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));
			history.add(prov);

			// draft modification
			testId = "prov-citation-pm-draft2";
			addEditProv(prov, user1, "lvl|ci|pm|26148331");
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

			testId = "prov-citation-pm-draft3";
			addEditProv(prov, user2, "lvl|ci|pm|26148331");
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));			

			// new release
			testId = "prov-citation-pm-rel1";
			prov = newReleaseProv(user1, "lvl|ci|pm|26148331", "", "|rel1");
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));
			history.add(prov);

			testId = "prov-citation-pm-rel2";
			prov = newReleaseProv(user1, "lvl|ci|pm|26148331", "|rel1", "|rel2");
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));
			history.add(prov);			

			// record invalidation
			testId = "prov-citation-pm-inv";
			prov = newObsoleteProv(user1, "lvl|ci|pm|26148331");
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));
			history.add(prov);			

			// combined record provenance
			testId = "prov-citation-pm-combined";
			prov = combineProv(history.toArray(new Document[history.size()]));
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

			// user created citation
			testId = "prov-citation-ur";
			prov = newCustomObjectProv(user1, "lvl|ci|ur|MY_CIT");
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ProvenanceTest.test() failed: " + e.getMessage());
		} finally {
			System.out.println("ProvenanceTest.test() has finished");
		}
	}

	@Test
	public void testSequenceProv() {
		try {
			System.out.println("ProvenanceTest.testSequenceProv()");

			// create test dataset
			final Point point = Point.builder().coordinates(LngLatAlt.builder().longitude(2.0d).latitude(1.0d).build()).build();
			final User user1 = User.builder().userid("user1").build();

			// sequence imported from external data source
			String testId = "prov-sequence-gb-draft1";			
			Document prov = newObjectImportProv(newGenBankSequence("ACCN|U49845", "sandflies"), "lvl|sf|gb|U49845", newGeocoding(point));
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			File file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

			// user created sequence
			testId = "prov-sequence-ur";
			prov = newCustomObjectProv(user1, "lvl|sf|ur|MY_SEQ");
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			file = new File(TEST_OUTPUT_DIR, testId + ".json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, testId + ".svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ProvenanceTest.testSequenceProv() failed: " + e.getMessage());
		} finally {
			System.out.println("ProvenanceTest.testSequenceProv() has finished");
		}
	}

}