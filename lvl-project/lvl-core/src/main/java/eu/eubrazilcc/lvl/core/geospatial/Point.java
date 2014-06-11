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

package eu.eubrazilcc.lvl.core.geospatial;

import static com.google.common.base.Preconditions.checkState;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Validator.checkLatitude;
import static eu.eubrazilcc.lvl.core.geospatial.Wgs84Validator.checkLongitude;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * Stores geospatial locations in GeoJSON format.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
@XmlRootElement(name = "point")
public class Point implements Geometry {

	public static final String POINT = "Point";

	private String type;
	private double[] coordinates;

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public double[] getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(final double[] coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Point)) {
			return false;
		}
		final Point other = Point.class.cast(obj);
		return Objects.equal(type, other.type) 
				&& Arrays.equals(coordinates, other.coordinates);		
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type) + Arrays.hashCode(coordinates);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("type", type)
				.add("coordinates", Arrays.toString(coordinates))				
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		final Point location = new Point();

		public Builder() {
			location.setType(POINT);
		}

		/**
		 * Sets the coordinate of this point.
		 * @param longitude - longitude in WGS84 coordinate reference system (CRS)
		 * @param latitude - latitude in WGS84 coordinate reference system (CRS)
		 * @return a reference to this point
		 */
		public Builder coordinate(final double longitude, final double latitude) {
			location.setCoordinates(new double[]{ checkLongitude(longitude), checkLatitude(latitude) });
			return this;
		}

		public Point build() {
			checkState(location.coordinates != null && location.coordinates.length == 2, 
					"No coordinates found");
			return location;
		}

	}

}