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
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

/**
 * Filters out the records that are passed by the caller, returning the identifiers that match the filtering 
 * criteria implemented in the filter method.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class RecordFilter {

	private final ImmutableList<String> dataSources;

	public RecordFilter(final ImmutableList<String> dataSources) {
		this.dataSources = copyOf(dataSources);
	}

	/**
	 * Returns the list of data sources to which the filter can be applied to filter the records stored
	 * in this data source.
	 * @return the list of data sources to which the filter can be applied.
	 */
	public List<String> dataSources() {
		return copyOf(dataSources);
	}

	/**
	 * Checks whether or not the filter can be applied to the specified data source to filter the 
	 * records stored in this data source.
	 * @param dataSource - the data source to be checked
	 * @return {@code true} if the filter can be applied to filter the records of the specified data
	 *         source, otherwise {@code false}.
	 */
	public boolean canBeApplied(final String dataSource) {
		checkArgument(isNotBlank(dataSource), "Uninitialized or invalid data source");
		return dataSources.contains(dataSource);
	}	

	/**
	 * Returns the input identifier or {@code null}, depending on whether the filter applies to
	 * the specified identifier or not.
	 * @param id - record identifier to be filtered
	 * @return the input identifier or {@code null}, depending on whether the filter applies to
	 *         the specified identifier or not.
	 */
	public abstract @Nullable String filterById(String id);

}