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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;

import java.util.List;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Processes a collection of {@link Sequence} items and analyzes them using different criteria such as geospatial location.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceAnalyzer {

	/**
	 * Wrapper method to {@link LocalizableAnalyzer#toFeatureCollection(List, Crs, boolean, boolean)} that provides a specialized version
	 * to handle a collection of sequences as input.
	 * @param sequences - sequences to convert to features
	 * @param crs - coordinate reference system (CRS) of the input items
	 * @param group - setting this to {@code true} will cause that all the items that share the same location will be grouped in a single feature
	 * @param heatmap - setting this to {@code true} will cause that the groups of items that share the same location will be reorganized for 
	 *                  better display in a map (items visualization in a heat-maps will improve considerably)
	 * @return the input items prepared for export and display using GeoJSON format.
	 */
	public static FeatureCollection toFeatureCollection(final List<Sequence> sequences, final Crs crs, 
			final boolean group, final boolean heatmap) {
		checkArgument(sequences != null, "Uninitialized list of sequences");
		return LocalizableAnalyzer.toFeatureCollection(from(sequences).transform(new Function<Sequence, Localizable<Point>>() {
			@Override
			public Localizable<Point> apply(final Sequence sequence) {
				return sequence;
			}			
		}).filter(notNull()).toList(), crs, group, heatmap);		
	}

}