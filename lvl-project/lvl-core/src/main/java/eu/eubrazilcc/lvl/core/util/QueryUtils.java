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

package eu.eubrazilcc.lvl.core.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.normalizeSpace;
import static org.apache.commons.lang.StringUtils.remove;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

/**
 * Utility class to help with query parameters parsing.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class QueryUtils {

	private static final char STRIKE_MARK = ' ';
	private static final String KEYWORD_SEPARATOR = ":";
	private static final String QUOTES = "\"";
	private static final Pattern KEYWORD_PATTERN = Pattern.compile("[^\\s^\"]+:[^\\s^\"]+|[^\\s^\"]+:\"\\w+[\\s|\\w]*\"");

	public static ImmutableMap<String, String> parseQuery(final String query) {
		return parseQuery(query, false);
	}

	public static ImmutableMap<String, String> parseQuery(final String query, final boolean deduplicate) {
		final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		if (isNotBlank(query)) {
			String fullText = query;
			final Matcher matcher = KEYWORD_PATTERN.matcher(query);
			while (matcher.find()) {
				final String[] keyword = extractKeyword(matcher.group());
				builder.put(keyword[0], remove(keyword[1], QUOTES));
				fullText = strikethrough(fullText, matcher.start(), matcher.end());
			}
			fullText = deduplicate ? normalize(fullText) : normalizeSpace(fullText);
			if (isNotBlank(fullText)) {
				builder.put("text", remove(fullText, QUOTES));
			}
		}
		return builder.build();
	}

	private static String[] extractKeyword(final String str) {
		checkArgument(isNotBlank(str), "Invalid or empty input string");
		final String[] tokens = str.split(KEYWORD_SEPARATOR, 2);
		checkState(tokens.length == 2, "Invalid keyword search term");
		return tokens;
	}

	private static String strikethrough(final String str, final int start, final int end) {
		checkArgument(str != null, "Invalid input string");
		checkArgument(start >= 0, "Invalid start index");
		checkArgument(end <= str.length(), "Invalid end index");
		char[] copy = str.toCharArray();
		for (int i = start; i < end; i++) {
			copy[i] = STRIKE_MARK;
		}		
		return new String(copy);
	}

	private static String normalize(final String str) {
		checkArgument(str != null, "Invalid input string");
		return new LinkedHashSet<String>(Arrays.asList(normalizeSpace(str).split(" "))).toString().replaceAll("(^\\[|\\]$)", "").replace(", ", " ");
	}

}