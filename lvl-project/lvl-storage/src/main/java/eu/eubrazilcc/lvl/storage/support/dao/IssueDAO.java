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
import eu.eubrazilcc.lvl.core.support.Issue;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link Issue} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum IssueDAO implements BaseDAO<String, Issue> {

	ISSUE_DAO;

	private final static Logger LOGGER = getLogger(IssueDAO.class);

	public static final String COLLECTION  = "issues";
	public static final String DB_PREFIX   = "issue.";
	public static final String PRIMARY_KEY = DB_PREFIX + "id";
	public static final String OPENED_KEY  = DB_PREFIX + "opened";
	public static final String STATUS_KEY  = DB_PREFIX + "status";
	public static final String OWNER_KEY   = DB_PREFIX + "owner";
	public static final String CLOSED_KEY  = DB_PREFIX + "closed";

	private IssueDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(OPENED_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(STATUS_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(OWNER_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(CLOSED_KEY, COLLECTION, false);
	}

	@Override
	public WriteResult<Issue> insert(final Issue issue) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<Issue> store = startStore(issue);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<Issue>().id(id).build();
	}

	@Override
	public WriteResult<Issue> insert(final Issue issue, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public Issue update(final Issue issue) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<Issue> store = startStore(issue);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(issue.getId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String pmid) {
		MONGODB_CONN.remove(key(pmid), COLLECTION);
	}

	@Override
	public List<Issue> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null, null);
	}

	@Override
	public Issue find(final String pmid) {
		final BasicDBObject obj = MONGODB_CONN.get(key(pmid), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<Issue> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable ImmutableMap<String, Boolean> projection, final @Nullable MutableLong count) {		
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, toProjection(projection), count), new Function<BasicDBObject, Issue>() {
			@Override
			public Issue apply(final BasicDBObject obj) {
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
	public List<Issue> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<Issue> geoWithin(final Polygon polygon) {
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
		return new BasicDBObject(OPENED_KEY, 1);
	}

	private Issue parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getIssue();
	}

	private Issue parseBasicDBObjectOrNull(final BasicDBObject obj) {
		Issue issue = null;
		if (obj != null) {
			final IssueEntity entity = map(obj);
			if (entity != null) {
				issue = entity.getIssue();
			}
		}
		return issue;
	}

	@SuppressWarnings("unused")
	private String entity2ObjectId(final BasicDBObject obj) {
		return map(obj).getId().toString();
	}

	@SuppressWarnings("unused")
	private Issue parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		return map((BasicDBObject) obj2.get("obj")).getIssue();
	}

	private DBObject map(final LinkableTransientStore<Issue> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new IssueEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write issue to DB object", e);
		}
		return obj;
	}

	private IssueEntity map(final BasicDBObject obj) {
		IssueEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), IssueEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read issue from DB object", e);
		}
		return entity;
	}	

	/**
	 * Issue entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */	
	public static class IssueEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private Issue issue;

		public IssueEntity() { }

		public IssueEntity(final Issue issue) {
			setIssue(issue);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public Issue getIssue() {
			return issue;
		}

		public void setIssue(final Issue issue) {
			this.issue = issue;
		}

	}	

}