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

package eu.eubrazilcc.lvl.service.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.efetch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.esearch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.parseEsearchResponseCount;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.parseEsearchResponseIds;
import static java.nio.file.Files.createTempDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.concurrent.TaskRunner;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;

/**
 * Manages the sequences in the LVL collection, participating in the discovering, importation
 * and update of the sequences in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum SequenceManager {

	INSTANCE;

	private final static Logger LOGGER = LoggerFactory.getLogger(SequenceManager.class);

	public final static ImmutableList<String> DEFAULT_DATA_SOURCES = ImmutableList.of(DataSource.GENBANK);

	public final static long FETCH_TIMEOUT_SECONDS     = 900l; // 15 minutes
	public final static long DB_IMPORT_TIMEOUT_SECONDS = 300l; // 5 minutes

	private SequenceManager() { }

	/**
	 * Imports sequences from default, external databases into the application's database.
	 */
	public void importSequences() {
		for (final String dataSource : DEFAULT_DATA_SOURCES) {
			importSequences(dataSource);
		}		
	}

	/**
	 * Imports sequences from external databases into the application's database.
	 * @param dataSource - source database, for example, GenBank
	 */
	public void importSequences(final String dataSource) {
		checkArgument(isNotBlank(dataSource), "Uninitialized data source");
		if (DataSource.GENBANK.equals(dataSource)) {
			importGenBankSequences();
		} else {
			throw new IllegalArgumentException("Unsupported data source: " + dataSource);
		}
	}

	private void importGenBankSequences() {
		int esearchResultCount = -1, totalIds = 0;
		try {
			int retstart = 0, count = 0;
			final int retmax = EntrezHelper.MAX_RECORDS_LISTED;
			do {
				final Document results = esearch(EntrezHelper.PHLEBOTOMUS_QUERY, retstart, retmax);
				if (esearchResultCount < 0) {
					esearchResultCount = parseEsearchResponseCount(results);
				}
				final List<String> ids = parseEsearchResponseIds(results);
				count = ids.size();
				totalIds += count;				
				// submit a new task to filter out the identifiers already stored in the database. Once that the identifiers
				// are filtered, a new task is submitted to fetch the sequences from GenBank. A callback function is called
				// every time a bulk of sequences is fetched and this function submits a new task to import the sequences into
				// the database
				addCallback(transform(TaskRunner.INSTANCE.submit(filterMissingIds(ids)), fetchGBFlatFiles()), importGBFlatFiles());
				LOGGER.trace("Listing Ids (start=" + retstart + ", max=" + retmax + ") produced " + count + " new records");
				retstart += count;
			} while (count > 0 && retstart < esearchResultCount);
		} catch (Exception e) {
			LOGGER.error("Listing nucleotide ids failed", e);
		}
		checkState(esearchResultCount == -1 || totalIds == esearchResultCount, "No all ids were imported");
	}

	/**
	 * Filters out the sequence that are already stored in the database, returning to the caller
	 * the list of identifiers that are missing from the database. Duplicate identifiers are also
	 * removed from the original list.
	 * @param ids - the list of sequence identifiers to filter
	 * @return the list of unique identifiers that are missing from the database
	 */
	private static Callable<List<String>> filterMissingIds(final List<String> ids) {
		return new Callable<List<String>>() {
			@Override
			public List<String> call() throws Exception {
				return from(ids).transform(new Function<String, String>() {
					@Override
					public String apply(final String id) {

						// TODO
						return ("353470160".equals(id) || "353483325".equals(id) || "353481165".equals(id) ? id : null);
						// TODO

						/* return SequenceDAO.INSTANCE.find(SequenceKey.builder()
								.dataSource(DataSource.GENBANK)
								.accession(id)
								.build()) == null ? id : null; */
					}
				}).filter(notNull()).toSet().asList();
			}					
		};		
	}

	private static Function<List<String>, Collection<File>> fetchGBFlatFiles() {
		return new Function<List<String>, Collection<File>>() {
			@Override
			public Collection<File> apply(final List<String> ids) {
				final ListenableFuture<Collection<File>> future = TaskRunner.INSTANCE.submit(new Callable<Collection<File>>() {
					@Override
					public Collection<File> call() throws Exception {
						File tmpDir = null;
						boolean shouldClean = false;
						try {
							tmpDir = createTempDirectory("tmp_sequences").toFile();
							efetch(ids, 0, EntrezHelper.MAX_RECORDS_FETCHED, tmpDir, Format.FLAT_FILE);
							final Collection<File> files = listFiles(tmpDir, new String[] { "gb" }, false);
							checkState(files != null && files.size() == ids.size(), "No all sequences were fetched");					
							return files;
						} catch (Exception e) {
							shouldClean = true;
							throw new IllegalStateException("Failed to import nucleotide sequences from GenBank", e);
						} finally {
							if (shouldClean) {
								deleteQuietly(tmpDir);
							}
						}
					}					
				});
				try {
					return future.get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to fecth sequences from GenBank using flat file format", e);
				}
			}
		};		
	}

	private static FutureCallback<Collection<File>> importGBFlatFiles() {
		return new FutureCallback<Collection<File>>() {
			@Override
			public void onSuccess(final Collection<File> files) {
				final ListenableFuture<Void> future = TaskRunner.INSTANCE.submit(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						for (final File file : files) {

							// TODO
							System.err.println("\n\nImport file: " + file + "\n\n");
							// TODO

						}
						return null;
					}
				});
				try {
					future.get(DB_IMPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
			@Override
			public void onFailure(final Throwable cause) {
				throw new IllegalStateException("Failed to import sequences from GenBank using flat file format", cause);
			}
		};
	}

}