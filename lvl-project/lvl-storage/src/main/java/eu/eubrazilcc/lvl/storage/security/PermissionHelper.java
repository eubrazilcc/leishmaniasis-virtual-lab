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

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Ordering.natural;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.assertValidResourceOwnerId;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.core.Shareable.SharedAccess;

/**
 * Helper class to handle access permissions and roles.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class PermissionHelper {

	/**
	 * Users with this role will be granted with full access to the system (data, storage, computing).
	 */
	public static final String ADMIN_ROLE = "admin";

	/**
	 * Users with this role will be granted with permissions for accessing the public parts of the infrastructure for
	 * viewing (edition and creation of new items will be restricted to users with additional roles or permissions) 
	 * and to a workspace specific to each user where they will be able of storing their private data (unpublished 
	 * sequences, papers, pipelines, etc.) in a secure way.
	 */
	public static final String USER_ROLE = "user";

	/**
	 * Users with this role will be granted with additional permissions for editing and creating new data items in 
	 * the system.
	 */
	public static final String DATA_CURATOR_ROLE  = "curator";

	public static final char PERMISSIONS_SEPARATOR      = ' ';
	public static final char PERMISSION_LEVEL_SEPARATOR = ':';
	public static final char PERMISSION_VALUE_SEPARATOR = ',';

	public static final int NUMBER_OF_PERMISSIONS_GRANTED_TO_REGULAR_USERS = 14;

	/**
	 * Permissions granted to the users with the role {@link #ADMIN_ROLE}.
	 * @return The set of permissions that grant users with the role {@link #ADMIN_ROLE} to access all the parts of the
	 *         system (including data) without restrictions.
	 */
	public static final String allPermissions() {
		return "*";
	}

	/**
	 * Any authenticated user will be granted with this basic set of permissions, regardless of the roles assigned to him/her 
	 * in the system. Even a user with no roles (guest) also will be granted with this permissions.
	 * @return The basic set of permissions that will be granted to any authenticated user, regardless of the roles assigned
	 *         to him/her in the system.
	 */
	public static final String defaultPermissions() {
		return on(PERMISSIONS_SEPARATOR).skipNulls().join(
				"sequences:*:public:*:view",
				"samples:*:public:*:view",
				"citations:*:public:*:view",
				"pipelines:*:public:*:view",
				"datasets:*:public:*:view");
	}

	/**
	 * Beyond the {@link #defaultPermissions() default permissions}, any user with the role {@link #USER_ROLE} will be granted 
	 * with this additional set of permissions to allow access to the public parts of the system as well as to a private workspace.
	 * @param ownerid - resource owner identifier of the user who will be granted with these permissions
	 * @return The additional set of permissions (beyond the {@link #defaultPermissions() default ones}) that will be granted
	 *         to users with the role {@link #USER_ROLE}.
	 */
	public static final String userPermissions(final String ownerid) {
		final String ownerid2 = assertValidResourceOwnerId(ownerid);
		return on(PERMISSIONS_SEPARATOR).skipNulls().join(
				defaultPermissions(),
				"users:*:*:" + ownerid2 + ":view",
				"users:active:profile:" + ownerid2 + ":view,edit",
				"users:active:profile:*:view",
				"sequences:*:" + ownerid2 + ":*:view,edit,create",
				"citations:*:" + ownerid2 + ":*:view,edit,create",
				"datasets:files:" + ownerid2 + ":*:view,edit,create",
				"pipelines:runs:" + ownerid2 + ":*:view,edit,create",
				"notifications:*:" + ownerid2 + ":*:view,edit",
				"saved:searches:" + ownerid2 + ":*:view,edit,create");
	}

	/**
	 * Permissions granted to the users with the role {@link #DATA_CURATOR_ROLE} who will be allowed to edit and create new data 
	 * items in the system.
	 * @param ownerid - resource owner identifier of the user who will be granted with these permissions
	 * @return The additional set of permissions (beyond the {@link #userPermissions() user ones}) that will be granted
	 *         to users with the role {@link #DATA_CURATOR_ROLE}.
	 */
	public static final String dataCuratorPermissions(final String ownerid) {
		return on(PERMISSIONS_SEPARATOR).skipNulls().join(
				userPermissions(ownerid),
				"sequences:*:*:*:view,edit,create",
				"citations:*:*:*:view,edit,create",
				"tasks:data:maintenance:*:view,edit,create",
				"notifications:data:*:*:view,edit,create");
	}

	public static final String datasetSharePermission(final DatasetShare share) {
		String namespace = null, filename = null;
		SharedAccess accessType = null;
		checkArgument(share != null && isNotBlank(namespace = trimToNull(share.getNamespace())) 
				&& isNotBlank(filename = trimToNull(share.getFilename()))
				&& (accessType = Optional.of(share.getAccessType()).or(VIEW_SHARE)) != null, "Uninitialized or invalid data share");
		return "datasets:files:" + namespace + ":" + filename + ":view" + (accessType.equals(EDIT_SHARE) ? ",edit" : "");
	}

	public static final String asOAuthString(final String... permissions) {
		final List<String> collection = newArrayList();
		if (permissions != null && permissions.length > 0) {
			collection.addAll(asList(permissions));			
		}
		return asOAuthString(collection);
	}

	public static final String asOAuthString(final Collection<String> collection) {
		return asOAuthString(collection, false);
	}

	public static final String asOAuthString(final Collection<String> collection, final boolean sort) {
		checkArgument(collection != null && !collection.isEmpty(), "Uninitialized or invalid scope collection");
		final Iterable<String> filtered = filter(transform(collection, new Function<String, String>() {
			@Override
			public String apply(final String permission) {
				return trimToNull(permission);
			}			
		}), notNull());
		return on(PERMISSIONS_SEPARATOR).skipNulls()
				.join(sort ? natural().nullsFirst().immutableSortedCopy(filtered) : filtered);
	}

	public static final List<String> asPermissionList(final String permissions) {
		checkArgument(isNotBlank(permissions), "Uninitialized or invalid permissions");
		return Splitter.on(PERMISSIONS_SEPARATOR)
				.omitEmptyStrings()
				.trimResults()
				.splitToList(permissions);
	}

	public static final Set<String> asPermissionSet(final String permissions) {
		final List<String> list = asPermissionList(permissions);
		if (list != null) {
			return newHashSet(list);
		}
		return newHashSet();
	}

	public static boolean hasRole(final String roleIdentifier, final User user) {
		return user != null && user.getRoles() != null && user.getRoles().contains(roleIdentifier);
	}

}