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

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

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
	private Point location;        // Geospatial location

	public Reference() { }

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

	public Point getLocation() {
		return location;
	}

	public void setLocation(final Point location) {
		this.location = location;
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
				&& Objects.equals(location, other.location);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, title, pubmedId, location);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("title", title)
				.add("pubmedId", pubmedId)
				.add("location", location)
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

		public Builder location(final Point location) {
			instance.setLocation(location);
			return this;
		}

		public Reference build() {
			return instance;
		}

	}

}