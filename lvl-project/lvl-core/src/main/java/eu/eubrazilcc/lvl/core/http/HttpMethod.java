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

package eu.eubrazilcc.lvl.core.http;


/**
 * A subset of HTTP/1.1 methods available for REST.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">RFC 2616 - HTTP/1.1: Method Definitions</a>
 */
public final class HttpMethod {

	public static final String GET     = "GET";
	public static final String PUT     = "PUT";
	public static final String DELETE  = "DELETE";
	public static final String POST    = "POST";
	public static final String HEAD    = "HEAD";
	public static final String OPTIONS = "OPTIONS";

}