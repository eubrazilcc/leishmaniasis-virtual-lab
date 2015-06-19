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

package eu.eubrazilcc.lvl.storage.mock;

import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoConnector.MONGODB_CONN;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.Queue;

import com.google.common.util.concurrent.Monitor;

import eu.eubrazilcc.lvl.core.CloserServiceIf;

/**
 * Close registered resources when it is closed. This instance only features a partial implementation, reducing the number of services started.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum LightCloserServiceMock implements CloserServiceIf {

	LIGHT_CLOSER_SERVICE_MOCK;

	private final Monitor monitor = new Monitor();

	private final Queue<Closeable> queue = new LinkedList<Closeable>();

	private LightCloserServiceMock() { }

	@Override
	public void preload() {		
		// load default configuration and register it for closing
		CONFIG_MANAGER.preload();
		register(CONFIG_MANAGER);
		// load MongoDB connector and register it for closing
		MONGODB_CONN.preload();
		register(MONGODB_CONN);
		// load task runner
		TASK_RUNNER.preload();
		register(TASK_RUNNER);
	}

	@Override
	public void register(final Closeable closeable) {
		monitor.enter();
		try {
			queue.add(closeable);
		} finally {
			monitor.leave();
		}
	}

	@Override
	public void close() {
		monitor.enter();
		try {
			while (!queue.isEmpty()) {
				try {
					queue.remove().close();
				} catch (Exception ignore) { }
			}
		} finally {
			monitor.leave();
		}
	}

}