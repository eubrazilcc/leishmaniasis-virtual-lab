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

package eu.eubrazilcc.lvl.core.concurrent;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Schedules tasks to run periodically. Tasks are scheduled with a pool of threads for periodic execution 
 * and a {@link ListenableScheduledFuture} is returned to the caller.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum TaskScheduler implements Closeable2 {
	
	INSTANCE;

	private final static Logger LOGGER = getLogger(TaskScheduler.class);

	public static final String THREAD_NAME_PATTERN = "lvl-scheduler-%d";
	private static final int MIN_NUM_THREADS = 2;

	private static final int TIMEOUT_SECS = 20;

	private final ListeningScheduledExecutorService scheduler;

	private AtomicBoolean shouldRun = new AtomicBoolean(false);

	private TaskScheduler() { 
		final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(
				Math.max(MIN_NUM_THREADS, Runtime.getRuntime().availableProcessors()), 
				new ThreadFactoryBuilder()
				.setNameFormat(THREAD_NAME_PATTERN)
				.setDaemon(false)
				.setUncaughtExceptionHandler(TaskUncaughtExceptionHandler.INSTANCE)
				.build());
		scheduledThreadPoolExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
		scheduler = listeningDecorator(scheduledThreadPoolExecutor);
	}

	/**
	 * See description of {@link java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate}
	 */
	public ListenableScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
		checkState(shouldRun.get(), "Task scheduler uninitialized");
		return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	
	/**
	 * See description of {@link java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay}
	 */
	public ListenableScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
		checkState(shouldRun.get(), "Task scheduler uninitialized");
		return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	@Override
	public void setup(final Collection<URL> urls) {
		// nothing to do
	}

	@Override
	public void preload() {
		final boolean previousStatus = shouldRun.getAndSet(true);
		if (!previousStatus) {
			LOGGER.info("Task scheduler initialized successfully");
		} else {
			LOGGER.info("Task scheduler was already initialized: status is left untouched");
		}		
	}

	@Override
	public void close() throws IOException {
		shouldRun.set(false);		
		try {
			if (!shutdownAndAwaitTermination(scheduler, TIMEOUT_SECS, TimeUnit.SECONDS)) {
				scheduler.shutdownNow();
			}		
		} finally {
			LOGGER.info("Task scheduler shutdown successfully");	
		}
	}

}