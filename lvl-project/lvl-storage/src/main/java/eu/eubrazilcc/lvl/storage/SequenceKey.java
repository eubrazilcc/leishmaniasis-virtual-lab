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

import java.util.List;
import java.util.Objects;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.DataSource.Notation;
import eu.eubrazilcc.lvl.core.util.NamingUtils;

/**
 * Stores a sequence key that can be used to transmit over the network and to store it in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceKey {

	private String dataSource;
	private String accession;

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(final String dataSource) {
		this.dataSource = dataSource;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(final String accession) {
		this.accession = accession;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SequenceKey)) {
			return false;
		}
		final SequenceKey other = SequenceKey.class.cast(obj);
		return Objects.equals(dataSource, other.dataSource)
				&& Objects.equals(accession, other.accession);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataSource, accession);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("dataSource", dataSource)
				.add("accession", accession)
				.toString();
	}

	/**
	 * Creates an identifier that uniquely identifies the sequence in the LVL. This identifier 
	 * is computed from the data source and the accession fields. This method uses the default
	 * character {@link NamingUtils#ID_FRAGMENT_SEPARATOR} to separate these particles in the 
	 * created identifier and the default notation {@link DataSource.Notation#NOTATION_SHORT}.
	 * @return an identifier that uniquely identifies the sequence in the LVL.
	 */
	public String toId() {
		return NamingUtils.toId(dataSource, accession, NOTATION_SHORT);		
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SequenceKey instance = new SequenceKey();

		public Builder dataSource(final String dataSource) {
			instance.setDataSource(dataSource);
			return this;
		}

		public Builder accession(final String accession) {
			instance.setAccession(accession);
			return this;
		}

		public SequenceKey build() {
			return instance;
		}

		/**
		 * Parses the input identifier, extracting the parts of a {@link SequenceKey}. In addition, the data source of the generated sequence key is
		 * converted to the specified notation.
		 * @param id - the identifier to be parsed
		 * @param separator - the separator the identifier uses to separate its different parts
		 * @param notation - the notation the data source of the generated sequence key is converted to
		 * @return a sequence key.
		 */
		public SequenceKey parse(final String id, final char separator, final Notation notation) {
			checkArgument(notation != null, "Uninitialized notation");
			final SequenceKey sequenceKey = parse(id, separator);
			switch (notation) {
			case NOTATION_SHORT:
				sequenceKey.setDataSource(toShortNotation(sequenceKey.getDataSource()));
				break;
			case NOTATION_LONG:
				sequenceKey.setDataSource(toLongNotation(sequenceKey.getDataSource()));
				break;
			default:
				break;
			}
			return sequenceKey;
		}

		/**
		 * Parses the input identifier, extracting the parts of a {@link SequenceKey}.
		 * @param id - the identifier to be parsed
		 * @param separator - the separator the identifier uses to separate its different parts
		 * @return a sequence key.
		 */
		public SequenceKey parse(final String id, final char separator) {
			checkArgument(isNotBlank(id) && id.matches("[a-zA-Z_0-9]+" + separator + "[a-zA-Z_0-9]+"), 
					"Uninitialized or invalid id");
			final List<String> tokens = on(separator)
					.omitEmptyStrings()
					.trimResults()
					.splitToList(id);
			checkState(tokens != null && tokens.size() == 2, "Invalid id or separator");
			return dataSource(tokens.get(0))
					.accession(tokens.get(1))
					.build();
		}

	}

}