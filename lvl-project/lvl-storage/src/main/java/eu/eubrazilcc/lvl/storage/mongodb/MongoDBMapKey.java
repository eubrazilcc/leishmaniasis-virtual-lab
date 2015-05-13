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

package eu.eubrazilcc.lvl.storage.mongodb;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang.StringEscapeUtils.unescapeJava;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Provides a key compatible with mongoDB field names by escaping the restricted characters with the Unicode full width equivalents:
 * U+FF04 (i.e. "＄") and U+FF0E (i.e. "．").
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://docs.mongodb.org/manual/reference/limits/#Restrictions-on-Field-Names">mongoDB Restrictions on Field Names</a>
 * <a href="http://docs.mongodb.org/manual/faq/developers/#dollar-sign-operator-escaping">Dollar Sign Operator Escaping</a>
 */
public class MongoDBMapKey {

	private static final Pattern DOLLAR_PATTERN = compile(quote("$"));
	private static final Pattern DOT_PATTERN = compile(quote("."));

	private static final String DOLLAR_REPLACEMENT = quoteReplacement("\\") + "uff04";
	private static final String DOT_REPLACEMENT = quoteReplacement("\\") + "uff0e";

	private static final Pattern UDOLLAR_PATTERN = compile(quote("\uff04"));
	private static final Pattern UDOT_PATTERN = compile(quote("\uff0e"));

	private static final String UDOLLAR_REPLACEMENT = quoteReplacement("$");
	private static final String UDOT_REPLACEMENT = quoteReplacement(".");

	private String key;

	public MongoDBMapKey() {
	}

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	@JsonIgnore
	public String getUnescapedKey() {
		return unescapeFieldName(key);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof MongoDBMapKey)) {
			return false;
		}
		final MongoDBMapKey other = MongoDBMapKey.class.cast(obj);
		return Objects.equals(key, other.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("key", key)
				.toString();
	}

	public static MongoDBMapKey escapeMapKey(final String name) {
		final MongoDBMapKey instance = new MongoDBMapKey();
		instance.setKey(escapeFieldName(name));
		return instance;
	}

	public static String escapeFieldName(final String name) {
		String name2 = null;
		checkArgument(isNotBlank(name2 = trimToNull(name)), "Uninitialized or invalid field name");		
		String escaped = DOLLAR_PATTERN.matcher(name2).replaceAll(DOLLAR_REPLACEMENT);
		escaped = DOT_PATTERN.matcher(escaped).replaceAll(DOT_REPLACEMENT);
		return unescapeJava(escaped);
	}

	public static String unescapeFieldName(final String name) {
		String name2 = null;
		checkArgument(isNotBlank(name2 = trimToNull(name)), "Uninitialized or invalid field name");
		String unescaped = UDOLLAR_PATTERN.matcher(name2).replaceAll(UDOLLAR_REPLACEMENT);
		unescaped = UDOT_PATTERN.matcher(unescaped).replaceAll(UDOT_REPLACEMENT);
		return unescaped;
	}

}