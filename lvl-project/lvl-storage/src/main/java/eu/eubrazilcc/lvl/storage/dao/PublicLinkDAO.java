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

import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
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

import eu.eubrazilcc.lvl.core.PublicLink;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.transform.LinkableTransientStore;

/**
 * {@link PublicLink} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum PublicLinkDAO implements BaseDAO<String, PublicLink> {

	PUBLIC_LINK_DAO;

	private final static Logger LOGGER = getLogger(PublicLinkDAO.class);

	public static final String COLLECTION  = "public_links";
	public static final String DB_PREFIX   = "publicLink.";
	public static final String PRIMARY_KEY = DB_PREFIX + "path";
	public static final String OWNER_KEY   = DB_PREFIX + "owner";

	private PublicLinkDAO() {
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		MONGODB_CONN.createNonUniqueIndex(OWNER_KEY, COLLECTION, false);
	}

	@Override
	public WriteResult<PublicLink> insert(final PublicLink publicLink) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<PublicLink> store = startStore(publicLink);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<PublicLink>().id(id).build();
	}

	@Override
	public WriteResult<PublicLink> insert(final PublicLink publicLink, final boolean ignoreDuplicates) {
		throw new UnsupportedOperationException("Inserting ignoring duplicates is not currently supported in this class");
	}

	@Override
	public PublicLink update(final PublicLink publicLink) {
		// remove transient fields from the element before saving it to the database
		final LinkableTransientStore<PublicLink> store = startStore(publicLink);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(publicLink.getPath()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String path) {
		MONGODB_CONN.remove(key(path), COLLECTION);
	}

	@Override
	public List<PublicLink> findAll() {
		return list(0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public PublicLink find(final String path) {
		final BasicDBObject obj = MONGODB_CONN.get(key(path), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<PublicLink> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, count), new Function<BasicDBObject, PublicLink>() {
			@Override
			public PublicLink apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<PublicLink> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<PublicLink> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String key) {
		return new BasicDBObject(PRIMARY_KEY, key);		
	}

	private BasicDBObject ownerKey(final String key) {
		return new BasicDBObject(OWNER_KEY, key);		
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(PRIMARY_KEY, 1);
	}

	private PublicLink parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getPublicLink();
	}

	private PublicLink parseBasicDBObjectOrNull(final BasicDBObject obj) {
		PublicLink publicLink = null;
		if (obj != null) {
			final PublicLinkEntity entity = map(obj);
			if (entity != null) {
				publicLink = entity.getPublicLink();
			}
		}
		return publicLink;
	}

	private DBObject map(final LinkableTransientStore<PublicLink> store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new PublicLinkEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write authN code to DB object", e);
		}
		return obj;
	}

	private PublicLinkEntity map(final BasicDBObject obj) {
		PublicLinkEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), PublicLinkEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read public link from DB object", e);
		}
		return entity;
	}

	public List<PublicLink> listByOwner(final String owner) {
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, 0, Integer.MAX_VALUE, ownerKey(owner), null), new Function<BasicDBObject, PublicLink>() {
			@Override
			public PublicLink apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
	}	

	/**
	 * {@link PublicLink} entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class PublicLinkEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private PublicLink publicLink;

		public PublicLinkEntity() { }

		public PublicLinkEntity(final PublicLink publicLink) {
			setPublicLink(publicLink);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public PublicLink getPublicLink() {
			return publicLink;
		}

		public void setPublicLink(final PublicLink publicLink) {
			this.publicLink = publicLink;
		}

	}

}