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

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.xml.GeoJSONXmlBinder.GEOJSON_XML;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.eubrazilcc.lvl.core.geospatial.Line;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;

/**
 * Geometry adapter for XML/JSON data binding.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GeometryAdapter extends XmlAdapter<String, Object> {

	@Override
	public String marshal(final Object geometry) throws Exception {
		if (geometry != null) {
			checkArgument(geometry instanceof Point || geometry instanceof Line || geometry instanceof Polygon, 
					"Unsupported geometry: " + geometry.getClass().getCanonicalName());
			return GEOJSON_XML.typeToXml(geometry);
		}
		return null;
	}

	@Override
	public Object unmarshal(final String payload) throws Exception {
		if (isNotBlank(payload)) {
			return GEOJSON_XML.typeFromXml(payload);
		}
		return null;
	}

}