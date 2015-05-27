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

package eu.eubrazilcc.lvl.core.workflow;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

/**
 * Defines workflow options.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WfOption {

	private String name;
	private Optional<String> folderId = absent();

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getFolderId() {
		return folderId.or("");
	}

	public void setFolderId(final @Nullable String folderId) {
		this.folderId = fromNullable(folderId);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WfOption)) {
			return false;
		}
		final WfOption other = WfOption.class.cast(obj);
		return Objects.equals(name, other.name)
				&& Objects.equals(folderId.orNull(), other.folderId.orNull());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, folderId);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("name", name)
				.add("folderId", folderId.orNull())
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final WfOption instance = new WfOption();		

		public Builder name(final String name) {
			instance.setName(name);
			return this;
		}

		public Builder folderId(final @Nullable String folderId) {
			instance.setFolderId(folderId);			
			return this;
		}

		public WfOption build() {
			return instance;
		}

	}

}