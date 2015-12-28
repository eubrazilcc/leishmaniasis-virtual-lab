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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Provides information about a shared dataset.
 * @author Erik Torres <ertorser@upv.es>
 * @deprecated As of release 0.9.2, replaced by {@link eu.eubrazilcc.lvl.core.SharedObject}
 */
@Deprecated
public class DatasetShare extends Shareable implements Linkable<DatasetShare> {

	@InjectLinks({
		@InjectLink(value="datasets/shares/{urlSafeNamespace}/{urlSafeFilename}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeNamespace", value="${instance.urlSafeNamespace}"),
				@Binding(name="urlSafeFilename", value="${instance.urlSafeFilename}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeNamespace;
	@JsonIgnore
	private String urlSafeFilename;
	@JsonIgnore
	private String urlSafeSubject;

	private String namespace;
	private String filename;

	public DatasetShare() {
		super();
	}

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		if (links != null) {
			this.links = newArrayList(links);
		} else {
			this.links = null;
		}
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

	public String getUrlSafeSubject() {
		return urlSafeSubject;
	}

	public void setUrlSafeSubject(final String urlSafeSubject) {
		this.urlSafeSubject = urlSafeSubject;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
		setUrlSafeNamespace(urlEncodeUtf8(trimToEmpty(namespace)));
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
		setUrlSafeFilename(urlEncodeUtf8(trimToEmpty(filename)));
	}

	@Override
	public void setUser(final String user) {
		super.setUser(user);
		setUrlSafeSubject(urlEncodeUtf8(trimToEmpty(user)));
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof DatasetShare)) {
			return false;
		}
		final DatasetShare other = DatasetShare.class.cast(obj);
		return  Objects.equals(urlSafeNamespace, other.urlSafeNamespace)
				&& Objects.equals(urlSafeFilename, other.urlSafeFilename)				
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final DatasetShare other) {
		if (other == null) {
			return false;
		}
		return super.equals((Shareable)other)
				&& Objects.equals(namespace, other.namespace)
				&& Objects.equals(filename, other.filename)
				&& Objects.equals(links, other.links);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, urlSafeNamespace, urlSafeFilename, namespace, filename);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Shareable", super.toString())
				.add("links", links)
				.add("urlSafeNamespace", urlSafeNamespace)
				.add("urlSafeFilename", urlSafeFilename)
				.add("urlSafeSubject", urlSafeSubject)				
				.add("namespace", namespace)
				.add("filename", filename)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final DatasetShare instance = new DatasetShare();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder namespace(final String namespace) {
			checkArgument(isNotBlank(namespace), "Uninitialized or invalid namespace");
			instance.setNamespace(namespace.trim());
			return this;
		}

		public Builder filename(final String filename) {
			checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
			instance.setFilename(filename.trim());
			return this;
		}

		/* Inherited from Shareable */

		public Builder subject(final String subject) {
			checkArgument(isNotBlank(subject), "Uninitialized or invalid subject");
			instance.setUser(subject.trim());
			return this;
		}

		public Builder sharedDate(final Date sharedDate) {
			checkNotNull(sharedDate, "Uninitialized shared date");
			instance.setSharedDate(sharedDate);
			return this;
		}

		public Builder sharedNow() {
			instance.setSharedDate(new Date());
			return this;
		}

		public Builder accessType(final SharedAccess accessType) {
			checkNotNull(accessType, "Uninitialized access type");
			instance.setAccessType(accessType);
			return this;
		}

		public DatasetShare build() {
			return instance;
		}

	}

}