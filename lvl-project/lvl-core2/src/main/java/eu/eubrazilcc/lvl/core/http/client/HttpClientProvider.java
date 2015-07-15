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

package eu.eubrazilcc.lvl.core.http.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.io.Files.asByteSink;
import static java.util.Collections.synchronizedList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.http.entity.ContentType.getOrDefault;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.http.Consts;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.google.common.io.ByteSink;

/**
 * Provides two classes of HTTP clients: a) a client that reuses connections from a pool; and b) a client isolated from the pool.
 * Regardless of the client, HTTP/1.1 protocol version is used.
 * @author Erik Torres <ertorser@upv.es>
 */
public class HttpClientProvider implements AutoCloseable {

	private final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
	private final ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {
		public long getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
			// honor 'keep-alive' header
			final HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));			
			while (it.hasNext()) {
				final HeaderElement he = it.nextElement();
				final String param = he.getName();
				final String value = he.getValue();
				if (value != null && param.equalsIgnoreCase("timeout")) {
					try {
						return Long.parseLong(value) * 1000l;
					} catch(NumberFormatException ignore) { }
				}
			}
			// otherwise keep alive for 30 seconds
			return 30l * 1000l;
		}
	};
	private final CloseableHttpClient httpClient;
	private final List<Http1_1Request> requests = synchronizedList(new ArrayList<Http1_1Request>());	

	private HttpClientProvider() {
		final ConnectionConfig connConfig = ConnectionConfig.custom()
				.setCharset(Consts.UTF_8)
				.build();
		connManager.setDefaultConnectionConfig(connConfig);
		connManager.setMaxTotal(100);
		connManager.setDefaultMaxPerRoute(10);
		httpClient = HttpClients.custom()
				.setConnectionManager(connManager)
				.setKeepAliveStrategy(keepAliveStrategy)
				.evictExpiredConnections()
				.evictIdleConnections(30l, TimeUnit.SECONDS)
				.build();
	}

	protected CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	protected CloseableHttpClient getIsolatedHttpClient() {
		return HttpClients.custom()
				.setKeepAliveStrategy(keepAliveStrategy)
				.evictExpiredConnections()
				.evictIdleConnections(30l, TimeUnit.SECONDS)
				.build();
	}

	public Http1_1Request request(final String uri) {
		final Http1_1Request request = new Http1_1Request(uri, this);
		requests.add(request);
		return request;
	}

	public void abort(final Http1_1Request request) {
		requests.remove(request);
	}

	public void dispose(final Http1_1Request request) {
		requests.remove(request);
	}

	public static HttpClientProvider create() {
		return new HttpClientProvider();
	}

	@Override
	public void close() throws Exception {
		if (httpClient != null) {
			httpClient.close();
		}
	}

	/**
	 * HTTP/1.1 request.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class Http1_1Request {

		private final URI uri;
		private String method;
		private Iterable <? extends NameValuePair> formParams;

		private final HttpClientProvider pool;
		private final RequestConfig.Builder configBuilder = RequestConfig.custom();		

		public Http1_1Request(final String uri, final HttpClientProvider manager) {			
			checkArgument(isNotBlank(trimToNull(uri)), "Uninitialized or invalid URI");
			this.uri = URI.create(uri);
			this.pool = checkNotNull(manager, "Uninitialized manager");
		}

		public void abort() {
			pool.abort(this);
		}

		public void dispose() {
			pool.dispose(this);
		}

		/**
		 * Prepare a GET request that uses HTTP/1.1.
		 * @return a reference to this class.
		 */
		public Http1_1Request get() {
			method = HttpGet.METHOD_NAME;
			this.configBuilder.setExpectContinueEnabled(false);
			return this;
		}

		/**
		 * Prepare a POST request that uses HTTP/1.1 with the 'expect-continue' handshake.
		 * @return a reference to this class.
		 */
		public Http1_1Request post() {
			method = HttpPost.METHOD_NAME;
			this.configBuilder.setExpectContinueEnabled(true);			
			return this;
		}

		public Http1_1Request bodyForm(final NameValuePair... formParams) {
			return bodyForm(Arrays.asList(formParams));
		}

		public Http1_1Request bodyForm(final Iterable <? extends NameValuePair> formParams) {
			this.formParams = formParams;
			return this;
		}

		public <T> T handleResponse(final ResponseHandler<T> responseHandler, final boolean isolated) throws ClientProtocolException, IOException {
			try {
				final HttpUriRequest httpRequest = createHttpRequest();
				T response = null;
				if (!isolated) {
					response = pool.getHttpClient().execute(httpRequest, responseHandler);
				} else {
					try (final CloseableHttpClient isolatedClient = pool.getIsolatedHttpClient()) {
						response = isolatedClient.execute(httpRequest, responseHandler);
					}
				}
				return response;				
			} finally {
				dispose();
			}
		}

		public void saveContent(final File outfile, final @Nullable List<String> validTypes, final boolean isolated) throws IOException {
			handleResponse(new ResponseHandler<Void>() {
				@Override
				public Void handleResponse(final HttpResponse response) throws IOException {
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
					if (validTypes != null && !validTypes.contains(mimeType)) {
						throw new ClientProtocolException("Unexpected content type: " + contentType);
					}					
					Charset charset = contentType.getCharset();
					if (charset == null) {
						charset = HTTP.DEF_CONTENT_CHARSET;
					}
					final ByteSink sink = asByteSink(outfile);
					sink.writeFrom(entity.getContent());
					return null;
				}
			}, isolated);
		}

		private HttpUriRequest createHttpRequest() {
			HttpUriRequest httpRequest = null;
			if (HttpGet.METHOD_NAME.equals(method)) {
				httpRequest = RequestBuilder.get(uri)
						.setVersion(HttpVersion.HTTP_1_1)
						.build();
			} else if (HttpPost.METHOD_NAME.equals(method)) {
				final Iterable <? extends NameValuePair> bodyForm = formParams != null ? formParams : new ArrayList<NameValuePair>();
				httpRequest = RequestBuilder.post(uri)
						.setEntity(new UrlEncodedFormEntity(bodyForm, Consts.UTF_8))
						.setVersion(HttpVersion.HTTP_1_1)
						.build();						
			} else {
				throw new IllegalStateException("Unsupported HTTP method: " + method);
			}
			return httpRequest;
		}

	}	

}