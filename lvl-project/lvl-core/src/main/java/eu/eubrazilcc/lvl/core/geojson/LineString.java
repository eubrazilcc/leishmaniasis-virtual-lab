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

package eu.eubrazilcc.lvl.core.geojson;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores geospatial locations in GeoJSON format. For type "LineString", the "coordinates" member must be 
 * an array of two or more positions.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class LineString extends Geometry<LngLatAlt> {	
	
	@Override
	public String toString() {
		return toStringHelper(this)
				.add("geometry", super.toString())
				.toString();
	}
	
	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private LineString instance = new LineString();

		public Builder coordinates(final LngLatAlt... coordinates) {			
			return coordinates(newArrayList(coordinates));
		}
		
		public Builder coordinates(final List<LngLatAlt> coordinates) {
			instance.setCoordinates(coordinates != null ? coordinates : new ArrayList<LngLatAlt>());
			return this;
		}

		public LineString build() {
			final List<LngLatAlt> coordinates = instance.getCoordinates();
			checkState(coordinates != null && coordinates.size() >= 2, "Lines need at least two coordinate pairs");
			return instance;
		}

	}
	
}