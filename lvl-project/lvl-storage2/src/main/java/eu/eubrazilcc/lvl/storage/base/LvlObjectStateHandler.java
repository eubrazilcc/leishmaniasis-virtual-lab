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

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static eu.eubrazilcc.lvl.storage.base.DeleteOptions.DELETE_ACTIVE;
import static eu.eubrazilcc.lvl.storage.base.DeleteOptions.DELETE_ALL;
import static eu.eubrazilcc.lvl.storage.base.DeleteOptions.ON_DELETE_CASCADE;
import static eu.eubrazilcc.lvl.storage.base.DeleteOptions.ON_DELETE_NO_ACTION;
import static eu.eubrazilcc.lvl.storage.base.LvlObject.copyProperties;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static java.util.Arrays.asList;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Provides different behaviors for different object states.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class LvlObjectStateHandler<T extends LvlObject> {

	public abstract ListenableFuture<Void> save(T obj, SaveOptions... options);

	public ListenableFuture<Void> fetch(final T obj, final FetchOptions... options) {
		final LvlObject __obj = obj;
		final ListenableFuture<LvlObject> findFuture = MONGODB_CONN.findActive(obj, obj.getClass());
		final SettableFuture<Void> foundFuture = SettableFuture.create();
		addCallback(findFuture, new FutureCallback<LvlObject>() {
			@Override
			public void onSuccess(final LvlObject result) {				
				try {
					copyProperties(result, __obj);						
					foundFuture.set(null);
				} catch (IllegalAccessException | InvocationTargetException e) {
					foundFuture.setException(e);
				}
			}
			@Override
			public void onFailure(final Throwable t) {				
				foundFuture.setException(t);
			}
		});
		return transform(findFuture, new AsyncFunction<LvlObject, Void>() {
			@Override
			public ListenableFuture<Void> apply(final LvlObject input) throws Exception {				
				return foundFuture;
			}
		});
	}

	public ListenableFuture<Boolean> delete(final T obj, final DeleteOptions... options) {
		final List<DeleteOptions> optList = (options != null ? asList(options) : Collections.<DeleteOptions>emptyList());
		return MONGODB_CONN.delete(obj, !optList.contains(DELETE_ACTIVE) && optList.contains(DELETE_ALL), 
				!optList.contains(ON_DELETE_NO_ACTION) && optList.contains(ON_DELETE_CASCADE));
	}

	public ListenableFuture<List<T>> versions(final T obj) {
		// TODO Auto-generated method stub
		return null;
	}

}