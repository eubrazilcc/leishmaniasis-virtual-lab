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

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newCachedThreadPool;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Runs tasks in a pool of threads that must be disposed as part of the application termination.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum TaskRunner implements Closeable2 {

	INSTANCE;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);

	public static final String THREAD_NAME_PATTERN = "lvl-runner-%d";

	private final ListeningExecutorService runner = listeningDecorator(newCachedThreadPool(
			new ThreadFactoryBuilder()
			.setNameFormat(THREAD_NAME_PATTERN)
			.setDaemon(false)
			.setUncaughtExceptionHandler(TaskUncaughtExceptionHandler.INSTANCE)
			.build()));

	private AtomicBoolean shouldRun = new AtomicBoolean(false);

	private TaskRunner() { }	

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
		
		
		// TODO		
	}

}