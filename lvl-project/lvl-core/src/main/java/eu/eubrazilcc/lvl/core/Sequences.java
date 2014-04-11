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

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;

import java.util.List;

/**
 * Wraps a collection of GenBank sequences.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Sequences extends Paginable {

	private List<Sequence> sequences = newArrayList();	

	public List<Sequence> getSequences() {
		return sequences;
	}

	public void setSequences(final List<Sequence> sequences) {
		this.sequences = newArrayList(sequences);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("paginable", super.toString())
				.add("sequences", collectionToString(sequences))
				.toString();
	}

	public static SequencesBuilder start() {
		return new SequencesBuilder();
	}

	public static class SequencesBuilder {

		private final Sequences sequences;

		public SequencesBuilder() {
			sequences = new Sequences();
		}

		public SequencesBuilder paginable(final Paginable paginable) {
			sequences.push(paginable);
			return this;
		}

		public SequencesBuilder sequences(final List<Sequence> seqList) {
			sequences.setSequences(seqList);
			return this;			
		}

		public Sequences build() {
			return sequences;
		}

	}

}