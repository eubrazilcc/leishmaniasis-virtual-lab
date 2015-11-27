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

import javax.xml.bind.JAXBElement;

import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.ObjectFactory;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.RequestType;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.ResponseType;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.SearchTemplateType;

/**
 * TDWG TAPIR access protocol for information retrieval XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.tdwg.org/activities/tapir/specification">TAPIR - TDWG Access Protocol for Information Retrieval</a>
 */
public class TapirXmlBinder extends XmlBinder {

	private static final Class<?>[] SUPPORTED_CLASSES = {
		SearchTemplateType.class,
		RequestType.class,
		ResponseType.class
	};

	public static final ObjectFactory TAPIR_XML_FACTORY = new ObjectFactory();

	public static final TapirXmlBinder TAPIR_XMLB = new TapirXmlBinder();

	private TapirXmlBinder() {
		super(SUPPORTED_CLASSES);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(SearchTemplateType.class)) {
			element = TAPIR_XML_FACTORY.createSearchTemplate((SearchTemplateType)obj);
		} else if (clazz.equals(RequestType.class)) {
			element = TAPIR_XML_FACTORY.createRequest((RequestType)obj);
		} else if (clazz.equals(ResponseType.class)) {
			element = TAPIR_XML_FACTORY.createResponse((ResponseType)obj);
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}

}