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

package eu.eubrazilcc.lvl.core.http;

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static java.nio.file.Files.createTempDirectory;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.apache.http.conn.ssl.SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

/**
 * Provides a HTTP client associated to a temporary custom SSL context which will trust own CA and self-signed certificates.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientCustomSSL.java">ClientCustomSSL.java</a>
 * @see <a href="https://code.google.com/p/java-use-examples/source/browse/trunk/src/com/aw/ad/util/InstallCert.java">InstallCert.java</a>
 */
public final class TrustedHttpsClient implements AutoCloseable {

	private static final Logger LOGGER = getLogger(TrustedHttpsClient.class);

	private final File trustStoreDir;	
	private final char[] password = random(8, true, true).toCharArray();

	public TrustedHttpsClient() {
		trustStoreDir = createTrustStoreDir();
	}

	public <T> T executeGet(final String url, final ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
		String url2 = null;
		checkArgument(isNotBlank(url2 = trimToNull(url)), "Uninitialized or invalid URL");
		try (final CloseableHttpClient httpClient = createHttpClient(trustStoreDir, password, url2)) {			
			final HttpGet request = new HttpGet(url2);
			LOGGER.trace("Executing request: " + request.getRequestLine());
			return httpClient.execute(request, responseHandler);
		}
	}

	public <T> T executePost(final String url, final ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
		String url2 = null;
		checkArgument(isNotBlank(url2 = trimToNull(url)), "Uninitialized or invalid URL");
		try (final CloseableHttpClient httpClient = createHttpClient(trustStoreDir, password, url2)) {			
			final HttpPost request = new HttpPost(url2);
			LOGGER.trace("Executing request: " + request.getRequestLine());
			return httpClient.execute(request, responseHandler);
		}
	}

	@Override
	public void close() throws Exception {		
		deleteQuietly(trustStoreDir);
	}

	private static final File createTrustStoreDir() {
		File tmpDir = null;
		try {
			tmpDir = createTempDirectory(CONFIG_MANAGER.getLocalCacheDir().toPath(), TrustedHttpsClient.class.getSimpleName() + "_").toFile();
		} catch (IOException e) {
			LOGGER.error("Failed to create trust store directory", e);
		}
		return tmpDir;
	}

	/**
	 * Creates a custom SSL context where clients will trust own CA and self-signed certificates and associates a HTTP client to the context.
	 * @return a HTTP client that will trust own CA and self-signed certificates.
	 * @throws Exception if an error occurs.
	 */
	private static final CloseableHttpClient createHttpClient(final File trustStoreDir, final char[] password, final String url) {
		CloseableHttpClient httpClient = null;
		try {			
			final File trustStoreFile = new File(trustStoreDir, "trusted.keystore");
			final KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
			// create a new, empty trust store
			if (!trustStoreFile.exists()) {
				trustStoreDir.mkdirs();
				trustStoreFile.createNewFile();				
				trustStore.load(null, password);

			}			
			// import certificate to trust store
			importCertificate(url, trustStore);
			// save trust store to disk
			try (final FileOutputStream outstream = new FileOutputStream(trustStoreFile)) {
				trustStore.store(outstream, password);
			}
			// trust own CA and all self-signed certificates			
			final SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
					.build();
			// allow trusted protocols only
			final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext,
					new String[] { "SSLv2Hello", "TLSv1", "TLSv1.1", "TLSv1.2" },
					null,
					BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			httpClient = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.build();
		} catch (Exception e) {
			LOGGER.error("Failed to create HTTP client", e);
		}
		return httpClient;
	}

	private static final void importCertificate(final String url, final KeyStore trustStore) throws Exception {
		final URL url2 = new URL(url);
		final SSLContext sslContext = SSLContext.getInstance("TLS");
		final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);
		final X509TrustManager defaultTrustManager = (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
		final SavingTrustManager trustManager = new SavingTrustManager(defaultTrustManager);
		sslContext.init(null, new TrustManager[]{ trustManager }, null);
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();		
		final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(url2.getHost(), url2.getPort() > 0 ? url2.getPort() : 443);
		socket.setSoTimeout(10000);
		try {
			socket.startHandshake();
			socket.close();			
		} catch (SSLException e) { }

		final X509Certificate[] chain = trustManager.chain;
		if (chain == null) {
			LOGGER.error("Could not obtain server certificate chain from: " + url);
			return;
		}

		final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		final MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < chain.length; i++) {
			final X509Certificate cert = chain[i];
			final String alias = url2.getHost() + "-" + (i + 1);
			if (!trustStore.containsAlias(alias)) {
				sha1.update(cert.getEncoded());
				md5.update(cert.getEncoded());
				LOGGER.trace("Importing certificate to trusted keystore >> "
						+ "Subject: " + cert.getSubjectDN()
						+ ", Issuer: " + cert.getIssuerDN()
						+ ", SHA1: " + printHexBinary(sha1.digest())
						+ ", MD5: " + printHexBinary(md5.digest())
						+ ", Alias: " + alias);
				trustStore.setCertificateEntry(alias, cert);
			}
		}
	}

	private static class SavingTrustManager implements X509TrustManager {

		private final X509TrustManager tm;
		private X509Certificate[] chain;

		public SavingTrustManager(final X509TrustManager tm) {
			this.tm = tm;
		}

		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			throw new UnsupportedOperationException();
		}

		public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
			this.chain = chain;
			tm.checkServerTrusted(chain, authType);
		}
	}

}