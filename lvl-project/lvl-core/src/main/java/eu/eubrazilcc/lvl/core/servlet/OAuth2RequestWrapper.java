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

package eu.eubrazilcc.lvl.core.servlet;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Workaround to deal with {@link HttpServletRequest} in Jersey-based post methods, where the form 
 * is passed as a {@link MultivaluedMap}. This wrapper class gets the form parameters from the servlet 
 * request or the map. Apache Oltu OAuth2 implementation needs a {@link HttpServletRequest}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class OAuth2RequestWrapper extends HttpServletRequestWrapper {

	private final MultivaluedMap<String, String> form;
	private final MultivaluedMap<String, String> headers;

	public OAuth2RequestWrapper(final HttpServletRequest request, 
			final @Nullable MultivaluedMap<String, String> form,
			final @Nullable HttpHeaders headers) { 
		super(request);
		this.form = form != null ? form : new MultivaluedHashMap<String, String>();
		this.headers = headers != null ? headers.getRequestHeaders() : new MultivaluedHashMap<String, String>();
	}

	@Override
	public String getParameter(final String name) {
		final String value = super.getParameter(name);
		return (value != null ? value : form.getFirst(name));
	}

	@Override
	public String getHeader(final String name) {
		final String value = super.getHeader(name);
		return (value != null ? value : headers.getFirst(name));
	}

}