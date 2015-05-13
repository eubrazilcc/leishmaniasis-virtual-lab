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

package eu.eubrazilcc.lvl.storage;

/**
 * Provides common patterns for resource identifiers.
 * @author Erik Torres <ertorser@upv.es>
 */
public interface ResourceIdPattern {

	/**
	 * ASCII set of characters that can be used to sent an URL over the Internet, as defined in the RFC 1738.
	 * @see <a href="https://www.ietf.org/rfc/rfc1738.txt">RFC 1738: Uniform Resource Locators (URL)</a>
	 * @see <a href="http://en.wikipedia.org/wiki/ASCII#ASCII_printable_characters">ASCII printable characters</a>
	 */
	public static final String US_ASCII_PRINTABLE_PATTERN = "[\\u0021-\\u007E]+";

	/**
	 * Subset of {@link #US_ASCII_PRINTABLE_PATTERN} excluding the path separator (<tt>/</tt>).
	 */
	public static final String URL_FRAGMENT_PATTERN = "[\\u0021-\\u002E\\u0030-\\u007E]+";

	public static final String SEQUENCE_ID_PATTERN = "[a-zA-Z_0-9]+(:|%3A)[a-zA-Z_0-9]+";
	public static final String CITATION_ID_PATTERN = "[0-9]+";	

	public static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

}