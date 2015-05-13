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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Stores geospatial locations in GeoJSON format. A GeoJSON object with the type "FeatureCollection" 
 * is a feature collection object.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class FeatureCollection extends GeoJsonObject implements Iterable<Feature> {

	private List<Feature> features = newArrayList();

	public List<Feature> getFeatures() {
		return features;
	}
	public void setFeatures(final List<Feature> features) {
		this.features = features;
	}

	public FeatureCollection add(final Feature feature) {
		features.add(feature);
		return this;
	}

	public void addAll(final Collection<Feature> features) {
		this.features.addAll(features);
	}

	@Override
	public Iterator<Feature> iterator() {
		return features.iterator();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof FeatureCollection)) {
			return false;
		}
		final FeatureCollection other = FeatureCollection.class.cast(obj);
		return super.equals(obj)
				&& Objects.equals(features, other.features);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(features);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("geojson_obj", super.toString())
				.add("features", features != null ? collectionToString(features) : null)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final FeatureCollection instance = new FeatureCollection();

		public Builder crs(final Crs crs) {
			instance.setCrs(crs);
			return this;
		}

		public Builder features(final Feature... features) {
			return features(newArrayList(features));
		}

		public Builder features(final List<Feature> features) {
			instance.setFeatures(features != null ? features : new ArrayList<Feature>());
			return this;
		}

		public FeatureCollection build() {
			return instance;
		}

	}

}