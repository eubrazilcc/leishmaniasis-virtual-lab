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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

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
 * Provides information aboud a shared object.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SharedObject extends Shareable implements Linkable<SharedObject> {

	@InjectLinks({
		@InjectLink(value="shares/{urlSafeNamespace}/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeNamespace", value="${instance.urlSafeNamespace}"),
				@Binding(name="urlSafeId", value="${instance.urlSafeId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeNamespace;
	@JsonIgnore
	private String urlSafeId;

	private String id;        // Resource identifier	

	private String collection;
	private String objectId;

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

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	@Override
	public void setSubject(final String subject) {
		super.setSubject(subject);
		setUrlSafeNamespace(urlEncodeUtf8(defaultIfBlank(subject, LVL_DEFAULT_NS).trim()));
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(id != null ? urlEncodeUtf8(trimToEmpty(id)) : id);
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(final String objectId) {
		this.objectId = objectId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SharedObject)) {
			return false;
		}
		final SharedObject other = SharedObject.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);		
	}

	@Override
	public boolean equalsIgnoringVolatile(final SharedObject other) {
		if (other == null) {
			return false;
		}
		return super.equals((Shareable)other)
				&& Objects.equals(id, other.id)
				&& Objects.equals(collection, other.collection)
				&& Objects.equals(objectId, other.objectId);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, id, collection, objectId);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Shareable", super.toString())
				.add("links", links)
				.add("id", id)
				.add("collection", collection)
				.add("objectId", objectId)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SharedObject instance = new SharedObject();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder newId() {
			instance.setId(compactRandomUUID());
			return this;
		}

		public Builder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder collection(final String collection) {
			String collection2 = null;
			checkArgument(isNotBlank(collection2 = trimToNull(collection)), "Uninitialized or invalid collection");
			instance.setCollection(collection2);
			return this;
		}

		public Builder objectId(final String objectId) {
			String objectId2 = null;
			checkArgument(isNotBlank(objectId2 = trimToNull(objectId)), "Uninitialized or invalid object Id");
			instance.setObjectId(objectId2);
			return this;
		}


		/* Inherited from Shareable */

		public Builder subject(final String subject) {
			checkArgument(isNotBlank(subject), "Uninitialized or invalid subject");
			instance.setSubject(subject.trim());
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

		public SharedObject build() {
			return instance;
		}

	}

}