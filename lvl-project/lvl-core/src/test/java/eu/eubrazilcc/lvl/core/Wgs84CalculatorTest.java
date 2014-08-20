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

import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.deg2rad;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.distance;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.geographicCenter;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator;

/**
 * Tests {@link Wgs84Calculator} utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Wgs84CalculatorTest {

	@Test
	public void test() {
		System.out.println("Wgs84CalculatorTest.test()");
		try {
			// test convert degrees to radians
			final Point barcelona = Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(2.1734034999999494d, 41.3850639d).build())
					.build();
			assertThat("distance coincides with expected (with 2 decimal places)", 
					deg2rad(barcelona.getCoordinates().getLatitude()), closeTo(0.72d, 0.01d));
			assertThat("distance coincides with expected (with 2 decimal places)", 
					deg2rad(barcelona.getCoordinates().getLongitude()), closeTo(0.04d, 0.01d));				

			// test calculate distance in kilometers
			final Point valencia = Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-0.3762881000000107d, 39.4699075d).build())
					.build();
			final Point madrid = Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-3.7037901999999576d, 40.4167754d).build())
					.build();
			assertThat("point A is not null", valencia, notNullValue());
			assertThat("point B is not null", madrid, notNullValue());
			/* uncomment for additional output */
			System.out.println(" >> point A: " + valencia.toString());
			System.out.println(" >> point B: " + madrid.toString());

			final double distance = distance(valencia, madrid);
			assertThat("distance coincides with expected (with 2 decimal places)", distance, closeTo(302.90d, 0.01d));

			// test geographic center calculation
			final Point center = geographicCenter(valencia, barcelona, madrid);
			assertThat("geographic center is not null", center, notNullValue());
			assertThat("geographic center latitude coincides with expected", 
					center.getCoordinates().getLatitude(), closeTo(40.448708d, 0.00001d));
			assertThat("geographic center latitude coincides with expected", 
					center.getCoordinates().getLongitude(), closeTo(-0.647856d, 0.00001d));
			/* uncomment for additional output */
			System.out.println(" >> geographic center: " + center.toString());

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("Wgs84CalculatorTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("Wgs84CalculatorTest.test() has finished");
		}
	}

}