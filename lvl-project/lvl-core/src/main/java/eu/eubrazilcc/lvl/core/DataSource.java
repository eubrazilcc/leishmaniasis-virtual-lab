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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * This class stores data sources as {@link String}, which is an alternative to BioJava 
 * {@link org.biojava3.core.sequence.DataSource}. This is more convenient than BioJava to exchange 
 * information to a client application where BioJava is not available (for example, a Web application).
 * @author Erik Torres <ertorser@upv.es>
 */
public final class DataSource {

	/* Long or full notation */

	public static final String GENBANK = "GenBank";

	/* Short notation */

	public static final String GENBANK_SHORT = "gb";

	/**
	 * Converts from the specified notation to short notation.
	 * @param dataSource - the data source to be converted.
	 * @param fromNotation - the original notation of the data source.
	 * @return the short version of the specified data source. If the data source cannot be converted, an {@link Exception} 
	 *         will be thrown to the caller.
	 */
	public static String toShortNotation(final String dataSource, final Notation fromNotation) {
		checkArgument(isNotBlank(dataSource), "Uninitialized or invalid data source");
		checkArgument(fromNotation != null, "Uninitialized notation");
		final String trimmedDataSource = dataSource.trim();
		String shortenedDataSource = null;
		switch (fromNotation) {
		case NOTATION_LONG:
			if (GENBANK.equalsIgnoreCase(trimmedDataSource)) {
				shortenedDataSource = GENBANK_SHORT;
			}
			break;
		default:
			break;
		}
		checkState(isNotBlank(shortenedDataSource), "The specified data source cannot be found");
		return shortenedDataSource;
	}

	/**
	 * Converts from the specified notation to long notation.
	 * @param dataSource - the data source to be converted.
	 * @param fromNotation - the original notation of the data source.
	 * @return the long version of the specified data source. If the data source cannot be converted, an {@link Exception} 
	 *         will be thrown to the caller.
	 */
	public static String toLongNotation(final String dataSource, final Notation fromNotation) {
		checkArgument(isNotBlank(dataSource), "Uninitialized or invalid data source");
		checkArgument(fromNotation != null, "Uninitialized notation");
		final String trimmedDataSource = dataSource.trim();
		String expandedDataSource = null;
		switch (fromNotation) {
		case NOTATION_SHORT:
			if (GENBANK_SHORT.equalsIgnoreCase(trimmedDataSource)) {
				expandedDataSource = GENBANK;
			}
			break;
		default:
			break;
		}
		checkState(isNotBlank(expandedDataSource), "The specified data source cannot be found");
		return expandedDataSource;
	}

	/**
	 * Data source notation.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static enum Notation {
		NOTATION_LONG,
		NOTATION_SHORT
	}

}