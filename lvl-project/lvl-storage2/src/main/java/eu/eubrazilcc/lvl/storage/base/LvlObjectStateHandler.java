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

package eu.eubrazilcc.lvl.storage.base;

import java.util.Date;
import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Provides different behaviors for different object states.
 * @author Erik Torres <ertorser@upv.es>
 */
public interface LvlObjectStateHandler<T extends LvlObject> {

	ListenableFuture<Boolean> save(T obj, SaveOptions... options);

	ListenableFuture<Boolean> fetch(T obj, FetchOptions... options);

	ListenableFuture<Boolean> delete(T obj, DeleteOptions... options);

	ListenableFuture<Date> undo(T obj);

	ListenableFuture<List<T>> versions(T obj);

}