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

import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.SALT_BYTES_SIZE;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.generateFastUrlSafeSecret;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.generateSalt;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.generateSecret;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.hashAndSaltPassword;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.passwordsMatch;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

/**
 * Tests secret generator.
 * @author Erik Torres <ertorser@upv.es>
 */
public class CryptProviderTest {

	@Test
	public void test() {
		System.out.println("CryptProviderTest.test()");
		try {
			// generate secret (no token provided)
			String secret = generateSecret();
			assertThat("secret is not null", secret, notNullValue());
			assertThat("secret is not empty", isNotBlank(secret));
			/* uncomment for additional output */
			System.out.println(" >> Secret (no token provided)    : " + secret);
			
			// generate secret from provided tokens
			secret = generateSecret("Hello world!");
			assertThat("secret is not null", secret, notNullValue());
			assertThat("secret is not empty", isNotBlank(secret));
			/* uncomment for additional output */
			System.out.println(" >> Secret (from provided tokens) : " + secret);

			// generate fast URL-safe secret
			secret = generateFastUrlSafeSecret();
			assertThat("fast URL-safe secret is not null", secret, notNullValue());
			assertThat("fast URL-safe secret is not empty", isNotBlank(secret));
			/* uncomment for additional output */
			System.out.println(" >> Fast URL-safe secret          : " + secret);

			// generate salt
			final byte[] salt = generateSalt(SALT_BYTES_SIZE);
			assertThat("salt is not null", salt, notNullValue());
			assertThat("salt is not empty", isNotBlank(new String(salt)));
			/* uncomment for additional output */
			System.out.println(" >> Salt length: " + salt.length);

			// protect password
			final String password = "piece of cake";
			final String[] secret2 = hashAndSaltPassword(password);
			assertThat("protected password is not null", secret2, notNullValue());
			assertThat("protected password is correct", secret2.length, equalTo(2));
			assertThat("protected password - salt is not empty", isNotBlank(secret2[0]));
			assertThat("protected password - password is not empty", isNotBlank(secret2[1]));
			/* uncomment for additional output */
			System.out.println(" >> Protected password: " + Arrays.toString(secret2));

			// validate password
			assertThat("correct password is valid", passwordsMatch(password, secret2[0], secret2[1]), equalTo(true));
			assertThat("incorrect hash is invalid", passwordsMatch(password, secret2[0], "bad hash"), equalTo(false));
			assertThat("incorrect salt is invalid", passwordsMatch(password, "0123456789abcdef", secret2[1]), equalTo(false));			
			assertThat("incorrect salt and password is invalid", passwordsMatch(password, "0123456789abcdef", "bad hash"), equalTo(false));
			assertThat("incorrect password is invalid", passwordsMatch("bad password", secret2[0], secret2[1]), equalTo(false));
			
			// invalid salt (not Hex representation)
			try {
				passwordsMatch(password, "bad salt", secret2[1]);
				throw new Exception("expected exception was not caught when no hexadecimal salt was provided");
			} catch (IllegalArgumentException ignore) { }			

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("CryptProviderTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("CryptProviderTest.test() has finished");
		}			
	}

}