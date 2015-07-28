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

package eu.eubrazilcc.lvl.drive;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.microservices.LvlDaemon;

/**
 * Tests {@link AppDaemon}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DriveDaemonTest {

	private int port = 8080;
	private String uri;
	private LvlDaemon daemon;

	@Before
	public void setUp() throws Exception {
		daemon = new AppDaemon();
		daemon.init(new DaemonContext() {			
			@Override
			public DaemonController getController() {
				return null;
			}
			@Override
			public String[] getArguments() {
				return null;
			}
		});
		daemon.start();
		daemon.awaitHealthy(5, TimeUnit.SECONDS);
		uri = "http://localhost:" + port + "/";
	}

	@After
	public void cleanUp() throws Exception {
		daemon.stop();
		daemon.destroy();
	}

	@Test
	public void test() {
		System.out.println("DriveDaemonTest.test()");
		try {
			// test index page
			String payload = getHtml("");
			// uncomment for additional output
			System.out.println(" >> Response: " + abbreviate(payload, 64));

			// test get dataset
			payload = getJson("rest/v1/datasets/~/21");
			// uncomment for additional output
			System.out.println(" >> Response: " + payload);


			// TODO

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DriveDaemonTest.test() failed: " + e.getMessage());
		} finally {
			System.out.println("DriveDaemonTest.test() has finished");
		}
	}

	private String getHtml(final String path) throws URISyntaxException, IOException {
		return get(path, "text/html");
	}

	private String getJson(final String path) throws URISyntaxException, IOException {
		return get(path, "application/json");		
	}

	private String get(final String path, final String mimeType) throws URISyntaxException, IOException {
		String payload = null;
		final URIBuilder uriBuilder = new URIBuilder(uri + path);
		final Response response = Request.Get(uriBuilder.build())
				.version(HttpVersion.HTTP_1_1)
				.addHeader("Accept", mimeType)
				.execute();
		assertThat("response is not null", response, notNullValue());
		HttpResponse httpResponse = response.returnResponse();
		assertThat("HTTP response is not null", httpResponse, notNullValue());
		assertThat("status line is not null", httpResponse.getStatusLine(), notNullValue());
		assertThat("status code coincides with expected", httpResponse.getStatusLine().getStatusCode(), equalTo(200));
		Header[] headers = httpResponse.getAllHeaders();
		assertThat("headers are not null", headers, notNullValue());
		assertThat("headers are not empty", headers.length, greaterThan(0));
		HttpEntity entity = httpResponse.getEntity();
		assertThat("entity is not null", entity, notNullValue());
		assertThat("content length coincides with expected", entity.getContentLength(), greaterThan(0l));
		try (final Scanner scanner = new Scanner(entity.getContent(), "UTF-8")) {
			payload = scanner.useDelimiter("\\A").next();			
		}
		return payload;
	}

}