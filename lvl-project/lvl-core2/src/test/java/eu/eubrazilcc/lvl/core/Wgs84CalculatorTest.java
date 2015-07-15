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

import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.WGS84_LATITUDE_MAX;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.WGS84_LATITUDE_MIN;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.WGS84_LONGITUDE_MAX;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.WGS84_LONGITUDE_MIN;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.deg2rad;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.distance;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.geographicCenter;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.matrixPoints;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.normalize;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.translate;
import static org.hamcrest.CoreMatchers.equalTo;
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

			// test coordinate normalization
			double coordinate = 1237.13d;
			double normalized = normalize(coordinate, WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
			assertThat("normalized coordinate coincides with expected", normalized, closeTo(157.13d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinate=" + coordinate + ", normalized=" + normalized);

			coordinate = 1297.83d;
			normalized = normalize(coordinate, WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
			assertThat("normalized coordinate coincides with expected", normalized, closeTo(-142.17d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinate=" + coordinate + ", normalized=" + normalized);

			coordinate = -472.11d;;
			normalized = normalize(coordinate, WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
			assertThat("normalized coordinate coincides with expected", normalized, closeTo(-112.11d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinate=" + coordinate + ", normalized=" + normalized);

			coordinate = -1363.56d;
			normalized = normalize(coordinate, WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
			assertThat("normalized coordinate coincides with expected", normalized, closeTo(76.44d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinate=" + coordinate + ", normalized=" + normalized);

			coordinate = -13.29d;
			normalized = normalize(coordinate, WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
			assertThat("normalized coordinate coincides with expected", normalized, equalTo(-13.29d));
			/* uncomment for additional output */
			System.out.println(" >> coordinate=" + coordinate + ", normalized=" + normalized);

			coordinate = -887.91d;
			normalized = normalize(coordinate, WGS84_LATITUDE_MIN, WGS84_LATITUDE_MAX);
			assertThat("normalized coordinate coincides with expected", normalized, closeTo(-37.91d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinate=" + coordinate + ", normalized=" + normalized);

			// test coordinate translation
			Point point = Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0d, 0.0d).build()).build();
			Point translated = translate(point, 21.11d, -13.2d);
			assertThat("translated longitude coincides with expected", translated.getCoordinates().getLongitude(), closeTo(21.11d, 0.0000001d));
			assertThat("translated latitude coincides with expected", translated.getCoordinates().getLatitude(), closeTo(-13.2d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinates=" + translated.getCoordinates() + ", translated=" + translated.getCoordinates());

			point = Point.builder().coordinates(LngLatAlt.builder().coordinates(112.014d, -889.131d).build()).build();
			translated = translate(point, -1027.098d, 137.782d);
			assertThat("translated longitude coincides with expected", translated.getCoordinates().getLongitude(), closeTo(164.916d, 0.0000001d));
			assertThat("translated latitude coincides with expected", translated.getCoordinates().getLatitude(), closeTo(-71.349d, 0.0000001d));
			/* uncomment for additional output */
			System.out.println(" >> coordinates=" + translated.getCoordinates() + ", translated=" + translated.getCoordinates());

			// test matrix creation
			point = Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0d, 0.0d).build()).build();
			Point[][] matrix = matrixPoints(11, point);
			assertThat("matrix is not null", matrix, notNullValue());
			assertThat("matrix rows coincides with expected", matrix.length, equalTo(4));
			assertThat("matrix columns coincides with expected", matrix[0].length, equalTo(4));
			validate(matrix, point);
			/* uncomment for additional output */
			printMatrix(matrix);

			point = Point.builder().coordinates(LngLatAlt.builder().coordinates(179.99999d, -85.99996d).build()).build();			
			matrix = matrixPoints(10, point);
			assertThat("matrix is not null", matrix, notNullValue());
			assertThat("matrix rows coincides with expected", matrix.length, equalTo(4));
			assertThat("matrix columns coincides with expected", matrix[0].length, equalTo(4));
			validate(matrix, point);
			/* uncomment for additional output */
			printMatrix(matrix);

			point = Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0d, 0.0d).build()).build();
			matrix = matrixPoints(9, point);
			assertThat("matrix is not null", matrix, notNullValue());
			assertThat("matrix rows coincides with expected", matrix.length, equalTo(3));
			assertThat("matrix columns coincides with expected", matrix[0].length, equalTo(3));
			validate(matrix, point);
			/* uncomment for additional output */
			printMatrix(matrix);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("Wgs84CalculatorTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("Wgs84CalculatorTest.test() has finished");
		}
	}

	private static void validate(final Point[][] matrix, final Point point) {
		final Point center = matrix[matrix.length / 2][matrix[0].length / 2];
		final double lng = normalize(center.getCoordinates().getLongitude(), WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
		final double lat = normalize(center.getCoordinates().getLatitude(), WGS84_LATITUDE_MIN, WGS84_LATITUDE_MAX);
		assertThat("matrix center longitude coincides with expected", center.getCoordinates().getLongitude(), equalTo(lng));
		assertThat("matrix center latitude coincides with expected", center.getCoordinates().getLatitude(), equalTo(lat));		
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				final LngLatAlt coordinates = matrix[i][j].getCoordinates();				
				if (i < matrix.length - 1) {
					final LngLatAlt next = matrix[i + 1][j].getCoordinates();
					assertThat("matrix latitude coincides with expected: " + coordinates.getLatitude() + " > " + next.getLatitude(), 
							coordinates.getLatitude() > next.getLatitude(), equalTo(true));
				}
				if (j < matrix[i].length - 1) {
					final LngLatAlt next = matrix[i][j + 1].getCoordinates();
					assertThat("matrix longitude coincides with expected: " + coordinates.getLongitude() + " < " + next.getLongitude(), 
							coordinates.getLongitude() < next.getLongitude(), equalTo(true));
				}
			}
		}
	}

	private static void printMatrix(final Point[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			System.err.println();
			for (int j = 0; j < matrix[i].length; j++) {
				final LngLatAlt coordinates = matrix[i][j].getCoordinates();
				System.err.printf("(%11.5f,%11.5f) ", coordinates.getLongitude(), coordinates.getLatitude());
			}
		}
		System.err.println();
	}

}