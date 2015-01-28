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

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Any object that can coexists with their versions.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class Versionable {

	/**
	 * A property to explicitly indicate the latest version of a file.
	 */
	private String isLastestVersion = null;

	public @Nullable String getIsLastestVersion() {
		return isLastestVersion;
	}

	public void setIsLastestVersion(final @Nullable String isLastestVersion) {
		this.isLastestVersion = isLastestVersion;
	}

	public abstract void isLastestVersion();

	public void isNotLastestVersion() {
		isLastestVersion = null;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Versionable)) {
			return false;
		}
		final Versionable other = Versionable.class.cast(obj);
		return Objects.equals(isLastestVersion, other.isLastestVersion);
	}

	@Override
	public int hashCode() {
		return Objects.hash(isLastestVersion);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("isLastestVersion", isLastestVersion)
				.toString();
	}

}