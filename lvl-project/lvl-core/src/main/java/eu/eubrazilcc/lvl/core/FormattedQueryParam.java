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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Formatted query parameter.
 * @author Erik Torres <ertorser@upv.es>
 */
public class FormattedQueryParam {

	private String term;
	private boolean validity;

	public String getTerm() {
		return term;
	}
	public void setTerm(final String term) {
		this.term = term;
	}
	public boolean isValidity() {
		return validity;
	}
	public void setValidity(final boolean validity) {
		this.validity = validity;
	}

	@Override
	public String toString() {
		return toStringHelper(FormattedQueryParam.class)
				.add("term", term)
				.add("validity", validity)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final FormattedQueryParam instance = new FormattedQueryParam();

		public Builder term(final String term) {
			instance.setTerm(term);
			return this;
		}

		public Builder validity(final boolean validity) {
			instance.setValidity(validity);
			return this;
		}

		public FormattedQueryParam build() {
			return instance;
		}

	}

}