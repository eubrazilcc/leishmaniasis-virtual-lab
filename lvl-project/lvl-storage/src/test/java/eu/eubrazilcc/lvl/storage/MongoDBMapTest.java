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

package eu.eubrazilcc.lvl.storage;

import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBMapKey.escapeFieldName;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBMapKey.escapeMapKey;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBMapKey.unescapeFieldName;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Map.Entry;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import eu.eubrazilcc.lvl.storage.mongodb.MongoDBMap;
import eu.eubrazilcc.lvl.storage.mongodb.MongoDBMapKey;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBMapKeyDeserializer;
import eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBMapKeySerializer;

/**
 * Tests {@link MongoDBMap} maps.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MongoDBMapTest {

	public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		// apply general configuration
		JSON_MAPPER.setSerializationInclusion(Include.NON_NULL);
		JSON_MAPPER.setSerializationInclusion(Include.NON_DEFAULT);
		// register external serializers/deserializers		
		final SimpleModule simpleModule = new SimpleModule("LvLModule", new Version(1, 0, 0, null, "eu.eubrazilcc.lvl", "lvl-storage"));
		simpleModule.addKeySerializer(MongoDBMapKey.class, new MongoDBMapKeySerializer());
		simpleModule.addKeyDeserializer(MongoDBMapKey.class, new MongoDBMapKeyDeserializer());
		JSON_MAPPER.registerModule(simpleModule);
	}

	@Test
	public void test() {
		System.out.println("MongoDBMapTest.test()");		
		try {
			// test field name manipulation
			final String[] names = { "$this.is.an.invalid...s$ring." };
			final String[] escapedNames = { "\uff04this\uff0eis\uff0ean\uff0einvalid\uff0e\uff0e\uff0es\uff04ring\uff0e" };

			for (int i = 0; i < names.length; i++) {
				final String escapedName = escapeFieldName(names[i]);
				assertThat("escaped name is not null", escapedName, notNullValue());
				assertThat("escaped name is not empty", isNotBlank(escapedName), equalTo(true));
				assertThat("escaped name coincides with expected", escapedName, equalTo(escapedNames[i]));
				// uncomment for additional output
				System.out.println(" >> Original: " + names[i] + ", escaped=" + escapedName);

				final String unescapedName = unescapeFieldName(escapedName);
				assertThat("unescaped name is not null", unescapedName, notNullValue());
				assertThat("unescaped name is not empty", isNotBlank(unescapedName), equalTo(true));
				assertThat("unescaped name coincides with expected", unescapedName, equalTo(names[i]));
			}

			// test map operations
			final MongoDBMap<MongoDBMapKey, String> map = new MongoDBMap<>();
			map.put(escapeMapKey(names[0]), "Hello World!");
			assertThat("map is not empty", map.isEmpty(), equalTo(false));
			// uncomment for additional output
			for (final Entry<MongoDBMapKey, String> entry : map.entrySet()) {
				System.out.println(" >> Map entry: [" + entry.getKey().getKey() + ", " + entry.getValue() + "]\n");
			}			

			final MongoDBMapKey key = new MongoDBMapKey();
			key.setKey(escapedNames[0]);

			String value = map.get(key);
			assertThat("value is not null", value, notNullValue());
			assertThat("value is not empty", isNotBlank(value), equalTo(true));
			assertThat("value coincides with expected", value, equalTo("Hello World!"));

			value = map.getUnescaped(names[0]);
			assertThat("value is not null", value, notNullValue());
			assertThat("value is not empty", isNotBlank(value), equalTo(true));
			assertThat("value coincides with expected", value, equalTo("Hello World!"));

			// test JSON serialization
			final String payload = JSON_MAPPER.writeValueAsString(map);
			assertThat("serialized map is not null", payload, notNullValue());
			assertThat("serialized map is not empty", isNotBlank(payload), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Serialized map (JSON): " + payload);

			// test JSON deserialization
			@SuppressWarnings("unchecked")
			final MongoDBMap<MongoDBMapKey, String> map2 = JSON_MAPPER.readValue(payload, MongoDBMap.class);
			assertThat("deserialized map is not null", map2, notNullValue());
			assertThat("deserialized map coincides with expected", map2, equalTo(map));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("MongoDBMapTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("MongoDBMapTest.test() has finished");
		}
	}

}