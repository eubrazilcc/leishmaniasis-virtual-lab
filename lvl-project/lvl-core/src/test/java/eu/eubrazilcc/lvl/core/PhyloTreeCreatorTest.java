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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationFinder.DEFAULT_LOCATION;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getPhylogeneticFiles;
import static eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.TEST_CONFIG_DIR;
import static java.io.File.separator;
import static java.lang.System.getProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.io.PhyloTreeCreator;
import eu.eubrazilcc.lvl.core.util.TestUtils.Phylogenetic;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.ConditionalIgnore;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Tests phylogenetic tree creator.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PhyloTreeCreatorTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			PhyloTreeCreatorTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Rule
	public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

	@Before
	public void setUp() {
		// setup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	@After
	public void cleanUp() throws IOException {
		// cleanup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	@ConditionalIgnore(condition=EnablePhyloTreeTestIsFound.class)
	public void test() {
		System.out.println("PhyloTreeCreatorTest.test()");
		try {
			final Collection<Phylogenetic> phyloFiles = getPhylogeneticFiles();
			for (final Phylogenetic phyloFile : phyloFiles) {
				System.out.println(" >> Tree file: " + phyloFile.getTree().getCanonicalPath() + ", Aligment file: " 
						+ (phyloFile.getAlignment() != null ? phyloFile.getAlignment().getCanonicalPath() : null));
				final String name = getBaseName(phyloFile.getTree().getName());
				final File outputDir = new File(TEST_OUTPUT_DIR, name);
				final List<String> images = PhyloTreeCreator.newickExportAll(phyloFile.getTree(), phyloFile.getAlignment(), outputDir);
				assertThat("exported images are not null", images, notNullValue());
				assertThat("exported images are not empty", images.isEmpty(), equalTo(false));
				assertThat("number of exported images coincides with expected", images.size(), equalTo(phyloFile.getAlignment() != null ? 4 : 2));
				for (final String image : images) {
					final File imgFile = new File(image);
					assertThat("exported image exists", imgFile.canRead(), equalTo(true));
					assertThat("exported image is not empty", imgFile.length() > 0l, equalTo(true));
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PhyloTreeCreatorTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PhyloTreeCreatorTest.test() has finished");
		}
	}

	/**
	 * Checks whether a flag file is available in the local filesystem.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public class EnablePhyloTreeTestIsFound implements IgnoreCondition {
		private final File file = new File(concat(DEFAULT_LOCATION, TEST_CONFIG_DIR + separator + "phylo-tree.test"));
		@Override
		public boolean isSatisfied() {			
			return !file.canRead();
		}		
	}

}