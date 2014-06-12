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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.eubrazilcc.lvl.core.geospatial.FeatureCollection;
import eu.eubrazilcc.lvl.core.geospatial.Point;

/**
 * Tests mapping utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MappingUtilsTest {

	@Test
	public void test() {
		System.out.println("MappingUtilsTest.test()");
		try {
			// feature collection
			final FeatureCollection collection = FeatureCollection.builder()
					.wgs84()
					.feature(Point.builder().coordinate(1.0d, 2.0d).build())
					.feature(Point.builder().coordinate(3.0d, 4.0d).build())
					.build();
			final ObjectMapper mapper = new ObjectMapper();
			final String payload = mapper.writeValueAsString(collection);
			assertThat("payload is not null", payload, notNullValue());
			assertThat("payload is not empty", isNotBlank(payload));			
			System.out.println(payload);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("MappingUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("MappingUtilsTest.test() has finished");
		}
	}

}