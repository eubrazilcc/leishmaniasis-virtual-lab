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

package eu.eubrazilcc.lvl.storage.dao;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.transform.SameTransientStore.startStore;
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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.LvlInstance;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBDuplicateKeyException;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.SameTransientStore;

/**
 * {@link LvlInstance} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum LvlInstanceDAO implements BaseDAO<String, LvlInstance> {

	INSTANCE_DAO;

	private final static Logger LOGGER = getLogger(LvlInstanceDAO.class);

	public static final String COLLECTION      = "lvl_instances";
	public static final String DB_PREFIX       = "lvlInstance.";
	public static final String PRIMARY_KEY     = DB_PREFIX + "instanceId";
	public static final String GEOLOCATION_KEY = DB_PREFIX + "location";

	private LvlInstanceDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createGeospatialIndex(GEOLOCATION_KEY, COLLECTION);
	}

	@Override
	public WriteResult<LvlInstance> insert(final LvlInstance instance) {
		return insert(instance, false);
	}

	@Override
	public WriteResult<LvlInstance> insert(final LvlInstance instance, final boolean ignoreDuplicates) {
		// remove transient fields from the element before saving it to the database
		final SameTransientStore<LvlInstance> store = startStore(instance);
		final DBObject obj = map(store);
		String id = null; 		
		try {
			id = MONGODB_CONN.insert(obj, COLLECTION);
		} catch (MongoDBDuplicateKeyException dke) {
			if (ignoreDuplicates) {
				final BasicDBObject duplicate = MONGODB_CONN.get(key(instance.getInstanceId()), COLLECTION);
				id = entity2ObjectId(duplicate);
			} else {
				// re-throw exception
				throw dke;
			}
		}
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<LvlInstance>().id(id).build();
	}

	@Override
	public LvlInstance update(final LvlInstance instance) {
		// remove transient fields from the element before saving it to the database
		final SameTransientStore<LvlInstance> store = startStore(instance);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(instance.getInstanceId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String pmid) {
		MONGODB_CONN.remove(key(pmid), COLLECTION);
	}

	@Override
	public List<LvlInstance> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public LvlInstance find(final String pmid) {
		final BasicDBObject obj = MONGODB_CONN.get(key(pmid), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<LvlInstance> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {		
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, toProjection(projection), count), new Function<BasicDBObject, LvlInstance>() {
			@Override
			public LvlInstance apply(final BasicDBObject obj) {
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
	public List<LvlInstance> getNear(final Point point, final double maxDistance) {
		final List<LvlInstance> instances = newArrayList();
		final BasicDBList list = MONGODB_CONN.geoNear(COLLECTION, point.getCoordinates().getLongitude(), 
				point.getCoordinates().getLatitude(), maxDistance);
		for (int i = 0; i < list.size(); i++) {
			instances.add(parseObject(list.get(i)));
		}
		return instances;
	}

	@Override
	public List<LvlInstance> geoWithin(final Polygon polygon) {
		return transform(MONGODB_CONN.geoWithin(GEOLOCATION_KEY, COLLECTION, polygon), new Function<BasicDBObject, LvlInstance>() {
			@Override
			public LvlInstance apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
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

	private LvlInstance parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getLvlInstance();
	}

	private LvlInstance parseBasicDBObjectOrNull(final BasicDBObject obj) {
		LvlInstance instance = null;
		if (obj != null) {
			final LvlInstanceEntity entity = map(obj);
			if (entity != null) {
				instance = entity.getLvlInstance();
			}
		}
		return instance;
	}

	private String entity2ObjectId(final BasicDBObject obj) {
		return map(obj).getId().toString();
	}

	private LvlInstance parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getLvlInstance();
	}

	private DBObject map(final SameTransientStore<LvlInstance> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new LvlInstanceEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write instance to DB object", e);
		}
		return obj;
	}

	private LvlInstanceEntity map(final BasicDBObject obj) {
		LvlInstanceEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), LvlInstanceEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read instance from DB object", e);
		}
		return entity;
	}	

	/**
	 * LvlInstance entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class LvlInstanceEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private LvlInstance instance;

		public LvlInstanceEntity() { }

		public LvlInstanceEntity(final LvlInstance instance) {
			setLvlInstance(instance);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public LvlInstance getLvlInstance() {
			return instance;
		}

		public void setLvlInstance(final LvlInstance instance) {
			this.instance = instance;
		}

	}	

}