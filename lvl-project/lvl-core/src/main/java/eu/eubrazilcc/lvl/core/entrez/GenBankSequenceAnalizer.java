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

package eu.eubrazilcc.lvl.core.entrez;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.countryFeature;
import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static org.biojava3.core.sequence.io.GenbankReaderHelper.readGenbankDNASequence;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.biojava3.core.sequence.DNASequence;

import com.google.common.collect.ImmutableList;

/**
 * Analyzes GenBank sequences.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class GenBankSequenceAnalizer {

	/**
	 * Infers the possible countries of the species from which the DNA sequence was obtained 
	 * and returns a list of Java {@link Locale}. The country is inferred from the annotations 
	 * of the GenBank file format, using the fields in the following order:
	 * <ol>
	 * <li>If a /country entry exists in the FEATURES of the file, then this is returned to 
	 * the caller and no other check is performed;</li>
	 * <li>DEFINITION;</li>
	 * <li>TITLE; or</li>
	 * <li>Check PUBMED title and abstract fields.</li>
	 * </ol>
	 * @param file sequence file.
	 * @return a Java {@link Locale} inferred from the sequence file.
	 * @throws Exception if an error occurs.
	 */
	public static ImmutableList<Locale> inferCountry(final File file) throws Exception {		
		final LinkedHashMap<String, DNASequence> dnaSequences = readGenbankDNASequence(file);		
		checkState(dnaSequences != null && !dnaSequences.isEmpty(), "No DNA sequences found");
		checkState(dnaSequences.size() == 1, "More than one DNA sequences found");
		return inferCountry(file, getOnlyElement(dnaSequences.entrySet()).getValue());
	}

	// TODO : two-letter code compatible with ISO 3166
	private static ImmutableList<Locale> inferCountry(final File file, final DNASequence sequence) throws Exception {
		final ImmutableList.Builder<Locale> builder = new ImmutableList.Builder<Locale>();		
		// infer from features
		final Locale locale = getLocale(countryFeature(file));
		if (locale != null) {
			builder.add(locale);
		} else {
			
			
			// TODO
		}
		return builder.build();
	}	

}