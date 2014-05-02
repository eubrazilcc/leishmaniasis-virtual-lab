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

package eu.eubrazilcc.lvl.core.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.geocoding.GeocodingHelper.geocode;
import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.google.common.collect.ImmutableMultimap;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBFeature;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBQualifier;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeqid;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.ObjectFactory;

/**
 * NCBI GenBank Sequence XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class NCBIXmlBinder extends XmlBinder {	

	private static final Class<?>[] SUPPORTED_CLASSES = {
		GBSet.class,
		GBSeq.class
	};
	
	private static JAXBContext CONTEXT;
	private static JAXBIntrospector INTROSPECTOR;
	
	static {
		try {
			// context
			CONTEXT = JAXBContext.newInstance(SUPPORTED_CLASSES, new HashMap<String, Object>());
			// introspector
			INTROSPECTOR = CONTEXT.createJAXBIntrospector();
		} catch (Exception e) { }
	}

	public static final ObjectFactory GB_SEQXML_FACTORY = new ObjectFactory();	
	
	public static final NCBIXmlBinder GB_SEQXML = new NCBIXmlBinder();
	
	private NCBIXmlBinder() {
		super(CONTEXT, INTROSPECTOR);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(GBSet.class)) {
			element = GB_SEQXML_FACTORY.createGBSet();
		} else if (clazz.equals(GBSeq.class)) {
			element = GB_SEQXML_FACTORY.createGBSeq();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}

	/**
	 * Gets the GenInfo identifier (gi) from a sequence.
	 * @param gbSeq - sequence to be analyzed
	 * @return if found, the GenInfo identifier (gi), otherwise {@code null}.
	 */
	public static final @Nullable Integer getGenInfoIdentifier(final GBSeq gbSeq) {
		checkArgument(gbSeq != null && gbSeq.getGBSeqOtherSeqids() != null && gbSeq.getGBSeqOtherSeqids().getGBSeqid() != null, 
				"Uninitialized or invalid sequence");
		String gi = null;
		final List<GBSeqid> ids = gbSeq.getGBSeqOtherSeqids().getGBSeqid();
		for (int i = 0; i < ids.size() && gi == null; i++) {
			final Pattern pattern = compile("(gi\\|\\d+)");
			final Matcher matcher = pattern.matcher(ids.get(i).getvalue());
			if (matcher.find()) {
				gi = trimToNull(matcher.group().substring(3));
			}
		}
		return isNumeric(gi) ? parseInt(gi) : null;
	}

	/**
	 * Gets the country feature from a sequence.
	 * @param sequence - sequence to be analyzed
	 * @return the value of the country feature or {@code null}.
	 */
	public static final @Nullable String countryFeature(final GBSeq sequence) {
		checkArgument(sequence != null, "Uninitialized or invalid sequence");
		String country = null;
		if (sequence.getGBSeqFeatureTable() != null && sequence.getGBSeqFeatureTable().getGBFeature() != null) {
			final List<GBFeature> features = sequence.getGBSeqFeatureTable().getGBFeature();
			for (int i = 0; i < features.size() && country == null; i++) {
				final GBFeature feature = features.get(i);
				if (feature.getGBFeatureQuals() != null && feature.getGBFeatureQuals().getGBQualifier() != null) {
					final List<GBQualifier> qualifiers = feature.getGBFeatureQuals().getGBQualifier();
					for (int j = 0; j < qualifiers.size() && country == null; j++) {
						final GBQualifier qualifier = qualifiers.get(j);
						if ("country".equals(qualifier.getGBQualifierName())) {							
							country = qualifier.getGBQualifierValue();
						}
					}
				}
			}
		}
		return trimToNull(country);
	}

	/**
	 * Infers the possible countries of the species from which the DNA sequence was obtained and 
	 * returns a map of Java {@link Locale} where the key of the map is the GenBank field that was
	 * used to infer the country. The country is inferred from the annotations of the GenBank file 
	 * format, using the fields in the following order:
	 * <ol>
	 * <li>If a country entry exists in the features of the file, then this is returned to 
	 * the caller and no other check is performed;</li>
	 * <li>Definition field;</li>
	 * <li>Title field; or</li>
	 * <li>Check PubMed title and abstract fields.</li>
	 * </ol>
	 * @param sequence - sequence to be analyzed
	 * @return a map of Java {@link Locale} inferred from the input sequence, where the key of the map
	 *         is the GenBank field used to infer the country.
	 */	
	public static final ImmutableMultimap<String, Locale> inferCountry(final GBSeq sequence) {
		checkArgument(sequence != null, "Uninitialized or invalid sequence");
		final ImmutableMultimap.Builder<String, Locale> builder = new ImmutableMultimap.Builder<String, Locale>();		
		// infer from features
		final String countryFeature = countryFeature(sequence);
		Locale locale = isNotBlank(countryFeature) ? countryFeatureToLocale(countryFeature) : null;
		if (locale != null) {
			builder.put("features", locale);
		} else {			
			// infer from definition
			// TODO

			// infer from title
			// TODO

			// infer from PubMed title and abstract fields
			// TODO
		}
		return builder.build();
	}

	/**
	 * Converts country feature to Java {@link Locale}. Java {@link Locale} allows latter to export the 
	 * country to several different formats, including a two-letter code compatible with ISO 3166-1 
	 * alpha-2 standard.
	 * @param countryFeature - value of country feature field
	 * @return a Java {@link Locale} inferred from the input sequence.
	 */
	public static final Locale countryFeatureToLocale(final String countryFeature) {
		checkArgument(isNotBlank(countryFeature), "Uninitialized or invalid country feature");
		return getLocale(countryFeature.replace(":.*", ""));
	}

	public static final Sequence parse(final GBSeq gbSeq) {
		checkArgument(gbSeq != null, "Uninitialized or invalid sequence");
		final String countryFeature = countryFeature(gbSeq);		
		return Sequence.builder()
				.dataSource(DataSource.GENBANK)
				.definition(gbSeq.getGBSeqDefinition())
				.accession(gbSeq.getGBSeqPrimaryAccession())
				.version(gbSeq.getGBSeqAccessionVersion())
				.organism(gbSeq.getGBSeqOrganism())
				.countryFeature(countryFeature)
				.location(isNotBlank(countryFeature) ? geocode(countryFeature) : null)
				.locale(isNotBlank(countryFeature) ? countryFeatureToLocale(countryFeature) : null)
				.build();
	}

}