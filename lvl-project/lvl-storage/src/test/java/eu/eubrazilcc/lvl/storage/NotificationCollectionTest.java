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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.NotificationManager.NOTIFICATION_MANAGER;
import static eu.eubrazilcc.lvl.storage.dao.NotificationDAO.NOTIFICATION_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.dao.ResourceOwnerDAO.ADMIN_USER;
import static eu.eubrazilcc.lvl.storage.security.PermissionHelper.ADMIN_ROLE;
import static java.util.Collections.shuffle;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.Notification;
import eu.eubrazilcc.lvl.core.Notification.Action;
import eu.eubrazilcc.lvl.core.Notification.Priority;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests notification collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NotificationCollectionTest extends LeishvlTestCase {

	public NotificationCollectionTest() {
		super(false);
	}

	@Test
	public void test() {
		printMsg("NotificationCollectionTest.test()");
		try {
			// insert
			final Notification notification = Notification.builder()
					.newId()
					.message("This is an example")
					.addressee("user1")
					.build();
			final String id = NOTIFICATION_DAO.insert(notification).getId();
			assertThat("notification id is not empty", trim(id), allOf(notNullValue(), not(equalTo(""))));

			// find
			Notification notification2 = NOTIFICATION_DAO.find(notification.getId());
			assertThat("notification is not null", notification2, notNullValue());
			assertThat("notification coincides with original", notification2, equalTo(notification));
			printMsg(notification2.toString());

			// update
			notification.setAction(new Action("/#/action", "This is an action"));
			notification.setPriority(Priority.HIGH);
			NOTIFICATION_DAO.update(notification);

			// find after update
			notification2 = NOTIFICATION_DAO.find(notification.getId());
			assertThat("notification is not null", notification2, notNullValue());
			assertThat("notification coincides with original", notification2, equalTo(notification));
			printMsg(notification2.toString());

			// remove
			NOTIFICATION_DAO.delete(notification.getId());
			final long numRecords = NOTIFICATION_DAO.count();
			assertThat("number of notifications stored in the database coincides with expected", numRecords, equalTo(0l));

			// create a large dataset to test complex operations
			final List<String> ids = newArrayList();
			final int numItems = 11;
			for (int i = 0; i < numItems; i++) {
				final Notification notification3 = Notification.builder()
						.id("notif-" + i)
						.addressee("username")
						.message(Integer.toString(i)).build();
				NOTIFICATION_DAO.insert(notification3);
				ids.add(notification3.getId());
			}

			// pagination			
			final int size = 3;
			int start = 0;
			List<Notification> notifications = null;
			final MutableLong count = new MutableLong(0l);
			do {
				notifications = NOTIFICATION_DAO.list(start, size, null, null, null, count);
				if (notifications.size() != 0) {
					printMsg("Paging: first item " + start + ", showing " + notifications.size() + " of " + count.getValue() + " items");
				}
				start += notifications.size();
			} while (!notifications.isEmpty());

			// clean-up and display database statistics
			for (final String id2 : ids) {			
				NOTIFICATION_DAO.delete(id2);
			}
			NOTIFICATION_DAO.stats(System.out);

			// test notification sending
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
						.newId()
						.priority(priority)
						.addressee(ADMIN_USER)
						.message(Integer.toString(i)).build();
				notifications2.add(notification3);
			}
			shuffle(notifications2);
			for (final Notification notification3 : notifications2) {
				NOTIFICATION_MANAGER.send(notification3);
			}
			int tries = 0;
			while (NOTIFICATION_MANAGER.hasPendingNotifications() && tries++ < 30) {
				Thread.sleep(1000l);
			}
			notifications = NOTIFICATION_DAO.list(0, 200, null, null, null, null);
			assertThat("notifications is not null", notifications, notNullValue());
			assertThat("notifications size coincides with expected", notifications.size(), equalTo(200));

			// test notification broadcasting
			final Notification notification3 = Notification.builder()
					.scope(ADMIN_ROLE)
					.message("Notification to all users granted with full access to the system").build();
			NOTIFICATION_MANAGER.broadcast(notification3);
			tries = 0;
			while (NOTIFICATION_MANAGER.hasPendingNotifications() && tries++ < 30) {
				Thread.sleep(1000l);
			}
			notifications = NOTIFICATION_DAO.list(0, 201, null, null, null, null);
			assertThat("notifications (including broadcasted) is not null", notifications, notNullValue());
			assertThat("notifications (including broadcasted) size coincides with expected", notifications.size(), equalTo(201));			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NotificationCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("NotificationCollectionTest.test() has finished");
		}
	}

}