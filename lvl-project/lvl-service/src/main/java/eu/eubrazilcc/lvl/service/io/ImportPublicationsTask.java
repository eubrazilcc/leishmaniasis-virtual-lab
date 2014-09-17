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
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.ListenableFutureTask.create;
import static eu.eubrazilcc.lvl.core.DataSource.PUBMED;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.MAX_RECORDS_FETCHED;
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
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
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
 * Imports publications from public databases, such as PubMed. This class receives a list of identifiers from other task, which
 * also passes its identifier to link this task with the parent task that creates this one. As a consequence, this class uses a
 * specialized form of the {@code insert} method that avoids failing when a duplicate record is found in the database, supporting
 * concurrent use from different threads without expensive synchronization.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ImportPublicationsTask extends CancellableTask<Integer> {

	private final static Logger LOGGER = getLogger(ImportPublicationsTask.class);

	public final static ImmutableList<String> DEFAULT_DATA_SOURCES = of(PUBMED);

	private ImmutableList<RecordFilter> filters = of();

	private final ImmutableList<String> ids;

	private AtomicInteger pending = new AtomicInteger(0);
	private AtomicInteger fetched = new AtomicInteger(0);

	public ImportPublicationsTask(final UUID parent, final ImmutableList<String> ids) {
		super(parent);
		this.ids = copyOf(ids);
		this.task = create(importPublicationsTask());
	}

	public ImmutableList<RecordFilter> getFilters() {
		return filters;
	}

	public void setFilters(final Iterable<RecordFilter> filters) {
		final ImmutableList.Builder<RecordFilter> builder = new ImmutableList.Builder<RecordFilter>();
		this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
	}

	private Callable<Integer> importPublicationsTask() {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				LOGGER.info("Importing new publications from: " + DEFAULT_DATA_SOURCES);
				int importedPublicationsCount = 0;
				final File tmpDir = createTmpDir();
				try {
					final List<ListenableFuture<Integer>> futures = newArrayList();
					for (final String dataSource : DEFAULT_DATA_SOURCES) {						
						futures.addAll(importPublications(dataSource, tmpDir));
					}
					final ListenableFuture<List<Integer>> futuresList = allAsList(futures);
					final List<Integer> results = futuresList.get();
					if(results != null) {
						for (final Integer item : results) {
							if (item != null) {
								importedPublicationsCount += (item != null ? item : 0);
							}
						}
					}
					setStatus(importedPublicationsCount + " new publications were imported from: " + on(", ").join(DEFAULT_DATA_SOURCES));					
				} catch (Exception e) {
					setHasErrors(true);
					setStatus("Error while importing publications: some publications might have been skipped to avoid data integrity problems.");
					NOTIFICATION_MANAGER.broadcast(Notification.builder()
							.scope(PUBLICATIONS)
							.message("Error while importing publications: " + e.getMessage()).build());
					LOGGER.error("Error while importing publications", e);
				} finally {					
					deleteQuietly(tmpDir);
				}
				NOTIFICATION_MANAGER.broadcast(Notification.builder()
						.scope(PUBLICATIONS)
						.message(importedPublicationsCount + " new publications were imported from: " + on(", ").join(DEFAULT_DATA_SOURCES)).build());
				LOGGER.info(importedPublicationsCount + " new publications were imported from: " + on(", ").join(DEFAULT_DATA_SOURCES));				
				return new Integer(importedPublicationsCount);
			}
		};
	}

	/**
	 * Imports publications from external databases into the application's database.
	 * @param dataSource - source database, for example, PubMed
	 */
	private List<ListenableFuture<Integer>> importPublications(final String dataSource, final File tmpDir) {
		checkArgument(isNotBlank(dataSource), "Uninitialized data source");
		if (PUBMED.equals(dataSource)) {
			return importPubMedPublications(tmpDir);
		} else {
			throw new IllegalArgumentException("Unsupported data source: " + dataSource);
		}
	}

	private List<ListenableFuture<Integer>> importPubMedPublications(final File tmpDir) {
		final List<ListenableFuture<Integer>> futures = newArrayList();		
		futures.add(TASK_RUNNER.submit(fetchPubMedPublications(ids, tmpDir, PUBMED_XML, "xml")));		
		return futures;
	}

	private Callable<Integer> fetchPubMedPublications(final List<String> ids, final File tmpDir,
			final Format format, final String extension) {
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
					// fetch article files
					final Path tmpDir2 = createTempDirectory(tmpDir.toPath(), "fetch_task_");
					efetch(ids2, 0, MAX_RECORDS_FETCHED, tmpDir2.toFile(), format);
					// copy sequence files to their final location and import them to the database
					final Path seqPath = CONFIG_MANAGER.getPubMedDir(format).toPath();
					for (final String id : ids2) {
						setStatus("Importing PubMed publications into local collection");
						final Path source = tmpDir2.resolve(id + "." + extension);
						try {
							// copy publication to storage						
							final Path target = seqPath.resolve(source.getFileName());
							copy(source, target, REPLACE_EXISTING);
							LOGGER.info("New PubMed XML file stored: " + target.toString());
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
						} finally {
							deleteQuietly(source.toFile());
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
			tmpDir = createTempDirectory("tmp_publications_").toFile();
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
		private UUID parent = null;
		private ImmutableList<String> ids = of();		

		public Builder filter(final RecordFilter filter) {
			this.filters = of(filter);
			return this;
		}

		public Builder filters(final Iterable<RecordFilter> filters) {
			this.filters = copyOf(filters);
			return this;
		}

		public Builder parent(final UUID parent) {
			this.parent = parent;
			return this;
		}

		public Builder ids(final Iterable<String> ids) {
			this.ids = copyOf(ids);
			return this;
		}

		public ImportPublicationsTask build() {
			final ImportPublicationsTask task = new ImportPublicationsTask(parent, ids);
			task.setFilters(filters);
			return task;
		}

	}

}