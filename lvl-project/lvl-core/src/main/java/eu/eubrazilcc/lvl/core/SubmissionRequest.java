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
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Objects;

/**
 * Any new data submitted to the system.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SubmissionRequest {

	private String assignedTo;
	private SubmissionResolution resolution;
	private SubmissionStatus status;
	private String allocatedCollection;
	private String allocatedId;

	public String getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(final String assignedTo) {
		this.assignedTo = assignedTo;
	}

	public SubmissionResolution getResolution() {
		return resolution;
	}

	public void setResolution(final SubmissionResolution resolution) {
		this.resolution = resolution;
	}

	public SubmissionStatus getStatus() {
		return status;
	}

	public void setStatus(final SubmissionStatus status) {
		this.status = status;
	}

	public String getAllocatedCollection() {
		return allocatedCollection;
	}

	public void setAllocatedCollection(final String allocatedCollection) {
		this.allocatedCollection = allocatedCollection;
	}

	public String getAllocatedId() {
		return allocatedId;
	}

	public void setAllocatedId(final String allocatedId) {
		this.allocatedId = allocatedId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SubmissionRequest)) {
			return false;
		}
		final SubmissionRequest other = SubmissionRequest.class.cast(obj);
		return  Objects.equals(assignedTo, other.assignedTo)
				&& Objects.equals(resolution, other.resolution)
				&& Objects.equals(status, other.status)
				&& Objects.equals(allocatedCollection, other.allocatedCollection)
				&& Objects.equals(allocatedId, other.allocatedId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(assignedTo, resolution, status, allocatedCollection, allocatedId);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("assignedTo", assignedTo)
				.add("resolution", resolution)
				.add("status", status)
				.add("allocatedCollection", allocatedCollection)
				.add("allocatedId", allocatedId)
				.toString();
	}

	/**
	 * Possible resolutions of a submission request.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public enum SubmissionResolution {
		ACCEPTED,
		INVALID,
		DUPLICATE
	}

	/**
	 * Status of the submission request.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public enum SubmissionStatus {
		NEW,
		ASSIGNED,
		ACCEPTED,
		CLOSED,
		REOPENED
	}

	/* Fluent API */

	public static class Builder<T extends SubmissionRequest> {

		protected final T instance;

		public Builder(final Class<T> clazz) {
			T tmp = null;
			try {
				tmp = clazz.newInstance();
			} catch (Exception ignore) { }
			instance = tmp;
		}

		public Builder<T> assignedTo(final String assignedTo) {
			String assignedTo2 = null;
			checkArgument(isNotBlank(assignedTo2 = trimToNull(assignedTo)), "Uninitialized or invalid owner");
			instance.setAssignedTo(assignedTo2);
			return this;
		}

		public Builder<T> resolution(final SubmissionResolution resolution) {
			instance.setResolution(requireNonNull(resolution, "Uninitialized resolution"));
			return this;
		}

		public Builder<T> status(final SubmissionStatus status) {
			instance.setStatus(requireNonNull(status, "Uninitialized status"));
			return this;
		}

		public Builder<T> allocatedCollection(final String allocatedCollection) {
			String allocatedCollection2 = null;
			checkArgument(isNotBlank(allocatedCollection2 = trimToNull(allocatedCollection)), "Uninitialized or invalid allocated collection");
			instance.setAllocatedCollection(allocatedCollection2);
			return this;
		}

		public Builder<T> allocatedId(final String allocatedId) {
			String allocatedId2 = null;
			checkArgument(isNotBlank(allocatedId2 = trimToNull(allocatedId)), "Uninitialized or invalid allocated Id");
			instance.setAllocatedId(allocatedId2);
			return this;
		}

		public T build() {
			return instance;
		}

	}

}