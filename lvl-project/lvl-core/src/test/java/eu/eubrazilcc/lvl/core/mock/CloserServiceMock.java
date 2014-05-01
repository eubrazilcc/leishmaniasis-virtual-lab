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

package eu.eubrazilcc.lvl.core.mock;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.Queue;

import com.google.common.util.concurrent.Monitor;

import eu.eubrazilcc.lvl.core.CloserServiceIf;
import eu.eubrazilcc.lvl.core.concurrent.TaskRunner;
import eu.eubrazilcc.lvl.core.concurrent.TaskScheduler;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;

/**
 * Close registered resources when it is closed.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum CloserServiceMock implements CloserServiceIf {

	INSTANCE;

	private final Monitor monitor = new Monitor();

	private final Queue<Closeable> queue = new LinkedList<Closeable>();

	private CloserServiceMock() { }

	@Override
	public void preload() {		
		// load default configuration
		ConfigurationManager.INSTANCE.preload();
		// register task runner for clean up (initialization will be performed as part of the 
		// concurrency tests)
		register(TaskRunner.INSTANCE);
		register(TaskScheduler.INSTANCE);
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