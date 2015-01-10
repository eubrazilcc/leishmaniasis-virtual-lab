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

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.authzHeader;
import static java.util.Arrays.asList;
import static java.util.Locale.getAvailableLocales;
import static java.util.Locale.getDefault;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Simple implementation of {@link HttpHeaders} to overcome a current limitation in the JavaScript
 * EventSource interface that does not allows HTTP headers to be send with the request when a client
 * subscribe to a server for events. HTTP headers are needed to support OAuth authorization (this
 * application only supports HTTP based authorization tokens. Tokens provided as part of the request
 * body are not taken into account in this version).
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/EventSource">EventSource</a>
 */
public class SseSubscriptionHttpHeaders implements HttpHeaders {

	private final MultivaluedMap<String, String> headers;	

	public SseSubscriptionHttpHeaders(final String token) {
		headers = authzHeader(token);
	}

	@Override
	public List<Locale> getAcceptableLanguages() {
		return asList(getAvailableLocales());
	}

	@Override
	public List<MediaType> getAcceptableMediaTypes() {		
		return newArrayList(new MediaType("text", "event-stream"));
	}

	@Override
	public Map<String, Cookie> getCookies() {
		return newHashMap();
	}

	@Override
	public Date getDate() {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -1);
		return calendar.getTime();
	}

	@Override
	public Locale getLanguage() {
		return getDefault();
	}

	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public MediaType getMediaType() {
		return MediaType.TEXT_PLAIN_TYPE;
	}

	@Override
	public String getHeaderString(final String name) {
		String str = null;
		final List<String> list = headers.get(name);
		if (list != null) {
			str = on(',').skipNulls().join(list);
		}
		return str;
	}

	@Override
	public List<String> getRequestHeader(final String name) {
		return headers.get(name);
	}

	@Override
	public MultivaluedMap<String, String> getRequestHeaders() {
		return new MultivaluedHashMap<>(headers);
	}
	
	public static HttpHeaders ssehHttpHeaders(final String token) {
		return new SseSubscriptionHttpHeaders(token);
	}

}