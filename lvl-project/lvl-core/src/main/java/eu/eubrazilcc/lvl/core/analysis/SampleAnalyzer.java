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
import static java.util.function.Function.identity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.eubrazilcc.lvl.core.Sample;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;

/**
 * Processes a collection of {@link Sample} items and analyzes them using different criteria such as geospatial location.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SampleAnalyzer {

	/**
	 * Wrapper method to {@link LocalizableAnalyzer#toFeatureCollection(List, Crs, boolean, boolean)} that provides a specialized version
	 * to handle a collection of samples as input.
	 * @param samples - samples to convert to features
	 * @param crs - coordinate reference system (CRS) of the input items
	 * @param group - setting this to {@code true} will cause that all the items that share the same location will be grouped in a single feature
	 * @param heatmap - setting this to {@code true} will cause that the groups of items that share the same location will be reorganized for 
	 *                  better display in a map (items visualization in a heat-maps will improve considerably)
	 * @return the input items prepared for export and display using GeoJSON format.
	 */
	public static <T extends Sample> FeatureCollection sample2FeatCol(final List<T> samples, final Crs crs, 
			final boolean group, final boolean heatmap) {
		checkArgument(samples != null, "Uninitialized list of sequences");
		return LocalizableAnalyzer.toFeatureCollection(samples.stream().map(identity())
				.filter(Objects::nonNull).collect(Collectors.toList()), crs, group, heatmap);		
	}

}