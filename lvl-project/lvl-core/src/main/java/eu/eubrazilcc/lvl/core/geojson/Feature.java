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

import java.util.Map;

import com.google.common.base.Objects;

/**
 * Stores geospatial locations in GeoJSON format.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class Feature extends GeoJsonObject {

	private GeoJsonObject geometry;
	private String id;

	public GeoJsonObject getGeometry() {
		return geometry;
	}
	public void setGeometry(final GeoJsonObject geometry) {
		this.geometry = geometry;
	}
	public String getId() {
		return id;
	}
	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Feature)) {
			return false;
		}
		final Feature other = Feature.class.cast(obj);
		return super.equals(obj)
				&& Objects.equal(geometry, other.geometry)
				&& Objects.equal(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), geometry, id);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("geojson_obj", super.toString())
				.add("geometry", geometry)
				.add("id", id)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private Feature instance = new Feature();

		public Builder geometry(final GeoJsonObject geometry) {
			instance.setGeometry(geometry);
			return this;
		}

		public Builder properties(final Map<String, Object> properties) {
			instance.getProperties().putAll(properties);
			return this;
		}

		public Builder property(final String key, final Object value) {
			instance.getProperties().put(key, value);
			return this;
		}

		public Builder id(final String id) {
			instance.setId(id);
			return this;
		}

		public Feature build() {		
			return instance;
		}

	}

}