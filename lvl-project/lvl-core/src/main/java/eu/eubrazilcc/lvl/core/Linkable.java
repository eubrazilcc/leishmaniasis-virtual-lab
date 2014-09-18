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


/**
 * Classes that include volatile fields, such as links that depends on the location where the application runs, implement this interface to provide 
 * a method that can be used to compare two instances of the class ignoring the volatile fields.
 * @author Erik Torres <ertorser@upv.es>
 * @param <T>  the type of objects that this object may be compared to
 */
public interface Linkable<T> {
	
	/**
	 * Ignores volatile fields when comparing two instances of this class. A volatile field is a class attribute with its value assigned from local variables.
	 * For example, a field that contains the URI of the service.
	 * @param other - the instance to be compared to.
	 * @return {@code true} if all the attributes of both instances coincide in value with the sole exception of those considered volatile. 
	 *        Otherwise, {@code false}.
	 */
	boolean equalsIgnoringVolatile(T other);
	
}