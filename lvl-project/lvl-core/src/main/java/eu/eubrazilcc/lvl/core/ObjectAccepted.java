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
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

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
 * Convenient class to represent a shared object when is accepted by the granted user.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ObjectAccepted extends Shareable implements Linkable<ObjectAccepted> {

	@InjectLinks({
		@InjectLink(value="shares/accepted/{urlSafeNs}/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeNs", value="${instance.urlSafeNs}"),
				@Binding(name="urlSafeId", value="${instance.urlSafeId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeNs;	
	@JsonIgnore
	private String urlSafeId;

	private String id;        // Resource identifier

	public ObjectAccepted() {
		setUrlSafeNs(LVL_DEFAULT_NS);
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

	public String getUrlSafeNs() {
		return urlSafeNs;
	}

	public void setUrlSafeNs(final String urlSafeNs) {
		this.urlSafeNs = urlSafeNs;
	}	

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(id != null ? urlEncodeUtf8(trimToEmpty(id)) : id);
	}

	@Override
	public void setUser(final String user) {
		setUrlSafeNs(urlEncodeUtf8(defaultIfBlank(user, LVL_DEFAULT_NS).trim()));
		super.setUser(user);
	}	

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof ObjectAccepted)) {
			return false;
		}
		final ObjectAccepted other = ObjectAccepted.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);		
	}

	@Override
	public boolean equalsIgnoringVolatile(final ObjectAccepted other) {
		if (other == null) {
			return false;
		}
		return super.equals((Shareable)other)
				&& Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, id);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Shareable", super.toString())
				.add("links", links)
				.add("id", id)
				.toString();
	}

	/* Fluent API */

	public static ObjectAcceptedBuilder builder() {
		return new ObjectAcceptedBuilder();
	}

	public static class ObjectAcceptedBuilder extends Builder<ObjectAccepted> {

		public ObjectAcceptedBuilder() {
			super(ObjectAccepted.class);
		}

		public ObjectAcceptedBuilder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public ObjectAcceptedBuilder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public ObjectAcceptedBuilder newId() {
			instance.setId(compactRandomUUID());
			return this;
		}

		public ObjectAccepted build() {
			return instance;
		}

	}

}