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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Splitter.on;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Iterables.getFirst;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static java.lang.Thread.sleep;
import static java.util.Locale.ENGLISH;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Converts addresses into geographic coordinates using the Google Geocoding API v3.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class GeocodingHelper {
	
	private static final Logger LOGGER = getLogger(GeocodingHelper.class);

	public static final int MAX_CACHED_ELEMENTS = 1000;
	public static final char COUNTRY_SEPARATOR = ':';	

	public static final int OVER_QUERY_LIMIT_MIN_DELAY = 800;
	public static final int OVER_QUERY_LIMIT_MAX_DELAY = 2000;

	private static final LoadingCache<String, Optional<Point>> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.build(new CacheLoader<String, Optional<Point>>() {
				@Override
				public Optional<Point> load(final String key) throws ExecutionException {
					return geocodeFromGoogle(key, true, true);
				}
				@Override
				public ListenableFuture<Optional<Point>> reload(final String key, final Optional<Point> oldValue) throws Exception {
					return TASK_RUNNER.submit(new Callable<Optional<Point>>() {
						public Optional<Point> call() {
							return geocodeFromGoogle(key, true, true);
						}
					});					
				}
			});

	/**
	 * Converts a Java {@link Locale} to geographic coordinates.
	 * @param locale - the locale to be converted
	 * @return a {@link Point} that represents a geospatial location in GeoJSON format using WGS84 
	 *         coordinate reference system (CRS).
	 */
	public static final Optional<Point> geocode(final Locale locale) {
		checkArgument(locale != null, "Uninitialized or invalid locale");
		return geocode(locale.getDisplayCountry(ENGLISH));
	}

	/**
	 * Converts an address to geographic coordinates.
	 * @param address - the address to be converted
	 * @return a {@link Point} that represents a geospatial location in GeoJSON format using WGS84 
	 *         coordinate reference system (CRS).
	 */
	public static final Optional<Point> geocode(final String address) {
		checkArgument(isNotBlank(address), "Uninitialized or invalid address");
		Optional<Point> point = absent();
		try {
			point = CACHE.get(address.trim());
		} catch (Exception e) {
			LOGGER.error("Failed to get geospatial location from cache", e);
		}
		return point;
	}

	/**
	 * Returns the geographic coordinates associated with the specified address in this cache, or {@code null} 
	 * if there is no cached value for address.
	 * @param address - the address to be searched for
	 * @return the geographic coordinates associated with the specified address in this cache, or {@code null} 
	 *         if there is no cached value for address.
	 */
	public static final Optional<Point> getIfPresent(final String address) {
		return CACHE.getIfPresent(address);
	}

	/**
	 * Converts an address to geographic coordinates. The expected address format is: <code>'country:region'</code>. 
	 * @param address - the address to be converted
	 * @param recoverZeroResults - when the query fails due to an error in the region, the method will try a second 
	 *        search using only the country part of the address
	 * @param recoverOverQueryLimit - when the maximum number of queries is reached, the method will suspend for a
	 *        predefined time and will retry the same search one more ocassion
	 * @return a {@link Point} that represents a geospatial location in GeoJSON format using WGS84 
	 *         coordinate reference system (CRS), or {@code null} if the address cannot be found.
	 */
	private static final Optional<Point> geocodeFromGoogle(final String address, final boolean recoverZeroResults, final boolean recoverOverQueryLimit) {
		checkArgument(isNotBlank(address), "Uninitialized or invalid address");
		Optional<Point> point = absent();
		try {
			final Geocoder geocoder = new Geocoder();
			final GeocodeResponse response = geocoder.geocode(new GeocoderRequestBuilder().setAddress(address)
					.setLanguage("en")
					.getGeocoderRequest());
			checkState(response != null, "No response received from Google Geocoding service");
			checkState(response.getStatus() != null, "No status code included in the response received from Google Geocoding service");
			switch (response.getStatus()) {
			case OK:
				break;
			case ZERO_RESULTS:
				if (recoverZeroResults) {
					final String country = countryName(address);
					checkState(isNotBlank(country), "Invalid address format, expected 'country:region' but found: " + address);					
					point = geocodeFromGoogle(country, false, recoverOverQueryLimit);
				}
				break;
			case OVER_QUERY_LIMIT:
				if (recoverOverQueryLimit) {
					try {
						sleep(new Random().nextInt(OVER_QUERY_LIMIT_MAX_DELAY - OVER_QUERY_LIMIT_MIN_DELAY + 1) + OVER_QUERY_LIMIT_MIN_DELAY);
						point = geocodeFromGoogle(address, recoverZeroResults, false);
					} catch (InterruptedException ie) { }					
				} else {
					// throwing an exception will prevent this value to be cached					
					throw new IllegalStateException("Search failed, maximum number of Google Geocoding queries exceeded");
				}
				break;
			default:
				throw new IllegalStateException("Searching for '" + address + "' produces invalid Google Geocoding server response: " 
						+ response.getStatus());
			}
			if (!point.isPresent() && response.getResults() != null) {
				for (int i = 0; i < response.getResults().size() && !point.isPresent(); i++) {
					final GeocoderResult result = response.getResults().get(i);
					if (result != null && result.getGeometry() != null 
							&& result.getGeometry().getLocation() != null
							&& result.getGeometry().getLocation().getLng() != null
							&& result.getGeometry().getLocation().getLat() != null) {
						point = of(Point.builder()
								.coordinates(LngLatAlt.builder()
										.longitude(result.getGeometry().getLocation().getLng().doubleValue())
										.latitude(result.getGeometry().getLocation().getLat().doubleValue())
										.build())
										.build());
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to convert address to geographic coordinates", e);
		}
		return point;
	}

	private static final String countryName(final String address) {
		return getFirst(on(COUNTRY_SEPARATOR)
				.trimResults()
				.omitEmptyStrings()
				.split(address), "");
	}

}