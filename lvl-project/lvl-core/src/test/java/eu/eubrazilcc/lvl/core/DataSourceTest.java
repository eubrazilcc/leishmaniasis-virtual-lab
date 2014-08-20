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

import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.toLongNotation;
import static eu.eubrazilcc.lvl.core.DataSource.toShortNotation;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Test {@link DataSource} manipulation utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DataSourceTest {

	@Test
	public void test() {
		System.out.println("DataSourceTest.test()");
		try {
			// test conversion from long to short notation
			String dataSource = toShortNotation(GENBANK, NOTATION_LONG);
			assertThat("converted data source is not null", dataSource, notNullValue());
			assertThat("converted data source is not empty", isNotBlank(dataSource), equalTo(true));
			assertThat("converted data source coincides with expected", dataSource, equalTo(GENBANK_SHORT));

			// test conversion from short to long notation
			dataSource = toLongNotation(GENBANK_SHORT, NOTATION_SHORT);
			assertThat("converted data source is not null", dataSource, notNullValue());
			assertThat("converted data source is not empty", isNotBlank(dataSource), equalTo(true));
			assertThat("converted data source coincides with expected", dataSource, equalTo(GENBANK));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DataSourceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DataSourceTest.test() has finished");
		}
	}

}