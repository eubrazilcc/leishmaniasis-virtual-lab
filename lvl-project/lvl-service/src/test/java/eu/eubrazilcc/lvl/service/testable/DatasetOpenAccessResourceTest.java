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

package eu.eubrazilcc.lvl.service.testable;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.getSequence;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;

import eu.eubrazilcc.lvl.core.DatasetOpenAccess;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.DatasetOpenAccesses;
import eu.eubrazilcc.lvl.service.rest.DatasetOpenAccessResource;
import eu.eubrazilcc.lvl.service.rest.PublicResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to open access objects in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetOpenAccessResourceTest extends Testable {

	public DatasetOpenAccessResourceTest(final TestContext testCtxt) {
		super(testCtxt);
	}

	@Override
	public void test() throws Exception {
		printTestStart(DatasetOpenAccessResourceTest.class.getSimpleName(), "test");

		// test create open access link
		final Path path = DatasetOpenAccessResource.class.getAnnotation(Path.class);
		final DatasetOpenAccess openAccess = DatasetOpenAccess.builder()
				.filename("my_ncbi_sequence.xml")
				.namespace(testCtxt.ownerid("user1"))
				.build();
		Response response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(openAccess, APPLICATION_JSON));
		assertThat("Create dataset open access link response is not null", response, notNullValue());
		assertThat("Create dataset open access link response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset open access link response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create dataset open access link response entity is not null", payload, notNullValue());
		assertThat("Create dataset open access link response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset open access link response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset open access link response JAX-RS object: " + response);
		System.out.println(" >> Create dataset open access link HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));			
		assertThat("Create dataset open access link location is not null", location, notNullValue());
		assertThat("Create dataset open access link path is not empty", isNotBlank(location.getPath()), equalTo(true));					

		// test get open access link (from user unauthorized user account)
		DatasetOpenAccess openAccess2 = null;
		try {
			openAccess2 = testCtxt.target().path(path.value())
					.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
					.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
					.request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user3")))
					.get(DatasetOpenAccess.class);				
			fail("get dataset open access link from unauthorized user account must produce 401 error");
		} catch (NotAuthorizedException e) {
			// uncomment for additional output		
			System.out.println(" >> Get dataset open access link (unauthorized user account) produced the expected 401 error");
		}

		// test get open access link (from owner account)
		openAccess2 = testCtxt.target().path(path.value())				
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(DatasetOpenAccess.class);
		assertThat("Get dataset open access link (from owner) result is not null", openAccess2, notNullValue());					
		assertThat("Get dataset open access link (from owner) filename is not empty", isNotBlank(openAccess2.getFilename()), equalTo(true));
		assertThat("Get dataset open access link (from owner) filename coincides with expected", openAccess2.getFilename(), equalTo("my_ncbi_sequence.xml"));
		assertThat("Get dataset open access link (from owner) namespace is not empty", isNotBlank(openAccess2.getNamespace()), equalTo(true));
		assertThat("Get dataset open access link (from owner) namespace coincides with expected", openAccess2.getNamespace(), equalTo(testCtxt.ownerid("user1")));
		assertThat("Get dataset open access link (from owner) creation date is not null", openAccess2.getOpenAccessDate(), notNullValue());
		assertThat("Get dataset open access link (from owner) link is not empty", isNotBlank(openAccess2.getOpenAccessLink()), equalTo(true));	
		// uncomment for additional output
		System.out.println(" >> Get dataset open access link (from owner) result: " + openAccess2.toString());			

		// test list open access links (from owner account)
		DatasetOpenAccesses openAccesses = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(DatasetOpenAccesses.class);
		assertThat("Get dataset open access links (owner) result is not null", openAccesses, notNullValue());
		assertThat("Get dataset open access links (owner) list is not null", openAccesses.getElements(), notNullValue());
		assertThat("Get dataset open access links (owner) list is not empty", openAccesses.getElements().isEmpty(), equalTo(false));
		assertThat("Get dataset open access links (owner) items count coincide with list size", openAccesses.getElements().size(), 
				equalTo(openAccesses.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get dataset open access links (owner) result: " + openAccesses.toString());			

		// test download from open access link			
		URI downloadUri = testCtxt.target().path(PublicResource.class.getAnnotation(Path.class).value())
				.path("datasets")
				.path(urlEncodeUtf8(openAccesses.getElements().get(0).getOpenAccessLink()))
				.getUri();
		org.apache.http.client.fluent.Response response3 = Request.Get(downloadUri)
				.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
				.addHeader("Accept", APPLICATION_OCTET_STREAM)
				.execute();
		assertThat("Download open access dataset response is not null", response3, notNullValue());
		HttpResponse response4 = response3.returnResponse();
		assertThat("Download open access dataset HTTP response is not null", response4, notNullValue());
		assertThat("Download open access dataset status line is not null", response4.getStatusLine(), notNullValue());
		assertThat("Download open access dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
				equalTo(OK.getStatusCode()));
		Header[] headers = response4.getAllHeaders();
		assertThat("Download open access dataset headers is not null", headers, notNullValue());
		assertThat("Download open access dataset headers is not empty", headers.length, greaterThan(0));
		String filename = null;
		for (int i = 0; i < headers.length && filename == null; i++) {
			if ("content-disposition".equalsIgnoreCase(headers[i].getName())) {
				final HeaderElement[] elements = headers[i].getElements();
				if (elements != null) {
					for (int j = 0; j < elements.length && filename == null; j++) {							
						if ("attachment".equalsIgnoreCase(elements[j].getName())) {
							final NameValuePair pair = elements[j].getParameterByName("filename");
							if (pair != null) {
								filename = pair.getValue();
							}
						}
					}
				}
			}
		}
		assertThat("Download open access dataset filename is not empty", isNotBlank(filename), equalTo(true));
		HttpEntity entity = response4.getEntity();
		assertThat("Download open access dataset entity is not null", entity, notNullValue());
		assertThat("Download open access dataset content length coincides with expected", entity.getContentLength(), greaterThan(0l));
		File outfile = new File(testCtxt.testOutputDir(), filename);
		deleteQuietly(outfile);
		outfile.createNewFile();
		try (final InputStream inputStream = entity.getContent();
				final FileOutputStream outputStream = new FileOutputStream(outfile)) {
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		}
		assertThat("Downloaded open access file exists", outfile.exists(), equalTo(true));
		assertThat("Downloaded open access file is not empty", outfile.length(), greaterThan(0L));			
		GBSeq sequence = getSequence(outfile);
		assertThat("XML parsed from downloaded open access file is not null", sequence, notNullValue());
		// uncomment for additional output
		System.out.println(" >> Saved open access file: " + filename);

		// URL shortening tests (depends on the availability of the Google API key)
		if (isNotBlank(CONFIG_MANAGER.getGoogleAPIKey())) {
			// test shorten URL with valid public resource
			URI endpointUri = testCtxt.target().path(PublicResource.class.getAnnotation(Path.class).value())
					.path("datasets")
					.path(urlEncodeUtf8(openAccesses.getElements().get(0).getOpenAccessLink()))
					.path("shortened_url")
					.getUri();
			payload = Request.Get(endpointUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", TEXT_PLAIN)
					.execute()
					.returnContent()
					.asString();
			assertThat("Shorten open access dataset response entity is not null", payload, notNullValue());
			assertThat("Shorten open access dataset response entity is empty", isNotBlank(payload));
			// uncomment for additional output
			System.out.println(" >> Shortened URL: " + payload);

			// test shorten URL with invalid public resource (should fail with status 404)
			endpointUri = testCtxt.target().path(PublicResource.class.getAnnotation(Path.class).value())
					.path("datasets")
					.path("this_is_an_invalid_secret")
					.path("shortened_url")
					.getUri();
			response3 = Request.Get(endpointUri)
					.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
					.addHeader("Accept", TEXT_PLAIN)
					.execute();
			assertThat("Shorten open access dataset response is not null", response3, notNullValue());
			response4 = response3.returnResponse();
			assertThat("Shorten open access dataset HTTP response is not null", response4, notNullValue());
			assertThat("Shorten open access dataset status line is not null", response4.getStatusLine(), notNullValue());
			assertThat("Shorten open access dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
					equalTo(NOT_FOUND.getStatusCode()));
		} else {
			System.err.println("\n\n >> Skipping URL shortening test since Google API key is not available in the test system\n");
		}

		// test remove open access link
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml"))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete dataset open access link response is not null", response, notNullValue());
		assertThat("Delete dataset open access link response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete dataset open access link response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete dataset open access link response entity is not null", payload, notNullValue());
		assertThat("Delete dataset open access link response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete dataset open access link response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete dataset open access link response JAX-RS object: " + response);
		System.out.println(" >> Delete dataset open access link HTTP headers: " + response.getStringHeaders());

		printTestEnd(DatasetOpenAccessResourceTest.class.getSimpleName(), "test");
	}

}