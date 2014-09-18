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

package eu.eubrazilcc.lvl.core.json.jackson;

import static com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Link;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serialize lists of JAX-RS {@link Link} Java class to JSON.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LinkListSerializer extends StdSerializer<List<Link>> {

	protected LinkListSerializer() {
		super(defaultInstance().constructCollectionType(List.class, Link.class));
	}

	@Override
	public void serialize(final List<Link> value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, 
	JsonProcessingException {
		if (value == null) {
			jgen.writeNull();
		} else {
			jgen.writeStartArray();
			for (final Link link : value) {
				serializeLink(jgen, link);				
			}
			jgen.writeEndArray();
		}
	}

	private void serializeLink(final JsonGenerator jgen, final Link link) throws JsonGenerationException, IOException {
		jgen.writeStartObject();
		// URI reference
		if (link.getUri() != null) {
			jgen.writeStringField("href", link.getUri().toString());	
		}
		// parameters
		jgen.writeFieldName("params");
		jgen.writeStartObject();
		if (link.getRel() != null) {
			jgen.writeStringField("rel", link.getRel());
		}
		if (link.getType() != null) {
			jgen.writeStringField("type", link.getType());	
		}
		if (link.getTitle() != null) {
			jgen.writeStringField("titles", link.getTitle());	
		}		
		jgen.writeEndObject();		
		jgen.writeEndObject();
	}

}