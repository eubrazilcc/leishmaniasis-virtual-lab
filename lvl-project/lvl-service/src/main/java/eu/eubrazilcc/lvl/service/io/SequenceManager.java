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
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.allAsList;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.efetch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.esearch;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getCount;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getIds;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.GB_SEQXML;
import static eu.eubrazilcc.lvl.core.xml.NCBIXmlBinder.parse;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.ESearchResult;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;

/**
 * Manages the sequences in the LVL collection, participating in the discovering, importation
 * and update of the sequences in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceManager {

	private final static Logger LOGGER = getLogger(SequenceManager.class);

	public final static ImmutableList<String> DEFAULT_DATA_SOURCES = of(DataSource.GENBANK);

	private ImmutableList<SequenceFilter> filters = of();

	public SequenceManager() { }	

	public ImmutableList<SequenceFilter> getFilters() {
		return filters;
	}

	public void setFilters(final Iterable<SequenceFilter> filters) {
		final ImmutableList.Builder<SequenceFilter> builder = new ImmutableList.Builder<SequenceFilter>();
		this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
	}

	/**
	 * Imports sequences from default, external databases into the application's database.
	 */
	public void importSequences() {
		final File tmpDir = createTmpDir();
		final List<ListenableFuture<Integer>> futures = newArrayList();		
		for (final String dataSource : DEFAULT_DATA_SOURCES) {
			futures.addAll(importSequences(dataSource, tmpDir));
		}
		Futures.addCallback(allAsList(futures), new FutureCallback<List<Integer>>() {
			@Override
			public void onSuccess(final List<Integer> result) {
				deleteQuietly(tmpDir);
				checkState(result != null, "No sequences imported");
				int efetchCount = 0;
				for (int i = 0; i < result.size(); i++) {
					efetchCount += (result.get(i) != null ? result.get(i) : 0);
				}
				LOGGER.info(efetchCount + " sequences imported");
				// TODO: send notification, update database
			}
			@Override
			public void onFailure(final Throwable cause) {
				deleteQuietly(tmpDir);
				LOGGER.error("Error while importing sequences", cause);
				// TODO: send notification, update database
			}				
		});
		// TODO : final List<Integer> list = combinedFuture.get();
	}

	private File createTmpDir() {
		File tmpDir = null;
		try {
			tmpDir = createTempDirectory("tmp_sequences_").toFile();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create temporary directory", e);
		}
		return tmpDir;
	}

	/**
	 * Imports sequences from external databases into the application's database.
	 * @param dataSource - source database, for example, GenBank
	 */
	public List<ListenableFuture<Integer>> importSequences(final String dataSource, final File tmpDir) {
		checkArgument(isNotBlank(dataSource), "Uninitialized data source");
		if (DataSource.GENBANK.equals(dataSource)) {
			return importGenBankSequences(tmpDir);
		} else {
			throw new IllegalArgumentException("Unsupported data source: " + dataSource);
		}
	}

	private List<ListenableFuture<Integer>> importGenBankSequences(final File tmpDir) {
		final List<ListenableFuture<Integer>> futures = newArrayList();
		int esearchCount = -1, retstart = 0, count = 0, retries = 0;
		final int retmax = EntrezHelper.MAX_RECORDS_LISTED;
		do {
			try {
				final ESearchResult result = esearch(EntrezHelper.PHLEBOTOMUS_QUERY, retstart, retmax);
				if (esearchCount < 0) {
					esearchCount = getCount(result);
				}
				final List<String> ids = getIds(result);
				count = ids.size();
				futures.add(TASK_RUNNER.submit(fetchGenBankSequences(ids, tmpDir, Format.GB_SEQ_XML, "xml")));
				LOGGER.trace("Listing Ids (start=" + retstart + ", max=" + retmax + ") produced " + count + " new records");
				retstart += count;
			} catch (Exception e) {
				if (++retries > 3) {
					throw new IllegalStateException("Failed to import GenBank sequences", e);
				}
			}
		} while (count > 0 && retstart < esearchCount);
		return futures;
	}

	private Callable<Integer> fetchGenBankSequences(final List<String> ids, final File tmpDir,
			final Format format, final String extension) {
		return new Callable<Integer>() {
			private int efetchCount = 0;
			@Override
			public Integer call() throws Exception {
				// filter out the sequence that are already stored in the database, creating a new set
				// with the identifiers that are missing from the database. Using a set ensures that 
				// duplicate identifiers are also removed from the original list
				final List<String> ids2 = from(ids).transform(new Function<String, String>() {
					@Override
					public String apply(final String id) {
						String result = id;
						for (int i = 0; i < filters.size() && result != null; i++) {
							final SequenceFilter filter = filters.get(i);
							if (filter.canBeApplied(DataSource.GENBANK)) {
								result = filters.get(i).filterById(id);
							}
						}
						return result;
					}
				}).filter(notNull()).toSet().asList();
				if (ids2.size() > 0) {
					// fetch sequence files
					final Path tmpDir2 = createTempDirectory(tmpDir.toPath(), "fetch_task_");
					efetch(ids2, 0, EntrezHelper.MAX_RECORDS_FETCHED, tmpDir2.toFile(), format);
					// copy sequence files to their final location and import them to the database
					final Path seqPath = CONFIG_MANAGER.getGenBankDir(format).toPath();
					for (final String id : ids2) {
						final Path source = tmpDir2.resolve(id + "." + extension);
						try {
							// copy sequence to storage						
							final Path target = seqPath.resolve(source.getFileName());
							copy(source, target, REPLACE_EXISTING);
							LOGGER.info("New GBSeqXML file stored: " + target.toString());
							// insert sequence in the database
							final Sequence sequence = parse((GBSeq)GB_SEQXML.typeFromFile(target.toFile()));
							SEQUENCE_DAO.insert(sequence);
							efetchCount++;
						} catch (Exception e) {
							LOGGER.warn("Failed to import sequence from file: " + source.getFileName(), e);
						} finally {
							deleteQuietly(source.toFile());
						}
					}
				}
				checkState(ids2.size() == efetchCount, "No all sequences were imported");
				return efetchCount;
			}
		};
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final SequenceManager sequenceManager = new SequenceManager();

		public Builder filter(final SequenceFilter filter) {
			sequenceManager.setFilters(ImmutableList.of(filter));
			return this;
		}

		public Builder filters(final Iterable<SequenceFilter> filters) {
			sequenceManager.setFilters(filters);
			return this;
		}

		public SequenceManager build() {
			return sequenceManager;
		}

	}

}