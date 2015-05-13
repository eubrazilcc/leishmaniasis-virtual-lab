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

package eu.eubrazilcc.lvl.service.io.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.of;
import static eu.eubrazilcc.lvl.core.DataSource.PUBMED;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.REFERENCE_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableList;

/**
 * Implements a {@link RecordFilter} that filters out the references that are already stored 
 * in the application's database, returning to the caller the identifiers that are missing 
 * in the application's database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NewReferenceFilter extends RecordFilter {

	private static final ImmutableList<String> DATA_SOURCES = of(PUBMED);

	public NewReferenceFilter() {
		super(DATA_SOURCES);
	}

	@Override
	public String filterById(final String id) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid identifier");	
		return REFERENCE_DAO.find(id) == null ? id : null;
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final NewReferenceFilter instance = new NewReferenceFilter();

		public NewReferenceFilter build() {
			return instance;
		}

	}

}