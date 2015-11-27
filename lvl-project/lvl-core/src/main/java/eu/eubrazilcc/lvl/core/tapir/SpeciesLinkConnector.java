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

package eu.eubrazilcc.lvl.core.tapir;

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager.TapirCollection;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecordSet;
import eu.eubrazilcc.lvl.core.xml.tdwg.tapir.ResponseType;

/**
 * Implements a speciesLink client that uses the TAPIR protocol to access the collections provided with this service. The configuration 
 * is obtained from the application's configuration, which is expected to be properly configured by listing the server capabilities (i.e.
 * by executing the TAPIR command: <tt>http://tapir.cria.org.br/tapirlink/tapir.php/specieslink?op=capabilities</tt>).
 * @author Erik Torres <ertorser@upv.es>
 */
public class SpeciesLinkConnector extends TapirClient {

	private static final Logger LOGGER = getLogger(SpeciesLinkConnector.class);

	private final TapirCollection tapir = CONFIG_MANAGER.getSpeciesLink();
	private final Map<String, String> collections;

	public SpeciesLinkConnector() {
		this.collections = (tapir != null ? tapir.getCollections() : Collections.<String, String>emptyMap());
	}

	public Set<String> collectionNames() {
		return collections.keySet();
	}

	public long count(final String collection) {
		final Entry<String, String> entry = getCollection(collection);
		long count = 0l;
		try {
			final ResponseType response = count(tapir.getUrl().toString(), tapir.getConcept(), parseFilter(tapir.getFilter(), entry.getValue()));
			checkArgument(response != null && response.getInventory() != null && response.getInventory().getRecord() != null
					&& response.getInventory().getRecord().size() == 1 && response.getInventory().getRecord().get(0).getCount() != null,
					"Invalid or incomplete server response");
			count = response.getInventory().getRecord().get(0).getCount().longValue();			
		} catch (URISyntaxException | IOException e) {
			LOGGER.error("Failed to obtain the number of elements in the collection: " + collection, e);
		}
		return count;
	}

	public SimpleDarwinRecordSet fetch(final String collection, final int start, final int limit) {
		SimpleDarwinRecordSet dwcSet = null;
		final Entry<String, String> entry = getCollection(collection);
		try {
			dwcSet = fetchDarwinCore(tapir.getUrl().toString(), tapir.getOutputModel(), 
					parseFilter(tapir.getFilter(), entry.getValue()), tapir.getOrderby(), start, limit);
		} catch (URISyntaxException | IOException | XPathExpressionException | SAXException | ParserConfigurationException e) {
			LOGGER.error("Failed to fetch elements from collection: " + collection, e);
		}
		return dwcSet;
	}

	private Entry<String, String> getCollection(final String collection) {		
		String key = null, value = null;
		checkArgument(isNotBlank(key = trimToNull(collection)) && isNotBlank(value = trimToNull(collections.get(key))), 
				"Uninitialized or invalid collection");
		return new SimpleImmutableEntry<String, String>(key, value);
	}

}