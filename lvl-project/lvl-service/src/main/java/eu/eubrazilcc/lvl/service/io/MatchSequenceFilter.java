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

package eu.eubrazilcc.lvl.service.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.DataSource;

/**
 * Implements a {@link SequenceFilter} that filters out the sequences that are not included in a list, 
 * returning to the caller the identifiers that are in the list.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MatchSequenceFilter implements SequenceFilter {

	private static final ImmutableList<String> DATA_SOURCES = of(DataSource.GENBANK);

	private ImmutableList<String> ids = of();

	public MatchSequenceFilter(final Iterable<String> ids) {
		setIds(ids);
	}
	
	public ImmutableList<String> getIds() {
		return copyOf(ids);
	}

	public void setIds(final Iterable<String> ids) {
		final ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();
		this.ids = (ids != null ? builder.addAll(ids).build() : builder.build());
	}

	@Override
	public List<String> dataSources() {
		return copyOf(DATA_SOURCES);
	}

	@Override
	public boolean canBeApplied(final String dataSource) {
		checkArgument(isNotBlank(dataSource), "Uninitialized or invalid data source");
		return DATA_SOURCES.contains(dataSource);
	}

	@Override
	public String filterById(final String id) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid identifier");
		return ids.contains(id) ? id : null;
	}	

}