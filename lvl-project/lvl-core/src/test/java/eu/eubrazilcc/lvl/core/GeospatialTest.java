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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.geospatial.Line;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;

/**
 * Test geospatial types and utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GeospatialTest {

	@Test
	public void test() {
		System.out.println("GeoJsonTest.test()");
		try {
			// GeoJSON
			final Point point = Point.builder()
					.coordinate(1.0d, 1.0d)
					.build();
			assertThat("point is not null", point, notNullValue());
			System.out.println("point: " + point.toString());			
			final Line line = Line.builder()			
					.coordinate(1.0d, 1.0d)
					.coordinate(2.0d, 2.0d)
					.build();
			assertThat("line is not null", line, notNullValue());
			System.err.println("line: " + line.toString());			
			final Polygon polygon = Polygon.builder()
					.coordinate(1.0d, 1.0d)
					.coordinate(2.0d, 2.0d)
					.coordinate(3.0d, 3.0d)
					.coordinate(1.0d, 1.0d)
					.build();
			assertThat("polygon is not null", polygon, notNullValue());
			System.err.println("polygon: " + polygon.toString());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("GeoJsonTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("GeoJsonTest.test() has finished");
		}
	}

}