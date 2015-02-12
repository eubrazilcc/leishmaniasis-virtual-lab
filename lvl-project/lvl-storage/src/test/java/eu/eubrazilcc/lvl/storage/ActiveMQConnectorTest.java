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

import static eu.eubrazilcc.lvl.storage.activemq.ActiveMQConnector.ACTIVEMQ_CONN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.activemq.ActiveMQConnector;

/**
 * Test message queuing operations using the {@link ActiveMQConnector ActiveMQ connector}. To run this test an ActiveMQ server should be available.
 * Running the test with the integration test will allow using the embedded ActiveMQ server that is deployed with the LVL storage.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ActiveMQConnectorTest {

	private static int sent = 0;
	private static int received = 0;

	@Test
	public void test() {
		System.out.println("ActiveMQConnectorTest.test()");
		try {
			// test subscription
			ACTIVEMQ_CONN.subscribe("lvl:test", new MessageListener() {				
				@Override
				public void onMessage(final Message message) {
					if (message instanceof TextMessage) {
						final TextMessage textMessage = (TextMessage) message;						
						try {
							received++;
							System.out.println(" >> Received message: " + textMessage.getText() + "'");
						} catch (JMSException e) {
							fail("Failed to receive message");
						}
					}
				}
			});

			// test message sending
			for (int i = 1; i <= 3; i++) {
				ACTIVEMQ_CONN.sendMessage("lvl:test", "Hello World " + i + "!");
				sent++;
			}
			Thread.sleep(2000l);
			assertThat("number of received messages coincides with messages sent", received, equalTo(sent));

			// test unsubscribe
			ACTIVEMQ_CONN.unsubscribe("lvl:test");

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ActiveMQConnectorTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ActiveMQConnectorTest.test() has finished");
		}
	}

}