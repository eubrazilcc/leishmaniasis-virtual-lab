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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;
import static eu.eubrazilcc.lvl.core.util.PaginationUtils.firstEntryOf;
import static eu.eubrazilcc.lvl.core.util.PaginationUtils.totalPages;
import static java.lang.Math.max;

import java.util.List;

import javax.ws.rs.core.Link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Use this class to store any collection that can be returned to the client as a series of pages that contain a part of the collection. Includes 
 * Jackson annotations to serialize this class to JSON. In addition, this class include HATEOAS links to previous, next, first and last pages, which 
 * allow clients using infinite scroll pagination. Additionally, the total count of records in provided to support classic server-side pagination.
 * @param <T> the type of objects that this class stores
 * @author Erik Torres <ertorser@upv.es>
 */
@JsonIgnoreProperties({ "page", "perPage", "query", "sort", "order", "pageFirstEntry", "totalPages" })
public abstract class Paginable<T> {

	public final static int PER_PAGE_MIN = 1;	

	private int page; // query parameters
	private int perPage = PER_PAGE_MIN;	
	private String sort;
	private String order;
	private String query;

	private List<FormattedQueryParam> formattedQuery = newArrayList(); // formatted query parameters
	private int pageFirstEntry; // first entry of the current page
	private int totalPages; // total number of pages
	private int totalCount; // total number of elements

	private String hash; // hash computed from the query parameters
	private List<String> includedFields = newArrayList();
	private List<String> excludedFields = newArrayList();

	private List<T> elements = newArrayList(); // elements of the current page

	public Paginable() { }

	public abstract List<Link> getLinks();

	public abstract void setLinks(final List<Link> links);

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

	public String getHash() {
		return hash;
	}

	public void setHash(final String hash) {
		this.hash = hash;
	}

	public List<String> getIncludedFields() {
		return includedFields;
	}

	public void setIncludedFields(final List<String> includedFields) {
		if (includedFields != null) {
			this.includedFields = newArrayList(includedFields);
		} else {
			this.includedFields.clear();
		}
	}

	public List<String> getExcludedFields() {
		return excludedFields;
	}

	public void setExcludedFields(final List<String> excludedFields) {
		if (excludedFields != null) {
			this.excludedFields = newArrayList(excludedFields);
		} else {
			this.excludedFields.clear();
		}
	}

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

	@Override
	public String toString() {
		final List<Link> links = getLinks();
		return toStringHelper(Paginable.class.getSimpleName())
				.add("page", page)
				.add("perPage", perPage)
				.add("sort", sort)
				.add("order", order)
				.add("query", query)
				.add("formattedQuery", collectionToString(formattedQuery))
				.add("pageFirstEntry", pageFirstEntry)
				.add("totalPages", totalPages)
				.add("totalCount", totalCount)
				.add("hash", hash)
				.add("elements", collectionToString(elements))
				.add("links", links != null ? collectionToString(links) : null)
				.toString();
	}

}