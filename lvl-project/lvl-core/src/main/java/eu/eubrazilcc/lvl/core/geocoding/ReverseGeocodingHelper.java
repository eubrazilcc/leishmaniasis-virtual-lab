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

package eu.eubrazilcc.lvl.core.geocoding;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Performs reverse geographic coordinates lookups using the Google Geocoding API v3.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ReverseGeocodingHelper {

	private static final Logger LOGGER = getLogger(GeocodingHelper.class);

	public static final int MAX_CACHED_ELEMENTS = 1000;

	public static final int OVER_QUERY_LIMIT_MIN_DELAY = 800;
	public static final int OVER_QUERY_LIMIT_MAX_DELAY = 2000;

	private static final LoadingCache<Point, Optional<String>> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.build(new CacheLoader<Point, Optional<String>>() {
				@Override
				public Optional<String> load(final Point location) throws ExecutionException {
					return countryLookup(location, true);
				}
				@Override
				public ListenableFuture<Optional<String>> reload(final Point location, final Optional<String> oldValue) throws Exception {
					return TASK_RUNNER.submit(new Callable<Optional<String>>() {
						public Optional<String> call() {
							return countryLookup(location, true);
						}
					});					
				}
			});

	/**
	 * Reverse lookups a country from the provided geographic coordinates.
	 * @param location - geographic coordinates to search for
	 * @return the long name of the country where the provided geographic coordinates point to.
	 */
	public static final Optional<String> rgeocode(final Point location) {
		requireNonNull(location, "A valid location expected");
		Optional<String> country = empty();
		try {
			country = CACHE.get(location);			
		} catch (Exception e) {
			LOGGER.error("Failed to get country from cache", e);
		}
		return country;
	}

	private static final Optional<String> countryLookup(final Point location, final boolean recoverOverQueryLimit) {
		requireNonNull(location, "A valid location expected");
		requireNonNull(location.getCoordinates(), "A valid location expected");
		Optional<String> country = empty();
		try {
			final Geocoder geocoder = new Geocoder();
			final GeocodeResponse response = geocoder.geocode(new GeocoderRequestBuilder()
					.setLocation(new LatLng(BigDecimal.valueOf(location.getCoordinates().getLatitude()), BigDecimal.valueOf(location.getCoordinates().getLongitude())))
					.setLanguage("en")
					.getGeocoderRequest());
			checkState(response != null, "No response received from Google Geocoding service");
			checkState(response.getStatus() != null, "No status code included in the response received from Google Geocoding service");
			switch (response.getStatus()) {
			case OK:
				break;			
			case OVER_QUERY_LIMIT:
				if (recoverOverQueryLimit) {
					try {
						sleep(new Random().nextInt(OVER_QUERY_LIMIT_MAX_DELAY - OVER_QUERY_LIMIT_MIN_DELAY + 1) + OVER_QUERY_LIMIT_MIN_DELAY);
						country = countryLookup(location, false);
					} catch (InterruptedException ie) { }					
				} else {
					// throwing an exception will prevent this value to be cached					
					throw new IllegalStateException("Search failed, maximum number of Google Geocoding queries exceeded");
				}
				break;
			case ZERO_RESULTS:
			default:
				throw new IllegalStateException(String.format("Searching for %s produces invalid Google Geocoding server response: %s", location, response.getStatus()));
			}
			if (!country.isPresent() && response.getResults() != null) {
				for (int i = 0; i < response.getResults().size() && !country.isPresent(); i++) {
					final GeocoderResult result = response.getResults().get(i);
					if (result != null && result.getAddressComponents() != null) {
						for (int j = 0; j < result.getAddressComponents().size() && !country.isPresent(); j++) {
							final GeocoderAddressComponent component = result.getAddressComponents().get(j);
							if (component.getTypes() != null && component.getTypes().contains("country")) {
								country = ofNullable(component.getLongName());
							}
						}
					}					
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to lookup for country", e);
		}
		return country;
	}

}