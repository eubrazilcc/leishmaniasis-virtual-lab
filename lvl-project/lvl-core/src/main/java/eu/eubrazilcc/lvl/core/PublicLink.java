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
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import java.util.Date;
import java.util.Objects;

/**
 * A link that a user create to share a private object with other users without requiring authentication (any user that knows the link 
 * can access the content of the object).
 * @author Erik Torres <ertorser@upv.es>
 */
public class PublicLink {

	private String path; // path relative to the storage directory + filename with (optional) extension	
	private Date created;
	private String downloadUri;

	public String getPath() {
		return path;
	}
	
	public void setPath(final String path) {
		this.path = path;
	}
	
	public String getDownloadUri() {
		return downloadUri;
	}
	
	public void setDownloadUri(final String downloadUri) {
		this.downloadUri = downloadUri;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void setCreated(final Date created) {
		this.created = created;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PublicLink)) {
			return false;
		}
		final PublicLink other = PublicLink.class.cast(obj);
		return Objects.equals(path, other.path)
				&& Objects.equals(created, other.created)
				&& Objects.equals(downloadUri, other.downloadUri);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(path, created, downloadUri);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("path", path)
				.add("created", created)
				.add("downloadUri", downloadUri)				
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final PublicLink instance = new PublicLink();

		public Builder path(final String path) {
			checkArgument(isNotBlank(path), "Uninitialized or invalid path");
			instance.setPath(path.trim());
			return this;
		}

		public Builder created(final Date created) {
			checkArgument(created != null, "Uninitialized creation time");
			instance.setCreated(created);			
			return this;
		}

		public Builder downloadUri(final String downloadUri) {
			instance.setDownloadUri(trimToEmpty(downloadUri));
			return this;
		}

		public PublicLink build() {
			return instance;
		}

	}

}