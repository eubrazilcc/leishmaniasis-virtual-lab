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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;
import org.openprovenance.prov.model.Document;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions;

/**
 * Classes should extend this class to support common features of the LeishVL, such as geolocalization (an optional GeoJSON point can be
 * included to georeference the object) and provenance (implementing W3C PROV). In addition, a database identifier is provided, as well 
 * as a global LeishVL identifier, unique for all instances of this application.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON open standard format for encoding geographic data structures</a>
 * @see <a href="http://www.w3.org/TR/prov-overview/">PROV-Overview: An Overview of the PROV Family of Documents</a>
 */
public abstract class LvlObject implements Linkable {

	@JsonIgnore
	protected final Logger logger;
	@JsonIgnore
	private final String collection;
	@JsonIgnore
	private final MongoCollectionConfigurer configurer;
	@JsonIgnore
	private final String primaryKey;
	@JsonIgnore
	private String dbId; // database identifier	

	private Optional<String> namespace = absent(); // (optional) namespace
	private String lvlId; // globally unique identifier

	private Optional<Point> location = absent(); // (optional) geospatial location
	private Optional<Document> provenance = absent(); // (optional) provenance
	private Optional<LvlObjectStatus> status = absent(); // (optional) status

	private static final List<String> PROPS_TO_SUPPRESS = ImmutableList.<String>of("logger", "collection", "configurer", "primaryKey", 
			"urlSafeNamespace", "urlSafeLvlId");

	@JsonIgnore
	protected String urlSafeNamespace;
	@JsonIgnore
	protected String urlSafeLvlId;

	public LvlObject(final String collection, final MongoCollectionConfigurer configurer, final String primaryKey, final Logger logger) {
		this.collection = collection;
		this.configurer = configurer;
		this.primaryKey = primaryKey;
		this.logger = logger;
	}

	public String getCollection() {
		return collection;
	}

