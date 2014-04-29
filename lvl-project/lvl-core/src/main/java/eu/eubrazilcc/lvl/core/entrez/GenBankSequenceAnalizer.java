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

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.countryFeature;
import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static org.apache.commons.io.FileUtils.listFiles;

import java.io.File;
import java.util.Collection;
import java.util.Locale;

import com.google.common.collect.ImmutableMultimap;

/**
 * Analyzes GenBank sequences.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class GenBankSequenceAnalizer {

	/**
	 * Lists the GenBank sequences found in the specified directory (subdirectories are not searched).
	 * @param directory - the directory to search for sequences in
	 * @return
	 */
	public static Collection<File> listGBFlatFiles(final File directory) {
		checkArgument(directory != null && directory.isDirectory() && directory.canRead(), 
				"Uninitialized or invalid directory");
		return listFiles(directory, new String[] { "gb" }, false);
	}
	
	public static Collection<File> listGBSeqXMLFiles(final File directory) {
		checkArgument(directory != null && directory.isDirectory() && directory.canRead(), 
				"Uninitialized or invalid directory");
		return listFiles(directory, new String[] { "xml" }, false);
	}

	/**
	 * Infers the possible countries of the species from which the DNA sequence was obtained and 
	 * returns a map of Java {@link Locale} where the key of the map is the GenBank field that was
	 * used to infer the country. The country is inferred from the annotations of the GenBank file 
	 * format, using the fields in the following order:
	 * <ol>
	 * <li>If a /country entry exists in the FEATURES of the file, then this is returned to 
	 * the caller and no other check is performed;</li>
	 * <li>DEFINITION;</li>
	 * <li>TITLE; or</li>
	 * <li>Check PUBMED title and abstract fields.</li>
	 * </ol>
	 * Java {@link Locale} allows latter to export the country to several different formats, including a 
	 * two-letter code compatible with ISO 3166-1 alpha-2 standard.
	 * @param file - sequence file.
	 * @return a Java {@link Locale} inferred from the sequence file.
	 * @throws Exception if an error occurs.
	 */	
	public static final ImmutableMultimap<GenBankField, Locale> inferCountry(final File file) throws Exception {
		checkArgument(file != null && file.canRead(), "Uninitialized or invalid file");
		final ImmutableMultimap.Builder<GenBankField, Locale> builder = new ImmutableMultimap.Builder<GenBankField, Locale>();		
		// infer from features
		final Locale locale = getLocale(countryFeature(file));
		if (locale != null) {
			builder.put(GenBankField.COUNTRY_FEATURE, locale);
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
	 * Represents a GenBank field, for example, the field that was used to infer the country of a sequence.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static enum GenBankField {		
		COUNTRY_FEATURE,
		DEFINITION,
		TITLE,
		PUBMED_TITLE,
		PUBMED_ABSTRACT		
	}

}