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
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.EDIT_SHARE;
import static eu.eubrazilcc.lvl.core.Shareable.SharedAccess.VIEW_SHARE;
import static eu.eubrazilcc.lvl.storage.activemq.ActiveMQConnector.ACTIVEMQ_CONN;
import static eu.eubrazilcc.lvl.storage.activemq.TopicHelper.permissionChangedTopic;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner.copyOf;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.convertToValidResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.ADMIN_ROLE;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.allPermissions;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asOAuthString;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.asPermissionList;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.datasetSharePermission;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.latestModification;
import static eu.eubrazilcc.lvl.storage.security.shiro.CryptProvider.hashAndSaltPassword;
import static eu.eubrazilcc.lvl.storage.transform.UserTransientStore.startStore;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.DatasetShare;
import eu.eubrazilcc.lvl.core.Shareable.SharedAccess;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.gravatar.Gravatar;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;
import eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModification;
import eu.eubrazilcc.lvl.storage.security.User;
import eu.eubrazilcc.lvl.storage.transform.UserTransientStore;

/**
 * {@link ResourceOwner} DAO. This class detects any attempt to insert a new resource owner in the database with an unprotected password and 
 * automatically creates a salt and hashes the password before inserting the element in the database. Any element an empty salt will be considered
 * insecure and therefore the password will be protected. <strong>Note</strong> that permission history is excluded by default when resource 
 * owners are listed. Only when the {@link #find(String)} method is used to retrieve the details of a given user, the permission history is
 * included.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum ResourceOwnerDAO implements BaseDAO<String, ResourceOwner> {

	RESOURCE_OWNER_DAO;

	private final static Logger LOGGER = getLogger(ResourceOwnerDAO.class);

	public static final String COLLECTION  = "resource_owners";
	public static final String PRIMARY_KEY = "resourceOwner.ownerId";
	public static final String EMAIL_KEY   = "resourceOwner.user.email";
	/**
	 * <strong>Note</strong> indexing a field that holds an array value has side effects. To change any field of this class you should check
	 * the compatibility with this kind of indexes.
	 * @see <a href="http://docs.mongodb.org/manual/core/index-multikey/">mongoDB Multikey Indexes</a>
	 */
	public static final String PERMISSIONS_KEY = "resourceOwner.user.permissions";

	public static final String ADMIN_USER           = "root";
	public static final String ADMIN_DEFAULT_PASSWD = "changeit";
	public static final String ADMIN_DEFAULT_EMAIL  = "root@example.com";

	private boolean useGravatar;

	private ResourceOwnerDAO() {
		for (final String idx : new String[]{ PRIMARY_KEY, EMAIL_KEY }) {
			MONGODB_CONN.createIndex(idx, COLLECTION);
		}
		MONGODB_CONN.createNonUniqueIndex(PERMISSIONS_KEY, COLLECTION, false);		
		// reset parameters to their default values
		reset();
		// ensure that at least the administrator account exists in the database
		final List<ResourceOwner> owners = list(0, 1, null, null, null, null);
		if (owners == null || owners.isEmpty()) {
			final ResourceOwner admin = ResourceOwner.builder()
					.user(User.builder()
							.userid(ADMIN_USER)
							.password(ADMIN_DEFAULT_PASSWD)
							.email(ADMIN_DEFAULT_EMAIL)
							.fullname("LVL root user")
							.role(ADMIN_ROLE)
							.permissions(asPermissionList(allPermissions()))
							.build()).build();
			final String[] shadowed = hashAndSaltPassword(admin.getUser().getPassword());			
			admin.getUser().setSalt(shadowed[0]);
			admin.getUser().setPassword(shadowed[1]);
			insert(admin);
		}
	}

	public ResourceOwnerDAO useGravatar(final boolean useGravatar) {
		this.useGravatar = useGravatar;
		return this;
	}	

	public ResourceOwnerDAO reset() {
		this.useGravatar = false;
		return this;
	}

	@Override
	public WriteResult<ResourceOwner> insert(final ResourceOwner resourceOwner) {
		if (isNotBlank(resourceOwner.getUser().getSalt())) {
			// remove transient fields from the element before saving it to the database
			final UserTransientStore<ResourceOwner> store = startStore(resourceOwner);
			final DBObject obj = map(store);
			final String id = MONGODB_CONN.insert(obj, COLLECTION);
			// restore transient fields
			store.restore();
			return new WriteResult.Builder<ResourceOwner>().id(id).build();				
		} else {
			// protect sensitive fields before storing the element in the database
			final ResourceOwner copy = copyOf(resourceOwner);
			final String[] shadowed = hashAndSaltPassword(copy.getUser().getPassword());			
			copy.getUser().setSalt(shadowed[0]);
			copy.getUser().setPassword(shadowed[1]);
			// remove transient fields from the element before saving it to the database
			final UserTransientStore<ResourceOwner> store = startStore(copy);
			final DBObject obj = map(store);
			final String id = MONGODB_CONN.insert(obj, COLLECTION);
			return new WriteResult.Builder<ResourceOwner>()
					.id(id)
					.element(copy)
					.build();
		}		
	}

	@Override
	public WriteResult<ResourceOwner> insert(final ResourceOwner resourceOwner, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public ResourceOwner update(final ResourceOwner resourceOwner) {
		// remove transient fields from the element before saving it to the database
		final UserTransientStore<ResourceOwner> store = startStore(resourceOwner);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(resourceOwner.getOwnerId()), COLLECTION);
		// restore transient fields
		store.restore();
		// notify security manager about the changes
		ACTIVEMQ_CONN.sendMessage(permissionChangedTopic(resourceOwner.getUser().getProvider()), 
				resourceOwner.getOwnerId());
		return null;
	}

	@Override
	public void delete(final String resourceOwnerId) {
		MONGODB_CONN.remove(key(resourceOwnerId), COLLECTION);
	}

	@Override
	public List<ResourceOwner> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public ResourceOwner find(final String ownerId) {
		final BasicDBObject obj = MONGODB_CONN.get(key(ownerId), COLLECTION);		
		return parseBasicDBObjectOrNull(obj, false);
	}

	@Override
	public List<ResourceOwner> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, toProjection(projection), count), new Function<BasicDBObject, ResourceOwner>() {
			@Override
			public ResourceOwner apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj, true);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
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
		MONGODB_CONN.stats(os, COLLECTION);
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

	private ResourceOwner parseBasicDBObject(final BasicDBObject obj, final boolean excludeHistory) {
		final ResourceOwner owner = map(obj).getResourceOwner();
		addGravatar(owner);
		excludeHistory(owner, excludeHistory);
		return owner;
	}

	private ResourceOwner parseBasicDBObjectOrNull(final BasicDBObject obj, final boolean excludeHistory) {
		ResourceOwner owner = null;
		if (obj != null) {
			final ResourceOwnerEntity entity = map(obj);
			if (entity != null) {
				owner = entity.getResourceOwner();
				addGravatar(owner);
				excludeHistory(owner, excludeHistory);
			}
		}
		return owner;
	}

	@SuppressWarnings("unused")
	private ResourceOwner parseObject(final Object obj, final boolean excludeHistory) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		final ResourceOwner owner = map((BasicDBObject) obj2.get("obj")).getResourceOwner();
		addGravatar(owner);
		excludeHistory(owner, excludeHistory);
		return owner;
	}

	private void addGravatar(final ResourceOwner owner) {
		if (useGravatar) {
			final URL url = Gravatar.builder()
					.email(owner.getUser().getEmail())
					.build().imageUrl();
			if (url != null) {
				owner.getUser().setPictureUrl(url.toString());
			}
		}
	}

	private void excludeHistory(final ResourceOwner owner, final boolean excludeHistory) {
		if (excludeHistory) {
			owner.getUser().getPermissionHistory().getHistory().clear();
		}
	}

	private DBObject map(final UserTransientStore<ResourceOwner> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new ResourceOwnerEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write resource owner to DB object", e);
		}
		return obj;
	}

	private ResourceOwnerEntity map(final BasicDBObject obj) {
		ResourceOwnerEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), ResourceOwnerEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read resource owner from DB object", e);
		}
		return entity;
	}

	public static void updatePassword(final ResourceOwner resourceOwner, final String newPassword) {
		final String shadowed = hashAndSaltPassword(newPassword, resourceOwner.getUser().getSalt());
		resourceOwner.getUser().setPassword(shadowed);
	}

	/**
	 * Search for an resource owner in the database using the specified email address.
	 * @param email - email address whose associate resource owner is to be returned
	 * @return the resource owner to which the specified email address is associated in 
	 *         the database, or {@code null} if the database contains no entry for the email
	 */
	public ResourceOwner findByEmail(final String email) {
		final BasicDBObject obj = MONGODB_CONN.get(emailKey(email), COLLECTION);		
		return parseBasicDBObjectOrNull(obj, false);
	}

	/**
	 * Checks whether or not the specified user name and password coincides with those stored for resource 
	 * owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner
	 * @param username - user name to be validated, or email address if {@code useEmail}
	 *        is set to true
	 * @param password - password to be validated
	 * @param useEmail - use email address to search the database, instead of owner Id
	 * @param scopeRef - if set and the resource owner is valid, then the scopes associated with the resource 
	 *        owner are concatenated and returned to the caller to be used with OAuth
	 * @param ownerIdRef - if set and the resource owner is valid, then the Id of the resource owner is returned 
	 *        to the caller
	 * @return {@code true} only if the provided user name and password coincides
	 *        with those stored for the resource owner. Otherwise, returns {@code false}.
	 */
	public boolean isValid(final String ownerId, final String username, final String password, final boolean useEmail,
			final @Nullable AtomicReference<String> scopeRef, final @Nullable AtomicReference<String> ownerIdRef) {
		return !useEmail ? isValidUsingOwnerId(ownerId, username, password, scopeRef, ownerIdRef) 
				: isValidUsingEmail(username, password, scopeRef, ownerIdRef);
	}

	private boolean isValidUsingOwnerId(final String ownerId, final String username, final String password,
			final @Nullable AtomicReference<String> scopeRef, final @Nullable AtomicReference<String> ownerIdRef) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(isNotBlank(username), "Uninitialized or invalid username");
		checkArgument(isNotBlank(password), "Uninitialized or invalid password");
		final ResourceOwner resourceOwner = find(ownerId);
		final boolean isValid = (resourceOwner != null && resourceOwner.getUser() != null 
				&& username.equals(resourceOwner.getUser().getUserid())				
				&& hashAndSaltPassword(password, resourceOwner.getUser().getSalt()).equals(resourceOwner.getUser().getPassword()));		
		if (isValid) {
			if (scopeRef != null) {
				scopeRef.set(oauthScope(resourceOwner, false));
			}
			if (ownerIdRef != null) {
				ownerIdRef.set(resourceOwner.getOwnerId());
			}
		}
		return isValid;
	}

	private boolean isValidUsingEmail(final String email, final String password, final @Nullable AtomicReference<String> scopeRef, 
			final @Nullable AtomicReference<String> ownerIdRef) {
		checkArgument(isNotBlank(email), "Uninitialized or invalid email address");
		checkArgument(isNotBlank(password), "Uninitialized or invalid password");
		final ResourceOwner resourceOwner = findByEmail(email);
		final boolean isValid = (resourceOwner != null && resourceOwner.getUser() != null 
				&& email.equals(resourceOwner.getUser().getEmail())
				&& hashAndSaltPassword(password, resourceOwner.getUser().getSalt()).equals(resourceOwner.getUser().getPassword()));
		if (isValid) {
			if (scopeRef != null) {
				scopeRef.set(oauthScope(resourceOwner, false));
			}
			if (ownerIdRef != null) {
				ownerIdRef.set(resourceOwner.getOwnerId());
			}
		}
		return isValid;
	}

	/**
	 * Checks whether or not the specified user exists (the specified username coincides with the 
	 * username stored for the resource owner identified by the provided id).
	 * @param ownerId - identifier of the resource owner
	 * @param username - user name to be validated, or email address if {@code useEmail} is set to true
	 * @param scopeRef - if set and the resource owner is valid, then the scopes associated with the resource 
	 *        owner are concatenated and returned to the caller to be used with OAuth 
	 * @return {@code true} only if the provided identifier and user name coincide with those stored for the 
	 *         resource owner. Otherwise, returns {@code false}.
	 */
	public boolean exist(final String ownerId, final String username, final @Nullable AtomicReference<String> scopeRef) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(isNotBlank(username), "Uninitialized or invalid username");
		final ResourceOwner resourceOwner = find(ownerId);
		final boolean exist = (resourceOwner != null && resourceOwner.getUser() != null 
				&& username.equals(resourceOwner.getUser().getUserid()));
		if (exist && scopeRef != null) {
			scopeRef.set(oauthScope(resourceOwner, false));
		}
		return exist;		
	}

	/**
	 * Simply check whether a resource owner exists in the database using its identifier.
	 * @param ownerId - identifier of the resource owner
	 * @return 
	 */
	public boolean exist(final String ownerId) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");	
		return find(ownerId) != null;		
	}

	/**
	 * Adds one or more roles to the resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner whose roles will be modified
	 * @param roles - roles to be added to the resource owner
	 */
	public void addRoles(final String ownerId, final String... roles) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(roles != null && roles.length > 0, "Uninitialized or invalid roles");
		final ResourceOwner resourceOwner = find(ownerId);
		checkState(resourceOwner != null, "Resource owner not found");
		for (final String role : roles) {
			if (isNotBlank(role)) {
				resourceOwner.getUser().getRoles().add(role);
			}
		}
		update(resourceOwner);
	}

	/**
	 * Removes one or more roles from the resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner whose roles will be modified
	 * @param roles - roles to be removed from the resource owner
	 */
	public void removeRoles(final String ownerId, final String... roles) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(roles != null && roles.length > 0, "Uninitialized or invalid roles");
		final ResourceOwner resourceOwner = find(ownerId);
		checkState(resourceOwner != null, "Resource owner not found");
		for (final String role : roles) {
			if (isNotBlank(role)) {
				resourceOwner.getUser().getRoles().remove(role);
			}
		}
		update(resourceOwner);
	}

	/**
	 * Adds permissions to data shares calling the method {@link #addPermissions(String, String...)}.
	 * @param ownerId - identifier of the resource owner whose permissions will be modified
	 * @param shares - data shares to which the resource owner will be granted with the defined access
	 */
	public void addPermissions(final String ownerId, final DatasetShare... shares) {
		checkArgument(shares != null && shares.length > 0, "Uninitialized or invalid data shares");
		addPermissions(ownerId ,from(Arrays.asList(shares)).transform(new Function<DatasetShare, String>() {
			@Override
			public String apply(final DatasetShare share) {
				checkState(share.getSubject().equals(ownerId), "Subject does not coincide with owner id");
				return datasetSharePermission(share);
			}			
		}).filter(notNull()).toArray(String.class));		
	}

	/**
	 * Removes permissions from data shares calling the method {@link #removePermissions(String, String...)}.
	 * @param ownerId - identifier of the resource owner whose permissions will be modified
	 * @param shares - data shares from which the access of the resource owner will be removed
	 */
	public void removePermissions(final String ownerId, final DatasetShare... shares) {
		checkArgument(shares != null && shares.length > 0, "Uninitialized or invalid data shares");
		removePermissions(ownerId ,from(Arrays.asList(shares)).transform(new Function<DatasetShare, String>() {
			@Override
			public String apply(final DatasetShare share) {
				checkState(share.getSubject().equals(ownerId), "Subject does not coincide with owner id");
				return datasetSharePermission(share);
			}			
		}).filter(notNull()).toArray(String.class));
	}

	/**
	 * Adds one or more permissions to the resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner whose permissions will be modified
	 * @param permissions - permissions to be added to the resource owner
	 */
	public void addPermissions(final String ownerId, final String... permissions) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(permissions != null && permissions.length > 0, "Uninitialized or invalid permissions");
		final ResourceOwner resourceOwner = find(ownerId);
		checkState(resourceOwner != null, "Resource owner not found");
		for (final String permission : permissions) {
			if (isNotBlank(permission)) {
				resourceOwner.getUser().addPemissions(permission);
			}
		}
		update(resourceOwner);
	}

	/**
	 * Removes one or more permissions from the resource owner identified by the provided id.
	 * @param ownerId - identifier of the resource owner whose permissions will be modified
	 * @param permissions - permissions to be removed from the resource owner
	 */
	public void removePermissions(final String ownerId, final String... permissions) {
		checkArgument(isNotBlank(ownerId), "Uninitialized or invalid resource owner id");
		checkArgument(permissions != null && permissions.length > 0, "Uninitialized or invalid permissions");
		final ResourceOwner resourceOwner = find(ownerId);
		checkState(resourceOwner != null, "Resource owner not found");
		for (final String permission : permissions) {
			if (isNotBlank(permission)) {
				resourceOwner.getUser().removePemissions(permissions);
			}
		}
		update(resourceOwner);
	}

	public List<DatasetShare> listDatashares(final String namespace, final String filename, final int start, final int size, 
			final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		final String namespace2 = trimToNull(namespace), filename2 = trimToNull(filename);
		checkArgument(isNotBlank(namespace2), "Uninitialized or invalid namespace");
		checkArgument(isNotBlank(filename2), "Uninitialized or invalid filename");
		// execute the query in the database (unsupported filter)
		final DatasetShare share = DatasetShare.builder()
				.namespace(namespace2)
				.filename(filename2)
				.accessType(EDIT_SHARE)
				.build();
		final String rw = datasetSharePermission(share);
		share.setAccessType(VIEW_SHARE);
		final String ro = datasetSharePermission(share);
		final BasicDBObject query = new BasicDBObject(PERMISSIONS_KEY, new BasicDBObject("$in", new String[]{ rw, ro }));
		LOGGER.trace("listDatashares query: " + JSON.serialize(query));
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, query, null, count), new Function<BasicDBObject, DatasetShare>() {
			@Override
			public DatasetShare apply(final BasicDBObject obj) {
				return parseResourceOwner(parseBasicDBObject(obj, true), namespace2, filename2, new String[]{ rw, ro });				
			}
		});		
	}

	public DatasetShare findDatashare(final String namespace, final String filename, final String subject) {
		final String namespace2 = trimToNull(namespace), filename2 = trimToNull(filename), subject2 = convertToValidResourceOwnerId(subject, true);
		checkArgument(isNotBlank(namespace2), "Uninitialized or invalid namespace");
		checkArgument(isNotBlank(filename2), "Uninitialized or invalid filename");
		checkArgument(isNotBlank(subject2), "Uninitialized or invalid subject");
		// execute the query in the database
		final DatasetShare share = DatasetShare.builder()
				.namespace(namespace2)
				.filename(filename2)
				.accessType(EDIT_SHARE)
				.build();
		final String rw = datasetSharePermission(share);
		share.setAccessType(VIEW_SHARE);
		final String ro = datasetSharePermission(share);
		final BasicDBObject query = new BasicDBObject(PERMISSIONS_KEY, new BasicDBObject("$in", new String[]{ rw, ro })).append(PRIMARY_KEY, subject2);
		LOGGER.trace("findDatashare query: " + JSON.serialize(query));
		final ResourceOwner owner = parseBasicDBObjectOrNull(MONGODB_CONN.get(query, COLLECTION), true);
		return owner != null ? parseResourceOwner(owner, namespace2, filename2, new String[]{ rw, ro }) : null;
	}

	private DatasetShare parseResourceOwner(final ResourceOwner owner, final String namespace, final String filename, 
			final String[] permissions) {
		PermissionModification history = null;
		SharedAccess sharedAccess = null;		
		if (owner.getUser().getPermissions().contains(permissions[0])) {
			sharedAccess = EDIT_SHARE;
			history = latestModification(owner.getUser().getPermissionHistory().getHistory(), permissions[0]);					
		} else {
			sharedAccess = VIEW_SHARE;
			history = latestModification(owner.getUser().getPermissionHistory().getHistory(), permissions[1]);
		}
		return DatasetShare.builder()
				.accessType(sharedAccess)
				.filename(filename)
				.namespace(namespace)
				.sharedDate(history != null && history.getModificationDate() != null ? history.getModificationDate() : new Date())
				.subject(owner.getOwnerId())
				.build();
	}

	/**
	 * Concatenates in a single string all the roles of the specified resource owner 
	 * and returns the generated string to the caller. This string can be used to transmit 
	 * the scope in OAuth.
	 * @param resourceOwner - the resource owner whose scopes will be returned
	 * @param sort - set to {@code true} to sort the resulting scope in lexicographical order
	 * @return A string where all the scopes of the specified resource owner are concatenated.
	 */
	public static final String oauthScope(final ResourceOwner resourceOwner, final boolean sort) {
		checkArgument(resourceOwner != null, "Uninitialized or invalid resource owner");		
		return (resourceOwner.getUser() != null && resourceOwner.getUser().getRoles() != null 
				? asOAuthString(resourceOwner.getUser().getRoles(), sort) : null);
	}	

	/**
	 * Resource owner entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class ResourceOwnerEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

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