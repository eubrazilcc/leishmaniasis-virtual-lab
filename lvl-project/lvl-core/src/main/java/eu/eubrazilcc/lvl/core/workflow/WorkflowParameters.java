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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.eubrazilcc.lvl.core.Pair;

/**
 * Contains workflow parameters.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowParameters {

	private Map<String, List<Pair<String, String>>> parameters = new Hashtable<String, List<Pair<String, String>>>();

	public Map<String, List<Pair<String, String>>> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, List<Pair<String, String>>> parameters) {
		if (parameters != null) {
			this.parameters = parameters;
		} else {
			this.parameters = new Hashtable<String, List<Pair<String, String>>>();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof WorkflowParameters)) {
			return false;
		}
		final WorkflowParameters other = WorkflowParameters.class.cast(obj);
		return Objects.equals(parameters, other.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameters);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("parameters", parameters)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final WorkflowParameters instance = new WorkflowParameters();

		public Builder parameter(final String block, final String parameter, final String value) {
			final List<Pair<String, String>> pairs = instance.getParameters().containsKey(block) ? instance.getParameters().get(block) 
					: new ArrayList<Pair<String,String>>();			
			pairs.add(Pair.of(parameter, value));		
			instance.getParameters().put(block, pairs);			
			return this;
		}

		public Builder parameters(final Map<String, List<Pair<String, String>>> parameters) {
			instance.setParameters(parameters);
			return this;
		}

		public WorkflowParameters build() {
			return instance;
		}

	}

}