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

package eu.eubrazilcc.lvl.core.support;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Subscription request.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SubscriptionRequest implements Linkable<SubscriptionRequest> {

	@InjectLinks({
		@InjectLink(value="support/subscriptions/requests/{id}", rel=SELF, type=APPLICATION_JSON, 
				bindings={@Binding(name="id", value="${instance.id}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private String id;
	private String email;
	private Date requested;
	private Set<String> channels = newHashSet();

	private Optional<Date> fulfilled = absent();

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

	public String getId() {
		return id;
	}
	public void setId(final String id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}
	public Date getRequested() {
		return requested;
	}
	public void setRequested(final Date requested) {
		this.requested = requested;
	}
	public Set<String> getChannels() {
		return channels;
	}
	public void setChannels(final Set<String> channels) {
		this.channels.clear();
		if (channels != null) {
			this.channels.addAll(channels);
		}
	}
	public Date getFulfilled() {
		return fulfilled.orNull();
	}
	public void setFulfilled(final @Nullable Date fulfilled) {
		this.fulfilled = fromNullable(fulfilled);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SubscriptionRequest)) {
			return false;
		}
		final SubscriptionRequest other = SubscriptionRequest.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final SubscriptionRequest other) {
		return Objects.equals(id, other.id)
				&& Objects.equals(email, other.email)
				&& Objects.equals(requested, other.requested)
				&& Objects.equals(channels, other.channels)
				&& Objects.equals(fulfilled.orNull(), other.fulfilled.orNull());
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, id, email, requested, channels, fulfilled);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("id", id)
				.add("email", email)
				.add("requested", requested)
				.add("channels", channels)
				.add("fulfilled", fulfilled.orNull())
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final SubscriptionRequest instance = new SubscriptionRequest();

		public Builder newId() {
			instance.setId(randomUUID().toString());
			return this;
		}

		public Builder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder email(final String email) {
			String email2 = null;
			checkArgument(isNotBlank(email2 = trimToNull(email)), "Uninitialized or invalid email");
			instance.setEmail(email2);
			return this;
		}

		public Builder requested(final Date requested) {
			instance.setRequested(checkNotNull(requested, "Uninitialized requested date"));
			return this;
		}

		public Builder channels(final @Nullable Set<String> channels) {
			instance.setChannels(channels);
			return this;
		}

		public Builder fulfilled(final @Nullable Date fulfilled) {
			instance.setFulfilled(fulfilled);
			return this;
		}		

		public SubscriptionRequest build() {
			return instance;
		}

	}

}