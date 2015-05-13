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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import eu.eubrazilcc.lvl.core.geospatial.Wgs84Validator;

/**
 * Stores the coordinate reference system (CRS) of a GeoJSON object.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class Crs {

	private String type = "name";
	private Map<String, Object> properties = new HashMap<String, Object>();

	public String getType() {
		return type;
	}
	public void setType(final String type) {
		this.type = type;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Crs)) {
			return false;
		}
		final Crs other = Crs.class.cast(obj);
		return Objects.equals(type, other.type)
				&& Objects.equals(properties, other.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, properties);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("type", type)
				.add("properties", properties)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Crs instance = new Crs();

		public Builder wgs84() {
			instance.getProperties().put("name", Wgs84Validator.URN_CRS);
			return this;
		}

		public Builder type(final String type) {
			instance.setType(type);
			return this;
		}

		public Builder properties(final Map<String, Object> properties) {
			instance.setProperties(properties);
			return this;
		}

		public Crs build() {
			return instance;
		}

	}

}