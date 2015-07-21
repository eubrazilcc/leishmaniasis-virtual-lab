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
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.LVL_IDENTITY_PROVIDER;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.defaultIdentityProvider;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModificationType.GRANTED;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModificationType.REMOVED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModification;

/**
 * Provides user information (profile).
 * @author Erik Torres <ertorser@upv.es>
 */
public class User implements Serializable {

	private static final long serialVersionUID = 3134362067201756025L;

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
		return Objects.equals(password, other.password)
				&& Objects.equals(salt, other.salt)
				&& equalsToUnprotected(other);
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
		return Objects.equals(pictureUrl, other.pictureUrl)
				&& equalsToAnonymous(other);
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

	@Override
	public int hashCode() {
		return Objects.hash(pictureUrl, provider, userid, password, salt, email, firstname, lastname, roles, permissions, permissionHistory);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
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

}