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

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.transform.SameTransientStore.startStore;
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

import eu.eubrazilcc.lvl.core.SavedSearch;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBDuplicateKeyException;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.SameTransientStore;

/**
 * {@link SavedSearch} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum SavedSearchDAO implements AuthenticatedDAO<String, SavedSearch> {

	SAVED_SEARCH_DAO;

	private final static Logger LOGGER = getLogger(ReferenceDAO.class);

	public static final String COLLECTION    = "saved_searches";
	public static final String DB_PREFIX     = "savedSearch.";
	public static final String PRIMARY_KEY   = DB_PREFIX + "id";
	public static final String NAMESPACE_KEY = DB_PREFIX + "namespace";
	public static final String SAVED_KEY     = DB_PREFIX + "saved";

	private SavedSearchDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(NAMESPACE_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(SAVED_KEY, COLLECTION, false);
	}

	@Override
	public WriteResult<SavedSearch> insert(final SavedSearch savedSearch) {
		return insert(savedSearch, false);
	}

	@Override
	public WriteResult<SavedSearch> insert(final SavedSearch savedSearch, final boolean ignoreDuplicates) {
		// remove transient fields from the element before saving it to the database
		final SameTransientStore<SavedSearch> store = startStore(savedSearch);
		final DBObject obj = map(store);
		String id = null; 		
		try {
			id = MONGODB_CONN.insert(obj, COLLECTION);
		} catch (MongoDBDuplicateKeyException dke) {
			if (ignoreDuplicates) {
				final BasicDBObject duplicate = MONGODB_CONN.get(key(savedSearch.getId()), COLLECTION);
				id = entity2ObjectId(duplicate);
			} else {
				// re-throw exception
				throw dke;
			}
		}
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<SavedSearch>().id(id).build();
	}

	@Override
	public SavedSearch update(final SavedSearch savedSearch) {
		// remove transient fields from the element before saving it to the database
		final SameTransientStore<SavedSearch> store = startStore(savedSearch);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(savedSearch.getId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String id) {
		MONGODB_CONN.remove(key(id), COLLECTION);
	}

	@Override
	public List<SavedSearch> findAll() {
		return findAll(null);
	}

	@Override
	public List<SavedSearch> findAll(final String user) {
		return list(0, Integer.MAX_VALUE, null, null, null, null, user);
	}

	@Override
	public SavedSearch find(final String path) {	
		return find(path, null);
	}

	@Override
	public SavedSearch find(final String path, final String user) {
		final BasicDBObject obj = MONGODB_CONN.get(isNotBlank(user) ? compositeKey(path, user) : key(path), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<SavedSearch> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
		return list(start, size, filter, sorting, projection, count, null);
	}	

	@Override
	public List<SavedSearch> list(final int start, final int size, final ImmutableMap<String, String> filter, final Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final MutableLong count, final String user) {
		// execute the query in the database using the user to filter the results in case that a valid one is provided
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, isNotBlank(user) ? submitterKey(user) : null, toProjection(projection), count), 
				new Function<BasicDBObject, SavedSearch>() {
			@Override
			public SavedSearch apply(final BasicDBObject obj) {
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
	public List<SavedSearch> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<SavedSearch> getNear(final Point point, final double maxDistance, final String user) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<SavedSearch> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<SavedSearch> geoWithin(final Polygon polygon, final String user) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String key) {
		return new BasicDBObject(PRIMARY_KEY, key);		
	}

	private BasicDBObject submitterKey(final String key) {
		return new BasicDBObject(NAMESPACE_KEY, key);		
	}

	private BasicDBObject compositeKey(final String path, final String submitter) {
		return new BasicDBObject(of(PRIMARY_KEY, path, NAMESPACE_KEY, submitter));
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(PRIMARY_KEY, 1);
	}

	private SavedSearch parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getSavedSearch();
	}

	private SavedSearch parseBasicDBObjectOrNull(final BasicDBObject obj) {
		SavedSearch savedSearch = null;
		if (obj != null) {
			final SavedSearchEntity entity = map(obj);
			if (entity != null) {
				savedSearch = entity.getSavedSearch();
			}
		}
		return savedSearch;
	}

	private String entity2ObjectId(final BasicDBObject obj) {
		return map(obj).getId().toString();
	}

	@SuppressWarnings("unused")
	private SavedSearch parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getSavedSearch();
	}

	private DBObject map(final SameTransientStore<SavedSearch> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new SavedSearchEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write saved search to DB object", e);
		}
		return obj;
	}

	private SavedSearchEntity map(final BasicDBObject obj) {
		SavedSearchEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), SavedSearchEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read saved search from DB object", e);
		}
		return entity;
	}	

	/**
	 * {@link SavedSearch} entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class SavedSearchEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private SavedSearch savedSearch;

		public SavedSearchEntity() { }

		public SavedSearchEntity(final SavedSearch savedSearch) {
			setSavedSearch(savedSearch);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public SavedSearch getSavedSearch() {
			return savedSearch;
		}

		public void setSavedSearch(final SavedSearch savedSearch) {
			this.savedSearch = savedSearch;
		}

	}

}