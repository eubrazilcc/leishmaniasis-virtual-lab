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

package eu.eubrazilcc.lvl.test;

import static java.util.Collections.unmodifiableMap;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides a common environment especially useful for integration tests.
 * @author Erik Torres <ertorser@upv.es>
 */
public class TestContext {

	private final String service;
	private final File testOutputDir;
	private final WebTarget target;
	private final ObjectMapper jsonMapper;
	private final Map<String, TestCredential> credentials;

	public TestContext(final File testOutputDir, final String service, final WebTarget target, final ObjectMapper jsonMapper, 
			final Map<String, TestCredential> credentials) {
		this.testOutputDir = testOutputDir;
		this.service = service;
		this.target = target;
		this.jsonMapper = jsonMapper;
		this.credentials = (credentials != null ? unmodifiableMap(credentials) : Collections.<String, TestCredential>emptyMap());
	}
	
	public File testOutputDir() {
		return testOutputDir;
	}

	public String service() {
		return service;
	}

	public WebTarget target() {
		return target;
	}

	public ObjectMapper jsonMapper() {
		return jsonMapper;
	}

	public Map<String, TestCredential> credentials() {
		return credentials;
	}

	public String token(final String id) {
		return credentials.get(id).getToken();
	}
	
	public String ownerid(final String id) {
		return credentials.get(id).getOwnerId();
	}

	public static class TestCredential {
		private final String ownerId;
		private final String token;
		private final String email;
		
		public TestCredential(final String ownerId, final String token) {
			this(ownerId, token, null);
		}

		public TestCredential(final String ownerId, final String token, final String email) {
			this.ownerId = ownerId;
			this.token = token;
			this.email = email;
		}

		public String getOwnerId() {
			return ownerId;
		}

		public String getToken() {
			return token;
		}
		
		public String getEmail() {
			return email;
		}
	}

}