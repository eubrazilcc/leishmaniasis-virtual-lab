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
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;

/**
 * Stores a publication reference as a subset of PubMed fields (since publications comes from PubMed) annotated 
 * with additional fields. In particular, a GeoJSON point is included that allows callers to georeference the sequence.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON open standard format for encoding geographic data structures</a>
 */
public class Reference implements Linkable<Reference> {

	@InjectLinks({
		@InjectLink(value="references/{id}", rel=SELF, type=APPLICATION_JSON, bindings={@Binding(name="id", value="${instance.pubmedId}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links;      // HATEOAS links

	private String title;          // Title of the published work
	private String pubmedId;       // PubMed Identifier (PMID)
	private int publicationYear;   // Journal publication year
	private Set<String> seqids;    // Sequences mentioned in this publication (must include database and accession number)

	private PubmedArticle article; // Original PubMed article
	
	public Reference() { }

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

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(final String pubmedId) {
		this.pubmedId = pubmedId;
	}

	public int getPublicationYear() {
		return publicationYear;
	}

	public void setPublicationYear(final int publicationYear) {
		this.publicationYear = publicationYear;
	}

	public Set<String> getSeqids() {
		return seqids;
	}

	public void setSeqids(final Set<String> seqids) {
		this.seqids = seqids;
	}

	public PubmedArticle getArticle() {
		return article;
	}

	public void setArticle(final PubmedArticle article) {
		this.article = article;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Reference)) {
			return false;
		}
		final Reference other = Reference.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);		
	}

	@Override
	public boolean equalsIgnoringVolatile(final Reference other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(title, other.title)
				&& Objects.equals(pubmedId, other.pubmedId)
				&& Objects.equals(publicationYear, other.publicationYear)
				&& Objects.equals(seqids, other.seqids);
		// article
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, title, pubmedId); // article
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("title", title)
				.add("pubmedId", pubmedId)
				.add("publicationYear", publicationYear)
				.add("seqids", seqids)
				.add("article", "<<original article is not displayed>>")
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Reference instance = new Reference();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder title(final String title) {
			instance.setTitle(title);
			return this;
		}

		public Builder pubmedId(final String pubmedId) {
			instance.setPubmedId(pubmedId);
			return this;
		}

		public Builder publicationYear(final int publicationYear) {
			instance.setPublicationYear(publicationYear);
			return this;
		}

		public Builder seqids(final Set<String> seqids) {
			instance.setSeqids(seqids);
			return this;
		}
		
		public Builder article(final PubmedArticle article) {
			instance.setArticle(article);
			return this;
		}

		public Reference build() {
			return instance;
		}

	}

}