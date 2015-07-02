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

package eu.eubrazilcc.lvl.storage.base;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Maps.newHashMap;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.base.ObjectState.DRAFT;
import static eu.eubrazilcc.lvl.storage.base.ObjectState.OBSOLETE;
import static eu.eubrazilcc.lvl.storage.base.ObjectState.RELEASE;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.storage.Linkable;
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

	public static final String LVL_NAMESPACE_FIELD = "namespace";
	public static final String LVL_GUID_FIELD = "lvlId";
	public static final String LVL_VERSION_FIELD = "version";
	public static final String LVL_LOCATION_FIELD = "location";
	public static final String LVL_STATE_FIELD = "state";
	public static final String LVL_LAST_MODIFIED_FIELD = "lastModified";
	public static final String LVL_SPARSE_IS_ACTIVE_FIELD = "isActive";
	public static final String LVL_DENSE_IS_ACTIVE_FIELD = "isActive2";

	@JsonIgnore
	protected final Logger logger;
	@JsonIgnore
	private final String collection;
	@JsonIgnore
	private final MongoCollectionConfigurer configurer;
	@JsonIgnore
	private String dbId; // database identifier

	private Optional<String> namespace = absent(); // (optional) namespace
	private String lvlId; // globally unique identifier
	private String version; // version identifier

	private Optional<Point> location = absent(); // (optional) geospatial location
	private Optional<Document> provenance = absent(); // (optional) provenance
	private Optional<ObjectState> state = absent(); // (optional) state

	private Date lastModified; // last modification date
	private String isActive; // set to the GUID value in the active version (in most cases, the latest version)
	private String isActive2; // a copy of the field with an alternative index
	private Map<String, List<String>> references; // references to other documents	

	@JsonIgnore
	protected String urlSafeNamespace;
	@JsonIgnore
	protected String urlSafeLvlId;

	@JsonIgnore
	private ObjectStateHandler<LvlObject> stateHandler = new DraftStateHandler<>();

	private static final List<String> FIELDS_TO_SUPPRESS = ImmutableList.<String>of("logger", "collection", "configurer", "urlSafeNamespace", 
			"urlSafeLvlId", "stateHandler");

	public LvlObject(final String collection, final MongoCollectionConfigurer configurer, final Logger logger) {
		this.collection = collection;
		this.configurer = configurer;
		this.logger = logger;
		this.references = newHashMap();
	}

	public String getCollection() {
		return collection;
	}

	public MongoCollectionConfigurer getConfigurer() {
		return configurer;
	}

	public String getDbId() {
		return dbId;
	}

	public void setDbId(final String dbId) {
		this.dbId = dbId;
	}

	public @Nullable String getNamespace() {
		return namespace.orNull();
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

	public String getVersion() {
		return version;
	}

	public void setVersion(final String version) {
		this.version = trimToEmpty(version);
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

	public ObjectState getState() {
		return state.orNull();
	}

	public void setState(final @Nullable ObjectState state) {
		this.state = fromNullable(state);
		switch (this.state.or(DRAFT)) {
		case RELEASE:			
			stateHandler = new ReleaseStateHandler<>();
			break;
		case OBSOLETE:
			stateHandler = new ObsoleteStateHandler<>();
			break;
		case DRAFT:
		default:
			stateHandler = new DraftStateHandler<>();
			break;
		}
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(final Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(final String isActive) {
		this.isActive = isActive;
	}
	
	public String getIsActive2() {
		return isActive2;
	}

	public void setIsActive2(final String isActive2) {
		this.isActive2 = isActive2;
	}

	public Map<String, List<String>> getReferences() {
		return references;
	}

	public void setReferences(final Map<String, List<String>> references) {
		this.references = references;
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
	public ListenableFuture<Void> save(final SaveOptions... options) {
		return stateHandler.save(this, options);
	}

	/**
	 * Gets from the database the element that has the key that coincides with this object.
	 * @param options - operation options
	 * @return a future.
	 */
	public ListenableFuture<Void> fetch(final FetchOptions... options) {
		return stateHandler.fetch(this, options);
	}

	/**
	 * Removes the element from the database.
	 * @param options - operation options
	 * @return a future.
	 */
	public ListenableFuture<Boolean> delete(final DeleteOptions... options) {
		return stateHandler.delete(this, options);
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
		return Objects.equals(namespace, other.namespace)
				&& Objects.equals(lvlId, other.lvlId)
				&& Objects.equals(version, other.version)
				&& Objects.equals(location.orNull(), other.location.orNull())
				&& Objects.equals(provenance.orNull(), other.provenance.orNull())
				&& Objects.equals(state.orNull(), other.state.orNull())
				&& Objects.equals(lastModified, other.lastModified)
				&& Objects.equals(isActive, other.isActive)
				&& Objects.equals(isActive2, other.isActive2)
				&& Objects.equals(references, other.references);
	}

	@Override
	public int hashCode() {
		return Objects.hash(dbId, namespace, lvlId, version, location, provenance, state, lastModified, isActive, isActive2, references);
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("dbId", dbId)
				.add("namespace", namespace.orNull())
				.add("lvlId", lvlId)
				.add("version", version)
				.add("location", location.orNull())
				.add("provenance", "<<not displayed>>")
				.add("state", state.or(DRAFT))
				.add("lastModified", lastModified)
				.add("isActive", isActive)
				.add("isActive2", isActive2)
				.add("references", references)
				.toString();
	}

	/* Utility methods */

	public static void copyProperties(final LvlObject orig, final LvlObject dest) throws IllegalAccessException, InvocationTargetException {
		final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
		propertyUtilsBean.addBeanIntrospector(new SuppressPropertiesBeanIntrospector(FIELDS_TO_SUPPRESS));				
		final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean(), propertyUtilsBean);
		beanUtilsBean.copyProperties(dest, orig);			
	}

	public static String randomVersion() {
		return random(8, true, true);
	}

	/* Fluent API */

	/**
	 * Sets the status to {@link ObjectState.RELEASE}.
	 * @return a reference to this class.
	 */
	public LvlObject approve() {
		this.setState(RELEASE);
		return this;
	}

	/**
	 * Sets the status to {@link ObjectState.OBSOLETE}.
	 * @return a reference to this class.
	 */
	public LvlObject invalidate() {
		this.setState(OBSOLETE);
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

		public B state(final @Nullable ObjectState state) {
			instance.setState(state);
			return builder;
		}

	}

}