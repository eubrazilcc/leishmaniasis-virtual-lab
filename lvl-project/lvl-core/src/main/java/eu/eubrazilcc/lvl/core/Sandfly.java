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
 * Stores Sandfly nucleotide sequences that includes specific information related to this family. In addition, Jersey annotations
 * are included to inject links from a RESTful resource.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Sandfly extends Sequence implements Linkable<Sandfly> {

	@InjectLinks({
		@InjectLink(value="sandflies/{id}", rel=SELF, type=APPLICATION_JSON, bindings={@Binding(name="id", value="${instance.id}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private Date collected;   // collection date

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

	public Date getCollected() {
		return collected;
	}

	public void setCollected(final Date collected) {
		this.collected = collected;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Sandfly)) {
			return false;
		}
		final Sandfly other = Sandfly.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Sandfly other) {
		if (other == null) {
			return false;
		}
		return super.equals((Sequence)other)
				&& Objects.equals(collected, other.collected);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(links, collected);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Sequence", super.toString())
				.add("links", links)
				.add("collected", collected)
				.toString();
	}

	/* Fluent API */

	public static SandflyBuilder builder() {
		return new SandflyBuilder();
	}

	public static class SandflyBuilder extends Builder<Sandfly> {

		public SandflyBuilder() {
			super(Sandfly.class);
		}

		public SandflyBuilder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public SandflyBuilder collected(final Date collected) {
			instance.setCollected(collected);
			return this;
		}

		public Sandfly build() {
			return instance;
		}

	}

}