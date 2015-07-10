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

import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions.JSON_PRETTY_PRINTER;
import static java.util.Arrays.asList;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Binds Java objects to/from mongoDB using the Jackson JSON processor.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class MongoJsonMapper {

	public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		// apply general configuration
		JSON_MAPPER.setSerializationInclusion(Include.NON_NULL);
		JSON_MAPPER.setSerializationInclusion(Include.NON_EMPTY);
		JSON_MAPPER.setSerializationInclusion(Include.NON_DEFAULT);		
	}

	public static final String objectToJson(final Object obj, final MongoJsonOptions... options) throws JsonProcessingException {
		boolean pretty = false;
		if (options != null) {
			final List<MongoJsonOptions> optList = asList(options);
			pretty = optList.contains(JSON_PRETTY_PRINTER);
		}
		return !pretty ? JSON_MAPPER.writeValueAsString(obj) : JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);		
	}

}