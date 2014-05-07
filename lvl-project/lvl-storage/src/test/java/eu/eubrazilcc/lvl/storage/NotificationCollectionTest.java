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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.dao.NotificationDAO.NOTIFICATION_DAO;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static java.util.Collections.shuffle;

import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Notification.Action;
import eu.eubrazilcc.lvl.core.Notification.Priority;

/**
 * Tests notification collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NotificationCollectionTest {

	@Test
	public void test() {
		System.out.println("NotificationCollectionTest.test()");
		try {
			// insert
			final Notification notification = Notification.builder()
					.message("This is an example")
					.build();
			final String id = NOTIFICATION_DAO.insert(notification);
			assertThat("notification id is not null", id, notNullValue());
			assertThat("notification id is not empty", isNotBlank(id));
			notification.setId(id);
			
			// find
			Notification notification2 = NOTIFICATION_DAO.find(notification.getId());
			assertThat("notification is not null", notification2, notNullValue());
			assertThat("notification coincides with original", notification2, equalTo(notification));
			System.out.println(notification2.toString());

			// update
			notification.setAction(new Action("/#/action", "This is an action"));
			notification.setPriority(Priority.HIGH);
			NOTIFICATION_DAO.update(notification);

			// find after update
			notification2 = NOTIFICATION_DAO.find(notification.getId());
			assertThat("notification is not null", notification2, notNullValue());
			assertThat("notification coincides with original", notification2, equalTo(notification));
			System.out.println(notification2.toString());

			// remove
			NOTIFICATION_DAO.delete(notification.getId());

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final Notification notification3 = Notification.builder()
						.message(Integer.toString(i)).build();
				ids.add(NOTIFICATION_DAO.insert(notification3));
			}
			final int size = 3;
			int start = 0;
			List<Notification> notifications = null;
			final MutableLong count = new MutableLong(0l);
			do {
				notifications = NOTIFICATION_DAO.list(start, size, count);
				if (notifications.size() != 0) {
					System.out.println("Paging " + start + " - " + notifications.size() + " of " + count.getValue());
				}
				start += notifications.size();
			} while (!notifications.isEmpty());
			for (final String id2 : ids) {			
				NOTIFICATION_DAO.delete(id2);
			}
			NOTIFICATION_DAO.stats(System.out);
			
			// test notification manager
			final List<Notification> notifications2 = newArrayList();
			for (int i = 0; i < 200; i++) {
				Priority priority;
				if (i < 50) {
					priority = Priority.CRITICAL;
				} else if (i >= 50 && i < 100) {
					priority = Priority.HIGH;
				} else if (i >= 100 && i < 150) {
					priority = Priority.NORMAL;
				} else {
					priority = Priority.LOW;
				}
				final Notification notification3 = Notification.builder()
						.priority(priority)
						.message(Integer.toString(i)).build();
				notifications2.add(notification3);
			}
			shuffle(notifications2);
			for (final Notification notification3 : notifications2) {
				NOTIFICATION_MANAGER.sendNotification(notification3);
			}
			int tries = 0;
			while (NOTIFICATION_MANAGER.hasPendingNotifications() && tries++ < 30) {
				Thread.sleep(1000l);
			}
			notifications = NOTIFICATION_DAO.list(0, 200, null);
			assertThat("notifications is not null", notifications, notNullValue());
			assertThat("notifications size coincides with expected", notifications.size(), equalTo(200));
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NotificationCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("NotificationCollectionTest.test() has finished");
		}
	}

}