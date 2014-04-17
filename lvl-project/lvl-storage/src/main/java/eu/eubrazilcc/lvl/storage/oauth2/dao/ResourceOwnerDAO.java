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

package eu.eubrazilcc.lvl.storage.oauth2.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.all;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asList;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.asOAuthString;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import com.google.common.base.Function;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;
import eu.eubrazilcc.lvl.core.http.LinkRelation;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.oauth2.User;

/**
 * Resource owner DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum ResourceOwnerDAO implements BaseDAO<String, ResourceOwner> {

	INSTANCE;

	public static final String COLLECTION  = "resource_owners";
	public static final String PRIMARY_KEY = "resourceOwner.ownerId";
	public static final String EMAIL_KEY   = "resourceOwner.user.email";

	public static final String ADMIN_USER           = "root";
	public static final String ADMIN_DEFAULT_PASSWD = "changeit";
	public static final String ADMIN_DEFAULT_EMAIL  = "root@example.com";

	private final Morphia morphia = new Morphia();

	private URI baseUri = null;

	private ResourceOwnerDAO() {
		MongoDBConnector.INSTANCE.createIndex(PRIMARY_KEY, COLLECTION);
		MongoDBConnector.INSTANCE.createIndex(EMAIL_KEY, COLLECTION);
		morphia.map(ResourceOwnerEntity.class);
		// ensure that at least the administrator account exists in the database
		final List<ResourceOwner> owners = list(0, 1, null);
		if (owners == null || owners.isEmpty()) {
			insert(ResourceOwner.builder()
					.id(ADMIN_USER)
					.user(User.builder()
							.username(ADMIN_USER)
							.password(ADMIN_DEFAULT_PASSWD)
							.email(ADMIN_DEFAULT_EMAIL)
							.fullname("LVL root user")
							.scope(asList(all()))
							.build()).build());
		}
	}

	public ResourceOwnerDAO baseUri(final URI baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	@Override
	public String insert(final ResourceOwner resourceOwner) {
		// remove transient fields from the element before saving it to the database
		final ResourceOwnerTransientStore store = ResourceOwnerTransientStore.start(resourceOwner);
		final DBObject obj = morphia.toDBObject(new ResourceOwnerEntity(store.purge()));
		final String id = MongoDBConnector.INSTANCE.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return id;
	}

	@Override
	public void update(final ResourceOwner resourceOwner) {
		// remove transient fields from the element before saving it to the database
		final ResourceOwnerTransientStore store = ResourceOwnerTransientStore.start(resourceOwner);
		final DBObject obj = morphia.toDBObject(new ResourceOwnerEntity(store.purge()));
		MongoDBConnector.INSTANCE.update(obj, key(resourceOwner.getOwnerId()), COLLECTION);
		// restore transient fields
		store.restore();
	}

	@Override
	public void delete(final String resourceOwnerId) {
		MongoDBConnector.INSTANCE.remove(key(resourceOwnerId), COLLECTION);
	}

	@Override
	public List<ResourceOwner> findAll() {
		return list(0, Integer.MAX_VALUE, null);
	}

	@Override
	public ResourceOwner find(final String ownerId) {
		final BasicDBObject obj = MongoDBConnector.INSTANCE.get(key(ownerId), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<ResourceOwner> list(final int start, final int size, final @Nullable MutableLong count) {
		return transform(MongoDBConnector.INSTANCE.list(sortCriteria(), COLLECTION, start, size, count), new Function<BasicDBObject, ResourceOwner>() {
			@Override
			public ResourceOwner apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MongoDBConnector.INSTANCE.count(COLLECTION);
	}

	@Override
	public List<ResourceOwner> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<ResourceOwner> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MongoDBConnector.INSTANCE.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String key) {
		return new BasicDBObject(PRIMARY_KEY, key);		
	}

	private BasicDBObject emailKey(final String key) {
		return new BasicDBObject(EMAIL_KEY, key);		
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(PRIMARY_KEY, 1);
	}

	private ResourceOwner parseBasicDBObject(final BasicDBObject obj) {
		final ResourceOwner owner = morphia.fromDBObject(ResourceOwnerEntity.class, obj).getResourceOwner();
		addLink(owner);
		return owner;
	}

	private ResourceOwner parseBasicDBObjectOrNull(final BasicDBObject obj) {
		ResourceOwner owner = null;
		if (obj != null) {
			final ResourceOwnerEntity entity = morphia.fromDBObject(ResourceOwnerEntity.class, obj);
			if (entity != null) {
				owner = morphia.fromDBObject(ResourceOwnerEntity.class, obj).getResourceOwner();
				addLink(owner);
			}
		}
		return owner;
	}

	@SuppressWarnings("unused")
	private ResourceOwner parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		final ResourceOwner owner = morphia.fromDBObject(ResourceOwnerEntity.class, (BasicDBObject) obj2.get("obj")).getResourceOwner();
		addLink(owner);
		return owner;
	}

	private void addLink(final ResourceOwner owner) {
		if (baseUri != null) {
			owner.getUser().setLink(Link.fromUri(UriBuilder.fromUri(baseUri).path(owner.getOwnerId()).build())
					.rel(LinkRelation.SELF).type(MediaType.APPLICATION_JSON).build());
		}
	}

	/**
	 * Search for an resource owner in the database using the specified email address.
	 * @param email - email address whose associate resource owner is to be returned
	 * @return the resource owner to which the specified email address is associated in 
	 *         the database, or {@code null} if the database contains no entry for the email
	 */
	public ResourceOwner findByEmail(final String email) {
		final BasicDBObject obj = MongoDBConnector.INSTANCE.get(emailKey(email), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	/**
	 * Checks whether or not the specified user name and password coincides with 
	 * those stored for resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner
	 * @param username - user name to be validated, or email address if {@code useEmail}
	 *        is set to true
	 * @param password - password to be validated
	 * @param useEmail - use email address to search the database, instead of owner Id
	 * @param scopeRef - if set and the resource owner is valid, then the scopes associated 
	 *        with the resource owner are concatenated and returned to the caller to be used
	 *        with OAuth 
	 * @return {@code true} only if the provided user name and password coincides
	 *        with those stored for the resource owner. Otherwise, returns {@code false}.
	 */
	public boolean isValid(final String ownerId, final String username, final String password, final boolean useEmail,
			final @Nullable AtomicReference<String> scopeRef) {
		return !useEmail ? isValidUsingOwnerId(ownerId, username, password, scopeRef) : isValidUsingEmail(username, password,scopeRef);
	}	

	private boolean isValidUsingOwnerId(final String ownerId, final String username, final String password,
			final @Nullable AtomicReference<String> scopeRef) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(isNotBlank(username), "Uninitialized or invalid username");
		checkArgument(isNotBlank(password), "Uninitialized or invalid password");
		final ResourceOwner resourceOwner = find(ownerId);
		final boolean isValid = (resourceOwner != null && resourceOwner.getUser() != null 
				&& username.equals(resourceOwner.getUser().getUsername())
				&& password.equals(resourceOwner.getUser().getPassword()));
		if (isValid && scopeRef != null) {
			scopeRef.set(oauthScope(resourceOwner, false));
		}
		return isValid;
	}

	private boolean isValidUsingEmail(final String email, final String password, final @Nullable AtomicReference<String> scopeRef) {
		checkArgument(isNotBlank(email), "Uninitialized or invalid email address");
		checkArgument(isNotBlank(password), "Uninitialized or invalid password");
		final ResourceOwner resourceOwner = findByEmail(email);
		final boolean isValid = (resourceOwner != null && resourceOwner.getUser() != null 
				&& email.equals(resourceOwner.getUser().getEmail())
				&& password.equals(resourceOwner.getUser().getPassword()));
		if (isValid && scopeRef != null) {
			scopeRef.set(oauthScope(resourceOwner, false));
		}
		return isValid;
	}

	/**
	 * Adds one or more scopes to the resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner whose scopes will be modified
	 * @param scopes - scopes to be added to the resource owner
	 */
	public void addScopes(final String ownerId, final String... scopes) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(scopes != null && scopes.length > 0, "Uninitialized or invalid scopes");
		final ResourceOwner resourceOwner = find(ownerId);
		checkState(resourceOwner != null, "Resource owner not found");
		for (final String scope : scopes) {
			if (isNotBlank(scope)) {
				resourceOwner.getUser().getScopes().add(scope);
			}
		}
		update(resourceOwner);
	}

	/**
	 * Removes one or more scopes from the resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner whose scopes will be modified
	 * @param scopes - scopes to be removed from the resource owner
	 */
	public void removeScopes(final String ownerId, final String... scopes) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(scopes != null && scopes.length > 0, "Uninitialized or invalid scopes");
		final ResourceOwner resourceOwner = find(ownerId);
		checkState(resourceOwner != null, "Resource owner not found");
		for (final String scope : scopes) {
			if (isNotBlank(scope)) {
				resourceOwner.getUser().getScopes().remove(scope);
			}
		}
		update(resourceOwner);
	}

	/**
	 * Concatenates in a single string all the scopes of the specified resource owner 
	 * and returns the generated string to the caller. This string can be used to transmit 
	 * the scope in OAuth.
	 * @param resourceOwner - the resource owner whose scopes will be returned
	 * @param sort - set to {@code true} to sort the resulting scope in lexicographical order
	 * @return A string where all the scopes of the specified resource owner are concatenated.
	 */
	public static final String oauthScope(final ResourceOwner resourceOwner, final boolean sort) {
		checkArgument(resourceOwner != null, "Uninitialized or invalid resource owner");		
		return (resourceOwner.getUser() != null && resourceOwner.getUser().getScopes() != null 
				? asOAuthString(resourceOwner.getUser().getScopes(), sort) : null);
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class ResourceOwnerTransientStore extends TransientStore<ResourceOwner> {

		private Link link;

		public ResourceOwnerTransientStore(final ResourceOwner resourceOwner) {
			super(resourceOwner);
		}

		public Link getLink() {
			return link;
		}

		public ResourceOwner purge() {
			link = element.getUser().getLink();
			element.getUser().setLink(null);
			return element;
		}

		public ResourceOwner restore() {
			element.getUser().setLink(link);
			return element;
		}

		public static ResourceOwnerTransientStore start(final ResourceOwner resourceOwner) {
			return new ResourceOwnerTransientStore(resourceOwner);
		}

	}

	/**
	 * Resource owner Morphia entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	@Entity(value=COLLECTION, noClassnameStored=true)
	@Indexes({@Index(PRIMARY_KEY)})
	public static class ResourceOwnerEntity {

		@Id
		private ObjectId id;

		@Embedded
		private ResourceOwner resourceOwner;

		public ResourceOwnerEntity() { }

		public ResourceOwnerEntity(final ResourceOwner resourceOwner) {
			setResourceOwner(resourceOwner);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public ResourceOwner getResourceOwner() {
			return resourceOwner;
		}

		public void setResourceOwner(final ResourceOwner resourceOwner) {
			this.resourceOwner = resourceOwner;
		}

	}	

}