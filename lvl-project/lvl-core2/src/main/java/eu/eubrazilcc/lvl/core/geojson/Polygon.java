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

package eu.eubrazilcc.lvl.core.geojson;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Stores geospatial locations in GeoJSON format. For type "Polygon", the "coordinates" member must be 
 * an array of LinearRing coordinate arrays. For Polygons with multiple rings, the first must be the 
 * exterior ring and any others must be interior rings or holes.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class Polygon extends Geometry<List<LngLatAlt>> {

	@JsonIgnore
	public List<LngLatAlt> getExteriorRing() {
		checkState(coordinates != null && !coordinates.isEmpty(), "No exterior ring defined");
		return coordinates.get(0);
	}

	@JsonIgnore
	public List<List<LngLatAlt>> getInteriorRings() {
		checkState(coordinates != null && !coordinates.isEmpty(), "No exterior ring defined");
		return coordinates.subList(1, coordinates.size());
	}

	@JsonIgnore
	public List<LngLatAlt> getInteriorRing(final int index) {
		checkState(coordinates != null && !coordinates.isEmpty(), "No exterior ring defined");
		return coordinates.get(1 + index);
	}

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

		private Polygon instance = new Polygon();

		public Builder exteriorRing(final LngLatAlt... coordinates) {
			return exteriorRing(newArrayList(coordinates));
		}

		public Builder exteriorRing(final List<LngLatAlt> coordinates) {
			instance.getCoordinates().add(0, coordinates);
			return this;
		}

		public Builder interiorRing(final LngLatAlt... coordinates) {
			return interiorRing(newArrayList(coordinates));
		}

		public Builder interiorRing(final List<LngLatAlt> coordinates) {
			checkState(coordinates != null && !coordinates.isEmpty(), "No exterior ring defined");
			instance.getCoordinates().add(coordinates);
			return this;
		}

		public Polygon build() {
			final List<List<LngLatAlt>> coordinates = instance.getCoordinates();
			checkState(coordinates != null && !coordinates.isEmpty(), "No exterior ring defined");
			final List<LngLatAlt> exteriorRing = coordinates.get(0);
			checkState(exteriorRing.size() >= 4, "Exterior ring needs at least four coordinate pairs");
			checkState(exteriorRing.get(0).equals(exteriorRing.get(exteriorRing.size() - 1)), 
					"The same position must be specified as the first and last coordinates");
			return instance;
		}

	}

}