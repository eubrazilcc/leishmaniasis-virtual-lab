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
import eu.eubrazilcc.lvl.storage.oauth2.AuthCode;

/**
 * Authentication DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum AuthCodeDAO implements BaseDAO<String, AuthCode> {

	AUTH_CODE_DAO;

	private final static Logger LOGGER = getLogger(AuthCodeDAO.class);

	public static final String COLLECTION = "authz_codes";
	public static final String DB_PREFIX = "authCode.";
	public static final String PRIMARY_KEY = DB_PREFIX + "code";

	private AuthCodeDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
	}

	@Override
	public WriteResult<AuthCode> insert(final AuthCode authCode) {
		// remove transient fields from the element before saving it to the database
		final AuthCodeTransientStore store = AuthCodeTransientStore.start(authCode);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<AuthCode>().id(id).build();
	}

	@Override
	public WriteResult<AuthCode> insert(final AuthCode authCode, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public AuthCode update(final AuthCode authCode) {
		// remove transient fields from the element before saving it to the database
		final AuthCodeTransientStore store = AuthCodeTransientStore.start(authCode);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(authCode.getCode()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String code) {
		MONGODB_CONN.remove(key(code), COLLECTION);
	}

	@Override
	public List<AuthCode> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public AuthCode find(final String code) {
		final BasicDBObject obj = MONGODB_CONN.get(key(code), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<AuthCode> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {		
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, count), new Function<BasicDBObject, AuthCode>() {
			@Override
			public AuthCode apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<AuthCode> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<AuthCode> geoWithin(final Polygon polygon) {
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

	private AuthCode parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getAuthCode();
	}

	private AuthCode parseBasicDBObjectOrNull(final BasicDBObject obj) {
		AuthCode authCode = null;
		if (obj != null) {
			final AuthCodeEntity entity = map(obj);
			if (entity != null) {
				authCode = entity.getAuthCode();
			}
		}
		return authCode;
	}

	private DBObject map(final AuthCodeTransientStore store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new AuthCodeEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write authN code to DB object", e);
		}
		return obj;
	}

	private AuthCodeEntity map(final BasicDBObject obj) {
		AuthCodeEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), AuthCodeEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read authN code from DB object", e);
		}
		return entity;
	}

	/**
	 * Checks whether or not the specified secret (access code) was previously stored
	 * and is currently valid (not expired).
	 * @param code - the secret associated to the access code
	 * @return {@code true} only if the provided secret (access code) is found in the
	 *         storage and is currently valid (not expired). Otherwise, returns 
	 *         {@code false}.
	 */
	public boolean isValid(final String code) {		
		checkArgument(isNotBlank(code), "Uninitialized or invalid code");
		final AuthCode authCode = find(code);		
		return (authCode != null && authCode.getCode() != null && code.equals(authCode.getCode())
				&& (authCode.getIssuedAt() + authCode.getExpiresIn()) > (System.currentTimeMillis() / 1000l));
	}	

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class AuthCodeTransientStore extends TransientStore<AuthCode> {

		public AuthCodeTransientStore(final AuthCode authCode) {
			super(authCode);
		}

		public AuthCode purge() {
			return element;
		}

		public AuthCode restore() {
			return element;
		}

		public static AuthCodeTransientStore start(final AuthCode authCode) {
			return new AuthCodeTransientStore(authCode);
		}

	}

	/**
	 * AuthCode entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class AuthCodeEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private AuthCode authCode;

		public AuthCodeEntity() { }

		public AuthCodeEntity(final AuthCode authCode) {
			setAuthCode(authCode);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public AuthCode getAuthCode() {
			return authCode;
		}

		public void setAuthCode(final AuthCode authCode) {
			this.authCode = authCode;
		}

	}

}