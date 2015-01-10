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

package eu.eubrazilcc.lvl.storage.security.shiro;

import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.TokenDAO.TOKEN_DAO;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.CredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authc.pam.UnsupportedTokenException;

import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;

/**
 * Security realm that relies on LVL users authenticated through access tokens.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LvlRealm extends BaseAuthorizingRealm {

	public LvlRealm() {
		super(new SimpleCredentialsMatcher());
		// add support for token-based authentication
		setAuthenticationTokenClass(AccessTokenToken.class);		
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
		// validate token
		if (token == null) {
			throw new CredentialsException("Uninitialized token");
		}
		if (!(token instanceof AccessTokenToken)) {
			throw new UnsupportedTokenException("Unsuported token type: " + token.getClass().getCanonicalName());
		}
		// get access token
		final AccessTokenToken accessToken = (AccessTokenToken) token;
		final String secret = trimToNull(accessToken.getToken());
		if (isEmpty(secret)) {
			throw new AccountException("Empty tokens are not allowed in this realm");
		}
		// find token in the LVL OAuth2 database
		String ownerid = null;
		final AtomicReference<String> ownerIdRef = new AtomicReference<String>();
		if (TOKEN_DAO.isValid(secret, ownerIdRef)) {				
			ownerid = ownerIdRef.get();
		}
		if (isEmpty(ownerid)) {
			throw new IncorrectCredentialsException("Incorrect credentials found");
		}
		// find resource owner in the LVL IdP database		
		final ResourceOwner owner = RESOURCE_OWNER_DAO.useGravatar(false).find(ownerid);
		if (owner == null || owner.getUser() == null) {
			throw new UnknownAccountException("No account found for user [" + ownerid + "]");
		}
		return new SimpleAuthenticationInfo(ownerid, secret, getName());
	}	

}