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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;
import static eu.eubrazilcc.lvl.core.util.PaginationUtils.firstEntryOf;
import static eu.eubrazilcc.lvl.core.util.PaginationUtils.totalPages;
import static java.lang.Math.max;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Use this class to store any collection that can be returned to the client as a series of pages that contain a part of the collection. Includes 
 * Jackson annotations to serialize this class to JSON. In addition, this class include HATEOAS links to previous, next, first and last pages, which 
 * allow clients using infinite scroll pagination. Additionally, the total count of records in provided to support classic server-side pagination.
 * @param <T> the type of objects that this class stores
 * @author Erik Torres <ertorser@upv.es>
 */
@JsonIgnoreProperties({ "resource", "page", "perPage", "query", "sort", "order", "pageFirstEntry", "totalPages" })
public class Paginable<T> {

	public final static int PER_PAGE_MIN = 1;
	public final static String URI_TEMPLATE = "{resource}?page={page}&per_page={per_page}&sort={sort}&order={order}&q={query}";

	@InjectLinks({
		@InjectLink(value=URI_TEMPLATE, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}", bindings={
				@Binding(name="resource", value="${instance.resource}"),
				@Binding(name="page", value="${instance.page - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="query", value="${instance.query}")
		}),
		@InjectLink(value=URI_TEMPLATE, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}", bindings={
				@Binding(name="resource", value="${instance.resource}"),
				@Binding(name="page", value="${0}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="query", value="${instance.query}")
		}),
		@InjectLink(value=URI_TEMPLATE, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}", bindings={
				@Binding(name="resource", value="${instance.resource}"),
				@Binding(name="page", value="${instance.page + 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="query", value="${instance.query}")
		}),
		@InjectLink(value=URI_TEMPLATE, rel=LAST, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}", bindings={
				@Binding(name="resource", value="${instance.resource}"),
				@Binding(name="page", value="${instance.totalPages}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="query", value="${instance.query}")
		})		
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private String resource; // query parameters
	private int page;
	private int perPage = PER_PAGE_MIN;	
	private String sort;
	private String order;
	private String query;

	private int pageFirstEntry; // first entry of the current page
	private int totalPages; // total number of pages
	private int totalCount; // total number of elements

	private List<T> elements = newArrayList(); // elements of the current page

	public Paginable() { }

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(final List<Link> links) {
		if (links != null) {
			this.links = newArrayList(links);
		} else {
			this.links = null;
		}
	}	

	public String getResource() {
		return resource;
	}

	public void setResource(final String resource) {
		this.resource = checkNotNull(resource, "Uninitialized resource").trim();
		// remove path separators from the beginning of the route
		while (this.resource.charAt(0) == '/') {
			this.resource = this.resource.length() > 1 ? this.resource.substring(1) : "";
		}
	}

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

	public List<T> getElements() {
		return elements;
	}

	public void setElements(final List<T> elements) {
		if (elements != null) {
			this.elements = newArrayList(elements);
		} else {
			this.elements = null;
		}
	}

	@Override
	public String toString() {
		return toStringHelper(Paginable.class.getSimpleName())
				.add("resource", resource)
				.add("page", page)
				.add("perPage", perPage)
				.add("sort", sort)
				.add("order", order)
				.add("query", query)
				.add("pageFirstEntry", pageFirstEntry)
				.add("totalPages", totalPages)
				.add("totalCount", totalCount)
				.add("elements", collectionToString(elements))
				.add("links", links != null ? collectionToString(links) : null)
				.toString();
	}

}