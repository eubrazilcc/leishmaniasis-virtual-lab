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

package eu.eubrazilcc.lvl.service.rest;

import java.util.List;
import java.util.UUID;

import com.google.common.base.Objects;

/**
 * Encapsulates a task for communicating with a client. Requested tasks are internally converted to classes
 * that can be executed in the background of the application.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Task {

	private UUID uuid;
	private TaskType type;
	private List<String> ids;

	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(final UUID uuid) {
		this.uuid = uuid;
	}
	public TaskType getType() {
		return type;
	}
	public void setType(final TaskType type) {
		this.type = type;
	}
	public List<String> getIds() {
		return ids;
	}
	public void setIds(final List<String> ids) {
		this.ids = ids;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Task)) {
			return false;
		}
		final Task other = Task.class.cast(obj);
		return Objects.equal(uuid, other.uuid)
				&& Objects.equal(type, other.type)
				&& Objects.equal(ids, other.ids);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(uuid, type, ids);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("uuid", uuid)
				.add("type", type)
				.add("ids", ids)
				.toString();
	}

	/* Task types */

	public static enum TaskType {
		IMPORT_SEQUENCES;
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Task task = new Task();

		public Builder uuid(final UUID uuid) {
			task.setUuid(uuid);
			return this;
		}

		public Builder type(final TaskType type) {
			task.setType(type);
			return this;
		}

		public Builder ids(final List<String> ids) {
			task.setIds(ids);
			return this;
		}

		public Task build() {
			return task;
		}

	}	

}