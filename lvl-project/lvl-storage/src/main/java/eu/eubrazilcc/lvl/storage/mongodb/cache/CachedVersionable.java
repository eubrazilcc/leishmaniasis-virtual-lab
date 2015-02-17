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

package eu.eubrazilcc.lvl.storage.mongodb.cache;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Cached versionable object.
 * @author Erik Torres <ertorser@upv.es>
 */
public class CachedVersionable {

	private String cachedFilename;
	private String version;

	public String getCachedFilename() {
		return cachedFilename;
	}

	public void setCachedFilename(final String cachedFilename) {
		this.cachedFilename = cachedFilename;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("cachedFilename", cachedFilename)
				.add("version", version)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final CachedVersionable instance = new CachedVersionable();

		public Builder cachedFilename(final String cachedFilename) {
			instance.setCachedFilename(cachedFilename);
			return this;
		}

		public Builder version(final String version) {
			instance.setVersion(version);
			return this;
		}

		public CachedVersionable build() {
			return instance;
		}

	}

}