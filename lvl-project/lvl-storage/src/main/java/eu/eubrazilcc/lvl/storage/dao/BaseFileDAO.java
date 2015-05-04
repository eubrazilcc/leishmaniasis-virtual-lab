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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.BaseFile;
import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Sorting;

/**
 * Provides methods for handling files stored in the database. All methods provided through this interface provides the following
 * guarantees to callers:
 * <ul>
 * <li>Versioning property: Two or more files sharing the same <tt>namespace</tt> and <tt>filename</tt> are considered versions of 
 *     the same file. A new version will not override the previously existing versions in the database. The most recently uploaded 
 *     version will be always considered the latest version of a file.</li>
 * <li>Isolation property: Each <tt>namespace</tt> is isolated in the database (e.g. using a collection per namespace) and will 
 *     include its own indexes.</li>
 * </ul>
 * @author Erik Torres <ertorser@upv.es>
 * @param <K> - the type of keys used in the database to identify the files
 * @param <E> - the type of files in this DAO
 */
public interface BaseFileDAO<K, E extends BaseFile> {

	/**
	 * Inserts a new file (or version) in the database.
	 * @param namespace - (optional) name space where the file will be stored. When nothing specified, the default namespace is used
	 * @param filename - (optional) filename that will be assigned to the file in the database. When nothing specified, the
	 *                   filename returned by the method {@link File#getName()} will be used
	 * @param srcFile - file to be inserted in the database
	 * @param metadata - (optional) descriptive information about the file
	 * @return an instance of {@link WriteResult} that includes the id assigned to the file in the database and a copy of the file
	 *         information inserted in the database.
	 */
	WriteResult<E> insert(@Nullable String namespace, @Nullable String filename, File srcFile, @Nullable Metadata metadata);	

	/**
	 * Updates the metadata of an existing element in the database. The following metadata elements cannot be updated and their 
	 * user-provided values will be silently ignored by this method:
	 * <ul>
	 * <li>{@link Metadata#getIsLastestVersion() Version}</li>
	 * <li>{@link Metadata#getOpenAccessLink() Open Access Link}</li>
	 * <li>{@link Metadata#getOpenAccessDate() Open Access Date}</li>
	 * </ul> 
	 * @param namespace - (optional) name space of the file to be updated in the database. When nothing specified, the default 
	 *                    namespace is used
	 * @param filename - filename of the file to be updated in the database
	 * @param update - new version of the metadata to be updated in the database
	 * @return a copy of the element updated in the database. When the metadata associated to the element is stored in the database 
	 *         unmodified, this method can return {@code null}.
	 */
	@Nullable E updateMetadata(@Nullable String namespace, String filename, @Nullable Metadata update);

	/**
	 * Creates a secret token that can be used to access the file from an anonymous account (no need to authenticate with the
	 * application to access the file). When several versions of the file exists in the database, the link will be created in the
	 * latest version. <strong>Note</strong> that when a previous version is restored the open access link to that version will be
	 * restored too.
	 * @param namespace - (optional) name space of the file to which the open access link will be created. When nothing specified, 
	 *                    the default namespace is used
	 * @param filename - filename of the file to which the open access link will be created
	 * @return the secret token created in the database that allows anonymous clients to access the file.
	 */
	String createOpenAccessLink(@Nullable String namespace, String filename);

	/**
	 * Removes a previously existing open access link. When several versions of the file exists in the database, the link will be 
	 * removed from all versions.
	 * @param namespace - (optional) name space of the file from which the open access link will be removed. When nothing 
	 *                    specified, the default namespace is used
	 * @param filename - filename of the file from which the open access link will be removed
	 */
	void removeOpenAccessLink(@Nullable String namespace, String filename);	

	/**
	 * Removes a file (and all its versions) from the database.
	 * @param namespace - (optional) name space of the file to be removed from the database. When nothing specified, the default 
	 *                    namespace is used
	 * @param filename - filename of the file to be removed from the database
	 */
	void delete(@Nullable String namespace, String filename);

	/**
	 * Returns all the files from the database under the specified name space. Versions are not included, only the latest uploaded 
	 * file is returned.
	 * @param namespace - (optional) name space of the file to be removed from the database. When nothing specified, the default 
	 *                    namespace is used
	 * @return all the files that are in the database
	 */
	List<E> findAll(@Nullable String namespace);

