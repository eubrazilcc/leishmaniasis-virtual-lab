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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

/**
 * Stores the key of a sample.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SampleKey {

	private String collectionId;
	private String catalogNumber;

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(final String collectionId) {
		this.collectionId = collectionId;
	}

	public String getCatalogNumber() {
		return catalogNumber;
	}

	public void setCatalogNumber(final String catalogNumber) {
		this.catalogNumber = catalogNumber;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SampleKey)) {
			return false;
		}
		final SampleKey other = SampleKey.class.cast(obj);
		return Objects.equals(collectionId, other.collectionId)
				&& Objects.equals(catalogNumber, other.catalogNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(collectionId, catalogNumber);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("collectionId", collectionId)
				.add("catalogNumber", catalogNumber)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SampleKey instance = new SampleKey();

		public Builder collectionId(final String collectionId) {
			instance.setCollectionId(collectionId);
			return this;
		}

		public Builder catalogNumber(final String catalogNumber) {
			instance.setCatalogNumber(catalogNumber);
			return this;
		}

		public SampleKey build() {
			return instance;
		}

	}

}