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

package eu.eubrazilcc.lvl.service;

import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.CITATION_ID_PATTERN;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.SEQUENCE_ID_PATTERN;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.URL_FRAGMENT_PATTERN;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.US_ASCII_PRINTABLE_PATTERN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern;

/**
 * Tests {@link ResourceIdentifierPattern}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ResourceIdentifierPatternTest {

	@Test
	public void test() {
		System.out.println("ResourceIdentifierPatternTest.test()");
		try {
			// test US ASCII printable pattern
			String[] input = new String[]{ "user1%40lvl", "my_fasta_sequences.zip", "user1%40lvl/my_fasta_sequences.zip", "" };
			boolean[] result = new boolean[]{ true, true, true, false };
			doTest(input, result, US_ASCII_PRINTABLE_PATTERN);			

			// test URL fragment pattern			
			input = new String[]{ "user1%40lvl", "my_fasta_sequences.zip", "user1%40lvl/my_fasta_sequences.zip", "" };
			result = new boolean[]{ true, true, false, false };
			doTest(input, result, URL_FRAGMENT_PATTERN);

			// test sequence Id pattern			
			input = new String[]{ "abcde123:fg456hi", "abcde123%3Afg456hi" };
			result = new boolean[]{ true, true };
			doTest(input, result, SEQUENCE_ID_PATTERN);

			// test citation Id pattern			
			input = new String[]{ "1234" };
			result = new boolean[]{ true };
			doTest(input, result, CITATION_ID_PATTERN);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ResourceIdentifierPatternTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ResourceIdentifierPatternTest.test() has finished");
		}
	}
	
	private void doTest(final String[] input, final boolean[] result, final String patternStr) {
		final Pattern pattern = Pattern.compile(patternStr);
		for (int i = 0; i < input.length; i++) {
			final Matcher matcher = pattern.matcher(input[i]);
			assertThat("pattern '" + patternStr + "', matcher '" + input[i] + "'", matcher.matches(), equalTo(result[i]));
		}
	}

}