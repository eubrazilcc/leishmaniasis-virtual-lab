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

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Binds Java objects to/from MongoDB using the Jackson JSON processor.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class MongoDBJsonMapper {

	public static final Logger LOGGER = getLogger(MongoDBJsonMapper.class);

	public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		// apply general configuration
		JSON_MAPPER.setSerializationInclusion(Include.NON_NULL);
		JSON_MAPPER.setSerializationInclusion(Include.NON_DEFAULT);
	}

	public static String toJson(final Object obj, final JsonOptions... options) {
		String payload = "";
		try {
			final boolean pretty = asList(ofNullable(options)
					.orElse(new JsonOptions[]{}))
					.contains(JsonOptions.JSON_PRETTY_PRINTER);
			payload = !pretty ? JSON_MAPPER.writeValueAsString(obj) : JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (final JsonProcessingException e) {
			LOGGER.error("Failed to export object to JSON", e);
		}
		return payload;
	}

	/**
	 * Object mapper options.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public enum JsonOptions {

		JSON_PRETTY_PRINTER

	}

}