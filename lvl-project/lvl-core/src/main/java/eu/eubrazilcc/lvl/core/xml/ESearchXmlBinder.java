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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.util.List;

import javax.xml.bind.JAXBElement;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.Count;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.ESearchResult;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.Id;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.IdList;
import eu.eubrazilcc.lvl.core.xml.ncbi.esearch.ObjectFactory;

/**
 * NCBI Entrez ESearch XML binding helper.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ESearchXmlBinder extends XmlBinder {

	private static final Class<?>[] SUPPORTED_CLASSES = {
		ESearchResult.class
	};

	public static final ObjectFactory ESEARCH_XML_FACTORY = new ObjectFactory();	

	public static final ESearchXmlBinder ESEARCH_XMLB = new ESearchXmlBinder();

	private ESearchXmlBinder() {
		super(SUPPORTED_CLASSES);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> JAXBElement<T> createType(final T obj) {
		Object element = null;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.equals(ESearchResult.class)) {
			element = ESEARCH_XML_FACTORY.createESearchResult();		
		} else {
			throw new IllegalArgumentException("Unsupported type: " + clazz.getCanonicalName());
		}
		return (JAXBElement<T>) element;
	}

	public static int getCount(final ESearchResult result) {
		checkArgument(result != null, "Uninitialized result");
		int count = -1;
		final List<Object> elements = result.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR();
		if (elements != null) {
			for (int i = 0; i < elements.size() && count < 0; i++) {
				if (elements.get(i) instanceof Count) {
					count = Integer.parseInt(((Count)elements.get(i)).getvalue());
				}
			}
		}
		checkState(count >= 0, "Count node not found");
		return count;
	}

	public static List<String> getIds(final ESearchResult result) {
		checkArgument(result != null, "Uninitialized result");
		List<String> ids = null;
		final List<Object> elements = result.getCountOrRetMaxOrRetStartOrQueryKeyOrWebEnvOrIdListOrTranslationSetOrTranslationStackOrQueryTranslationOrERROR();
		if (elements != null) {
			for (int i = 0; i < elements.size() && ids == null; i++) {
				if (elements.get(i) instanceof IdList) {
					ids = from(((IdList)elements.get(i)).getId()).transform(new Function<Id, String>() {
						@Override
						public String apply(final Id id) {							
							return id != null ? trimToNull(id.getvalue()) : null;
						}
					}).filter(notNull()).toList();
				}
			}
		}
		if (ids == null) {
			ids = newArrayList();
		}
		return ids;
	}

}