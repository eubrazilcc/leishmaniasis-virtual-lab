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

package eu.eubrazilcc.lvl.core.concurrent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static eu.eubrazilcc.lvl.core.concurrent.TaskUncaughtExceptionHandler.TASK_UNCAUGHT_EXCEPTION_HANDLER;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Runs tasks in a pool of threads that must be disposed as part of the application termination. Tasks are submitted to the pool of threads 
 * for execution and a {@link ListenableFuture} is returned to the caller.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained">ListenableFutureExplained</a>
 */
public enum TaskRunner implements Closeable2 {

	/**
	 * Singleton instance of {@link TaskRunner} that executes short-living tasks in a pool of threads.
	 */
	TASK_RUNNER;

	private final static Logger LOGGER = getLogger(TaskRunner.class);

	public static final String THREAD_NAME_PATTERN = "lvl-runner-%d";

	private static final int TIMEOUT_SECS = 20;

	/**
	 * Reuse the code of {@link java.util.concurrent.Executors#newCachedThreadPool(java.util.concurrent.ThreadFactory)}
	 * to create the thread pool, limiting the maximum number of threads in the pool to 128.
	 */
	private final ListeningExecutorService runner = listeningDecorator(new ThreadPoolExecutor(0, 128, 60l, SECONDS, new SynchronousQueue<Runnable>(), 
			new ThreadFactoryBuilder().setNameFormat(THREAD_NAME_PATTERN)
			.setDaemon(false)
			.setUncaughtExceptionHandler(TASK_UNCAUGHT_EXCEPTION_HANDLER)
			.build()));

	private AtomicBoolean shouldRun = new AtomicBoolean(false);

	private TaskRunner() { }

	/**
	 * Submits a new task for execution to the pool of threads managed by this class.
	 * @param task - task to be executed
	 * @return a {@link ListenableFuture} that the caller can use to track the execution of the
	 *         task and to register a callback function.
	 */
	public <T> ListenableFuture<T> submit(final Callable<T> task) {
		checkState(shouldRun.get(), "Task runner uninitialized");
		return runner.submit(task);
	}

	/**
	 * Executes a new task that supports cancellation and provides a unique identifier.
	 * @param task - task to be executed
	 */
	public <T> void execute(final CancellableTask<T> task) {
		checkState(shouldRun.get(), "Task runner uninitialized");
		checkArgument(task != null, "Uninitialized task");
		runner.execute(task.getTask());
	}
	
	public Executor executor() {
		return runner;
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
			if (!shutdownAndAwaitTermination(runner, TIMEOUT_SECS, SECONDS)) {
				runner.shutdownNow();
			}
		} catch (Exception e) {
			// force shutdown if current thread also interrupted, preserving interrupt status
			runner.shutdownNow();
			if (e instanceof InterruptedException) {
				currentThread().interrupt();
			}
		} finally {
			LOGGER.info("Task runner shutdown successfully");	
		}
	}

}