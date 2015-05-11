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

package eu.eubrazilcc.lvl.storage.support.dao;

import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore.startStore;
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
import eu.eubrazilcc.lvl.core.support.SubscriptionRequest;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link SubscriptionRequest} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum SubscriptionRequestDAO implements BaseDAO<String, SubscriptionRequest> {

	SUBSCRIPTION_REQ_DAO;

	private final static Logger LOGGER = getLogger(SubscriptionRequestDAO.class);

	public static final String COLLECTION  = "subscription_requests";
	public static final String DB_PREFIX   = "subscriptionRequest.";
	public static final String PRIMARY_KEY = DB_PREFIX + "id";
	public static final String REQUESTED_KEY = DB_PREFIX + "requested";
	public static final String FULFILLED_KEY = DB_PREFIX + "fulfilled";

	private SubscriptionRequestDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(REQUESTED_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(FULFILLED_KEY, COLLECTION, false);
	}

	@Override
	public WriteResult<SubscriptionRequest> insert(final SubscriptionRequest request) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<SubscriptionRequest> store = startStore(request);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<SubscriptionRequest>().id(id).build();
	}

	@Override
	public WriteResult<SubscriptionRequest> insert(final SubscriptionRequest request, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public SubscriptionRequest update(final SubscriptionRequest request) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<SubscriptionRequest> store = startStore(request);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(request.getId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String pmid) {
		MONGODB_CONN.remove(key(pmid), COLLECTION);
	}

	@Override
	public List<SubscriptionRequest> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public SubscriptionRequest find(final String pmid) {
		final BasicDBObject obj = MONGODB_CONN.get(key(pmid), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<SubscriptionRequest> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {		
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, toProjection(projection), count), new Function<BasicDBObject, SubscriptionRequest>() {
			@Override
			public SubscriptionRequest apply(final BasicDBObject obj) {
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
	public List<SubscriptionRequest> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<SubscriptionRequest> geoWithin(final Polygon polygon) {
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
		return new BasicDBObject(REQUESTED_KEY, -1);
	}

	private SubscriptionRequest parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getSubscriptionRequest();
	}

	private SubscriptionRequest parseBasicDBObjectOrNull(final BasicDBObject obj) {
		SubscriptionRequest request = null;
		if (obj != null) {
			final SubscriptionRequestEntity entity = map(obj);
			if (entity != null) {
				request = entity.getSubscriptionRequest();
			}
		}
		return request;
	}

	@SuppressWarnings("unused")
	private String entity2ObjectId(final BasicDBObject obj) {
		return map(obj).getId().toString();
	}

	@SuppressWarnings("unused")
	private SubscriptionRequest parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getSubscriptionRequest();
	}

	private DBObject map(final LinkableTransientStore<SubscriptionRequest> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new SubscriptionRequestEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write request to DB object", e);
		}
		return obj;
	}

	private SubscriptionRequestEntity map(final BasicDBObject obj) {
		SubscriptionRequestEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), SubscriptionRequestEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read request from DB object", e);
		}
		return entity;
	}	

	/**
	 * SubscriptionRequest entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class SubscriptionRequestEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private SubscriptionRequest request;

		public SubscriptionRequestEntity() { }

		public SubscriptionRequestEntity(final SubscriptionRequest request) {
			setSubscriptionRequest(request);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public SubscriptionRequest getSubscriptionRequest() {
			return request;
		}

		public void setSubscriptionRequest(final SubscriptionRequest request) {
			this.request = request;
		}

	}	

}