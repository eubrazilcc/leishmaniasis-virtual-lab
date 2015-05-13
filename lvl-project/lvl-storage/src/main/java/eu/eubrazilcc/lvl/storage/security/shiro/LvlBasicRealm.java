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

package eu.eubrazilcc.lvl.storage.security.shiro;

import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.LVL_IDENTITY_PROVIDER;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.HASH_ALGORITHM;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.KEY_DEVIATION_ITERATIONS;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.decodeHex;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;

import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;

/**
 * Security realm that relies on LVL users authenticated through username/password.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LvlBasicRealm extends BaseAuthorizingRealm {	

	private static final CredentialsMatcher CREDENTIAL_MATCHER = new HashedCredentialsMatcher();
	static {
		final HashedCredentialsMatcher matcher = HashedCredentialsMatcher.class.cast(CREDENTIAL_MATCHER);
		matcher.setHashAlgorithmName(HASH_ALGORITHM);
		matcher.setHashIterations(KEY_DEVIATION_ITERATIONS);		
	}

	public LvlBasicRealm() {
		super(CREDENTIAL_MATCHER, LVL_IDENTITY_PROVIDER);
		// add support for username/password authentication
		setAuthenticationTokenClass(UsernamePasswordToken.class);
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
		// validate token
		if (token == null) {
			throw new CredentialsException("Uninitialized token");
		}
		if (!(token instanceof UsernamePasswordToken)) {
			throw new UnsupportedTokenException("Unsuported token type: " + token.getClass().getCanonicalName());
		}
		// get user name
		final UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;
		final String username = trimToNull(usernamePasswordToken.getUsername());
		if (isEmpty(username)) {
			throw new AccountException("Empty usernames are not allowed in this realm");
		}
		// find resource owner in the LVL IdP database
		final String ownerid = toResourceOwnerId(LVL_IDENTITY_PROVIDER, username);
		final ResourceOwner owner = RESOURCE_OWNER_DAO.useGravatar(false).find(ownerid);
		if (owner == null || owner.getUser() == null) {
			throw new UnknownAccountException("No account found for user [" + username + "]");
		}
		return new SimpleAuthenticationInfo(ownerid, owner.getUser().getPassword().toCharArray(),
				decodeHex(owner.getUser().getSalt()), getName());		
	}	

}