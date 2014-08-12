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

package eu.eubrazilcc.lvl.storage.oauth2.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
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

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.core.util.NamingUtils;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.ObjectIdSerializer;
import eu.eubrazilcc.lvl.storage.oauth2.ClientApp;

/**
 * Client application (registration) DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum ClientAppDAO implements BaseDAO<String, ClientApp> {

	CLIENT_APP_DAO;

	private final static Logger LOGGER = getLogger(ClientAppDAO.class);

	public static final String COLLECTION = "client_apps";
	public static final String PRIMARY_KEY = "clientApp.clientId";

	public static final String LVL_PORTAL_NAME        = "lvl_portal";
	public static final String LVL_PORTAL_SECRET      = "changeit";
	public static final String LVL_PORTAL_DESCRIPTION = "LVL Web portal";

	private ClientAppDAO() {		
		MONGODB_CONN.createIndex(PRIMARY_KEY, COLLECTION);
		// ensure that at least the administrator account exists in the database
		final List<ClientApp> clientApps = list(0, 1, null, null);
		if (clientApps == null || clientApps.isEmpty()) {
			insert(ClientApp.builder()
					.name(LVL_PORTAL_NAME)
					// .url("http://localhost/client")
					.description(LVL_PORTAL_DESCRIPTION)
					// .icon("http://localhost/client/icon")
					// .redirectURL("http://localhost/client/redirect")
					.clientId(NamingUtils.toAsciiSafeName(LVL_PORTAL_NAME))
					.clientSecret(LVL_PORTAL_SECRET)
					.issuedAt(System.currentTimeMillis() / 1000l)
					.expiresIn(Long.MAX_VALUE) // never expires
					.build());					
		}
	}

	@Override
	public WriteResult<ClientApp> insert(final ClientApp clientApp) {
		// remove transient fields from the element before saving it to the database
		final ClientAppTransientStore store = ClientAppTransientStore.start(clientApp);
		final DBObject obj = map(store);
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return new WriteResult.Builder<ClientApp>().id(id).build();
	}

	@Override
	public ClientApp update(final ClientApp clientApp) {
		// remove transient fields from the element before saving it to the database
		final ClientAppTransientStore store = ClientAppTransientStore.start(clientApp);
		final DBObject obj = map(store);
		MONGODB_CONN.update(obj, key(clientApp.getClientId()), COLLECTION);
		// restore transient fields
		store.restore();
		return null;
	}

	@Override
	public void delete(final String clientId) {
		MONGODB_CONN.remove(key(clientId), COLLECTION);
	}

	@Override
	public List<ClientApp> findAll() {
		return list(0, Integer.MAX_VALUE, null, null);
	}

	@Override
	public ClientApp find(final String clientId) {
		final BasicDBObject obj = MONGODB_CONN.get(key(clientId), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<ClientApp> list(final int start, final int size, final @Nullable ImmutableMap<String, String> filter, final @Nullable MutableLong count) {
		// execute the query in the database (unsupported filter)
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, null, count), new Function<BasicDBObject, ClientApp>() {
			@Override
			public ClientApp apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<ClientApp> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<ClientApp> geoWithin(final Polygon polygon) {
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
		return new BasicDBObject(PRIMARY_KEY, 1);
	}

	private ClientApp parseBasicDBObject(final BasicDBObject obj) {
		return map(obj).getClientApp();
	}

	private ClientApp parseBasicDBObjectOrNull(final BasicDBObject obj) {
		ClientApp clientApp = null;
		if (obj != null) {
			final ClientAppEntity entity = map(obj);
			if (entity != null) {
				clientApp = entity.getClientApp();
			}
		}
		return clientApp;
	}

	private DBObject map(final ClientAppTransientStore store) {
		DBObject obj = null;
		try {
			obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(new ClientAppEntity(store.purge())));
		} catch (JsonProcessingException e) {
			LOGGER.error("Failed to write client app to DB object", e);
		}
		return obj;
	}

	private ClientAppEntity map(final BasicDBObject obj) {
		ClientAppEntity entity = null;
		try {
			entity = JSON_MAPPER.readValue(obj.toString(), ClientAppEntity.class);		
		} catch (IOException e) {
			LOGGER.error("Failed to read client app from DB object", e);
		}
		return entity;
	}

	/**
	 * Checks whether or not the specified client Id were previously stored (registered).
	 * @param clientId - the client identifier
	 * @return {@code true} only if the client Id is found in the storage. Otherwise, returns 
	 *         {@code false}.
	 */
	public boolean isValid(final String clientId) {
		checkArgument(isNotBlank(clientId), "Uninitialized or invalid client Id");
		return find(clientId) != null;		
	}

	/**
	 * Checks whether or not the specified client Id and the associated secret were previously 
	 * stored (registered).
	 * @param clientId - the client identifier
	 * @param clientSecret - the secret associated to the client
	 * @return {@code true} only if the client Id and secret are found in the storage. Otherwise, returns 
	 *         {@code false}.
	 */
	public boolean isValid(final String clientId, final String clientSecret) {
		checkArgument(isNotBlank(clientId), "Uninitialized or invalid client Id");
		checkArgument(isNotBlank(clientSecret), "Uninitialized or invalid client secret");
		final ClientApp clientApp = find(clientId);		
		return (clientApp != null && clientSecret.equals(clientApp.getClientSecret()));
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class ClientAppTransientStore extends TransientStore<ClientApp> {

		public ClientAppTransientStore(final ClientApp clientApp) {
			super(clientApp);
		}

		public ClientApp purge() {			
			return element;
		}

		public ClientApp restore() {
			return element;
		}

		public static ClientAppTransientStore start(final ClientApp clientApp) {
			return new ClientAppTransientStore(clientApp);
		}

	}

	/**
	 * Client application entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class ClientAppEntity {

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		@JsonProperty("_id")
		private ObjectId id;

		private ClientApp clientApp;

		public ClientAppEntity() { }

		public ClientAppEntity(final ClientApp clientApp) {
			setClientApp(clientApp);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public ClientApp getClientApp() {
			return clientApp;
		}

		public void setClientApp(final ClientApp clientApp) {
			this.clientApp = clientApp;
		}

	}

}	