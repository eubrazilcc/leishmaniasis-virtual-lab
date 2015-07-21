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

package eu.eubrazilcc.lvl.storage.oauth2;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.addCallback;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.SELF;
import static eu.eubrazilcc.lvl.storage.activemq.ActiveMQConnector.ACTIVEMQ_CONN;
import static eu.eubrazilcc.lvl.storage.activemq.TopicHelper.permissionChangedTopic;
import static eu.eubrazilcc.lvl.storage.base.StorageDefaults.TIMEOUT;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.indexModel;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.nonUniqueIndexModel;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.LVL_IDENTITY_PROVIDER;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.ADMIN_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.hashAndSaltPassword;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.InjectLink;
import org.glassfish.jersey.linking.InjectLinks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.storage.base.DeleteOptions;
import eu.eubrazilcc.lvl.storage.base.LvlObject;
import eu.eubrazilcc.lvl.storage.base.SaveOptions;
import eu.eubrazilcc.lvl.storage.gravatar.Gravatar;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer;
import eu.eubrazilcc.lvl.storage.security.User;
import eu.eubrazilcc.lvl.storage.ws.rs.jackson.LinkListDeserializer;
import eu.eubrazilcc.lvl.storage.ws.rs.jackson.LinkListSerializer;

/**
 * Simple implementation of the OAuth 2.0 resource owner using username+password. Jackson annotations are included to serialize this class 
 * to XML and JSON.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ResourceOwner extends LvlObject implements UserType, Serializable {

	private static final long serialVersionUID = 459055135060565086L;

	public static final String COLLECTION     = "resource_owners";
	public static final String LVL_USER_EMAIL = "user.email";
	/**
	 * <strong>Note</strong> indexing a field that holds an array value has side effects. To change any field of this class you should check
	 * the compatibility with this kind of indexes.
	 * @see <a href="http://docs.mongodb.org/manual/core/index-multikey/">mongoDB Multikey Indexes</a>
	 */
	public static final String LVL_USER_PERMISSIONS = "user.permissions";

	public static final String ADMIN_USER  = "root";
	public static final String SYSTEM_USER = "system";

	private static final Map<String, String> SYS_ACCOUNTS = new ImmutableMap.Builder<String, String>()
			.put(ADMIN_USER  + ".password", "changeit")
			.put(ADMIN_USER  + ".email",    "root@example.com")			
			.put(SYSTEM_USER + ".password", "changeit")
			.put(SYSTEM_USER + ".email",    "system@example.com")			
			.build();

	public static final MongoCollectionConfigurer CONFIGURER = new MongoCollectionConfigurer(COLLECTION, true, newArrayList(
			indexModel(LVL_USER_EMAIL),
			nonUniqueIndexModel(LVL_USER_PERMISSIONS, false)),
			new Callable<Void>() {
		@Override
		public Void call() throws Exception {
			final ResourceOwners owners = new ResourceOwners();
			final ListenableFuture<Integer> future = owners.fetch(0, 0, null, null, ImmutableMap.<String, Boolean>of(LVL_GUID_FIELD, true));
			addCallback(future, new FutureCallback<Integer>() {
				@Override
				public void onSuccess(final Integer result) {
					if (owners.collection().size() == 0) {
						final String[] users = { ADMIN_USER, SYSTEM_USER };
						for (final String username : users) {
							final ResourceOwner owner = ResourceOwner.builder()
									.user(User.builder()
											.userid(username)
											.password(SYS_ACCOUNTS.get(username  + ".password"))
											.email(SYS_ACCOUNTS.get(username  + ".email"))
											.firstname(capitalize(username))
											.lastname("LVL User")
											.role(ADMIN_ROLE)
											.permissions(asPermissionList(allPermissions()))
											.build())
											.build();
							final String[] shadowed = hashAndSaltPassword(owner.getUser().getPassword());			
							owner.getUser().setSalt(shadowed[0]);
							owner.getUser().setPassword(shadowed[1]);
							try {
								owner.save().get(TIMEOUT, SECONDS);
							} catch (Exception e) {
								throw new IllegalStateException("Failed to create account: " + username, e);
							}
						}						
					}
				}
				@Override
				public void onFailure(final Throwable t) {
					throw new IllegalStateException("Failed to create root and system accounts in the database", t);
				}				
			});
			return null;
		}		
	});

	@InjectLinks({
		@InjectLink(value="users/{urlSafeOwnerId}", rel=SELF, type=APPLICATION_JSON, bindings={
				@Binding(name="urlSafeOwnerId", value="${instance.urlSafeOwnerId}")
		})
	})
	@JsonSerialize(using = LinkListSerializer.class)
	@JsonDeserialize(using = LinkListDeserializer.class)
	@JsonProperty("links")
	private List<Link> links; // HATEOAS links

	private User user;

	public ResourceOwner() {
		super(COLLECTION, CONFIGURER, getLogger(ResourceOwner.class));
	}

	@Override
	public List<Link> getLinks() {
		return links;
	}

	@Override
	public void setLinks(final List<Link> links) {
		this.links = (links != null ? newArrayList(links) : null);		
	}

	@Override	
	public User getUser() {
		return user;
	}

	@Override
	public void setUser(final User user) {
		this.user = user;
		if (this.user != null) {
			setLvlId(toResourceOwnerId(defaultIfBlank(this.user.getProvider(), LVL_IDENTITY_PROVIDER).trim(), 
					defaultIfBlank(this.user.getUserid(), "userid").trim()));			
		}
	}

	/* Override database operations */

	@Override
	public ListenableFuture<Void> save(final SaveOptions... options) {
		return this.save(null, options);
	}

	@Override
	public ListenableFuture<Void> save(final User user2, final SaveOptions... options) {
		if (user != null) {
			// hash salt
			if (isBlank(user.getSalt())) {
				final String[] shadowed = hashAndSaltPassword(user.getPassword());			
				user.setSalt(shadowed[0]);
				user.setPassword(shadowed[1]);
			}
			// add gravatar
			if (isBlank(user.getPictureUrl()) && isNotBlank(user.getEmail())) {
				final URL url = Gravatar.builder()
						.email(user.getEmail())
						.build()
						.imageUrl();
				if (url != null) {
					user.setPictureUrl(url.toString());
				}
			}
		}
		final ListenableFuture<Void> future = super.save(user2, options);
		addCallback(future, newNotifier(), TASK_RUNNER.executor());	
		return future;
	}

	@Override
	public ListenableFuture<Boolean> delete(DeleteOptions... options) {
		final ListenableFuture<Boolean> future = super.delete(options);
		addCallback(future, newNotifier(), TASK_RUNNER.executor());
		return future;
	}

	private <T> FutureCallback<T> newNotifier() {
		return new FutureCallback<T>() {
			@Override
			public void onSuccess(final T result) {
				ACTIVEMQ_CONN.sendMessage(permissionChangedTopic(user.getProvider()), getLvlId());
			}
			@Override
			public void onFailure(final Throwable t) { }
		};
	}

	/* General methods */

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof ResourceOwner)) {
			return false;
		}
		final ResourceOwner other = ResourceOwner.class.cast(obj);
		return super.equals((LvlObject)other)
				&& ((user == null && other.user == null) || (user.equalsToUnprotected(other.user)));	
	}

	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(user);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add(LvlObject.class.getSimpleName(), super.toString())
				.add("user", user)
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends LvlObject.Builder<ResourceOwner, Builder> {

		public Builder() {
			super(new ResourceOwner());
			setBuilder(this);
		}

		public Builder user(final User user) {
			checkArgument(user != null, "Uninitialized user");
			instance.setUser(user);
			return this;
		}

		public ResourceOwner build() {
			return instance;
		}

	}

}