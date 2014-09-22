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

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ComparisonChain;

/**
 * An immutable pair consisting of two elements. Unlikely the implementation provided by Apache Commons,
 * this class is final and uses helper methods from Google Guava.
 * @author Erik Torres <ertorser@upv.es>
 * @param <L> the left element type
 * @param <R> the right element type
 */
public final class ImmutablePair<L extends Comparable<L>, R extends Comparable<R>> implements Map.Entry<L, R>,
Comparable<ImmutablePair<L, R>> {

	public final L left;
	public final R right;

	public ImmutablePair(final L left, final R right) {
		super();
		this.left = left;
		this.right = right;
	}	

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public L getKey() {
		return getLeft();
	}

	@Override
	public R getValue() {
		return getRight();
	}

	/**
	 * Throws {@code UnsupportedOperationException} since this pair is immutable and therefore this operation is not supported.
	 * @param value - the value to set
	 * @return never
	 * @throws UnsupportedOperationException as this operation is not supported
	 */
	@Override
	public R setValue(final R value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(final ImmutablePair<L, R> other) {
		return ComparisonChain.start()				
				.compare(left, other.left)
				.compare(right, other.right)
				.result();		
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof ImmutablePair)) {
			return false;
		}
		final ImmutablePair<?, ?> other = ImmutablePair.class.cast(obj);
		return Objects.equals(left, other.left)
				&& Objects.equals(right, other.right);
	}

	@Override
	public int hashCode() {
		return Objects.hash(left, right);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("left", left)
				.add("right", right)				
				.toString();
	}	

	/* Fluent API */

	public static <L extends Comparable<L>, R extends Comparable<R>> ImmutablePair<L, R> of(final L left, final R right) {
		return new ImmutablePair<L, R>(left, right);
	}

}