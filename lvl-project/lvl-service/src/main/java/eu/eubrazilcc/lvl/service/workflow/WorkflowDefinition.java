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

package eu.eubrazilcc.lvl.service.workflow;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.fromNullable;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

/**
 * Definition status.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowDefinition {

	private String id;
	private String name;
	private Optional<String> description;

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
		return Objects.equals(id, other.id)
				&& Objects.equals(name, other.name)
				&& Objects.equals(description.orNull(), other.description.orNull());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
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