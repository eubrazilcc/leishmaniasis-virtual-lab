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

package eu.eubrazilcc.lvl.core.tapir;

import static eu.eubrazilcc.lvl.core.util.XmlUtils.nodeToString;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XMLB;
import static eu.eubrazilcc.lvl.core.xml.TapirXmlBinder.TAPIR_XMLB;
import static javax.xml.xpath.XPathConstants.NODE;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.apache.http.entity.ContentType.TEXT_XML;
import static org.apache.http.entity.ContentType.getOrDefault;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.eubrazilcc.lvl.core.http.client.HttpClientProvider;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecordSet;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.ResponseType;

/**
 * Provides a generic client which supports the TAPIR model to access collections of biological data.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.tdwg.org/activities/tapir/specification">TAPIR - TDWG Access Protocol for Information Retrieval</a>
 */
public abstract class TapirClient implements AutoCloseable {

	public static final String COLLECTION_VAR = "\\$COLLECTION";
	public static final String XPATH_TO_DWCSET = "/*[local-name()=\"response\"]"
			+ "/*[local-name()=\"search\"]"
			+ "/*[local-name()=\"SimpleDarwinRecordSet\"]";

	private final HttpClientProvider httpClient = HttpClientProvider.create();

	protected ResponseType count(final String url, final String concept, final String filter) throws URISyntaxException, IOException {
		final URIBuilder uriBuilder = new URIBuilder(url);
		uriBuilder.addParameter("op", "inventory");
		uriBuilder.addParameter("count", "true");
		uriBuilder.addParameter("start", "0");
		uriBuilder.addParameter("limit", "1"); // at least 1 record is needed to get a count result
		uriBuilder.addParameter("concept", concept);
		uriBuilder.addParameter("filter", filter);
		final String url2 = uriBuilder.build().toString();
		final HttpClientProvider httpClient = HttpClientProvider.create();
		return httpClient.request(url2).get().handleResponse(new ResponseHandler<ResponseType>() {
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
	}
	
	private String fetch(final String url, final String outputModel, final String filter, final String orderby, final int start, 
			final int limit) throws URISyntaxException, IOException {
		final URIBuilder uriBuilder = new URIBuilder(url);
		uriBuilder.addParameter("op", "search");
		uriBuilder.addParameter("start", Integer.toString(start));
		uriBuilder.addParameter("limit", Integer.toString(limit));
		uriBuilder.addParameter("model", outputModel);
		uriBuilder.addParameter("filter", filter);
		uriBuilder.addParameter("orderby", orderby);
		final String url2 = uriBuilder.build().toString();
		return httpClient.request(url2).get().handleResponse(new ResponseHandler<String>() {
			@Override
			public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
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
				try (final Scanner scanner = new Scanner(entity.getContent(), "UTF-8")) {
					return scanner.useDelimiter("\\A").next(); 
				}
			}
		}, false);
	}

	protected ResponseType fetchResponse(final String url, final String outputModel, final String filter, final String orderby, final int start, 
			final int limit) throws URISyntaxException, IOException {					
		return TAPIR_XMLB.typeFromXml(fetch(url, outputModel, filter, orderby, start, limit));			
	}

	protected SimpleDarwinRecordSet fetchDarwinCore(final String url, final String outputModel, final String filter, final String orderby, final int start, 
			final int limit) throws URISyntaxException, IOException, XPathExpressionException, SAXException, ParserConfigurationException {
		// fetch response from server
		final String response = fetch(url, outputModel, filter, orderby, start, limit);
		// parse server response
		final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));		
		// extract DarwinCore from server response
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final XPathExpression xPathExpression = xPath.compile(XPATH_TO_DWCSET);
		final Node node = (Node) xPathExpression.evaluate(document, NODE);		
		final String payload = nodeToString(node);		
		return DWC_XMLB.typeFromXml(payload);
	}

	protected String parseFilter(final String filter, final String collection) {
		return filter.replaceAll(COLLECTION_VAR, collection);
	}

	@Override
	public void close() throws Exception {
		if (httpClient != null) {
			httpClient.close();
		}
	}

}