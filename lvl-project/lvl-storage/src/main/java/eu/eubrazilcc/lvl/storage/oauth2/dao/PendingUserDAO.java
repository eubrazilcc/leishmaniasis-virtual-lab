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
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.core.http.LinkRelation;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.PendingUser;

/**
 * Pending user DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum PendingUserDAO implements BaseDAO<String, PendingUser> {

	PENDING_USER_DAO;

	private final static Logger LOGGER = getLogger(PendingUserDAO.class);

	public static final String COLLECTION  = "pending_users";
	public static final String PRIMARY_KEY = "pendingUser.pendingUserId";
	public static final String EMAIL_KEY   = "pendingUser.user.email";

	private URI baseUri = null;

	private PendingUserDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createIndex(EMAIL_KEY, COLLECTION);
	}

	public PendingUserDAO baseUri(final URI baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	@Override
	public String insert(final PendingUser pendingUser) {
		// remove transient fields from the element before saving it to the database
		final PendingUserTransientStore store = PendingUserTransientStore.start(pendingUser);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return id;
	}

	@Override
	public void update(final PendingUser pendingUser) {
		// remove transient fields from the element before saving it to the database
		final PendingUserTransientStore store = PendingUserTransientStore.start(pendingUser);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(pendingUser.getPendingUserId()), COLLECTION);
		// restore transient fields
		store.restore();
	}

	@Override
	public void delete(final String pendingUserId) {
		MONGODB_CONN.remove(key(pendingUserId), COLLECTION);
	}

	@Override
	public List<PendingUser> findAll() {
		return list(0, Integer.MAX_VALUE, null);
	}

	@Override
	public PendingUser find(final String pendingUserId) {
		final BasicDBObject obj = MONGODB_CONN.get(key(pendingUserId), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<PendingUser> list(final int start, final int size, final @Nullable MutableLong count) {
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, count), new Function<BasicDBObject, PendingUser>() {
			@Override
			public PendingUser apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<PendingUser> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<PendingUser> geoWithin(final Polygon polygon) {
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

	private PendingUser parseBasicDBObject(final BasicDBObject obj) {
		final PendingUser pendingUser = map(obj).getPendingUser();
		addLink(pendingUser);
		return pendingUser;
	}

	private PendingUser parseBasicDBObjectOrNull(final BasicDBObject obj) {
		PendingUser pendingUser = null;
		if (obj != null) {
			final PendingUserEntity entity = map(obj);
			if (entity != null) {
				pendingUser = entity.getPendingUser();
				addLink(pendingUser);
			}
		}
		return pendingUser;
	}

	@SuppressWarnings("unused")
	private PendingUser parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		final PendingUser pendingUser = map((BasicDBObject) obj2.get("obj")).getPendingUser();
		addLink(pendingUser);
		return pendingUser;
	}

	private void addLink(final PendingUser pendingUser) {
		if (baseUri != null) {
			pendingUser.getUser().setLink(Link.fromUri(UriBuilder.fromUri(baseUri).path(pendingUser.getPendingUserId()).build())
					.rel(LinkRelation.SELF).type(MediaType.APPLICATION_JSON).build());
		}
	}

	private DBObject map(final PendingUserTransientStore store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new PendingUserEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write pending user to DB object", e);
		}
		return obj;
	}

	private PendingUserEntity map(final BasicDBObject obj) {
		PendingUserEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), PendingUserEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read pending user from DB object", e);
		}
		return entity;
	}

	/**
	 * Search for a pending user in the database using the specified email address.
	 * @param email - email address whose associate pending user is to be returned
	 * @return the pending user to which the specified email address is associated in 
	 *         the database, or {@code null} if the database contains no entry for the email
	 */
	public PendingUser findByEmail(final String email) {
		final BasicDBObject obj = MONGODB_CONN.get(emailKey(email), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	/**
	 * Checks whether or not the specified user name and confirmation code coincides with 
	 * those stored for pending user identified by the provided id.
	 * @param pendingUserId - identifier of the pending user
	 * @param username - user name to be validated, or email address if {@code useEmail}
	 *        is set to true
	 * @param confirmationCode - confirmation code to be validated
	 * @param useEmail - use email address to search the database, instead of pending user Id
	 * @return {@code true} only if the provided user name and confirmation code coincides
	 *        with those stored for the pending user. Otherwise, returns {@code false}.
	 */
	public boolean isValid(final String pendingUserId, final String username, final String confirmationCode, 
			final boolean useEmail) {
		return !useEmail ? isValidUsingPendingUserId(pendingUserId, username, confirmationCode) : isValidUsingEmail(username, confirmationCode);
	}	

	private boolean isValidUsingPendingUserId(final String pendingUserId, final String username, 
			final String activationCode) {
		checkArgument(isNotBlank(pendingUserId), "Uninitialized or invalid pending user id");
		checkArgument(isNotBlank(username), "Uninitialized or invalid username");
		checkArgument(isNotBlank(activationCode), "Uninitialized or invalid activation code");
		final PendingUser pendingUser = find(pendingUserId);
		final boolean isValid = (pendingUser != null && pendingUser.getUser() != null 
				&& username.equals(pendingUser.getUser().getUsername())
				&& activationCode.equals(pendingUser.getActivationCode()));
		return isValid;
	}

	private boolean isValidUsingEmail(final String email, final String activationCode) {
		checkArgument(isNotBlank(email), "Uninitialized or invalid email address");
		checkArgument(isNotBlank(activationCode), "Uninitialized or invalid activation code");
		final PendingUser pendingUser = findByEmail(email);
		final boolean isValid = (pendingUser != null && pendingUser.getUser() != null 
				&& email.equals(pendingUser.getUser().getEmail())
				&& activationCode.equals(pendingUser.getActivationCode()));
		return isValid;
	}	

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PendingUserTransientStore extends TransientStore<PendingUser> {

		private Link link;

		public PendingUserTransientStore(final PendingUser pendingUser) {
			super(pendingUser);
		}

		public Link getLink() {
			return link;
		}

		public PendingUser purge() {
			link = element.getUser().getLink();
			element.getUser().setLink(null);
			return element;
		}

		public PendingUser restore() {
			element.getUser().setLink(link);
			return element;
		}

		public static PendingUserTransientStore start(final PendingUser pendingUser) {
			return new PendingUserTransientStore(pendingUser);
		}

	}

	/**
	 * Pending user entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PendingUserEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private PendingUser pendingUser;

		public PendingUserEntity() { }

		public PendingUserEntity(final PendingUser pendingUser) {
			setPendingUser(pendingUser);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public PendingUser getPendingUser() {
			return pendingUser;
		}

		public void setPendingUser(final PendingUser pendingUser) {
			this.pendingUser = pendingUser;
		}

	}	

}