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

package eu.eubrazilcc.lvl.core.geospatial;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nullable;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Calculates distances and other metrics using World Geodetic System 84 (WGS84) coordinates.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://spatialreference.org/ref/epsg/4326/">epsg projection 4326 - wgs 84</a>
 */
public final class Wgs84Calculator {

	/**
	 * The diameter of earth in kilometers according to WGS84 system.
	 */
	public static final double EARTH_DIAMETER_KM = 6378.137d;

	/**
	 * The diameter of earth in miles according to WGS84 system.
	 */
	public static final double EARTH_DIAMETER_MI = 3963.191d;

	/**
	 * Minimum decimal degree precision: approximately 1.1132 meters.
	 * @see <a href="http://en.wikipedia.org/wiki/Decimal_degrees">Decimal degrees</a>
	 */
	public static final double DEGREE_PRECISION_MIN = 0.00001d;

	/**
	 * Minimum length precision (in meters): approximately 0.00001 degrees.
	 * @see <a href="http://en.wikipedia.org/wiki/Decimal_degrees">Decimal degrees</a>
	 */
	public static final double LENGTH_PRECISION_MIN = 1.1132d;

	/**
	 * Minimum longitude in WGS84 coordinates.
	 */
	public static final double WGS84_LONGITUDE_MIN = -180.0d;

	/**
	 * Maximum longitude in WGS84 coordinates.
	 */
	public static final double WGS84_LONGITUDE_MAX = 180.0d;

	/**
	 * Minimum latitude in WGS84 coordinates.
	 */
	public static final double WGS84_LATITUDE_MIN = -85.0d;

	/**
	 * Maximum latitude in WGS84 coordinates.
	 */
	public static final double WGS84_LATITUDE_MAX = 85.0d;	

