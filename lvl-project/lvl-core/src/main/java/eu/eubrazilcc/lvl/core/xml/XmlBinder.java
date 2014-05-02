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

package eu.eubrazilcc.lvl.core.xml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;

/**
 * Provides a template for implementing XML binding classes.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class XmlBinder {

	protected JAXBContext context;
	protected JAXBIntrospector introspector;
	
	public XmlBinder(final JAXBContext context, final JAXBIntrospector introspector) {
		this.context = context;
		this.introspector = introspector;
	}

	protected abstract <T> JAXBElement<T> createType(final T obj);
	
	public <T> String typeToXml(final T obj) throws IOException {
		try (final StringWriter writer = new StringWriter()) {
			final Marshaller marshaller = context.createMarshaller();
			if (null == introspector.getElementName(obj)) {
				marshaller.marshal(createType(obj), writer);
			} else {
				marshaller.marshal(obj, writer);
			}
			return writer.toString();
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T typeFromXml(final String payload) throws IOException {
		try (final StringReader reader = new StringReader(payload)) {
			return (T) JAXBIntrospector.getValue(context.createUnmarshaller().unmarshal(reader));
		} catch (Exception e) {
			throw new IOException(e);
		}		
	}

	public <T> void typeToFile(final T obj, final File file) throws IOException {
		try (final FileWriter writer = new FileWriter(file, false)) {
			final Marshaller marshaller = context.createMarshaller();			
			if (null == introspector.getElementName(obj)) {
				marshaller.marshal(createType(obj), writer);
			} else {
				marshaller.marshal(obj, writer);
			}			
		} catch (JAXBException e) {
			throw new IOException(e);
		}		
	}

	@SuppressWarnings("unchecked")
	public <T> T typeFromFile(final File file) throws IOException {
		try (final FileReader reader = new FileReader(file)) {
			return (T) JAXBIntrospector.getValue(context.createUnmarshaller().unmarshal(reader));
		} catch (JAXBException e) {
			throw new IOException(e);
		}		
	}
	
}