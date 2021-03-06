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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
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
 * Represents a single object or a collection of objects that is backed to a file in the application's database. Besides the properties
 * that are provided by the base class {@link BaseFile}, this class also provides the following additional properties: a {@link #getNamespace() namespace} 
 * for isolating the data sets.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Dataset extends BaseFile implements Linkable<Dataset> {

	@InjectLinks({
		@InjectLink(value="datasets/objects/{urlSafeNamespace}/{urlSafeFilename}", rel=SELF, type=APPLICATION_JSON, bindings={
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

	private String namespace;

	public Dataset() {
		super();
		setNamespace(LVL_DEFAULT_NS);
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

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
		setUrlSafeNamespace(urlEncodeUtf8(defaultIfBlank(namespace, LVL_DEFAULT_NS).trim()));
	}

	@Override
	public void setFilename(final String filename) {
		super.setFilename(filename);
		setUrlSafeFilename(urlEncodeUtf8(trimToEmpty(filename)));
		// update latest version property
		Metadata metadata2 = getMetadata();		
		if (metadata2 == null) {
			metadata2 = Metadata.builder().build(); 
		}
		if (metadata2.getIsLastestVersion() != null) {
			metadata2.setIsLastestVersion(getFilename());
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Dataset)) {
			return false;
		}
		final Dataset other = Dataset.class.cast(obj);
		return  Objects.equals(urlSafeNamespace, other.urlSafeNamespace)
				&& Objects.equals(urlSafeFilename, other.urlSafeFilename)				
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Dataset other) {
		if (other == null) {
			return false;
		}
		return super.equals((BaseFile)other)
				&& Objects.equals(namespace, other.namespace)
				&& Objects.equals(links, other.links);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, urlSafeNamespace, urlSafeFilename, namespace);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("BaseFile", super.toString())
				.add("links", links)
				.add("urlSafeNamespace", urlSafeNamespace)
				.add("urlSafeFilename", urlSafeFilename)
				.add("namespace", namespace)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final Dataset instance = new Dataset();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder namespace(final String namespace) {
			instance.setNamespace(trimToEmpty(namespace));
			return this;
		}

		/* Inherited from BaseFile */

		public Builder id(final String id) {
			instance.setId(id);
			return this;
		}

		public Builder length(final long length) {
			instance.setLength(length);
			return this;
		}

		public Builder chunkSize(final long chunkSize) {
			instance.setChunkSize(chunkSize);
			return this;
		}

		public Builder uploadDate(final Date uploadDate) {
			instance.setUploadDate(uploadDate);
			return this;
		}

		public Builder md5(final String md5) {
			instance.setMd5(md5);
			return this;
		}

		public Builder filename(final String filename) {
			checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
			instance.setFilename(filename.trim());
			return this;
		}

		public Builder contentType(final String contentType) {
			instance.setContentType(contentType);
			return this;
		}

		public Builder aliases(final List<String> aliases) {
			instance.setAliases(aliases);
			return this;
		}

		public Builder metadata(final Metadata metadata) {
			instance.setMetadata(metadata);			
			return this;
		}

		public Dataset build() {
			return instance;
		}

	}

}