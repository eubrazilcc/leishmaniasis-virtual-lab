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

package eu.eubrazilcc.lvl.core.geojson.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;

/**
 * Stores geospatial locations in GeoJSON format.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://geojson.org/">GeoJSON -- JSON Geometry and Feature Description</a>
 */
public class LngLatAltDeserializer extends JsonDeserializer<LngLatAlt> {

	@Override
	public LngLatAlt deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException, JsonProcessingException {
		if (parser.isExpectedStartArrayToken()) {
			return deserializeArray(parser, context);
		}
		throw context.mappingException(LngLatAlt.class);
	}

	private LngLatAlt deserializeArray(final JsonParser parser, final DeserializationContext context) 
			throws IOException, JsonProcessingException {
		final LngLatAlt node = new LngLatAlt();
		node.setLongitude(extractDouble(parser, context, false));
		node.setLatitude(extractDouble(parser, context, false));
		node.setAltitude(extractDouble(parser, context, true));
		if (parser.hasCurrentToken() && parser.getCurrentToken() != JsonToken.END_ARRAY) {
			parser.nextToken();
		}
		return node;
	}

	private double extractDouble(final JsonParser parser, final DeserializationContext context, final boolean optional)
			throws JsonParseException, IOException {
		final JsonToken token = parser.nextToken();
		if (token == null) {
			if (optional) {
				return Double.NaN;
			} 
			throw context.mappingException("Unexpected end-of-input when binding data into LngLatAlt");
		} else {
			switch (token) {
			case END_ARRAY:
				if (optional) {
					return Double.NaN;
				}
				throw context.mappingException("Unexpected end-of-input when binding data into LngLatAlt");
			case VALUE_NUMBER_FLOAT:
				return parser.getDoubleValue();
			case VALUE_NUMBER_INT:
				return parser.getLongValue();
			default:
				throw context.mappingException("Unexpected token (" + token.name() + ") when binding data into LngLatAlt");
			}
		}
	}

}