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
 * Behavior corresponding to the finalized state.
 * @author Erik Torres <ertorser@upv.es>
 */
public class FinalizedStateHandler<T extends LvlObject> implements LvlObjectStateHandler<T> {

	@Override
	public ListenableFuture<Boolean> save(final T obj, final SaveOptions... options) {
		throw new UnsupportedOperationException("Modifying finalized objects is not supported");
	}

	@Override
	public ListenableFuture<Boolean> fetch(final T obj, final FetchOptions... options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListenableFuture<Boolean> delete(final T obj, final DeleteOptions... options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListenableFuture<Date> undo(final T obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListenableFuture<List<T>> versions(final T obj) {
		// TODO Auto-generated method stub
		return null;
	}
	
}