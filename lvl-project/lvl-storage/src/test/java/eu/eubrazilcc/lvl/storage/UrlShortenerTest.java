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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationFinder.DEFAULT_LOCATION;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.storage.urlshortener.UrlShortener.shortenUrl;
import static eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.TEST_CONFIG_ROOT;
import static java.io.File.separator;
import static org.apache.commons.io.FileUtils.toURLs;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.urlshortener.UrlShortener;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.ConditionalIgnore;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Tests URL shortener {@link UrlShortener}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class UrlShortenerTest {

	private static boolean hasToClean = false;

	@Rule
	public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

	@BeforeClass
	public static void setUp() throws IOException {
		final File file = new File(concat(DEFAULT_LOCATION, TEST_CONFIG_ROOT + separator + "etc" + separator + REST_SERVICE_CONFIG));
		if (file.canRead()) {
			final List<URL> urls = newArrayList(getDefaultConfiguration());
			for (final ListIterator<URL> it = urls.listIterator(); it.hasNext();) {
				final URL url = it.next();
				if (url.getPath().endsWith(REST_SERVICE_CONFIG)) {
					it.remove();
					it.add(toURLs(new File[]{ file })[0]);
				}
			}
			CONFIG_MANAGER.setup(urls);
			hasToClean = true;
		}		
	}

	@AfterClass
	public static void cleanUp() throws IOException {
		if (hasToClean) {
			CONFIG_MANAGER.setup(null);
		}
	}

	@Test
	@ConditionalIgnore(condition=GoogleAPIkeyIsDefined.class)
	public void test() {
		System.out.println("UrlShortenerTest.test()");
		try {
			final String[] urls = { "https://www.google.com", "https://localhost" };

			// test shorten URL
			for (final String url : urls) {
				final String shortened = shortenUrl(url);
				assertThat("shortened URL is not null", shortened, notNullValue());
				assertThat("shortened URL is not empty", isNotBlank(shortened));
				System.out.println(" >> Long: " + url + ", shortened: " + shortened);
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("UrlShortenerTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("UrlShortenerTest.test() has finished");
		}
	}

	/**
	 * Checks whether a Google API key is available in the configuration.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public class GoogleAPIkeyIsDefined implements IgnoreCondition {
		@Override
		public boolean isSatisfied() {		
			return isBlank(CONFIG_MANAGER.getGoogleAPIKey());
		}
	}

}