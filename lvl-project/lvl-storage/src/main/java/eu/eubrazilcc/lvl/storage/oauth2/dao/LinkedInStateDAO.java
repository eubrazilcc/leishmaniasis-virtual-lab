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
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.transform.SameTransientStore.startStore;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
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

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.linkedin.LinkedInState;
import eu.eubrazilcc.lvl.storage.transform.SameTransientStore;

/**
 * {@link LinkedInState} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum LinkedInStateDAO implements BaseDAO<String, LinkedInState> {

	LINKEDIN_STATE_DAO;

	private final static Logger LOGGER = getLogger(LinkedInStateDAO.class);

	public static final String COLLECTION    = "linkedin_state";
	public static final String DB_PREFIX     = "linkedInState.";
	public static final String PRIMARY_KEY   = DB_PREFIX + "state";
	public static final String ISSUED_AT_KEY = DB_PREFIX + "issuedAt";

	private LinkedInStateDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(ISSUED_AT_KEY, COLLECTION, false);
	}

	@Override
	public WriteResult<LinkedInState> insert(final LinkedInState state) {
		// remove transient fields from the element before saving it to the database
		final SameTransientStore<LinkedInState> store = startStore(state);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<LinkedInState>().id(id).build();
	}

	@Override
	public WriteResult<LinkedInState> insert(final LinkedInState state, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public LinkedInState update(final LinkedInState state) {
		// remove transient fields from the element before saving it to the database
		final SameTransientStore<LinkedInState> store = startStore(state);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(state.getState()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String state) {
		MONGODB_CONN.remove(key(state), COLLECTION);
	}

	@Override
	public List<LinkedInState> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public LinkedInState find(final String state) {
		final BasicDBObject obj = MONGODB_CONN.get(key(state), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<LinkedInState> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, toProjection(projection), count), new Function<BasicDBObject, LinkedInState>() {
			@Override
			public LinkedInState apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}
	
	@Override
	public List<String> typeahead(final String field, final String query, final int size) {
		throw new UnsupportedOperationException("Typeahead searches are not currently supported in this class");
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<LinkedInState> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<LinkedInState> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String key) {
		return new BasicDBObject(PRIMARY_KEY, key);		
	}

	private BasicDBObject issuedAtKey(final Date key) {
		return new BasicDBObject(ISSUED_AT_KEY, key.getTime());		
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(PRIMARY_KEY, 1);
	}

	private LinkedInState parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getLinkedInState();
	}

	private LinkedInState parseBasicDBObjectOrNull(final BasicDBObject obj) {
		LinkedInState state = null;		
		if (obj != null) {
			final LinkedInStateEntity entity = map(obj);
			if (entity != null) {
				state = entity.getLinkedInState();
			}
		}
		return state;
	}

	private DBObject map(final SameTransientStore<LinkedInState> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new LinkedInStateEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write access state to DB object", e);
		}
		return obj;
	}

	private LinkedInStateEntity map(final BasicDBObject obj) {
		LinkedInStateEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), LinkedInStateEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read access state from DB object", e);
		}
		return entity;
	}

	public List<LinkedInState> listByIssuedDate(final Date date) {
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, 0, Integer.MAX_VALUE, issuedAtKey(date), null, null), new Function<BasicDBObject, LinkedInState>() {
			@Override
			public LinkedInState apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
	}

	/**
	 * Checks whether or not the specified secret (state) was previously stored and is currently valid (not expired).
	 * @param secret - the secret associated to the state
	 * @param redirectUriRef - if set and the state is valid, then the redirect URI is returned to the caller
	 * @param callbackRef - if set and the state is valid, then the callback URI is returned to the caller
	 * @return {@code true} only if the provided secret (state) is found in the storage and is currently valid (not expired). 
	 *         Otherwise, returns {@code false}.
	 */
	public boolean isValid(final String secret, final @Nullable AtomicReference<String> redirectUriRef, final @Nullable AtomicReference<String> callbackRef) {
		checkArgument(isNotBlank(secret), "Uninitialized or invalid state");
		final LinkedInState state = find(secret);
		final boolean isValid = (state != null && state.getState() != null && secret.equals(state.getState())
				&& (state.getIssuedAt() + state.getExpiresIn()) > (currentTimeMillis() / 1000l));
		if (isValid) {
			if (redirectUriRef != null) {
				redirectUriRef.set(state.getRedirectUri());
			}
			if (callbackRef != null) {
				callbackRef.set(state.getCallback());
			}
		}
		return isValid;
	}

	/**
	 * Token entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class LinkedInStateEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private LinkedInState state;

		public LinkedInStateEntity() { }

		public LinkedInStateEntity(final LinkedInState state) {
			setLinkedInState(state);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public LinkedInState getLinkedInState() {
			return state;
		}

		public void setLinkedInState(final LinkedInState state) {
			this.state = state;
		}

	}

}