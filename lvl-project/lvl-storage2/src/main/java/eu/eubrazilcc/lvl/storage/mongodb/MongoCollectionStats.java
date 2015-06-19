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

package eu.eubrazilcc.lvl.storage.mongodb;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Objects;

/**
 * Collection statistics.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MongoCollectionStats {

	private String name;
	private long count;
	private List<String> indexes;

	public MongoCollectionStats() { }

	public MongoCollectionStats(final String name) {
		this.name = name;
		this.count = 0l;
		this.indexes = newArrayList();	
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public long getCount() {
		return count;
	}

	public void setCount(final long count) {
		this.count = count;
	}

	public List<String> getIndexes() {
		return indexes;
	}

	public void setIndexes(final List<String> indexes) {
		this.indexes = indexes;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof MongoCollectionStats)) {
			return false;
		}
		final MongoCollectionStats other = MongoCollectionStats.class.cast(obj);
		return Objects.equals(name, other.name)
				&& Objects.equals(count, other.count)
				&& Objects.equals(indexes, other.indexes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, count, indexes);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("name", name)
				.add("count", count)
				.add("indexes", indexes)
				.toString();
	}

}