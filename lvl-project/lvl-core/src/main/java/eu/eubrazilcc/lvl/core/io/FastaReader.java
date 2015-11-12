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

package eu.eubrazilcc.lvl.core.io;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads a sequence from a file in FASTA format.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.ncbi.nlm.nih.gov/books/NBK21097/#A631">The NCBI Handbook, Chapter 16: Appendix 1 - FASTA identifiers</a>
 */
public class FastaReader {

	private static final int MAX_LINE_LENGTH = Integer.MAX_VALUE;

	public static final Pattern COMMENT = compile("^[>|;].*");
	public static final Pattern NA_CODE = compile("^[ACGTURYKMSWBDHVNX\\-]+$", CASE_INSENSITIVE);
	public static final Pattern AA_CODE = compile("^[A-Z\\*\\-]+$", CASE_INSENSITIVE);

	public static String[] readFasta(final File file) throws IOException {
		final List<String> sequences = newArrayList();		
		StringBuilder sb = null;
		String type = null, last = null;
		try (final Scanner scanner = new Scanner(file)) {
			scanner.useLocale(Locale.US);
			while (scanner.hasNextLine()) {
				final String line = scanner.nextLine();
				if (COMMENT.matcher(line).matches()) {
					// process headers
					if ("fragment".equals(last) && sb != null) {
						sequences.add(sb.toString());						
					}
					sb = null;
					last = "comment";
				} else {
					// process sequences
					if (type == null) type = getType(line);
					if (isNotBlank(line)) {
						checkState(line.length() <= MAX_LINE_LENGTH && checkType(line, type));
						if (sb == null) sb = new StringBuilder();
						sb.append(line);
					}
					last = "fragment";
				}
			}
		}
		if ("fragment".equals(last) && sb != null) {
			sequences.add(sb.toString());						
		}
		return sequences.toArray(new String[sequences.size()]);
	}

	private static String getType(final String line) {
		if (NA_CODE.matcher(line).matches()) {
			return "NA";
		} else if (AA_CODE.matcher(line).matches()) {
			return "AA";
		}
		throw new IllegalStateException("Unknown type");
	}

	private static boolean checkType(final String line, final String type) {
		if ("NA".equals(type)) {
			return NA_CODE.matcher(line).matches();
		} else if ("AA".equals(type)) {
			return AA_CODE.matcher(line).matches();
		}
		throw new IllegalStateException("Unsupported type: " + type);
	}

}