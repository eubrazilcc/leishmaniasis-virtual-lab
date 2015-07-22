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

package eu.eubrazilcc.lvl.core.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * XML manipulation utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class XmlUtils {

	private static final Logger LOGGER = getLogger(XmlUtils.class);

	public static String nodeToString(final Node node) {
		String nodeStr = null;
		try {
			final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Node copyNode = document.importNode(node, true);
			document.appendChild(copyNode);

			final DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
			final LSSerializer serializer = domImplLS.createLSSerializer();

			final LSOutput output = domImplLS.createLSOutput();
			output.setEncoding("UTF-8");
			final Writer stringWriter = new StringWriter();
			output.setCharacterStream(stringWriter);
			serializer.write(document, output);

			nodeStr = stringWriter.toString();
		} catch (Exception e) {
			nodeStr = null;
			LOGGER.warn("Failed to convert DOM element to String", e);
		}
		return nodeStr;
	}

}