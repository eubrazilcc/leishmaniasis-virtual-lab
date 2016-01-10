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
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
 * Stores a user citation that is pending for sanitation and later approval. Therefore, this records
 * could be incomplete or inaccurate.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingReference extends SubmissionRequest implements Linkable<PendingReference> {

	@InjectLinks({
		@InjectLink(value="pending/citations/{urlSafeNamespace}/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, bindings={
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

	private String namespace;      // Name space where the record is inscribed
	private String id;             // Resource identifier
	private String pubmedId;       // PubMed Identifier (PMID)	
	private Set<String> seqids;    // Sequences mentioned in this publication (must include database and accession number)
	private Set<String> sampleids; // Samples mentioned in this publication (must include collection and catalog number)
	private Date modified;         // Last modification time-stamp

	public PendingReference() {
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

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
		setUrlSafeNamespace(urlEncodeUtf8(defaultIfBlank(namespace, LVL_DEFAULT_NS).trim()));
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(id != null ? urlEncodeUtf8(trimToEmpty(id)) : id);
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(final String pubmedId) {
		this.pubmedId = pubmedId;
	}

	public Set<String> getSeqids() {
		return seqids;
	}

	public void setSeqids(final Set<String> seqids) {
		this.seqids = seqids;
	}	

	public Set<String> getSampleids() {
		return sampleids;
	}

	public void setSampleids(final Set<String> sampleids) {
		this.sampleids = sampleids;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(final Date modified) {
		this.modified = modified;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PendingReference)) {
			return false;
		}
		final PendingReference other = PendingReference.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);		
	}

	@Override
	public boolean equalsIgnoringVolatile(final PendingReference other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(namespace, other.namespace)
				&& Objects.equals(id, other.id)
				&& Objects.equals(pubmedId, other.pubmedId)
				&& Objects.equals(seqids, other.seqids)
				&& Objects.equals(sampleids, other.sampleids)
				&& Objects.equals(modified, other.modified);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, namespace, id, pubmedId, seqids, sampleids, modified);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("namespace", namespace)
				.add("id", id)
				.add("pubmedId", pubmedId)
				.add("seqids", seqids)
				.add("sampleids", sampleids)
				.add("modified", modified)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends SubmissionRequest.Builder<PendingReference> {

		public Builder() {
			super(PendingReference.class);			
		}

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder namespace(final String namespace) {
			instance.setNamespace(trimToEmpty(namespace));
			return this;
		}

		public Builder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder pubmedId(final String pubmedId) {
			instance.setPubmedId(pubmedId);
			return this;
		}

		public Builder seqids(final Set<String> seqids) {
			instance.setSeqids(seqids);
			return this;
		}

		public Builder sampleids(final Set<String> sampleids) {
			instance.setSampleids(sampleids);
			return this;
		}

		public Builder modified(final Date modified){
			instance.setModified(modified);
			return this;
		}

		public PendingReference build() {
			return instance;
		}

	}

}