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
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.community.Post;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.InvalidFilterParseException;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link Post} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum PostDAO implements BaseDAO<String, Post> {

	POST_DAO;

	private final static Logger LOGGER = getLogger(PostDAO.class);

	public static final String COLLECTION   = "posts";
	public static final String DB_PREFIX    = "post.";
	public static final String PRIMARY_KEY  = DB_PREFIX + "id";
	public static final String CREATED_KEY  = DB_PREFIX + "created";
	public static final String AUTHOR_KEY   = DB_PREFIX + "author";
	public static final String CATEGORY_KEY = DB_PREFIX + "category";
	public static final String LEVEL_KEY    = DB_PREFIX + "level";

	private PostDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(CREATED_KEY, COLLECTION, true);
		MONGODB_CONN.createNonUniqueIndex(AUTHOR_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(CATEGORY_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(LEVEL_KEY, COLLECTION, false);
	}

	@Override
	public WriteResult<Post> insert(final Post post) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<Post> store = startStore(post);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<Post>().id(id).build();
	}

	@Override
	public WriteResult<Post> insert(final Post post, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public Post update(final Post post) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<Post> store = startStore(post);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(post.getId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String pmid) {
		MONGODB_CONN.remove(key(pmid), COLLECTION);
	}

	@Override
	public List<Post> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public Post find(final String pmid) {
		final BasicDBObject obj = MONGODB_CONN.get(key(pmid), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<Post> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {
		// parse the filter or return an empty list if the filter is invalid
		BasicDBObject query = null;
		try {
			query = buildQuery(filter);			
		} catch (InvalidFilterParseException e) {
			LOGGER.warn("Discarding operation after an invalid filter was found: " + e.getMessage());
			return newArrayList();
		}		
		// execute the query in the database (sort criteria is set to default)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, query, toProjection(projection), count), new Function<BasicDBObject, Post>() {
			@Override
			public Post apply(final BasicDBObject obj) {				
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
	
	public long countBefore(final long timestamp) {
		return MONGODB_CONN.count(COLLECTION, new BasicDBObject(CREATED_KEY, new BasicDBObject("$lt", timestamp)));
	}
	
	public long countAfter(final long timestamp) {
		return MONGODB_CONN.count(COLLECTION, new BasicDBObject(CREATED_KEY, new BasicDBObject("$gt", timestamp)));
	}

	@Override
	public List<Post> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<Post> geoWithin(final Polygon polygon) {
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
		return new BasicDBObject(CREATED_KEY, -1);
	}	

	private @Nullable BasicDBObject buildQuery(final @Nullable ImmutableMap<String, String> filter) 
			throws InvalidFilterParseException {
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
			if ("created".equalsIgnoreCase(parameter)) {
				field = CREATED_KEY;
			} else if ("author".equalsIgnoreCase(parameter)) {
				field = AUTHOR_KEY;
			} else if ("category".equalsIgnoreCase(parameter)) {
				field = CATEGORY_KEY;
			} else if ("level".equalsIgnoreCase(parameter)) {
				field = LEVEL_KEY;
			}
			if (isNotBlank(field)) {
				if ("created".equalsIgnoreCase(parameter)) {
					// comparison operator
					query2 = mongoNumeriComparison(field, expression);				
				} else if ("category".equalsIgnoreCase(parameter) || "level".equalsIgnoreCase(parameter)) {
					// convert the expression to upper case and compare for exact matching					
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, expression.startsWith("!") 
							? new BasicDBObject("$ne", expression.substring(1).toUpperCase()) : expression.toUpperCase());
					LOGGER.trace(String.format("Exact match query: ", query2));
				} else {
					// regular expression to match all entries that contains the keyword (use negative lookahead)
					final Pattern regex = compile(expression.startsWith("!") ? String.format("^((?!%s).)*$", expression.substring(1))
							: String.format(".*%s.*", expression), CASE_INSENSITIVE);
					query2 = (query2 != null ? query2 : new BasicDBObject()).append(field, regex);
					LOGGER.trace(String.format("Pattern match query: ", query2));					
				}
			} else {
				throw new InvalidFilterParseException(parameter);
			}
		}		
		return query2;
	}

	private Post parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getPost();
	}

	private Post parseBasicDBObjectOrNull(final BasicDBObject obj) {
		Post post = null;
		if (obj != null) {
			final PostEntity entity = map(obj);
			if (entity != null) {
				post = entity.getPost();
			}
		}
		return post;
	}

	@SuppressWarnings("unused")
	private String entity2ObjectId(final BasicDBObject obj) {
		return map(obj).getId().toString();
	}

	@SuppressWarnings("unused")
	private Post parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getPost();
	}

	private DBObject map(final LinkableTransientStore<Post> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new PostEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write post to DB object", e);
		}
		return obj;
	}

	private PostEntity map(final BasicDBObject obj) {
		PostEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), PostEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read post from DB object", e);
		}
		return entity;
	}	

	/**
	 * Post entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class PostEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private Post post;

		public PostEntity() { }

		public PostEntity(final Post post) {
			setPost(post);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public Post getPost() {
			return post;
		}

		public void setPost(final Post post) {
			this.post = post;
		}

	}	

}