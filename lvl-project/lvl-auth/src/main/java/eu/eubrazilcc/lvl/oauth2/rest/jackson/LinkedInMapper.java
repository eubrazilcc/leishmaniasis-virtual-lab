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

package eu.eubrazilcc.lvl.oauth2.rest.jackson;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JSON_MAPPER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Optional;

/**
 * Marshal/unmarshal LinkedIn JSON messages from/to Java classes. 
 * @author Erik Torres <ertorser@upv.es>
 */
public class LinkedInMapper {

	private JsonNode rootNode;

	public LinkedInMapper readObject(final String payload) {
		try {
			rootNode = JSON_MAPPER.readValue(payload, JsonNode.class);
			return this;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read root node", e);
		}
	}

	public String getUserId() {
		final String value = readValue("id");		
		checkState(isNotBlank(value), "Uninitialized or invalid user id: " + value);
		return value;
	}

	public String getEmailAddress() {
		final String value = readValue("emailAddress");		
		checkState(isNotBlank(value), "Uninitialized or invalid email address: " + value);
		return value;
	}

	public String getFirstName() {
		final String value = readValue("firstName");		
		checkState(isNotBlank(value), "Uninitialized or invalid firstname: " + value);
		return value;
	}

	public String getLastName() {
		final String value = readValue("lastName");		
		checkState(isNotBlank(value), "Uninitialized or invalid lastname: " + value);
		return value;
	}

	public Optional<String> getIndustry() {
		return readOptionalValue("industry");
	}

	public Optional<Set<String>> getPositions() {
		final JsonNode node = rootNode.path("positions").path("values");
		Set<String> values = null;
		if (!node.isMissingNode()) {
			for (final JsonNode node2 : node) {
				final JsonNode isCurrentNode = node2.path("isCurrent");
				final JsonNode companyNode = node2.path("company").path("name");
				if (!isCurrentNode.isMissingNode() && !companyNode.isMissingNode()) {					
					String value = null;
					if (isCurrentNode.booleanValue() && isNotBlank(value = trimToNull(companyNode.textValue()))) {
						if (values == null) {
							values = newHashSet();
						}
						values.add(value);
					}
				}
			}
		}
		return fromNullable(values);
	}

	public Optional<String> readOptionalValue(final String name) {
		checkArgument(isNotBlank(name), "Uninitialized or invalid name");
		String value = null;
		final JsonNode node = rootNode.path(name);
		if (!node.isMissingNode()) {
			value = trimToEmpty(node.textValue());
		}
		return fromNullable(value);
	}

	public String readValue(final String name) {
		checkArgument(isNotBlank(name), "Uninitialized or invalid name");
		final JsonNode node = rootNode.path(name);
		checkState(!node.isMissingNode(), "Could not find: " + name);
		final String value = trimToNull(node.textValue());
		checkState(isNotBlank(value), "Empty string is not a valid value");
		return value;
	}

	public static LinkedInMapper createLinkedInMapper() {
		return new LinkedInMapper();
	}

}