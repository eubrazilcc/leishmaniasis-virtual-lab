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
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Iterables.partition;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.util.concurrent.Futures.successfulAsList;
import static com.google.common.util.concurrent.ListenableFutureTask.create;
import static eu.eubrazilcc.lvl.core.DataSource.PUBMED;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_FETCHED;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_LISTED;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.efetch;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format.PUBMED_XML;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.PUBMED_XMLB;
import static eu.eubrazilcc.lvl.core.xml.PubMedXmlBinder.parseArticle;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.dao.ReferenceDAO.REFERENCE_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.PUBLICATIONS;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.io.FileUtils.deleteQuietly;
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
import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;
import eu.eubrazilcc.lvl.core.xml.ncbi.pubmed.PubmedArticle;
import eu.eubrazilcc.lvl.service.io.filter.RecordFilter;

/**
 * Imports publications from public databases, such as PubMed. This class receives a list of identifiers from other task and uses a
 * specialized form of the {@code insert} method that avoids failing when a duplicate record is found in the database, supporting
 * concurrent use from different threads without expensive synchronization.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ImportPublicationsTask extends CancellableTask<Integer> {

	private static final Logger LOGGER = getLogger(ImportPublicationsTask.class);

	public static final ImmutableList<String> DATABASES = of(PUBMED);

	public static final long TIMEOUT_MINUTES = 60l;

	private ImmutableList<RecordFilter> filters = of();

	private final ImmutableList<String> pmids;
	
	private AtomicInteger pending = new AtomicInteger(0);
	private AtomicInteger fetched = new AtomicInteger(0);

	public ImportPublicationsTask(final List<String> pmids) {
		this.pmids = pmids != null ? copyOf(pmids) : new ImmutableList.Builder<String>().build();
		this.task = create(importPublicationsTask());
	}

	public ImmutableList<RecordFilter> getFilters() {
		return filters;
	}

	public void setFilters(final Iterable<RecordFilter> filters) {
		final ImmutableList.Builder<RecordFilter> builder = new ImmutableList.Builder<RecordFilter>();
		this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
	}

	/**
	 * Imports publications from external databases into the application's database.
	 */
	private Callable<Integer> importPublicationsTask() {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				LOGGER.info("Importing new publications from: " + DATABASES);
				int count = 0;
				final File tmpDir = createTmpDir();
				try {
					final List<ListenableFuture<Integer>> subTasks = newArrayList();
					for (final String db : DATABASES) {
						if (PUBMED.equals(db)) {
							subTasks.addAll(importPubMedSubTasks(tmpDir));
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
							setStatus("Error while importing publications: not all publications were imported");
						}
					}
				} catch (Exception e) {
					setHasErrors(true);
					setStatus("Uncaught error while importing publications: not all publications were imported");
					LOGGER.error("Uncaught error while importing publications", e);
				} finally {
					deleteQuietly(tmpDir);
				}
				final String msg = count + " new publications were imported from: " + on(", ").join(DATABASES);				
				if (!hasErrors()) {
					setStatus(msg);
					LOGGER.info(msg);
				} else {
					LOGGER.warn(msg + " - errors reported");
				}
				NOTIFICATION_MANAGER.broadcast(Notification.builder()
						.scope(PUBLICATIONS)
						.message(msg).build());
				// unregister this task before returning the result to the execution service
				TASK_STORAGE.remove(getUuid());
				return new Integer(count);
			}
		};
	}

	private List<ListenableFuture<Integer>> importPubMedSubTasks(final File tmpDir) {
		final List<ListenableFuture<Integer>> subTasks = newArrayList();
		final Set<String> deduplicated = newHashSet(pmids);		
		final Iterable<List<String>> subsets = partition(deduplicated, MAX_RECORDS_LISTED);
		for (final List<String> subset : subsets) {
			subTasks.add(TASK_RUNNER.submit(importPubMedSubTask(subset, tmpDir, PUBMED_XML, "xml")));
			LOGGER.trace("Partition produced " + subset.size() + " new records");
		}
		return subTasks;
	}

	private Callable<Integer> importPubMedSubTask(final List<String> ids, final File tmpDir, final Format format, final String extension) {
		return new Callable<Integer>() {
			private int efetchCount = 0;
			@Override			
			public Integer call() throws Exception {
				setStatus("Finding missing publications between PubMed and the local collection");
				// filter out the publications that are already stored in the database, creating a new set
				// with the identifiers that are missing from the database. Using a set ensures that 
				// duplicate identifiers are also removed from the original list
				final List<String> ids2 = from(ids).transform(new Function<String, String>() {
					@Override
					public String apply(final String id) {
						String result = id;
						for (int i = 0; i < filters.size() && result != null; i++) {
							final RecordFilter filter = filters.get(i);
							if (filter.canBeApplied(PUBMED)) {
								result = filters.get(i).filterById(id);
							}
						}
						return result;
					}
				}).filter(notNull()).toSet().asList();
				if (ids2.size() > 0) {
					setStatus("Fetching publications from PubMed");
					// update progress
					int pendingCount = pending.addAndGet(ids2.size());
					setProgress(100.0d * fetched.get() / pendingCount);
					// fetch sequence files
					final Path tmpDir2 = createTempDirectory(tmpDir.toPath(), "fetch_pub_task_");
					efetch(ids2, 0, MAX_RECORDS_FETCHED, tmpDir2.toFile(), format);
					// copy publication files to their final location and import them to the database
					final Path seqPath = CONFIG_MANAGER.getPubMedDir(format).toPath();
					for (final String id : ids2) {
						setStatus("Importing PubMed publications into local collection");
						final Path source = tmpDir2.resolve(id + "." + extension);
						try {
							// copy publication to storage						
							final Path target = seqPath.resolve(source.getFileName());
							copy(source, target, REPLACE_EXISTING);
							LOGGER.info("New PubMed file stored: " + target.toString());
							// insert publication in the database
							final PubmedArticle pmArticle = PUBMED_XMLB.typeFromFile(target.toFile());
							final Reference reference = parseArticle(pmArticle);
							REFERENCE_DAO.insert(reference, true);							
							efetchCount++;							
							// update progress
							int fetchedCount = fetched.incrementAndGet();
							setProgress(100.0d * fetchedCount / pending.get());							
						} catch (Exception e) {
							LOGGER.warn("Failed to import publication from file: " + source.getFileName(), e);
						}
					}
				}
				checkState(ids2.size() == efetchCount, "No all publications were imported");
				return efetchCount;
			}
		};
	}

	private static File createTmpDir() {
		File tmpDir = null;
		try {
			tmpDir = createTempDirectory("tmp_pub_").toFile();
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

		private ImmutableList<RecordFilter> filters = of();
		private ImmutableList<String> pmids = of();		

		public Builder filter(final RecordFilter filter) {
			this.filters = of(filter);
			return this;
		}

		public Builder filters(final Iterable<RecordFilter> filters) {
			this.filters = copyOf(filters);
			return this;
		}

		public Builder pmids(final Iterable<String> pmids) {
			this.pmids = copyOf(pmids);
			return this;
		}

		public ImportPublicationsTask build() {
			final ImportPublicationsTask task = new ImportPublicationsTask(pmids);
			task.setFilters(filters);
			return task;
		}

	}

}