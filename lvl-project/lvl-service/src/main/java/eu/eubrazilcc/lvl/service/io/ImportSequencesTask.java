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

import static com.google.common.base.Joiner.on;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.union;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.ListenableFutureTask.create;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_FETCHED;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_LISTED;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.NUCLEOTIDE_DB;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.PHLEBOTOMUS_QUERY;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.efetch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.esearch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.GB_SEQ_XML;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getCount;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getIds;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getPubMedIds;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.parseSequence;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.SEQUENCES;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.ESearchResult;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.io.filter.NewReferenceFilter;
import eu.eubrazilcc.lvl.service.io.filter.RecordFilter;

/**
 * Discovers and imports new sequences in the LVL collection. Sequences are discovered from public databases, such as GenBank. Sequence
 * references are also imported (when available). To this end, this class creates an instance of {@link ImportPublicationsTask}, which
 * uses a specialized form of the {@code insert} method that avoids failing when a duplicate record is found in the database, supporting
 * concurrent use from different threads without expensive synchronization.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ImportSequencesTask extends CancellableTask<Integer> {

	private final static Logger LOGGER = getLogger(ImportSequencesTask.class);

	public final static ImmutableList<String> DEFAULT_DATA_SOURCES = of(GENBANK);

	private ImmutableList<RecordFilter> filters = of();

	private AtomicInteger pending = new AtomicInteger(0);
	private AtomicInteger fetched = new AtomicInteger(0);

	public ImportSequencesTask() {
		this.task = create(importSequencesTask());
	}

	public ImmutableList<RecordFilter> getFilters() {
		return filters;
	}

	public void setFilters(final Iterable<RecordFilter> filters) {
		final ImmutableList.Builder<RecordFilter> builder = new ImmutableList.Builder<RecordFilter>();
		this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
	}

	/**
	 * Imports sequences from default, external databases into the application's database.
	 */
	private Callable<Integer> importSequencesTask() {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				LOGGER.info("Importing new sequences from: " + DEFAULT_DATA_SOURCES);
				int importedSequencesCount = 0;
				final File tmpDir = createTmpDir();
				try {
					final List<ListenableFuture<Integer>> futures = newArrayList();
					for (final String dataSource : DEFAULT_DATA_SOURCES) {						
						futures.addAll(importSequences(dataSource, tmpDir));
					}
					final ListenableFuture<List<Integer>> futuresList = allAsList(futures);
					final List<Integer> results = futuresList.get(); // TODO
					if(results != null) {
						for (final Integer item : results) {
							if (item != null) {
								importedSequencesCount += (item != null ? item : 0);
							}
						}
					}
					setStatus(importedSequencesCount + " new sequences were imported from: " + on(", ").join(DEFAULT_DATA_SOURCES));
				} catch (Exception e) {
					setHasErrors(true);
					setStatus("Error while importing sequences: some sequences might have been skipped to avoid data integrity problems.");
					NOTIFICATION_MANAGER.broadcast(Notification.builder()
							.scope(SEQUENCES)
							.message("Error while importing sequences: " + e.getMessage()).build());
					LOGGER.error("Error while importing sequences", e);
				} finally {					
					deleteQuietly(tmpDir);
				}
				NOTIFICATION_MANAGER.broadcast(Notification.builder()
						.scope(SEQUENCES)
						.message(importedSequencesCount + " new sequences were imported from: " + on(", ").join(DEFAULT_DATA_SOURCES)).build());
				LOGGER.info(importedSequencesCount + " new sequences were imported from: " + on(", ").join(DEFAULT_DATA_SOURCES));
				return new Integer(importedSequencesCount);
			}			
		};
	}

	/**
	 * Imports sequences from external databases into the application's database.
	 * @param dataSource - source database, for example, GenBank
	 */
	private List<ListenableFuture<Integer>> importSequences(final String dataSource, final File tmpDir) {
		checkArgument(isNotBlank(dataSource), "Uninitialized data source");
		if (GENBANK.equals(dataSource)) {
			return importGenBankSequences(tmpDir);
		} else {
			throw new IllegalArgumentException("Unsupported data source: " + dataSource);
		}
	}

	private List<ListenableFuture<Integer>> importGenBankSequences(final File tmpDir) {		
		final List<ListenableFuture<Integer>> futures = newArrayList();
		int esearchCount = -1, retstart = 0, count = 0, retries = 0;
		final int retmax = MAX_RECORDS_LISTED;		
		do {
			setStatus("Searching GenBank for sequence identifiers");
			try {
				final ESearchResult result = esearch(NUCLEOTIDE_DB, PHLEBOTOMUS_QUERY, retstart, retmax);
				if (esearchCount < 0) {
					esearchCount = getCount(result);
				}
				final List<String> ids = getIds(result);
				count = ids.size();
				futures.add(TASK_RUNNER.submit(fetchGenBankSequences(ids, tmpDir, GB_SEQ_XML, "xml")));
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
				setStatus("Finding missing sequences between GenBank and the local collection");
				// filter out the sequence that are already stored in the database, creating a new set
				// with the identifiers that are missing from the database. Using a set ensures that 
				// duplicate identifiers are also removed from the original list
				final List<String> ids2 = from(ids).transform(new Function<String, String>() {
					@Override
					public String apply(final String id) {
						String result = id;
						for (int i = 0; i < filters.size() && result != null; i++) {
							final RecordFilter filter = filters.get(i);
							if (filter.canBeApplied(GENBANK)) {
								result = filters.get(i).filterById(id);
							}
						}
						return result;
					}
				}).filter(notNull()).toSet().asList();
				if (ids2.size() > 0) {
					setStatus("Fetching sequences from GenBank");
					// update progress
					int pendingCount = pending.addAndGet(ids2.size());
					setProgress(100.0d * fetched.get() / pendingCount);
					// fetch sequence files
					final Path tmpDir2 = createTempDirectory(tmpDir.toPath(), "fetch_task_");
					efetch(ids2, 0, MAX_RECORDS_FETCHED, tmpDir2.toFile(), format);
					// copy sequence files to their final location and import them to the database
					final Path seqPath = CONFIG_MANAGER.getGenBankDir(format).toPath();
					Set<String> pmids = newHashSet();
					for (final String id : ids2) {
						setStatus("Importing GenBank sequences into local collection");
						final Path source = tmpDir2.resolve(id + "." + extension);
						try {
							// copy sequence to storage						
							final Path target = seqPath.resolve(source.getFileName());
							copy(source, target, REPLACE_EXISTING);
							LOGGER.info("New GBSeqXML file stored: " + target.toString());
							// insert sequence in the database
							final GBSeq gbSeq = GBSEQ_XMLB.typeFromFile(target.toFile());
							final Sequence sequence = parseSequence(gbSeq);
							SEQUENCE_DAO.insert(sequence);
							efetchCount++;							
							// update progress							
							int fetchedCount = fetched.incrementAndGet();
							setProgress(100.0d * fetchedCount / pending.get());							
							// extract references from the sequence
							pmids = union(pmids, getPubMedIds(gbSeq));
						} catch (Exception e) {
							LOGGER.warn("Failed to import sequence from file: " + source.getFileName(), e);
						} finally {
							deleteQuietly(source.toFile());
						}
					}
					// import references
					final ImportPublicationsTask importPublicationsTask = ImportPublicationsTask.builder()
							.filter(NewReferenceFilter.builder().build())
							.parent(getUuid())
							.ids(pmids)
							.build();
					// TODO TASK_RUNNER.execute(importPublicationsTask);
					// TODO TASK_STORAGE.add(importPublicationsTask);					
				}
				checkState(ids2.size() == efetchCount, "No all sequences were imported");
				return efetchCount;
			}
		};
	}

	private static File createTmpDir() {
		File tmpDir = null;
		try {
			tmpDir = createTempDirectory("tmp_sequences_").toFile();
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create temporary directory", e);
		}
		return tmpDir;
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.addValue(super.toString())
				.toString();
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final ImportSequencesTask instance = new ImportSequencesTask();

		public Builder filter(final RecordFilter filter) {
			instance.setFilters(of(filter));
			return this;
		}

		public Builder filters(final Iterable<RecordFilter> filters) {
			instance.setFilters(filters);
			return this;
		}

		public ImportSequencesTask build() {
			return instance;
		}

	}

}