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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;

import java.util.List;

/**
 * Wraps a collection of PubMed publications.
 * @author Erik Torres <ertorser@upv.es>
 */
public class References extends Paginable {

	private List<Reference> references = newArrayList();

	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(final List<Reference> references) {
		this.references = newArrayList(references);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("paginable", super.toString())
				.add("references", collectionToString(references))
				.toString();
	}

	public static ReferencesBuilder start() {
		return new ReferencesBuilder();
	}

	public static class ReferencesBuilder {

		private final References references;

		public ReferencesBuilder() {
			references = new References();
		}

		public ReferencesBuilder paginable(final Paginable paginable) {
			references.push(paginable);
			return this;
		}

		public ReferencesBuilder references(final List<Reference> refList) {
			references.setReferences(refList);
			return this;			
		}

		public References build() {
			return references;
		}

	}

}