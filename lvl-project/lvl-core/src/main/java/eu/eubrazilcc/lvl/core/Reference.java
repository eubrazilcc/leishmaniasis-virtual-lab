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

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * Stores a publication reference as a subset of PubMed fields (since publications comes from PubMed) 
 * annotated with additional fields.
 * @author Erik Torres <ertorser@upv.es>
 */
@XmlRootElement
public class Reference {

	private String title;     // Title of the published work
	private String pubmedId;  // PubMed Identifier (PMID)

	public Reference() { }

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(final String pubmedId) {
		this.pubmedId = pubmedId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Reference)) {
			return false;
		}
		final Reference other = Reference.class.cast(obj);
		return Objects.equal(title, other.title)
				&& Objects.equal(pubmedId, other.pubmedId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(title, pubmedId);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("title", title)
				.add("pubmedId", pubmedId)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Reference instance = new Reference();

		public Builder title(final String title) {
			instance.setTitle(title);
			return this;
		}

		public Builder pubmedId(final String pubmedId) {
			instance.setPubmedId(pubmedId);
			return this;
		}

		public Reference build() {
			return instance;
		}

	}

}