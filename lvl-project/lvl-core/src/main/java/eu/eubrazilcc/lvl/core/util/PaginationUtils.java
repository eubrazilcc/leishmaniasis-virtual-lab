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

package eu.eubrazilcc.lvl.core.util;

import static eu.eubrazilcc.lvl.core.util.NumberUtils.roundUp;

/**
 * Utility class to help with collection pagination.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class PaginationUtils {

	/**
	 * Computes the position of the first entry of a page.
	 * @param page - current page
	 * @param perPage - number of entries per page
	 * @return The position of the first entry of the specified page.
	 */
	public static int firstEntryOf(final int page, final int perPage) {
		if (page > 0 && perPage > 0) {
			return page * perPage;
		}
		return 0;
	}
	
	/**
	 * Computes the total number of pages needed to display a collection of items
	 * with a fixed number of elements per page.
	 * @param totalEntries - total number of items in the collection to be displayed
	 * @param perPage - maximum number of items per page
	 * @return The total number of pages needed to display the collection.
	 */
	public static int totalPages(final int totalEntries, final int perPage) {		
		return roundUp(totalEntries, perPage);
	}
	
}