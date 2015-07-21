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

import eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy.ObjectFactory;
import eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy.TaxaSet;
import eu.eubrazilcc.lvl.core.xml.ncbi.taxonomy.Taxon;

/**
 * NCBI taxonomy XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public class TaxonomyXmlBinder extends XmlBinder {

	private static final Class<?>[] SUPPORTED_CLASSES = {
		TaxaSet.class,
		Taxon.class
	};

	public static final ObjectFactory TAXONOMY_XML_FACTORY = new ObjectFactory();

	public static final TaxonomyXmlBinder TAXONOMY_XMLB = new TaxonomyXmlBinder();

	private TaxonomyXmlBinder() {
		super(SUPPORTED_CLASSES);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(TaxaSet.class)) {
			element = TAXONOMY_XML_FACTORY.createTaxaSet();
		} else if (clazz.equals(Taxon.class)) {
			element = TAXONOMY_XML_FACTORY.createTaxon();
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}

}