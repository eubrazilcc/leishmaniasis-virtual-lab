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

package eu.eubrazilcc.lvl.storage.oauth2;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkSerializer;

/**
 * User identity provider. Include JAXB annotations to serialize this class to XML and JSON.
 * Most JSON processing libraries like Jackson support these JAXB annotations.
 * @author Erik Torres <ertorser@upv.es>
 */
public class User implements Serializable, Linkable<User> {

	private static final long serialVersionUID = -8320525767063830149L;

	@InjectLinks({
		@InjectLink(value="users/{id}", rel="self", bindings={@Binding(name="id", value="${instance.username}")})
	})
	@JsonSerialize(using = LinkSerializer.class)
	@JsonDeserialize(using = LinkDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private String pictureUrl;

	private String username;
	private String password;
	private String email;
	private String fullname;
	private Set<String> scopes;
	private String salt;

	public User() {
		links = newArrayList();
	}

	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(final List<Link> links) {
		if (links != null && !links.isEmpty()) {
			this.links = newArrayList(links);
		} else {
			this.links = newArrayList();
		}
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(final String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(final String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(final String email) {
		this.email = email;
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(final String fullname) {
		this.fullname = fullname;
	}
	public Set<String> getScopes() {
		return scopes;
	}
	public void setScopes(final Set<String> scopes) {
		this.scopes = scopes;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(final String salt) {
		this.salt = salt;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof User)) {
			return false;
		}
		final User other = User.class.cast(obj);
		return Objects.equal(links, other.links)
				&& Objects.equal(pictureUrl, other.pictureUrl)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(username, other.username)
				&& Objects.equal(password, other.password)
				&& Objects.equal(email, other.email)				
				&& Objects.equal(fullname, other.fullname)
				&& Objects.equal(scopes, other.scopes)
				&& Objects.equal(salt, other.salt);
	}

	/**
	 * Compare two instances of this class using identity fields.
	 * @param other - the instance to be compared to.
	 * @return {@code true} if all the identity attributes of both instances coincide in value. Otherwise, {@code false}.
	 */
	public boolean equalsToAnonymous(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(username, other.username)			
				&& Objects.equal(fullname, other.fullname)
				&& Objects.equal(scopes, other.scopes);
	}

	/**
	 * Ignores password and salt fields when comparing two instances of this class. Use this method when comparing an instance of this class that contains
	 * an unprotected password (password in plain text and no salt) with a protected one (hashed password with a valid salt).
	 * @param other - the instance to be compared to.
	 * @return {@code true} if all the attributes of both instances coincide in value with the sole exception of those considered part of the password. 
	 *        Otherwise, {@code false}.
	 */
	public boolean equalsToUnprotected(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(links, other.links)
				&& Objects.equal(pictureUrl, other.pictureUrl)
				&& equalsToUnprotectedIgnoringVolatile(other);
	}

	/**
	 * Ignores password, salt and volatile fields when comparing two instances of this class.
	 * @param other - the instance to be compared to.
	 * @return {@code true} if all the attributes of both instances coincide in value with the sole exception of those considered part of the password
	 *        and the volatile ones. Otherwise, {@code false}.
	 */
	public boolean equalsToUnprotectedIgnoringVolatile(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equal(username, other.username)
				&& Objects.equal(email, other.email)				
				&& Objects.equal(fullname, other.fullname)
				&& Objects.equal(scopes, other.scopes);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(links, pictureUrl, username, password, email, fullname, scopes, salt);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("link", links)
				.add("pictureUrl", pictureUrl)
				.add("username", username)
				.add("password", password)
				.add("email", email)				
				.add("fullname", fullname)
				.add("scopes", scopes)
				.add("salt", salt)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final User instance = new User();

		public Builder() {			
			instance.setScopes(new HashSet<String>());
		}

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder pictureUrl(final String pictureUrl) {
			instance.setPictureUrl(pictureUrl);
			return this;
		}

		public Builder username(final String username) {
			checkArgument(isNotBlank(username), "Uninitialized or invalid username");
			instance.setUsername(username);
			return this;
		}

		public Builder password(final String password) {
			checkArgument(isNotBlank(password), "Uninitialized or invalid password");
			instance.setPassword(password);
			return this;
		}

		public Builder email(final String email) {
			checkArgument(isNotBlank(email), "Uninitialized or invalid email");
			instance.setEmail(email);
			return this;
		}

		public Builder fullname(final String fullname) {
			checkArgument(isNotBlank(fullname), "Uninitialized or invalid fullname");
			instance.setFullname(fullname);
			return this;
		}

		public Builder scope(final String scope) {
			checkArgument(isNotBlank(scope), "Uninitialized or invalid scope");
			instance.getScopes().add(scope);
			return this;
		}

		public Builder scopes(final Collection<String> scopes) {
			checkArgument(scopes != null && !isEmpty(scopes), "Uninitialized scopes");
			instance.getScopes().addAll(scopes);
			return this;
		}

		public Builder salt(final String salt) {
			checkArgument(isNotBlank(salt), "Uninitialized or invalid salt");
			instance.setSalt(salt);
			return this;
		}

		public User build() {
			return instance;
		}

	}

	/**
	 * Performs a deep copy of the input instance.
	 * @param original - the original instance to be copied.
	 * @return a deep copy of the input instance.
	 */
	public static final User copyOf(final User original) {
		User copy = null;
		if (original != null) {
			final Builder builder = builder()
					.pictureUrl(original.pictureUrl)
					.username(original.username)
					.password(original.password)
					.email(original.email)
					.fullname(original.fullname);
			if (original.getLinks() != null) {
				final List<Link> linksCopy = newArrayList();
				for (final Link link : original.getLinks()) {
					linksCopy.add(Link.fromLink(link).build());					
				}				
				builder.links(linksCopy);
			}
			if (original.getScopes() != null) {
				builder.scopes(original.scopes);
			}
			if (isNotBlank(original.salt)) {
				builder.salt(original.salt);
			}
			return builder.build();
		}
		return copy;
	}

}