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
import eu.eubrazilcc.lvl.core.BaseFile.Metadata;
import eu.eubrazilcc.lvl.core.Sorting;

/**
 * Provides methods for handling files stored in the database.
 * @author Erik Torres <ertorser@upv.es>
 * @param <K> - the type of keys used in the database to identify the files
 * @param <E> - the type of files in this DAO
 */
public interface FileBaseDAO<K, E extends BaseFile> {

	/**
	 * Inserts a new file in the database.
	 * @param namespace - name space where the file will be stored
	 * @param file - file to be inserted in the database
	 * @param metadata - optional metadata
	 * @return an instance of {@link WriteResult} that includes the id assigned to the file in the database and a copy of the file
	 *         information inserted in the database.
	 */
	WriteResult<E> insert(@Nullable String namespace, File file, @Nullable Metadata metadata);

	/**
	 * Removes a file from the database.
	 * @param namespace - name space of the file to be removed from the database
	 * @param filename - filename of the file to be removed from the database
	 */
	void delete(@Nullable String namespace, String filename);

	/**
	 * Returns all the files from the database under the specified name space.
	 * @return all the files that are in the database
	 */
	List<E> findAll(@Nullable String namespace);

	/**
	 * Search for a file in the database using the specified name space and filename.
	 * @param namespace - name space to be searched in the database
	 * @param filename - filename whose associate file is to be returned
	 * @param outfile - file where the result will be written
	 * @return the file to which the specified filename is associated in the database under the specified name space, or {@code null} if the 
	 *         database contains no entry for the filename.
	 */
	E find(@Nullable String namespace, String filename, File outfile);

	/**
	 * Returns a view of the files in the database that contains the specified range. The files are sorted by the key in ascending order.
	 * An optional filter can be specified to filter the database response. However, if the filter is invalid, an empty list will be returned
	 * to the caller. Optionally, the number of files found in the database is returned to the caller.
	 * @param namespace - name space to be searched in the database
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
	 * Returns the number of files in the database under the specified name space.
	 * @param namespace - name space to be searched in the database
	 * @return the number of files in the database
	 */
	long count(@Nullable String namespace);

	/**
	 * Writes statistics about the files to the specified output stream.
	 * @param namespace - name space to be searched in the database
	 * @param os - the output stream to write the statistics to
	 */
	void stats(@Nullable String namespace, OutputStream os) throws IOException;

}