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
import static com.google.common.util.concurrent.Futures.successfulAsList;
import static com.google.common.util.concurrent.ListenableFutureTask.create;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_FETCHED;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_LISTED;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.NUCLEOTIDE_DB;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.efetch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.esearch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.GB_SEQ_XML;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getCount;
import static eu.eubrazilcc.lvl.core.xml.ESearchXmlBinder.getIds;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XMLB;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.getPubMedIds;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.parseSequence;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.DATA_CURATOR_ROLE;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.synchronizedList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.ESearchResult;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.io.filter.NewReferenceFilter;
import eu.eubrazilcc.lvl.service.io.filter.RecordFilter;
import eu.eubrazilcc.lvl.storage.dao.SequenceDAO;

/**
 * Discovers and imports new sequences in the LVL collection. Sequences are discovered from public databases, such as GenBank. Sequence
 * references are also imported (when available). To this end, this class creates an instance of {@link ImportPublicationsTask}, which
 * uses a specialized form of the {@code insert} method that avoids failing when a duplicate record is found in the database, supporting
 * concurrent use from different threads without expensive synchronization.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ImportSequencesTask<T extends Sequence> extends CancellableTask<Integer> {

	private static final Logger LOGGER = getLogger(ImportSequencesTask.class);

	public static final ImmutableList<String> DATABASES = of(GENBANK);

	public static final long TIMEOUT_MINUTES = 60l;

	private ImmutableList<RecordFilter> filters = of();

	private final AtomicInteger pending = new AtomicInteger(0);
	private final AtomicInteger fetched = new AtomicInteger(0);

	private final List<String> pmids = synchronizedList(new ArrayList<String>());

	private UUID importPublicationsTaskId = null;

	private final String query;
	private final Sequence.Builder<T> builder;
	private final SequenceDAO<T> dao;

	public ImportSequencesTask(final String query, final Sequence.Builder<T> builder, final SequenceDAO<T> dao) {
		this.query = query;
		this.builder = builder;
		this.dao = dao;
		this.task = create(importSequencesTask());
	}

	public ImmutableList<RecordFilter> getFilters() {		
		return filters;
	}

	public void setFilters(final Iterable<RecordFilter> filters) {
		final ImmutableList.Builder<RecordFilter> builder = new ImmutableList.Builder<RecordFilter>();
		this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
	}	

	public @Nullable UUID getImportPublicationsTaskId() {
		return importPublicationsTaskId;
	}

	/**
	 * Imports sequences from external databases into the application's database.
	 */
	private Callable<Integer> importSequencesTask() {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				LOGGER.info("Importing new sequences from: " + DATABASES);
				int count = 0;
				final File tmpDir = createTmpDir();
				try {
					final List<ListenableFuture<Integer>> subTasks = newArrayList();
					for (final String db : DATABASES) {
						if (GENBANK.equals(db)) {
							subTasks.addAll(importGenBankSubTasks(tmpDir));
						} else {
							throw new IllegalArgumentException("Unsupported database: " + db);
						}
					}
					final ListenableFuture<List<Integer>> globalTask = successfulAsList(subTasks);					
					final List<Integer> results = globalTask.get(TIMEOUT_MINUTES, MINUTES);
					for (final Integer result : results) {
						if (result != null) {
							count += result;
						} else {
							setHasErrors(true);
							setStatus("Error while importing sequences: not all sequences were imported");
						}
					}
				} catch (InterruptedException ie) {					
					// ignore and propagate
					LOGGER.warn("Sequence import was interrupted, exiting");
					throw ie;
				} catch (Exception e) {
					setHasErrors(true);
					setStatus("Uncaught error while importing sequences: not all sequences were imported");
					LOGGER.error("Uncaught error while importing sequences", e);
				} finally {
					deleteQuietly(tmpDir);
				}
				final String msg = count + " new sequences were imported from: " + on(", ").join(DATABASES);				
				if (!hasErrors()) {
					setStatus(msg);
					LOGGER.info(msg);
				} else {
					LOGGER.warn(msg + " - errors reported");
				}
				NOTIFICATION_MANAGER.broadcast(Notification.builder()
						.scope(DATA_CURATOR_ROLE)
						.message(msg).build());			
				// schedule publication import
				final ImportPublicationsTask importPublicationsTask = ImportPublicationsTask.builder()
						.filter(NewReferenceFilter.builder().build())
						.pmids(pmids)
						.build();
				importPublicationsTaskId = importPublicationsTask.getUuid();
				TASK_RUNNER.execute(importPublicationsTask);
				TASK_STORAGE.add(importPublicationsTask);				
				// unregister this task before returning the result to the execution service
				TASK_STORAGE.remove(getUuid());
				return new Integer(count);
			}
		};
	}

	private List<ListenableFuture<Integer>> importGenBankSubTasks(final File tmpDir) {
		final List<ListenableFuture<Integer>> subTasks = newArrayList();
		int esearchCount = -1, retstart = 0, count = 0, retries = 0;
		final int retmax = MAX_RECORDS_LISTED;
		do {
			setStatus("Searching GenBank for sequence identifiers");
			try {
				final ESearchResult result = esearch(NUCLEOTIDE_DB, query, retstart, retmax);
				if (esearchCount < 0) {
					esearchCount = getCount(result);
				}
				final List<String> ids = getIds(result);
				count = ids.size();
				subTasks.add(TASK_RUNNER.submit(importGenBankSubTask(ids, tmpDir, GB_SEQ_XML, "xml")));
				LOGGER.trace("Listing Ids (start=" + retstart + ", max=" + retmax + ") produced " + count + " new records. Query: " + query);
				retstart += count;
			} catch (Exception e) {
				if (++retries > 3) {
					throw new IllegalStateException("Failed to import GenBank sequences", e);
				}
			}
		} while (count > 0 && retstart < esearchCount);
		return subTasks;
	}

	private Callable<Integer> importGenBankSubTask(final List<String> ids, final File tmpDir, final Format format, final String extension) {
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
					final Path tmpDir2 = createTempDirectory(tmpDir.toPath(), "fetch_seq_task_");
					efetch(ids2, 0, MAX_RECORDS_FETCHED, tmpDir2.toFile(), format);
					// copy sequence files to their final location and import them to the database
					final Path seqPath = CONFIG_MANAGER.getGenBankDir(format).toPath();
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
							final T sequence = parseSequence(gbSeq, builder);
							dao.insert(sequence);
							efetchCount++;
							// update progress
							int fetchedCount = fetched.incrementAndGet();
							setProgress(100.0d * fetchedCount / pending.get());							
							// extract references from the sequence
							pmids.addAll(getPubMedIds(gbSeq));
						} catch (Exception e) {
							LOGGER.warn("Failed to import sequence from file: " + source.getFileName(), e);
						}
					}
				}
				checkState(ids2.size() == efetchCount, "No all sequences were imported");
				return efetchCount;
			}
		};
	}

	private static File createTmpDir() {
		File tmpDir = null;
		try {
			tmpDir = createTempDirectory("tmp_seq_").toFile();
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

	public static Builder<Leishmania>leishmaniaBuilder() {
		return new Builder<Leishmania>();
	}

	public static Builder<Sandfly> sandflyBuilder() {
		return new Builder<Sandfly>();
	}

	public static class Builder<T extends Sequence> {

		private String query;
		private Sequence.Builder<T> builder;
		private SequenceDAO<T> dao;		
		private ImmutableList<RecordFilter> filters;

		public Builder<T> query(final String query) {
			this.query = query;
			return this;
		}

		public Builder<T> dao(final SequenceDAO<T> dao) {
			this.dao = dao;
			return this;
		}

		public Builder<T> builder(final Sequence.Builder<T> builder) {
			this.builder = builder;
			return this;
		}

		public Builder<T> filter(final RecordFilter filter) {
			this.filters = of(filter);
			return this;
		}

		public Builder<T> filters(final Iterable<RecordFilter> filters) {
			final ImmutableList.Builder<RecordFilter> builder = new ImmutableList.Builder<RecordFilter>();
			this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
			return this;
		}

		public ImportSequencesTask<T> build() {
			checkArgument(isNotBlank(query), "Uninitialized or invalid database query");
			checkArgument(builder != null, "Uninitialized or invalid sequence builder");
			checkArgument(dao != null, "Uninitialized or invalid sequence DAO");
			final ImportSequencesTask<T> instance = new ImportSequencesTask<T>(query, builder, dao);
			instance.setFilters(filters);
			return instance;
		}

	}

}