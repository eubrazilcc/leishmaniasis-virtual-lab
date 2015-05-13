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
import static com.google.common.collect.ComparisonChain.start;

import static java.util.Collections.reverse;
import static java.util.Collections.sort;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Provides basic statistics about an object.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SimpleStat implements Comparable<SimpleStat> {

	private String label;
	private int value;

	public String getLabel() {
		return label;
	}
	public void setLabel(final String label) {
		this.label = label;
	}
	public int getValue() {
		return value;
	}
	public void setValue(final int value) {
		this.value = value;
	}

	@Override
	public int compareTo(final SimpleStat other) {
		if (this == other) return 0;
		return start()
				.compare(this.value, other.value)
				.compare(this.label, other.label)
				.result();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SimpleStat)) {
			return false;
		}
		final SimpleStat other = SimpleStat.class.cast(obj);
		return Objects.equals(value, other.value)
				&& Objects.equals(label, other.label);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, label);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("value", value)
				.add("label", label)
				.toString();
	}

	public static List<SimpleStat> normalizeStats(final List<SimpleStat> stats) {
		List<SimpleStat> normalized = null;
		if (stats != null) {
			normalized = newArrayList(stats);
			sort(normalized);
			reverse(normalized);
			
			if (normalized.size() > 10) {
				int value = 0;
				for (int i = 10; i < normalized.size(); i++) {
					value += normalized.get(i).getValue();
				}
				normalized.get(10).setValue(value);
				normalized.get(10).setLabel("Other");
				if (normalized.size() > 11) {
					normalized.subList(11, normalized.size()).clear();
				}
			}
		} else {
			normalized = stats;
		}
		return normalized;
	}
	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SimpleStat instance = new SimpleStat();

		public Builder value(final int value) {
			instance.setValue(value);
			return this;
		}

		public Builder label(final String label) {
			instance.setLabel(label);
			return this;
		}

		public SimpleStat build() {
			return instance;
		}

	}


}