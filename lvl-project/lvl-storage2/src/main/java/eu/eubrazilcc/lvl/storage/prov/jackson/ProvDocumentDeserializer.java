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

package eu.eubrazilcc.lvl.storage.prov.jackson;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.openprovenance.prov.interop.InteropFramework.ProvFormat.JSON;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

import org.openprovenance.prov.interop.InteropFramework;
import org.openprovenance.prov.model.Document;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Deserialize W3C PROV documents from JSON to {@link Document} Java class.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ProvDocumentDeserializer extends StdDeserializer<Document> {

	private static final long serialVersionUID = 2413052050095160323L;

	private static final Pattern UDOLLAR_PATTERN = compile(quote("\uff04"));
	private static final String UDOLLAR_REPLACEMENT = quoteReplacement("$");

	protected ProvDocumentDeserializer() {
		super(Document.class);
	}

	@Override
	public Document deserialize(final JsonParser parser, final DeserializationContext context) throws IOException, JsonProcessingException {
		// read "raw" object with Jackson parser
		final Map<?, ?> map = parser.readValueAs(Map.class);
		// write map to JSON using Gson
		final Type typeOfMap = new TypeToken<Map<String, Object>>(){}.getType();
		final String json = new Gson().toJson(map, typeOfMap);
		// unescape mongoDB special characters and read with W3C PROV
		final String unescaped = UDOLLAR_PATTERN.matcher(json).replaceAll(UDOLLAR_REPLACEMENT);
		return new InteropFramework().readDocument(new ByteArrayInputStream(unescaped.getBytes()), JSON);
	}

}