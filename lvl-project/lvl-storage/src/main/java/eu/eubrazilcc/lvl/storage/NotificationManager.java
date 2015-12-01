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
package eu.eubrazilcc.lvl.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.storage.dao.NotificationDAO.NOTIFICATION_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.RESOURCE_OWNER_DAO;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.assertValidResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.toResourceOwnerId;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.hasRole;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.storage.oauth2.ResourceOwner;

/**
 * Manages notifications sent to this application.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum NotificationManager {

	NOTIFICATION_MANAGER;

	private final static Logger LOGGER = getLogger(NotificationManager.class);

	public static final int MAX_WORKERS = 4;
	public static final int PAGE_SIZE = 100;

	private final Queue<Notification> queue = new PriorityBlockingQueue<>(10, new Comparator<Notification>() {
		@Override
		public int compare(final Notification n1, final Notification n2) {
			if (!n1.getPriority().equals(n2.getPriority())) {
				return (new Integer(n1.getPriority().getNumVal())).compareTo(new Integer(n2.getPriority().getNumVal()));
			}
			return (n1.getIssuedAt()).compareTo(n2.getIssuedAt());
		}
	});

	private AtomicInteger workers = new AtomicInteger(0);

	public void send(final Notification notification) {
		checkArgument(notification != null && notification.getPriority() != null && isNotBlank(notification.getAddressee()),
				"Uninitialized or invalid notification");
		checkState(queue.offer(notification), "No space is currently available");
		if (workers.get() == 0) {
			createWorker();
		}		
	}

	public void broadcast(final Notification notification) {
		checkArgument(notification != null && notification.getPriority() != null && isNotBlank(notification.getScope()),
				"Uninitialized or invalid notification");
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
						if (!queue.isEmpty() && workers.get() < MAX_WORKERS) {
							createWorker();
						}
						if (isNotBlank(notification.getScope())) {
							// broadcast
							int start = 0;
							List<ResourceOwner> resourceOwners = null;
							final MutableLong count = new MutableLong(0l);
							do {
								resourceOwners = RESOURCE_OWNER_DAO.list(start, PAGE_SIZE, null, null, null, count);
								for (final ResourceOwner resourceOwner : resourceOwners) {
									if (hasRole(notification.getScope(), resourceOwner.getUser())) {
										notification.setAddressee(resourceOwner.getUser().getUserid());
										NOTIFICATION_DAO.insert(notification);
										LOGGER.trace("Notification broadcasted: " + notification);
									}
								}
								start += resourceOwners.size();
							} while (!resourceOwners.isEmpty());							
						} else {
							// send to user converting user-names to valid resource-owner-identifiers
							try {
								assertValidResourceOwnerId(notification.getAddressee());
							} catch (Exception ignore) {
								try {
									final String ownerid = toResourceOwnerId(notification.getAddressee());
									notification.setAddressee(ownerid);
								} catch (Exception ignore2) { }
							}
							if (RESOURCE_OWNER_DAO.exist(notification.getAddressee())) {
								NOTIFICATION_DAO.insert(notification);
								LOGGER.trace("Notification sent: " + notification);
							} else {
								LOGGER.info("Discarding notification after checking that the addressee user does not exist: " + notification);
							}
						}
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