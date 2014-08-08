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

import static com.google.common.base.Objects.toStringHelper;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Any collection that can be returned to the client as a series of pages that
 * contains a part of the collection. Include JAXB annotations to serialize this 
 * class to XML and JSON. Most JSON processing libraries like Jackson support 
 * these JAXB annotations.
 * @author Erik Torres <ertorser@upv.es>
 */
@XmlRootElement
public class Paginable {

	private Link previous;
	private Link next;

	private Link first;
	private Link last;

	private int totalCount;

	public Paginable() { }

	public void push(final Paginable other) {
		this.previous = other.previous;
		this.next = other.next;
		this.first = other.first;
		this.last = other.last;
		this.totalCount = other.totalCount;
	}

	@XmlElement(name="previous")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	public @Nullable Link getPrevious() {
		return previous;
	}

	public void setPrevious(final Link previous) {
		this.previous = previous;
	}

	@XmlElement(name="next")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	public @Nullable Link getNext() {
		return next;
	}

	public void setNext(final Link next) {
		this.next = next;
	}

	@XmlElement(name="first")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	public @Nullable Link getFirst() {
		return first;
	}

	public void setFirst(final Link first) {
		this.first = first;
	}

	@XmlElement(name="last")
	@XmlJavaTypeAdapter(JaxbAdapter.class)
	public @Nullable Link getLast() {
		return last;
	}

	public void setLast(final Link last) {
		this.last = last;
	}	

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(final int totalCount) {
		this.totalCount = totalCount;
	}

	@Override
	public String toString() {
		return toStringHelper(Paginable.class.getSimpleName())
				.add("previous", previous)
				.add("next", next)
				.add("first", first)
				.add("last", last)
				.add("totalCount", totalCount)				
				.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Paginable paginable;

		public Builder() {
			paginable = new Paginable();
		}

		public Builder previous(final Link previous) {
			paginable.setPrevious(previous);
			return this;
		}

		public Builder next(final Link next) {
			paginable.setNext(next);
			return this;
		}

		public Builder first(final Link first) {
			paginable.setFirst(first);
			return this;
		}

		public Builder last(final Link last) {
			paginable.setLast(last);
			return this;
		}

		public Builder totalCount(final int totalCount) {
			paginable.setTotalCount(totalCount);
			return this;
		}

		public Paginable build() {
			return paginable;
		}

	}

}