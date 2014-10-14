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
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.partition;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.io.Files.asByteSink;
import static com.google.common.util.concurrent.Futures.addCallback;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.ESEARCH_XMLB;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getCount;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getIds;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getGenInfoIdentifier;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XMLB;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.getPubMedId;
import static java.io.File.createTempFile;
import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.newBufferedReader;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import static org.apache.http.entity.ContentType.APPLICATION_XML;
import static org.apache.http.entity.ContentType.TEXT_PLAIN;
import static org.apache.http.entity.ContentType.TEXT_XML;
import static org.apache.http.entity.ContentType.getOrDefault;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

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

import com.google.common.base.Joiner;
import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.ESearchResult;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticleSet;

/**
 * Utilities to interact with the Entrez NCBI search system.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.ncbi.nlm.nih.gov/nuccore">Nucleotide NCBI</a>
 */
public final class EntrezHelper {

	private static final Logger LOGGER = getLogger(EntrezHelper.class);

	public static final String NUCLEOTIDE_DB = "nuccore";
	public static final String PUBMED_DB = "pubmed";

	public static final String ESEARCH_BASE_URI = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
	public static final String EFETCH_BASE_URI = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";

	public static final int MAX_RECORDS_LISTED = 10000;  // esearch maximum 100,000
	public static final int MAX_RECORDS_FETCHED = 10000; // efetch maximum 100,000

	public static final Charset DEFAULT_CHARSET = forName("UTF-8");

	public static final String PHLEBOTOMUS_QUERY = "phlebotomus[Organism]";
	public static final String PHLEBOTOMUS_CYTOCHROME_OXIDASE_I = "(sandfly[Organism] OR sand fly[Organism] OR Phlebotomus[Organism] OR Sergentomyia[Organism] OR Lutzomyia[Organism]) AND (cytochrome oxidase I[Gene Name] OR cytochrome oxidase 1[Gene Name] OR coi[Gene Name] OR COI[Gene Name] OR co1[Gene Name] OR CO1[Gene Name] OR coxi[Gene Name] OR cox1[Gene Name] OR COXI[Gene Name])";

	public static final String LEISHMANIA_QUERY = "leishmania[Organism]";
	
