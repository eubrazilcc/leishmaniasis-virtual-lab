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
package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.storage.dao.NotificationDAO.NOTIFICATION_DAO;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.Notification;

/**
 * Manages notifications sent to this application.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum NotificationManager {

	NOTIFICATION_MANAGER;
	
	private final static Logger LOGGER = getLogger(NotificationManager.class);
	
	public static final int MAX_WORKERS = 3;

	private final Queue<Notification> queue = new PriorityBlockingQueue<>(10, new Comparator<Notification>() {
		@Override
		public int compare(final Notification n1, final Notification n2) {
			if (!n1.getPriority().equals(n2.getPriority())) {
				return (new Integer(n1.getPriority().getNumVal())).compareTo(new Integer(n2.getPriority().getNumVal()));
			}
			return (new Long(n1.getIssuedAt())).compareTo(new Long(n2.getIssuedAt()));
		}
	});
	
	private AtomicInteger workers = new AtomicInteger(0);
	
	public void sendNotification(final Notification notification) {
		checkArgument(notification != null && notification.getPriority() != null, "Uninitialized or invalid notification");
		checkState(queue.offer(notification), "No space is currently available");
		if (workers.get() == 0) {
			createWorker();
		}		
	}
	
	public boolean hasPendingNotifications() {
		return !queue.isEmpty();
	}
	
	private void createWorker() {
		workers.incrementAndGet();
		final ListenableFuture<Void> future = TASK_RUNNER.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Notification notification;
				do {
					notification = queue.poll();
					if (notification != null) {
						if (!queue.isEmpty() && workers.get() < 3) {
							createWorker();
						}
						NOTIFICATION_DAO.insert(notification);
						LOGGER.trace("Notification sent: " + notification);
					}					
				} while (notification != null);
				return null;
			}
		});
		Futures.addCallback(future, new FutureCallback<Void>() {
			@Override
			public void onSuccess(final Void result) {
				workers.decrementAndGet();
			}
			@Override
			public void onFailure(final Throwable error) {
				workers.decrementAndGet();
			}
		});
	}

}