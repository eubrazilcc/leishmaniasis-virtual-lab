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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.core.CollectionNames.SHARED_OBJECTS_COLLECTION;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBHelper.toProjection;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore.startStore;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;
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

import eu.eubrazilcc.lvl.core.ObjectAccepted;
import eu.eubrazilcc.lvl.core.ObjectGranted;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.InvalidFilterParseException;
import eu.eubrazilcc.lvl.storage.InvalidSortParseException;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link ObjectGranted} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum SharedObjectDAO implements AuthenticatedDAO<String, ObjectGranted> {

	SHARED_OBJECT_DAO;

	private final static Logger LOGGER = getLogger(SharedObjectDAO.class);

	public static final String COLLECTION       = SHARED_OBJECTS_COLLECTION;
	public static final String DB_PREFIX        = "objectGranted.";
	public static final String PRIMARY_KEY      = DB_PREFIX + "id";
	public static final String UNIQUE_KEY_PART1 = DB_PREFIX + "user";
	public static final String UNIQUE_KEY_PART2 = DB_PREFIX + "collection";
	public static final String UNIQUE_KEY_PART3 = DB_PREFIX + "itemId";	
	public static final String SHARED_DATE_KEY  = DB_PREFIX + "sharedDate";

	private SharedObjectDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createIndex(ImmutableList.of(UNIQUE_KEY_PART1, UNIQUE_KEY_PART2, UNIQUE_KEY_PART3), COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(DB_PREFIX + "owner", COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(DB_PREFIX + "user", COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(DB_PREFIX + "collection", COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(SHARED_DATE_KEY, COLLECTION, true);
		MONGODB_CONN.createNonUniqueIndex(DB_PREFIX + "accessType", COLLECTION, false);
	}

	@Override
	public WriteResult<ObjectGranted> insert(final ObjectGranted objGranted) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<ObjectGranted> store = startStore(objGranted);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<ObjectGranted>().id(id).build();
	}

	@Override
	public WriteResult<ObjectGranted> insert(final ObjectGranted objGranted, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public ObjectGranted update(final ObjectGranted objGranted) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<ObjectGranted> store = startStore(objGranted);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(objGranted.getId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String id) {
		MONGODB_CONN.remove(key(id), COLLECTION);
	}

	@Override
	public List<ObjectGranted> findAll() {
		return findAll(null);
	}

	@Override
	public List<ObjectGranted> findAll(final String owner) {
		return list(0, Integer.MAX_VALUE, null, null, null, null, owner);
	}

	@Override
	public ObjectGranted find(final String id) {	
		return find(id, null);
	}

	@Override
	public ObjectGranted find(final String id, final String owner) {
		final BasicDBObject obj = MONGODB_CONN.get(isNotBlank(owner) ? key(id).append(String.format("%s%s", DB_PREFIX, "owner"), owner) : key(id), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<ObjectGranted> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
		return list(start, size, filter, sorting, projection, count, null);
	}	

	@Override
	public List<ObjectGranted> list(final int start, final int size, final ImmutableMap<String, String> filter, final Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final MutableLong count, final String owner) {
		// parse the filter or return an empty list if the filter is invalid
		BasicDBObject query = null;
		try {
			query = buildQuery(filter, owner);
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
		return transform(MONGODB_CONN.list(sort, COLLECTION, start, size, query, toProjection(projection), count), new Function<BasicDBObject, ObjectGranted>() {
			@Override
			public ObjectGranted apply(final BasicDBObject obj) {				
				return parseBasicDBObject(obj);
			}
		});
	}

	public ObjectAccepted findAccepted(final String id, final String user) {
		ObjectAccepted objAccepted = null;
		try {
			final BasicDBObject obj = MONGODB_CONN.get(key(id).append(String.format("%s%s", DB_PREFIX, "user"), 
					requireNonNull(trimToNull(user), "A valid user expected")), COLLECTION);		
			final ObjectGranted objGranted = parseBasicDBObjectOrNull(obj);			
			if (objGranted != null) {
				ObjectAccepted tmp = new ObjectAccepted();
				copyProperties(objGranted, tmp);
				objAccepted = tmp;
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOGGER.error("Failed to convert shared object", e);
		}
		return objAccepted;
	}

	public List<ObjectAccepted> listAccepted(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count, final String user) {
		final List<ObjectAccepted> objsAccepted = newArrayList();
		final ImmutableMap.Builder<String, String> filterBuilder = new ImmutableMap.Builder<String, String>()
				.put("user", requireNonNull(trimToNull(user), "A valid user expected"));
		ofNullable(filter).orElse(ImmutableMap.of()).entrySet().stream().filter(e -> !"user".equals(e.getKey())).forEach(e -> {
			filterBuilder.put(e.getKey(), e.getValue());
		});
		final List<ObjectGranted> objsGranted = list(start, size, filterBuilder.build(), sorting, projection, count);
		if (objsGranted != null) {
			for (final ObjectGranted objGranted : objsGranted) {
				try {
					final ObjectAccepted objAccepted = new ObjectAccepted();
					copyProperties(objGranted, objAccepted);
					objsAccepted.add(objAccepted);
				} catch (IllegalAccessException | InvocationTargetException e) {
					LOGGER.error("Failed to convert shared object", e);
				}
			}
		}
		return objsAccepted;
	}

	private static final List<String> FIELDS_TO_SUPPRESS = ImmutableList.of("links", "urlSafeNs", "urlSafeCol", "urlSafeId");	

	public static void copyProperties(final ObjectGranted orig, final ObjectAccepted dest) throws IllegalAccessException, InvocationTargetException {
		final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
		propertyUtilsBean.addBeanIntrospector(new SuppressPropertiesBeanIntrospector(FIELDS_TO_SUPPRESS));
		final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean(), propertyUtilsBean);
		beanUtilsBean.copyProperties(dest, orig);
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
	public long count(final String owner) {
		return MONGODB_CONN.count(COLLECTION, new BasicDBObject(String.format("%s%s", DB_PREFIX, "owner"), owner));
	}

	@Override
	public List<ObjectGranted> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<ObjectGranted> getNear(final Point point, final double maxDistance, final String owner) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<ObjectGranted> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<ObjectGranted> geoWithin(final Polygon polygon, final String owner) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String id) {
		return new BasicDBObject(PRIMARY_KEY, id);		
	}

	private BasicDBObject sortCriteria(final @Nullable Sorting sorting) throws InvalidSortParseException {
		if (sorting != null) {
			String field = null;
			// sortable fields
			if ("owner".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "owner";
			} else if ("user".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "user";
			} else if ("collection".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "collection";
			} else if ("itemId".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "itemId";
			} else if ("sharedDate".equalsIgnoreCase(sorting.getField())) {
				field = SHARED_DATE_KEY;
			} else if ("accessType".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "accessType";
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
		// shared date order
		return new BasicDBObject(ImmutableMap.of(SHARED_DATE_KEY, -1));
	}

	private @Nullable BasicDBObject buildQuery(final @Nullable ImmutableMap<String, String> filter, final @Nullable String owner) 
			throws InvalidFilterParseException {
		BasicDBObject query = null;		
		if (filter != null) {
			for (final Entry<String, String> entry : filter.entrySet()) {
				query = parseFilter(entry.getKey(), entry.getValue(), query);
			}
		}
		return isNotBlank(owner) ? (query != null ? query : new BasicDBObject()).append(String.format("%s%s", DB_PREFIX, "owner"), owner) : query;
	}

	private BasicDBObject parseFilter(final String parameter, final String expression, final BasicDBObject query) throws InvalidFilterParseException {
		BasicDBObject query2 = query;
		if (isNotBlank(parameter) && isNotBlank(expression)) {
			String field = null;
			// keyword matching search
			if ("user".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "user";
			} else if ("collection".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "collection";
			} else if ("itemId".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "itemId";
			} else if ("accessType".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "accessType";
			}
			if (isNotBlank(field)) {
				if ("user".equalsIgnoreCase(parameter) || "collection".equalsIgnoreCase(parameter) || "itemId".equalsIgnoreCase(parameter)) {
					// compare for exact matching
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression);
				} else if ("accessType".equalsIgnoreCase(parameter)) {
					// convert the expression to upper case and compare for exact matching
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression.toUpperCase());
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

	private ObjectGranted parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getObjectGranted();
	}

	private ObjectGranted parseBasicDBObjectOrNull(final BasicDBObject obj) {
		ObjectGranted objGranted = null;
		if (obj != null) {
			final ObjectGrantedEntity entity = map(obj);
			if (entity != null) {
				objGranted = entity.getObjectGranted();
			}
		}
		return objGranted;
	}

	private DBObject map(final LinkableTransientStore<ObjectGranted> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new ObjectGrantedEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write saved search to DB object", e);
		}
		return obj;
	}	

	private ObjectGrantedEntity map(final BasicDBObject obj) {
		ObjectGrantedEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), ObjectGrantedEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read saved search from DB object", e);
		}
		return entity;
	}	

	/**
	 * {@link ObjectGranted} entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class ObjectGrantedEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private ObjectGranted objectGranted;

		public ObjectGrantedEntity() { }

		public ObjectGrantedEntity(final ObjectGranted objectGranted) {
			setObjectGranted(objectGranted);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public ObjectGranted getObjectGranted() {
			return objectGranted;
		}

		public void setObjectGranted(final ObjectGranted objectGranted) {
			this.objectGranted = objectGranted;
		}

	}

}