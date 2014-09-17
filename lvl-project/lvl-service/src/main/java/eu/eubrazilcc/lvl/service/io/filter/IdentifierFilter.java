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

package eu.eubrazilcc.lvl.service.io.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableList;

/**
 * Implements a {@link RecordFilter} that filters out the records that are not included in a list
 * of identifiers provided by the caller, returning to the caller the identifiers that are present 
 * in the list.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class IdentifierFilter extends RecordFilter {
	
	private ImmutableList<String> ids = of();

	public IdentifierFilter(final ImmutableList<String> dataSources, final Iterable<String> ids) {
		super(dataSources);
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
	public String filterById(final String id) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid identifier");
		return ids.contains(id) ? id : null;
	}	

}