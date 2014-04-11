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

package eu.eubrazilcc.lvl.oauth2.security;

import org.apache.oltu.oauth2.as.issuer.ValueGenerator;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider;

/**
 * OAuth3 Token Generator.
 * @author Erik Torres <ertorser@upv.es>
 */
public class OAuth2TokenGenerator implements ValueGenerator {

	@Override
	public String generateValue() throws OAuthSystemException {
		try {
			return SecretProvider.generateSecret();
		} catch (Exception e) {
			throw new OAuthSystemException("OAuth2 token cannot be generated", e);
		}
	}

	@Override
	public String generateValue(final String param) throws OAuthSystemException {
		try {
			return (param != null ? SecretProvider.generateSecret(param) : generateValue());
		} catch (Exception e) {
			throw new OAuthSystemException("OAuth2 token cannot be generated", e);
		}
	}

}