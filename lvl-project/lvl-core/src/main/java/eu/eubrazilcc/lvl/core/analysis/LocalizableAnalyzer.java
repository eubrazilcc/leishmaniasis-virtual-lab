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

package eu.eubrazilcc.lvl.core.analysis;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.distance;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.geographicCenter;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Calculator.matrixPoints;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.mergeIds;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.splitIds;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.Feature;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Processes a collection of {@link Localizable} items and analyzes them using the geospatial location.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LocalizableAnalyzer<T extends Localizable<Point>> {

	/**
	 * Default error margin (in meters) allowed by this class in distance computations: 1/10 meters.
	 */
	public static double DEFAULT_ERROR = 0.1d;

	private final ImmutableList<T> items;

	public LocalizableAnalyzer(final Collection<T> items) {
		if (items != null) {
			this.items = ImmutableList.copyOf(items);
		} else {
			this.items = ImmutableList.of();
		}
	}

	/**
	 * Wrapper method to {@link LocalizableAnalyzer#groupByLocation(double)} that uses the default error margin 
	 * {@link LocalizableAnalyzer#DEFAULT_ERROR}.
	 * @return a list of {@link Feature} that contains the clusters computed from the original dataset. Each
	 *         {@link Feature} is centered in the geospatial center of the cluster and the name of the feature
	 *         contains the accession numbers of all the {@link Feature} that belong to the cluster.
	 */
	public List<Feature> groupByLocation() {
		return groupByLocation(DEFAULT_ERROR);
	}

	/**
	 * Analyzes a collection of {@link Localizable} items and uses their locations to group the possible repetitions.
	 * The items that are found to have the same location are grouped in a single {@link Feature}. Each feature
	 * is annotated with the property {@code name}, which will contain the identifiers of all the items grouped 
	 * in the feature. Also, the property {@code count} will contain the number of items grouped in the feature.
	 * @param error - the error margin (in meters) allowed in distance calculations (the endpoint is also included 
	 *        in the interval)
	 * @return a list of {@link Feature} that contains the features computed from the original dataset. Each feature
	 *         is centered in the geospatial center of the locations that contains, the {@code name} property of the 
	 *         feature contains the identifiers of all the {@link Localizable} that belong to the feature and the 
	 *         {@code count} property contains the number of items.
	 */
	public List<Feature> groupByLocation(final double error) {
		// filter out the items without a valid location
		final List<Localizable<Point>> filtered = from(items).transform(new Function<Localizable<Point>, Localizable<Point>>() {
			@Override
			public Localizable<Point> apply(final Localizable<Point> sequence) {
				return sequence != null && sequence.getLocation() != null ? sequence : null;
			}			
		}).filter(notNull()).toList();
		if (filtered.isEmpty()) {
			return newArrayList();
		}		
		// create a disjoint set and process the items		
		final DisjointSet<Localizable<Point>> disjointSet = DisjointSet.of(filtered);
		final boolean[] visited = new boolean[filtered.size()];
		for (int i = 0; i < filtered.size(); i++) {			
			if (!visited[i]) {
				final Localizable<Point> seqI = filtered.get(i);
				for (int j = 0; j < filtered.size(); j++) {
					final Localizable<Point> seqJ = filtered.get(j);					
					if (!visited[j] && !seqI.equals(seqJ) && distance(seqI.getLocation(), seqJ.getLocation()) * 1000.0d <= error) {
						disjointSet.merge(seqI, seqJ);
						visited[j] = true;
					}
				}
			}
		}
		// process the sets and create the list of features
		final List<Localizable<Point>> rootLocalizablePoints = disjointSet.rootElements();
		return from(rootLocalizablePoints).transform(new Function<Localizable<Point>, Feature>() {
			@Override
			public Feature apply(final Localizable<Point> rootLocalizablePoint) {
				final List<Localizable<Point>> items = disjointSet.getElementsInSet(rootLocalizablePoint);
				if (items != null && !items.isEmpty()) {
					return Feature.builder()
							.property("name", mergeIds(items))
							.property("count", items.size())
							.geometry(geographicCenter(getLocations(items)))
							.build();
				}
				return null;
			}
		}).filter(notNull()).toList();
	}

	private List<Point> getLocations(final List<Localizable<Point>> items) {
		return from(items).transform(new Function<Localizable<Point>, Point>() {
			@Override
			public Point apply(final Localizable<Point> sequence) {
				return sequence.getLocation();
			}
		}).filter(notNull()).toList();
	}

	/**
	 * Calculates the average weight of the specified features and annotates each feature with the property {@code weight}.
	 * @param features - the features to calculate the weights.
	 */
	public static void aveWeights(final List<Feature> features) {
		double total = 0.0d;
		for (final Feature feature : features) {
			total += (int)feature.getProperty("count");
		}
		for (final Feature feature : features) {
			feature.setProperty("weight", ((int)feature.getProperty("count")) / total);
		}
	}

	/**
	 * Calculates the relative weight of the specified features and annotates each feature with the property {@code weight}.
	 * @param features - the features to calculate the weights.
	 */
	public static void relWeights(final List<Feature> features) {
		double max = Double.MIN_VALUE;
		for (final Feature feature : features) {
			final int count = (int)feature.getProperty("count");
			max = Math.max(max, (double)count);
		}
		for (final Feature feature : features) {
			feature.setProperty("weight", ((int)feature.getProperty("count")) / max);
		}
	}

	/**
	 * Reallocates a set of features, distributing each group found in a arithmetic spiral. This can be used for example 
	 * to represent a set of points in a heat-map.
	 * @param features - the features to reallocate.
	 * @return the reallocated features.
	 */
	public static List<Feature> realoc4Heatmap(final List<Feature> features) {
		final List<Feature> features2 = newArrayList();
		for (final Feature feature : features) {
			final int count = (int)feature.getProperty("count");
			if (count > 1) {				
				final List<String> names = splitIds((String)feature.getProperty("name"));
				final Point point = (Point) feature.getGeometry();
				final Point[][] matrix = matrixPoints(names.size(), point);
				int count2 = 0;
				for (int i = 0; i < matrix.length && count2 < count; i++) {
					for (int j = 0; j < matrix[i].length && count2 < count; j++) {						
						features2.add(Feature.builder()
								.property("name", names.get(count2))
								.property("count", 1)
								.geometry(matrix[i][j])
								.build());
						count2++;
					}
				}
			} else {
				features2.add(feature);
			}
		}
		return features2;
	}

	/**
	 * Converts a collection of {@link Localizable} items for export and display using GeoJSON format. Optionally, post-processing methods can 
	 * be applied to improve data visualization with specific map types.
	 * @param items - localizable items to convert to features
	 * @param crs - coordinate reference system (CRS) of the input items
	 * @param group - setting this to {@code true} will cause that all the items that share the same location will be grouped in a single feature
	 * @param heatmap - setting this to {@code true} will cause that the groups of items that share the same location will be reorganized for 
	 *                  better display in a map (items visualization in a heat-maps will improve considerably)
	 * @return the input items prepared for export and display using GeoJSON format.
	 */
	public static FeatureCollection toFeatureCollection(final List<Localizable<Point>> items, final Crs crs, 
			final boolean group, final boolean heatmap) {
		checkArgument(items != null, "Uninitialized list of localizable items");
		checkArgument(crs != null, "Uninitialized coordinate reference system (CRS)");
		List<Feature> features = null;
		if (group) {
			features = of(items).groupByLocation(heatmap ? 1 : DEFAULT_ERROR);
			if (heatmap) {
				features = realoc4Heatmap(features);
			}
		} else {
			features = newArrayList();
			for (final Localizable<Point> item : items) {
				features.add(Feature.builder()
						.property("name", item.getTag())
						.geometry(item.getLocation())
						.build());
			}
		}
		return FeatureCollection.builder().crs(crs).features(features).build();
	}

	/* Fluent API */

	public static <T extends Localizable<Point>> LocalizableAnalyzer<T> of(final Collection<T> items) {
		return new LocalizableAnalyzer<T>(items);
	}

}