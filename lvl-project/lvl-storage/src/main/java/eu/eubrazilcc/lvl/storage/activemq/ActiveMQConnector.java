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

package eu.eubrazilcc.lvl.storage.activemq;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Message queuing connector for Apache ActiveMQ.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://activemq.apache.org/">Apache ActiveMQ</a>
 */
public enum ActiveMQConnector implements Closeable2 {

	ACTIVEMQ_CONN;

	private static final Logger LOGGER = getLogger(ActiveMQConnector.class);

	private Lock mutex = new ReentrantLock();
	private ActiveMQConnectionFactory __connFactory = null;

	private Set<TopicSubscriber> subscribers = newHashSet();

	private ActiveMQConnectionFactory connectionFactory() {
		mutex.lock();
		try {
			if (__connFactory == null) {
				// TODO __connFactory = new ActiveMQConnectionFactory("failover://(tcp://localhost:61616,tcp://lvl.i3m.upv.es:61616)?randomize=true");
				__connFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
			}
			return __connFactory;
		} finally {
			mutex.unlock();
		}
	}

	public void subscribe(final String topicName, final MessageListener listener) {
		String topicName2 = null;
		checkArgument(isNotBlank(topicName2 = trimToEmpty(topicName)), "Uninitialized or invalid topic");
		checkNotNull(listener);
		TopicConnection conn = null;
		TopicSession session = null;
		MessageConsumer consumer = null;
		try {
			conn = connectionFactory().createTopicConnection();
			conn.start();
			session = conn.createTopicSession(false, AUTO_ACKNOWLEDGE);
			final Topic topic = session.createTopic(topicName2);
			consumer = session.createConsumer(topic);			
			consumer.setMessageListener(listener);
			register(TopicSubscriber.builder()
					.topicName(topicName2)
					.connection(conn)
					.session(session)
					.consumer(consumer)
					.build());
			LOGGER.info("Subscribed to topic: " + topicName2);
		} catch (JMSException e) {
			if (consumer != null) {
				try {
					consumer.close();
				} catch (JMSException ignore) { }
			}
			if (session != null) {
				try {
					session.close();
				} catch (JMSException ignore) { }
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (JMSException ignore) { }
			}
			LOGGER.error("Failed to subscribe to topic: " + topicName2, e);
		}
	}

	public void unsubscribe(final String topicName) {
		String topicName2 = null;
		checkArgument(isNotBlank(topicName2 = trimToEmpty(topicName)), "Uninitialized or invalid topic");
		mutex.lock();
		try {			
			boolean found = false;
			final Iterator<TopicSubscriber> it = subscribers.iterator();
			while (it.hasNext() && !found) {
				try {
					final TopicSubscriber subscriber = it.next();
					if (topicName2.equals(subscriber.getTopicName())) {
						subscriber.close();
						it.remove();
						found = true;
					}
				} catch (Exception ignore) { }
			}
		} finally {
			mutex.unlock();			
		}		
	}

	public void sendMessage(final String topicName, final String message) {
		checkArgument(isNotBlank(topicName), "Uninitialized or invalid topic");
		checkArgument(isNotBlank(message), "Uninitialized or invalid message");
		TopicConnection conn = null;
		TopicSession session = null;
		MessageProducer producer = null;
		try {
			conn = connectionFactory().createTopicConnection();
			conn.start();
			session = conn.createTopicSession(false, AUTO_ACKNOWLEDGE);
			final Topic topic = session.createTopic(topicName);
			producer = session.createProducer(topic);
			producer.setDeliveryMode(NON_PERSISTENT);
			final TextMessage textMessage = session.createTextMessage(message);
			producer.send(textMessage);
		} catch (JMSException e) {
			if (producer != null) {
				try {
					producer.close();
				} catch (JMSException ignore) { }
			}
			if (session != null) {
				try {
					session.close();
				} catch (JMSException ignore) { }
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (JMSException ignore) { }
			}
			LOGGER.error("Failed to send message to topic: " + topicName, e);
		}
	}

	private void register(final TopicSubscriber subscriber) {
		mutex.lock();
		try {
			subscribers.add(subscriber);
		} finally {
			mutex.unlock();
		}
	}

	/* General methods */

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("ActiveMQ connector initialized successfully");
	}

	@Override
	public void close() throws IOException {
		mutex.lock();
		try {
			if (__connFactory != null) {
				__connFactory = null;
			}
			final Iterator<TopicSubscriber> it = subscribers.iterator();
			while (it.hasNext()) {
				try {
					it.next().close();
					it.remove();					
				} catch (Exception ignore) { }
			}
		} finally {
			mutex.unlock();
			LOGGER.info("ActiveMQ connector shutdown successfully");
		}		
	}

	/* Inner classes */

	public static class TopicSubscriber {

		private final String topicName;
		private final TopicConnection conn;
		private final TopicSession session;
		private final MessageConsumer consumer;

		public TopicSubscriber(final String topicName, final TopicConnection conn, final TopicSession session, final MessageConsumer consumer) {
			checkArgument(isNotBlank(topicName), "Uninitialized or invalid topic");
			checkNotNull(conn);
			checkNotNull(session);
			checkNotNull(consumer);
			this.topicName = trimToEmpty(topicName);
			this.conn = conn;
			this.session = session;
			this.consumer = consumer;
		}

		public String getTopicName() {
			return topicName;
		}

		public TopicConnection getConn() {
			return conn;
		}		

		public TopicSession getSession() {
			return session;
		}		

		public MessageConsumer getConsumer() {
			return consumer;
		}

		public void close() {
			if (consumer != null) {
				try {
					consumer.close();
				} catch (JMSException ignore) { }
			}
			if (session != null) {
				try {
					session.close();
				} catch (JMSException ignore) { }
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (JMSException ignore) { }
			}
			LOGGER.info("Unsubscribed from topic: " + topicName);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof TopicSubscriber)) {
				return false;
			}
			final TopicSubscriber other = TopicSubscriber.class.cast(obj);
			return Objects.equals(topicName, other.topicName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(topicName);
		}

		@Override
		public String toString() {
			return toStringHelper(this)
					.add("topicName", topicName)
					.toString();
		}

		/* Fluent API */

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private String topicName;
			private TopicConnection conn;
			private TopicSession session;
			private MessageConsumer consumer;

			public Builder topicName(final String topicName) {				
				this.topicName = trimToEmpty(topicName);
				return this;
			}

			public Builder connection(final TopicConnection conn) {				
				this.conn = conn;
				return this;
			}

			public Builder session(final TopicSession session) {				
				this.session = session;
				return this;
			}

			public Builder consumer(final MessageConsumer consumer) {
				this.consumer = consumer;
				return this;
			}

			public TopicSubscriber build() {
				return new TopicSubscriber(topicName, conn, session, consumer);
			}

		}

	}

}