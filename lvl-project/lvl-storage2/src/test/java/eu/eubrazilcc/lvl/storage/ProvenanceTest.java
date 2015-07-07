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

import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.addObjectImportProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.addUserCreatedProv;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newGenBankSequence;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newGeocoding;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newProvDocument;
import static eu.eubrazilcc.lvl.storage.prov.ProvFactory.newPubMedArticle;
import static eu.eubrazilcc.lvl.storage.prov.ProvWriter.provToFile;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
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
			ProvenanceTest.class.getSimpleName() + "_")); // + TODO random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
		TEST_OUTPUT_DIR.mkdirs();
	}

	@After
	public void cleanUp() throws IOException {
		// TODO deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		try {
			System.out.println("ProvenanceTest.test()");

			// create test dataset
			final Point point = Point.builder().coordinates(LngLatAlt.builder().longitude(2.0d).latitude(1.0d).build()).build();
			final User user1 = User.builder().userid("user1").build();

			// test provenance of sequence imported from external data source
			Document prov = newProvDocument();
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			addObjectImportProv(prov, newGenBankSequence("ACCN:U49845", "sandflies"), "lvl.gb.U49845", newGeocoding(point));
			File file = new File(TEST_OUTPUT_DIR, "prov1.json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, "prov1.svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

			// test provenance of citation imported from external data source
			prov = newProvDocument();
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			addObjectImportProv(prov, newPubMedArticle("PMID:26148331"), "lvl.pm.26148331", null);
			file = new File(TEST_OUTPUT_DIR, "prov2.json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, "prov2.svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));

			// test provenance of user created sequence
			prov = newProvDocument();
			assertThat("prov document is not null", prov, notNullValue());
			assertThat("prov bundle is not null", prov.getStatementOrBundle(), notNullValue());
			assertThat("prov bundle is not empty", prov.getStatementOrBundle().isEmpty(), equalTo(false));
			addUserCreatedProv(prov, user1, "lvl.ur.SEQ_2");
			file = new File(TEST_OUTPUT_DIR, "prov3.json");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov JSON file exists", file.exists(), equalTo(true));
			assertThat("prov JSON file is not empty", file.length() > 0l, equalTo(true));
			file = new File(TEST_OUTPUT_DIR, "prov3.svg");
			provToFile(prov, file.getCanonicalPath());
			assertThat("prov SVG file exists", file.exists(), equalTo(true));
			assertThat("prov SVG file is not empty", file.length() > 0l, equalTo(true));



			/* TODO
			// test sequence provenance
			final Point point = Point.builder().coordinates(LngLatAlt.builder().longitude(2.0d).latitude(1.0d).build()).build();
			final SequenceProv sequenceProv = new SequenceProv();
			final Document doc1 = sequenceProv.importFromGenbank("ACCN:U49845", point, "LVL:GB:U49845-draft1");
			sequenceProv.exportToFile(doc1, new File(TEST_OUTPUT_DIR, "prov1.json").getCanonicalPath());
			sequenceProv.exportToFile(doc1, new File(TEST_OUTPUT_DIR, "prov1.svg").getCanonicalPath());

			// TODO

			// modify sequence draft
			final Document doc2 = sequenceProv.modifySequenceDraft("LVL:GB:U49845-draft1", User.builder().userid("user1").build(), "LVL:GB:U49845-draft2");
			sequenceProv.exportToFile(doc2, new File(TEST_OUTPUT_DIR, "prov2.json").getCanonicalPath());
			sequenceProv.exportToFile(doc2, new File(TEST_OUTPUT_DIR, "prov2.svg").getCanonicalPath());

			// TODO

			// test sequence approval
			final Document doc3 = sequenceProv.approveSequence("LVL:GB:U49845-draft2", User.builder().userid("user1").build(), "LVL:GB:U49845-v1");
			sequenceProv.exportToFile(doc3, new File(TEST_OUTPUT_DIR, "prov3.json").getCanonicalPath());
			sequenceProv.exportToFile(doc3, new File(TEST_OUTPUT_DIR, "prov3.svg").getCanonicalPath());

			// TODO

			// test combine provenances
			final Document combined = sequenceProv.combine(doc1, doc2, doc3);
			sequenceProv.exportToFile(combined, new File(TEST_OUTPUT_DIR, "combined.json").getCanonicalPath());
			sequenceProv.exportToFile(combined, new File(TEST_OUTPUT_DIR, "combined.svg").getCanonicalPath()); */

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ProvenanceTest.test() failed: " + e.getMessage());
		} finally {
			System.out.println("ProvenanceTest.test() has finished");
		}
	}

}