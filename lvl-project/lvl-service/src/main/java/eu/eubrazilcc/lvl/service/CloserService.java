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

package eu.eubrazilcc.lvl.service;

import static com.google.common.collect.Queues.newArrayDeque;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskScheduler.TASK_SCHEDULER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationFinder.findConfigurationFiles;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBConnector.MONGODB_CONN;

import java.io.Closeable;
import java.util.Deque;

import com.google.common.util.concurrent.Monitor;

import eu.eubrazilcc.lvl.core.CloserServiceIf;

/**
 * Close registered resources when it is closed.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum CloserService implements CloserServiceIf {

	CLOSER_SERVICE;

	private final Monitor monitor = new Monitor();

	private final Deque<Closeable> stack = newArrayDeque();

	private CloserService() { }

	@Override
	public void preload() {		
		// load configuration
		CONFIG_MANAGER.setup(findConfigurationFiles());
		CONFIG_MANAGER.preload();
		// load MongoDB connector and register it for closing
		MONGODB_CONN.preload();
		register(MONGODB_CONN);
		// load task runner, task scheduler and task storage
		TASK_RUNNER.preload();
		register(TASK_RUNNER);
		TASK_SCHEDULER.preload();
		register(TASK_SCHEDULER);
		TASK_STORAGE.preload();
		register(TASK_STORAGE);
		// load e-SC connector and register it for closing
		ESCENTRAL_CONN.preload();
		register(ESCENTRAL_CONN);
	}

	@Override
	public void register(final Closeable closeable) {
		monitor.enter();
		try {
			stack.push(closeable);
		} finally {
			monitor.leave();
		}
	}

	@Override
	public void close() {
		monitor.enter();
		try {
			while (!stack.isEmpty()) {
				try {
					stack.pop().close();
				} catch (Exception ignored) { }
			}
		} finally {
			monitor.leave();
		}
	}

}