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

import java.util.List;
import java.util.Objects;

/**
 * Additional annotations provided by the LeishVL users to the citations.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LvlCitation {

	private List<String> cited;

	public List<String> getCited() {
		return cited;
	}

	public void setCited(final List<String> cited) {
		this.cited = cited;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof LvlCitation)) {
			return false;
		}
		final LvlCitation other = LvlCitation.class.cast(obj);
		return Objects.equals(cited, other.cited);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cited);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("cited", cited)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private LvlCitation instance = new LvlCitation();

		public Builder cited(final List<String> cited) {
			instance.setCited(cited);
			return this;
		}

		public LvlCitation build() {
			return instance;
		}

	}

}