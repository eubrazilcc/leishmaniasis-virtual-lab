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
import static eu.eubrazilcc.lvl.storage.base.LvlObject.copyProperties;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static java.util.Arrays.asList;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * Behavior corresponding to the draft state.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DraftStateHandler<T extends LvlObject> implements LvlObjectStateHandler<T> {

	@Override
	public ListenableFuture<Boolean> save(final T obj, final SaveOptions... options) {
		return MONGODB_CONN.save(obj);
	}

	@Override
	public ListenableFuture<Boolean> fetch(final T obj, final FetchOptions... options) {
		final LvlObject __obj = obj;
		final ListenableFuture<LvlObject> findFuture = MONGODB_CONN.find(obj, obj.getClass());
		final SettableFuture<Boolean> foundFuture = SettableFuture.create();
		addCallback(findFuture, new FutureCallback<LvlObject>() {
			@Override
			public void onSuccess(final LvlObject result) {				
				try {
					copyProperties(result, __obj);
					foundFuture.set(true);
				} catch (IllegalAccessException | InvocationTargetException e) {
					foundFuture.setException(e);
				}
			}
			@Override
			public void onFailure(final Throwable t) {				
				foundFuture.setException(t);
			}
		});
		return transform(findFuture, new AsyncFunction<LvlObject, Boolean>() {
			@Override
			public ListenableFuture<Boolean> apply(final LvlObject input) throws Exception {				
				return foundFuture;
			}
		});
	}

	@Override
	public ListenableFuture<Boolean> delete(final T obj, final DeleteOptions... options) {
		final List<DeleteOptions> optList = (options != null ? asList(options) : Collections.<DeleteOptions>emptyList());
		return MONGODB_CONN.delete(obj, optList.contains(DeleteOptions.DELETE_CASCADING));
	}

	@Override
	public ListenableFuture<Date> undo(final T obj) {
		final SettableFuture<Date> undoFuture = SettableFuture.create();
		final ListenableFuture<Boolean> deleteFuture = MONGODB_CONN.delete(obj, false);
		addCallback(deleteFuture, new FutureCallback<Boolean>() {
			@Override
			public void onSuccess(final Boolean result) {
				undoFuture.set(null);
			}
			@Override
			public void onFailure(final Throwable t) {				
				undoFuture.setException(t);
			}
		});
		return transform(deleteFuture, new AsyncFunction<Boolean, Date>() {
			@Override
			public ListenableFuture<Date> apply(final Boolean input) throws Exception {				
				return undoFuture;
			}
		});
	}

	@Override
	public ListenableFuture<List<T>> versions(final T obj) {
		// TODO Auto-generated method stub
		return null;
	}

}