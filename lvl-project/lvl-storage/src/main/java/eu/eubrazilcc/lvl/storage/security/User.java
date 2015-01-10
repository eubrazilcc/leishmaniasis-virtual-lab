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

package eu.eubrazilcc.lvl.storage.security;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.defaultIdentityProvider;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.core.Linkable;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.core.json.jackson.LinkListSerializer;

/**
 * Provides user information (profile). Jackson annotations are included to serialize this class to XML and JSON.
 * Also includes the identity provider and the identifier assigned in the provider.
 * @author Erik Torres <ertorser@upv.es>
 */
public class User implements Serializable, Linkable<User> {

	private static final long serialVersionUID = -8320525767063830149L;	

	@InjectLinks({
		@InjectLink(value="users/{id}", rel=SELF, type=APPLICATION_JSON, bindings={@Binding(name="id", value="${instance.userid}")})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links;        // HATEOAS links

	private String pictureUrl;       // URL to user's picture

	private String provider;         // identity provider (LVL, LinkedId, etc.)
	private String userid;           // user identity (assigned in the provider)
	private String password;         // password
	private String salt;             // salt to defend the password against dictionary attacks
	private String email;            // email address
	private String fullname;         // (optional) full name
	private Set<String> roles;       // roles
	private Set<String> permissions; // permissions

	public User() { }

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

	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(final String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}	
	public String getProvider() {
		return provider;
	}
	public void setProvider(final String provider) {
		this.provider = provider;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(final String userid) {
		this.userid = userid;
	}	
	public String getPassword() {
		return password;
	}
	public void setPassword(final String password) {
		this.password = password;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(final String salt) {
		this.salt = salt;
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
	public Set<String> getRoles() {
		return roles;
	}
	public void setRoles(final Set<String> roles) {
		this.roles = roles;
	}
	public Set<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(final Set<String> permissions) {
		this.permissions = permissions;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof User)) {
			return false;
		}
		final User other = User.class.cast(obj);
		return Objects.equals(links, other.links)
				&& Objects.equals(pictureUrl, other.pictureUrl)
				&& equalsIgnoringVolatile(other);
	}

	@Override
	public boolean equalsIgnoringVolatile(final User other) {
		if (other == null) {
			return false;
		}
		return Objects.equals(provider, other.provider)
				&& Objects.equals(userid, other.userid)
				&& Objects.equals(password, other.password)
				&& Objects.equals(salt, other.salt)
				&& Objects.equals(email, other.email)
				&& Objects.equals(fullname, other.fullname)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(permissions, other.permissions);
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
		return Objects.equals(provider, other.provider)
				&& Objects.equals(userid, other.userid)
				&& Objects.equals(email, other.email)							
				&& Objects.equals(fullname, other.fullname)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(permissions, other.permissions);
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
		return Objects.equals(links, other.links)
				&& Objects.equals(pictureUrl, other.pictureUrl)
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
		return Objects.equals(provider, other.provider)
				&& Objects.equals(userid, other.userid)
				&& Objects.equals(email, other.email)										
				&& Objects.equals(fullname, other.fullname)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(permissions, other.permissions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, pictureUrl, provider, userid, password, salt, email, fullname, roles, permissions);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("link", links)
				.add("pictureUrl", pictureUrl)							
				.add("provider", provider)
				.add("userid", userid)
				.add("password", password)
				.add("salt", salt)
				.add("email", email)
				.add("fullname", fullname)
				.add("roles", roles)
				.add("permissions", permissions)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final User instance = new User();

		public Builder() {
			instance.setProvider(defaultIdentityProvider());
			instance.setRoles(new HashSet<String>());
			instance.setPermissions(new HashSet<String>());
		}

		public Builder links(final List<Link> links) {
			instance.setLinks(links);
			return this;
		}

		public Builder pictureUrl(final String pictureUrl) {
			instance.setPictureUrl(pictureUrl);
			return this;
		}		

		public Builder provider(final String provider) {
			checkArgument(isNotBlank(provider), "Uninitialized or invalid provider");
			instance.setProvider(provider.trim());
			return this;
		}		

		public Builder userid(final String userid) {
			checkArgument(isNotBlank(userid), "Uninitialized or invalid user identifier");
			instance.setUserid(userid.trim());
			return this;
		}

		public Builder password(final String password) {
			checkArgument(isNotBlank(password), "Uninitialized or invalid password");
			instance.setPassword(password.trim());
			return this;
		}

		public Builder salt(final String salt) {
			checkArgument(isNotBlank(salt), "Uninitialized or invalid salt");
			instance.setSalt(salt.trim());
			return this;
		}

		public Builder email(final String email) {
			checkArgument(isNotBlank(email), "Uninitialized or invalid email");
			instance.setEmail(email.trim());
			return this;
		}

		public Builder fullname(final String fullname) {
			checkArgument(isNotBlank(fullname), "Uninitialized or invalid fullname");
			instance.setFullname(fullname.trim());
			return this;
		}

		public Builder role(final String role) {
			checkArgument(isNotBlank(role), "Uninitialized or invalid role");
			instance.getRoles().add(role);
			return this;
		}

		public Builder roles(final Collection<String> roles) {
			checkArgument(roles != null && !isEmpty(roles), "Uninitialized roles");
			instance.getRoles().addAll(roles);
			return this;
		}

		public Builder permission(final String permission) {
			checkArgument(isNotBlank(permission), "Uninitialized or invalid permission");
			instance.getPermissions().add(permission);
			return this;
		}

		public Builder permissions(final Collection<String> permissions) {
			checkArgument(permissions != null && !isEmpty(permissions), "Uninitialized permissions");
			instance.getPermissions().addAll(permissions);
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
					.provider(original.provider)
					.userid(original.userid)
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
			if (isNotBlank(original.salt)) {
				builder.salt(original.salt);
			}
			if (original.roles != null && !original.roles.isEmpty()) {
				builder.roles(original.roles);
			}
			if (original.permissions != null && !original.permissions.isEmpty()) {
				builder.permissions(original.permissions);
			}
			return builder.build();
		}
		return copy;
	}

}