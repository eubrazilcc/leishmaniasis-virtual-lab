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

package eu.eubrazilcc.lvl.core.community;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Contains a post to the social networking system.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Post implements Linkable<Post> {

	@InjectLinks({
		@InjectLink(value="community/posts/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, 
				bindings={@Binding(name="urlSafeId", value="${instance.urlSafeId}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeId;

	private String id;        // Resource identifier
	private Date created;
	private String author;
	private PostCategory category;
	private PostLevel level;
	private String body;

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		this.links = links != null ? newArrayList(links) : null;		
	}

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(urlEncodeUtf8(trimToEmpty(id)));
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(final Date created) {
		this.created = created;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(final String author) {
		this.author = author;
	}

	public PostCategory getCategory() {
		return category;
	}

	public void setCategory(final PostCategory category) {
		this.category = category;
	}

	public PostLevel getLevel() {
		return level;
	}

	public void setLevel(final PostLevel level) {
		this.level = level;
	}

	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Post)) {
			return false;
		}
		final Post other = Post.class.cast(obj);
		return Objects.equals(links, other.links)
				&& Objects.equals(urlSafeId, other.urlSafeId)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final Post other) {
		return Objects.equals(id, other.id)
				&& Objects.equals(created, other.created)
				&& Objects.equals(author, other.author)
				&& Objects.equals(category, other.category)
				&& Objects.equals(level, other.level)
				&& Objects.equals(body, other.body);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, urlSafeId, id, created, author, category, level, body);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("urlSafeId", urlSafeId)
				.add("id", id)
				.add("created", created)
				.add("author", author)
				.add("category", category)
				.add("level", level)
				.add("body", body)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Post instance = new Post();

		public Builder newId() {
			instance.setId(compactRandomUUID());
			return this;
		}

		public Builder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder created(final Date created) {
			instance.setCreated(requireNonNull(created, "Uninitialized or invalid creation date"));
			return this;
		}

		public Builder author(final String author) {
			String author2 = null;
			checkArgument(isNotBlank(author2 = trimToNull(author)), "Uninitialized or invalid author");
			instance.setAuthor(author2);
			return this;
		}

		public Builder category(final PostCategory category) {
			instance.setCategory(requireNonNull(category, "Uninitialized or invalid category"));
			return this;
		}

		public Builder level(final PostLevel level) {
			instance.setLevel(requireNonNull(level, "Uninitialized or invalid level"));
			return this;
		}		

		public Builder body(final String body) {
			String body2 = null;
			checkArgument(isNotBlank(body2 = trimToNull(body)), "Uninitialized or invalid body");
			instance.setBody(body2);
			return this;
		}

		public Post build() {
			return instance;
		}

	}

}