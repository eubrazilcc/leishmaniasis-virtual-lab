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
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import com.google.common.base.Function;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.dao.BaseDAO;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector;
import eu.eubrazilcc.lvl.storage.oauth2.AuthCode;

/**
 * Authentication DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum AuthCodeDAO implements BaseDAO<String, AuthCode> {

	INSTANCE;

	public static final String COLLECTION = "authz_codes";
	public static final String PRIMARY_KEY = "authCode.code";

	private final Morphia morphia = new Morphia();	

	private AuthCodeDAO() {
		MongoDBConnector.INSTANCE.createIndex(PRIMARY_KEY, COLLECTION);
		morphia.map(AuthCodeEntity.class);
	}

	@Override
	public String insert(final AuthCode authCode) {
		// remove transient fields from the element before saving it to the database
		final AuthCodeTransientStore store = AuthCodeTransientStore.start(authCode);
		final DBObject obj = morphia.toDBObject(new AuthCodeEntity(store.purge()));
		final String id = MongoDBConnector.INSTANCE.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return id;
	}

	@Override
	public void update(final AuthCode authCode) {
		// remove transient fields from the element before saving it to the database
		final AuthCodeTransientStore store = AuthCodeTransientStore.start(authCode);
		final DBObject obj = morphia.toDBObject(new AuthCodeEntity(store.purge()));
		MongoDBConnector.INSTANCE.update(obj, key(authCode.getCode()), COLLECTION);
		// restore transient fields
		store.restore();
	}

	@Override
	public void delete(final String code) {
		MongoDBConnector.INSTANCE.remove(key(code), COLLECTION);
	}

	@Override
	public List<AuthCode> findAll() {
		return list(0, Integer.MAX_VALUE, null);
	}

	@Override
	public AuthCode find(final String code) {
		final BasicDBObject obj = MongoDBConnector.INSTANCE.get(key(code), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<AuthCode> list(final int start, final int size, final @Nullable MutableLong count) {
		return transform(MongoDBConnector.INSTANCE.list(sortCriteria(), COLLECTION, start, size, count), new Function<BasicDBObject, AuthCode>() {
			@Override
			public AuthCode apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MongoDBConnector.INSTANCE.count(COLLECTION);
	}

	@Override
	public List<AuthCode> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<AuthCode> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MongoDBConnector.INSTANCE.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String accession) {
		return new BasicDBObject(PRIMARY_KEY, accession);		
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(PRIMARY_KEY, 1);
	}

	private AuthCode parseBasicDBObject(final BasicDBObject obj) {
		return morphia.fromDBObject(AuthCodeEntity.class, obj).getAuthCode();
	}

	private AuthCode parseBasicDBObjectOrNull(final BasicDBObject obj) {
		AuthCode authCode = null;
		if (obj != null) {
			final AuthCodeEntity entity = morphia.fromDBObject(AuthCodeEntity.class, obj);
			if (entity != null) {
				authCode = morphia.fromDBObject(AuthCodeEntity.class, obj).getAuthCode();
			}
		}
		return authCode;
	}

	/**
	 * Checks whether or not the specified secret (access code) was previously stored
	 * and is currently valid (not expired).
	 * @param code - the secret associated to the access code
	 * @return {@code true} only if the provided secret (access code) is found in the
	 *         storage and is currently valid (not expired). Otherwise, returns 
	 *         {@code false}.
	 */
	public boolean isValid(final String code) {		
		checkArgument(isNotBlank(code), "Uninitialized or invalid code");
		final AuthCode authCode = find(code);		
		return (authCode != null && authCode.getCode() != null && code.equals(authCode.getCode())
				&& (authCode.getIssuedAt() + authCode.getExpiresIn()) > (System.currentTimeMillis() / 1000l));
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class AuthCodeTransientStore extends TransientStore<AuthCode> {

		public AuthCodeTransientStore(final AuthCode authCode) {
			super(authCode);
		}

		public AuthCode purge() {
			return element;
		}

		public AuthCode restore() {
			return element;
		}

		public static AuthCodeTransientStore start(final AuthCode authCode) {
			return new AuthCodeTransientStore(authCode);
		}

	}

	/**
	 * AuthCode Morphia entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	@Entity(value=COLLECTION, noClassnameStored=true)
	@Indexes({@Index(PRIMARY_KEY)})
	public static class AuthCodeEntity {

		@Id
		private ObjectId id;

		@Embedded
		private AuthCode authCode;

		public AuthCodeEntity() { }

		public AuthCodeEntity(final AuthCode authCode) {
			setAuthCode(authCode);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public AuthCode getAuthCode() {
			return authCode;
		}

		public void setAuthCode(final AuthCode authCode) {
			this.authCode = authCode;
		}

	}

}