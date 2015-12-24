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
import static eu.eubrazilcc.lvl.core.util.TestUtils.getDarwinCoreSets;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getTapirResponses;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XMLB;
import static eu.eubrazilcc.lvl.core.xml.TapirXmlBinder.TAPIR_XMLB;
import static eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.TEST_CONFIG_DIR;
import static java.io.File.separator;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.tapir.SpeciesLinkConnector;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecordSet;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.ResponseType;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.ConditionalIgnore;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Test TAPIR access protocol for information retrieval.
 * @author Erik Torres <ertorser@upv.es>
 */
@FixMethodOrder(NAME_ASCENDING)
public class TapirTest extends LeishvlTestCase {

	public TapirTest() {
		super(false);
	}

	@Rule
	public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

	@Test
	public void test01XmlBinding() {
		printMsg("TapirTest.test01XmlBinding()");
		try {
			// test parsing TAPIR XML responses
			Collection<File> files = getTapirResponses();
			for (final File file : files) {
				printMsg(" >> TAPIR response XML file: " + file.getCanonicalPath());
				final ResponseType response = TAPIR_XMLB.typeFromFile(file);
				assertThat("TAPIR response is not null", response, notNullValue());
				final boolean isInventory = response.getInventory() != null;
				final boolean isSearch = response.getSearch() != null;				
				assertThat("TAPIR response content is not empty", isInventory || isSearch, equalTo(true));		
			}

			// test parsing DarwinCore XML records
			files = getDarwinCoreSets();
			for (final File file : files) {
				printMsg(" >> DarwinCore set XML file: " + file.getCanonicalPath());
				final SimpleDarwinRecordSet dwcSet = DWC_XMLB.typeFromFile(file);
				assertThat("DwC set is not null", dwcSet, notNullValue());
				assertThat("DwC records are not empty", dwcSet.getSimpleDarwinRecord(), allOf(notNullValue(), not(empty())));
				assertThat("DwC contains at least one record", dwcSet.getSimpleDarwinRecord().size(), greaterThan(0));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("TapirTest.test01XmlBinding() failed: " + e.getMessage());
		} finally {			
			printMsg("TapirTest.test01XmlBinding() has finished");
		}
	}

	@Test
	@ConditionalIgnore(condition=EnableSpeciesLinkTestIsFound.class)
	public void test02splink() {
		printMsg("TapirTest.test02splink()");
		try {
			// test speciesLink connector
			try (final SpeciesLinkConnector splink = new SpeciesLinkConnector()) {
				final Set<String> collections = splink.collectionNames();
				assertThat("collections is not null or empty", collections, allOf(notNullValue(), not(empty())));
				for (final String collection : collections) {
					// test count
					final long count = splink.count(collection);
					assertThat("collection contains at least one element", count, greaterThan(0l));
					// uncomment for additional output
					printMsg(" >> speciesLink collection '" + collection + "' count: " + count);

					// test collection fetching
					final SimpleDarwinRecordSet dwcSet = splink.fetch(collection, 0, 1);
					assertThat("DwC set is not null", dwcSet, notNullValue());
					assertThat("DwC records are not empty", dwcSet.getSimpleDarwinRecord(), allOf(notNullValue(), not(empty())));
					assertThat("DwC number of records coincides with expected", dwcSet.getSimpleDarwinRecord().size(), equalTo(1));
					// uncomment for additional output
					printMsg(" >> speciesLink collection '" + collection + "' fetched (start:0, limit:1):\n" + DWC_XMLB.typeToXml(dwcSet));
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("TapirTest.test02splink() failed: " + e.getMessage());
		} finally {			
			printMsg("TapirTest.test02splink() has finished");
		}
	}

	/**
	 * Checks whether a flag file is available in the local filesystem.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public class EnableSpeciesLinkTestIsFound implements IgnoreCondition {
		private final File file = new File(concat(DEFAULT_LOCATION, TEST_CONFIG_DIR + separator + "splink.test"));
		@Override
		public boolean isSatisfied() {			
			return !file.canRead();
		}		
	}

}