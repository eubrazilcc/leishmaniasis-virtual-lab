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

/**
 * Extra utilities for numeric types.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class NumberUtils {

	/**
	 * Rounds an integer division to the nearest integer.
	 * @param number - value that is rounded
	 * @param base - divisor
	 * @return the nearest integer that multiplied by the base has the largest 
	 *         integer value
	 */
	public static int roundUp(final int number, final int base) {
		return (number + base - 1) / base;
	}
	
	/**
	 * Rounds an long division to the nearest long.
	 * @param number - value that is rounded
	 * @param base - divisor
	 * @return the nearest long that multiplied by the base has the largest 
	 *         long value
	 */
	public static long roundUp(final long number, final long base) {
		return (number + base - 1l) / base;
	}
	
}