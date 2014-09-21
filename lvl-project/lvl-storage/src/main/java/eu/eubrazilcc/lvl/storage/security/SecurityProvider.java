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

package eu.eubrazilcc.lvl.storage.security;

import static java.lang.System.arraycopy;
import static java.security.MessageDigest.getInstance;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Date;

import javax.annotation.Nullable;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base32;

/**
 * Provides functions for basic security operations like password storage and key derivation. High-level functions are also provided upon those functions 
 * for generating OAuth2 secret tokens, storing users' password safely in a database, etc. The following standards and definitions are used:<br>
 * <ul>
 * <li>Key derivation uses PBKDF2, which is the NIST recommendation for password-based key derivation. However, since it's relatively cheap to implement 
 * brute-force attacks against PBKDF2 using GPUs and application-specific integrated circuit like FPGA if you want to be protected against such attacks, 
 * then you should use other alternatives. For example, the bcrypt key derivation function is slighter stronger than PBKDF2 against such attacks.</li>
 * <li>NIST recommendation for password storage is not clear, so the standard cryptographic function SHA-256 is used for this purpose, prepending 
 * a long random salt of {@link #DEFAULT_STRENGTH} bits length to defend the password against dictionary attacks.</li>
 * </ul>
 * @see <a href="http://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-133.pdf">NIST -- Recommendation for Cryptographic Key Generation</a>
 * @see <a href="http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf">NIST -- Recommendation for Password-Based Key Derivation</a>
 * @see <a href="https://password-hashing.net/">Password Hashing Competition</a>
 * @author Erik Torres <ertorser@upv.es>
 */
public final class SecurityProvider {

	/**
	 * Length of 256 bits entropy needed when producing strong cryptographic keys.
	 */
	public static final int DEFAULT_STRENGTH = 256;

	/**
	 * Length of 8 bits entropy needed when producing fast cryptographic keys.
	 */
	public static final int FAST_STRENGTH = 8;

	/**
	 * Number of iterations to compute strong key deviation.
	 */
	public static final int KEY_DEVIATION_ITERATIONS = 65536;

	/**
	 * Joins the caller-provided tokens with the current date, computes a digest and returns a Base64 representation of the string produced 
	 * that can be used to protect a resource from access.
	 * @param tokens - optional list of input tokens that (when present) are used to compute the key
	 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC2045</a>
	 * @return A Base64 representation of the computed key.
	 */
	public static String generateSecret(final String... tokens) {		
		try {
			// copy tokens
			final int numTokens = (tokens != null ? tokens.length : 0);
			final String[] tokenArr = new String[numTokens];
			if (numTokens > 0) {
				arraycopy(tokens, 0, tokenArr, 0, numTokens);
			}			
			// compute secret and encode it with Base64 before returning it to the caller
			final byte[] digest = digest(tokenArr, new Date[]{ new Date() });
			return encodeBase64String(digest);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to generate secret", e);
		}
	}

	/**
	 * Generates a short salt from {@link #FAST_STRENGTH} random bytes and returns a Base32 representation of the string produced that can 
	 * be used in URLs. The generated key will only contain the characters A-Z and 2-7.
	 * @return A URL-safe representation of the computed key.
	 * @see <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC4648</a>
	 */
	public static String generateFastUrlSafeSecret() {
		return new Base32().encodeAsString(generateSalt(FAST_STRENGTH));
	}

	/**
	 * Creates a random salt of the specified bytes, selected by a {@link SecureRandom} that can be passed as input parameter for password-based 
	 * key derivation and for password storage protection.
	 * @param strength - length used to generate the salt in bytes.
	 * @return a random salt of the specified bytes selected by a {@link SecureRandom}.
	 */
	public static byte[] generateSalt(final int strength) {
		final byte[] salt = new byte[strength];
		new SecureRandom().nextBytes(salt);		
		return salt;
	}

	/**
	 * Joins the tokens and dates provided as parameters with a salt generated from {@link #DEFAULT_STRENGTH} random bytes and computes a digest using 
	 * the PBKDF2 key derivation function.
	 * @param tokens - list of input tokens
	 * @param dates - list of input dates
	 * @return a digest of the input parameters (tokens and dates).
	 * @throws IOException is thrown if the computation of the digest fails.
	 * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html">JCA Standard Algorithm Name Documentation</a>
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
			final char[] charOfMixName = new String(bytesOfMixName).toCharArray();
			final byte[] salt = generateSalt(DEFAULT_STRENGTH);
			final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			final PBEKeySpec spec = new PBEKeySpec(charOfMixName, salt, KEY_DEVIATION_ITERATIONS, DEFAULT_STRENGTH);
			return factory.generateSecret(spec).getEncoded();
		} catch (Exception e) {			
			throw new IOException("Digest computation has failed", e);
		}
	}

	/**
	 * Protects a password with the method {@link #obfuscatePassword(String)}. A long random salt of length {@link #DEFAULT_STRENGTH} is computed and
	 * passed as argument to that method. The obfuscated password generated with this method can be used to store the password in a permanent support 
	 * (e.g. in a database). The method {@link #validatePassword(String, String, String)} compliments this method and can be used to validate a given 
	 * password against the stored one.
	 * @param password - password to be protected
	 * @return An array with two elements: the first position of the array contains a Base64 
	 *        representation of the salt, while the second position contains a Base64 representation
	 *        of the protected key.
	 */
	public static String[] obfuscatePassword(final String password) {
		// compute a long random salt to defend against dictionary attacks
		final String salt = encodeBase64String(generateSalt(DEFAULT_STRENGTH));
		// shadow the password and encode it with Base64 before returning it to the caller
		final String shadow = computeHash(password, salt);
		return new String[]{ salt, shadow };
	}

	/**
	 * Validates a password using a hash computed from the original password and the provided salt. This validation compliments the method 
	 * {@link #obfuscatePassword(String)}, which is used to protect a password.
	 * @param password - password to be validated
	 * @param salt - salt used to compute the original hash
	 * @param hash - hash computed from the original password
	 * @return {@code true} if the password matches, otherwise {@code false}.
	 */
	public static boolean validatePassword(final String password, final String salt, final String hash) {
		// compare the hash of the given password with the hash from the caller
		final String shadow = computeHash(password, salt);
		return shadow.equals(hash);
	}

	/**
	 * Prepends the provided salt to the password and computes a hash from the obtained mix using the standard cryptographic function SHA-256.
	 * Passwords obfuscated in this way can be stored in a permanent support (e.g. a database). The computed hash is coded in Base64 before 
	 * returning it to the caller.
	 * @param password - password to be obfuscated
	 * @param salt - salt used to protect the password against dictionary attacks
	 * @return a Base64 representation of the hash computed from the specified password and the specified salt.
	 */
	public static String computeHash(final String password, final String salt) {
		try {
			// prepend the salt to the password and hash the obtained mix using a standard cryptographic function
			final String mixName = salt.concat(password);
			final byte[] bytesOfMixName = mixName.getBytes("UTF-8");			
			final MessageDigest md = getInstance("SHA-256");
			final byte[] digest = md.digest(bytesOfMixName);				
			// encode the hashed password with Base64 before returning it to the caller
			return encodeBase64String(digest);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to compute the hash", e);
		}
	}

}