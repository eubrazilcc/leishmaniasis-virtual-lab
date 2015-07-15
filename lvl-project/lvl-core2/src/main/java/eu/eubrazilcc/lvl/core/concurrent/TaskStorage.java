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
import static com.google.common.collect.Maps.newHashMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.util.concurrent.Monitor;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Provides memory storage of tasks sent to the system for execution. These tasks can be accessed to track
 * their progress or to cancel them.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum TaskStorage implements Closeable2 {

	/**
	 * Singleton instance of {@link TaskStorage} that provides a memory storage of tasks sent to the 
	 * system for execution.
	 */
	TASK_STORAGE;

	private final static Logger LOGGER = getLogger(TaskStorage.class);

	private final Monitor monitor = new Monitor();

	private final Map<UUID, CancellableTask<?>> map = newHashMap();

	private boolean isActive = false;

	private TaskStorage() { }

	public void add(final CancellableTask<?> task) {
		checkArgument(task != null && task.getUuid() != null, "Uninitialized or invalid task");
		monitor.enter();
		try {
			checkState(isActive, "Task storage uninitialized");
			map.put(task.getUuid(), task);
		} finally {
			monitor.leave();
		}
	}

	public @Nullable CancellableTask<?> remove(final UUID key) {
		monitor.enter();
		try {
			checkState(isActive, "Task storage uninitialized");
			return map.remove(key);
		} finally {
			monitor.leave();
		}
	}

	public @Nullable CancellableTask<?> get(final UUID key) {
		monitor.enter();
		try {
			checkState(isActive, "Task storage uninitialized");
			return map.get(key);		
		} finally {
			monitor.leave();
		}
	}

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		monitor.enter();
		try {			
			if (!isActive) {
				isActive = true;
				LOGGER.info("Task storage initialized successfully");
			} else {
				LOGGER.info("Task storage was already initialized: status is left untouched");
			}
		} finally {
			monitor.leave();
		}
	}

	@Override
	public void close() throws IOException {
		monitor.enter();
		try {
			isActive = false;
			for (final Map.Entry<UUID, CancellableTask<?>> entry : map.entrySet()) {
				try {
					final CancellableTask<?> task = entry.getValue();
					LOGGER.info("Cancelling task: " + task);
					task.getTask().cancel(true);
				} catch (Exception ignored) { }
			}
		} finally {
			monitor.leave();
			LOGGER.info("Task storage shutdown successfully");
		}
	}

}