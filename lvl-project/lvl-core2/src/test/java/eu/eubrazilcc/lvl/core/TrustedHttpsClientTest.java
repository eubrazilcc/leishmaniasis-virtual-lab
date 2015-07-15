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

package eu.eubrazilcc.lvl.core;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.http.client.TrustedHttpsClient;

/**
 * Tests HTTP clients provided by the class {@link TrustedHttpsClient}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class TrustedHttpsClientTest {

	@Test
	public void test() {
		System.out.println("TrustedHttpsClientTest.test()");
		try {
			try (final TrustedHttpsClient httpClient = new TrustedHttpsClient()) {
				final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
					public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
						assertThat("HTTP response is not null", response, notNullValue());
						assertThat("Status line is not null", response.getStatusLine(), notNullValue());
						assertThat("Status coincides with expected", response.getStatusLine().getStatusCode(), 
								allOf(greaterThanOrEqualTo(200), lessThan(300)));
						final HttpEntity entity = response.getEntity();
						assertThat("Entity is not null", entity, notNullValue());
						return entity != null ? EntityUtils.toString(entity) : null;	                    
					}
				};
				final String payload = httpClient.executeGet("https://www.google.es", null, responseHandler);
				assertThat("Content is not null", payload, notNullValue());
				assertThat("Content not is empty", isNotBlank(payload));
				/* uncomment for additional output
				System.out.println(" >> Server response: " + payload); */				
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("TrustedHttpsClientTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("TrustedHttpsClientTest.test() has finished");
		}
	}

}