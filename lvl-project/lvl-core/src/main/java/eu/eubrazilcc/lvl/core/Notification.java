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

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.System.currentTimeMillis;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * Represents a notification that can be stored, for example, in the application database or sent to the 
 * user through the network serialized as a JSON object.
 * @author Erik Torres <ertorser@upv.es>
 */
public class Notification {

	private String id; // optional identifier that is normally assigned to store the notification permanently	
	private Priority priority;
	private String addressee;
	private String scope;
	private long issuedAt;
	private String message;
	private Action action;

	public Notification() {
		this.priority = Priority.NORMAL;
		this.issuedAt = currentTimeMillis() / 1000l;
	}

	public @Nullable String getId() {
		return id;
	}
	public void setId(final @Nullable String id) {
		this.id = id;
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
	public long getIssuedAt() {
		return issuedAt;
	}
	public void setIssuedAt(final long issuedAt) {
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
		return Objects.equal(id, other.id)
				&& equalsIgnoreId(other);
	}

	public boolean equalsIgnoreId(final Notification other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(priority, other.priority)
				&& Objects.equal(addressee, other.addressee)
				&& Objects.equal(scope, other.scope)
				&& Objects.equal(issuedAt, other.issuedAt)
				&& Objects.equal(message, other.message)
				&& Objects.equal(action, other.action);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, priority, addressee, scope, issuedAt, message, action);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
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

		public Builder id(final String id) {
			instance.setId(id);
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
		
		public Builder issuedAt(final long issuedAt) {
			checkArgument(issuedAt >= 0l, "Invalid issued at");
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
			return Objects.equal(link, other.link)
					&& Objects.equal(text, other.text);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(link, text);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
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