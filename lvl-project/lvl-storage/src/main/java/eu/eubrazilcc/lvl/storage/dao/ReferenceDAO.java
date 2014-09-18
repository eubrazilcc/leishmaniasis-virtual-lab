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
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBDuplicateKeyException;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;

/**
 * {@link Reference} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum ReferenceDAO implements BaseDAO<String, Reference> {

	REFERENCE_DAO;

	private final static Logger LOGGER = getLogger(ReferenceDAO.class);

	public static final String COLLECTION      = "references";
	public static final String DB_PREFIX       = "reference.";
	public static final String PRIMARY_KEY     = DB_PREFIX + "pubmedId";
	public static final String GEOLOCATION_KEY = DB_PREFIX + "location";

	private ReferenceDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createGeospatialIndex(GEOLOCATION_KEY, COLLECTION);
		MONGODB_CONN.createTextIndex(ImmutableList.of(
				DB_PREFIX + "title"), 
				COLLECTION);
	}

	@Override
	public WriteResult<Reference> insert(final Reference reference) {
		return insert(reference, false);
	}

	@Override
	public WriteResult<Reference> insert(final Reference reference, final boolean ignoreDuplicates) {
		// remove transient fields from the element before saving it to the database
		final ReferenceTransientStore store = ReferenceTransientStore.start(reference);
		final DBObject obj = map(store);
		String id = null; 		
		try {
			id = MONGODB_CONN.insert(obj, COLLECTION);
		} catch (MongoDBDuplicateKeyException dke) {
			if (ignoreDuplicates) {
				final BasicDBObject duplicate = MONGODB_CONN.get(key(reference.getPubmedId()), COLLECTION);
				id = entity2ObjectId(duplicate);
			} else {
				// re-throw exception
				throw dke;
			}
		}
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<Reference>().id(id).build();
	}

	@Override
	public Reference update(final Reference reference) {
		// remove transient fields from the element before saving it to the database
		final ReferenceTransientStore store = ReferenceTransientStore.start(reference);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(reference.getPubmedId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String pmid) {
		MONGODB_CONN.remove(key(pmid), COLLECTION);
	}

	@Override
	public List<Reference> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public Reference find(final String pmid) {
		final BasicDBObject obj = MONGODB_CONN.get(key(pmid), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<Reference> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {		
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, count), new Function<BasicDBObject, Reference>() {
			@Override
			public Reference apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<Reference> getNear(final Point point, final double maxDistance) {
		final List<Reference> references = newArrayList();
		final BasicDBList list = MONGODB_CONN.geoNear(COLLECTION, point.getCoordinates().getLongitude(), 
				point.getCoordinates().getLatitude(), maxDistance);
		for (int i = 0; i < list.size(); i++) {
			references.add(parseObject(list.get(i)));
		}
		return references;
	}

	@Override
	public List<Reference> geoWithin(final Polygon polygon) {
		return transform(MONGODB_CONN.geoWithin(GEOLOCATION_KEY, COLLECTION, polygon), new Function<BasicDBObject, Reference>() {
			@Override
			public Reference apply(final BasicDBObject obj) {
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

	private Reference parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getReference();
	}

	private Reference parseBasicDBObjectOrNull(final BasicDBObject obj) {
		Reference reference = null;
		if (obj != null) {
			final ReferenceEntity entity = map(obj);
			if (entity != null) {
				reference = entity.getReference();
			}
		}
		return reference;
	}

	private String entity2ObjectId(final BasicDBObject obj) {
		return map(obj).getId().toString();
	}

	private Reference parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getReference();
	}

	private DBObject map(final ReferenceTransientStore store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new ReferenceEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write reference to DB object", e);
		}
		return obj;
	}

	private ReferenceEntity map(final BasicDBObject obj) {
		ReferenceEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), ReferenceEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read reference from DB object", e);
		}
		return entity;
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class ReferenceTransientStore extends TransientStore<Reference> {

		public ReferenceTransientStore(final Reference reference) {
			super(reference);
		}

		public Reference purge() {
			return element;
		}

		public Reference restore() {
			return element;
		}

		public static ReferenceTransientStore start(final Reference reference) {
			return new ReferenceTransientStore(reference);
		}

	}

	/**
	 * Reference entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class ReferenceEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private Reference reference;

		public ReferenceEntity() { }

		public ReferenceEntity(final Reference reference) {
			setReference(reference);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public Reference getReference() {
			return reference;
		}

		public void setReference(final Reference reference) {
			this.reference = reference;
		}

	}	

}