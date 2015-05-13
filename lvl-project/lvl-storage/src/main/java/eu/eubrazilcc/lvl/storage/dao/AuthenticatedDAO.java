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

package eu.eubrazilcc.lvl.storage.dao;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;

/**
 * Base DAO that requires authenticated access.
 * @author Erik Torres <ertorser@upv.es>
 * @param <K> - the type of keys used in the database to identify the elements
 * @param <E> - the type of elements in this DAO
 */
public interface AuthenticatedDAO<K, E> extends BaseDAO<K, E> {

	/**
	 * Returns all the elements from the database whose owner coincides with the specified user.
	 * @param user - caller identity
	 * @return all the elements that are in the database
	 */
	List<E> findAll(String user);

	/**
	 * Search for an element in the database using the specified id and user. In case that the owner of the element associated to the specified
	 * key does not coincide with the specified user, this method fails throwing and exception.
	 * @param key - identifier whose associate value is to be returned
	 * @param user - caller identity
	 * @return the element to which the specified key is associated in the database, or {@code null} if the database contains no entry for the key.
	 */
	E find(K key, String user);

	/**
	 * Returns a view of the elements in the database that contains the specified range and whose owner coincides with the specified user. The 
	 * elements are sorted by the key in ascending order. An optional filter can be specified to filter the database response. However, if the 
	 * filter is invalid, an empty list will be returned to the caller. Optionally, the number of elements found in the database is returned 
	 * to the caller.
	 * @param start - starting index
	 * @param size - maximum number of elements returned
	 * @param filter - (optional) the expression to be used to filter the collection
	 * @param sorting - (optional) sorting order
	 * @param projection - (optional) specifies the fields to return. Set the field name to <tt>true</tt> to include the field, <tt>false</tt>
	 *                     to exclude the field. To return all fields in the matching document, omit this parameter
	 * @param count - (optional) is updated with the number of elements in the database
	 * @param user - caller identity
	 * @return a view of the elements in the database that contains the specified range
	 */
	List<E> list(int start, int size, @Nullable ImmutableMap<String, String> filter, @Nullable Sorting sorting, 
			@Nullable ImmutableMap<String, Boolean> projection, @Nullable MutableLong count, String user);

	/**
	 * Returns the elements in the database that are within the specified distance (in meters) from the center point specified (using WGS84) and 
	 * whose owner coincides with the specified user.
	 * @param point - longitude, latitude pair represented in WGS84 coordinate reference system (CRS)
	 * @param maxDistance - limits the results to those elements that fall within the specified distance (in meters) from the center point
	 * @param user - caller identity
	 * @return the elements that are within the specified distance from the center point
	 */
	List<E> getNear(Point point, double maxDistance, String user);

	/**
	 * Returns the elements in the database that exist entirely within the defined polygon and whose owner coincides with the specified user.
	 * @param polygon - geometric shape with at least four edges
	 * @param user - caller identity
	 * @return the elements that exist entirely within the defined polygon
	 */
	List<E> geoWithin(Polygon polygon, String user);

}