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

package eu.eubrazilcc.lvl.service.cache;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static eu.eubrazilcc.lvl.core.analysis.SequenceAnalyzer.toFeatureCollection;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static java.lang.Double.valueOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Stores calls to the application database that includes a sequence geospatial query.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class SequenceGeolocationCache {

	private static final Logger LOGGER = getLogger(SequenceGeolocationCache.class);

	public static final int MAX_CACHED_ELEMENTS = 100;
	public static final int CACHE_EXPIRATION_SECONDS = 60; // one hour
	public static final String SEPARATOR = ":";

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###############");

	private static final LoadingCache<String, Optional<FeatureCollection>> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.refreshAfterWrite(CACHE_EXPIRATION_SECONDS, SECONDS)
			.build(new CacheLoader<String, Optional<FeatureCollection>>() {
				@Override
				public Optional<FeatureCollection> load(final String key) throws ExecutionException {
					return findNearbySequences(key);
				}
				@Override
				public ListenableFuture<Optional<FeatureCollection>> reload(final String key, final Optional<FeatureCollection> old) throws Exception {
					return TASK_RUNNER.submit(new Callable<Optional<FeatureCollection>>() {
						public Optional<FeatureCollection> call() {
							return findNearbySequences(key);
						}
					});					
				}
			});

	public static FeatureCollection findNearbySequences(final Point point, final double maxDistance, final boolean group, final boolean heatmap) {
		Optional<FeatureCollection> collection = absent();
		try {
			collection = CACHE.get(key(point, maxDistance, group, heatmap));
		} catch (ExecutionException e) {
			LOGGER.error("Failed to get sequence geolocation from cache", e);
		}
		return collection.or(FeatureCollection.builder().build());
	}

	private static Optional<FeatureCollection> findNearbySequences(final String key) {
		checkArgument(isNotBlank(key), "Uninitialized or invalid key");
		LOGGER.trace("Key: " + key);
		final Iterator<String> it = Splitter.on(SEPARATOR)
				.trimResults()
				.split(key).iterator();
		// longitude
		checkState(it.hasNext(), "Invalid key");		
		final double longitude = doubleValue(it.next());
		// latitude
		checkState(it.hasNext(), "Invalid key");
		final double latitude = doubleValue(it.next());
		// max distance
		checkState(it.hasNext(), "Invalid key");
		final double maxDistance = doubleValue(it.next());
		// group
		checkState(it.hasNext(), "Invalid key");
		final boolean group = booleanValue(it.next());
		// heatmap
		checkState(it.hasNext(), "Invalid key");
		final boolean heatmap = booleanValue(it.next());
		// get from database
		final List<Sequence> sequences = SEQUENCE_DAO.getNear(Point.builder()
				.coordinates(LngLatAlt.builder().coordinates(longitude, latitude).build()).build(), maxDistance);
		// transform to improve visualization
		return fromNullable(toFeatureCollection(sequences, Crs.builder().wgs84().build(), group, heatmap));
	}

	private static String key(final Point point, final double maxDistance, final boolean group, final boolean heatmap) {
		return key(point) + SEPARATOR + key(maxDistance) + SEPARATOR + (group ? "t" : "f") + SEPARATOR + (heatmap ? "t" : "f"); 
	}

	private static String key(final Point point) {
		checkArgument(point != null, "Uninitialized or invalid point");
		checkArgument(point.getCoordinates() != null, "Uninitialized or invalid coordinates");
		final LngLatAlt coord = point.getCoordinates();
		return key(coord.getLongitude()) + SEPARATOR + key(coord.getLatitude());
	}

	private static String key(final double value) {
		return value != Double.NaN ? DECIMAL_FORMAT.format(value) : "nan";
	}

	private static double doubleValue(final String str) {
		return !"nan".equals(str) ? valueOf(str) : Double.NaN;
	}

	private static boolean booleanValue(final String str) {
		return "t".equals(str) ? true : false;
	}

}