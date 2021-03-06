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

import static eu.eubrazilcc.lvl.core.conf.LogManager.LOG_MANAGER;
import static eu.eubrazilcc.lvl.storage.mock.LightCloserServiceMock.LIGHT_CLOSER_SERVICE_MOCK;
import static org.apache.commons.io.FilenameUtils.concat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit tests.
 * @author Erik Torres <ertorser@upv.es>
 */
@RunWith(Suite.class)
@SuiteClasses({ SequenceKeyTest.class, SampleKeyTest.class, PermissionElBuilderTest.class, PermissionHelperTest.class, PermissionHistoryTest.class, 
	CryptProviderTest.class, MongoDBMapTest.class, MongoDBComparisonTest.class, ResourceIdentifierPatternTest.class, ProvenanceTest.class, 
	GravatarTest.class, UrlShortenerTest.class })
public class AllJUnitTests {

	public static final String ANCHOR_FILENAME = "m2anchor";

	@BeforeClass
	public static void setup() {
		System.out.println("AllJUnitTests.setup()");
		final URL anchorURL = AllIntegrationTests.class.getClassLoader().getResource(ANCHOR_FILENAME);
		File anchorFile = null;
		try {
			anchorFile = new File(anchorURL.toURI());
		} catch (Exception e) {
			anchorFile = new File(System.getProperty("user.dir"));
		}
		TEST_RESOURCES_PATH = concat(anchorFile.getParent(), "files");
		final File resDir = new File(TEST_RESOURCES_PATH);
		if (resDir != null && resDir.isDirectory() && resDir.canRead()) {
			try {
				TEST_RESOURCES_PATH = resDir.getCanonicalPath();
			} catch (IOException e) {
				// nothing to do
			}
		} else {
			throw new IllegalStateException("Invalid test resources pathname: " + TEST_RESOURCES_PATH);
		}
		System.out.println("Test resources pathname: " + TEST_RESOURCES_PATH);
		// load logging bridges
		LOG_MANAGER.preload();
		// system pre-loading
		LIGHT_CLOSER_SERVICE_MOCK.preload();
	}

	@AfterClass
	public static void release() {
		LIGHT_CLOSER_SERVICE_MOCK.close();
	}

	public static String TEST_RESOURCES_PATH;

}