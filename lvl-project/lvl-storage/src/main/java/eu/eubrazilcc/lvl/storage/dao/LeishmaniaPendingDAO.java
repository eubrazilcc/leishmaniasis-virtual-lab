/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
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
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.core.CollectionNames.LEISHMANIA_PENDING_COLLECTION;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBComparison.mongoNumeriComparison;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore.startStore;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.LeishmaniaPending;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.InvalidFilterParseException;
import eu.eubrazilcc.lvl.storage.InvalidSortParseException;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link LeishmaniaPending} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum LeishmaniaPendingDAO implements AuthenticatedDAO<String, LeishmaniaPending> {

	LEISHMANIA_PENDING_DAO;

	private final static Logger LOGGER = getLogger(LeishmaniaPendingDAO.class);

	public static final String COLLECTION    = LEISHMANIA_PENDING_COLLECTION;
	public static final String DB_PREFIX     = "leishmaniaPending.";
	public static final String PRIMARY_KEY   = DB_PREFIX + "id";
	public static final String NAMESPACE_KEY = DB_PREFIX + "namespace";

	public static final String ORIGINAL_SAMPLE_KEY = DB_PREFIX + "sample";

	private LeishmaniaPendingDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(NAMESPACE_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(ORIGINAL_SAMPLE_KEY + ".collectionCode", COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(ORIGINAL_SAMPLE_KEY + ".catalogNumber", COLLECTION, false);
		MONGODB_CONN.createTextIndex(ImmutableList.of(
				ORIGINAL_SAMPLE_KEY + ".recordedBy",
				ORIGINAL_SAMPLE_KEY + ".stateProvince",
				ORIGINAL_SAMPLE_KEY + ".county",
				ORIGINAL_SAMPLE_KEY + ".locality",
				ORIGINAL_SAMPLE_KEY + ".identifiedBy",
				ORIGINAL_SAMPLE_KEY + ".scientificName",
				ORIGINAL_SAMPLE_KEY + ".phylum",
				ORIGINAL_SAMPLE_KEY + ".clazz",
				ORIGINAL_SAMPLE_KEY + ".order",
				ORIGINAL_SAMPLE_KEY + ".family",
				ORIGINAL_SAMPLE_KEY + ".genus",
				ORIGINAL_SAMPLE_KEY + ".specificEpithet"),
				COLLECTION);
	}

	@Override
	public WriteResult<LeishmaniaPending> insert(final LeishmaniaPending pendingSeq) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<LeishmaniaPending> store = startStore(pendingSeq);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<LeishmaniaPending>().id(id).build();
	}

	@Override
	public WriteResult<LeishmaniaPending> insert(final LeishmaniaPending pendingSeq, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public LeishmaniaPending update(final LeishmaniaPending pendingSeq) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<LeishmaniaPending> store = startStore(pendingSeq);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(pendingSeq.getId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String id) {
		MONGODB_CONN.remove(key(id), COLLECTION);
	}

	@Override
	public List<LeishmaniaPending> findAll() {
		return findAll(null);
	}

	@Override
	public List<LeishmaniaPending> findAll(final String user) {
		return list(0, Integer.MAX_VALUE, null, null, null, null, user);
	}

	@Override
	public LeishmaniaPending find(final String path) {	
		return find(path, null);
	}

	@Override
	public LeishmaniaPending find(final String path, final String user) {
		final BasicDBObject obj = MONGODB_CONN.get(isNotBlank(user) ? compositeKey(path, user) : key(path), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<LeishmaniaPending> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
		return list(start, size, filter, sorting, projection, count, null);
	}	

	@Override
	public List<LeishmaniaPending> list(final int start, final int size, final ImmutableMap<String, String> filter, final Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final MutableLong count, final String user) {
		// parse the filter or return an empty list if the filter is invalid
		BasicDBObject query = null;
		try {
			query = buildQuery(filter, user);
		} catch (InvalidFilterParseException e) {
			LOGGER.warn("Discarding operation after an invalid filter was found: " + e.getMessage());
			return newArrayList();
		}
		// parse the sorting information or return an empty list if the sort is invalid
		BasicDBObject sort = null;
		try {
			sort = sortCriteria(sorting);
		} catch (InvalidSortParseException e) {
			LOGGER.warn("Discarding operation after an invalid sort was found: " + e.getMessage());
			return newArrayList();
		}
		// execute the query in the database
		return transform(MONGODB_CONN.list(sort, COLLECTION, start, size, query, toProjection(projection), count), new Function<BasicDBObject, LeishmaniaPending>() {
			@Override
			public LeishmaniaPending apply(final BasicDBObject obj) {				
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
	public List<LeishmaniaPending> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<LeishmaniaPending> getNear(final Point point, final double maxDistance, final String user) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<LeishmaniaPending> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<LeishmaniaPending> geoWithin(final Polygon polygon, final String user) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String key) {
		return new BasicDBObject(PRIMARY_KEY, key);		
	}

	private BasicDBObject compositeKey(final String path, final String submitter) {
		return new BasicDBObject(of(PRIMARY_KEY, path, NAMESPACE_KEY, submitter));
	}

	private BasicDBObject sortCriteria(final @Nullable Sorting sorting) throws InvalidSortParseException {
		if (sorting != null) {			
			String field = null;
			// sortable fields
			if ("collection".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".collectionCode";
			} else if ("catalogNumber".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".catalogNumber";
			} else if ("year".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".year";				
			} else if ("country".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".country";
			} else if ("province".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".stateProvince";
			} else if ("county".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".county";
			} else if ("locality".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".locality";
			} else if ("epithet".equalsIgnoreCase(sorting.getField())) {
				field = ORIGINAL_SAMPLE_KEY + ".specificEpithet";				
			}
			if (isNotBlank(field)) {
				int order = 1;
				switch (sorting.getOrder()) {
				case ASC:
					order = 1;
					break;
				case DESC:
					order = -1;
					break;
				default:
					order = 1;
					break;
				}
				return new BasicDBObject(field, order);
			} else {				
				throw new InvalidSortParseException(sorting.getField());					
			}				
		}
		// insertion order
		return new BasicDBObject(ImmutableMap.of(PRIMARY_KEY, 1));
	}

	private @Nullable BasicDBObject buildQuery(final @Nullable ImmutableMap<String, String> filter, final @Nullable String user) 
			throws InvalidFilterParseException {
		BasicDBObject query = null;		
		if (filter != null) {
			for (final Entry<String, String> entry : filter.entrySet()) {
				query = parseFilter(entry.getKey(), entry.getValue(), query);
			}
		}		
		return isNotBlank(user) ? (query != null ? query : new BasicDBObject()).append(NAMESPACE_KEY, user) : query;
	}

	private BasicDBObject parseFilter(final String parameter, final String expression, final BasicDBObject query) throws InvalidFilterParseException {
		BasicDBObject query2 = query;
		if (isNotBlank(parameter) && isNotBlank(expression)) {
			String field = null;
			// keyword matching search
			if ("collection".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".collectionCode";
			} else if ("catalogNumber".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".catalogNumber";
			} else if ("year".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".year";				
			} else if ("country".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".country";
			} else if ("province".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".stateProvince";
			} else if ("county".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".county";
			} else if ("locality".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".locality";
			} else if ("epithet".equalsIgnoreCase(parameter)) {
				field = ORIGINAL_SAMPLE_KEY + ".specificEpithet";				
			}
			if (isNotBlank(field)) {
				if ("catalogNumber".equalsIgnoreCase(parameter)) {
					// compare for exact matching
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression);
				} else if ("year".equalsIgnoreCase(parameter)) {
					// comparison operator
					query2 = mongoNumeriComparison(field, expression);
				} else {
					// regular expression to match all entries that contains the keyword
					final Pattern regex = compile(".*" + expression + ".*", CASE_INSENSITIVE);
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, regex);
				}
			} else {
				// full-text search
				if ("text".equalsIgnoreCase(parameter)) {
					field = "$text";
				}
				if (isNotBlank(field)) {
					if (query2 != null) {
						final BasicDBObject textSearch = (BasicDBObject)query2.get("$text");
						final BasicDBObject search = new BasicDBObject("$search", textSearch != null 
								? textSearch.getString("$search") + " " + expression : expression);
						query2 = query2.append("$text", search.append("$language", "english"));
					} else {
						final BasicDBObject search = new BasicDBObject("$search", expression);
						query2 = new BasicDBObject().append("$text", search.append("$language", "english"));					
					}
				} else {				
					throw new InvalidFilterParseException(parameter);					
				}
			}
		}
		return query2;
	}

	private LeishmaniaPending parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getLeishmaniaPending();
	}

	private LeishmaniaPending parseBasicDBObjectOrNull(final BasicDBObject obj) {
		LeishmaniaPending pendingSeq = null;
		if (obj != null) {
			final LeishmaniaPendingEntity entity = map(obj);
			if (entity != null) {
				pendingSeq = entity.getLeishmaniaPending();
			}
		}
		return pendingSeq;
	}

	private DBObject map(final LinkableTransientStore<LeishmaniaPending> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new LeishmaniaPendingEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write saved search to DB object", e);
		}
		return obj;
	}	

	private LeishmaniaPendingEntity map(final BasicDBObject obj) {
		LeishmaniaPendingEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), LeishmaniaPendingEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read saved search from DB object", e);
		}
		return entity;
	}	

	/**
	 * {@link LeishmaniaPending} entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class LeishmaniaPendingEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private LeishmaniaPending leishmaniaPending;

		public LeishmaniaPendingEntity() { }

		public LeishmaniaPendingEntity(final LeishmaniaPending leishmaniaPending) {
			setLeishmaniaPending(leishmaniaPending);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public LeishmaniaPending getLeishmaniaPending() {
			return leishmaniaPending;
		}

		public void setLeishmaniaPending(final LeishmaniaPending leishmaniaPending) {
			this.leishmaniaPending = leishmaniaPending;
		}

	}

}