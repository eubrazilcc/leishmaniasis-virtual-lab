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

package eu.eubrazilcc.lvl.service;

import com.google.common.base.Objects;

/**
 * Encapsulates a task progress for communicating with a client.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Progress {

	private boolean done;
	private double progress;
	private String status;
	private boolean hasErrors;

	public Progress() { }

	public boolean isDone() {
		return done;
	}
	public void setDone(final boolean done) {
		this.done = done;
	}
	public double getProgress() {
		return progress;
	}
	public void setProgress(final double progress) {
		this.progress = progress;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(final String status) {
		this.status = status;
	}
	public boolean isHasErrors() {
		return hasErrors;
	}
	public void setHasErrors(final boolean hasErrors) {
		this.hasErrors = hasErrors;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Progress)) {
			return false;
		}
		final Progress other = Progress.class.cast(obj);
		return Objects.equal(done, other.done)
				&& Objects.equal(progress, other.progress)
				&& Objects.equal(status, other.status)
				&& Objects.equal(hasErrors, other.hasErrors);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(done, progress, status, hasErrors);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("done", done)
				.add("progress", progress)
				.add("status", status)
				.add("hasErrors", hasErrors)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Progress instance = new Progress();

		public Builder done(final boolean done) {
			instance.setDone(done);
			return this;
		}

		public Builder progress(final double progress) {
			instance.setProgress(progress);
			return this;
		}

		public Builder status(final String status) {
			instance.setStatus(status);
			return this;
		}

		public Builder hasErrors(final boolean hasErrors) {
			instance.setHasErrors(hasErrors);
			return this;
		}

		public Progress build() {
			return instance;
		}

	}

}