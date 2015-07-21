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

import static eu.eubrazilcc.lvl.core.xml.TapirXmlBinder.TAPIR_XMLB;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.apache.http.entity.ContentType.TEXT_XML;
import static org.apache.http.entity.ContentType.getOrDefault;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.junit.FixMethodOrder;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.http.client.HttpClientProvider;
import eu.eubrazilcc.lvl.core.tapir.SpeciesLinkConnector;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.ResponseType;

/**
 * Test TAPIR access protocol for information retrieval.
 * @author Erik Torres <ertorser@upv.es>
 */
@FixMethodOrder(NAME_ASCENDING)
public class TapirTest {

	@Test
	public void test01XmlBinding() {
		System.out.println("TapirTest.test01XmlBinding()");
		try {

			// TODO
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("TapirTest.test01XmlBinding() failed: " + e.getMessage());
		} finally {			
			System.out.println("TapirTest.test01XmlBinding() has finished");
		}
	}

	@Test
	public void test02splink() {
		System.out.println("TapirTest.test02splink()");
		try {
			try (final SpeciesLinkConnector splink = new SpeciesLinkConnector()) {
				final Set<String> collections = splink.collectionNames();
				assertThat("collections is not null or empty", collections, allOf(notNullValue(), not(empty())));
				for (final String collection : collections) {
					// test count
					final long count = splink.count(collection);
					assertThat("collection contains at least one element", count, greaterThan(0l));
					// uncomment for additional output
					System.out.println(" >> speciesLink collection '" + collection + "' count: " + count);
					
					// test collection fetching
					
				}
			}


			final URIBuilder uriBuilder = new URIBuilder("http://tapir.cria.org.br/tapirlink/tapir.php/specieslink");
			uriBuilder.addParameter("op", "search");
			uriBuilder.addParameter("start", "0");
			uriBuilder.addParameter("limit", "1000");
			uriBuilder.addParameter("model", "http://rs.tdwg.org/tapir/cs/dwc/terms/2009-09-23/template/dwc_simple.xml");
			uriBuilder.addParameter("filter", "http://rs.tdwg.org/dwc/dwcore/CollectionCode equals \"FIOCRUZ-CLIOC\"");
			uriBuilder.addParameter("orderby", "http://rs.tdwg.org/dwc/dwcore/CatalogNumber");
			final String url = uriBuilder.build().toString();			

			final HttpClientProvider httpClient = HttpClientProvider.create();
			final ResponseType response = httpClient.request(url).get().handleResponse(new ResponseHandler<ResponseType>() {
				@Override
				public ResponseType handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					final StatusLine statusLine = response.getStatusLine();
					final HttpEntity entity = response.getEntity();
					if (statusLine.getStatusCode() >= 300) {
						throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
					}
					if (entity == null) {
						throw new ClientProtocolException("Response contains no content");
					}
					final ContentType contentType = getOrDefault(entity);
					final String mimeType = contentType.getMimeType();
					if (!mimeType.equals(APPLICATION_XML.getMimeType()) && !mimeType.equals(TEXT_XML.getMimeType())) {
						throw new ClientProtocolException("Unexpected content type:" + contentType);
					}
					Charset charset = contentType.getCharset();
					if (charset == null) {
						charset = HTTP.DEF_CONTENT_CHARSET;
					}					
					return TAPIR_XMLB.typeFromInputStream(entity.getContent());
				}
			}, false);
			assertThat("response is not null", response, notNullValue());


			System.err.println("\n\n >> HERE: " + TAPIR_XMLB.typeToXml(response) + "\n");


			// TODO


		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("TapirTest.test02splink() failed: " + e.getMessage());
		} finally {			
			System.out.println("TapirTest.test02splink() has finished");
		}
	}

}