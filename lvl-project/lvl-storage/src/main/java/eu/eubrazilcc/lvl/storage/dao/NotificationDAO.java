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

import com.google.common.base.Function;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;
import eu.eubrazilcc.lvl.storage.TransientStore;

/**
 * {@link Notification} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum NotificationDAO implements BaseDAO<String, Notification> {

	NOTIFICATION_DAO;

	public static final String COLLECTION = "notifications";
	public static final String MORPHIA_KEY = "_id";
	public static final String PRIORITY_KEY = "priority";
	public static final String ADDRESSE_KEY = "addressee";
	public static final String ISSUED_AT_KEY = "issuedAt";

	private final Morphia morphia = new Morphia();	

	private NotificationDAO() {
		MONGODB_CONN.createNonUniqueIndex(PRIORITY_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(ADDRESSE_KEY, COLLECTION, false);
		MONGODB_CONN.createNonUniqueIndex(ISSUED_AT_KEY, COLLECTION, false);		
		morphia.map(NotificationEntity.class);
	}

	@Override
	public String insert(final Notification notification) {
		// remove transient fields from the element before saving it to the database
		final NotificationTransientStore store = NotificationTransientStore.start(notification);
		final DBObject obj = morphia.toDBObject(new NotificationEntity(store.purge()));
		final String id = MONGODB_CONN.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return id;
	}

	@Override
	public void update(final Notification notification) {
		// remove transient fields from the element before saving it to the database
		final NotificationTransientStore store = NotificationTransientStore.start(notification);
		final DBObject obj = morphia.toDBObject(new NotificationEntity(store.purge()));
		MONGODB_CONN.update(obj, key(store.getId()), COLLECTION);
		// restore transient fields
		store.restore();
	}

	@Override
	public void delete(final String id) {
		MONGODB_CONN.remove(key(id), COLLECTION);
	}

	@Override
	public List<Notification> findAll() {
		return list(0, Integer.MAX_VALUE, null);
	}

	@Override
	public Notification find(final String id) {
		final BasicDBObject obj = MONGODB_CONN.get(key(id), COLLECTION);		
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<Notification> list(final int start, final int size, final @Nullable MutableLong count) {
		return transform(MONGODB_CONN.list(sortCriteria(), COLLECTION, start, size, count), new Function<BasicDBObject, Notification>() {
			@Override
			public Notification apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MONGODB_CONN.count(COLLECTION);
	}

	@Override
	public List<Notification> getNear(final Point point, final double maxDistance) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public List<Notification> geoWithin(final Polygon polygon) {
		throw new UnsupportedOperationException("Geospatial searches are not currently supported in this class");
	}

	@Override
	public void stats(final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, COLLECTION);
	}

	private BasicDBObject key(final String id) {
		return new BasicDBObject(MORPHIA_KEY, new ObjectId(id));		
	}

	private BasicDBObject sortCriteria() {
		return new BasicDBObject(MORPHIA_KEY, 1);
	}	

	private Notification parseBasicDBObject(final BasicDBObject obj) {
		final NotificationEntity entity = morphia.fromDBObject(NotificationEntity.class, obj);
		final Notification notification = entity.getNotification();
		notification.setId(entity.getId().toHexString());
		return notification;
	}

	private Notification parseBasicDBObjectOrNull(final BasicDBObject obj) {
		Notification notification = null;		
		if (obj != null) {
			final NotificationEntity entity = morphia.fromDBObject(NotificationEntity.class, obj);
			if (entity != null) {
				notification = entity.getNotification();
				notification.setId(entity.getId().toHexString());
			}
		}
		return notification;
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class NotificationTransientStore extends TransientStore<Notification> {

		private String id;

		public NotificationTransientStore(final Notification notification) {
			super(notification);
		}

		public String getId() {
			return id;
		}

		public Notification purge() {
			id = element.getId();
			element.setId(null);
			return element;
		}

		public Notification restore() {
			element.setId(id);
			return element;
		}

		public static NotificationTransientStore start(final Notification notification) {
			return new NotificationTransientStore(notification);
		}

	}

	/**
	 * Notification Morphia entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	@Entity(value=COLLECTION, noClassnameStored=true)
	public static class NotificationEntity {

		@Id
		private ObjectId id;

		@Embedded
		private Notification notification;

		public NotificationEntity() { }

		public NotificationEntity(final Notification notification) {
			setNotification(notification);
		}

		public ObjectId getId() {
			return id;
		}

		public void setId(final ObjectId id) {
			this.id = id;
		}

		public Notification getNotification() {
			return notification;
		}

		public void setNotification(final Notification notification) {
			this.notification = notification;
		}

	}

}