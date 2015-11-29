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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toId;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;

/**
 * Stores a sample collected and maintained in a collection.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Sample implements Localizable<Point> {

	private String id;                 // Resource identifier

	private String collectionId;       // Collection where the original sample is stored
	private String catalogNumber;      // The number of the sample in the collection catalog
	private Point location;            // Geospatial location
	private Locale locale;             // Represents country with standards

	private SimpleDarwinRecord sample; // Original sample in DWC format

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(final String collectionId) {
		this.collectionId = collectionId;
		updateId();
	}	

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public void setCatalogNumber(final String catalogNumber) {
		this.catalogNumber = catalogNumber;
		updateId();
	}

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public void setLocation(final Point location) {
		this.location = location;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	public SimpleDarwinRecord getSample() {
		return sample;
	}

	public void setSample(final SimpleDarwinRecord sample) {
		this.sample = sample;
	}

	@JsonIgnore
	@Override
	public String getTag() {
		return toId(this, NOTATION_SHORT);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Sample)) {
			return false;
		}
		final Sample other = Sample.class.cast(obj);
		return Objects.equals(id, other.id)
				&& Objects.equals(collectionId, other.collectionId)
				&& Objects.equals(catalogNumber, other.catalogNumber)
				&& Objects.equals(location, other.location)
				&& Objects.equals(locale, other.locale);
		// sequence
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, collectionId, catalogNumber, location, locale);
	}	

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("id", id)
				.add("collectionId", collectionId)
				.add("catalogNumber", catalogNumber)				
				.add("location", location)
				.add("locale", locale)
				.add("sample", "<<original sample is not displayed>>")
				.toString();
	}

	private void updateId() {
		id = collectionId != null && isNotBlank(catalogNumber) ? toId(collectionId, catalogNumber, NOTATION_SHORT) : null;
	}

	/* Fluent API */

	public static class Builder<T extends Sample> {

		protected final T instance;

		public Builder(final Class<T> clazz) {
			T tmp = null;
			try {
				tmp = clazz.newInstance();
			} catch (Exception ignore) { }
			instance = tmp;
		}

		public Builder<T> collectionId(final String collectionId) {
			instance.setCollectionId(collectionId);
			return this;
		}

		public Builder<T> catalogNumber(final String catalogNumber) {
			instance.setCatalogNumber(catalogNumber);
			return this;
		}

		public Builder<T> location(final Point location) {
			instance.setLocation(location);
			return this;
		}

		public Builder<T> locale(final Locale locale) {
			instance.setLocale(locale);
			return this;
		}

		public Builder<T> sample(final SimpleDarwinRecord sample) {
			instance.setSample(sample);
			return this;
		}

		public T build() {
			return instance;
		}

	}

}