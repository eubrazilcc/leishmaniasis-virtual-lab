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
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;
import static org.apache.activemq.ActiveMQConnectionFactory.DEFAULT_BROKER_URL;
import static org.apache.activemq.broker.BrokerFactory.createBroker;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.activemq.util.ServiceStopper;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Message queuing connector for Apache ActiveMQ.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://activemq.apache.org/">Apache ActiveMQ</a>
 */
public enum ActiveMQConnector implements Closeable2 {

	ACTIVEMQ_CONN;

	private static final Logger LOGGER = getLogger(ActiveMQConnector.class);
	
	public static final int MAX_POOLED_PRODUCERS_CONNECTIONS = 10;

	private Lock mutex = new ReentrantLock();
	private BrokerManager __broker = null;

	private Set<TopicSubscriber> subscribers = newHashSet();

	private BrokerManager broker() {
		mutex.lock();
		try {
			if (__broker == null) {
				final String brokers = Joiner.on(",").skipNulls().join(from(CONFIG_MANAGER.getMessageBrokers()).transform(new Function<String, String>() {
					@Override
					public String apply(final String host) {
						final String host2 = trimToNull(host);
						return isNotBlank(host2) ? "nio://" + host2 : null;						
					}					
				}).filter(notNull()).toList());
				try {
					__broker = BrokerManager.builder()
							.broker(CONFIG_MANAGER.isBrokerEmbedded() ? createBroker(new URI("xbean:activemq.xml"), true) : null)
							.connFactory(new ActiveMQConnectionFactory(isNotBlank(brokers) ? "failover://(" + brokers + ")?randomize=true" : DEFAULT_BROKER_URL))
							.build();					
				} catch (Exception e) {
					throw new IllegalStateException("Failed to create a broker service", e);
				}
			}
			return __broker;
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
			conn = broker().getConsumersConnFactory().createTopicConnection();
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
			conn = (TopicConnection)broker().getProducersConnFactory().createConnection();
			/* conn = broker().getConnFactory().createTopicConnection();
			conn.start(); */
			session = conn.createTopicSession(false, AUTO_ACKNOWLEDGE);
			final Topic topic = session.createTopic(topicName);
			producer = session.createProducer(topic);
			producer.setDeliveryMode(NON_PERSISTENT);
			final TextMessage textMessage = session.createTextMessage(message);
			producer.send(textMessage);
		} catch (JMSException e) {			
			LOGGER.error("Failed to send message to topic: " + topicName, e);
		} finally {
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
			/* if (conn != null) {
				try {
					conn.close();
				} catch (JMSException ignore) { }
			} */
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
			if (__broker != null) {
				if (__broker.getBroker().isPresent()) {
					try {
						__broker.getBroker().get().stopAllConnectors(new ServiceStopper());
					} catch (Exception ignore) { }
				}
				try {
					__broker.getProducersConnFactory().stop();
				} catch (Exception ignore) { }
				__broker = null;
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

	/**
	 * Manages this connector by providing the following artifacts:
	 * <ul>
	 * <li>an optional {@link BrokerService message broker}, which is embedded within this application;</li>
	 * <li>a {@link ActiveMQConnectionFactory consumer connections factory} to create new connections that are closed when the consumer is completed; and</li>
	 * <li>a {@link PooledConnectionFactory pooled producer connections factory} to create and reuse connections to send messages.</li>
	 * </ul>
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public static class BrokerManager {

		private final Optional<BrokerService> broker;
		private final ActiveMQConnectionFactory consumersConnFactory;
		private final PooledConnectionFactory producersConnFactory;

		public BrokerManager(final BrokerService broker, final ActiveMQConnectionFactory connFactory) {
			checkNotNull(connFactory);
			this.broker = Optional.fromNullable(broker);
			this.consumersConnFactory = connFactory;
			// configure pooled connection factory
			this.producersConnFactory = new PooledConnectionFactory();
			this.producersConnFactory.setConnectionFactory(connFactory);
			this.producersConnFactory.setMaxConnections(MAX_POOLED_PRODUCERS_CONNECTIONS);
		}

		public Optional<BrokerService> getBroker() {
			return broker;
		}

		public ActiveMQConnectionFactory getConsumersConnFactory() {
			return consumersConnFactory;
		}

		public PooledConnectionFactory getProducersConnFactory() {
			return producersConnFactory;
		}		

		/* Fluent API */

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {

			private BrokerService broker = null;
			private ActiveMQConnectionFactory connFactory = null;

			public Builder broker(final BrokerService broker) {
				this.broker = broker;
				return this;
			}

			public Builder connFactory(final ActiveMQConnectionFactory connFactory) {
				this.connFactory = connFactory;
				return this;
			}

			public BrokerManager build() {
				return new BrokerManager(broker, connFactory);
			}

		}

	}

	/**
	 * Stores information about a topic subscriber to allow closing the connection when the consumer is completed.
	 * @author Erik Torres <ertorser@upv.es>
	 */
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
			return Objects.equals(topicName, other.topicName)
					&& Objects.equals(conn, other.conn)
					&& Objects.equals(session, other.session)
					&& Objects.equals(consumer, other.consumer);
		}

		@Override
		public int hashCode() {
			return Objects.hash(topicName, conn, session, consumer);
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