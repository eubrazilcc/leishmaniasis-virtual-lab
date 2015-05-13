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

package eu.eubrazilcc.lvl.service.io.filter;

import static com.google.common.collect.ImmutableList.of;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;

import com.google.common.collect.ImmutableList;

/**
 * Filters sequences by identifier.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceIdFilter extends IdentifierFilter {

	private static final ImmutableList<String> DATA_SOURCES = of(GENBANK);

	public SequenceIdFilter(final Iterable<String> ids) {
		super(DATA_SOURCES, ids);
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SequenceIdFilter instance = new SequenceIdFilter(new ImmutableList.Builder<String>().build());

		public Builder ids(final Iterable<String> ids) {
			instance.setIds(ids);
			return this;
		}

		public SequenceIdFilter build() {
			return instance;
		}

	}

}