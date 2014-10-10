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
 * Stores a pair consisting of two elements.
 * @author Erik Torres <ertorser@upv.es>
 * @param <L> the left element type
 * @param <R> the right element type
 */
public class Pair<L extends Comparable<L>, R extends Comparable<R>> implements Map.Entry<L, R>, Comparable<Pair<L, R>> {

	public L left;
	public R right;

	public Pair() { }

	public Pair(final L left, final R right) {
		setLeft(left);
		setRight(right);		
	}

	public L getLeft() {
		return left;
	}

	public void setLeft(final L left) {
		this.left = left;
	}

	public R getRight() {
		return right;
	}

	public void setRight(final R right) {
		this.right = right;
	}

	@Override
	public L getKey() {
		return getLeft();
	}

	public void setKey(final L key) {
		setLeft(key);
	}

	@Override
	public R getValue() {
		return getRight();
	}

	public R setValue(final R value) {
		setRight(value);
		return getRight();
	}

	@Override
	public int compareTo(final Pair<L, R> other) {
		return ComparisonChain.start()				
				.compare(left, other.left)
				.compare(right, other.right)
				.result();		
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof  Pair)) {
			return false;
		}
		final  Pair<?, ?> other =  Pair.class.cast(obj);
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

	public static <L extends Comparable<L>, R extends Comparable<R>>  Pair<L, R> of(final L left, final R right) {
		return new  Pair<L, R>(left, right);
	}

}