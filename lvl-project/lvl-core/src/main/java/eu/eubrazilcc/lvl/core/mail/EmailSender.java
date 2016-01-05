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

package eu.eubrazilcc.lvl.core.mail;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;

/**
 * Sends e-mails.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum EmailSender {

	EMAIL_SENDER;

	private static final Logger LOGGER = getLogger(EmailSender.class);

	public void sendTextEmail(final String emailDestinatary, final String subject, final String body) {
		sendEmail(emailDestinatary, subject, body, false);
	}

	public void sendHtmlEmail(final String emailDestinatary, final String subject, final String body) {
		sendEmail(emailDestinatary, subject, body, true);
	}

	private void sendEmail(final String emailDestinatary, final String subject, final String body, final boolean useHtml) {
		try {
			final Properties properties = System.getProperties();
			properties.setProperty("mail.smtp.host", CONFIG_MANAGER.getSmtpHost());
			final Session session = Session.getDefaultInstance(properties);
			final MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(CONFIG_MANAGER.getSmtpNoreplyEmail()));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailDestinatary));
			message.setSubject(subject);
			if (useHtml) {
				message.setContent(body, "text/html");	
			} else {
				message.setText(body);
			}			
			Transport.send(message);
			LOGGER.trace("Message successfully sent to: " + emailDestinatary);
		} catch (MessagingException e) {
			throw new IllegalStateException("Failed to send message to: " + emailDestinatary, e);
		}
	}

}