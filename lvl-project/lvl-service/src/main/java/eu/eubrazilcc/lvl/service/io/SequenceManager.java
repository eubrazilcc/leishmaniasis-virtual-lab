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
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.Futures.transform;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.efetch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.esearch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.parseEsearchResponseCount;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.parseEsearchResponseIds;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.concurrent.TaskRunner;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
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
	public final static long DB_IMPORT_TIMEOUT_SECONDS = 600l; // 10 minutes	

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
		final Format format = Format.GB_SEQ_XML; // GenBank file format
		final String extension = "xml"; // GenBank file extension
		int esearchResultCount = -1, totalIds = 0;
		try {
			int retstart = 0, count = 0;
			final int retmax = EntrezHelper.MAX_RECORDS_LISTED;
			final List<ListenableFuture<Integer>> futures = newArrayList();
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
				futures.add(transform(transform(TaskRunner.INSTANCE.submit(filterMissingIds(ids)), fetchGBFiles(format, extension)), importGBFiles(format)));
				LOGGER.trace("Listing Ids (start=" + retstart + ", max=" + retmax + ") produced " + count + " new records");
				retstart += count;
			} while (count > 0 && retstart < esearchResultCount);
			final ListenableFuture<List<Integer>> combinedFuture = allAsList(futures);
			final List<Integer> list = combinedFuture.get();
			checkState(list != null, "No sequences imported");			
			LOGGER.info(list.size() + " sequences imported");
		} catch (Exception e) {
			LOGGER.error("Listing nucleotide ids failed", e);
		}
		checkState(esearchResultCount == -1 || totalIds == esearchResultCount, "No all ids were imported");
		// TODO: send notification on completion or error
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
						return ("353470160".equals(id) 
								|| "353483325".equals(id) 
								|| "353481165".equals(id)
								|| "545910417".equals(id)
								|| "545910416".equals(id)
								|| "545910415".equals(id)
								|| "545910387".equals(id)
								|| "545910370".equals(id)
								|| "507528205".equals(id)
								|| "430902590".equals(id)
								|| "451935057".equals(id)
								|| "384562886".equals(id)
								? id : null);
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

	private static Function<List<String>, Collection<File>> fetchGBFiles(final Format format, final String extension) {
		return new Function<List<String>, Collection<File>>() {
			@Override
			public Collection<File> apply(final List<String> ids) {
				final ListenableFuture<Collection<File>> future = (!ids.isEmpty() ? TaskRunner.INSTANCE.submit(fetchTask(ids, format, extension)) 
						: immediateFuture(Collections.checkedCollection(new ArrayList<File>(), File.class)));
				try {
					return future.get(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (Exception e) {
					throw new IllegalStateException("Failed to fecth sequences from GenBank using format: " + format, e);
				}
			}
		};
	}

	private static Callable<Collection<File>> fetchTask(final List<String> ids, final Format format, final String extension) {
		return new Callable<Collection<File>>() {
			@Override
			public Collection<File> call() throws Exception {
				File tmpDir = null;
				boolean shouldClean = false;
				try {
					tmpDir = createTempDirectory("tmp_sequences").toFile();
					efetch(ids, 0, EntrezHelper.MAX_RECORDS_FETCHED, tmpDir, format);
					final Collection<File> files = listFiles(tmpDir, new String[] { extension }, false);
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
		};
	}

	private static Function<Collection<File>, Integer> importGBFiles(final Format format) {
		switch (format) {
		case GB_SEQ_XML:
			return importGBSeqXMLFiles();
		default:
			throw new IllegalStateException("Unsupported format: " + format);
		}
	}

	private static Function<Collection<File>, Integer> importGBSeqXMLFiles() {
		return new Function<Collection<File>, Integer>() {
			@Override
			public Integer apply(final Collection<File> files) {
				final List<ListenableFuture<String>> futures = newArrayList();
				final Path dir = ConfigurationManager.INSTANCE.getGenBankDir(Format.GB_SEQ_XML).toPath();
				for (final File file : files) {
					final ListenableFuture<String> future = TaskRunner.INSTANCE.submit(new Callable<String>() {
						@Override
						public String call() throws Exception {
							try {
								final Path source = file.toPath();
								final Path target = dir.resolve(source.getFileName());
								copy(source, target, REPLACE_EXISTING);
								LOGGER.trace("Copy GBSeqXML file to: " + target.toString());

								// TODO

								return target.toString();
							} finally {
								deleteQuietly(file);
							}
						}
					});
					futures.add(future);
				}
				final ListenableFuture<List<String>> combinedFuture = allAsList(futures);
				try {
					final List<String> list = combinedFuture.get(DB_IMPORT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
					return list != null ? list.size() : 0;
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		};
	}

}