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

import static org.slf4j.LoggerFactory.getLogger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;

/**
 * Tracks changes in the users' permissions.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PermissionsChangeListener implements MessageListener {

	private static final Logger LOGGER = getLogger(PermissionsChangeListener.class);

	@Override
	public void onMessage(final Message message) {
		try {
			if (message instanceof TextMessage) {
				final TextMessage textMessage = (TextMessage) message;
				
				// TODO
				System.out.println("\n\n >> HERE: Received message" + textMessage.getText() + "'\n");
				// TODO
				
			}
		} catch (final JMSException e2) {
			LOGGER.error("Failed to read message", e2);
		}
	}

}