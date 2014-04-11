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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;

import com.google.common.base.Function;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;
import eu.eubrazilcc.lvl.core.http.LinkRelation;
import eu.eubrazilcc.lvl.storage.TransientStore;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector;

/**
 * Sequence DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum SequenceDAO implements BaseDAO<String, Sequence> {

	INSTANCE;

	public static final String COLLECTION = "sequences";
	public static final String PRIMARY_KEY = "sequence.accession";
	public static final String GEOLOCATION_KEY = "sequence.location";

	private final Morphia morphia = new Morphia();

	private URI baseUri = null;

	private SequenceDAO() {
		MongoDBConnector.INSTANCE.createIndex(PRIMARY_KEY, COLLECTION);
		MongoDBConnector.INSTANCE.createGeospatialIndex(GEOLOCATION_KEY, COLLECTION);
		morphia.map(SequenceEntity.class);
	}

	public SequenceDAO baseUri(final URI baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	@Override
	public String insert(final Sequence sequence) {
		// remove transient fields from the element before saving it to the database
		final SequenceTransientStore store = SequenceTransientStore.start(sequence);
		final DBObject obj = morphia.toDBObject(new SequenceEntity(store.purge()));
		final String id = MongoDBConnector.INSTANCE.insert(obj, COLLECTION);
		// restore transient fields
		store.restore();
		return id;
	}

	@Override
	public void update(final Sequence sequence) {
		// remove transient fields from the element before saving it to the database
		final SequenceTransientStore store = SequenceTransientStore.start(sequence);
		final DBObject obj = morphia.toDBObject(new SequenceEntity(store.purge()));
		MongoDBConnector.INSTANCE.update(obj, key(sequence.getAccession()), COLLECTION);
		// restore transient fields
		store.restore();
	}

	@Override
	public void delete(final String accession) {
		MongoDBConnector.INSTANCE.remove(key(accession), COLLECTION);
	}

	@Override
	public List<Sequence> findAll() {
		return list(0, Integer.MAX_VALUE, null);
	}

	@Override
	public Sequence find(final String accession) {
		final BasicDBObject obj = MongoDBConnector.INSTANCE.get(key(accession), COLLECTION);
		return parseBasicDBObjectOrNull(obj);
	}

	@Override
	public List<Sequence> list(final int start, final int size, final @Nullable MutableLong count) {
		return transform(MongoDBConnector.INSTANCE.list(sortCriteria(), COLLECTION, start, size, count), new Function<BasicDBObject, Sequence>() {
			@Override
			public Sequence apply(final BasicDBObject obj) {				
				return parseBasicDBObject(obj);
			}
		});		
	}

	@Override
	public long count() {
		return MongoDBConnector.INSTANCE.count(COLLECTION);
	}

	@Override
	public List<Sequence> getNear(final Point point, final double maxDistance) {
		final List<Sequence> sequences = newArrayList();
		final BasicDBList list = MongoDBConnector.INSTANCE.geoNear(COLLECTION, point.getCoordinates()[0], 
				point.getCoordinates()[1], maxDistance);
		for (int i = 0; i < list.size(); i++) {
			sequences.add(parseObject(list.get(i)));
		}
		return sequences;
	}

	@Override
	public List<Sequence> geoWithin(final Polygon polygon) {
		return transform(MongoDBConnector.INSTANCE.geoWithin(GEOLOCATION_KEY, COLLECTION, polygon), new Function<BasicDBObject, Sequence>() {
			@Override
			public Sequence apply(final BasicDBObject obj) {
				return parseBasicDBObject(obj);
			}
		});
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

	private Sequence parseBasicDBObject(final BasicDBObject obj) {
		final Sequence sequence = morphia.fromDBObject(SequenceEntity.class, obj).getSequence();
		addLink(sequence);
		return sequence;
	}
	
	private Sequence parseBasicDBObjectOrNull(final BasicDBObject obj) {
		Sequence sequence = null;
		final SequenceEntity entity = morphia.fromDBObject(SequenceEntity.class, obj);
		if (entity != null) {
			sequence = morphia.fromDBObject(SequenceEntity.class, obj).getSequence();
			addLink(sequence);
		}
		return sequence;
	}

	private Sequence parseObject(final Object obj) {
		final BasicDBObject obj2 = (BasicDBObject) obj;
		final Sequence sequence = morphia.fromDBObject(SequenceEntity.class, (BasicDBObject) obj2.get("obj")).getSequence();
		addLink(sequence);
		return sequence;
	}	

	private void addLink(final Sequence sequence) {
		if (baseUri != null) {
			sequence.setLink(Link.fromUri(UriBuilder.fromUri(baseUri).path(sequence.getAccession()).build())
					.rel(LinkRelation.SELF).type(MediaType.APPLICATION_JSON).build());
		}
	}

	/**
	 * Extracts from an entity the fields that depends on the service (e.g. links)
	 * before storing the entity in the database. These fields are stored in this
	 * class and can be reinserted later in the entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class SequenceTransientStore extends TransientStore<Sequence> {

		private Link link;

		public SequenceTransientStore(final Sequence sequence) {
			super(sequence);
		}

		public Link getLink() {
			return link;
		}

		public Sequence purge() {
			link = element.getLink();
			element.setLink(null);
			return element;
		}

		public Sequence restore() {
			element.setLink(link);
			return element;
		}

		public static SequenceTransientStore start(final Sequence sequence) {
			return new SequenceTransientStore(sequence);
		}

	}

	/**
	 * Sequence Morphia entity.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	@Entity(value=COLLECTION, noClassnameStored=true)
	@Indexes({@Index(PRIMARY_KEY)})
	public static class SequenceEntity {

		@Id
		private ObjectId id;

		@Embedded
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