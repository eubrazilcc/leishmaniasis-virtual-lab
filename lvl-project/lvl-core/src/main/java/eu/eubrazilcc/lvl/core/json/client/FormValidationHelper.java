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

package eu.eubrazilcc.lvl.core.json.client;

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Supports validation, which is a common requirement in end-user applications. Services must respond to validation 
 * requests by returning an encoded JSON of array containing the valid key to the caller: {@code { valid: 'true or false' }}.
 * The field {@code valid} informs the caller about the result of the validation.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class FormValidationHelper {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static {
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);		
	}

	public static String getValidationType(final MultivaluedMap<String, String> form) {
		if (form == null || form.get("type") == null || form.get("type").size() != 1 || isBlank(form.getFirst("type"))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		return form.getFirst("type");
	}

	public static String getValidationField(final String type, final MultivaluedMap<String, String> form) {
		if (form == null || form.get(type) == null || form.get(type).size() != 1 || isBlank(form.getFirst(type))) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		return form.getFirst(type);
	}

	public static String validationResponse(final boolean isValid) {
		return "{ \"valid\" : \"" + isValid + "\" }";
	}

	public static boolean readValid(final String content) {
		try {
			final JsonNode rootNode = JSON_MAPPER.readTree(content);
			final JsonNode validNode = rootNode.path("valid");
			return validNode.asBoolean();
		} catch (IOException e) {
			throw new RuntimeException("Error when parsing the JSON data", e);
		}
	}

}