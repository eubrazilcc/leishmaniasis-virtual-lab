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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Contains identifiers generated from a query and a hash code computed from the query parameters.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Identifiers {

	private String hash;
	private Set<String> identifiers;

	public String getHash() {
		return hash;
	}
	public void setHash(final String hash) {
		this.hash = hash;
	}
	public Set<String> getIdentifiers() {
		return identifiers;
	}
	public void setIdentifiers(final Set<String> identifiers) {
		this.identifiers = identifiers;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(hash, identifiers);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("hash", hash)
				.add("identifiers", identifiers)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final Identifiers instance = new Identifiers();

		public Builder hash(final String hash) {
			checkArgument(isNotBlank(hash), "Uninitialized or invalid hash");
			instance.setHash(hash.trim());
			return this;
		}

		public Builder identifiers(final @Nullable Set<String> identifiers) {
			instance.setIdentifiers(identifiers);
			return this;
		}

		public Identifiers build() {
			return instance;
		}

	}

}