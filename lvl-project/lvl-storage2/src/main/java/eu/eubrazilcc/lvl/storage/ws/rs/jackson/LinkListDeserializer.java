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

package eu.eubrazilcc.lvl.storage.ws.rs.jackson;

import static com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserialize lists of JAX-RS links from JSON to {@link Link} Java class.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LinkListDeserializer extends StdDeserializer<List<Link>> {

	private static final long serialVersionUID = -8033657526192304797L;

	protected LinkListDeserializer() {
		super(defaultInstance().constructCollectionType(List.class, Link.class));
	}

	@Override
	public List<Link> deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
		final List<Link> links = newArrayList();
		final JsonNode tree = parser.readValueAsTree();
		final Iterator<JsonNode> iterator = tree.elements();
		while (iterator.hasNext()) {
			final JsonNode nodeLink = iterator.next();
			final String href = nodeLink.get("href").textValue();
			final Builder builder = Link.fromUri(UriBuilder.fromUri(href).build());
			final JsonNode params = nodeLink.get("params");
			final JsonNode rel = params.get("rel");
			if (rel != null) {
				builder.rel(rel.textValue());
			}
			final JsonNode type = params.get("type");
			if (type != null) {
				builder.type(type.textValue());
			}
			links.add(builder.build());
		}
		return links;
	}

}