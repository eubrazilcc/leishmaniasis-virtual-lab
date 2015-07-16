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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.util.concurrent.Futures.addCallback;
import static eu.eubrazilcc.lvl.storage.base.SaveOptions.SAVE_OVERRIDING;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.Link;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.SuppressPropertiesBeanIntrospector;
import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import eu.eubrazilcc.lvl.storage.Linkable;
import eu.eubrazilcc.lvl.storage.mongodb.MongoFilesConfigurer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions;

/**
 * Extends the base file definition. The additional property {@link #outfile} can used to store a copy of the object in a file.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class LvlFile extends LvlBaseFile implements Linkable {

	@JsonIgnore
	protected final Logger logger;
	@JsonIgnore
	private final MongoFilesConfigurer configurer;

	/**
	 * Additional property (not part of the GridFS specification) that can be used to store a copy of the object in a file, avoiding
	 * unnecessary read operations.
	 */
	@JsonIgnore
	private Optional<File> outfile = absent();

	private static final List<String> FIELDS_TO_SUPPRESS = ImmutableList.<String>of("logger", "configurer", "urlSafeNamespace", "urlSafeFilename");

	public LvlFile(final MongoFilesConfigurer configurer, final Logger logger) {
		this.configurer = configurer;
		this.logger = logger;
	}

	public MongoFilesConfigurer getConfigurer() {
		return configurer;
	}

	public @Nullable File getOutfile() {
		return outfile.orNull();
	}

	public void setOutfile(final @Nullable File outfile) {
		this.outfile = fromNullable(outfile);
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
		if (obj == null || !(obj instanceof LvlFile)) {
			return false;
		}
		final LvlFile other = LvlFile.class.cast(obj);
		return super.equals((LvlBaseFile)other);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("LvlBaseFile", super.toString())
				.toString();
	}

	public ListenableFuture<Void> save(final File srcFile, final SaveOptions... options) {		
		final List<SaveOptions> optList = (options != null ? asList(options) : Collections.<SaveOptions>emptyList());
		return MONGODB_CONN.fsClient().saveFile(this, srcFile, optList.contains(SAVE_OVERRIDING));
	}

	public ListenableFuture<Void> updateMetadata() {
		return MONGODB_CONN.fsClient().updateMetadata(this);
	}

	public ListenableFuture<Void> createOpenAccessLink() {		
		return MONGODB_CONN.fsClient().createOpenAccessLink(this);
	}

	public ListenableFuture<Void> removeOpenAccessLink() {
		return MONGODB_CONN.fsClient().removeOpenAccessLink(this);
	}

	public ListenableFuture<Void> fetch() {
		final SettableFuture<Void> future = SettableFuture.create();
		final LvlFile __file = this;
		final ListenableFuture<LvlFile> fetchFuture = MONGODB_CONN.fsClient().fetchFile(this, this.getClass());
		addCallback(fetchFuture, new FutureCallback<LvlFile>() {
			@Override
			public void onSuccess(final LvlFile result) {
				try {
					copyProperties(result, __file);					
					future.set(null);
				} catch (IllegalAccessException | InvocationTargetException e) {
					future.setException(e);
				}
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}
		});
		return future;
	}	

	public ListenableFuture<Boolean> exists() {
		return MONGODB_CONN.fsClient().fileExists(this);
	}

	public ListenableFuture<Void> fetchOpenAccess() {
		final SettableFuture<Void> future = SettableFuture.create();
		final LvlFile __file = this;
		final ListenableFuture<LvlFile> fetchFuture = MONGODB_CONN.fsClient().fetchOpenAccessFile(this, this.getClass());
		addCallback(fetchFuture, new FutureCallback<LvlFile>() {
			@Override
			public void onSuccess(final LvlFile result) {
				try {
					copyProperties(result, __file);					
					future.set(null);
				} catch (IllegalAccessException | InvocationTargetException e) {
					future.setException(e);
				}
			}
			@Override
			public void onFailure(final Throwable t) {
				future.setException(t);
			}
		});
		return future;
	}

	public ListenableFuture<Boolean> delete(final DeleteOptions... options) {
		return MONGODB_CONN.fsClient().removeFile(this);
	}

	/* Utility methods */

	public static void copyProperties(final LvlFile orig, final LvlFile dest) throws IllegalAccessException, InvocationTargetException {
		final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
		propertyUtilsBean.addBeanIntrospector(new SuppressPropertiesBeanIntrospector(FIELDS_TO_SUPPRESS));				
		final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(new ConvertUtilsBean(), propertyUtilsBean);
		beanUtilsBean.copyProperties(dest, orig);			
	}

	/* Fluent API */

	public abstract static class Builder<T extends LvlFile, B extends Builder<T, B>> {

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

		public B id(final String id) {
			instance.setId(id);
			return builder;
		}

		public B length(final long length) {
			instance.setLength(length);
			return builder;
		}

		public B chunkSize(final long chunkSize) {
			instance.setChunkSize(chunkSize);
			return builder;
		}

		public B uploadDate(final Date uploadDate) {
			instance.setUploadDate(uploadDate);
			return builder;
		}

		public B md5(final String md5) {
			instance.setMd5(md5);
			return builder;
		}

		public B filename(final String filename) {
			checkArgument(isNotBlank(filename), "Uninitialized or invalid filename");
			instance.setFilename(filename.trim());
			return builder;
		}

		public B contentType(final String contentType) {
			instance.setContentType(contentType);
			return builder;
		}

		public B aliases(final List<String> aliases) {
			instance.setAliases(aliases);
			return builder;
		}

		public B metadata(final Metadata metadata) {
			instance.setMetadata(metadata);			
			return builder;
		}

	}

}