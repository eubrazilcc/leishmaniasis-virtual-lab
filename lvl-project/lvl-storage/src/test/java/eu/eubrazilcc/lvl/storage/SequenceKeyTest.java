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

package eu.eubrazilcc.lvl.storage;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests sequence key utility class.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceKeyTest {

	@Test
	public void test() {
		System.out.println("SequenceKeyTest.test()");
		try {
			// test sequence key parsing (using comma separator)
			SequenceKey sequenceKey = SequenceKey.builder()
					.parse("GenBank,U49845", ',');
			assertThat("sequence key is not null", sequenceKey, notNullValue());
			assertThat("sequence key data source is not empty", isNotBlank(sequenceKey.getDataSource()));
			assertThat("sequence key accession is not empty", isNotBlank(sequenceKey.getAccession()));
			assertThat("sequence key data source coincides with expected", sequenceKey.getDataSource(), equalTo("GenBank"));
			assertThat("sequence key accession coincides with expected", sequenceKey.getAccession(), equalTo("U49845"));
			/* uncomment for additional output */
			System.out.println(" >> Sequence key: " + sequenceKey);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SequenceKeyTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SequenceKeyTest.test() has finished");
		}
	}

}