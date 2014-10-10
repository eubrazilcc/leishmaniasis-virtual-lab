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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;
import static eu.eubrazilcc.lvl.core.DataSource.toShortNotation;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static java.lang.String.valueOf;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import eu.eubrazilcc.lvl.core.DataSource.Notation;
import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.GeoJsonObject;

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

	public static <T extends GeoJsonObject> String mergeIds(final Collection<Localizable<T>> items) {
		return Joiner.on(URI_ID_SEPARATOR).skipNulls().join(from(items).transform(new Function<Localizable<T>, String>() {
			@Override
			public String apply(final Localizable<T> item) {
				return item.getTag().trim();
			}
		}).filter(notNull()).toList());
	}

	/**
	 * Iterates over a collection of sequences and merges their sequence identifiers. A sequence identifier is composed
	 * by the sequence data source and the sequence accession number. Those fragments are joint with the {@link NamingUtils#ID_FRAGMENT_SEPARATOR}.
	 * Sequence identifiers are separated by the {@link NamingUtils#URI_ID_SEPARATOR}.
	 * @param sequences - the sequences to be processed to extract and merge their sequence identifiers.
	 * @param notation - (optional) the notation to be used with the sequence data source.
	 * @return an string with the merged sequence identifier.
	 */
	public static String mergeIds(final Collection<Sequence> sequences, final @Nullable Notation notation) {
		return Joiner.on(URI_ID_SEPARATOR).skipNulls().join(from(sequences).transform(new Function<Sequence, String>() {
			@Override
			public String apply(final Sequence sequence) {
				return toId(sequence, notation);
			}
		}).filter(notNull()).toList());
	}

	/**
	 * Extracts the sequence identifiers contained in the specified string.
	 * @param str - the string to split.
	 * @return a list with the split sequence identifiers.
	 */
	public static List<String> splitIds(final String str) {
		return from(Splitter.on(URI_ID_SEPARATOR).trimResults().omitEmptyStrings().split(str)).transform(new Function<String, String>() {
			@Override
			public String apply(final String input) {			
				return input;
			}
		}).filter(notNull()).toList();
	}

	/**
	 * Creates an identifier that uniquely identifies the sequence in the LVL. This identifier 
	 * is computed from the data source and the accession fields. This method uses the default
	 * character {@link NamingUtils#ID_FRAGMENT_SEPARATOR} to separate these particles in the 
	 * created identifier.
	 * @param sequence - the sequence to create the identifier for.
	 * @param notation - the notation to be used when creating the identifier.
	 * @return an identifier that uniquely identifies the sequence in the LVL.
	 */
	public static String toId(final Sequence sequence, final @Nullable Notation notation) {
		return toId(sequence.getDataSource(), sequence.getAccession(), notation);
	}

	/**
	 * Creates an identifier that uniquely identifies the sequence in the LVL. This identifier 
	 * is computed from the data source and the accession fields. This method uses the default
	 * character {@link NamingUtils#ID_FRAGMENT_SEPARATOR} to separate these particles in the 
	 * created identifier.
	 * @param dataSource - the data source to use.
	 * @param accession - the accession number to use.
	 * @param notation - the notation to be used when creating the identifier.
	 * @return an identifier that uniquely identifies the sequence in the LVL.
	 */
	public static String toId(final String dataSource, final String accession, final @Nullable Notation notation) {
		final String dataSource2 = NOTATION_SHORT.equals(notation) ? toShortNotation(dataSource, NOTATION_LONG) : dataSource;
		return dataSource2 + ID_FRAGMENT_SEPARATOR + accession;
	}

	public static String encodePublicLinkPath(final String path) {
		return path != null ? path.replaceAll("/", ID_FRAGMENT_SEPARATOR_STRING) : path;
	}

	public static String decodePublicLinkPath(final String path) {
		return path != null ? path.replaceAll(ID_FRAGMENT_SEPARATOR_STRING, "/") : path;
	}

	public static String[] parsePublicLinkId(final String id) {
		final String[] arr = new String[2];
		try {
			final String decoded = decode(id, UTF_8.name());
			final Iterable<String> splitted = Splitter.on(ID_FRAGMENT_SEPARATOR)
					.trimResults()
					.omitEmptyStrings()
					.limit(2)
					.split(decoded);
			arr[0] = getFirst(splitted, null);
			arr[1] = getLast(splitted, null);
		} catch (UnsupportedEncodingException ignore) { }
		return arr;
	}

}