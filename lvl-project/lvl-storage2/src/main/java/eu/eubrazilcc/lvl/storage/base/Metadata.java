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

package eu.eubrazilcc.lvl.storage.base;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.eubrazilcc.lvl.core.util.CollectionUtils;

/**
 * Stores metadata for files.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Metadata {

	private Optional<String> namespace = absent(); // (optional) namespace	
	private String filename;

	private Optional<String> description = absent(); // (optional) description
	private Set<String> tags = newHashSet(); // tag annotations

	private Optional<OpenAccess> openAccess = absent(); // (optional) open-access link	

	private Map<String, Object> others = newHashMap();	

	@JsonIgnore
	private String urlSafeNamespace;
	@JsonIgnore
	private String urlSafeFilename;		

	public @Nullable String getNamespace() {
		return namespace.orNull();
	}

	public void setNamespace(final @Nullable String namespace) {
		this.namespace = fromNullable(trimToNull(namespace));
		setUrlSafeNamespace(urlEncodeUtf8(this.namespace.or("")));
	}	

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = trimToEmpty(filename);
		setUrlSafeFilename(urlEncodeUtf8(this.filename));
	}	

	public @Nullable String getDescription() {
		return description.orNull();
	}

	public void setDescription(final String description) {
		this.description = fromNullable(trim(description));
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(final Set<String> tags) {
		this.tags = (tags != null ? Sets.<String>newHashSet(tags) : Sets.<String>newHashSet());
	}	

	public @Nullable OpenAccess getOpenAccess() {
		return openAccess.orNull();
	}

	public void setOpenAccess(final @Nullable OpenAccess openAccess) {
		this.openAccess = fromNullable(openAccess);
	}

	public Map<String, Object> getOthers() {
		return others;
	}

	public void setOthers(final Map<String, Object> others) {
		this.others = (others != null ? Maps.<String, Object>newHashMap(others) : Maps.<String, Object>newHashMap());
	}

	public String getUrlSafeNamespace() {
		return urlSafeNamespace;
	}

	public void setUrlSafeNamespace(final String urlSafeNamespace) {
		this.urlSafeNamespace = urlSafeNamespace;
	}

	public String getUrlSafeFilename() {
		return urlSafeFilename;
	}

	public void setUrlSafeFilename(final String urlSafeFilename) {
		this.urlSafeFilename = urlSafeFilename;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Metadata)) {
			return false;
		}
		final Metadata other = Metadata.class.cast(obj);		
		return Objects.equals(namespace.orNull(), other.namespace.orNull())				
				&& Objects.equals(filename, other.filename)
				&& Objects.equals(description.orNull(), other.description.orNull())
				&& Objects.equals(tags, other.tags)
				&& Objects.equals(openAccess.orNull(), other.openAccess.orNull())				
				&& CollectionUtils.equals(others, other.others);
	}

	@Override
	public int hashCode() {
		return Objects.hash(namespace, filename, description, tags, openAccess, others);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("namespace", namespace.orNull())
				.add("filename", filename)
				.add("description", description.orNull())
				.add("tags", tags)
				.add("openAccess", openAccess.orNull())
				.add("others", others)
				.toString();
	}

	/* Inner classes */

	public static class OpenAccess {

		private Integer bucket;	
		private String secret;
		private Date date;

		public Integer getBucket() {
			return bucket;
		}
		public void setBucket(final Integer bucket) {
			this.bucket = bucket;
		}
		public String getSecret() {
			return secret;
		}
		public void setSecret(final String secret) {
			this.secret = secret;			
		}
		public Date getDate() {
			return date;
		}
		public void setDate(final Date date) {
			this.date = date;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof OpenAccess)) {
				return false;
			}
			final OpenAccess other = OpenAccess.class.cast(obj);		
			return Objects.equals(bucket, other.bucket)				
					&& Objects.equals(secret, other.secret)
					&& Objects.equals(date, other.date);
		}

		@Override
		public int hashCode() {
			return Objects.hash(bucket, secret, date);
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("bucket", bucket)
					.add("secret", secret)
					.add("date", date)
					.toString();
		}
		
		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {
			
			private final OpenAccess instance = new OpenAccess();
			
			public Builder bucket(final Integer bucket) {
				instance.setBucket(bucket);
				return this;
			}
			
			public Builder date(final Date date) {
				instance.setDate(date);
				return this;
			}
			
			public Builder secret(final String secret) {
				instance.setSecret(secret);
				return this;
			}
			
			public OpenAccess build() {
				return instance;
			}
			
		}

	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Metadata instance = new Metadata();

		public Builder namespace(final String namespace) {
			instance.setNamespace(namespace);
			return this;
		}

		public Builder filename(final String filename) {
			instance.setFilename(filename);
			return this;
		}

		public Builder description(final String description) {
			instance.setDescription(description);
			return this;
		}

		public Builder tags(final Set<String> tags) {
			instance.setTags(tags);
			return this;
		}

		public Builder openAccess(final OpenAccess openAccess) {
			instance.setOpenAccess(openAccess);
			return this;
		}

		public Builder others(final Map<String, Object> others) {
			instance.setOthers(others);
			return this;
		}

		public Builder newOther(final String name, final Object value) {
			synchronized (instance) {
				if (instance.getOthers() == null) instance.setOthers(Maps.<String, Object>newHashMap());
				instance.getOthers().put(name, value);	
			}			
			return this;
		}

		public Metadata build() {
			return instance;
		}

	}

}