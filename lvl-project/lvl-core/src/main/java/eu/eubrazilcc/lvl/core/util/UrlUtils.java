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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * URL manipulation utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class UrlUtils {

	public static final String FILE = "file";
	public static final String HTTP = "http";
	public static final String HTTPS = "https";

	/**
	 * Extracts the protocol from an URL.
	 * @param url the source URL.
	 * @return the protocol of the URL. In case that no protocol is found, {@link #FILE}
	 *         is assumed.
	 */
	public static String extractProtocol(final URL url) {
		checkArgument(url != null, "Uninitialized URL");
		String protocol = url.getProtocol();
		if (StringUtils.isBlank(protocol)) {
			protocol = FILE;
		}
		return protocol;
	}
	
	/**
	 * Checks whether the specified URL points to an object accessible through one of the 
	 * remote protocols supported by this application.
	 * @param url input URL.
	 * @return {@code true} if the specified URL points to an object accessible through one 
	 *         of the remote protocols supported by this application, otherwise {@code false}.
	 */
	public static boolean isRemoteProtocol(final URL url) {
		checkArgument(url != null, "Uninitialized URL");
		final String protocol = extractProtocol(url);
		return protocol.equals(HTTP) || protocol.equals(HTTPS);
	}

	/**
	 * Checks whether the specified URL points to a file in the local file-system.
	 * @param url input URL.
	 * @return {@code true} if the specified URL points to a file, otherwise {@code false}.
	 */
	public static boolean isFileProtocol(final URL url) {
		checkArgument(url != null, "Uninitialized URL");
		final String protocol = extractProtocol(url);
		return protocol.equals(FILE);
	}

	/**
	 * Parses a URL from a String. This method supports file-system paths 
	 * (e.g. /foo/bar).
	 * @param str String representation of an URL.
	 * @return an URL.
	 * @throws IOException If an input/output error occurs.
	 */
	public static URL parseURL(final String str) throws MalformedURLException {
		URL url = null;
		if (StringUtils.isNotBlank(str)) {
			try {
				url = new URL(str);
			} catch (MalformedURLException e) {
				url = null;
				if (!str.matches("^[a-zA-Z]+[/]*:[^\\\\]")) {
					// convert path to UNIX path
					String path = FilenameUtils.separatorsToUnix(str.trim());
					final Pattern pattern = Pattern.compile("^([a-zA-Z]:/)");
					final Matcher matcher = pattern.matcher(path);
					path = matcher.replaceFirst("/");
					// convert relative paths to absolute paths
					if (!path.startsWith("/")) {
						path = path.startsWith("~") ? path.replaceFirst("~", System.getProperty("user.home"))
								: FilenameUtils.concat(System.getProperty("user.dir"), path);
					}
					// normalize path
					path = FilenameUtils.normalize(path, true);
					if (StringUtils.isNotBlank(path)) {
						url = new File(path).toURI().toURL();
					} else {
						throw new MalformedURLException("Invalid path: " + path);
					}
				} else {
					throw e;
				}
			}
		}
		return url;
	}
	
	/**
	 * Checks whether or not a URL is valid.
	 * @param str input URL.
	 * @param strict when is set to {@code true}, an additional check is performed 
	 *        to verify that the URL is formatted strictly according to RFC2396.
	 * @return {@code true} if the specified URL is valid. Otherwise, {@code false}.
	 */
	public static boolean isValid(final String str, final boolean strict) {
		URL url = null;
		if (StringUtils.isNotBlank(str)) {
			try {
				url = new URL(str);
				if (strict) {
					url.toURI();
				}
			} catch (MalformedURLException | URISyntaxException e) {
				url = null;
			}			
		}
		return url != null;
	}

}