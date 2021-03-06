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

package eu.eubrazilcc.lvl.core.event;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;
import static eu.eubrazilcc.lvl.core.event.EventHandler.EVENT_HANDLER;

import java.util.NoSuchElementException;
import java.util.Queue;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.Monitor;

/**
 * Provides a FIFO queue that stores requests sent to the system for fulfillment. This class can be
 * combined with the event bus to handle such a requests.
 * @author Erik Torres <ertorser@upv.es>
 */
public class RequestQueue<T> {

	private final Monitor monitor = new Monitor();

	private final Queue<T> queue = newLinkedList();

	public void add(final T request, final BaseEvent event) {
		checkArgument(request != null, "Uninitialized request");
		monitor.enter();
		try {
			queue.add(request);
			EVENT_HANDLER.post(event);			
		} finally {
			monitor.leave();
		}
	}

	public @Nullable T remove() {
		monitor.enter();
		try {
			return queue.remove();
		} catch (NoSuchElementException e) {
			return null;
		} finally {
			monitor.leave();
		}
	}

	public @Nullable T element() {
		monitor.enter();
		try {
			return queue.element();
		} catch (NoSuchElementException e) {
			return null;
		} finally {
			monitor.leave();
		}
	}

	public final void restart() {
		monitor.enter();
		try {
			queue.clear();
		} finally {
			monitor.leave();
		}
	}

}