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

package eu.eubrazilcc.lvl.core.entrez;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.partition;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.Files.asByteSink;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.nio.file.Files.newBufferedReader;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Utilities to interact with the Entrez NCBI search system.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.ncbi.nlm.nih.gov/nuccore">Nucleotide NCBI</a>
 */
public final class EntrezHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(EntrezHelper.class);

	public static final String ESEARCH_BASE_URI = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
	public static final String EFETCH_BASE_URI = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";

	public static final String XPATH_TO_ESEARCH_COUNT = 
			"/*[local-name()=\"eSearchResult\"]"
					+ "/*[local-name()=\"Count\"]";

	public static final String XPATH_TO_ESEARCH_IDS = 
			"/*[local-name()=\"eSearchResult\"]"
					+ "/*[local-name()=\"IdList\"]"
					+ "/*[local-name()=\"Id\"]";

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	public static final String PHLEBOTOMUS_QUERY = "phlebotomus[Organism]";	
	
	/**
	 * Searches all occurrences of phlebotomus in the nucleotide database and returns their accession
	 * identifiers in a {@link Set}.
	 * @return a {@link Set} with the accession identifiers of all the phlebotomus found in the
	 *         nucleotide database.
	 */
	public static Set<String> listAllPhlebotomines() {
		return listNucleotideIds(PHLEBOTOMUS_QUERY);
	}
	
	/**
	 * Searches all occurrences of phlebotomus in the nucleotide database and saves them to the
	 * specified directory. The saved files are named id.gb, where id is the original sequence
	 * accession identifier.
	 * @param directory the directory where the files will be saved.
	 */
	public static void saveAllPhlebotomines(final File directory) {
		saveNucleotides(PHLEBOTOMUS_QUERY, directory);
	}	

	/**
	 * Searches for sequences in the nucleotide database and saves the entries in the specified
	 * directory. Searching the nucleotide database with general text queries will produce links 
	 * to results in Nucleotide, Genome Survey Sequence (GSS), and Expressed Sequence Tag (EST) 
	 * databases. The saved files are named id.gb, where id is the original sequence accession 
	 * identifier.
	 * @param query - Entrez query.
	 * @param directory - the directory where the files will be saved.
	 */
	public static void saveNucleotides(final String query, final File directory) {
		checkArgument(isNotBlank(query), "Uninitialized or invalid query");
		checkArgument(directory != null && (directory.isDirectory() || directory.mkdirs()) && directory.canWrite(), 
				"Uninitialized or invalid directory");
		// list Ids
		final Set<String> ids = listNucleotideIds(query);
		checkState(ids != null && !ids.isEmpty(), "No sequences were found");
		// save sequences
		saveNucleotides(ids, directory);
	}
	
	/**
	 * Fetches the sequences identified by their accession ids and saves them in the specified directory.
	 * The saved files are named id.gb, where id is the original sequence accession identifier.
	 * @param ids - accession identifiers.
	 * @param directory - the directory where the files will be saved.
	 */
	public static void saveNucleotides(final Set<String> ids, final File directory) {
		checkState(ids != null && !ids.isEmpty(), "Uninitialized or invalid sequence ids");
		checkArgument(directory != null && (directory.isDirectory() || directory.mkdirs()) && directory.canWrite(), 
				"Uninitialized or invalid directory");
		try {
			final int retstart = 0, retmax = 10000; // maximum 100,000
			final Iterable<List<String>> partitions = partition(ids, retmax);
			for (final List<String> chunk : partitions) {
				efetch(chunk, retstart, retmax, directory);			
			}
		} catch (Exception e) {
			LOGGER.error("Saving nucleotide sequences failed", e);
		}
	}

	/**
	 * Fetches the sequence identified by its accession id and seves it in the specified directory.
	 * @param id - accession identifier.
	 * @param directory - the directory where the file will be saved.
	 */
	public static void fecthNucleotide(final String id, final File directory) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid Id");
		checkArgument(directory != null && (directory.isDirectory() || directory.mkdirs()) && directory.canWrite(), 
				"Uninitialized or invalid directory");
		try {
			efetch(newArrayList(id), 0, 1, directory);
		} catch (Exception e) {
			LOGGER.error("Fetching nucleotide sequence failed", e);
		}
	}

	/**
	 * Lists the identifiers of the sequences found in the nucleotide database. Searching the nucleotide 
	 * database with general text queries will produce links to results in Nucleotide, Genome Survey 
	 * Sequence (GSS), and Expressed Sequence Tag (EST) databases.
	 * @param query - Entrez query.
	 * @return a collection of ids with no duplications.
	 */
	public static Set<String> listNucleotideIds(final String query) {
		final Set<String> ids = newHashSet();
		int esearchResultCount = -1;
		try {
			int retstart = 0, count = 0;
			final int retmax = 10000; // maximum 100,000
			do {
				final Document results = esearch(query, retstart, retmax);
				if (esearchResultCount < 0) {
					esearchResultCount = parseEsearchResponseCount(results);
				}
				final List<String> moreIds = parseEsearchResponseIds(results);
				count = moreIds.size();
				ids.addAll(moreIds);			
				LOGGER.trace("Listing Ids (start=" + retstart + ", max=" + retmax + ") produced " 
						+ count + " new records");
				retstart += count;				
			} while (count > 0 && retstart < esearchResultCount);
		} catch (Exception e) {
			LOGGER.error("Listing nucleotide ids failed", e);
		}
		checkState(ids.size() == esearchResultCount, "No all ids were imported");
		return ids;
	}

	private static Form esearchForm(final String query, final int retstart, final int retmax) {
		return Form.form()
				.add("db", "nuccore")
				.add("term", query)
				.add("retstart", Integer.toString(retstart))
				.add("retmax", Integer.toString(retmax));
	}

	private static Document esearch(final String query, final int retstart, final int retmax) throws Exception {
		return Request.Post(ESEARCH_BASE_URI)
				.useExpectContinue() // execute a POST with the 'expect-continue' handshake
				.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
				.bodyForm(esearchForm(query, retstart, retmax).build()).execute().handleResponse(new ResponseHandler<Document>() {
					@Override
					public Document handleResponse(final HttpResponse response) throws IOException {
						final StatusLine statusLine = response.getStatusLine();
						final HttpEntity entity = response.getEntity();
						if (statusLine.getStatusCode() >= 300) {
							throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
						}
						if (entity == null) {
							throw new ClientProtocolException("Response contains no content");
						}
						final DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
						try {
							final DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
							final ContentType contentType = ContentType.getOrDefault(entity);
							final String mimeType = contentType.getMimeType();
							if (!mimeType.equals(ContentType.APPLICATION_XML.getMimeType()) 
									&& !mimeType.equals(ContentType.TEXT_XML.getMimeType())) {
								throw new ClientProtocolException("Unexpected content type:" + contentType);
							}
							Charset charset = contentType.getCharset();
							if (charset == null) {
								charset = HTTP.DEF_CONTENT_CHARSET;
							}
							return docBuilder.parse(entity.getContent(), charset.name());
						} catch (ParserConfigurationException ex) {
							throw new IllegalStateException(ex);
						} catch (SAXException ex) {
							throw new ClientProtocolException("Malformed XML document", ex);
						}
					}
				});
	}

	private static int parseEsearchResponseCount(final Document document) throws Exception {
		checkNotNull(document, "Uninitialized document");
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final XPathExpression xPathExpression = xPath.compile(XPATH_TO_ESEARCH_COUNT);
		final Node node = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
		checkState(node != null, "No count node found");
		return Integer.parseInt(node.getTextContent());
	}

	private static List<String> parseEsearchResponseIds(final Document document) throws Exception {
		checkNotNull(document, "Uninitialized document");
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final XPathExpression xPathExpression = xPath.compile(XPATH_TO_ESEARCH_IDS);
		final NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
		final List<String> ids = newArrayList();
		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				ids.add(nodes.item(i).getTextContent().trim());
			}
		}
		/* if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(eu.eubrazilcc.lvl.core.xml.XmlHelper.documentToStringOmitXmlDeclaration(document));
		} */
		return ids;
	}

	private static void efetch(final List<String> ids, final int retstart, final int retmax, final File directory) throws Exception {
		// save the bulk of files to a temporary file
		final File tmpFile = File.createTempFile("gb-", ".tmp", directory);
		final String idsParam = Joiner.on(",").skipNulls().join(ids);
		LOGGER.trace("Fetching files from GenBank: ids=" + idsParam + ", retstart=" + retstart + ", retmax=" + retmax
				+ ", file=" + tmpFile.getPath());
		Request.Post(EFETCH_BASE_URI)
		.useExpectContinue() // execute a POST with the 'expect-continue' handshake
		.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
		.bodyForm(efetchForm(idsParam, retstart, retmax).build()).execute().handleResponse(new ResponseHandler<Void>() {
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
				final ContentType contentType = ContentType.getOrDefault(entity);
				final String mimeType = contentType.getMimeType();
				if (!mimeType.equals(ContentType.APPLICATION_OCTET_STREAM.getMimeType())) {
					throw new ClientProtocolException("Unexpected content type:" + contentType);
				}
				Charset charset = contentType.getCharset();
				if (charset == null) {
					charset = HTTP.DEF_CONTENT_CHARSET;
				}
				final ByteSink sink = asByteSink(tmpFile);
				sink.writeFrom(entity.getContent());
				return null;
			}
		});
		// go over the file extracting the sequences
		final ListeningExecutorService executorService = listeningDecorator(newCachedThreadPool());
		final ListenableFuture<String[]> future = executorService.submit(new Callable<String[]>() {
			@Override
			public String[] call() throws Exception {
				final Set<String> files = newHashSet();
				final BufferedReader reader = newBufferedReader(tmpFile.toPath(), DEFAULT_CHARSET);
				int i = -1;
				File file = null;
				ByteSink sink = null;
				String line = null;
				while ((line = reader.readLine()) != null) {
					// start parsing a new fragment
					if (file == null && i < ids.size() - 1) {						
						file = new File(directory, ids.get(++i) + ".gb");
						sink = asByteSink(file, FileWriteMode.APPEND);
						LOGGER.info("Processing file: " + file.getCanonicalPath());
					}
					if (i < ids.size()) {						
						// write non-empty lines to the file
						if (isNotBlank(line)) {
							sink.write((line + "\n").getBytes(DEFAULT_CHARSET));
						}
						// process line
						if (line.startsWith("VERSION    ")) {
							checkState(line.contains(ids.get(i)), "Id not found in the VERSION section");
						} else if (line.startsWith("//")) {						
							files.add(file.getCanonicalPath());
							file = null;						
						}
					} else {
						if (isNotBlank(line)) {
							LOGGER.info("Ingoring line after all sequences were processed" + line);
						}
					}
				}
				return files.toArray(new String[files.size()]);
			}
		});
		Futures.addCallback(future, new FutureCallback<String[]>() {
			@Override
			public void onSuccess(final String[] result) {
				LOGGER.info("One bulk sequence file was processed successfully: " + tmpFile.getName()
						+ ", number of created files: " + result.length);
				deleteQuietly(tmpFile);
			}
			@Override
			public void onFailure(final Throwable error) {
				LOGGER.error("Failed to process bulk sequence file " + tmpFile.getName(), error);
			}
		});
		// wait for files to be processed
		future.get();
	}

	private static Form efetchForm(final String ids, final int retstart, final int retmax) {
		return Form.form()
				.add("db", "nuccore")
				.add("id", ids)
				.add("retstart", Integer.toString(retstart))
				.add("retmax", Integer.toString(retmax))
				// GenBank flat file
				.add("rettype", "gb") // use gbwithparts to download the file with the full sequence (this could produce huge files)
				.add("retmode", "text");
	}

	/**
	 * Extracts the country source from the feature list of a GenBank DNA sequence file.
	 * @param file GenBank sequence file.
	 * @return the value of the country source feature if exists, otherwise {@code null}.
	 * @throws Exception if an error occurs.
	 */
	public static @Nullable String countryFeature(final File file) throws Exception {
		String country = null;
		final BufferedReader reader = newBufferedReader(file.toPath(), DEFAULT_CHARSET);
		boolean featuresFound = false, sourceFound = false;
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("ORIGIN") || line.startsWith("CONTIG      ")) {
				break;				
			} else if (!featuresFound) {
				if (line.startsWith("FEATURES             Location/Qualifiers")) {
					featuresFound = true;
				}
			} else if (!sourceFound) {
				if (line.startsWith("     source          ")) {
					sourceFound = true;
				}
			} else {
				if (line.startsWith("                     /country=")) {
					final Pattern pattern = Pattern.compile("\"(.*?)\"");
					final Matcher matcher = pattern.matcher(line.substring(30));
					if (matcher.find()) {
						country = matcher.group(1);
					}
				}
			}			
		}
		return country;
	}

}