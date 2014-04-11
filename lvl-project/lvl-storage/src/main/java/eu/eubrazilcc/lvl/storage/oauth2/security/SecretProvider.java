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

package eu.eubrazilcc.lvl.storage.oauth2.security;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

/**
 * Provides secret token generation.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class SecretProvider {	

	public static final int DEFAULT_STRENGTH = 256;

	/**
	 * Joins a salt generated from {@link #DEFAULT_STRENGTH} random bytes with the current date, 
	 * computes a digest and returns a Base64 representation of the string produced that can be 
	 * used to protect a resource from access.
	 * @param tokens - optional list of input tokens that (when present) are used to compute the key
	 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC2045</a>
	 * @return A Base64 representation of the computed key.
	 */
	public static String generateSecret(final String... tokens) {		
		try {
			// copy tokens
			final int numTokens = (tokens != null ? tokens.length : 0);
			final String[] tokenArr = new String[numTokens + 1];
			if (numTokens > 0) {
				System.arraycopy(tokens, 0, tokenArr, 0, numTokens);
			}
			// add random salt to defend against dictionary attacks
			tokenArr[tokenArr.length - 1] = new String(generateSalt(DEFAULT_STRENGTH));
			// compute secret and encode it with Base64 before returning to the caller
			final byte[] digest = digest(tokenArr, new Date[]{ new Date() });
			return new String(Base64.encodeBase64(digest, false, false));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to generate secret", e);
		}
	}

	/**
	 * Generates a short salt from 8 random bytes and returns a Base32 representation of the 
	 * string produced that can be used in URLs. The generated key will only contain the 
	 * characters A-Z and 2-7.
	 * @return A URL-safe representation of the computed key.
	 * @see <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC4648</a>
	 */
	public static String generateFastUrlSafeSecret() {
		return new Base32().encodeAsString(SecretProvider.generateSalt(8));
	}

	/**
	 * Creates a random salt of the specified bytes, selected by a {@link SecureRandom} that can 
	 * be passed as input parameter to a function that hashes a password to protect the generated 
	 * password against dictionary attacks.
	 * @param strength - length used to generate the salt in bytes.
	 * @return a random salt of the specified bytes selected by a {@link SecureRandom}.
	 */
	public static byte[] generateSalt(final int strength) {
		final byte[] salt = new byte[strength];
		new SecureRandom().nextBytes(salt);		
		return salt;
	}

	/**
	 * Joins the tokens and dates provided as parameters and computes a digest using SHA-256.
	 * @param tokens - list of input tokens
	 * @param dates - list of input dates
	 * @return a SHA-256 digest of the input parameters (tokens and dates).
	 * @throws IOException is thrown if the computation of the digest fails.
	 * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest">MessageDigest Algorithms</a>
	 */
	public static byte[] digest(final @Nullable String[] tokens, @Nullable final Date[] dates) throws IOException {
		try {
			String mixName = "";
			if (tokens != null) {
				for (final String token : tokens) {
					mixName += (token != null ? token.trim() : "");					
				}
			}
			if (dates != null) {
				for (final Date date : dates) {
					mixName += (date != null ? Long.toString(date.getTime()) : "");
				}
			}
			// compute digest
			final byte[] bytesOfMixName = mixName.getBytes("UTF-8");
			final MessageDigest md = MessageDigest.getInstance("SHA-256");
			return md.digest(bytesOfMixName);			
		} catch (Exception e) {			
			throw new IOException("Digest computation has failed", e);
		}
	}

}