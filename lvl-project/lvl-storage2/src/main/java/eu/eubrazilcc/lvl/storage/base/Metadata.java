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
import static com.google.common.collect.Iterables.elementsEqual;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.newHashMap;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Equivalence;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * Stores metadata for files.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Metadata {

	private Optional<String> namespace = absent(); // (optional) namespace
	private String filename;
	private Optional<String> openAccessLink = absent(); // (optional) openaccess link
	private Optional<Date> openAccessDate = absent(); // (optional) openaccess date

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

	public @Nullable String getOpenAccessLink() {
		return openAccessLink.orNull();
	}

	public void setOpenAccessLink(final @Nullable String openAccessLink) {
		this.openAccessLink = fromNullable(openAccessLink);
		setOpenAccessDate(new Date());
	}

	public @Nullable Date getOpenAccessDate() {
		return openAccessDate.orNull();
	}

	public void setOpenAccessDate(final @Nullable Date openAccessDate) {
		this.openAccessDate = fromNullable(openAccessDate);
	}

	public Map<String, Object> getOthers() {
		return others;
	}

	public void setOthers(final Map<String, Object> others) {
		this.others = others;
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
		final Equivalence<Object> equivalence = new Equivalence<Object>() {
			@Override
			protected boolean doEquivalent(final Object a, final Object b) {
				if (a instanceof Iterable && b instanceof Iterable) {
					return elementsEqual((Iterable<?>)a, (Iterable<?>)b);
				} else if (a instanceof Map && b instanceof Map) {
					return difference((Map<?, ?>)a, (Map<?, ?>)b, this).areEqual();
				} else return a.equals(b);					
			}
			@Override
			protected int doHash(final Object t) {
				return t.hashCode();
			}
		};
		return Objects.equals(namespace.orNull(), other.namespace.orNull())
				&& Objects.equals(filename, other.filename)
				&& Objects.equals(openAccessLink.orNull(), other.openAccessLink.orNull())
				&& Objects.equals(openAccessDate.orNull(), other.openAccessDate.orNull())
				&& ((others == null && other.others == null) || difference(others, other.others, equivalence).areEqual());
	}

	@Override
	public int hashCode() {
		return Objects.hash(namespace, filename, others);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("namespace", namespace.orNull())
				.add("filename", filename)
				.add("openAccessLink", openAccessLink.orNull())
				.add("openAccessDate", openAccessDate.orNull())
				.add("others", others)
				.toString();
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

		public Builder openAccessLink(final String openAccessLink) {
			instance.setOpenAccessLink(openAccessLink);
			return this;
		}

		public Builder openAccessDate(final Date openAccessDate) {
			instance.setOpenAccessDate(openAccessDate);
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