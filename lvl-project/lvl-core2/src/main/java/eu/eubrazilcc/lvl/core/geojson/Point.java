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

import java.util.Objects;

/**
 * Stores geospatial locations in GeoJSON format. For type "Point", the "coordinates" member must be 
 * a single position.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class Point extends GeoJsonObject {

	private LngLatAlt coordinates;

	public LngLatAlt getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(final LngLatAlt coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Point)) {
			return false;
		}
		final Point other = Point.class.cast(obj);
		return super.equals(obj)
				&& Objects.equals(coordinates, other.coordinates);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(coordinates);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("geojson_obj", super.toString())
				.add("coordinates", coordinates)
				.toString();
	}	

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Point instance = new Point();

		public Builder crs(final Crs crs) {
			instance.setCrs(crs);
			return this;
		}

		public Builder coordinates(final LngLatAlt coordinates) {
			instance.setCoordinates(coordinates);
			return this;
		}

		public Point build() {		
			checkState(instance.getCoordinates() != null, "No coordinates found");
			return instance;
		}

	}

}