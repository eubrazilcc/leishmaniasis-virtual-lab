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

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static eu.eubrazilcc.lvl.core.DataSource.toShortNotation;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static java.lang.String.valueOf;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BaseNCodec;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.escape.ArrayBasedEscaperMap;
import com.google.common.escape.ArrayBasedUnicodeEscaper;

import eu.eubrazilcc.lvl.core.DataSource.Notation;
import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.Sample;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.GeoJsonObject;

/**
 * Utility class to work with user-supplied text (e.g. names) and convert them into
 * safe strings that can be used for example to name files in a file-system.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://github.com/ctzen/uuid-compactor">uuid-compactor</a>
 */
public final class NamingUtils {

	public static final String NO_NAME = "noname";
	public static final char URI_ID_SEPARATOR = ',';
	public static final char ID_FRAGMENT_SEPARATOR = ':';
	public static final String ID_FRAGMENT_SEPARATOR_STRING = valueOf(ID_FRAGMENT_SEPARATOR);

	private static final long LONG_BYTE_MASK = -1 ^ (-1 << Byte.SIZE);
	private static final int NBYTES = Long.SIZE / Byte.SIZE;
	private static final Base64 BASE64 = new Base64(true);
	private static final Base32 BASE32 = new Base32();

	/**
	 * Generates a random UUID with {@link UUID#randomUUID()} and uses the method {@link #compactUUID(UUID)} to compact it.
	 * @return A random UUID string compacted as a URL-safe string of 22 characters.
	 */
	public static String compactRandomUUID() {
		return compactUUID(randomUUID());		
	}

