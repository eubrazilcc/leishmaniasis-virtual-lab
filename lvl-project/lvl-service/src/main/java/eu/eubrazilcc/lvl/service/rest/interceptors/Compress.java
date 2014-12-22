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

package eu.eubrazilcc.lvl.service.rest.interceptors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

/**
 * Provides an annotation that can be used to mark the methods on resources which should be compressed (with GZIP). For example,
 * GET methods that return a very large response to the caller. Add the annotation <code>@Compress</code> to the desired method 
 * in to resource class to enable compression in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://jersey.java.net/documentation/latest/user-guide.html#d0e8359">Jersey User Guide - Interceptors</a>
 * @see <a href="http://www.javacodegeeks.com/2014/11/how-to-compress-responses-in-java-rest-api-with-gzip-and-jersey.html">How to compress responses in Java REST API with GZip and Jersey</a>
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface Compress {

}