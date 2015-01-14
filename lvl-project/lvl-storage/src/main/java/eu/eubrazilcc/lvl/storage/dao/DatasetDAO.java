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
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static com.google.common.collect.Lists.transform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.BaseFile.Metadata;
import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Dataset.DatasetMetadata;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.storage.InvalidSortParseException;

/**
 * {@link Dataset} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum DatasetDAO implements FileBaseDAO<String, Dataset> {

	DATASET_DAO;

	private final static Logger LOGGER = getLogger(DatasetDAO.class);

	public static final String COLLECTION  = "files";
	public static final String DEFAULT_NAMESPACE  = "fs";

	private DatasetDAO() {
	}

	@Override
	public WriteResult<Dataset> insert(final @Nullable String namespace, final File file, final @Nullable Metadata metadata) {
		final String id = MONGODB_CONN.saveFile(file.getName(), namespace, file, fromMetadata(metadata));
		return new WriteResult.Builder<Dataset>().id(id).build();		
	}	

	@Override
	public void delete(final @Nullable String namespace, final String filename) {
		MONGODB_CONN.removeFile(filename, namespace);
	}

	@Override
	public List<Dataset> findAll(final @Nullable String namespace) {
		return list(namespace, 0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public Dataset find(final @Nullable String namespace, final String filename, final File outfile) {
		try {
			final File parentDir = outfile.getParentFile();
			if (parentDir.exists() || parentDir.mkdirs());
			if ((outfile.isFile() && outfile.canWrite()) || outfile.createNewFile());
			return parseGridFSDBFileOrNull(MONGODB_CONN.readFile(filename, namespace), outfile, namespace);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read file", e);
		}		
	}

	@Override
	public List<Dataset> list(final @Nullable String namespace, final int start, final int size, final ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		// parse the sorting information or return an empty list if the sort is invalid
		BasicDBObject sort = null;
		try {
			sort = sortCriteria(sorting, namespace);
		} catch (InvalidSortParseException e) {
			LOGGER.warn("Discarding operation after an invalid sort was found: " + e.getMessage());
			return newArrayList();
		}
		// execute the query in the database using the user to filter the results in case that a valid one is provided
		return transform(MONGODB_CONN.listFiles(namespace, sort, start, size, count), new Function<GridFSDBFile, Dataset>() {
			@Override
			public Dataset apply(final GridFSDBFile gfsFile) {
				return toDataset(gfsFile, namespace);
			}
		});
	}

	@Override
	public long count(final @Nullable String namespace) {
		return MONGODB_CONN.count((isNotBlank(namespace) ? namespace.trim() : DEFAULT_NAMESPACE) + "." + COLLECTION);
	}

	@Override
	public void stats(final @Nullable String namespace, final OutputStream os) throws IOException {
		MONGODB_CONN.stats(os, (isNotBlank(namespace) ? namespace.trim() : DEFAULT_NAMESPACE) + "." + COLLECTION);
	}

	private Dataset parseGridFSDBFileOrNull(final GridFSDBFile gfsFile, final File outfile, final String namespace) throws IOException {
		Dataset dataset = null;
		if (gfsFile != null) {
			dataset = toDataset(gfsFile, namespace);
			gfsFile.writeTo(outfile); // TODO : implement cache here!
		}		
		return dataset;
	}

	private Dataset toDataset(final GridFSDBFile gfsFile, final String namespace) {
		return Dataset.builder()
				.length(gfsFile.getLength())
				.chunkSize(gfsFile.getChunkSize())
				.uploadDate(gfsFile.getUploadDate())
				.md5(gfsFile.getMD5())
				.filename(gfsFile.getFilename())
				.contentType(gfsFile.getContentType())
				.aliases(gfsFile.getAliases())
				.metadata(toMetadata(gfsFile.getMetaData()))
				.namespace(namespace)
				.build();		
	}

	private DBObject fromMetadata(final Metadata metadata) {
		DBObject obj = null;
		if (metadata != null) {
			try {
				obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(metadata));
			} catch (JsonProcessingException e) {
				LOGGER.error("Failed to write dataset to DB object", e);
			}
		}
		return obj;		
	}

	private DatasetMetadata toMetadata(final DBObject obj) {
		DatasetMetadata metadata = null;
		if (obj != null) {
			try {
				metadata = JSON_MAPPER.readValue(obj.toString(), DatasetMetadata.class);		
			} catch (IOException e) {
				LOGGER.error("Failed to read dataset from DB object", e);
			}
		}
		return metadata;
	}

	private BasicDBObject sortCriteria(final @Nullable Sorting sorting, final @Nullable String namespace) throws InvalidSortParseException {
		if (sorting != null) {			
			String field = null;
			// sortable fields
			if ("length".equalsIgnoreCase(sorting.getField())) {
				field = "length";				
			} else if ("uploadDate".equalsIgnoreCase(sorting.getField())) {
				field = "uploadDate";
			} else if ("filename".equalsIgnoreCase(sorting.getField())) {
				field = "filename";
			} else if ("contentType".equalsIgnoreCase(sorting.getField())) {
				field = "contentType";
			}
			if (isNotBlank(field)) {
				int order = 1;
				switch (sorting.getOrder()) {
				case ASC:
					order = 1;
					break;
				case DESC:
					order = -1;
					break;
				default:
					order = 1;
					break;
				}
				return new BasicDBObject(field, order);
			} else {				
				throw new InvalidSortParseException(sorting.getField());					
			}				
		}
		// insertion order
		return new BasicDBObject(ImmutableMap.of("uploadDate", 1));
	}

}