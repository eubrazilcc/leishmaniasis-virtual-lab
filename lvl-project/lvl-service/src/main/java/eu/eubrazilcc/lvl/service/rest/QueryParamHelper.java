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

package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlDecodeUtf8;
import static eu.eubrazilcc.lvl.storage.security.IdentityProviderHelper.OWNERID_EL_TEMPLATE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.trimToNull;

import javax.ws.rs.WebApplicationException;

/**
 * Utility class to deal with query parameters.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class QueryParamHelper {

	public static final String parseParam(final String param) {
		String param2 = null;
		if (isBlank(param) || isBlank(param2 = trimToNull(urlDecodeUtf8(param)))) {
			throw new WebApplicationException("Missing required parameters", BAD_REQUEST);
		}
		return param2;
	}

	public static final String ns2permission(final String namespace) {
		return LVL_DEFAULT_NS.equals(namespace) ? OWNERID_EL_TEMPLATE : namespace;
	}

	public static final String ns2dbnamespace(final String namespace, final String ownerid) {
		return LVL_DEFAULT_NS.equals(namespace) ? ownerid : namespace;
	}

}