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
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static eu.eubrazilcc.lvl.core.DataSource.toShortNotation;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import eu.eubrazilcc.lvl.core.DataSource.Notation;
import eu.eubrazilcc.lvl.core.Sequence;

/**
 * Utility class to work with user-supplied text (e.g. names) and convert them into
 * safe strings that can be used for example to name files in a file-system.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class NamingUtils {

	public static final String NO_NAME = "noname";
	public static final char URI_ID_SEPARATOR = ',';
	public static final char ID_FRAGMENT_SEPARATOR = ':';

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

	/**
	 * Wrapper method around the {@link NamingUtils#mergeIds(Collection, Notation)} method that uses the default notation:
	 * {@link Notation#NOTATION_SHORT}.
	 * @param sequences - the sequences to be processed to extract and merge their sequence identifiers.
	 * @return an string with the merged sequence identifier.
	 */
	public static String mergeIds(final Collection<Sequence> sequences) {
		return mergeIds(sequences, Notation.NOTATION_SHORT);
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
				final String dataSource = Notation.NOTATION_SHORT.equals(notation) ? toShortNotation(sequence.getDataSource(), Notation.NOTATION_LONG) : sequence.getDataSource();
				return dataSource + ID_FRAGMENT_SEPARATOR + sequence.getAccession();
			}
		}).filter(notNull()).toList());
	}

	public static List<String> splitIds(final String str) {
		return from(Splitter.on(URI_ID_SEPARATOR).trimResults().omitEmptyStrings().split(str)).transform(new Function<String, String>() {
			@Override
			public String apply(final String input) {			
				return input;
			}
		}).filter(notNull()).toList();
	}

}