	/**
	 * Encodes a {@link UUID} string using Base64 to produce a URL-safe string of 22 characters, resulting in a shorter
	 * representation (standard UUID strings use 36 characters).
	 * @param uuid - UUID to be compacted
	 * @return A UUID string compacted as a URL-safe string of 22 characters.
	 */
	public static String compactUUID(final UUID uuid) {
		requireNonNull(uuid, "A non-null UUID expected");
		return compact(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), BASE64, 22);
	}

	/**
	 * Generates a random UUID with {@link UUID#randomUUID()} and uses the method {@link #humanUUID(UUID)} to compact it.
	 * @return A random UUID string compacted as a URL-safe string of 26 unambiguous characters.
	 */
	public static String humanRandomUUID() {
		return humanUUID(randomUUID());
	}

	/**
	 * Encodes a {@link UUID} string using Base32 to produce a URL-safe string of 26 characters, resulting in a shorter
	 * representation (standard UUID strings use 36 characters). This method uses an unambiguous character set:
	 * <br />
	 * {@code a b c d e f g h i j k l m n o p q r s t u v w x y z 2 3 4 5 6 7}
	 * @param uuid - UUID to be compacted
	 * @return A UUID string compacted as a URL-safe string of 26 unambiguous characters.
	 * @see <a href="https://en.wikipedia.org/wiki/Base32#RFC_4648_Base32_alphabet">RFC 4648 - Base32 alphabet</a>
	 */
	public static String humanUUID(final UUID uuid) {
		requireNonNull(uuid, "A non-null UUID expected");
		return compact(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(), BASE32, 26).toLowerCase();
	}

	private static String compact(final long msb, final long lsb, final BaseNCodec codec, final int length) {
		final byte[] bytes = new byte[2 * NBYTES];
		int i = 2 * NBYTES;
		long x = lsb;
		while (i > NBYTES) {
			bytes[--i] = (byte)x;
			x >>>= Byte.SIZE;
		}
		x = msb;
		while (i > 0) {
			bytes[--i] = (byte)x;
			x >>>= Byte.SIZE;
		}
		// strips any trailing equal signs (=) from the resulting string
		return codec.encodeAsString(bytes).substring(0, length);
	}

	/**
	 * Expands a compact representation of a {@link UUID} string produced with the method {@link #compactUUID(UUID)} 
	 * to its standard representation.
	 * @param cuuid - compact UUID to be expanded
	 * @return A standard UUID.
	 */
	public static UUID expandUUID(final String cuuid) {
		final String cuuid2 = requireNonNull(trimToNull(cuuid), "A valid compact UUID expected");
		switch (cuuid2.length()) {
		case 22:
			return expand(cuuid2, BASE64);
		case 26:
			return expand(cuuid2.toUpperCase(), BASE32);
		default:
			throw new IllegalArgumentException("Not a valid compact UUID: " + cuuid2);
		}		
	}

	private static UUID expand(final String cuuid, final BaseNCodec codec) {
		final byte[] bytes = codec.decode(cuuid);
		if (bytes.length != 2 * NBYTES) {
			throw new IllegalArgumentException("A compact UUID expected but was: " + cuuid);
		}
		int i = 0;
		long msb = bytes[i++];
		while (i < NBYTES) {
			msb = msb << Byte.SIZE | (bytes[i++] & LONG_BYTE_MASK);
		}
		long lsb = bytes[i++];
		while (i < 2 * NBYTES) {
			lsb = lsb << Byte.SIZE | (bytes[i++] & LONG_BYTE_MASK);
		}
		return new UUID(msb, lsb);
	}

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
	public static String mergeSeqIds(final Collection<Sequence> sequences, final @Nullable Notation notation) {
		return Joiner.on(URI_ID_SEPARATOR).skipNulls().join(from(sequences).transform(new Function<Sequence, String>() {
			@Override
			public String apply(final Sequence sequence) {
				return toId(sequence, notation);
			}
		}).filter(notNull()).toList());
	}

	/**
	 * Iterates over a collection of samples and merges their sample identifiers. A sample identifier is composed
	 * by the collection Id and the catalog number. Those fragments are joint with the {@link NamingUtils#ID_FRAGMENT_SEPARATOR}.
	 * Sample identifiers are separated by the {@link NamingUtils#URI_ID_SEPARATOR}.
	 * @param samples - the samples to be processed to extract and merge their sample identifiers.
	 * @param notation - (optional) the notation to be used with the sample collection Id.
	 * @return an string with the merged sample identifier.
	 */
	public static String mergeSampleIds(final Collection<Sample> samples, final @Nullable Notation notation) {
		return Joiner.on(URI_ID_SEPARATOR).skipNulls().join(from(samples).transform(new Function<Sample, String>() {
			@Override
			public String apply(final Sample sample) {
				return toId(sample, notation);
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
		}).filter(notNull()).toSet().asList();
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
	 * Creates an identifier that uniquely identifies the sequence/sample in the LVL. This identifier 
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

	/**
	 * Creates an identifier that uniquely identifies the sample in the LVL. This identifier 
	 * is computed from the collection Id and the catalog number. This method uses the default
	 * character {@link NamingUtils#ID_FRAGMENT_SEPARATOR} to separate these particles in the 
	 * created identifier.
	 * @param sample - the sample to create the identifier for.
	 * @param notation - the notation to be used when creating the identifier.
	 * @return an identifier that uniquely identifies the sample in the LVL.
	 */
	public static String toId(final Sample sample, final @Nullable Notation notation) {
		return toId(sample.getCollectionId(), sample.getCatalogNumber(), notation);
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

	public static String escapeUrlPathSegment(final @Nullable String segment) {
		if (segment == null) return null;
		return new UrlPathSegmentEscaper().escape(segment);
	}

	public static String unescapeUrlPathSegment(final @Nullable String segment) {
		if (segment == null) return null;
		return new UrlPathSegmentEscaper().unescape(segment);
	}

	/**
	 * Escapes/unescapes URL path segments.
	 * @author Erik Torres <ertorser@upv.es>
	 * @see <a href="https://url.spec.whatwg.org/#syntax-url-path-segment">URL path segment</a>
	 */
	public static final class UrlPathSegmentEscaper extends ArrayBasedUnicodeEscaper {

		private static final ArrayBasedEscaperMap REPLACEMENT_MAP = ArrayBasedEscaperMap.create(createReplacementMap());

		// Replacement pattern: $g-glyph!
		private static final Pattern ESCAPED_PATTERN = Pattern.compile("(.*)(\\$g-[a-zA-Z\\-]+\\!)(.*)");

		protected UrlPathSegmentEscaper() {
			super(REPLACEMENT_MAP, 32, 126, null);
		}

		@Override
		protected char[] escapeUnsafe(final int cp) {
			return null;
		}

		public static final Map<Character, String> createReplacementMap() {
			return ImmutableMap.of('/', "$g-forward-slash!", '?', "$g-question-mark!");
		}

		public String unescape(final @Nullable String segment) {
			if (segment == null) return null;
			final StringBuilder sb = new StringBuilder();
			final Matcher matcher = ESCAPED_PATTERN.matcher(trim(segment));
			if (matcher.find()) {
				final int count = matcher.groupCount();
				final Set<Entry<Character, String>> replacementMap = createReplacementMap().entrySet();
				for (int i = 1; i <= count; i++) {					
					final String group = matcher.group(i);
					if (isNotBlank(group)) {
						final Optional<Entry<Character, String>> replacement = replacementMap.stream().filter(e -> {
							return group.contains(e.getValue());
						}).findFirst();						
						sb.append(replacement.isPresent() ? group.replace(replacement.get().getValue(), replacement.get().getKey().toString()) : group);
					}
				}
			} else sb.append(segment);
			return sb.toString();
		}

	}

}