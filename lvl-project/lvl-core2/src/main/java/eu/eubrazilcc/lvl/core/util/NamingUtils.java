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

import static java.lang.String.valueOf;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.UnsupportedEncodingException;

/**
 * Utility class to work with user-supplied text (e.g. names) and convert them into
 * safe strings that can be used for example to name files in a file-system.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class NamingUtils {

	public static final String NO_NAME = "noname";
	public static final char URI_ID_SEPARATOR = ',';
	public static final char ID_FRAGMENT_SEPARATOR = ':';
	public static final String ID_FRAGMENT_SEPARATOR_STRING = valueOf(ID_FRAGMENT_SEPARATOR);

	/**
	 * Replaces non-printable Unicode characters from an specified name, producing a 
	 * short representation of the name that does not contains any spaces. The produced
	 * name can be used for example to name files in a file-system.
	 * @param name - the name to be converted.
	 * @return an ASCII printable representation.
	 */
	public static String toAsciiSafeName(final String name) {
		final String safeName = isNotBlank(name) ? name : "";
		return defaultIfEmpty(safeName.trim().replaceAll("\\p{C}", "?")
				.replaceAll("\\W+", "_").replaceFirst("[_]+$", "").replaceFirst("^[_]+", "")
				.toLowerCase(), NO_NAME);
	}

	public static String urlEncodeUtf8(final String str) {
		String encoded = str;
		try {
			encoded = encode(str, UTF_8.name());
		} catch (UnsupportedEncodingException ignore) { }
		return encoded;
	}

	public static String urlDecodeUtf8(final String str) {
		String decoded = str;
		try {
			decoded = decode(str, UTF_8.name());
		} catch (UnsupportedEncodingException ignore) { }
		return decoded;
	}

}