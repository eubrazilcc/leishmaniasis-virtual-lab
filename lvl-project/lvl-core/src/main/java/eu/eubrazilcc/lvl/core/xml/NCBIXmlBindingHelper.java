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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;

import eu.eubrazilcc.lvl.core.xml.ncbi.GBSeq;
import eu.eubrazilcc.lvl.core.xml.ncbi.GBSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.ObjectFactory;

/**
 * NCBI XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class NCBIXmlBindingHelper {

	private static final Class<?>[] SUPPORTED_CLASSES = {
		GBSet.class,
		GBSeq.class
	};

	private static JAXBContext CONTEXT;
	private static JAXBIntrospector INTROSPECTOR;

	public static final ObjectFactory FACTORY = new ObjectFactory();

	static {
		try {
			// context
			CONTEXT = JAXBContext.newInstance(SUPPORTED_CLASSES, new HashMap<String, Object>());
			// introspector
			INTROSPECTOR = CONTEXT.createJAXBIntrospector();
		} catch (Exception e) { }
	}

	public static <T> String typeToXml(final T obj) throws IOException {
		try {
			final Marshaller marshaller = CONTEXT.createMarshaller();
			final StringWriter stringWriter = new StringWriter();
			if (null == INTROSPECTOR.getElementName(obj)) {
				marshaller.marshal(createType(obj), stringWriter);
			} else {
				marshaller.marshal(obj, stringWriter);
			}
			return stringWriter.toString();
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T typeFromXml(final String payload) throws IOException {
		try {
			return (T) JAXBIntrospector.getValue(CONTEXT.createUnmarshaller().unmarshal(new StringReader(payload)));
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static <T> void typeToFile(final T obj,
			final File file) throws IOException {
		try {
			final Marshaller marshaller = CONTEXT.createMarshaller();
			final OutputStream os = new FileOutputStream(file, false);
			if (null == INTROSPECTOR.getElementName(obj)) {
				marshaller.marshal(createType(obj), os);
			} else {
				marshaller.marshal(obj, os);
			}			
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T typeFromFile(final File file) throws IOException {
		try {
			return (T) JAXBIntrospector.getValue(CONTEXT.createUnmarshaller().unmarshal(new StreamSource(file)));
		} catch (JAXBException e) {
			throw new IOException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(GBSet.class)) {
			element = FACTORY.createGBSet();
		} else if (clazz.equals(GBSeq.class)) {
			element = FACTORY.createGBSeq();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}	

}