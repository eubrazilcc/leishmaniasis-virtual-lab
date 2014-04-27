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
import static java.util.concurrent.Executors.newCachedThreadPool;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Runs tasks in a pool of threads that must be disposed as part of the application termination. Tasks
 * are submitted to the pool of threads for execution and a {@link ListenableFuture} is returned to the
 * caller.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained">ListenableFutureExplained</a>
 */
public enum TaskRunner implements Closeable2 {

	INSTANCE;

	private final static Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);

	public static final String THREAD_NAME_PATTERN = "lvl-runner-%d";

	private static final int TIMEOUT_SECS = 20;

	private final ListeningExecutorService runner = listeningDecorator(newCachedThreadPool(
			new ThreadFactoryBuilder()
			.setNameFormat(THREAD_NAME_PATTERN)
			.setDaemon(false)
			.setUncaughtExceptionHandler(TaskUncaughtExceptionHandler.INSTANCE)
			.build()));

	private AtomicBoolean shouldRun = new AtomicBoolean(false);

	private TaskRunner() { }

	public <T> ListenableFuture<T> submit(final Callable<T> task) {
		checkState(shouldRun.get(), "Task runner uninitialized");
		return runner.submit(task);
	}

	@Override
	public void setup(final Collection<URL> urls) {
		// nothing to do
	}

	@Override
	public void preload() {
		final boolean previousStatus = shouldRun.getAndSet(true);
		if (!previousStatus) {
			LOGGER.info("Task runner initialized successfully");
		} else {
			LOGGER.info("Task runner was already initialized: status is left untouched");
		}
	}

	@Override
	public void close() throws IOException {
		shouldRun.set(false);
		try {
			if (!shutdownAndAwaitTermination(runner, TIMEOUT_SECS, TimeUnit.SECONDS)) {
				runner.shutdownNow();
			}		
		} finally {
			LOGGER.info("Task runner shutdown successfully");	
		}
	}

}