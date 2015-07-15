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

package eu.eubrazilcc.lvl.storage.security;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.LVL_IDENTITY_PROVIDER;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.defaultIdentityProvider;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModificationType.GRANTED;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModificationType.REMOVED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.eubrazilcc.lvl.storage.Linkable;
import eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModification;
import eu.eubrazilcc.lvl.storage.ws.rs.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.storage.ws.rs.jackson.LinkListSerializer;

/**
 * Provides user information (profile). Jackson annotations are included to serialize this class to XML and JSON.
 * Also includes the identity provider and the identifier assigned in the provider.
 * @author Erik Torres <ertorser@upv.es>
 */
public class User implements Serializable, Linkable {

	private static final long serialVersionUID = -8320525767063830149L;	

	@InjectLinks({
		@InjectLink(value="users/{urlSafeOwnerId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeOwnerId", value="${instance.urlSafeOwnerId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links;        // HATEOAS links

	@JsonIgnore
	private String urlSafeOwnerId;   // identity

	private String pictureUrl;       // URL to user's picture

	private String provider;         // identity provider (LVL, LinkedId, etc.)
	private String userid;           // user identity (assigned in the provider)
	private String password;         // password
	private String salt;             // salt to defend the password against dictionary attacks
	private String email;            // email address
	private String firstname;        // (optional) first name
	private String lastname;         // (optional) last name
	private Set<String> roles;       // roles
	private Set<String> permissions; // permissions

	private String industry;         // (optional) the industry the user belongs to
	private Set<String> positions;   // (optional) the user's current position(s)

	private PermissionHistory permissionHistory;

	public User() {
		setProvider(LVL_IDENTITY_PROVIDER);
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

	public String getUrlSafeOwnerId() {
		return urlSafeOwnerId;
	}

	public void setUrlSafeOwnerId(final String urlSafeOwnerId) {
		this.urlSafeOwnerId = urlSafeOwnerId;
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
		setUrlSafeOwnerId(urlEncodeUtf8(toResourceOwnerId(defaultIfBlank(this.provider, LVL_IDENTITY_PROVIDER).trim(), 
				defaultIfBlank(this.userid, "userid").trim())));
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(final String userid) {
		this.userid = userid;
		setUrlSafeOwnerId(urlEncodeUtf8(toResourceOwnerId(defaultIfBlank(this.provider, LVL_IDENTITY_PROVIDER).trim(), 
				defaultIfBlank(this.userid, "userid").trim())));
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
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(final String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(final String lastname) {
		this.lastname = lastname;
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
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(final String industry) {
		this.industry = industry;
	}
	public Set<String> getPositions() {
		return positions;
	}
	public void setPositions(final Set<String> positions) {
		this.positions = positions;
	}
	public PermissionHistory getPermissionHistory() {
		return permissionHistory;
	}
	public void setPermissionHistory(final PermissionHistory permissionHistory) {
		this.permissionHistory = permissionHistory;
	}

	@JsonIgnore
	public String getFormattedName() {
		final String firstname2 = trimToNull(firstname);
		final String lastname2 = trimToNull(lastname);
		return (firstname2 != null ? firstname2 : "") + (firstname2 != null && lastname2 != null ? " " : "") + (lastname2 != null ? lastname2 : "");
	}

	@JsonIgnore
	public void addPemissions(final String... permissions) {
		if (permissions != null) {
			for (final String permission : permissions) {
				String permission2 = null;
				if (isNotBlank(permission2 = trimToNull(permission))) {
					if (getPermissions().add(permission2)) {
						getPermissionHistory().getHistory().add(PermissionModification.builder()
								.permission(permission2)
								.modificationDate(new Date())
								.modificationType(GRANTED)
								.build());
					}
				}
			}
		}
	}

	@JsonIgnore
	public void removePemissions(final String... permissions) {
		if (permissions != null) {
			for (final String permission : permissions) {
				String permission2 = null;
				if (isNotBlank(permission2 = trimToNull(permission))) {
					if (getPermissions().remove(permission2)) {
						getPermissionHistory().getHistory().add(PermissionModification.builder()
								.permission(permission2)
								.modificationDate(new Date())
								.modificationType(REMOVED)
								.build());						
					}
				}
			}
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof User)) {
			return false;
		}
		final User other = User.class.cast(obj);
		return Objects.equals(provider, other.provider)
				&& Objects.equals(userid, other.userid)
				&& Objects.equals(password, other.password)
				&& Objects.equals(salt, other.salt)
				&& Objects.equals(email, other.email)
				&& Objects.equals(firstname, other.firstname)
				&& Objects.equals(lastname, other.lastname)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(permissions, other.permissions)
				&& Objects.equals(industry, other.industry)
				&& Objects.equals(positions, other.positions)
				&& Objects.equals(permissionHistory, other.permissionHistory);
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
				&& Objects.equals(firstname, other.firstname)
				&& Objects.equals(lastname, other.lastname)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(permissions, other.permissions)
				&& Objects.equals(industry, other.industry)
				&& Objects.equals(positions, other.positions)
				&& Objects.equals(permissionHistory, other.permissionHistory);
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
				&& Objects.equals(firstname, other.firstname)
				&& Objects.equals(lastname, other.lastname)
				&& Objects.equals(roles, other.roles)
				&& Objects.equals(permissions, other.permissions)
				&& Objects.equals(industry, other.industry)
				&& Objects.equals(positions, other.positions)
				&& Objects.equals(permissionHistory, other.permissionHistory);
	}

	@Override
	public int hashCode() {
		return Objects.hash(links, pictureUrl, provider, userid, password, salt, email, firstname, lastname, roles, permissions, permissionHistory);
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
				.add("firstname", firstname)
				.add("lastname", lastname)
				.add("roles", roles)
				.add("permissions", permissions)
				.add("industry", industry)
				.add("positions", positions)
				.add("permissionHistory", permissionHistory)
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
			instance.setPositions(new HashSet<String>());
			instance.setPermissionHistory(new PermissionHistory());
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

		public Builder firstname(final String firstname) {
			checkArgument(isNotBlank(firstname), "Uninitialized or invalid firstname");
			instance.setFirstname(firstname.trim());
			return this;
		}

		public Builder lastname(final String lastname) {
			checkArgument(isNotBlank(lastname), "Uninitialized or invalid lastname");
			instance.setLastname(lastname.trim());
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

		public Builder industry(final @Nullable String industry) {
			instance.setIndustry(trimToNull(industry));
			return this;
		}

		public Builder positions(final @Nullable Collection<String> positions) {
			if (positions != null && !isEmpty(positions)) {
				instance.getPositions().addAll(positions);
			}
			return this;
		}

		public Builder permissionHistory(final PermissionHistory permissionHistory) {
			checkArgument(permissionHistory != null, "Uninitialized permission history");
			instance.setPermissionHistory(permissionHistory);
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
					.email(original.email);
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
			if (original.permissionHistory != null) {
				builder.permissionHistory(original.permissionHistory);
			}
			if (isNotBlank(original.firstname)) {
				builder.firstname(original.firstname);
			}
			if (isNotBlank(original.lastname)) {
				builder.lastname(original.lastname);
			}
			if (isNotBlank(original.industry)) {
				builder.industry(original.industry);
			}
			if (original.positions != null && !original.positions.isEmpty()) {
				builder.positions(original.positions);
			}
			return builder.build();
		}
		return copy;
	}

}