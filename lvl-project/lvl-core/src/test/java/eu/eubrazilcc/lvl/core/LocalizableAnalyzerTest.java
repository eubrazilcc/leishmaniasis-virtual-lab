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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer.DEFAULT_ERROR;
import static eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer.aveWeights;
import static eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer.realoc4Heatmap;
import static eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer.relWeights;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.distance;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.splitIds;
import static java.lang.Integer.valueOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer;
import eu.eubrazilcc.lvl.core.geojson.Feature;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Tests {@link SequenceAnalyzer} class.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LocalizableAnalyzerTest {

	@Test
	public void test() {
		System.out.println("SequenceAnalyzerTest.test()");
		try {
			// create test dataset (minimum distance (excluding 0 distance): 0.09504164755344391 meters, between points 9 and 10,
			// which is bellow the error margin of 1/10 meters; maximum distance: 5048619.25141461 meters, between points 2 and 8)
			final Point[] points = {
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0d,     0.0d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0001d,  0.0d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0001d, -0.0001d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0d,     0.0001d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0001d,  0.0001d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0001d, -0.0001d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(-0.0001d, 0.0001d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0d,    -0.0001d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(-1.2d,    45.34d).build()).build(),
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0001d, -0.000101d).build()).build(), // is excluded in exact matching
					Point.builder().coordinates(LngLatAlt.builder().coordinates(0.0001d, -0.0001d).build()).build()
			};
			final List<Sequence> sequences = newArrayList();
			for (int i = 0; i < points.length; i++) {
				sequences.add(Sequence.builder()
						.dataSource(GENBANK)
						.accession("SEQ_" + i)
						.location(points[i])
						.build());
			}

			// calculate distances between points
			final double[][] distMeters = new double[points.length][points.length];
			for (int i = 0; i < points.length; i++) {
				for (int j = i; j < points.length; j++) {
					distMeters[i][j] = distance(points[i], points[j]) * 1000.0d;
					if (i != j) {
						distMeters[j][i] = distMeters[i][j];
					}
				}
			}

			// test group sequences by location with margin error
			LocalizableAnalyzer<Sequence> clusterer = LocalizableAnalyzer.of(sequences);
			double errorMeters = DEFAULT_ERROR;
			List<Feature> features = clusterer.groupByLocation(errorMeters);
			assertThat("features is not null", features, notNullValue());
			assertThat("features is not empty", features.isEmpty(), equalTo(false));
			assertThat("number of features coincides with expected", features.size(), equalTo(8));
			for (final Feature feature : features) {				
				validate(feature, distMeters, errorMeters);
				/* uncomment for additional output */
				System.out.println(" >> feature name: " + feature.getProperty("name") + ", location: " + feature.getGeometry());
			}

			// test group sequences by location with exact location matching
			clusterer = LocalizableAnalyzer.of(sequences);
			errorMeters = 0.0d;
			features = clusterer.groupByLocation(errorMeters);
			assertThat("features (exact match) is not null", features, notNullValue());
			assertThat("features (exact match) is not empty", features.isEmpty(), equalTo(false));
			assertThat("number of features coincides with expected", features.size(), equalTo(9));
			for (final Feature feature : features) {
				validate(feature, distMeters, errorMeters);
				/* uncomment for additional output */
				System.out.println(" >> feature name (exact match): " + feature.getProperty("name") + ", location: " + feature.getGeometry());
			}

			// test average weight calculation
			aveWeights(features);
			assertThat("features is not null after weight calculation", features, notNullValue());
			assertThat("number of features after weight calculation coincides with expected", features.size(), equalTo(9));
			/* uncomment for additional output */
			for (final Feature feature : features) {								
				System.out.println(" >> feature name: " + feature.getProperty("name") + ", average weigth: " + feature.getProperty("weight"));
			}			

			// test relative weight calculation
			relWeights(features);
			assertThat("features is not null after weight calculation", features, notNullValue());
			assertThat("number of features after weight calculation coincides with expected", features.size(), equalTo(9));
			/* uncomment for additional output */
			for (final Feature feature : features) {								
				System.out.println(" >> feature name: " + feature.getProperty("name") + ", relative weigth: " + feature.getProperty("weight"));
			}

			// test feature reallocation
			final List<Feature> features2 = realoc4Heatmap(features);
			assertThat("features is not null after reallocation", features2, notNullValue());
			assertThat("number of features after reallocation coincides with expected", features2.size(), equalTo(11));			

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SequenceAnalyzerTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SequenceAnalyzerTest.test() has finished");
		}
	}

	private static void validate(final Feature feature, final double[][] distMeters, final double errorMeters) {
		final List<Integer> ids = extractIds(feature.getProperty("name").toString());
		assertThat("feature count property coincides with expected", (Integer)feature.getProperty("count"), equalTo(ids.size()));
		for (int i = 0; i < ids.size(); i++) {
			for (int j = 0; j < ids.size(); j++) {
				final int seqI = ids.get(i), seqJ = ids.get(j);
				final double dist = distMeters[seqI][seqJ];
				assertThat("computed distances coincide with expected dist(" + seqI + ", " + seqJ + ")", 
						dist, closeTo(0.0d, errorMeters));
			}
		}
	}

	private static List<Integer> extractIds(final String name) {
		return from(splitIds(name)).transform(new Function<String, Integer>() {
			@Override
			public Integer apply(final String id) {
				return valueOf(id.substring(id.indexOf(ID_FRAGMENT_SEPARATOR) + 1 + "SEQ_".length()));
			}
		}).filter(notNull()).toList();
	}

}