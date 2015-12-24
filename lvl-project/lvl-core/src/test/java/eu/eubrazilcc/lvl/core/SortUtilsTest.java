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

import static eu.eubrazilcc.lvl.core.util.SortUtils.parseSorting;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.util.SortUtils;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests {@link SortUtils} utility class.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SortUtilsTest extends LeishvlTestCase {

	public SortUtilsTest() {
		super(false);
	}

	@Test
	public void test() {
		printMsg("SortUtilsTest.test()");
		try {
			// test ascending order
			Sorting sorting = parseSorting("field1", "asc");
			assertThat("sorting is not null", sorting, notNullValue());
			assertThat("sorting value coincides with expected", "field1".equals(sorting.getField()) && Order.ASC.equals(sorting.getOrder()), equalTo(true));

			// test descending order
			sorting = parseSorting("field2", "desc");
			assertThat("sorting is not null", sorting, notNullValue());
			assertThat("sorting value coincides with expected", "field2".equals(sorting.getField()) && Order.DESC.equals(sorting.getOrder()), equalTo(true));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SortUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("SortUtilsTest.test() has finished");
		}
	}

}