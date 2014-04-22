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

package eu.eubrazilcc.lvl.storage;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.net.URL;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.gravatar.Gravatar;

/**
 * Tests Gravatar access.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GravatarTest {

	@Test
	public void test() {
		System.out.println("GravatarTest.test()");
		try {
			final String email = "username@example.com";
			// test image
			Gravatar gravatar = Gravatar.builder()
					.email(email)
					.build();
			URL url = gravatar.imageUrl();
			assertThat("image URL is not null", url, notNullValue());
			assertThat("image URL is not empty", isNotBlank(url.toString()));			
			System.out.println(url.toString());
			// test JSON profile
			url = gravatar.jsonProfileUrl();
			assertThat("JSON profile URL is not null", url, notNullValue());
			assertThat("JSON profile is not empty", isNotBlank(url.toString()));			
			System.out.println(url.toString());
			// test JSON profile with callback function
			gravatar = Gravatar.builder()
					.email(email)
					.profileCallback("alert")
					.build();
			url = gravatar.jsonProfileUrl();
			assertThat("JSON profile with callback URL is not null", url, notNullValue());
			assertThat("JSON profile with callback is not empty", isNotBlank(url.toString()));			
			System.out.println(url.toString());
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("GravatarTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("GravatarTest.test() has finished");
		}
	}

}