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

import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.storage.SampleKey.Builder.FLEXIBLE_NUMBER_PATTERN;
import static eu.eubrazilcc.lvl.storage.SampleKey.Builder.IOCL_PATTERN;
import static eu.eubrazilcc.lvl.storage.SampleKey.Builder.NUMBER_YEAR_PATTERN;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests sample key utility class.
 * @author Erik Torres <ertorser@upv.es>
 */
@RunWith(Parameterized.class)
public class SampleKeyTest extends LeishvlTestCase {

	public SampleKeyTest() {
		super(false);
	}

	@Parameters(name = "{index}: id={0}, separator={1}, pattern={2}, notation={3}, collectionId={4}, catalogNumber={5}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ "fcf:051/75",     ID_FRAGMENT_SEPARATOR, NUMBER_YEAR_PATTERN,     NOTATION_LONG,  COLFLEB,       "051/75" },
			{ "fcf:051/75",     ID_FRAGMENT_SEPARATOR, NUMBER_YEAR_PATTERN,     NOTATION_SHORT, COLFLEB_SHORT, "051/75" },
			{ "fcf:051/75_1",   ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_LONG,  COLFLEB,       "051/75_1" },
			{ "fcf:051/75_1",   ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_SHORT, COLFLEB_SHORT, "051/75_1" },
			{ "fcf:10849",      ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_LONG,  COLFLEB,       "10849" },
			{ "fcf:10849",      ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_SHORT, COLFLEB_SHORT, "10849" },
			{ "fcf:109_1",      ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_LONG,  COLFLEB,       "109_1" },
			{ "fcf:109_1",      ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_SHORT, COLFLEB_SHORT, "109_1" },
			{ "fcf:1106/63",    ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_LONG,  COLFLEB,       "1106/63" },
			{ "fcf:1106/63",    ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_SHORT, COLFLEB_SHORT, "1106/63" },
			{ "fcf:1106/63_2",  ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_LONG,  COLFLEB,       "1106/63_2" },
			{ "fcf:1106/63_2",  ID_FRAGMENT_SEPARATOR, FLEXIBLE_NUMBER_PATTERN, NOTATION_SHORT, COLFLEB_SHORT, "1106/63_2" },
			{ "fcl:IOCL 3468",  ID_FRAGMENT_SEPARATOR, IOCL_PATTERN,            NOTATION_LONG,  CLIOC,         "IOCL 3468" },
			{ "fcl:IOCL 3468",  ID_FRAGMENT_SEPARATOR, IOCL_PATTERN,            NOTATION_SHORT, CLIOC_SHORT,   "IOCL 3468" },
			{ "fcl:OE 2012037", ID_FRAGMENT_SEPARATOR, IOCL_PATTERN,            NOTATION_LONG,  CLIOC,         "OE 2012037" },
			{ "fcl:OE 2012037", ID_FRAGMENT_SEPARATOR, IOCL_PATTERN,            NOTATION_SHORT, CLIOC_SHORT,   "OE 2012037" }
		});
	}

	@Parameter(value = 0) public String id;
	@Parameter(value = 1) public char separator;
	@Parameter(value = 2) public String pattern;
	@Parameter(value = 3) public DataSource.Notation notation;
	@Parameter(value = 4) public String collectionId;
	@Parameter(value = 5) public String catalogNumber;

	@Test
	public void test() {
		printMsg("SampleKeyTest.test()");
		try {			
			// test sample key parsing
			SampleKey sampleKey = SampleKey.builder().parse(id, separator, pattern, notation);
			assertThat("sample key is not null", sampleKey, notNullValue());
			assertThat("collection Id coincides with expected", trim(sampleKey.getCollectionId()), allOf(notNullValue(), equalTo(collectionId)));
			assertThat("catalog number coincides with expected", trim(sampleKey.getCatalogNumber()), allOf(notNullValue(), equalTo(catalogNumber)));			
			// conditional output
			printMsg(" >> Sample key: " + sampleKey);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SampleKeyTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("SampleKeyTest.test() has finished");
		}
	}

}