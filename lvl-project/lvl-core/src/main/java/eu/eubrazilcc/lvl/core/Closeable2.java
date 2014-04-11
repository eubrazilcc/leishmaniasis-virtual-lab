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

package eu.eubrazilcc.lvl.core;

import java.io.Closeable;
import java.net.URL;
import java.util.Collection;

import javax.annotation.Nullable;

/**
 * {@link Closeable} with optional pre-loading.
 * @author Erik Torres <ertorser@upv.es>
 */
public interface Closeable2 extends Closeable {

	/**
	 * Set-ups the service for its first usage, overriding configuration when necessary.
	 * @param urls an optional collection of URLs that overrides the URLs passed to 
	 *        this service by configuration.
	 */
	void setup(@Nullable Collection<URL> urls);	

	/**
	 * Loads the service for its first usage.
	 */
	void preload();	

}