	/**
	 * Lists the identifiers of the sequences found in the nucleotide database. Searching the nucleotide 
	 * database with general text queries will produce links to results in Nucleotide, Genome Survey 
	 * Sequence (GSS), and Expressed Sequence Tag (EST) databases. For example, use {@code listNucleotides(PHLEBOTOMUS_QUERY)}
	 * to search all occurrences of phlebotomus in the nucleotide database.
	 * @param query - Entrez query.
	 * @return a {@link Set} with the accession identifiers of all the DNA sequences found in the nucleotide 
	 *         database for the specified query.
	 */
	public static Set<String> listNucleotides(final String query) {
		final Set<String> ids = newHashSet();
		int esearchResultCount = -1;
		try {
			int retstart = 0, count = 0;
			final int retmax = MAX_RECORDS_LISTED;
			do {
				final ESearchResult result = esearch(NUCLEOTIDE_DB, query, retstart, retmax);
				if (esearchResultCount < 0) {
					esearchResultCount = getCount(result);
				}
				final List<String> moreIds = getIds(result);
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

	/**
	 * Fetches the sequence identified by its accession id and saves it in the specified directory.
	 * @param id - accession identifier.
	 * @param directory - the directory where the file will be saved.
	 * @param format - the format that will be used to store the file.
	 */
	public static void saveNucleotide(final String id, final File directory, final Format format) {
		checkArgument(isNotBlank(id), "Uninitialized or invalid Id");
		checkArgument(directory != null && (directory.isDirectory() || directory.mkdirs()) && directory.canWrite(), 
				"Uninitialized or invalid directory");
		try {
			efetch(newArrayList(id), 0, 1, directory, format);
		} catch (Exception e) {
			LOGGER.error("Fetching nucleotide sequence failed", e);
		}
	}

	/**
	 * Fetches the sequences identified by their accession identifiers and saves them in the specified directory,
	 * using the specified data format. The saved files are named 'id.ext', where 'id' is the original sequence 
	 * accession identifier and 'ext' the file extension that matches the specified format.
	 * @param ids - accession identifiers.
	 * @param directory - the directory where the files will be saved.
	 * @param format - the format that will be used to store the files.
	 */
	public static void saveNucleotides(final Set<String> ids, final File directory, final Format format) {
		checkState(ids != null && !ids.isEmpty(), "Uninitialized or invalid sequence ids");
		checkArgument(directory != null && (directory.isDirectory() || directory.mkdirs()) && directory.canWrite(), 
				"Uninitialized or invalid directory");
		try {
			final int retstart = 0, retmax = MAX_RECORDS_FETCHED;
			final Iterable<List<String>> partitions = partition(ids, retmax);
			for (final List<String> chunk : partitions) {
				efetch(chunk, retstart, retmax, directory, format);
			}
		} catch (Exception e) {
			LOGGER.error("Saving nucleotide sequences failed", e);
		}
	}

	/**
	 * Gets the publications identified by their PubMed identifiers (PMIDs) and saves them in the specified 
	 * directory, using the specified data format. The saved files are named 'id.ext', where 'id' is the 
	 * original PMID and 'ext' the file extension that matches the specified format.
	 * @param ids
	 * @param directory
	 * @param format
	 */
	public static void savePublications(final Set<String> ids, final File directory, final Format format) {
		checkState(ids != null && !ids.isEmpty(), "Uninitialized or invalid sequence ids");
		checkArgument(directory != null && (directory.isDirectory() || directory.mkdirs()) && directory.canWrite(), 
				"Uninitialized or invalid directory");
		try {
			final int retstart = 0, retmax = MAX_RECORDS_FETCHED;
			final Iterable<List<String>> partitions = partition(ids, retmax);
			for (final List<String> chunk : partitions) {
				efetch(chunk, retstart, retmax, directory, format);
			}
		} catch (Exception e) {
			LOGGER.error("Saving publication references failed", e);
		}		
	}

	private static Form esearchForm(final String database, final String query, final int retstart, final int retmax) {
		return Form.form()
				.add("db", database)
				.add("term", query)
				.add("retstart", Integer.toString(retstart))
				.add("retmax", Integer.toString(retmax));
	}

	public static ESearchResult esearch(final String database, final String query, final int retstart, final int retmax) throws Exception {
		return Request.Post(ESEARCH_BASE_URI)
				.useExpectContinue() // execute a POST with the 'expect-continue' handshake
				.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
				.bodyForm(esearchForm(database, query, retstart, retmax).build()).execute().handleResponse(new ResponseHandler<ESearchResult>() {
					@Override
					public ESearchResult handleResponse(final HttpResponse response) throws IOException {
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
						return ESEARCH_XMLB.typeFromInputStream(entity.getContent());
					}
				});
	}

	public static void efetch(final List<String> ids, final int retstart, final int retmax, final File directory, final Format format) throws Exception {
		switch (format) {
		case FLAT_FILE:
			efetchFlatFiles(ids, retstart, retmax, directory);
			break;
		case GB_SEQ_XML:
			efetchGBSeqXMLFiles(ids, retstart, retmax, directory);			
			break;
		case PUBMED_XML:
			efetchPubmedXMLFiles(ids, retstart, retmax, directory);
			break;
		default:
			throw new IllegalArgumentException("Unsupported file format: " + format);
		}
	}

	private static void efetchGBSeqXMLFiles(final List<String> ids, final int retstart, final int retmax, final File directory) throws Exception {
		// save the bulk of files to a temporary file
		final File tmpFile = createTempFile("gb-", ".tmp", directory);
		final String idsParam = Joiner.on(",").skipNulls().join(ids);
		LOGGER.trace("Fetching " + ids.size() + " files from GenBank, retstart=" + retstart + ", retmax=" + retmax + ", file=" + tmpFile.getPath());
		Request.Post(EFETCH_BASE_URI)
		.useExpectContinue() // execute a POST with the 'expect-continue' handshake
		.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
		.bodyForm(efetchForm(NUCLEOTIDE_DB, idsParam, retstart, retmax, "xml").build()).execute().handleResponse(new ResponseHandler<Void>() {
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
				if (!mimeType.equals(APPLICATION_OCTET_STREAM.getMimeType()) && !mimeType.equals(TEXT_XML.getMimeType())) {
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
		final ListenableFuture<String[]> future = TASK_RUNNER.submit(new Callable<String[]>() {
			@Override
			public String[] call() throws Exception {
				final Set<String> files = newHashSet();
				final GBSet gbSet = GBSEQ_XMLB.typeFromFile(tmpFile);
				checkState(gbSet != null, "Expected GBSeqXML, but no content read from temporary file downloaded with efetch");
				if (gbSet.getGBSeq() != null) {
					final List<GBSeq> gbSeqs = gbSet.getGBSeq();
					for (final GBSeq gbSeq : gbSeqs) {
						final Integer gi = getGenInfoIdentifier(gbSeq);
						if (gi != null) {							
							final File file = new File(directory, gi.toString() + ".xml");							
							GBSEQ_XMLB.typeToFile(gbSeq, file);
							files.add(file.getCanonicalPath());
						} else {
							LOGGER.warn("Ingoring malformed sequence (gi not found) in efetch response");
						}
					}
				} else {
					LOGGER.warn("Ingoring malformed sequence (GBSeq not found) in efetch response");
				}
				return files.toArray(new String[files.size()]);
			}
		});
		addCallback(future, new FutureCallback<String[]>() {
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

	private static void efetchFlatFiles(final List<String> ids, final int retstart, final int retmax, final File directory) throws Exception {
		// save the bulk of files to a temporary file
		final File tmpFile = createTempFile("gb-", ".tmp", directory);
		final String idsParam = Joiner.on(",").skipNulls().join(ids);
		LOGGER.trace("Fetching " + ids.size() + " files from GenBank, retstart=" + retstart + ", retmax=" + retmax
				+ ", file=" + tmpFile.getPath());
		Request.Post(EFETCH_BASE_URI)
		.useExpectContinue() // execute a POST with the 'expect-continue' handshake
		.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
		.bodyForm(efetchForm(NUCLEOTIDE_DB, idsParam, retstart, retmax, "text").build()).execute().handleResponse(new ResponseHandler<Void>() {
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
				if (!mimeType.equals(APPLICATION_OCTET_STREAM.getMimeType()) && !mimeType.equals(TEXT_PLAIN.getMimeType())) {
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
		final ListenableFuture<String[]> future = TASK_RUNNER.submit(new Callable<String[]>() {
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
							if (sink != null) {
								sink.write((line + "\n").getBytes(DEFAULT_CHARSET));
							} else {
								LOGGER.warn("Ingoring line when all files were closed: " + line);	
							}
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
							LOGGER.warn("Ingoring line after all sequences were processed: " + line);
						}
					}
				}
				return files.toArray(new String[files.size()]);
			}
		});
		addCallback(future, new FutureCallback<String[]>() {
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

	private static void efetchPubmedXMLFiles(final List<String> ids, final int retstart, final int retmax, final File directory) throws Exception {
		// save the bulk of files to a temporary file
		final File tmpFile = createTempFile("pm-", ".tmp", directory);
		final String idsParam = Joiner.on(",").skipNulls().join(ids);
		LOGGER.trace("Fetching " + ids.size() + " files from PubMed, retstart=" + retstart + ", retmax=" + retmax + ", file=" + tmpFile.getPath());
		Request.Post(EFETCH_BASE_URI)
		.useExpectContinue() // execute a POST with the 'expect-continue' handshake
		.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
		.bodyForm(efetchForm(PUBMED_DB, idsParam, retstart, retmax, "xml").build()).execute().handleResponse(new ResponseHandler<Void>() {
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
				if (!mimeType.equals(APPLICATION_OCTET_STREAM.getMimeType()) && !mimeType.equals(TEXT_XML.getMimeType())) {
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
		final ListenableFuture<String[]> future = TASK_RUNNER.submit(new Callable<String[]>() {
			@Override
			public String[] call() throws Exception {
				final Set<String> files = newHashSet();
				final PubmedArticleSet articleSet = PUBMED_XMLB.typeFromFile(tmpFile);
				checkState(articleSet != null, "Expected PubMed article XML, but no content read from temporary file downloaded with efetch");
				if (articleSet.getPubmedArticle() != null) {
					final List<PubmedArticle> articles = articleSet.getPubmedArticle();
					for (final PubmedArticle article : articles) {
						final String pmid = getPubMedId(article);						
						if (pmid != null) {							
							final File file = new File(directory, pmid + ".xml");
							PUBMED_XMLB.typeToFile(article, file);
							files.add(file.getCanonicalPath());
						} else {
							LOGGER.warn("Ingoring malformed article (pmid not found) in efetch response");
						}
					}
				} else {
					LOGGER.warn("Ingoring malformed article (PubmedArticle not found) in efetch response");
				}				
				return files.toArray(new String[files.size()]);
			}
		});
		addCallback(future, new FutureCallback<String[]>() {
			@Override
			public void onSuccess(final String[] result) {
				LOGGER.info("One bulk publication file was processed successfully: " + tmpFile.getName()
						+ ", number of created files: " + result.length);
				deleteQuietly(tmpFile);
			}
			@Override
			public void onFailure(final Throwable error) {
				LOGGER.error("Failed to process bulk publication file " + tmpFile.getName(), error);
			}
		});
		// wait for files to be processed
		future.get();
	}

	private static Form efetchForm(final String database, final String ids, final int retstart, final int retmax, final String retmode) {
		final Form form = Form.form()
				.add("db", database)
				.add("id", ids)
				.add("retstart", Integer.toString(retstart))
				.add("retmax", Integer.toString(retmax))				
				.add("retmode", retmode);
		if (NUCLEOTIDE_DB.equals(database)) {
			form.add("rettype", "gb"); // use gbwithparts to download the file with the full sequence (this could produce huge files)
		} else if (PUBMED_DB.equals(database)) {
			// PubMed uses rettype = null for text ASN.1 and XML file format
		} else {
			throw new IllegalStateException("Invalid database " + database);
		}
		return form;
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

	/**
	 * Lists the GenBank sequences found in the specified directory (subdirectories are not searched).
	 * @param directory - the directory to search for sequences in
	 * @return 
	 */
	public static Collection<File> listGBFiles(final File directory, final Format format) {
		checkArgument(directory != null && directory.isDirectory() && directory.canRead(), 
				"Uninitialized or invalid directory");
		String extension;
		switch (format) {
		case GB_SEQ_XML:
			extension = "xml";
			break;
		case FLAT_FILE:
			extension = "gb";
			break;
		default:
			extension = null;
			break;
		}
		checkArgument (isNotBlank(extension), "Unsupported GenBank format: " + format);
		return listFiles(directory, new String[] { extension }, false);
	}

	/**
	 * GenBank formats.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public enum Format {
		FLAT_FILE,    // GenBank Flat Files
		GB_SEQ_XML,   // GBSeqXML
		TINY_SEQ_XML, // TinySeqXML
		PUBMED_XML    // PubMed XML
	}

}