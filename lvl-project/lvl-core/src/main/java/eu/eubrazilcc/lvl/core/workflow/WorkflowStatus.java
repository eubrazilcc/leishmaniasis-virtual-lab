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

package eu.eubrazilcc.lvl.core.workflow;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ComparisonChain.start;
import static com.google.common.collect.Range.closed;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

/**
 * Workflow status.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowStatus implements Comparable<WorkflowStatus> {

	private static final Range<Integer> PERCENT_RANGE = closed(0, 100);

	private int completeness;
	private String status;
	private Optional<String> description = absent();

	private boolean completed;
	private boolean failed;

	public int getCompleteness() {
		return completeness;
	}
	public void setCompleteness(final int completeness) {
		this.completeness = completeness;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(final String status) {
		this.status = status;
	}
	public String getDescription() {
		return description.orNull();
	}
	public void setDescription(final @Nullable String description) {
		this.description = fromNullable(description);
	}
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(final boolean completed) {
		this.completed = completed;
	}
	public boolean isFailed() {
		return failed;
	}
	public void setFailed(final boolean failed) {
		this.failed = failed;
	}

	@Override
	public int compareTo(final WorkflowStatus other) {
		if (this == other) return 0;
		return start()
				.compare(this.completeness, other.completeness)
				.result();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WorkflowStatus)) {
			return false;
		}
		final WorkflowStatus other = WorkflowStatus.class.cast(obj);
		return Objects.equals(completeness, other.completeness)
				&& Objects.equals(status, other.status)
				&& Objects.equals(description, other.description)
				&& Objects.equals(completed, other.completed)
				&& Objects.equals(failed, other.failed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(completeness, status, description, completed, failed);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("completeness", completeness)
				.add("status", status)
				.add("description", description.orNull())
				.add("completed", completed)
				.add("failed", failed)
				.toString();
	}

	public static final int checkPercent(final String percent) {
		checkArgument(isNotBlank(percent), "Uninitialized or invalid percent");
		return checkPercent(Integer.valueOf(percent));		
	}

	public static final int checkPercent(final int percent) {
		checkArgument(PERCENT_RANGE.contains(percent));
		return percent;
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final WorkflowStatus instance = new WorkflowStatus();

		public Builder completeness(final int completeness) {
			instance.setCompleteness(completeness);
			return this;
		}

		public Builder status(final String status) {
			instance.setStatus(status);
			return this;
		}

		public Builder description(final @Nullable String description) {
			instance.setDescription(description);
			return this;
		}

		public Builder completed(final boolean completed) {
			instance.setCompleted(completed);
			return this;
		}

		public Builder failed(final boolean failed) {
			instance.setFailed(failed);
			return this;
		}

		public WorkflowStatus build() {
			return instance;
		}

	}

}