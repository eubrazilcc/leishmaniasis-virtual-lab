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
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.LeishmaniaSample;
import eu.eubrazilcc.lvl.core.Sample;
import eu.eubrazilcc.lvl.core.SandflySample;
import eu.eubrazilcc.lvl.storage.SampleKey;
import eu.eubrazilcc.lvl.storage.dao.SampleDAO;

/**
 * Implements a {@link RecordFilter} that filters out the samples that are already stored 
 * in the application's database, returning to the caller the identifiers that are missing 
 * in the application's database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NewSampleFilter<T extends Sample> extends RecordFilter {

	private static final ImmutableList<String> DATA_SOURCES = of(CLIOC, COLFLEB);

	private final String collectionId;
	private final SampleDAO<T> dao;

	public NewSampleFilter(final String collectionId, final SampleDAO<T> dao) {
		super(DATA_SOURCES);
		this.collectionId = collectionId;
		this.dao = dao;
	}

	@Override
	public String filterById(final String id) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid identifier");
		return dao.find(SampleKey.builder()
				.collectionId(collectionId)
				.catalogNumber(id)
				.build()) == null ? id : null;
	}

	/* Fluent API */

	public static Builder<LeishmaniaSample>leishmaniaBuilder() {
		return new Builder<LeishmaniaSample>();
	}

	public static Builder<SandflySample> sandflyBuilder() {
		return new Builder<SandflySample>();
	}

	public static class Builder<T extends Sample> {

		private String collectionId;
		private SampleDAO<T> dao;		

		public Builder<T> collectionId(final String collectionId) {
			this.collectionId = collectionId;
			return this;
		}

		public Builder<T> dao(final SampleDAO<T> dao) {
			this.dao = dao;
			return this;
		}

		public NewSampleFilter<T> build() {
			checkArgument(dao != null, "Uninitialized or invalid sequence DAO");
			return new NewSampleFilter<T>(collectionId, dao);
		}

	}

}