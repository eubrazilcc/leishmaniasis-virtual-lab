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

package eu.eubrazilcc.lvl.service;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.FIRST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.NEXT;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.PREVIOUS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.Paginable;
import eu.eubrazilcc.lvl.core.community.Post;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;
import eu.eubrazilcc.lvl.service.rest.PostResource;

/**
 * Wraps a collection of {@link Post}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Posts extends Paginable<Post> {

	@InjectLinks({
		@InjectLink(resource=PostResource.class, method="getPosts", bindings={
				@Binding(name="page", value="${instance.page - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
		}, rel=PREVIOUS, type=APPLICATION_JSON, condition="${instance.page > 0}"),
		@InjectLink(resource=PostResource.class, method="getPosts", bindings={
				@Binding(name="page", value="${0}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
		}, rel=FIRST, type=APPLICATION_JSON, condition="${instance.page > 0}"),
		@InjectLink(resource=PostResource.class, method="getPosts", bindings={
				@Binding(name="page", value="${instance.page + 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
		}, rel=NEXT, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}"),
		@InjectLink(resource=PostResource.class, method="getPosts", bindings={
				@Binding(name="page", value="${instance.totalPages - 1}"),
				@Binding(name="per_page", value="${instance.perPage}"),
				@Binding(name="sort", value="${instance.sort}"),
				@Binding(name="order", value="${instance.order}"),
				@Binding(name="q", value="${instance.query}")
		}, rel=LAST, type=APPLICATION_JSON, condition="${instance.pageFirstEntry + instance.perPage < instance.totalCount}")
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		if (links != null) {
			this.links = newArrayList(links);
		} else {
			this.links = null;
		}
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("paginable", super.toString())
				.toString();
	}

	public static PostsBuilder start() {
		return new PostsBuilder();
	}

	public static class PostsBuilder {

		private final Posts instance = new Posts();

		public PostsBuilder page(final int page) {
			instance.setPage(page);
			return this;
		}

		public PostsBuilder perPage(final int perPage) {
			instance.setPerPage(perPage);
			return this;
		}

		public PostsBuilder sort(final String sort) {
			instance.setSort(sort);
			return this;
		}

		public PostsBuilder order(final String order) {
			instance.setOrder(order);
			return this;
		}

		public PostsBuilder query(final String query) {
			instance.setQuery(query);
			return this;
		}

		public PostsBuilder totalCount(final int totalCount) {
			instance.setTotalCount(totalCount);
			return this;
		}

		public PostsBuilder hash(final String hash) {
			instance.setHash(hash);
			return this;
		}

		public PostsBuilder instances(final List<Post> instances) {
			instance.setElements(instances);
			return this;			
		}

		public Posts build() {
			return instance;
		}

	}

}