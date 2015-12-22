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
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Objects;

import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;

/**
 * Stores a user sequence that is pending for sanitation and later approval. Therefore, this records
 * could be incomplete or inaccurate.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingSequence {

	private String namespace;          // Name space where the record is inscribed
	private String id;                 // Resource identifier	
	private SimpleDarwinRecord sample; // Sample in DWC format
	private String sequence;           // DNA sequence
	private SamplePreparation preparation;

	public PendingSequence() {
		setNamespace(LVL_DEFAULT_NS);
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;		
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;		
	}

	public SimpleDarwinRecord getSample() {
		return sample;
	}

	public void setSample(final SimpleDarwinRecord sample) {
		this.sample = sample;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(final String sequence) {
		this.sequence = sequence;
	}

	public SamplePreparation getPreparation() {
		return preparation;
	}

	public void setPreparation(final SamplePreparation preparation) {
		this.preparation = preparation;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof PendingSequence)) {
			return false;
		}
		final PendingSequence other = PendingSequence.class.cast(obj);
		return Objects.equals(namespace, other.namespace)
				&& Objects.equals(id, other.id)
				// && Objects.equals(sample, other.sample)
				&& Objects.equals(sequence, other.sequence)
				&& Objects.equals(preparation, other.preparation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(namespace, id, sequence, preparation);
	}

	@Override
	public String toString() {
		return toStringHelper(this)				
				.add("namespace", namespace)
				.add("id", id)
				.add("sample", "<<original sample is not displayed>>")
				.add("sequence", sequence)
				.add("preparation", preparation)
				.toString();
	}

	/* Fluent API */

	public static class Builder<T extends PendingSequence> {

		protected final T instance;

		public Builder(final Class<T> clazz) {
			T tmp = null;
			try {
				tmp = clazz.newInstance();
			} catch (Exception ignore) { }
			instance = tmp;
		}

		public Builder<T> namespace(final String namespace) {
			instance.setNamespace(trimToEmpty(namespace));
			return this;
		}

		public Builder<T> id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder<T> sample(final SimpleDarwinRecord sample) {
			instance.setSample(sample);
			return this;
		}

		public Builder<T> sequence(final String sequence) {
			instance.setSequence(sequence);
			return this;
		}

		public Builder<T> preparation(final SamplePreparation preparation) {
			instance.setPreparation(preparation);
			return this;
		}

		public T build() {
			return instance;
		}

	}

}