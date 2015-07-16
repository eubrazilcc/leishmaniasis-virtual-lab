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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;
import static eu.eubrazilcc.lvl.core.util.PaginationUtils.firstEntryOf;
import static eu.eubrazilcc.lvl.core.util.PaginationUtils.totalPages;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static java.lang.Math.max;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import eu.eubrazilcc.lvl.core.FormattedQueryParam;
import eu.eubrazilcc.lvl.storage.Filters;
import eu.eubrazilcc.lvl.storage.Linkable;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionStats;
import eu.eubrazilcc.lvl.storage.mongodb.MongoFilesConfigurer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions;

/**
 * Any collection of {@link LvlFile}.
 * @author Erik Torres <ertorser@upv.es>
 */
@JsonIgnoreProperties({ "page", "perPage", "query", "sort", "order", "pageFirstEntry", "totalPages" })
public abstract class LvlFiles<T extends LvlFile> implements Linkable {

	@JsonIgnore
	protected final Logger logger;
	@JsonIgnore
	private final Class<T> type;
	@JsonIgnore
	private final String namespace;
	@JsonIgnore
	private final MongoFilesConfigurer configurer;

	public final static int PER_PAGE_MIN = 1;

	private int page;
	private int perPage = PER_PAGE_MIN;	
	private String sort;
	private String order;
	private String query; // query parameters

	private List<FormattedQueryParam> formattedQuery = newArrayList(); // formatted query parameters
	private int pageFirstEntry; // first entry of the current page
	private int totalPages; // total number of pages
	private int totalCount; // total number of elements

	private List<T> elements = newArrayList(); // elements of the current page

	public LvlFiles(final String namespace, final Class<T> type, final MongoFilesConfigurer configurer, final Logger logger) {
		this.namespace = namespace;
		this.type = type;
		this.configurer = configurer;
		this.logger = logger;
	}

	public Class<T> getType() {
		return type;
	}

	public String getNamespace() {
		return namespace;
	}

	public MongoFilesConfigurer getConfigurer() {
		return configurer;
	}

	/* Database operations */

	public ListenableFuture<Integer> fetch(final int start, final int size, final @Nullable Filters filters, final @Nullable Map<String, Boolean> sorting) {
		final MutableLong totalCount = new MutableLong(0l);
		final ListenableFuture<List<T>> findFuture = MONGODB_CONN.fsClient().fetchFiles(this, type, start, size, filters, sorting, totalCount);
		final SettableFuture<Integer> countFuture = SettableFuture.create();
		addCallback(findFuture, new FutureCallback<List<T>>() {
			@Override
			public void onSuccess(final List<T> result) {
				setElements(result);
				setTotalCount(totalCount.getValue().intValue());
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

	public ListenableFuture<Integer> fetchOpenAccess(final int start, final int size, final @Nullable Map<String, Boolean> sorting) {
		final MutableLong totalCount = new MutableLong(0l);
		final ListenableFuture<List<T>> findFuture = MONGODB_CONN.fsClient().findOpenAccess(this, type, start, size, sorting, totalCount);
		final SettableFuture<Integer> countFuture = SettableFuture.create();
		addCallback(findFuture, new FutureCallback<List<T>>() {
			@Override
			public void onSuccess(final List<T> result) {
				setElements(result);
				setTotalCount(totalCount.getValue().intValue());
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

	public ListenableFuture<Long> totalCount() {
		return MONGODB_CONN.fsClient().totalCount(this);
	}

	public ListenableFuture<List<String>> typeahead(final String query, final int size) {
		return MONGODB_CONN.fsClient().typeaheadFile(this, query, size);
	}

	public ListenableFuture<MongoCollectionStats> stats() {
		return MONGODB_CONN.fsClient().statsFiles(this);
	}

	/* Operate on the elements loaded in the current view */

	public List<T> getElements() {
		return elements;
	}

	public void setElements(final List<T> elements) {
		if (elements != null) {
			this.elements = newArrayList(elements);
		} else {
			this.elements.clear();
		}
	}

	public List<String> ids() {
		return from(elements != null ? elements : Collections.<T>emptyList()).transform(new Function<T, String>() {
			@Override
			public String apply(final T input) {
				return input.getId();
			}
		}).filter(notNull()).toList();
	}

	public List<String> filenames() {
		return from(elements != null ? elements : Collections.<T>emptyList()).transform(new Function<T, String>() {
			@Override
			public String apply(final T input) {
				return input.getMetadata().getFilename();
			}
		}).filter(notNull()).toList();
	}

	/**
	 * The view maintains a size property, counting the number of elements it contains.
	 * @return the number of elements loaded in the current view
	 */
	public int size() {
		return elements.size();
	}

	/**
	 * Gets from the view the element specified by index.
	 * @param index - index of the element within the view
	 * @return the element specified by index
	 */
	public T get(final int index) {
		return elements.get(index);
	}

	/**
	 * Search the view for the element specified by id.
	 * @param id - identifier whose associate value is to be returned
	 * @return the element specified by id, or {@code null} if the view contains no entry for the id.
	 */
	public T get(final String id) {
		return Iterables.find(elements, new Predicate<T>() {
			@Override
			public boolean apply(final T item) {
				return item.getId().equals(id);
			}			
		}, null);
	}

	/* Pagination */

	public int getPage() {
		return page;
	}

	public void setPage(final int page) {
		this.page = page;
		setPageFirstEntry(firstEntryOf(this.page, this.perPage));
	}

	public int getPerPage() {
		return perPage;
	}

	public void setPerPage(final int perPage) {
		this.perPage = max(PER_PAGE_MIN, perPage);
		setPageFirstEntry(firstEntryOf(this.page, this.perPage));
		setTotalPages(totalPages(this.totalCount, this.perPage));
	}

	public String getSort() {
		return sort;
	}

	public void setSort(final String sort) {
		this.sort = sort;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(final String order) {
		this.order = order;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(final String query) {
		this.query = query;
	}

	public List<FormattedQueryParam> getFormattedQuery() {
		return formattedQuery;
	}

	public void setFormattedQuery(final List<FormattedQueryParam> formattedQuery) {		
		if (formattedQuery != null) {
			this.formattedQuery = newArrayList(formattedQuery);
		} else {
			this.formattedQuery.clear();
		}
	}

	public int getPageFirstEntry() {
		return pageFirstEntry;
	}

	public void setPageFirstEntry(final int pageFirstEntry) {
		this.pageFirstEntry = pageFirstEntry;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(final int totalPages) {
		this.totalPages = totalPages;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(final int totalCount) {
		this.totalCount = totalCount;
		setTotalPages(totalPages(this.totalCount, this.perPage));
	}

	/* General methods */

	/**
	 * Returns a String containing the attributes of each element loaded in the current view.
	 * @param options - JSON parser options
	 * @return a String containing the attributes of each element loaded in the current view
	 */
	public String toJson(final MongoJsonOptions... options) {
		String payload = "";		
		try {
			payload = objectToJson(this, options);
		} catch (final JsonProcessingException e) {
			logger.error("Failed to export object to JSON", e);
		}
		return payload;
	}	

	@Override
	public String toString() {
		final List<Link> links = getLinks();
		return toStringHelper(this)
				.add("page", page)
				.add("perPage", perPage)
				.add("sort", sort)
				.add("order", order)
				.add("query", query)
				.add("formattedQuery", collectionToString(formattedQuery))
				.add("pageFirstEntry", pageFirstEntry)
				.add("totalPages", totalPages)
				.add("totalCount", totalCount)
				.add("elements", collectionToString(elements))
				.add("links", links != null ? collectionToString(links) : null)
				.toString();
	}

}