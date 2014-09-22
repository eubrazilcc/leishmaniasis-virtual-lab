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
 * Base class from which to extend to create objects stored in the filesystem and referred in 
 * the application's database.
 * @author Erik Torres <ertorser@upv.es> 
 */
public class StorageObject {

	private String path; // relative path + filename
	private String mime;
	private String description;	// (optional)
	private String owner;

	public StorageObject() { }

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getMime() {
		return mime;
	}

	public void setMime(final String mime) {
		this.mime = mime;
	}

	public @Nullable String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(final String owner) {
		this.owner = owner;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof StorageObject)) {
			return false;
		}
		final StorageObject other = StorageObject.class.cast(obj);
		return Objects.equals(path, other.path)
				&& Objects.equals(mime, other.mime)				
				&& Objects.equals(description, other.description)
				&& Objects.equals(owner, other.owner);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, mime, description, owner);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("path", path)
				.add("mime", mime)
				.add("description", description)
				.add("owner", owner)
				.toString();
	}

}