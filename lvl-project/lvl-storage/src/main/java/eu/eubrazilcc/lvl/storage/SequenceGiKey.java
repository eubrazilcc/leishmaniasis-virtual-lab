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

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.base.Objects;

/**
 * Stores sequence key that is based on the GenBank GenInfo Identifier instead of the GenBank 
 * accession number.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceGiKey {

	private String dataSource;
	private int gi;

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(final String dataSource) {
		this.dataSource = dataSource;
	}

	public int getGi() {
		return gi;
	}

	public void setGi(final int gi) {
		this.gi = gi;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SequenceGiKey)) {
			return false;
		}
		final SequenceGiKey other = SequenceGiKey.class.cast(obj);
		return Objects.equal(dataSource, other.dataSource)
				&& Objects.equal(gi, other.gi);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(dataSource, gi);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("dataSource", dataSource)
				.add("gi", gi)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SequenceGiKey instance = new SequenceGiKey();

		public Builder dataSource(final String dataSource) {
			instance.setDataSource(dataSource);
			return this;
		}

		public Builder gi(final int gi) {
			instance.setGi(gi);
			return this;
		}

		public SequenceGiKey build() {
			return instance;
		}

	}

}