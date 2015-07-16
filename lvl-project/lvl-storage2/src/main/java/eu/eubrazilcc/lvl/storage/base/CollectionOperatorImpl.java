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

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.Filters;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionStats;

/**
 * Base implementation of the {@link CollectionOperator}.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class CollectionOperatorImpl<T extends LvlObject> implements CollectionOperator<T> {

	private final LvlCollection<T> lvlCol;

	private Optional<List<String>> excludedStates = absent(); // (optional) objects in these states are excluded from the collection

	public CollectionOperatorImpl(final LvlCollection<T> lvlCol, final @Nullable List<String> excludedStates) {
		this.lvlCol = lvlCol;
		this.excludedStates = fromNullable(excludedStates);
	}	

	@Override
	public ListenableFuture<Integer> fetch(final int start, final int size, final @Nullable Filters filters, final @Nullable Map<String, Boolean> sorting, 
			final @Nullable Map<String, Boolean> projections) {
		final MutableLong totalCount = new MutableLong(0l);
		final ListenableFuture<List<T>> findFuture = MONGODB_CONN.client().findActive(lvlCol, lvlCol.getType(), start, size, filters, sorting, projections, totalCount, 
				excludedStates.orNull());
		final SettableFuture<Integer> countFuture = SettableFuture.create();
		addCallback(findFuture, new FutureCallback<List<T>>() {
			@Override
			public void onSuccess(final List<T> result) {
				lvlCol.setElements(result);
				lvlCol.setTotalCount(totalCount.getValue().intValue());
				countFuture.set(result != null ? result.size() : 0);
			}
			@Override
			public void onFailure(final Throwable t) {				
				countFuture.setException(t);
			}
		});
		return transform(findFuture, new AsyncFunction<List<T>, Integer>() {
			@Override
			public ListenableFuture<Integer> apply(final List<T> input) throws Exception {				
				return countFuture;
			}
		});
	}

	@Override
	public ListenableFuture<FeatureCollection> getNear(final Point point, final double minDistance, final double maxDistance) {
		return MONGODB_CONN.client().fetchNear(lvlCol, lvlCol.getType(), point.getCoordinates().getLongitude(), point.getCoordinates().getLatitude(), 
				minDistance, maxDistance, excludedStates.orNull());
	}

	@Override
	public ListenableFuture<FeatureCollection> getWithin(final Polygon polygon) {
		return MONGODB_CONN.client().fetchWithin(lvlCol, lvlCol.getType(), polygon, excludedStates.orNull());
	}

	@Override
	public ListenableFuture<Long> totalCount() {
		return MONGODB_CONN.client().totalCount(lvlCol, excludedStates.orNull());		
	}

	@Override
	public ListenableFuture<List<String>> typeahead(final String field, final String query, final int size) {
		return MONGODB_CONN.client().typeahead(lvlCol, lvlCol.getType(), field, query, size, excludedStates.orNull());
	}

	@Override
	public ListenableFuture<MongoCollectionStats> stats() {
		return MONGODB_CONN.client().stats(lvlCol);
	}

	@Override
	public ListenableFuture<Void> drop() {
		return MONGODB_CONN.client().drop(lvlCol);
	}
	
	@Override
	public LvlCollection<T> collection() {
		return lvlCol;
	}

	@Override
	public List<String> excludedStates() {
		return excludedStates.orNull();
	}	

}