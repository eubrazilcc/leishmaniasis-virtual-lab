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

import static com.google.common.collect.ImmutableMap.of;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.LEISHVL;
import static eu.eubrazilcc.lvl.core.DataSource.LEISHVL_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.PUBMED;
import static eu.eubrazilcc.lvl.core.DataSource.PUBMED_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.toLongNotation;
import static eu.eubrazilcc.lvl.core.DataSource.toShortNotation;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Tests {@link DataSource} manipulation utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DataSourceTest {

	@Test
	public void test() {
		System.out.println("DataSourceTest.test()");
		try {
			// create test datasets
			final ImmutableMap<String, String> testDataset1 = of(GENBANK_SHORT, GENBANK, PUBMED_SHORT, PUBMED, LEISHVL_SHORT, LEISHVL);
			final ImmutableMap<String, String> testDataset2 = of(GENBANK, GENBANK_SHORT, PUBMED, PUBMED_SHORT, GENBANK_SHORT, GENBANK_SHORT, 
					PUBMED_SHORT, PUBMED_SHORT, LEISHVL, LEISHVL_SHORT);
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
			builder.put(GENBANK_SHORT, GENBANK); builder.put(PUBMED_SHORT, PUBMED); builder.put(LEISHVL_SHORT, LEISHVL);
			builder.put(GENBANK, GENBANK); builder.put(PUBMED, PUBMED); builder.put(LEISHVL, LEISHVL);
			final ImmutableMap<String, String> testDataset3 = builder.build();			
			
			// test conversion from long to short notation
			for (final Entry<String, String> entry : testDataset1.entrySet()) {
				final String dataSource = toShortNotation(entry.getValue(), NOTATION_LONG);
				assertThat("converted data source is not null", dataSource, notNullValue());
				assertThat("converted data source is not empty", isNotBlank(dataSource), equalTo(true));
				assertThat("converted data source coincides with expected", dataSource, equalTo(entry.getKey()));
			}

			// test conversion from short to long notation
			for (final Entry<String, String> entry : testDataset1.entrySet()) {
				final String dataSource = toLongNotation(entry.getKey(), NOTATION_SHORT);
				assertThat("converted data source is not null", dataSource, notNullValue());
				assertThat("converted data source is not empty", isNotBlank(dataSource), equalTo(true));
				assertThat("converted data source coincides with expected", dataSource, equalTo(entry.getValue()));
			}

			// test conversion to short notation discovering the original notation
			for (final Entry<String, String> entry : testDataset2.entrySet()) {
				final String dataSource = toShortNotation(entry.getKey());
				assertThat("converted data source is not null", dataSource, notNullValue());
				assertThat("converted data source is not empty", isNotBlank(dataSource), equalTo(true));
				assertThat("converted data source coincides with expected", dataSource, equalTo(entry.getValue()));
			}

			// test conversion to long notation discovering the original notation
			for (final Entry<String, String> entry : testDataset3.entrySet()) {
				final String dataSource = toLongNotation(entry.getKey());
				assertThat("converted data source is not null", dataSource, notNullValue());
				assertThat("converted data source is not empty", isNotBlank(dataSource), equalTo(true));
				assertThat("converted data source coincides with expected", dataSource, equalTo(entry.getValue()));
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DataSourceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DataSourceTest.test() has finished");
		}
	}

}