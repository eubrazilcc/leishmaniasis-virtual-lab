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
import static eu.eubrazilcc.lvl.core.util.NamingUtils.encodePublicLinkPath;
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
 * A link that a user create to share a private object with other users without requiring authentication (any user that knows the link 
 * can access the content of the object).
 * @author Erik Torres <ertorser@upv.es>
 */
@Deprecated
public class PublicLinkOLD extends StorageObject implements Linkable<PublicLinkOLD> {

	@InjectLinks({
		@InjectLink(value="public_links/{urlSafePath}", rel=SELF, type=APPLICATION_JSON, 
				bindings={@Binding(name="urlSafePath", value="${instance.urlSafePath}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private Target target;

	private String downloadUri;

	private String urlSafePath;

	public PublicLinkOLD() {
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

	@Override
	public void setPath(final String path) {		
		super.setPath(path);
		setUrlSafePath(encodePublicLinkPath(path));
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(final Target target) {
		this.target = target;
	}	

	public String getDownloadUri() {
		return downloadUri;
	}

	public void setDownloadUri(final String downloadUri) {
		this.downloadUri = downloadUri;
	}

	public String getUrlSafePath() {
		return urlSafePath;
	}

	public void setUrlSafePath(final String urlSafePath) {
		this.urlSafePath = urlSafePath;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PublicLinkOLD)) {
			return false;
		}
		final PublicLinkOLD other = PublicLinkOLD.class.cast(obj);
		return Objects.equals(links, other.links)
				&& Objects.equals(downloadUri, other.downloadUri)
				&& Objects.equals(urlSafePath, other.urlSafePath)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final PublicLinkOLD other) {
		if (other == null) {
			return false;
		}
		return super.equals((StorageObject)other)
				&& Objects.equals(target, other.target);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, target, downloadUri, urlSafePath);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("StorageObject", super.toString())
				.add("target", target)
				.add("downloadUri", downloadUri)
				.add("urlSafePath", urlSafePath)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final PublicLinkOLD instance = new PublicLinkOLD();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder path(final String path) {
			checkArgument(isNotBlank(path), "Uninitialized or invalid path");
			instance.setPath(path.trim());
			return this;
		}

		public Builder mime(final String mime) {
			instance.setMime(trimToEmpty(mime));
			return this;
		}

		public Builder description(final String description) {
			instance.setDescription(trimToEmpty(description));
			return this;
		}

		public Builder owner(final String owner) {
			checkArgument(isNotBlank(owner), "Uninitialized or invalid owner");
			instance.setOwner(owner.trim());
			return this;
		}

		public Builder created(final Date created) {
			checkArgument(created != null, "Uninitialized creation time");
			instance.setCreated(created);			
			return this;
		}

		public Builder target(final Target target) {
			instance.setTarget(target);
			return this;
		}

		public Builder downloadUri(final String downloadUri) {
			instance.setDownloadUri(trimToEmpty(downloadUri));
			return this;
		}

		public Builder urlSafePath(final String urlSafePath) {
			instance.setUrlSafePath(trimToEmpty(urlSafePath));
			return this;
		}

		public PublicLinkOLD build() {
			return instance;
		}

	}

}