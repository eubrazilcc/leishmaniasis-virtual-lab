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

package eu.eubrazilcc.lvl.storage.security.shiro;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.shiro.codec.Hex.decode;
import static org.apache.shiro.util.ByteSource.Util.bytes;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Nullable;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;

/**
 * Provides functions for basic cryptography operations like password storage and key derivation. High-level functions are also provided upon those functions 
 * for generating OAuth2 secret tokens, storing users' password safely in a database, etc. The following standards and definitions are used:<br>
 * <ul>
 * <li>Key derivation uses PBKDF2, which is the NIST recommendation for password-based key derivation. However, since it's relatively cheap to implement 
 * brute-force attacks against PBKDF2 using GPUs and application-specific integrated circuit like FPGA if you want to be protected against such attacks, 
 * then you should use other alternatives. For example, the bcrypt key derivation function is slighter stronger than PBKDF2 against such attacks.</li>
 * <li>NIST recommendation for password storage is not clear, so the standard cryptographic function SHA-512 is used for this purpose, prepending 
 * a long random salt of {@link #SALT_BYTES_SIZE} bytes length to defend the password against dictionary attacks.</li>
 * </ul>
 * @see <a href="http://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-133.pdf">NIST -- Recommendation for Cryptographic Key Generation</a>
 * @see <a href="http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf">NIST -- Recommendation for Password-Based Key Derivation</a>
 * @see <a href="https://password-hashing.net/">Password Hashing Competition</a>
 * @author Erik Torres <ertorser@upv.es>
 */
public final class CryptProvider {

	/**
	 * Default hash algorithm name.
	 */
	public static final String HASH_ALGORITHM = "SHA-512";

	/**
	 * Size of the generated byte array for calls to {@link #generateSalt()}. Defaults to <tt>32</tt>, which equals 256 bits.
	 */
	public static final int SALT_BYTES_SIZE = 32;

	/**
	 * Length of 8 bytes entropy needed when producing fast cryptographic keys. 
	 */
	public static final int FAST_BYTES_SIZE = 8;

	/**
	 * Number of iterations to compute strong key deviation.
	 */
	public static final int KEY_DEVIATION_ITERATIONS = 65536;

	/**
	 * Joins the caller-provided tokens with the current date, computes a digest and returns a Base64 representation of the string produced 
	 * that can be used to protect a resource from access. If no token is provided, a short random secret of {@link #FAST_BYTES_SIZE} bytes
	 * is computed and used to generate the key.
	 * @param tokens - optional list of input tokens that (when present) are used to compute the key
	 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC2045</a>
	 * @return A Base64 representation of the computed key.
	 */
	public static String generateSecret(final String... tokens) {		
		try {
			// copy tokens
			String[] tokenArr;
			if (tokens != null && tokens.length > 0) {
				tokenArr = new String[tokens.length];
				arraycopy(tokens, 0, tokenArr, 0, tokens.length);				
			} else {
				tokenArr = new String[1];
				tokenArr[0] = new String(generateSalt(FAST_BYTES_SIZE));
			}
			// compute secret and encode it with Base64 before returning it to the caller
			final byte[] digest = digest(tokenArr, new Date[]{ new Date() });
			return encodeBase64String(digest);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to generate secret", e);
		}
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
	private static byte[] digest(final @Nullable String[] tokens, @Nullable final Date[] dates) throws IOException {
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
			final byte[] bytesOfMixName = mixName.getBytes(UTF_8.name());
			final char[] charOfMixName = new String(bytesOfMixName).toCharArray();
			final byte[] salt = generateSalt(SALT_BYTES_SIZE);
			final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			final PBEKeySpec spec = new PBEKeySpec(charOfMixName, salt, KEY_DEVIATION_ITERATIONS, SALT_BYTES_SIZE * 8);
			return factory.generateSecret(spec).getEncoded();
		} catch (Exception e) {			
			throw new IOException("Digest computation has failed", e);
		}
	}

	/**
	 * Generates a short secret from {@link #FAST_BYTES_SIZE} random bytes and returns a Base32 representation of the string produced that can 
	 * be used in URLs. The generated key will only contain the characters A-Z and 2-7.
	 * @return A URL-safe representation of the computed key.
	 * @see <a href="http://www.ietf.org/rfc/rfc4648.txt">RFC4648</a>
	 */
	public static String generateFastUrlSafeSecret() {
		return new Base32().encodeAsString(generateSalt(FAST_BYTES_SIZE));
	}	

	/**
	 * Validates a password using a {@link reference} hash computed from the original password and the provided {@link salt}. This validation
	 * compliments the methods {@link #hashAndSaltPassword(String)} and {@link #hashAndSaltPassword(String, ByteSource)}, which can be used to 
	 * protect a password.
	 * @param challenge - password to be validated
	 * @param salt - salt used to compute the original hash
	 * @param reference - hash computed from the original password
	 * @return
	 */
	public static boolean passwordsMatch(final String challenge, final String salt, final String reference) {		
		final ByteSource decodedSalt = decodeHex(salt);
		final Sha512Hash hashedChallenge = hashAndSaltPassword(challenge, decodedSalt);
		return encodeHex(hashedChallenge).equals(reference);
	}

