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

package eu.eubrazilcc.lvl.core.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.transform.OutputKeys.ENCODING;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.w3c.dom.Document;

/**
 * Utilities to handle XML documents and fragments.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class XmlHelper {

	private static final Logger LOGGER = getLogger(XmlHelper.class);
	private static DatatypeFactory dtf = null;

	/**
	 * Obtains a String representation of a XML document, omitting the XML declaration.
	 * @param document the document to be transformed into String.
	 * @return a String representation of the specified XML document.
	 */
	public static String documentToStringOmitXmlDeclaration(final Document document) {
		checkNotNull(document, "Uninitialized document");
		String docStr = null;
		try {
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(ENCODING, UTF_8.name());
			transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
			final StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			docStr = writer.toString();
		} catch (Exception e) {
			docStr = null;
			LOGGER.warn("Failed to convert DOM document to String", e);
		}
		return docStr;
	}

	public static String prettyPrint(final String payload) {
		checkArgument(isNoneBlank(payload), "Uninitialized XML payload");
		String docStr = null;
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document document = db.parse(new ByteArrayInputStream(payload.getBytes()));
			docStr = prettyPrint(document);
		} catch (Exception e) {
			docStr = null;
			LOGGER.warn("Failed to convert DOM document to String", e);
		}
		return docStr;
	}

	public static String prettyPrint(final Document document) {
		checkNotNull(document, "Uninitialized document");
		String docStr = null;
		try {
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(ENCODING, UTF_8.name());
			transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(INDENT, "yes");
			final StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			docStr = writer.toString();
		} catch (Exception e) {
			docStr = null;
			LOGGER.warn("Failed to convert DOM document to String", e);
		}
		return docStr;
	}

	public static XMLGregorianCalendar yearAsXMLGregorianCalendar(final int year) {
		final GregorianCalendar gc = new GregorianCalendar();
		gc.set(Calendar.YEAR, year);
		return getDatatypeFactory().newXMLGregorianCalendar(gc);
	}

	private static DatatypeFactory getDatatypeFactory() {
		if (dtf == null) {
			try {
				dtf = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new IllegalStateException("Failed to create data type factory", e);
			}
		}
		return dtf;
	}

}