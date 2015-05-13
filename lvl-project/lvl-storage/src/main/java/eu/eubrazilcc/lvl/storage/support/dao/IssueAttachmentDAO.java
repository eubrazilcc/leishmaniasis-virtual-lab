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

package eu.eubrazilcc.lvl.storage.support.dao;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.util.JSON;

import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.support.IssueAttachment;
import eu.eubrazilcc.lvl.storage.InvalidSortParseException;
import eu.eubrazilcc.lvl.storage.dao.BaseFileDAO;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;
import eu.eubrazilcc.lvl.storage.mongodb.cache.CachedFile;
import eu.eubrazilcc.lvl.storage.mongodb.cache.FilePersistingCache;

/**
 * {@link IssueAttachment} DAO.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum IssueAttachmentDAO implements BaseFileDAO<String, IssueAttachment> {

	ISSUE_ATTACHMENT_DAO;

	private final static Logger LOGGER = getLogger(IssueAttachmentDAO.class);

	private final FilePersistingCache persistingCache = new FilePersistingCache();

	public static final String NAMESPACE = "lvl_issues_attachments";

	@Override
	public WriteResult<IssueAttachment> insert(final @Nullable String namespace, final @Nullable String filename, final File file, final @Nullable Metadata metadata) {
		final String id = MONGODB_CONN.saveFile(NAMESPACE, getFilename(filename, file), file, fromMetadata(metadata));
		return new WriteResult.Builder<IssueAttachment>().id(id).build();
	}

	@Override
	public @Nullable IssueAttachment updateMetadata(final @Nullable String namespace, final String filename, final @Nullable Metadata update) {
		MONGODB_CONN.updateMetadata(NAMESPACE, filename, fromMetadata(update));
		return null;
	}

	@Override
	public String createOpenAccessLink(final @Nullable String namespace, final String filename) {
		throw new UnsupportedOperationException("Open access links are not currently supported in this class");
	}

	@Override
	public void removeOpenAccessLink(final @Nullable String namespace, final String filename) {
		throw new UnsupportedOperationException("Open access links are not currently supported in this class");
	}

	@Override
	public void delete(final @Nullable String namespace, final String filename) {
		MONGODB_CONN.removeFile(NAMESPACE, filename);
	}

	@Override
	public List<IssueAttachment> findAll(final @Nullable String namespace) {
		return list(NAMESPACE, 0, Integer.MAX_VALUE, null, null, null);
	}

	@Override
	public IssueAttachment find(final @Nullable String namespace, final String filename) {
		try {
			return parseGridFSDBFileOrNull(MONGODB_CONN.readFile(NAMESPACE, filename));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read file", e);
		}		
	}

	@Override
	public @Nullable IssueAttachment findOpenAccess(final String secret) {
		throw new UnsupportedOperationException("Open access links are not currently supported in this class");
	}

	@Override
	public List<IssueAttachment> list(final @Nullable String namespace, final int start, final int size, final ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		// parse the sorting information or return an empty list if the sort is invalid
		BasicDBObject sort = null;
		try {
			sort = sortCriteria(sorting);
		} catch (InvalidSortParseException e) {
			LOGGER.warn("Discarding operation after an invalid sort was found: " + e.getMessage());
			return newArrayList();
		}
		// execute the query in the database using the user to filter the results in case that a valid one is provided
		return transform(MONGODB_CONN.listFiles(NAMESPACE, sort, start, size, count), new Function<GridFSDBFile, IssueAttachment>() {
			@Override
			public IssueAttachment apply(final GridFSDBFile gfsFile) {
				return toIssueAttachment(gfsFile);
			}
		});
	}

	@Override
	public List<IssueAttachment> listVersions(final @Nullable String namespace, final String filename, final int start, final int size, 
			final @Nullable ImmutableMap<String, String> filter, final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		// parse the sorting information or return an empty list if the sort is invalid
		BasicDBObject sort = null;
		try {
			sort = sortCriteria(sorting);
		} catch (InvalidSortParseException e) {
			LOGGER.warn("Discarding operation after an invalid sort was found: " + e.getMessage());
			return newArrayList();
		}
		// execute the query in the database using the user to filter the results in case that a valid one is provided
		return transform(MONGODB_CONN.listFileVersions(NAMESPACE, filename, sort, start, size, count), new Function<GridFSDBFile, IssueAttachment>() {
			@Override
			public IssueAttachment apply(final GridFSDBFile gfsFile) {
				return toIssueAttachment(gfsFile);
			}
		});
	}

	@Override
	public List<IssueAttachment> listOpenAccess(final @Nullable String namespace, final int start, final int size, final ImmutableMap<String, String> filter, 
			final @Nullable Sorting sorting, final @Nullable MutableLong count) {
		throw new UnsupportedOperationException("Open access links are not currently supported in this class");
	}

	@Override
	public boolean fileExists(final @Nullable String namespace, final String filename) {
		return MONGODB_CONN.fileExists(NAMESPACE, filename);
	}

	@Override
	public void undoLatestVersion(final @Nullable String namespace, final String filename) {
		MONGODB_CONN.undoLatestVersion(NAMESPACE, filename);		
	}

	@Override
	public List<String> typeahead(final @Nullable String namespace, final String query, final int size) {
		throw new UnsupportedOperationException("Typeahead searches are not currently supported in this class");
	}

	@Override
	public long count(final @Nullable String namespace) {
		return MONGODB_CONN.countFiles(NAMESPACE);
	}

	@Override
	public void stats(final @Nullable String namespace, final OutputStream os) throws IOException {
		MONGODB_CONN.statsFiles(os, NAMESPACE);
	}

	public void cleanCache() {
		persistingCache.invalidateAll();
	}

	private IssueAttachment parseGridFSDBFileOrNull(final GridFSDBFile gfsFile) throws IOException {
		IssueAttachment attachment = null;
		if (gfsFile != null) {
			attachment = toIssueAttachment(gfsFile);
			CachedFile cachedFile = persistingCache.getIfPresent(NAMESPACE, gfsFile.getFilename());
			if (cachedFile != null) {
				if (!cachedFile.getMd5().equals(gfsFile.getMD5())) {
					cachedFile = persistingCache.update(NAMESPACE, gfsFile);
				}
			} else {
				cachedFile = persistingCache.put(NAMESPACE, gfsFile);
			}
			attachment.setOutfile(new File(cachedFile.getCachedFilename()));
		}
		return attachment;
	}

	private IssueAttachment toIssueAttachment(final GridFSDBFile gfsFile) {		
		return IssueAttachment.builder()
				.id(ObjectId.class.cast(gfsFile.getId()).toString())
				.length(gfsFile.getLength())
				.chunkSize(gfsFile.getChunkSize())
				.uploadDate(gfsFile.getUploadDate())
				.md5(gfsFile.getMD5())
				.filename(gfsFile.getFilename())
				.contentType(gfsFile.getContentType())
				.aliases(gfsFile.getAliases())
				.metadata(toMetadata(gfsFile.getMetaData()))
				.build();
	}

	private DBObject fromMetadata(final Metadata metadata) {
		DBObject obj = null;
		if (metadata != null) {
			try {
				obj = (DBObject) JSON.parse(JSON_MAPPER.writeValueAsString(metadata));
			} catch (JsonProcessingException e) {
				LOGGER.error("Failed to write attachment to DB object", e);
			}
		}
		return obj;		
	}

	private Metadata toMetadata(final DBObject obj) {
		Metadata metadata = null;
		if (obj != null) {
			try {
				metadata = JSON_MAPPER.readValue(obj.toString(), Metadata.class);		
			} catch (IOException e) {
				LOGGER.error("Failed to read attachment from DB object", e);
			}
		}
		return metadata;
	}

	private BasicDBObject sortCriteria(final @Nullable Sorting sorting) throws InvalidSortParseException {
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

	private static String getFilename(final @Nullable String filename, final File file) {
		return isNotBlank(filename) ? getName(filename) : file.getName();
	}

}