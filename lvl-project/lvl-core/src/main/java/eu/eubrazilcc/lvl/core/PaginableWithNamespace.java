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

package eu.eubrazilcc.lvl.core;

import static com.google.common.base.MoreObjects.toStringHelper;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

/**
 * Extends {@link Paginable} to add a name-space property.
 * @param <T> the type of objects that this class stores
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class PaginableWithNamespace<T> extends Paginable<T> {

	private String namespace;

	public PaginableWithNamespace() {
		super();
		setNamespace(LVL_DEFAULT_NS);
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = urlEncodeUtf8(defaultIfBlank(namespace, LVL_DEFAULT_NS).trim());
	}

	@Override
	public String toString() {
		return toStringHelper(this)
				.add("Paginable", super.toString())
				.add("namespace", namespace)			
				.toString();
	}

}