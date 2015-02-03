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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Stores geospatial locations in GeoJSON format. Only a subset of the GeoJSON is supported in this version.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
@JsonTypeInfo(property = "type", use = Id.NAME)
@JsonSubTypes({ @Type(Point.class), @Type(LineString.class), @Type(Polygon.class), 
	@Type(Feature.class), @Type(FeatureCollection.class) })
@JsonInclude(Include.NON_NULL)
public abstract class GeoJsonObject {

	private Crs crs;
	private double[] bbox;
	@JsonInclude(Include.NON_EMPTY)
	private Map<String, Object> properties = new HashMap<String, Object>();

	public Crs getCrs() {
		return crs;
	}
	public void setCrs(final Crs crs) {
		this.crs = crs;
	}
	public double[] getBbox() {
		return bbox;
	}
	public void setBbox(final double[] bbox) {
		this.bbox = bbox;
	}
	public Map<String, Object> getProperties() {
		return properties;
	}
	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}

	public void setProperty(final String key, final Object value) {
		properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T getProperty(final String key) {
		return (T) properties.get(key);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof GeoJsonObject)) {
			return false;
		}
		final GeoJsonObject other = GeoJsonObject.class.cast(obj);
		return Objects.equals(crs, other.crs)
				&& Objects.equals(bbox, other.bbox)
				&& Objects.equals(properties, other.properties);		
	}

	@Override
	public int hashCode() {
		return Objects.hash(crs, bbox, properties);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("crs", crs)
				.add("bbox", bbox != null ? Arrays.toString(bbox) : null)
				.add("properties", properties)
				.toString();
	}

}