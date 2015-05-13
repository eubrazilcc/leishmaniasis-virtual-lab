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

package eu.eubrazilcc.lvl.core;

import eu.eubrazilcc.lvl.core.geojson.GeoJsonObject;

/**
 * Includes a geospatial location, which can be a point, a region, etc.
 * @author Erik Torres <ertorser@upv.es>
 * @param <T> the type of objects that can be used as geospatial location in this object
 */
public interface Localizable<T extends GeoJsonObject> {

	/**
	 * Gets the location.
	 * @return the location.
	 */
	T getLocation();

	/**
	 * Sets the location.
	 * @param location - point to be set as this class location
	 */
	void setLocation(T location);
	
	/**
	 * Gets a tag that can be used to annotate this object when is displayed in a map. For example, a unique identifier.
	 * @return a tag that can be used to annotate this object when is displayed in a map.
	 */
	String getTag();
	
}