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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Range.closed;

import com.google.common.collect.Range;

/**
 * Validates World Geodetic System 84 (WGS84) coordinates.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://spatialreference.org/ref/epsg/4326/">epsg projection 4326 - wgs 84</a>
 * @see <a href="http://wiki.geojson.org/GeoJSON_draft_version_5">GeoJSON draft version 5</a>
 */
public final class Wgs84Validator {

	public static final String LEGACY_CRS = "EPSG:3857";
	public static final String URN_CRS = "urn:ogc:def:crs:OGC:1.3:CRS84";
	
	private static final Range<Double> LONGITUDE_RANGE = closed(-180.0d, 180.0d);
	private static final Range<Double> LATITUDE_RANGE = closed(-90.0d, 90.0d);

	public static final double checkLongitude(final double longitude) {
		checkArgument(LONGITUDE_RANGE.contains(longitude));
		return longitude;
	}

	public static final double checkLatitude(final double latitude) {
		checkArgument(LATITUDE_RANGE.contains(latitude));
		return latitude;
	}

}