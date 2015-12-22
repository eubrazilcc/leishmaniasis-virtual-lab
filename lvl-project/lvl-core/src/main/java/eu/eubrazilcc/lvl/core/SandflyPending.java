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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

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
 * Stores a user sequence that is pending for sanitation and later approval. Therefore, this records
 * could be incomplete or inaccurate.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SandflyPending extends PendingSequence implements Linkable<SandflyPending> {

	@InjectLinks({
		@InjectLink(value="pending/sandflies/{urlSafeNamespace}/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, bindings={
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
	public void setNamespace(final String namespace) {		
		super.setNamespace(namespace);
		setUrlSafeNamespace(urlEncodeUtf8(defaultIfBlank(namespace, LVL_DEFAULT_NS).trim()));
	}

	@Override
	public void setId(final String id) {
		super.setId(id);
		setUrlSafeId(id != null ? urlEncodeUtf8(trimToEmpty(id)) : id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SandflyPending)) {
			return false;
		}
		final SandflyPending other = SandflyPending.class.cast(obj);
		return Objects.equals(links, other.links)
				&& Objects.equals(urlSafeNamespace, other.urlSafeNamespace)
				&& Objects.equals(urlSafeId, other.urlSafeId)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final SandflyPending other) {
		if (other == null) {
			return false;
		}
		return super.equals((PendingSequence)other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, urlSafeNamespace, urlSafeId);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("PendingSequence", super.toString())
				.add("links", links)
				.add("urlSafeNamespace", urlSafeNamespace)
				.add("urlSafeId", urlSafeId)
				.toString();
	}

	/* Fluent API */

	public static SandflyPendingBuilder builder() {
		return new SandflyPendingBuilder();
	}

	public static class SandflyPendingBuilder extends Builder<SandflyPending> {

		public SandflyPendingBuilder() {
			super(SandflyPending.class);
		}

		public SandflyPendingBuilder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public SandflyPending build() {
			return instance;
		}

	}

}