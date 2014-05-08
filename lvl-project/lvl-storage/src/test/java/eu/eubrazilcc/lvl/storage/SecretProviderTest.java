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

import static eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider.generateFastUrlSafeSecret;
import static eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider.generateSalt;
import static eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider.generateSecret;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider;

/**
 * Tests secret provider.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SecretProviderTest {

	@Test
	public void test() {
		System.out.println("SecretProviderTest.test()");
		try {
			// generate secret
			String secret = generateSecret("Hello world!");
			assertThat("secret is not null", secret, notNullValue());
			assertThat("secret is not empty", isNotBlank(secret));
			/* uncomment for additional output */
			System.out.println(" >> Secret: " + secret);
			
			// generate fast URL-safe secret
			secret = generateFastUrlSafeSecret();
			assertThat("fast URL-safe secret is not null", secret, notNullValue());
			assertThat("fast URL-safe secret is not empty", isNotBlank(secret));
			/* uncomment for additional output */
			System.out.println(" >> Fast URL-safe secret: " + secret);
			
			// generate salt
			final byte[] salt = generateSalt(SecretProvider.DEFAULT_STRENGTH);
			assertThat("salt is not null", salt, notNullValue());
			assertThat("salt is not empty", isNotBlank(new String(salt)));
			/* uncomment for additional output */
			System.out.println(" >> Salt: " + salt);
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SecretProviderTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SecretProviderTest.test() has finished");
		}			
	}

}