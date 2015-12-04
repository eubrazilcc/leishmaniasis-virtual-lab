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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Splitter.on;
import static eu.eubrazilcc.lvl.core.DataSource.toLongNotation;
import static eu.eubrazilcc.lvl.core.DataSource.toShortNotation;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.escapeUrlPathSegment;

import java.util.List;
import java.util.Objects;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.DataSource.Notation;
import eu.eubrazilcc.lvl.core.util.NamingUtils;

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
	
	public String toId() {
		return toId(false);
	}
	
	/**
	 * Creates an identifier that uniquely identifies the sample in the LVL. This identifier 
	 * is computed from the collection Id and the catalog number fields. This method uses the 
	 * default character {@link NamingUtils#ID_FRAGMENT_SEPARATOR} to separate these particles 
	 * in the created identifier and the default notation {@link DataSource.Notation#NOTATION_SHORT}.
	 * @param escape - escape URL path segment
	 * @return an identifier that uniquely identifies the sequence in the LVL.
	 */
	public String toId(final boolean escape) {		
		return NamingUtils.toId(collectionId, escape ? escapeUrlPathSegment(catalogNumber) : catalogNumber, NOTATION_SHORT);		
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		
		public static final String IOCL_PATTERN = "IOCL([ \\t]|%20|\\+)[0-9]+";
		public static final String NUMBER_YEAR_PATTERN = "[0-9]+(/|%2f|%2F)[0-9]+";

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

		/**
		 * Parses the input identifier, extracting the parts of a {@link SampleKey}. In addition, the data source of the generated 
		 * sample key is converted to the specified notation.
		 * @param id - the identifier to be parsed
		 * @param separator - the separator the identifier uses to separate its different parts
		 * @param keyFormar - the format used to produce catalog numbers (usually a regex pattern)
		 * @param notation - the notation the data source of the generated sample key is converted to
		 * @return a sample key.
		 */
		public SampleKey parse(final String id, final char separator, final String keyFormat, final Notation notation) {
			checkArgument(notation != null, "Uninitialized notation");
			final SampleKey sampleKey = parse(id, separator, keyFormat);
			switch (notation) {
			case NOTATION_SHORT:
				sampleKey.setCollectionId(toShortNotation(sampleKey.getCollectionId()));
				break;
			case NOTATION_LONG:
				sampleKey.setCollectionId(toLongNotation(sampleKey.getCollectionId()));
				break;
			default:
				break;
			}
			return sampleKey;
		}

		/**
		 * Parses the input identifier, extracting the parts of a {@link SampleKey}.
		 * Key examples:
		 * <ul>
		 * <li>Plain text: <tt>IOCL 0001</tt>, <tt>908/15</tt>.</li>
		 * <li>URL encoded: <tt>IOCL%200001</tt>, <tt>908%2F15</tt>.</li>
		 * </ul>
		 * @param id - the identifier to be parsed
		 * @param separator - the separator the identifier uses to separate its different parts
		 * @param keyFormar - the format used to produce catalog numbers (usually a regex pattern)
		 * @return a sample key.
		 */
		public SampleKey parse(final String id, final char separator, final String keyFormat) {
			checkArgument(isNotBlank(id) && id.matches("[a-zA-Z_0-9]+" + separator + keyFormat), 
					"Uninitialized or invalid id");
			final List<String> tokens = on(separator)
					.omitEmptyStrings()
					.trimResults()
					.splitToList(id);
			checkState(tokens != null && tokens.size() == 2, "Invalid id or separator");
			return collectionId(tokens.get(0))
					.catalogNumber(tokens.get(1))
					.build();
		}

	}

}