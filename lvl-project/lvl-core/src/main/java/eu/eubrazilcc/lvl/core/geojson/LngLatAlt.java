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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;

import eu.eubrazilcc.lvl.core.geojson.jackson.LngLatAltDeserializer;
import eu.eubrazilcc.lvl.core.geojson.jackson.LngLatAltSerializer;

/**
 * Stores geospatial locations in GeoJSON format.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
@JsonDeserialize(using = LngLatAltDeserializer.class)
@JsonSerialize(using = LngLatAltSerializer.class)
public class LngLatAlt {

	private double longitude;
	private double latitude;
	private double altitude = Double.NaN;

	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(final double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(final double latitude) {
		this.latitude = latitude;
	}
	public double getAltitude() {
		return altitude;
	}
	public void setAltitude(final double altitude) {
		this.altitude = altitude;
	}

	public boolean hasAltitude() {
		return !Double.isNaN(altitude);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof LngLatAlt)) {
			return false;
		}
		final LngLatAlt other = LngLatAlt.class.cast(obj);
		return Objects.equal(longitude, other.longitude)
				&& Objects.equal(latitude, other.latitude)
				&& Objects.equal(altitude, other.altitude);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(longitude, latitude, altitude);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("longitude", longitude)
				.add("latitude", latitude)
				.add("altitude", altitude)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private LngLatAlt instance = new LngLatAlt();

		public Builder coordinates(final double longitude, final double latitude) {
			return longitude(longitude).latitude(latitude);
		}

		public Builder coordinates(final double longitude, final double latitude, final double altitude) {
			return longitude(longitude).latitude(latitude).altitude(altitude);
		}

		public Builder longitude(final double longitude) {
			instance.setLongitude(longitude);
			return this;
		}

		public Builder latitude(final double latitude) {
			instance.setLatitude(latitude);
			return this;
		}

		public Builder altitude(final double altitude) {
			instance.setAltitude(altitude);
			return this;
		}

		public LngLatAlt build() {
			return instance;
		}

	}

}