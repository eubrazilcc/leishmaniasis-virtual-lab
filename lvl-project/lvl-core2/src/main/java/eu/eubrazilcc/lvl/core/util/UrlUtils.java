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

package eu.eubrazilcc.lvl.core.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

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
	 * @param url - the source URL
	 * @return the protocol of the URL. In case that no protocol is found, {@link #FILE}
	 *         is assumed.
	 */
	public static String extractProtocol(final URL url) {
		checkArgument(url != null, "Uninitialized URL");
		String protocol = url.getProtocol();
		if (isBlank(protocol)) {
			protocol = FILE;
		}
		return protocol;
	}

	/**
	 * Checks whether the specified URL points to an object accessible through one of the 
	 * remote protocols supported by this application.
	 * @param url - input URL
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
	 * @param url - input URL
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
	 * @param str - String representation of an URL
	 * @return an URL.
	 * @throws IOException If an input/output error occurs.
	 */
	public static @Nullable URL parseURL(final String str) throws MalformedURLException {
		URL url = null;
		if (isNotBlank(str)) {
			try {
				url = new URL(str);
			} catch (MalformedURLException e) {
				url = null;
				if (!str.matches("^[a-zA-Z]+[/]*:[^\\\\]")) {
					// convert path to UNIX path
					String path = separatorsToUnix(str.trim());
					final Pattern pattern = Pattern.compile("^([a-zA-Z]:/)");
					final Matcher matcher = pattern.matcher(path);
					path = matcher.replaceFirst("/");
					// convert relative paths to absolute paths
					if (!path.startsWith("/")) {
						path = path.startsWith("~") ? path.replaceFirst("~", System.getProperty("user.home"))
								: concat(System.getProperty("user.dir"), path);
					}
					// normalize path
					path = normalize(path, true);
					if (isNotBlank(path)) {
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
	 * @param str - input URL
	 * @param strict when is set to {@code true}, an additional check is performed 
	 *        to verify that the URL is formatted strictly according to RFC2396.
	 * @return {@code true} if the specified URL is valid. Otherwise, {@code false}.
	 */
	public static boolean isValid(final String str, final boolean strict) {
		URL url = null;
		if (isNotBlank(str)) {
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

	/**
	 * Gets the path part of a link.
	 * @param link - input link
	 * @return the path part of the specified link or an empty string if one does not exist.
	 * @throws IllegalArgumentException If a malformed link occurs.
	 */
	public static String getPath(final Link link) {
		checkArgument(link != null, "Uninitialized link");
		return getPath(link.getUri());
	}

	/**
	 * Gets the path part of an URI.
	 * @param uri - input URI
	 * @return the path part of the specified URI or an empty string if one does not exist.
	 * @throws IllegalArgumentException If a malformed URI occurs.
	 */
	public static String getPath(final URI uri) {
		checkArgument(uri != null, "Uninitialized URI");
		try {
			final URI nUri = uri.normalize();
			final URL url = nUri.isAbsolute() ? nUri.toURL() : new URL(new URL("http://example.org/"), nUri.getPath());
			return url.getPath();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Malformed URI", e);
		}
	}

	/**
	 * Returns a list of name-value pairs as built from the link's query portion.
	 * @param link - input link
	 * @return a list of name-value pairs as built from the link's query portion.
	 */
	public static Map<String, String> getQueryParams(final Link link) {
		checkArgument(link != null, "Uninitialized link");
		return getQueryParams(link.getUri());
	}

	/**
	 * Returns a list of name-value pairs as built from the URI's query portion.
	 * @param uri - input URI
	 * @return a list of name-value pairs as built from the URI's query portion.
	 * @throws IllegalArgumentException If a malformed URI occurs.
	 */
	public static Map<String, String> getQueryParams(final URI uri) {
		checkArgument(uri != null, "Uninitialized URI");
		try {
			final URI nUri = uri.normalize();
			final URL url = nUri.isAbsolute() ? nUri.toURL() : new URL(new URL("http://example.org/"), nUri.toString());
			final String query = new URL(URLDecoder.decode(url.toString(), defaultCharset().name())).getQuery();
			final Map<String, String> map = newHashMap();
			final List<NameValuePair> pairs = URLEncodedUtils.parse(query, defaultCharset());
			final Iterator<NameValuePair> iterator = pairs.iterator();
			while (iterator.hasNext()) {
				final NameValuePair pair = iterator.next();
				map.put(pair.getName(), pair.getValue());
			}
			return map;
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Malformed URI", e);			
		}
	}

}