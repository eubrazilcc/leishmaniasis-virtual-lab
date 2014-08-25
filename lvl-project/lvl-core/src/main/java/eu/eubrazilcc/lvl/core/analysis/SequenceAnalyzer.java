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

package eu.eubrazilcc.lvl.core.analysis;

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

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.Feature;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Processes a collection of {@link Sequence} items and analyzes them using different criteria such as location.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceAnalyzer {

	/**
	 * Default error margin (in meters) allowed by this class in distance computations: 1/10 meters.
	 */
	public static double DEFAULT_ERROR = 0.1d;

	private final ImmutableList<Sequence> sequences;

	public SequenceAnalyzer(final Collection<Sequence> sequences) {
		if (sequences != null) {
			this.sequences = ImmutableList.copyOf(sequences);
		} else {
			this.sequences = ImmutableList.of();
		}
	}

	/**
	 * Wrapper method to {@link SequenceAnalyzer#groupByLocation(double)} that uses the default error margin 
	 * {@link SequenceAnalyzer#DEFAULT_ERROR}.
	 * @return a list of {@link Feature} that contains the clusters computed from the original dataset. Each
	 *         {@link Feature} is centered in the geospatial center of the cluster and the name of the feature
	 *         contains the accession numbers of all the {@link Feature} that belong to the cluster.
	 */
	public List<Feature> groupByLocation() {
		return groupByLocation(DEFAULT_ERROR);
	}

	/**
	 * Analyzes a collection of {@link Sequence} items and uses their locations to group the possible repetitions.
	 * The sequences that are found to have the same location are grouped in a single {@link Feature}. Each feature
	 * is annotated with the property {@code name}, which will contain the identifiers of all the sequences grouped 
	 * in the feature. Also, the property {@code count} will contain the number of sequences grouped in the feature.
	 * @param error - the error margin (in meters) allowed in distance calculations (the endpoint is also included 
	 *        in the interval)
	 * @return a list of {@link Feature} that contains the features computed from the original dataset. Each feature
	 *         is centered in the geospatial center of the locations that contains, the {@code name} property of the 
	 *         feature contains the identifiers of all the {@link Sequence} that belong to the feature and the 
	 *         {@code count} property contains the number of sequences.
	 */
	public List<Feature> groupByLocation(final double error) {
		// filter out the sequences without a valid location
		final List<Sequence> filtered = from(sequences).transform(new Function<Sequence, Sequence>() {
			@Override
			public Sequence apply(final Sequence sequence) {
				return sequence != null && sequence.getLocation() != null ? sequence : null;
			}			
		}).filter(notNull()).toList();
		// create a disjoint set and process the sequences		
		final DisjointSet<Sequence> disjointSet = DisjointSet.of(filtered);
		final boolean[] visited = new boolean[filtered.size()];
		for (int i = 0; i < filtered.size(); i++) {			
			if (!visited[i]) {
				final Sequence seqI = filtered.get(i);
				for (int j = 0; j < filtered.size(); j++) {
					final Sequence seqJ = filtered.get(j);					
					if (!visited[j] && !seqI.equals(seqJ) && distance(seqI.getLocation(), seqJ.getLocation()) * 1000.0d <= error) {
						disjointSet.merge(seqI, seqJ);
						visited[j] = true;
					}
				}
			}
		}
		// process the sets and create the list of features
		final List<Sequence> rootSequences = disjointSet.rootElements();
		return from(rootSequences).transform(new Function<Sequence, Feature>() {
			@Override
			public Feature apply(final Sequence rootSequence) {
				final List<Sequence> sequences = disjointSet.getElementsInSet(rootSequence);
				if (sequences != null && !sequences.isEmpty()) {
					return Feature.builder()
							.property("name", mergeIds(sequences))
							.property("count", sequences.size())
							.geometry(geographicCenter(getLocations(sequences)))
							.build();
				}
				return null;
			}
		}).filter(notNull()).toList();
	}

	private List<Point> getLocations(final List<Sequence> sequences) {
		return from(sequences).transform(new Function<Sequence, Point>() {
			@Override
			public Point apply(final Sequence sequence) {
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

	/* Fluent API */

	public static SequenceAnalyzer of(final Collection<Sequence> sequences) {
		return new SequenceAnalyzer(sequences);
	}

}