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

package eu.eubrazilcc.lvl.storage.mongodb;

/**
 * Wrapper of the exception {@code DuplicateKeyException} defined in the MongoDB JAVA API.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://api.mongodb.org/java/current/com/mongodb/DuplicateKeyException.html">MongoDB class DuplicateKeyException</a>
 */
public class MongoDBDuplicateKeyException extends RuntimeException {

	private static final long serialVersionUID = 8798148503186116229L;

	public MongoDBDuplicateKeyException() {
		super();
	}

	public MongoDBDuplicateKeyException(final String message) {
		super(message);
	}

	public MongoDBDuplicateKeyException(final Throwable cause) {
		super(cause);
	}

	public MongoDBDuplicateKeyException(final String message, final Throwable cause) {
		super(message, cause);
	}

}