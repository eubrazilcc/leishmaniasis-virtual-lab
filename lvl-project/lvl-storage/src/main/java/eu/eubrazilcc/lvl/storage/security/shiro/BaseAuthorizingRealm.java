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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static eu.eubrazilcc.lvl.storage.activemq.ActiveMQConnector.ACTIVEMQ_CONN;
import static eu.eubrazilcc.lvl.storage.activemq.TopicHelper.permissionChangedTopic;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.getIdentityProvider;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.security.shiro.cache.GuavaCacheManager;

/**
 * Provides common security realm methods that are used in this application. This class only implements authorization (access control) 
 * behavior and leaves authentication support (log-in) operations to subclasses. In addition, this class enables authentication caching 
 * based on EhCache, therefore the subclasses derived from this class MUST return the credentials securely obfuscated and NOT plaintext 
 * (raw) credentials. Credential matching is based on hashing comparison. 
 * @author Erik Torres <ertorser@upv.es>
 * @see {@link AuthorizingRealm}
 * @see <ahref="http://ehcache.org/">Ehcache</a>
 */
public abstract class BaseAuthorizingRealm extends AuthorizingRealm {

	private static final Logger LOGGER = getLogger(LvlBasicRealm.class);

	private static final CacheManager CACHE_MANAGER = new GuavaCacheManager();

	public static final long CONNECTION_TIMEOUT = 30000l; // 30 seconds

	protected long connectionTimeout;

	public BaseAuthorizingRealm(final CredentialsMatcher credentialsMatcher, final String identityProvider) {
		super(CACHE_MANAGER, credentialsMatcher);
		// enable authentication caching
		setAuthenticationCachingEnabled(true);
		// subscribe to changes in users' permissions
		ACTIVEMQ_CONN.subscribe(permissionChangedTopic(identityProvider), new MessageListener() {
			@Override
			public void onMessage(final Message message) {
				if (message instanceof TextMessage) {
					final TextMessage textMessage = (TextMessage) message;						
					try {
						final String ownerId = textMessage.getText();
						clearCache(new SimplePrincipalCollection(ownerId, getName()));
						LOGGER.trace(getName() + " - Cached authorization info was evicted for account: " + ownerId);						
					} catch (JMSException e) {
						LOGGER.error(getName() + " - Failed to read message", e);
					}
				}
			}
		});
	}

	public long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(final long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principals) {
		checkNotNull(principals, "Uninitialized principals collection");
		// get owner Id
		final String ownerid = trimToNull((String) principals.getPrimaryPrincipal());
		checkState(isNotEmpty(ownerid), "Empty principals are not allowed in this realm");
		// use identity provider to filter realms 
		final String provider = getIdentityProvider(ownerid);
		checkState(isNotEmpty(provider), "Failed to extract identity provider from principal");
		if (!getName().startsWith(provider)) {
			return null;
		}
		// find resource owner in the LVL IdP database
		final ResourceOwner owner = RESOURCE_OWNER_DAO.useGravatar(false).find(ownerid);
		checkState(owner != null && owner.getUser() != null, "No account found for resource owner [" + ownerid + "]");
		// get user roles and permissions from the resource owner
		final SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(owner.getUser().getRoles());
		info.setStringPermissions(owner.getUser().getPermissions());
		return info;
	}

}