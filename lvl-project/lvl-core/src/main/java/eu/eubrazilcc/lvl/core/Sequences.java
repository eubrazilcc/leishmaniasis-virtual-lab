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

import java.util.List;

/**
 * Wraps a collection of GenBank sequences.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Sequences extends Paginable<Sequence> {

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("paginable", super.toString())
				.toString();
	}

	public static SequencesBuilder start() {
		return new SequencesBuilder();
	}

	public static class SequencesBuilder {
		
		private final Sequences instance = new Sequences();

		public SequencesBuilder resource(final String resource) {
			instance.setResource(resource);
			return this;
		}

		public SequencesBuilder page(final int page) {
			instance.setPage(page);
			return this;
		}

		public SequencesBuilder perPage(final int perPage) {
			instance.setPerPage(perPage);
			return this;
		}

		public SequencesBuilder sort(final String sort) {
			instance.setSort(sort);
			return this;
		}

		public SequencesBuilder order(final String order) {
			instance.setOrder(order);
			return this;
		}

		public SequencesBuilder query(final String query) {
			instance.setQuery(query);
			return this;
		}

		public SequencesBuilder totalCount(final int totalCount) {
			instance.setTotalCount(totalCount);
			return this;
		}

		public SequencesBuilder sequences(final List<Sequence> sequences) {
			instance.setElements(sequences);
			return this;			
		}

		public Sequences build() {
			return instance;
		}

	}

}