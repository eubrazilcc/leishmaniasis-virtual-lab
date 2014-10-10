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

package eu.eubrazilcc.lvl.core.workflow;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Defines workflows.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowDefinition implements Linkable<WorkflowDefinition> {

	@InjectLinks({
		@InjectLink(value="public_links/{id}", rel=SELF, type=APPLICATION_JSON, 
				bindings={@Binding(name="id", value="${instance.id}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private String id;
	private String name;
	private Optional<String> description;

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

	public String getId() {
		return id;
	}
	public void setId(final String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public String getDescription() {
		return description.or("");
	}
	public void setDescription(final @Nullable String description) {
		this.description = fromNullable(description);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WorkflowDefinition)) {
			return false;
		}
		final WorkflowDefinition other = WorkflowDefinition.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final WorkflowDefinition other) {
		return Objects.equals(id, other.id)
				&& Objects.equals(name, other.name)
				&& Objects.equals(description.orNull(), other.description.orNull());
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, id, name, description);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("id", id)
				.add("name", name)
				.add("description", description.orNull())
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final WorkflowDefinition instance = new WorkflowDefinition();

		public Builder id(final String id) {
			instance.setId(id);
			return this;
		}

		public Builder name(final String name) {
			instance.setName(name);
			return this;
		}

		public Builder description(final @Nullable String description) {
			instance.setDescription(description);			
			return this;
		}

		public WorkflowDefinition build() {
			return instance;
		}

	}	

}