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
import static com.mongodb.util.JSON.parse;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

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

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.InvalidFilterParseException;
import eu.eubrazilcc.lvl.storage.InvalidSortParseException;
import eu.eubrazilcc.lvl.storage.SequenceGiKey;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;

/**
 * {@link Sequence} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum SequenceDAO implements BaseDAO<SequenceKey, Sequence> {

	SEQUENCE_DAO;

	private final static Logger LOGGER = getLogger(SequenceDAO.class);

	public static final String COLLECTION        = "sequences";
	public static final String DB_PREFIX         = "sequence.";
	public static final String PRIMARY_KEY_PART1 = DB_PREFIX + "dataSource";
	public static final String PRIMARY_KEY_PART2 = DB_PREFIX + "accession";
	public static final String GI_KEY            = DB_PREFIX + "gi";
	public static final String GEOLOCATION_KEY   = DB_PREFIX + "location";

	private SequenceDAO() {
		MONGODB_CONN.createIndex(ImmutableList.of(PRIMARY_KEY_PART1, PRIMARY_KEY_PART2), COLLECTION);
		MONGODB_CONN.createIndex(ImmutableList.of(PRIMARY_KEY_PART1, GI_KEY), COLLECTION);
		MONGODB_CONN.createGeospatialIndex(GEOLOCATION_KEY, COLLECTION);
		MONGODB_CONN.createTextIndex(ImmutableList.of(
				DB_PREFIX + "dataSource", 
				DB_PREFIX + "definition", 
				DB_PREFIX + "accession", 
				DB_PREFIX + "organism",
				DB_PREFIX + "countryFeature"), 
				COLLECTION);
	}

	@Override
	public WriteResult<Sequence> insert(final Sequence sequence) {
		// remove transient fields from the element before saving it to the database
		final SequenceTransientStore store = SequenceTransientStore.start(sequence);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<Sequence>().id(id).build();
	}

	@Override
	public WriteResult<Sequence> insert(final Sequence sequence, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public Sequence update(final Sequence sequence) {
		// remove transient fields from the element before saving it to the database
		final SequenceTransientStore store = SequenceTransientStore.start(sequence);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(SequenceKey.builder()
				.dataSource(sequence.getDataSource())
				.accession(sequence.getAccession())
				.build()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final SequenceKey sequenceKey) {
		MONGODB_CONN.remove(key(sequenceKey), COLLECTION);
	}

	@Override
	public List<Sequence> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public Sequence find(final SequenceKey sequenceKey) {
		final BasicDBObject obj = MONGODB_CONN.get(key(sequenceKey), COLLECTION);
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<Sequence> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		// parse the filter or return an empty list if the filter is invalid
		BasicDBObject query = null;
		try {
			query = buildQuery(filter);
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
		return transform(MONGODB_CONN.list(sort, COLLECTION, start, size, query, count), new Function<BasicDBObject, Sequence>() {
			@Override
			public Sequence apply(final BasicDBObject obj) {				
				return parseBasicDBObject(obj);
			}
		});
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<Sequence> getNear(final Point point, final double maxDistance) {
		final List<Sequence> sequences = newArrayList();
		final BasicDBList list = MONGODB_CONN.geoNear(COLLECTION, point.getCoordinates().getLongitude(), 
				point.getCoordinates().getLatitude(), maxDistance);
		for (int i = 0; i < list.size(); i++) {
			sequences.add(parseObject(list.get(i)));
		}
		return sequences;
	}

	@Override
	public List<Sequence> geoWithin(final Polygon polygon) {
		return transform(MONGODB_CONN.geoWithin(GEOLOCATION_KEY, COLLECTION, polygon), new Function<BasicDBObject, Sequence>() {
			@Override
			public Sequence apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	public Sequence find(final SequenceGiKey sequenceGiKey) {
		final BasicDBObject obj = MONGODB_CONN.get(keyGi(sequenceGiKey), COLLECTION);
		return parseBasicDBObjectOrNull(obj);
	}

	private BasicDBObject key(final SequenceKey key) {
		return new BasicDBObject(ImmutableMap.of(PRIMARY_KEY_PART1, key.getDataSource(), 
				PRIMARY_KEY_PART2, key.getAccession()));		
	}

	private BasicDBObject keyGi(final SequenceGiKey key) {
		return new BasicDBObject(ImmutableMap.of(PRIMARY_KEY_PART1, key.getDataSource(), 
				GI_KEY, key.getGi()));
	}

	private BasicDBObject sortCriteria(final @Nullable Sorting sorting) throws InvalidSortParseException {
		if (sorting != null) {			
			String field = null;
			// sortable fields
			if ("dataSource".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "dataSource";				
			} else if ("definition".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "definition";
			} else if ("accession".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "accession";
			} else if ("organism".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "organism";
			} else if ("countryFeature".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "countryFeature";
			} else if ("locale".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "locale";
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
		return new BasicDBObject(ImmutableMap.of(PRIMARY_KEY_PART1, 1, PRIMARY_KEY_PART2, 1));
	}

	private @Nullable BasicDBObject buildQuery(final @Nullable ImmutableMap<String, String> filter) throws InvalidFilterParseException {
		BasicDBObject query = null;
		if (filter != null) {
			for (final Entry<String, String> entry : filter.entrySet()) {
				query = parseFilter(entry.getKey(), entry.getValue(), query);
			}
		}
		return query;
	}

	private BasicDBObject parseFilter(final String parameter, final String expression, final BasicDBObject query) throws InvalidFilterParseException {
		BasicDBObject query2 = query;
		if (isNotBlank(parameter) && isNotBlank(expression)) {
			String field = null;
			// keyword matching search
			if ("source".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "dataSource";				
			} else if ("definition".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "definition";
			} else if ("accession".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "accession";
			} else if ("organism".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "organism";
			} else if ("country".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "countryFeature";
			} else if ("locale".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "locale";
			}
			if (isNotBlank(field)) {
				if ("accession".equalsIgnoreCase(parameter)) {
					// convert the expression to upper case and compare for exact matching
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression.toUpperCase());
				} else if ("locale".equalsIgnoreCase(parameter)) {
					// regular expression to match the language part of the locale
					final Pattern regex = Pattern.compile("(" + expression.toLowerCase() + ")([_]{1}[A-Z]{2}){0,1}");					
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, regex);
				} else {
					// regular expression to match all entries that contains the keyword
					final Pattern regex = Pattern.compile(".*" + expression + ".*", Pattern.CASE_INSENSITIVE);
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

	private Sequence parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getSequence();
	}

	private Sequence parseBasicDBObjectOrNull(final BasicDBObject obj) {
		Sequence sequence = null;
		if (obj != null) {
			final SequenceEntity entity = map(obj);
			if (entity != null) {
				sequence = entity.getSequence();
			}
		}
		return sequence;
	}

	private Sequence parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getSequence();
	}

	private DBObject map(final SequenceTransientStore store) {
		DBObject obj = null;
		try {
			obj = (DBObject) parse(JSON_MAPPER.writeValueAsString(new SequenceEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write sequence to DB object", e);
		}
		return obj;
	}

	private SequenceEntity map(final BasicDBObject obj) {
		SequenceEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), SequenceEntity.class);
		} catch (IOException e) {
			LOGGER.error("Failed to read sequence from DB object", e);
		}
		return entity;
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class SequenceTransientStore extends TransientStore<Sequence> {
		
		private List<Link> links;

		public SequenceTransientStore(final Sequence sequence) {
			super(sequence);
		}
		
		public List<Link> getLinks() {
			return links;
		}

		public Sequence purge() {
			links = element.getLinks();
			element.setLinks(null);
			return element;
		}

		public Sequence restore() {
			element.setLinks(links);
			return element;
		}

		public static SequenceTransientStore start(final Sequence sequence) {
			return new SequenceTransientStore(sequence);
		}

	}

	/**
	 * Sequence entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class SequenceEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private Sequence sequence;

		public SequenceEntity() { }

		public SequenceEntity(final Sequence sequence) {
			setSequence(sequence);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public Sequence getSequence() {
			return sequence;
		}

		public void setSequence(final Sequence sequence) {
			this.sequence = sequence;
		}

	}

}