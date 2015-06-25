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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.nonUniqueIndexModel;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.textIndexModel;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.storage.base.LvlObject;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer;

/**
 * Represents a publication citation, including the original PubMed article and additional annotations provided by the LeishVL users.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Citation extends LvlObject {

	public static final String COLLECTION  = "citations";	
	public static final String PUBMED_KEY  = "pubmed.medlineCitation.pmid.value";

	public static final MongoCollectionConfigurer CONFIGURER = new MongoCollectionConfigurer(COLLECTION, true, newArrayList(
			nonUniqueIndexModel(PUBMED_KEY, false),
			textIndexModel(ImmutableList.of("pubmed.medlineCitation.article.articleTitle", 
					"pubmed.medlineCitation.article.abstract.abstractText"), COLLECTION)));

	@InjectLinks({
		@InjectLink(value="citations/{id}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="id", value="${instance.urlSafeLvlId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private LvlCitation lvl; // LeishVL citation	
	private PubmedArticle pubmed; // original PubMed article

	public Citation() {
		super(COLLECTION, CONFIGURER, getLogger(Citation.class));
	}

	@Override	
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		this.links = (links != null ? newArrayList(links) : null);		
	}

	public LvlCitation getLvl() {
		return lvl;
	}

	public void setLvl(final LvlCitation lvl) {
		this.lvl = lvl;
	}

	public PubmedArticle getPubmed() {
		return pubmed;
	}

	public void setPubmed(final PubmedArticle pubmed) {
		this.pubmed = pubmed;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Citation)) {
			return false;
		}
		final Citation other = Citation.class.cast(obj);
		return super.equals((LvlObject)other)
				&& Objects.equals(lvl, other.lvl);		
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(lvl);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add(LvlObject.class.getSimpleName(), super.toString())
				.add("lvl", lvl)				
				.add("pubmed", "<<original PubMed article is not displayed>>")
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder extends LvlObject.Builder<Citation, Builder> {

		public Builder() {
			super(new Citation());
			setBuilder(this);
		}

		public Builder lvl(final LvlCitation lvl) {
			instance.setLvl(lvl);			
			return this;
		}

		public Builder pubmed(final PubmedArticle pubmed) {
			instance.setPubmed(pubmed);
			return this;
		}

		public Citation build() {
			return instance;
		}

	}

}