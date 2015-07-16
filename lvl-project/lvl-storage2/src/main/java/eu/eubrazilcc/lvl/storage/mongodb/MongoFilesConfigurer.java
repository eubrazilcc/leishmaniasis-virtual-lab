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

package eu.eubrazilcc.lvl.storage.mongodb;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.indexModel;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.nonUniqueIndexModel;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;
import static java.util.Collections.synchronizedSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Applies configuration to file collections.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MongoFilesConfigurer {

	private static final Logger LOGGER = getLogger(MongoFilesConfigurer.class);

	public static final long TIMEOUT = 5l;

	private Set<String> buckets = synchronizedSet(Sets.<String>newHashSet());

	public void prepareFiles(final String bucket) {
		checkArgument(isNotBlank(bucket), "Uninitialized or invalid bucket");
		final boolean configured = buckets.contains(bucket);
		if (!configured) {
			/* create indexes: 1) enforce isolation property: each bucket has its own index where the filenames are unique;
			 * 2) create index to operate on metadata filename; and 3) create index to operate on open access links. */
			final ListenableFuture<List<String>> future = MONGODB_CONN.client().createIndexes(bucket + ".files", newArrayList(indexModel("filename"), 
					nonUniqueIndexModel("uploadDate", true), nonUniqueIndexModel("metadata.filename", false), 
					nonUniqueIndexModel("metadata.openAccess.secret", false)));
			try {
				future.get(TIMEOUT, SECONDS);
				buckets.add(bucket);
				LOGGER.info("Bucket configured: " + bucket);
			} catch (Exception e) {
				LOGGER.info("Failed to configure bucket: " + bucket, e);
			}
		}
	}	

}