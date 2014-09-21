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

import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * A link that a user create to share a private object with other users.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SharedObject extends StorageObject implements Linkable<SharedObject> {

	@InjectLinks({
		@InjectLink(value="shared_objects/{path}", rel=SELF, type=APPLICATION_JSON, 
				bindings={@Binding(name="path", value="${instance.path}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private List<String> roGuests;
	private List<String> rwGuests;

	public SharedObject() {
		super();
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(final List<Link> links) {
		if (links != null) {
			this.links = newArrayList(links);
		} else {
			this.links = null;
		}
	}

	public List<String> getRoGuests() {
		return roGuests;
	}

	public void setRoGuests(final List<String> roGuests) {
		if (roGuests != null) {
			this.roGuests = newArrayList(roGuests);
		} else {
			this.roGuests = null;
		}
	}

	public List<String> getRwGuests() {
		return rwGuests;
	}

	public void setRwGuests(final List<String> rwGuests) {
		if (rwGuests != null) {
			this.rwGuests = newArrayList(rwGuests);
		} else {
			this.rwGuests = null;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SharedObject)) {
			return false;
		}
		final SharedObject other = SharedObject.class.cast(obj);
		return Objects.equal(links, other.links)				
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final SharedObject other) {
		if (other == null) {
			return false;
		}
		return super.equals((StorageObject)other)
				&& Objects.equal(roGuests, other.roGuests)
				&& Objects.equal(rwGuests, other.rwGuests);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("StorageObject", super.toString())
				.add("roGuests", roGuests)
				.add("rwGuests", rwGuests)
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

		public Builder roGuests(final List<String> roGuests) {
			instance.setRoGuests(roGuests);
			return this;
		}

		public Builder rwGuests(final List<String> rwGuests) {
			instance.setRwGuests(rwGuests);
			return this;
		}

		public SharedObject build() {
			return instance;
		}

	}

}