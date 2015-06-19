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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

/**
 * Collection filter.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Filter {

	private String fieldName;	
	private String value;
	private FilterType type;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(final String fieldName) {
		this.fieldName = fieldName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}	

	public FilterType getType() {
		return type;
	}

	public void setType(final FilterType type) {
		this.type = type;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Filter)) {
			return false;
		}
		final Filter other = Filter.class.cast(obj);
		return Objects.equals(fieldName, other.fieldName)
				&& Objects.equals(value, other.value)
				&& Objects.equals(type, other.type);		
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldName, value, type);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("fieldName", fieldName)				
				.add("value", value)
				.add("type", type)
				.toString();
	}

	/* Inner classes */	

	public static enum FilterType {
		FILTER_COMPARE,
		FILTER_NOT,
		FILTER_REGEX,
		FILTER_TEXT
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final Filter instance = new Filter();		

		public Builder fieldName(final String fieldName) {
			instance.setFieldName(fieldName);			
			return this;
		}

		public Builder value(final String value) {
			instance.setValue(value);			
			return this;
		}

		public Builder type(final FilterType type) {
			instance.setType(type);			
			return this;
		}

		public Filter build() {
			return instance;
		}

	}

}