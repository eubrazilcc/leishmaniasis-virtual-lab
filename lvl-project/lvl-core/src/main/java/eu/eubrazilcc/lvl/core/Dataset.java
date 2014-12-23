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
 * A private object that can be shared with other users who are authorized to access the object.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Dataset extends BaseFile implements Linkable<Dataset> {

	@InjectLinks({
		@InjectLink(value="datasets/{namespace}/{filename}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="namespace", value="${instance.namespace}"),
				@Binding(name="filename", value="${instance.filename}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private String namespace;

	public Dataset() {
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

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Dataset)) {
			return false;
		}
		final Dataset other = Dataset.class.cast(obj);
		return Objects.equals(links, other.links)				
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Dataset other) {
		if (other == null) {
			return false;
		}
		return super.equals((BaseFile)other)
				&& Objects.equals(namespace, other.namespace);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, namespace);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("BaseFile", super.toString())
				.add("namespace", namespace)
				.toString();
	}

	/* Inner classes */

	public static class DatasetMetadata implements Metadata {

		private String description;
		private Target target;

		public String getDescription() {
			return description;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		public Target getTarget() {
			return target;
		}

		public void setTarget(final Target target) {
			this.target = target;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof DatasetMetadata)) {
				return false;
			}
			final DatasetMetadata other = DatasetMetadata.class.cast(obj);
			return Objects.equals(description, other.description)
					&& Objects.equals(target, other.target);
		}

		@Override
		public int hashCode() {
			return Objects.hash(description, target);
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("description", description)
					.add("target", target)
					.toString();
		}

		/* Fluent API */

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private final DatasetMetadata instance = new DatasetMetadata();

			public Builder description(final String description) {
				instance.setDescription(description);
				return this;
			}

			public Builder target(final Target target) {
				instance.setTarget(target);
				return this;
			}

			public DatasetMetadata build() {
				return instance;
			}

		}
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