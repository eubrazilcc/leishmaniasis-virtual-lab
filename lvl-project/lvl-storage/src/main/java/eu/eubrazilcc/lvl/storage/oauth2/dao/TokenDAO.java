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
import java.util.List;

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

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.AccessToken;
import eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager;

/**
 * Access token DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum TokenDAO implements BaseDAO<String, AccessToken> {

	TOKEN_DAO;

	private final static Logger LOGGER = getLogger(TokenDAO.class);

	public static final String COLLECTION = "access_tokens";
	public static final String PRIMARY_KEY = "accessToken.token";

	private TokenDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
	}

	@Override
	public WriteResult<AccessToken> insert(final AccessToken accessToken) {
		// remove transient fields from the element before saving it to the database
		final AccessTokenTransientStore store = AccessTokenTransientStore.start(accessToken);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<AccessToken>().id(id).build();
	}

	@Override
	public WriteResult<AccessToken> insert(final AccessToken accessToken, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public AccessToken update(final AccessToken accessToken) {
		// remove transient fields from the element before saving it to the database
		final AccessTokenTransientStore store = AccessTokenTransientStore.start(accessToken);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(accessToken.getToken()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String token) {
		MONGODB_CONN.remove(key(token), COLLECTION);
	}

	@Override
	public List<AccessToken> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public AccessToken find(final String token) {
		final BasicDBObject obj = MONGODB_CONN.get(key(token), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<AccessToken> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter,
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, count), new Function<BasicDBObject, AccessToken>() {
			@Override
			public AccessToken apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<AccessToken> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<AccessToken> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String key) {
		return new BasicDBObject(PRIMARY_KEY, key);		
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(PRIMARY_KEY, 1);
	}	

	private AccessToken parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getAccessToken();
	}

	private AccessToken parseBasicDBObjectOrNull(final BasicDBObject obj) {
		AccessToken token = null;		
		if (obj != null) {
			final AccessTokenEntity entity = map(obj);
			if (entity != null) {
				token = entity.getAccessToken();
			}
		}
		return token;
	}

	private DBObject map(final AccessTokenTransientStore store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new AccessTokenEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write access token to DB object", e);
		}
		return obj;
	}

	private AccessTokenEntity map(final BasicDBObject obj) {
		AccessTokenEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), AccessTokenEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read access token from DB object", e);
		}
		return entity;
	}

	/**
	 * Checks whether or not the specified secret (token) was previously stored
	 * and is currently valid (not expired).
	 * @param token - the secret associated to the token
	 * @return {@code true} only if the provided secret (token) is found in the
	 *         storage and is currently valid (not expired). Otherwise, returns 
	 *         {@code false}.
	 */
	public boolean isValid(final String token) {
		checkArgument(isNotBlank(token), "Uninitialized or invalid token");
		final AccessToken accessToken = find(token);		
		return (accessToken != null && accessToken.getToken() != null && token.equals(accessToken.getToken())
				&& (accessToken.getIssuedAt() + accessToken.getExpiresIn()) > (System.currentTimeMillis() / 1000l));
	}

	/**
	 * Checks whether or not the specified secret (token) was previously stored, 
	 * is currently valid (not expired) and grant access to the specified scope.
	 * When needed, the method can also check if the provided token grants full
	 * access to the target scope.
	 * @param token - the secret associated to the token
	 * @param targetScope - the scope where the caller operations will be performed
	 * @param requestFullAccess - {@code true} if the caller is requesting other
	 *        kind of access than read only access
	 * @return {@code true} only if the provided secret (token) is found in the
	 *         storage, is currently valid (not expired) and grant access to the 
	 *         specified scope with the specified permissions. Otherwise, returns 
	 *         {@code false}.
	 */
	public boolean isValid(final String token, final String targetScope, final boolean requestFullAccess) {
		checkArgument(isNotBlank(token), "Uninitialized or invalid token");
		checkArgument(isNotBlank(targetScope), "Uninitialized or invalid target scope");
		final AccessToken accessToken = find(token);
		return (accessToken != null && accessToken.getToken() != null && token.equals(accessToken.getToken())
				&& (accessToken.getIssuedAt() + accessToken.getExpiresIn()) > (System.currentTimeMillis() / 1000l)
				&& ScopeManager.isAccessible(targetScope, accessToken.getScopes(), requestFullAccess));
	}	

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class AccessTokenTransientStore extends TransientStore<AccessToken> {

		public AccessTokenTransientStore(final AccessToken accessToken) {
			super(accessToken);
		}

		public AccessToken purge() {
			return element;
		}

		public AccessToken restore() {
			return element;
		}

		public static AccessTokenTransientStore start(final AccessToken accessToken) {
			return new AccessTokenTransientStore(accessToken);
		}

	}

	/**
	 * Token entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class AccessTokenEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private AccessToken accessToken;

		public AccessTokenEntity() { }

		public AccessTokenEntity(final AccessToken accessToken) {
			setAccessToken(accessToken);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public AccessToken getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(final AccessToken accessToken) {
			this.accessToken = accessToken;
		}

	}

}