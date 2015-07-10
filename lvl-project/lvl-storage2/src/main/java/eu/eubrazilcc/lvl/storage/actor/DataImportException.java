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

package eu.eubrazilcc.lvl.storage.actor;

/**
 * Thrown when a {@link DataImporter} fails to import 
 * @author Erik Torres <ertorser@upv.es>
 */
public class DataImportException extends Exception {

	private static final long serialVersionUID = -2346814705959765474L;

	public DataImportException() {
		super();
	}

	public DataImportException(final String message) {
		super(message);
	}

	public DataImportException(final Throwable cause) {
		super(cause);
	}

	public DataImportException(final String message, final Throwable cause) {
		super(message, cause);
	}

}