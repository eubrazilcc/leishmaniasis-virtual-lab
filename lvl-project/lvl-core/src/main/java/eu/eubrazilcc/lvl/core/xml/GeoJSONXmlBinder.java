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

package eu.eubrazilcc.lvl.core.xml;

import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import eu.eubrazilcc.lvl.core.geospatial.Line;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;

/**
 * GeoJSON XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class GeoJSONXmlBinder extends XmlBinder {

	private static final Class<?>[] SUPPORTED_CLASSES = {
		Point.class,
		Line.class,
		Polygon.class
	};

	private static JAXBContext CONTEXT;
	private static JAXBIntrospector INTROSPECTOR;

	static {
		try {
			// context
			CONTEXT = JAXBContext.newInstance(SUPPORTED_CLASSES, new HashMap<String, Object>());
			// introspector
			INTROSPECTOR = CONTEXT.createJAXBIntrospector();
		} catch (Exception e) { }
	}

	public static final GeoJSONXmlBinder GEOJSON_XML = new GeoJSONXmlBinder();

	private GeoJSONXmlBinder() {
		super(CONTEXT, INTROSPECTOR);
	}	

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(Point.class)) {
			element = new Point();
		} else if (clazz.equals(Line.class)) {
			element = new Line();
		} else if (clazz.equals(Polygon.class)) {
			element = new Polygon();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}

}