	/**
	 * Search for a file in the database using the specified name space and filename. When several versions of the file exist in 
	 * the database, the one with the latest upload date will be retrieved. Callers can use the method {@link BaseFile#getOutfile()}
	 * to get the file where the result is to be written.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param filename - filename whose associate file is to be returned
	 * @return the file to which the specified filename is associated in the database under the specified name space, or {@code null} 
	 *         if the database contains no entry for the filename.
	 */
	E find(@Nullable String namespace, String filename);

	/**
	 * Gets a file from the database using the open access link.
	 * @param secret - the open access link
	 * @return the file to which the specified open access link is associated in the database, or {@code null} if the database 
	 *         contains no entry for the secret.
	 */
	@Nullable E findOpenAccess(String secret);

	/**
	 * Returns a view of the files in the database that contains the specified range. The files are sorted by the key in ascending 
	 * order. An optional filter can be specified to filter the database response. However, if the filter is invalid, an empty list 
	 * will be returned to the caller. Optionally, the number of files found in the database is returned to the caller. Versions are 
	 * not included, only the latest uploaded file is returned.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param start - starting index
	 * @param size - maximum number of files returned
	 * @param filter - (optional) the expression to be used to filter the collection
	 * @param sorting - (optional) sorting order
	 * @param count - (optional) is updated with the number of files in the database
	 * @return a view of the files in the database that contains the specified range
	 */
	List<E> list(@Nullable String namespace, int start, int size, @Nullable ImmutableMap<String, String> filter, @Nullable Sorting sorting, 
			@Nullable MutableLong count);

	/**
	 * Returns a view of the available versions of a file in the specified <tt>namespace</tt>.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param filename - filename whose associate versions are to be returned
	 * @param start - starting index
	 * @param size - maximum number of files returned
	 * @param filter - (optional) the expression to be used to filter the collection
	 * @param sorting - (optional) sorting order
	 * @param count - (optional) is updated with the number of files in the database
	 * @return
	 */
	List<E> listVersions(@Nullable String namespace, String filename, int start, int size, @Nullable ImmutableMap<String, String> filter,
			@Nullable Sorting sorting, @Nullable MutableLong count);

	/**
	 * Returns a view of the files in the database that contains the specified range and have an open access link associated to the
	 * file. An optional filter can be specified to filter the database response. However, if the filter is invalid, an empty list 
	 * will be returned to the caller. Optionally, the number of files found in the database is returned to the caller. Versions are 
	 * not included, only the latest uploaded file is returned.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param start - starting index
	 * @param size - maximum number of files returned
	 * @param filter - (optional) the expression to be used to filter the collection
	 * @param sorting - (optional) sorting order
	 * @param count - (optional) is updated with the number of files in the database
	 * @return a view of the files in the database that contains the specified range and have an open access link associated to the file.
	 */
	List<E> listOpenAccess(@Nullable String namespace, int start, int size, @Nullable ImmutableMap<String, String> filter, 
			@Nullable Sorting sorting, @Nullable MutableLong count);

	/**
	 * Checks whether or not the specified file exists in the database, returning <tt>true</tt> only when the file exists in the 
	 * specified namespace.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param filename - filename whose associate file is to be searched in the database
	 * @return <tt>true</tt> when the file exists in the specified namespace. Otherwise, <tt>false</tt>.
	 */
	boolean fileExists(@Nullable String namespace, String filename);

	/**
	 * Deletes the latest version from the database. When a previous version exists for the file in the specified namespace, the latest
	 * uploaded version will be the new latest version.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param filename - filename whose associate file is to be searched in the database
	 */
	void undoLatestVersion(@Nullable String namespace, String filename);
	
	/**
	 * Optional operation that search the filename for the specified query, returning a list of items that match the query.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @param query - the query to match
	 * @param size - maximum number of elements returned
	 * @return the fields that matches the query.
	 */
	List<String> typeahead(@Nullable String namespace, String query, int size);

	/**
	 * Returns the number of files in the database under the specified name space. All the files stored in the database (including versions) 
	 * will be counted.
	 * @param namespace - (optional) name space to be searched in the database. When nothing specified, the default namespace is used
	 * @return the total number of files in the database (including versions)
	 */
	long count(@Nullable String namespace);

	/**
	 * Writes statistics about the files to the specified output stream.
	 * @param namespace - name space to be searched in the database
	 * @param os - the output stream to write the statistics to
	 */
	void stats(@Nullable String namespace, OutputStream os) throws IOException;

}