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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.mutable.MutableLong;

import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;

/**
 * Base DAO.
 * @author Erik Torres <ertorser@upv.es>
 * @param <K> - the type of keys used in the database to identify the elements
 * @param <E> - the type of elements in this DAO
 */
public interface BaseDAO<K, E> {	
	
	/**
	 * Inserts a new element in the database.
	 * @param e - element to be inserted in the database
	 * @return the id assigned to the element in the database
	 */
	String insert(E e);
	
	/**
	 * Updates an existing element in the database.
	 * @param e - element to be updated in the database
	 */
	void update(E e);
	
	/**
	 * Removes an element from the database.
	 * @param key - identifier of the element to be removed from the database
	 */
	void delete(K key);
	
	/**
	 * Returns all the elements from the database.
	 * @return all the elements that are in the database
	 */
	List<E> findAll();
	
	/**
	 * Search for an element in the database using the specified id.
	 * @param key - identifier whose associate value is to be returned
	 * @return the element to which the specified key is associated in the database, 
	 *         or {@code null} if the database contains no entry for the key
	 */
	E find(K key);
	
	/**
	 * Returns a view of the elements in the database that contains the specified
	 * range. The elements are sorted by the key in ascending order. Optionally,
	 * the number of elements found in the database is returned to the caller.
	 * @param start - starting index
	 * @param size - maximum number of elements returned
	 * @param count - (optional) is updated with the number of elements in the database
	 * @return a view of the elements in the database that contains the specified range
	 */
	List<E> list(int start, int size, @Nullable MutableLong count);
	
	/**
	 * Returns the number of elements in the database.
	 * @return the number of elements in the database
	 */
	long count();
	
	/**
	 * Returns the elements in the database that are within the specified distance (in meters) 
	 * from the center point specified (using WGS84).
	 * @param point - longitude, latitude pair represented in WGS84 coordinate reference system (CRS)
	 * @param maxDistance - limits the results to those elements that fall within the specified 
	 *        distance (in meters) from the center point
	 * @return the elements that are within the specified distance from the center point
	 */
	List<E> getNear(Point point, double maxDistance);
	
	/**
	 * Returns the elements in the database that exist entirely within the defined polygon.
	 * @param polygon - geometric shape with at least four edges
	 * @return the elements that exist entirely within the defined polygon
	 */
	List<E> geoWithin(Polygon polygon);
	
	/**
	 * Writes statistics about the elements to the specified output stream.
	 * @param os - the output stream to write the statistics to
	 */
	void stats(OutputStream os) throws IOException;
	
}