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

package eu.eubrazilcc.lvl.storage.mongodb.jackson;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserialize mongoDB dates from JSON to {@link Date} Java class.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class MongoDateDeserializer extends StdDeserializer<Date> {

	private static final long serialVersionUID = -8033657526192304797L;

	public MongoDateDeserializer() {
		super(Date.class);
	}

	@Override
	public Date deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
		Date date = null;
		parser.nextToken();
		while (parser.nextToken() != JsonToken.END_OBJECT) {
			final String fieldname = parser.getCurrentName();			
			if ("$date".equals(fieldname)) {				
				date = new Date(parser.getLongValue());
			}
		}
		return date;
	}

}