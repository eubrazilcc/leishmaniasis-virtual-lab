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

package eu.eubrazilcc.lvl.storage.urlshortener;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.http.TrustedHttpsClient;

/**
 * Converts long URLs into short ones using the Google URL Shortener API.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://developers.google.com/url-shortener/v1/getting_started">URL Shortener API - Getting Started</a>
 */
public class UrlShortener {

	private static final Logger LOGGER = getLogger(UrlShortener.class);

	public static final int MAX_CACHED_ELEMENTS = 2000;
	public static final String URL_SHORTENER = "https://www.googleapis.com/urlshortener/v1/url";	

	private static final LoadingCache<String, String> CACHE = newBuilder()
			.maximumSize(MAX_CACHED_ELEMENTS)
			.build(new CacheLoader<String, String>() {
				@Override
				public String load(final String key) throws Exception {
					return loadShortenedUrl(key);
				}				
			});

	public static final String shortenUrl(final String url) {
		String url2 = null;
		checkArgument(isNotBlank(url2 = trimToNull(url)), "Uninitialized or invalid url");
		String shortenedUrl = null;
		try {
			shortenedUrl = CACHE.get(normalizeUrl(url2));
		} catch (Exception e) {
			LOGGER.error("Failed to shorten URL: " + url2, e);
		}
		return shortenedUrl;
	}

	private static final String normalizeUrl(final String url) {
		String normlizedUrl = url;
		try {
			final URI uri = new URI(url).normalize();
			normlizedUrl = uri.toURL().toString();
		} catch (Exception e) {
			LOGGER.error("Failed to normalize URL: " + url, e);
		}
		return normlizedUrl;
	}

	private static final String loadShortenedUrl(final String url) throws Exception {
		String url2 = null, shortenedUrl = null;
		checkArgument(isNotBlank(url2 = trimToNull(url)), "Uninitialized or invalid url");
		try (final TrustedHttpsClient httpClient = new TrustedHttpsClient()) {
			final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					final int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						final HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}					                    
				}
			};
			final String response = httpClient.executePost(URL_SHORTENER + (isNotBlank(CONFIG_MANAGER.getGoogleAPIKey()) ? "key=" + urlEncodeUtf8(CONFIG_MANAGER.getGoogleAPIKey()) : ""), 					
					ImmutableMap.of("Accept", "application/json", "Content-type", "application/json"),
					new StringEntity("{ \"longUrl\" : \"" + url2 + "\" }"),
					responseHandler);
			final Map<String, String> map = JSON_MAPPER.readValue(response, new TypeReference<HashMap<String,String>>() {});
			checkState(map != null, "Server response is invalid");
			shortenedUrl = map.get("id");
			checkState(isNotBlank(shortenedUrl), "No shortened URL found in server response");
		}
		return shortenedUrl;
	}

}