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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Arrays;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.common.base.Objects;

import eu.eubrazilcc.lvl.core.xml.GeometryAdapter;

/**
 * Contains a collection of objects with known geospatial location.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class FeatureCollection {

	public static final String FEATURE_COLLECTION = "FeatureCollection";
	public static final String FEATURE = "Feature";
	public static final String NAME = "name";

	private String type;
	private Crs crs;
	private Feature[] features;	

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Crs getCrs() {
		return crs;
	}

	public void setCrs(final Crs crs) {
		this.crs = crs;
	}

	public Feature[] getFeatures() {
		return features;
	}

	public void setFeatures(final Feature[] features) {
		this.features = features;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof FeatureCollection)) {
			return false;
		}
		final FeatureCollection other = FeatureCollection.class.cast(obj);
		return Objects.equal(type, other.type)
				&& Objects.equal(crs, other.crs)
				&& Arrays.equals(features, other.features);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type, crs, features);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("type", type)
				.add("crs", crs)
				.add("features", features != null ? Arrays.toString(features) : null)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		final FeatureCollection collection = new FeatureCollection();
		private Set<Object> featuresSet = newLinkedHashSet();

		public Builder() {
			collection.setType(FEATURE_COLLECTION);			
		}

		public Builder crs(final String crs) {
			collection.setCrs(new Crs());
			collection.getCrs().setType(NAME);
			collection.getCrs().setProperties(new Properties());
			collection.getCrs().getProperties().setName(crs);
			return this;
		}

		public Builder wgs84() {
			return crs(Wgs84Validator.LEGACY_CRS);
		}

		public Builder feature(final Geometry geometry) {
			checkArgument(geometry != null && (geometry instanceof Point 
					|| geometry instanceof Line || geometry instanceof Polygon),
					"Uninitalized or invalid geometry");
			final Feature feature = new Feature();
			feature.setType(FEATURE);
			feature.setGeometry(geometry);
			featuresSet.add(feature);
			return this;
		}

		public FeatureCollection build() {
			collection.setFeatures(featuresSet.toArray(new Feature[featuresSet.size()]));
			return collection;
		}

	}

	/* Inner classes */

	/**
	 * Coordinate reference system (CRS).
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class Crs {		

		private String type;
		private Properties properties;

		public String getType() {
			return type;
		}

		public void setType(final String type) {
			this.type = type;
		}

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(final Properties properties) {
			this.properties = properties;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof Crs)) {
				return false;
			}
			final Crs other = Crs.class.cast(obj);
			return Objects.equal(type, other.type)
					&& Objects.equal(properties, other.properties);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(type, properties);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("type", type)
					.add("properties", properties)
					.toString();
		}

	}

	/**
	 * Properties.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class Properties {

		private String name;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof Properties)) {
				return false;
			}
			final Properties other = Properties.class.cast(obj);
			return Objects.equal(name, other.name);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(name);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("name", name)
					.toString();
		}

	}

	/**
	 * An object with known geospatial location.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class Feature {

		private String type;

		@XmlJavaTypeAdapter(GeometryAdapter.class)
		private Geometry geometry;

		public String getType() {
			return type;
		}

		public void setType(final String type) {
			this.type = type;
		}

		public Object getGeometry() {
			return geometry;
		}

		public void setGeometry(final Geometry geometry) {
			this.geometry = geometry;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof Feature)) {
				return false;
			}
			final Feature other = Feature.class.cast(obj);
			return Objects.equal(type, other.type)
					&& Objects.equal(geometry, other.geometry);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(type, geometry);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("type", type)
					.add("geometry", geometry)
					.toString();
		}

	}

}