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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Locale;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;

/**
 * Utilities to deal with language.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class LocaleUtils {

	/** Unmodifiable list of available locales **/
	private static ImmutableList<Locale> availableLocaleList = null; // lazily created by availableLocaleList()

	/**
	 * Obtains an unmodifiable list of installed locales. This method is a wrapper around
	 * {@link Locale#getAvailableLocales()}, which caches the locales and uses generics.
	 * @return country names.
	 */
	public static ImmutableList<Locale> availableLocaleList() {
		if (availableLocaleList == null) {
			synchronized (LocaleUtils.class) {
				if (availableLocaleList == null) {
					final ImmutableList.Builder<Locale> builder = new ImmutableList.Builder<Locale>();
					final String[] locales = Locale.getISOCountries();
					for (final String countryCode : locales) {
						builder.add(new Locale("", countryCode));			
					}
					availableLocaleList = builder.build();
				}
			}
		}
		return availableLocaleList;	
	}

	/**
	 * Obtains the locale that best-matches with the specified string.
	 * @param str input string.
	 * @return the locale that best-matches with the specified string, or {@code null}.
	 */
	public static @Nullable Locale getLocale(final @Nullable String str) {
		Locale locale = null;
		if (isNotBlank(str)) {
			final ImmutableList<Locale> locales = availableLocaleList();
			for (int i = 0; i < locales.size() && locale == null; i++) {				
				final Locale item = locales.get(i);
				if (StringUtils.containsIgnoreCase(str, item.getDisplayCountry())) {
					locale = item;
				}
			}			
		}
		return locale;
	}

}