/*
 * Copyright 2014-2015 EUBrazilCC (EU‐Brazil Cloud Connect)
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
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.util.concurrent.Futures.successfulAsList;
import static com.google.common.util.concurrent.ListenableFutureTask.create;
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.parseSample;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.DATA_CURATOR_ROLE;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.LeishmaniaSample;
import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Sample;
import eu.eubrazilcc.lvl.core.SandflySample;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.core.tapir.SpeciesLinkConnector;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecordSet;
import eu.eubrazilcc.lvl.service.io.filter.RecordFilter;
import eu.eubrazilcc.lvl.storage.dao.SampleDAO;

/**
 * Discovers and imports new samples in the LVL collection. Sequences are discovered from public databases, such as CLIOC and COLFLEB.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ImportSamplesTask<T extends Sample> extends CancellableTask<Integer> {

	private static final Logger LOGGER = getLogger(ImportSamplesTask.class);

	public static final ImmutableList<String> DATABASES = of(CLIOC, COLFLEB);

	public static final long TIMEOUT_MINUTES = 60l;

	private ImmutableList<RecordFilter> filters = of();

	private final AtomicInteger pending = new AtomicInteger(0);
	private final AtomicInteger fetched = new AtomicInteger(0);

	private final Sample.Builder<T> builder;
	private final SampleDAO<T> dao;

	public ImportSamplesTask(final Sample.Builder<T> builder, final SampleDAO<T> dao) {
		this.builder = builder;
		this.dao = dao;
		this.task = create(importSamplesTask());
	}

	public ImmutableList<RecordFilter> getFilters() {		
		return filters;
	}

	public void setFilters(final Iterable<RecordFilter> filters) {
		final ImmutableList.Builder<RecordFilter> builder = new ImmutableList.Builder<RecordFilter>();
		this.filters = (filters != null ? builder.addAll(filters).build() : builder.build());
	}	

	/**
	 * Imports samples from external databases into the application's database.
	 */
	private Callable<Integer> importSamplesTask() {
		return new Callable<Integer>() {
			@Override			
			public Integer call() throws Exception {
				LOGGER.info("Importing new samples from: " + DATABASES);				
				int count = 0;
				try (final SpeciesLinkConnector splink = new SpeciesLinkConnector()) {
					final List<ListenableFuture<Integer>> subTasks = newArrayList();
					for (final String db : DATABASES) {
						if (CLIOC.equals(db)) {
							subTasks.addAll(importSplinkSubTasks(splink, "clioc"));
						} else if (COLFLEB.equals(db)) {
							subTasks.addAll(importSplinkSubTasks(splink, "colfleb"));
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
							setStatus("Error while importing samples: not all samples were imported");
						}
					}
				} catch (InterruptedException ie) {					
					// ignore and propagate
					LOGGER.warn("Sample import was interrupted, exiting");
					throw ie;
				} catch (Exception e) {
					setHasErrors(true);
					setStatus("Uncaught error while importing samples: not all samples were imported");
					LOGGER.error("Uncaught error while importing samples", e);
				}				
				final String msg = count + " new samples were imported from: " + on(", ").join(DATABASES);				
				if (!hasErrors()) {
					setStatus(msg);
					LOGGER.info(msg);
				} else {
					LOGGER.warn(msg + " - errors reported");
				}
				NOTIFICATION_MANAGER.broadcast(Notification.builder()
						.scope(DATA_CURATOR_ROLE)
						.message(msg).build());						
				// unregister this task before returning the result to the execution service
				TASK_STORAGE.remove(getUuid());
				return new Integer(count);
			}
		};
	}

	private List<ListenableFuture<Integer>> importSplinkSubTasks(final SpeciesLinkConnector splink, final String collection) {
		final List<ListenableFuture<Integer>> subTasks = newArrayList();
		setStatus("Counting collection items");
		final int count = (int)splink.count(collection);		
		checkState(count > 0l, "It expected that the collection had elements");
		pending.addAndGet(count);
		final Range<Integer> range = Range.closedOpen(0, count);
		final int STEP = 100;
		int i = 0;
		do {
			subTasks.add(TASK_RUNNER.submit(importSplinkSubTask(splink, i, STEP, collection)));
			i += STEP;
		} while (range.contains(i));
		LOGGER.trace(String.format("Collection %s items count: %d", collection, count));		
		return subTasks;
	}

	private Callable<Integer> importSplinkSubTask(final SpeciesLinkConnector splink, final int start, final int limit, final String collection) {
		return new Callable<Integer>() {
			private int fetchCount = 0;
			private int expected = 0;
			@Override			
			public Integer call() throws Exception {
				setStatus(String.format("Fetching samples from %s", collection));
				try {
					// fetch samples from remote data source
					final SimpleDarwinRecordSet dwcSet = splink.fetch(collection, start, limit);
					// import samples into the local database
					if (dwcSet != null && dwcSet.getSimpleDarwinRecord() != null) {
						expected = dwcSet.getSimpleDarwinRecord().size();
						for (final SimpleDarwinRecord record : dwcSet.getSimpleDarwinRecord()) {
							final T sample = parseSample(record, "clioc".equals(collection) ? CLIOC : COLFLEB, builder);
							// filter out the samples that are already stored in the database
							String catalogNumber = sample.getCatalogNumber();							
							for (int i = 0; i < filters.size() && catalogNumber != null; i++) {
								final RecordFilter filter = filters.get(i);
								if (filter.canBeApplied(GENBANK)) {
									catalogNumber = filters.get(i).filterById(catalogNumber);
								}
							}
							if (catalogNumber != null) {
								dao.insert(sample);
							}
							fetchCount++;
							// update progress
							int fetchedCount = fetched.incrementAndGet();
							setProgress(100.0d * fetchedCount / pending.get());	
						}
					}
				} catch (Exception e) {
					LOGGER.warn(String.format("Failed to import samples from collection: %s", collection), e);
				}
				checkState(expected == fetchCount, "No all samples were imported");				
				return fetchCount;
			}
		};
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.addValue(super.toString())
				.toString();
	}

	/* Fluent API */

	public static Builder<LeishmaniaSample> leishmaniaBuilder() {
		return new Builder<LeishmaniaSample>();
	}

	public static Builder<SandflySample> sandflyBuilder() {
		return new Builder<SandflySample>();
	}

	public static class Builder<T extends Sample> {

		private Sample.Builder<T> builder;
		private SampleDAO<T> dao;		
		private ImmutableList<RecordFilter> filters;

		public Builder<T> dao(final SampleDAO<T> dao) {
			this.dao = dao;
			return this;
		}

		public Builder<T> builder(final Sample.Builder<T> builder) {
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

		public ImportSamplesTask<T> build() {
			checkArgument(builder != null, "Uninitialized or invalid sample builder");
			checkArgument(dao != null, "Uninitialized or invalid sample DAO");
			final ImportSamplesTask<T> instance = new ImportSamplesTask<T>(builder, dao);
			instance.setFilters(filters);
			return instance;
		}

	}

}