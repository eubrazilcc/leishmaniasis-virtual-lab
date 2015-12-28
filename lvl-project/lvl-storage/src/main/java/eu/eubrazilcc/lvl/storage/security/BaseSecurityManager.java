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

import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.security.el.PermissionElBuilder.EL_PARAMETER_PATTERN;
import static eu.eubrazilcc.lvl.storage.security.el.PermissionElBuilder.buildPermission;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.security.shiro.AccessTokenToken;

/**
 * Base implementation of the security manager using Apache Shiro.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class BaseSecurityManager {	

	private final Subject currentUser;
	private ResourceOwner owner;

	protected BaseSecurityManager(final Subject currentUser) {
		this.currentUser = currentUser;
	}

	public final void login(final String username, final String password) {
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		// token.setRememberMe(true);
		currentUser.login(token);
		token.clear();
		token = null;
		owner = RESOURCE_OWNER_DAO.find(getPrincipal());
	}

	public final void login(final String accessToken) {
		AccessTokenToken token = new AccessTokenToken(accessToken, false); // true);
		currentUser.login(token);
		token.clear();
		token = null;
		owner = RESOURCE_OWNER_DAO.find(getPrincipal());
	}

	public final void logout() {
		currentUser.logout();
		owner = null;
	}
	
	public final boolean isAuthenticated() {
		return currentUser.isAuthenticated();
	}

	@Nullable
	public final String getPrincipal() {
		final Object principal = currentUser.getPrincipal();
		return (principal != null) && (principal instanceof String) ? (String)principal : null;
	}
	
	@Nullable
	public final String getEmail() {
		return owner != null && owner.getUser() != null ? owner.getUser().getEmail() : null;
	}

	public final boolean hasRole(final String roleIdentifier) {
		return currentUser.hasRole(roleIdentifier);
	}

	public final boolean hasAllRoles(final Collection<String> roleIdentifiers) {
		return currentUser.hasAllRoles(roleIdentifiers);
	}

	public final boolean isPermitted(final String permission) {
		return currentUser.isPermitted(parsePermission(owner, permission));
	}

	public final boolean isPermitted(final String... permissions) {
		return currentUser.isPermittedAll(parsePermissions(owner, permissions));
	}

	public final boolean isPermittedAll(final String... permissions) {
		return currentUser.isPermittedAll(parsePermissions(owner, permissions));
	}

	private static String parsePermission(final ResourceOwner owner, final String permission) {
		String permission2 = permission;
		if (permission != null && owner != null && permission.matches(EL_PARAMETER_PATTERN)) {
			permission2 = buildPermission(permission, owner.getUser());
		}
		return permission2;
	}

	private static String[] parsePermissions(final ResourceOwner owner, final String... permissions) {
		String[] permissions2 = permissions;
		if (permissions != null) {
			for (int i = 0; i < permissions2.length; i++) {
				permissions2[i] = parsePermission(owner, permissions2[i]);
			}
		}		
		return permissions2;
	}

}