	/**
	 * Protects a password with the default {@link #HASH_ALGORITHM}. A long random salt of length {@link #SALT_BYTES_SIZE} is computed and
	 * used to obfuscate the password. The obfuscated password generated with this method can be used to store the password in a permanent 
	 * support (e.g. in a database). The method {@link #passwordsMatch(String, String, String)} compliments this method and can be used to 
	 * validate a given password against the stored one.
	 * @param clearTextPassword - password to be protected
	 * @return An array with two elements: the first position of the array contains a Hex representation of the salt, while the second 
	 *         position contains an Hex representation of the protected key.
	 */
	public static String[] hashAndSaltPassword(final String clearTextPassword) {
		// compute a long random salt to defend against dictionary attacks
		final ByteSource salt = randomBytes(SALT_BYTES_SIZE);
		// shadow the password and encode the salt and the shadow to Hex before returning them to the caller
		final Sha512Hash shadow = hashAndSaltPassword(clearTextPassword, salt);
		return new String[]{ encodeHex(salt), encodeHex(shadow) };
	}

	/**
	 * Protects a password with the default {@link #HASH_ALGORITHM}. A long random salt of length {@link #SALT_BYTES_SIZE} is computed and
	 * used to obfuscate the password. The obfuscated password generated with this method can be used to store the password in a permanent 
	 * support (e.g. in a database). The method {@link #passwordsMatch(String, String, String)} compliments this method and can be used to 
	 * validate a given password against the stored one.
	 * @param clearTextPassword - password to be protected
	 * @param salt - salt to be used to protect the password against dictionary attacks
	 * @return an Hex representation of the protected key.
	 */
	public static String hashAndSaltPassword(final String clearTextPassword, final String salt) {
		return encodeHex(hashAndSaltPassword(clearTextPassword, decodeHex(salt)));
	}

	/**
	 * Protects a password with the default {@link #HASH_ALGORITHM} and the {@link salt} provided as parameter.
	 * @param clearTextPassword - password to be protected
	 * @param salt - salt to be used to protect the password against dictionary attacks
	 * @return An array with two elements: the first position of the array contains a Hex representation of the salt, while the second 
	 *         position contains a Hex representation of the protected key.
	 */
	private static Sha512Hash hashAndSaltPassword(final String clearTextPassword, final ByteSource salt) {
		return new Sha512Hash(clearTextPassword, salt, KEY_DEVIATION_ITERATIONS);
	}

	/**
	 * Creates a random salt of the default bytes {@link #SALT_BYTES_SIZE}.
	 * @return a random salt of the specified bytes generated with {@link #generateSalt(int)}.
	 */
	public static byte[] generateSalt() {
		return generateSalt(SALT_BYTES_SIZE);
	}

	/**
	 * Creates a random salt of the specified bytes, selected by a {@link SecureRandomNumberGenerator}. The generated salt complies the recommendations
	 * for use in password-based key derivation and for password storage protection.
	 * @param bytesSize - length used to generate the salt in bytes (often called strength)
	 * @return a random salt of the specified bytes selected by a {@link SecureRandomNumberGenerator}.
	 */
	public static byte[] generateSalt(final int bytesSize) {		
		return randomBytes(bytesSize).getBytes();
	}

	/**
	 * Creates a random salt of the specified bytes, selected by a {@link SecureRandomNumberGenerator}. The generated salt complies the recommendations
	 * for use in password-based key derivation and for password storage protection.
	 * @param bytesSize - length used to generate the salt in bytes (often called strength)
	 * @return a random salt of the specified bytes selected by a {@link SecureRandomNumberGenerator}.
	 */
	private static ByteSource randomBytes(final int bytesSize) {
		final SecureRandomNumberGenerator randomNumberGenerator = new SecureRandomNumberGenerator();
		randomNumberGenerator.setDefaultNextBytesSize(bytesSize);
		return randomNumberGenerator.nextBytes();
	}

	/**
	 * Encodes a {@link ByteSource} to a hex-encoded string representation.
	 * @param source - {@link ByteSource} to be encoded
	 * @return a hex-encoded string representation of the input source.
	 */
	private static String encodeHex(final ByteSource source) {
		return source.toHex();
	}

	/**
	 * Decodes a hex-encoded string to the corresponding {@link ByteSource} representation.
	 * @param source - hex-encoded string to be decoded
	 * @return a {@link ByteSource} representation of the input string.
	 */
	static final ByteSource decodeHex(final String source) {
		return bytes(decode(source));
	}

}