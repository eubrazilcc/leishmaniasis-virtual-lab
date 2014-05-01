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

package eu.eubrazilcc.lvl.core.geocoding;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;

import eu.eubrazilcc.lvl.core.geospatial.Point;

/**
 * Converts addresses into geographic coordinates using the Google Geocoding API v3.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class GeocodingHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeocodingHelper.class);

	/**
	 * Converts a Java {@link Locale} to geographic coordinates.
	 * @param locale - the locale to be converted
	 * @return a {@link Point} that represents a geospatial location in GeoJSON format using
	 *         WGS84 coordinate reference system (CRS).
	 */
	public static final Point geocode(final Locale locale) {
		checkArgument(locale != null, "Uninitialized or invalid locale");
		return geocode(locale.getDisplayCountry(Locale.ENGLISH));
	}

	/**
	 * Converts an address to geographic coordinates.
	 * @param address - the address to be converted
	 * @return a {@link Point} that represents a geospatial location in GeoJSON format using
	 *         WGS84 coordinate reference system (CRS).
	 */
	public static final Point geocode(final String address) {
		checkArgument(isNotBlank(address), "Uninitialized or invalid address");
		Point point = null;
		try {
			final Geocoder geocoder = new Geocoder();
			final GeocodeResponse response = geocoder.geocode(new GeocoderRequestBuilder().setAddress(address)
					.setLanguage("en")
					.getGeocoderRequest());
			checkState(response != null, "No response received from Google Geocoding service");
			checkState(response.getStatus() != null && response.getStatus().equals(GeocoderStatus.OK), 
					"Searching for '" + address + "' produces invalid Google Geocoding server response: " 
							+ response.getStatus());
			if (response.getResults() != null) {
				for (int i = 0; i < response.getResults().size() && point == null; i++) {
					final GeocoderResult result = response.getResults().get(i);
					if (result != null && result.getGeometry() != null 
							&& result.getGeometry().getLocation() != null
							&& result.getGeometry().getLocation().getLng() != null
							&& result.getGeometry().getLocation().getLat() != null) {
						point = Point.builder()
								.coordinate(result.getGeometry().getLocation().getLng().doubleValue(), 
										result.getGeometry().getLocation().getLat().doubleValue())
										.build();
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to convert address to geographic coordinates", e);
		}
		return point;
	}

}