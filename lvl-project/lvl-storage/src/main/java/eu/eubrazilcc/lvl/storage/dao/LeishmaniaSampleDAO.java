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
import static com.mongodb.util.JSON.parse;
import static eu.eubrazilcc.lvl.core.CollectionNames.LEISHMANIA_SAMPLES_COLLECTION;
import static eu.eubrazilcc.lvl.core.SimpleStat.normalizeStats;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.eubrazilcc.lvl.core.LeishmaniaSample;
import eu.eubrazilcc.lvl.core.SimpleStat;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.InvalidFilterParseException;
import eu.eubrazilcc.lvl.storage.InvalidSortParseException;
import eu.eubrazilcc.lvl.storage.SampleKey;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link LeishmaniaSample} DAO that manages the collection of CLIOC samples in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum LeishmaniaSampleDAO implements SampleDAO<LeishmaniaSample> {

	LEISHMANIA_SAMPLE_DAO;

	private final static Logger LOGGER = getLogger(LeishmaniaSampleDAO.class);	

	public static final String COLLECTION        = LEISHMANIA_SAMPLES_COLLECTION;
	public static final String DB_PREFIX         = "leishmaniaSample.";
	public static final String PRIMARY_KEY_PART1 = DB_PREFIX + "collectionId";
	public static final String PRIMARY_KEY_PART2 = DB_PREFIX + "catalogNumber";
	public static final String GEOLOCATION_KEY   = DB_PREFIX + "location";

	public static final String ORIGINAL_SAMPLE_KEY = DB_PREFIX + "sample";

	private LeishmaniaSampleDAO() {
		MONGODB_CONN.createIndex(ImmutableList.of(PRIMARY_KEY_PART1, PRIMARY_KEY_PART2), COLLECTION);		
		MONGODB_CONN.createGeospatialIndex(GEOLOCATION_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(ORIGINAL_SAMPLE_KEY + ".year", COLLECTION, false);
		MONGODB_CONN.createTextIndex(ImmutableList.of(
				DB_PREFIX + "collectionId",
				DB_PREFIX + "catalogNumber",
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
		MONGODB_CONN.createNonUniqueIndex(PRIMARY_KEY_PART1, COLLECTION, false);
		MONGODB_CONN.createSparseIndex(GEOLOCATION_KEY, COLLECTION, false);
	}	

	@Override
	public WriteResult<LeishmaniaSample> insert(final LeishmaniaSample sample) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<LeishmaniaSample> store = startStore(sample);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<LeishmaniaSample>().id(id).build();
	}

	@Override
	public WriteResult<LeishmaniaSample> insert(final LeishmaniaSample sample, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public LeishmaniaSample update(final LeishmaniaSample sample) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<LeishmaniaSample> store = startStore(sample);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(SampleKey.builder()
				.collectionId(sample.getCollectionId())
				.catalogNumber(sample.getCatalogNumber())
				.build()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final SampleKey sampleKey) {
		MONGODB_CONN.remove(key(sampleKey), COLLECTION);
	}

	@Override
	public List<LeishmaniaSample> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public LeishmaniaSample find(final SampleKey sampleKey) {
		final BasicDBObject obj = MONGODB_CONN.get(key(sampleKey), COLLECTION);
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<LeishmaniaSample> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, 
			final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
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
		return transform(MONGODB_CONN.list(sort, COLLECTION, start, size, query, toProjection(projection), count), new Function<BasicDBObject, LeishmaniaSample>() {
			@Override
			public LeishmaniaSample apply(final BasicDBObject obj) {				
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
	public List<LeishmaniaSample> getNear(final Point point, final double maxDistance) {
		final List<LeishmaniaSample> samples = newArrayList();
		final BasicDBList list = MONGODB_CONN.geoNear(COLLECTION, point.getCoordinates().getLongitude(), 
				point.getCoordinates().getLatitude(), maxDistance);
		for (int i = 0; i < list.size(); i++) {
			samples.add(parseObject(list.get(i)));
		}
		return samples;
	}

	@Override
	public List<LeishmaniaSample> geoWithin(final Polygon polygon) {
		return transform(MONGODB_CONN.geoWithin(GEOLOCATION_KEY, COLLECTION, polygon), new Function<BasicDBObject, LeishmaniaSample>() {
			@Override
			public LeishmaniaSample apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	public Map<String, List<SimpleStat>> collectionStats() {
		final Map<String, List<SimpleStat>> stats = new Hashtable<String, List<SimpleStat>>();
		// count samples per collection
		final List<SimpleStat> srcStats = newArrayList();		
		Iterable<DBObject> results = MONGODB_CONN.dataSourceStats(COLLECTION, DB_PREFIX);		
		for (final DBObject result : results) {
			srcStats.add(SimpleStat.builder()
					.label((String)((DBObject)result.get("_id")).get(DB_PREFIX + "collectionId"))
					.value((Integer)result.get("number"))
					.build());
		}
		stats.put(COLLECTION + ".collection", normalizeStats(srcStats));
		// total number of samples
		final long totalCount = MONGODB_CONN.count(COLLECTION);
		// count georeferred samples
		final List<SimpleStat> gisStats = newArrayList();		
		final int georefCount = MONGODB_CONN.countGeoreferred(COLLECTION, DB_PREFIX, new BasicDBObject(DB_PREFIX + "sample", 0));
		gisStats.add(SimpleStat.builder()
				.label("Yes")
				.value(georefCount)
				.build());
		gisStats.add(SimpleStat.builder()
				.label("No")
				.value((int)totalCount - georefCount)
				.build());
		stats.put(COLLECTION + ".gis", normalizeStats(gisStats));
		/* TODO
		// count collections per gene
		final List<SimpleStat> geneStats = newArrayList();		
		results = MONGODB_CONN.geneStats(COLLECTION, DB_PREFIX);		
		for (final DBObject result : results) {
			geneStats.add(SimpleStat.builder()
					.label((String)((DBObject)result.get("_id")).get(DB_PREFIX + "gene"))
					.value((Integer)result.get("number"))
					.build());
		}
		stats.put(COLLECTION + ".gene", normalizeStats(geneStats)); */
		return stats;
	}

	private BasicDBObject key(final SampleKey key) {
		return new BasicDBObject(ImmutableMap.of(PRIMARY_KEY_PART1, key.getCollectionId(), 
				PRIMARY_KEY_PART2, key.getCatalogNumber()));
	}

	private BasicDBObject sortCriteria(final @Nullable Sorting sorting) throws InvalidSortParseException {
		if (sorting != null) {			
			String field = null;
			// sortable fields
			if ("collection".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "collectionId";
			} else if ("catalogNumber".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "catalogNumber";				
			} else if ("locale".equalsIgnoreCase(sorting.getField())) {
				field = DB_PREFIX + "locale";
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
			if ("collection".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "collectionId";				
			} else if ("catalogNumber".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "catalogNumber";
			} else if ("locale".equalsIgnoreCase(parameter)) {
				field = DB_PREFIX + "locale";
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
					// convert the expression to upper case and compare for exact matching
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression.toUpperCase());
				} else if ("locale".equalsIgnoreCase(parameter)) {					
					if (compile("[a-z]{2}").matcher(expression).matches()) {
						// search the language part of the locale
						final Pattern regex = compile("(" + expression.toLowerCase() + ")([_]{1}[A-Z]{2}){0,1}");
						query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, regex);
					} else if (compile("_[A-Z]{2}").matcher(expression).matches()) {
						// search the country part of the locale
						final Pattern regex = compile("([a-z]{2}){0,1}(" + expression.toUpperCase() + ")");
						query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, regex);						
					} else {
						// exact match
						query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression);
					}
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

	private LeishmaniaSample parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getLeishmaniaSample();
	}

	private LeishmaniaSample parseBasicDBObjectOrNull(final BasicDBObject obj) {
		LeishmaniaSample sample = null;
		if (obj != null) {
			final LeishmaniaSampleEntity entity = map(obj);
			if (entity != null) {
				sample = entity.getLeishmaniaSample();
			}
		}
		return sample;
	}

	private LeishmaniaSample parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getLeishmaniaSample();
	}

	private DBObject map(final LinkableTransientStore<LeishmaniaSample> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) parse(JSON_MAPPER.writeValueAsString(new LeishmaniaSampleEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write sample to DB object", e);
		}
		return obj;
	}

	private LeishmaniaSampleEntity map(final BasicDBObject obj) {
		LeishmaniaSampleEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), LeishmaniaSampleEntity.class);
		} catch (IOException e) {
			LOGGER.error("Failed to read sample from DB object", e);
		}
		return entity;
	}

	/**
	 * {@link LeishmaniaSample} entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class LeishmaniaSampleEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private LeishmaniaSample leishmaniaSample;

		public LeishmaniaSampleEntity() { }

		public LeishmaniaSampleEntity(final LeishmaniaSample sample) {
			setLeishmaniaSample(sample);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public LeishmaniaSample getLeishmaniaSample() {
			return leishmaniaSample;
		}

		public void setLeishmaniaSample(final LeishmaniaSample leishmaniaSample) {
			this.leishmaniaSample = leishmaniaSample;
		}

	}

}