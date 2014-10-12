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

import java.util.Objects;

/**
 * Workflow product.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowProduct {

	private String path;

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WorkflowProduct)) {
			return false;
		}
		final WorkflowProduct other = WorkflowProduct.class.cast(obj);
		return Objects.equals(path, other.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}

	@Override
	public String toString() {
		return toStringHelper(this).add("path", path).toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final WorkflowProduct instance = new WorkflowProduct();

		public Builder path(final String path) {
			instance.setPath(path);
			return this;
		}

		public WorkflowProduct build() {
			return instance;
		}

	}

}