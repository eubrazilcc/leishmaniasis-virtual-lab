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

package eu.eubrazilcc.lvl.service.cache;

import static eu.eubrazilcc.lvl.core.concurrent.TaskScheduler.TASK_SCHEDULER;
import static eu.eubrazilcc.lvl.service.cache.GeolocationCache.findNearbyLeishmaniaSamples;
import static eu.eubrazilcc.lvl.service.cache.GeolocationCache.findNearbyLeishmaniaSequences;
import static eu.eubrazilcc.lvl.service.cache.GeolocationCache.findNearbySandflySamples;
import static eu.eubrazilcc.lvl.service.cache.GeolocationCache.findNearbySandflySequences;
import static eu.eubrazilcc.lvl.service.cache.StatisticsCache.leishmaniaStats;
import static eu.eubrazilcc.lvl.service.cache.StatisticsCache.sandflyStats;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.rest.jackson.MapperProvider;

/**
 * Warms up (or pre-populates) a cache with record updates.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum CacheWarmer implements Closeable2 {

	CACHE_WARMER;

	private static final Logger LOGGER = getLogger(CacheWarmer.class);

	private static final String BASE_URI = "http://localhost/lvl-service/rest/v1";

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		// create client
		final Client client = ClientBuilder.newBuilder()
				.register(MapperProvider.class)
				.register(JacksonFeature.class)
				.register(SseFeature.class)
				.register(MultiPartFeature.class)
				.build();
		final WebTarget target = client.target(BASE_URI);
		target.property(ClientProperties.FOLLOW_REDIRECTS, true);
		// schedule periodic updates
		TASK_SCHEDULER.scheduleAtFixedRate(new Runnable() {			
			@Override
			public void run() {
				// sandflies global vector and heat maps, collection statistics
				findNearbySandflySequences(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, false);
				findNearbySandflySequences(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, true);
				sandflyStats();
				findNearbySandflySamples(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, false);
				findNearbySandflySamples(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, true);
				// leishmania global vector and heat maps, collection statistics
				findNearbyLeishmaniaSequences(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, false);
				findNearbyLeishmaniaSequences(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, true);
				leishmaniaStats();				
				findNearbyLeishmaniaSamples(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, false);
				findNearbyLeishmaniaSamples(Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0, 0.0).build()).build(), 
						6500000.0, true, true);				
			}
		}, 30l, 3600l, TimeUnit.SECONDS); // repeat every 1 hour		
		LOGGER.info("Cache warmer initialized successfully");
	}

	@Override
	public void close() throws IOException {
		LOGGER.info("Cache warmer shutdown successfully");
	}

}