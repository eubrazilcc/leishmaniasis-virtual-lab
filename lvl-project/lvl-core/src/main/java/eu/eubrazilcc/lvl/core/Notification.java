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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
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

import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Represents a notification that can be stored, for example, in the application database or sent to the 
 * user through the network serialized as a JSON object.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Notification implements Linkable<Notification> {

	@InjectLinks({
		@InjectLink(value="notifications/{urlSafeNamespace}/{urlSafeId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeNamespace", value="${instance.urlSafeNamespace}"),
				@Binding(name="urlSafeId", value="${instance.urlSafeId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	@JsonIgnore
	private String urlSafeNamespace;
	@JsonIgnore
	private String urlSafeId;

	private String namespace; // Name space where the record is inscribed
	private String id;        // Resource identifier
	private Priority priority;
	private String addressee;
	private String scope;
	private Date issuedAt;
	private String message;
	private Action action;

	public Notification() {
		setNamespace(LVL_DEFAULT_NS);
		this.priority = Priority.NORMAL;
		this.issuedAt = new Date();
	}

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

	public String getUrlSafeNamespace() {
		return urlSafeNamespace;
	}

	public void setUrlSafeNamespace(final String urlSafeNamespace) {
		this.urlSafeNamespace = urlSafeNamespace;
	}

	public String getUrlSafeId() {
		return urlSafeId;
	}

	public void setUrlSafeId(final String urlSafeId) {
		this.urlSafeId = urlSafeId;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
		setUrlSafeNamespace(urlEncodeUtf8(defaultIfBlank(namespace, LVL_DEFAULT_NS).trim()));
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
		setUrlSafeId(id != null ? urlEncodeUtf8(trimToEmpty(id)) : id);
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(final Priority priority) {
		this.priority = priority;
	}

	public String getAddressee() {
		return addressee;
	}

	public void setAddressee(final String addressee) {
		this.addressee = addressee;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(final String scope) {
		this.scope = scope;
	}

	public Date getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(final Date issuedAt) {
		this.issuedAt = issuedAt;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(final Action action) {
		this.action = action;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Notification)) {
			return false;
		}
		final Notification other = Notification.class.cast(obj);
		return Objects.equals(links, other.links)
				&& equalsIgnoringVolatile(other);		
	}

	@Override
	public boolean equalsIgnoringVolatile(final Notification other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(namespace, other.namespace)
				&& Objects.equals(id, other.id)
				&& Objects.equals(priority, other.priority)
				&& Objects.equals(addressee, other.addressee)
				&& Objects.equals(scope, other.scope)
				&& Objects.equals(issuedAt, other.issuedAt)
				&& Objects.equals(message, other.message)
				&& Objects.equals(action, other.action);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, namespace, id, priority, addressee, scope, issuedAt, message, action);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("links", links)
				.add("namespace", namespace)
				.add("id", id)
				.add("priority", priority)
				.add("addressee", addressee)
				.add("scope", scope)
				.add("issuedAt", issuedAt)
				.add("message", message)
				.add("action", action)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final Notification instance = new Notification();

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder namespace(final String namespace) {
			instance.setNamespace(trimToEmpty(namespace));
			return this;
		}

		public Builder id(final String id) {
			String id2 = null;
			checkArgument(isNotBlank(id2 = trimToNull(id)), "Uninitialized or invalid id");
			instance.setId(id2);
			return this;
		}

		public Builder priority(final Priority priority) {
			instance.setPriority(priority);
			return this;
		}

		public Builder addressee(final String addressee) {
			instance.setAddressee(addressee);
			return this;
		}

		public Builder scope(final String scope) {
			instance.setScope(scope);
			return this;
		}

		public Builder issuedAt(final Date issuedAt) {
			checkArgument(issuedAt != null, "Invalid issued at");
			instance.setIssuedAt(issuedAt);
			return this;
		}

		public Builder message(final String message) {
			instance.setMessage(message);
			return this;
		}

		public Builder action(final Action action) {
			instance.setAction(action);
			return this;
		}

		public Notification build() {
			return instance;
		}

	}

	public static final class Action {

		private String link;
		private String text;

		/**
		 * Serialization-friendly constructor.
		 */
		public Action() { }

		public Action(final String link, final String text) {
			this.link = link;
			this.text = text;
		}

		public String getLink() {
			return link;
		}
		public void setLink(final String link) {
			this.link = link;
		}
		public String getText() {
			return text;
		}
		public void setText(final String text) {
			this.text = text;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof Action)) {
				return false;
			}
			final Action other = Action.class.cast(obj);
			return Objects.equals(link, other.link)
					&& Objects.equals(text, other.text);
		}

		@Override
		public int hashCode() {
			return Objects.hash(link, text);
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("link", link)
					.add("text", text)
					.toString();
		}	
	}

	public static enum Priority {
		/**
		 * Highest priority, for items that require the receiver's prompt attention.
		 */
		CRITICAL(1),
		/**
		 * Higher priority, for important alerts.
		 */
		HIGH(2),
		/**
		 * Default notification priority.
		 */
		NORMAL(3),
		/**
		 * Lower priority, for items that are less important.
		 */
		LOW(4);

		private int numVal;

		private Priority(int numVal) {
			this.numVal = numVal;
		}

		public int getNumVal() {
			return numVal;
		}		
	}

}