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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.google.common.base.Objects;

/**
 * Sorting information.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Sorting {

	private String field;
	private Order order;

	public Sorting() {
		setOrder(Order.ASC);
	}

	public String getField() {
		return field;
	}

	public void setField(final String field) {
		this.field = field;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(final Order order) {
		this.order = order;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Sorting)) {
			return false;
		}
		final Sorting other = Sorting.class.cast(obj);
		return Objects.equal(field, other.field)
				&& Objects.equal(order, other.order);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(field, order);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("field", field)
				.add("order", order)				
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Sorting instance = new Sorting();

		public Builder field(final String field) {
			checkArgument(isNotBlank(field), "Uninitialized or invalid field");
			instance.setField(field);
			return this;
		}

		public Builder order(final Order order) {
			checkArgument(order != null, "Uninitialized order");
			instance.setOrder(order);
			return this;
		}

		public Sorting build() {
			return instance;
		}

	}

	/**
	 * Sorting order.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static enum Order {
		ASC,
		DESC
	}

}