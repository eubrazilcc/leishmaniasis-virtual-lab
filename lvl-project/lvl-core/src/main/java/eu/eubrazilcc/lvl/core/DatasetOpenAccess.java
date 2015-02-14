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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Provides information about a dataset open access link.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetOpenAccess implements Linkable<DatasetOpenAccess> {

	@InjectLinks({
		@InjectLink(value="datasets/open_access/{urlSafeNamespace}/{urlSafeFilename}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeNamespace", value="${instance.urlSafeNamespace}"),
				@Binding(name="urlSafeFilename", value="${instance.urlSafeFilename}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private String urlSafeNamespace;
	private String urlSafeFilename;
	private String urlSafeSubject;

	private String namespace;
	private String filename;
	private String openAccessLink;
	private Date openAccessDate;

	public DatasetOpenAccess() {
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

	public String getOpenAccessLink() {
		return openAccessLink;
	}

	public void setOpenAccessLink(final String openAccessLink) {
		this.openAccessLink = openAccessLink;
	}

	public Date getOpenAccessDate() {
		return openAccessDate;
	}

	public void setOpenAccessDate(final Date openAccessDate) {
		this.openAccessDate = openAccessDate;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof DatasetOpenAccess)) {
			return false;
		}
		final DatasetOpenAccess other = DatasetOpenAccess.class.cast(obj);
		return  Objects.equals(urlSafeNamespace, other.urlSafeNamespace)
				&& Objects.equals(urlSafeFilename, other.urlSafeFilename)				
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final DatasetOpenAccess other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(namespace, other.namespace)
				&& Objects.equals(filename, other.filename)
				&& Objects.equals(openAccessLink, other.openAccessLink)
				&& Objects.equals(openAccessDate, other.openAccessDate)
				&& Objects.equals(links, other.links);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, urlSafeNamespace, urlSafeFilename, namespace, filename, openAccessLink, openAccessDate);
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
				.add("openAccessLink", openAccessLink)
				.add("openAccessDate", openAccessDate)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final DatasetOpenAccess instance = new DatasetOpenAccess();

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

		public Builder openAccessLink(final String openAccessLink) {
			instance.setOpenAccessLink(openAccessLink);
			return this;
		}

		public Builder openAccessDate(final Date openAccessDate) {
			instance.setOpenAccessDate(openAccessDate);
			return this;
		}

		public DatasetOpenAccess build() {
			return instance;
		}

	}

}