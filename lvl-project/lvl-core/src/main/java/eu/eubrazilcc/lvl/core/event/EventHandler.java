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

package eu.eubrazilcc.lvl.core.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Queues.newArrayDeque;
import static com.google.common.collect.Queues.synchronizedDeque;
import static eu.eubrazilcc.lvl.core.event.DeadEventListener.DEAD_EVENT_LISTENER;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Deque;

import javax.annotation.Nullable;

import com.google.common.eventbus.EventBus;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Handles events sent to an event bus available to all the components in the application.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum EventHandler implements Closeable2 {

	EVENT_HANDLER;

	public static final String EVENT_BUS_NAME = "lvl-service";

	private final Deque<Object> deque = synchronizedDeque(newArrayDeque());

	private final EventBus eventBus;

	private EventHandler() {
		eventBus = new EventBus(EVENT_BUS_NAME);
		// register dead event listener
		register(DEAD_EVENT_LISTENER);
	}

	public void register(final Object object) {
		checkArgument(object != null, "Uninitialized subscriber");
		eventBus.register(object);
		deque.push(object);
	}

	public void unregister(final Object object) {
		checkArgument(object != null, "Uninitialized subscriber");
		final boolean contains = deque.remove(object);
		if (contains) {
			try {
				eventBus.unregister(object);
			} catch (Exception e) { }
		}
	}

	public void post(final BaseEvent event) {
		checkArgument(event != null, "Uninitialized event");
		eventBus.post(event);
	}

	@Override
	public void setup(final @Nullable Collection<URL> urls) {
		// nothing to do
	}

	@Override
	public void preload() {
		// nothing to do
	}

	@Override
	public void close() throws IOException {
		while (!deque.isEmpty()) {
			final Object object = deque.pop();
			try {
				eventBus.unregister(object);
			} catch (Exception e) { }
		}
	}

}