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
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.geocoding.GeocodingHelper.geocode;
import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMultimap;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBFeature;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBQualifier;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBReference;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeqReferences;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeqid;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.ObjectFactory;

/**
 * NCBI GenBank Sequence XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class GbSeqXmlBinder extends XmlBinder {	

	private final static Logger LOGGER = getLogger(GbSeqXmlBinder.class);

	public static final String SUBMITTER_BLOCK_TITLE = "Direct Submission";

	private static final Class<?>[] SUPPORTED_CLASSES = {
		GBSet.class,
		GBSeq.class
	};

	public static final ObjectFactory GBSEQ_XML_FACTORY = new ObjectFactory();	

	public static final GbSeqXmlBinder GBSEQ_XMLB = new GbSeqXmlBinder();

	private GbSeqXmlBinder() {
		super(SUPPORTED_CLASSES);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(GBSet.class)) {
			element = GBSEQ_XML_FACTORY.createGBSet();
		} else if (clazz.equals(GBSeq.class)) {
			element = GBSEQ_XML_FACTORY.createGBSeq();
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

	/**
	 * Parses publication references from a GenBank entry. The submitter block as well as non-PubMed publications
	 * are excluded from the list of references returned by this method. A message will be written to the log system
	 * when a reference is excluded for any of the reasons mentioned before.
	 * @param sequence - GenBank sequence entry
	 * @return a list of {@link Reference} that contains the references to published work included in the 
	 *         input GenBank sequence that are included PubMed (has a valid PMID).
	 */
	public static final List<Reference> getPubMedReferences(final GBSeq sequence) {
		checkArgument(sequence != null, "Uninitialized or invalid sequence");
		final List<Reference> references = newArrayList();
		final GBSeqReferences gbRefs = sequence.getGBSeqReferences();
		if (gbRefs != null && gbRefs.getGBReference() != null) {
			final List<GBReference> list = gbRefs.getGBReference();
			for (final GBReference gbRef : list) {
				if (gbRef != null) {
					if (gbRef.getGBReferencePubmed() != null) {
						references.add(Reference.builder()
								.title(gbRef.getGBReferenceTitle())
								.pubmedId(gbRef.getGBReferencePubmed())
								.build());						
					} else if (SUBMITTER_BLOCK_TITLE.equals(gbRef.getGBReferenceTitle())) {						
						LOGGER.trace("Ignoring submitter block in GenBank sequence: " + sequenceId(sequence));						
					} else {
						LOGGER.info("Ignoring non-PubMed publication in GenBank sequence: " + sequenceId(sequence));						
					}					
				}
			}
		}
		return references;
	}

	/**
	 * Parses publication references from a GenBank entry. The submitter block as well as non-PubMed publications
	 * are excluded from the list of references returned by this method. A message will be written to the log system
	 * when a reference is excluded for any of the reasons mentioned before.
	 * @param sequence - GenBank sequence entry
	 * @return a list of {@link Reference} that contains the references to published work included in the 
	 *         input GenBank sequence that are included PubMed (has a valid PMID).
	 */
	public static final Set<String> getPubMedIds(final GBSeq sequence) {
		checkArgument(sequence != null, "Uninitialized or invalid sequence");
		return from(getPubMedReferences(sequence)).transform(new Function<Reference, String>() {
			@Override
			public String apply(final Reference reference) {
				return reference != null && isNotBlank(reference.getPubmedId()) ? reference.getPubmedId().trim() : null;				
			}			
		}).filter(notNull()).toSet();
	}

	/**
	 * Parses DNA sequence from a GenBank entry.
	 * @param gbSeq - GenBank sequence entry
	 * @return a {@link Sequence} built from the input GenBank sequence.
	 */
	public static final Sequence parseSequence(final GBSeq gbSeq) {
		checkArgument(gbSeq != null, "Uninitialized or invalid sequence");
		final String countryFeature = countryFeature(gbSeq);		
		return Sequence.builder()
				.dataSource(DataSource.GENBANK)
				.definition(gbSeq.getGBSeqDefinition())
				.accession(gbSeq.getGBSeqPrimaryAccession())
				.version(gbSeq.getGBSeqAccessionVersion())
				.gi(getGenInfoIdentifier(gbSeq))
				.organism(gbSeq.getGBSeqOrganism())
				.countryFeature(countryFeature)
				.location(isNotBlank(countryFeature) ? geocode(countryFeature).orNull() : null)
				.locale(isNotBlank(countryFeature) ? countryFeatureToLocale(countryFeature) : null)
				.build();
	}
	
	public static String sequenceId(final GBSeq gbSeq) {
		return "AC:" + gbSeq.getGBSeqPrimaryAccession() + ", GI:" + getGenInfoIdentifier(gbSeq);
	}

}