	public MongoCollectionConfigurer getConfigurer() {
		return configurer;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public String getDbId() {
		return dbId;
	}

	public void setDbId(final String dbId) {
		this.dbId = dbId;
	}

	public String getNamespace() {
		return namespace.or("");
	}

	public void setNamespace(final @Nullable String namespace) {
		this.namespace = fromNullable(trimToNull(namespace));
		setUrlSafeNamespace(urlEncodeUtf8(this.namespace.or("")));
	}

	public String getLvlId() {
		return lvlId;
	}

	public void setLvlId(final String lvlId) {
		this.lvlId = trimToEmpty(lvlId);
		setUrlSafeLvlId(urlEncodeUtf8(this.lvlId));
	}

	public @Nullable Point getLocation() {
		return location.orNull();
	}

	public void setLocation(final @Nullable Point location) {
		this.location = fromNullable(location);
	}

	public @Nullable Document getProvenance() {
		return provenance.orNull();
	}

	public void setProvenance(final @Nullable Document provenance) {
		this.provenance = fromNullable(provenance);
	}	

	public @Nullable LvlObjectStatus getStatus() {
		return status.orNull();
	}

	public void setStatus(final @Nullable LvlObjectStatus status) {
		this.status = fromNullable(status);
	}

	public String getUrlSafeNamespace() {
		return urlSafeNamespace;
	}

	public void setUrlSafeNamespace(final String urlSafeNamespace) {
		this.urlSafeNamespace = urlSafeNamespace;
	}

	public String getUrlSafeLvlId() {
		return urlSafeLvlId;
	}

	public void setUrlSafeLvlId(final String urlSafeLvlId) {
		this.urlSafeLvlId = urlSafeLvlId;
	}

	/**
	 * Inserts or update an element in the database.
	 * @param options - operation options
	 * @return a future.
	 */
	public ListenableFuture<String> save(final SaveOptions... options) {
		if (options != null && options.length > 0) {
			final List<SaveOptions> optList = asList(options);
			// update an existing record
			if (!optList.contains(SaveOptions.FAIL_IF_EXISTS) && optList.contains(SaveOptions.FORCE_TO_OVERRIDE)) {				
				final LvlObject __obj = this;
				final SettableFuture<String> saveFuture = SettableFuture.create();
				final ListenableFuture<Boolean> updateFuture = update();
				addCallback(updateFuture, new FutureCallback<Boolean>() {
					@Override
					public void onSuccess(final Boolean result) {
						saveFuture.set(__obj.getDbId());					
					}
					@Override
					public void onFailure(final Throwable t) {
						saveFuture.setException(t);
					}				
				});
				return transform(updateFuture, new AsyncFunction<Boolean, String>() {
					@Override
					public ListenableFuture<String> apply(final Boolean input) throws Exception {				
						return saveFuture;
					}
				});
			}
		}
		// insert new record
		return MONGODB_CONN.insert(this);
	}

	/**
	 * Updates an existing record in the database.
	 * @return a future.
	 */
	public ListenableFuture<Boolean> update() {		
		return MONGODB_CONN.update(this);		
	}

	/**
	 * Gets from the database the element that has the key that coincides with this object.
	 * @param options - operation options
	 * @return a future.
	 */
	public ListenableFuture<Boolean> fetch(final FetchOptions... options) {
		final LvlObject __obj = this;
		final ListenableFuture<LvlObject> findFuture = MONGODB_CONN.find(this, this.getClass());
		final SettableFuture<Boolean> foundFuture = SettableFuture.create();		
		addCallback(findFuture, new FutureCallback<LvlObject>() {
			@Override
			public void onSuccess(final LvlObject result) {				
				try {
					copyProperties(result, __obj);
					foundFuture.set(true);
				} catch (IllegalAccessException | InvocationTargetException e) {
					foundFuture.setException(e);
				}
			}
			@Override
			public void onFailure(final Throwable t) {				
				foundFuture.setException(t);
			}
		});
		return transform(findFuture, new AsyncFunction<LvlObject, Boolean>() {
			@Override
			public ListenableFuture<Boolean> apply(final LvlObject input) throws Exception {				
				return foundFuture;
			}
		});
	}

	/**
	 * Removes the element from the database.
	 * @param options - operation options
	 * @return a future.
	 */
	public ListenableFuture<Boolean> delete(final DeleteOptions... options) {		
		return MONGODB_CONN.delete(this);
	}

	/**
	 * Returns a String containing the attributes of each element loaded in the current view.
	 * @param options - JSON parser options
	 * @return a String containing the attributes of each element loaded in the current view
	 */
	public String toJson(final MongoJsonOptions... options) {
		String payload = "";		
		try {
			payload = objectToJson(this, options);
		} catch (final JsonProcessingException e) {
			logger.error("Failed to export object to JSON", e);
		}
		return payload;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof LvlObject)) {
			return false;
		}
		final LvlObject other = LvlObject.class.cast(obj);
		return Objects.equals(dbId, other.dbId)
				&& Objects.equals(namespace, other.namespace)
				&& Objects.equals(lvlId, other.lvlId)
				&& Objects.equals(location.orNull(), other.location.orNull())
				&& Objects.equals(provenance.orNull(), other.provenance.orNull())
				&& Objects.equals(status.orNull(), other.status.orNull());	
	}

	@Override
	public int hashCode() {
		return Objects.hash(dbId, namespace, lvlId, location, provenance, status);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("dbId", dbId)
				.add("namespace", namespace.orNull())
				.add("lvlId", lvlId)
				.add("location", location.orNull())
				.add("provenance", "<<not displayed>>")
				.add("status", status.orNull())
				.toString();
	}

	/* Database options */

	public static enum SaveOptions {
		FAIL_IF_EXISTS, // by default, the LeishVL will avoid overriding existing records
		FORCE_TO_OVERRIDE // force the database to override an existing record
	}

	public static enum FetchOptions {
		FIND_BY_OBJECTID // by default, the LeishVL identifier will be use to search the database
	}

	public static enum DeleteOptions {
		DELETE_BY_OBJECTID // by default, the LeishVL identifier will be use to search the database
	}

	/* Available states */

	public static enum LvlObjectStatus {
		DRAFT,   // (create) ->        draft
		RELEASE, //    draft ->   (approval) ->  release
		OBSOLETE //    *any* -> (invalidate) -> obsolete
	}

	/* Utility methods */

	public static void copyProperties(final LvlObject orig, final LvlObject dest) throws IllegalAccessException, InvocationTargetException {
		final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
		propertyUtilsBean.addBeanIntrospector(new SuppressPropertiesBeanIntrospector(PROPS_TO_SUPPRESS));				
		final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean(), propertyUtilsBean);
		beanUtilsBean.copyProperties(dest, orig);			
	}

	/* Fluent API */

	/**
	 * Sets the status to {@link LvlObjectStatus.RELEASE}.
	 * @return a reference to this class.
	 */
	public LvlObject approve() {
		this.setStatus(LvlObjectStatus.RELEASE);
		return this;
	}

	/**
	 * Sets the status to {@link LvlObjectStatus.OBSOLETE}.
	 * @return a reference to this class.
	 */
	public LvlObject invalidate() {
		this.setStatus(LvlObjectStatus.OBSOLETE);
		return this;
	}

	public abstract static class Builder<T extends LvlObject, B extends Builder<T, B>> {

		protected final T instance;
		private B builder;

		public Builder(final T instance) {
			this.instance = instance;
		}

		protected void setBuilder(final B builder) {
			this.builder = builder;
		}		

		public B links(final List<Link> links) {
			instance.setLinks(links);
			return builder;
		}

		public B dbId(final String dbId) {
			instance.setDbId(dbId);			
			return builder;
		}

		public B namespace(final @Nullable String namespace) {
			instance.setNamespace(namespace);
			return builder;
		}

		public B lvlId(final String lvlId) {
			instance.setLvlId(lvlId);
			return builder;
		}

		public B location(final @Nullable Point location) {
			instance.setLocation(location);
			return builder;
		}

		public B provenance(final @Nullable Document provenance) {
			instance.setProvenance(provenance);
			return builder;
		}

		public B status(final @Nullable LvlObjectStatus status) {
			instance.setStatus(status);
			return builder;
		}

	}

}