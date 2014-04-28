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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static com.google.common.base.Splitter.on;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Stores sequence key in the database.
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
		return Objects.equal(dataSource, other.dataSource)
				&& Objects.equal(accession, other.accession);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(dataSource, accession);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("dataSource", dataSource)
				.add("accession", accession)
				.toString();
	}
	
	public String toId(final char separator) {
		return dataSource + separator + accession;
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SequenceKey sequenceKey = new SequenceKey();

		public Builder dataSource(final String dataSource) {
			sequenceKey.setDataSource(dataSource);
			return this;
		}

		public Builder accession(final String accession) {
			sequenceKey.setAccession(accession);
			return this;
		}

		public SequenceKey build() {
			return sequenceKey;
		}

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