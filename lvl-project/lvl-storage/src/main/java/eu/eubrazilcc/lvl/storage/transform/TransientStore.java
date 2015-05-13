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

package eu.eubrazilcc.lvl.storage.transform;

/**
 * Extracts from an entity the fields that depends on the service (e.g. links) before storing the entity in the database. These fields are 
 * stored in this class and can be reinserted later in the entity.
 * @author Erik Torres <ertorser@upv.es>
 * @param <T> - the type of elements in this store
 */
public abstract class TransientStore<T> {

	protected final T element;

	public TransientStore(final T element) {
		this.element = element;
	}

	public abstract T purge();

	public abstract T restore();

}