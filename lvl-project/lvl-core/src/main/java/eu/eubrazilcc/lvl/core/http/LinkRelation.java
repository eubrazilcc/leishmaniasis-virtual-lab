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
 * A subset of the IETF link relations types.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.iana.org/assignments/link-relations/link-relations.xhtml">IETF link relations types</a>
 * @see <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 - Web Linking</a>
 */
public final class LinkRelation {
	
	/**
	 * A URI that can be retrieved, updated and deleted.
	 */
	public static final String EDIT = "edit";
	
	/**
	 * A URI that refers to the furthest preceding resource in a series of resources.
	 */
	public static final String FIRST = "first";
	
	/**
	 * A URI to an icon representing the link's context.
	 */
	public static final String ICON = "icon";
	
	/**
	 * A URI that refers to the furthest following resource in a series of resources.
	 */
	public static final String LAST = "last";
	
	/**
	 * A URI that refers to the immediately following resource in a series of resources.
	 */
	public static final String NEXT = "next";
	
	/**
	 * A URI where payment is accepted.
	 */
	public static final String PAYMENT = "payment"; 
	
	/**
	 * A URI that refers to the immediately preceding resource in a series of resources.
	 */
	public static final String PREVIOUS = "previous";
	
	/**
	 * A URI that refers to the link's context.
	 */
	public static final String SELF = "self";
	
}