	/**
	 * Calculate distance (in kilometers) between two WGS84 coordinates using the formula:
	 * <br><br>
	 * {@code distance = earth_diameter * ACos( Cos( Lat1 ) * Cos( Lat2 ) * Cos( Lon2 - Lon1 ) + Sin( Lat1 ) * Sin( Lat2 ) )}
	 * <br><br>
	 * Where: ACos - arc cosine
	 * @see <a href="http://www.mapanet.eu/EN/resources/Script-Distance.htm">Calculate distance between 2 geographical coordinates</a>
	 * @param a - point A
	 * @param b - point A
	 * @return the distance (in kilometers) between points A and B.
	 */
	public static double distance(final Point a, final Point b) {
		if (a.getCoordinates().equals(b.getCoordinates())) {
			return 0.0d;
		}		
		final double lat1 = deg2rad(a.getCoordinates().getLatitude()), lng1 = deg2rad(a.getCoordinates().getLongitude()), 
				lat2 = deg2rad(b.getCoordinates().getLatitude()), lng2 = deg2rad(b.getCoordinates().getLongitude());
		return EARTH_DIAMETER_KM * Math.acos(Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1) + Math.sin(lat1) * Math.sin(lat2));
	}

	/**
	 * Converts from degrees to radians.
	 * @param degrees - degrees to convert.
	 * @return The corresponding value in radians.
	 */
	public static double deg2rad(final double degrees) {
		return degrees * Math.PI / 180.0d;
	}

	/**
	 * Converts from radians to degrees.
	 * @param rads - radians to convert.
	 * @return The corresponding value in degrees.
	 */
	public static double rad2deg(final double rads) {
		return rads * 180.0d / Math.PI;
	}

	/**
	 * Converts latitude/longitude coordinates (in radians) to Cartesian coordinates.
	 * @param lat - latitude (in radians) to convert.
	 * @param lng - longitude (in radians) to convert.
	 * @return an array of three elements with Cartesian coordinates X, Y, Z in arrays positions 
	 *         0, 1 and 2, respectively.
	 */
	public static double[] rad2car(final double lat, final double lng) {
		return new double[]{ Math.cos(lat) * Math.cos(lng), Math.cos(lat) * Math.sin(lng), Math.sin(lat) };
	}

	/**
	 * Converts {@link Point} to Cartesian coordinates.
	 * @param point - the location to convert.
	 * @return an array of three elements with Cartesian coordinates X, Y, Z in array positions 
	 *         0, 1 and 2, respectively.
	 */
	public static double[] point2car(final Point point) {
		final double lat = deg2rad(point.getCoordinates().getLatitude());
		final double lng = deg2rad(point.getCoordinates().getLongitude());
		return rad2car(lat, lng);
	}

	/**
	 * Convert Cartesian coordinates to latitude/longitude coordinates (in radians);
	 * @param x - x coordinate to convert.
	 * @param y - y coordinate to convert.
	 * @param z - z coordinate to convert.
	 * @return an array of two elements with latitude, longitude coordinates (in radians) in array 
	 *         positions 0 and 1, respectively.
	 */
	public static double[] car2rad(final double x, final double y, final double z) {
		final double lng = Math.atan2(y, x);
		final double hyp = Math.sqrt(x * x + y * y);
		final double lat = Math.atan2(z, hyp);
		return new double[]{ lat, lng };
	}

	/**
	 * Convert Cartesian coordinates to latitude/longitude coordinates (in degrees);
	 * @param x - x coordinate to convert.
	 * @param y - y coordinate to convert.
	 * @param z - z coordinate to convert.
	 * @return an array of two elements with latitude, longitude coordinates (in degrees) in array 
	 *         positions 0 and 1, respectively.
	 */
	public static double[] car2deg(final double x, final double y, final double z) {
		final double[] rads = car2rad(x, y, z);
		return new double[]{ rad2deg(rads[0]), rad2deg(rads[1]) };
	}	

	public static Point geographicCenter(final Point ... points) {
		return points != null ? geographicCenter(asList(points)) : null;
	}

	/**
	 * Calculates the geographic midpoint (also known as the geographic center, or center of gravity) for 
	 * two or more points on the earth's surface. 
	 * @param points - the points for which the geographic midpoint will be calculated.
	 * @return a {@link Point} that contains the geographic midpoint of the specified points.
	 * @see <a href="http://www.geomidpoint.com/calculation.html">Calculation Methods</a>
	 */
	public static @Nullable Point geographicCenter(final List<Point> points) {
		// special cases
		if (points == null) {
			return null;
		} else {
			if (points.size() == 0) {
				return null;
			} else if (points.size() == 1) {
				return points.get(0);
			}
		}
		// compute weighted average by x, y, z coordinate (all locations are weighted equally)
		double x = 0.0d, y = 0.0d, z = 0.0d;
		for (int i = 0; i < points.size(); i++) {
			double[] cartesian = point2car(points.get(i));
			x += cartesian[0];
			y += cartesian[1];
			z += cartesian[2];
		}
		x /= points.size();
		y /= points.size();
		z /= points.size();

		// special case: if abs(x) < 10-9 and abs(y) < 10-9 and abs(z) < 10-9 then the geographic 
		// midpoint is the center of the earth
		x = Math.abs(x) < 10e-9d ? 0.0d : x;
		y = Math.abs(y) < 10e-9d ? 0.0d : y;
		z = Math.abs(z) < 10e-9d ? 0.0d : z;

		// convert average x, y, z coordinate to latitude and longitude (in degrees)
		final double[] center = car2deg(x, y, z);		
		return Point.builder()
				.coordinates(LngLatAlt.builder().coordinates(center[1], center[0]).build())
				.build();
	}

	/**
	 * Creates a matrix with enough space to store the specified number of points and fills the matrix
	 * by translating the initial coordinates. The central position of the matrix is filled with the 
	 * specified point and the rest of the positions are filled with incremental steps of 
	 * {@link Wgs84Calculator#DEGREE_PRECISION_MIN} degrees.
	 * @param count - the number of points to store.
	 * @param point - initial coordinates.
	 * @return a matrix filled with steps of {@link Wgs84Calculator#DEGREE_PRECISION_MIN} degrees and the
	 *         specified point in the central position.
	 */
	public static Point[][] matrixPoints(final int count, final Point point) {
		final int n = (int) Math.ceil(Math.sqrt(count));
		final Point[][] matrix = new Point[n][n];
		// calculate initial coordinates
		final Point initial = translate(point, -n/2 * DEGREE_PRECISION_MIN, n/2 * DEGREE_PRECISION_MIN);
		double lng = initial.getCoordinates().getLongitude(), lat = initial.getCoordinates().getLatitude(), aux = lng;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				matrix[i][j] = Point.builder()
						.coordinates(LngLatAlt.builder().coordinates(aux, lat).build())
						.build();				
				aux += DEGREE_PRECISION_MIN;
			}
			lat -= DEGREE_PRECISION_MIN;
			aux = lng;
		}
		return matrix;
	}

	public static Point translate(final Point point, final double lngInc, final double latInc) {
		double lng = normalize(point.getCoordinates().getLongitude() + lngInc, WGS84_LONGITUDE_MIN, WGS84_LONGITUDE_MAX);
		double lat = normalize(point.getCoordinates().getLatitude() + latInc, WGS84_LATITUDE_MIN, WGS84_LATITUDE_MAX);
		return Point.builder().coordinates(LngLatAlt.builder().coordinates(lng, lat).build()).build();		
	}

	public static double normalize(final double coordinate, final double min, final double max) {
		BigDecimal normalized = new BigDecimal(coordinate);
		if (coordinate < min || coordinate > max) {
			final BigDecimal min2 = new BigDecimal(min), max2 = new BigDecimal(max);
			final BigDecimal total = min2.abs().add(max2.abs());			
			normalized = normalized.remainder(total);
			if (coordinate < min) {
				if (normalized.compareTo(min2) < 0) {				
					normalized = max2.add(normalized).subtract(min2);
				}
			} else {
				if (normalized.compareTo(max2) > 0) {
					normalized = min2.add(normalized).subtract(max2);
				}
			}
		}
		return normalized.doubleValue();
	}

}