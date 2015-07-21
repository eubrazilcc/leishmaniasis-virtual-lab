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

package eu.eubrazilcc.lvl.oauth2.revocation;

import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.as.request.OAuthRequest;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.validators.AbstractValidator;
import org.apache.oltu.oauth2.common.validators.OAuthValidator;

/**
 * OAuth 2.0 token revocation request.
 * @author Erik Torres <ertorser@upv.es>
 */
public class OAuthTokenRevocationRequest extends OAuthRequest {

	public static String TOKEN           = "token";
	public static String TOKEN_TYPE_HINT = "token_type_hint";

	public static String ACCESS_TOKEN    = "access_token";
	public static String REFRESH_TOKEN   = "refresh_token";

	public OAuthTokenRevocationRequest(final HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {
		super(request);
	}

	@Override
	protected OAuthValidator<HttpServletRequest> initValidator() throws OAuthProblemException, OAuthSystemException {
		return new TokenRevocationValidator();
	}

	public static class TokenRevocationValidator extends AbstractValidator<HttpServletRequest> {
		public TokenRevocationValidator() {
			requiredParams.add(OAuth.OAUTH_CLIENT_ID);
			requiredParams.add(OAuth.OAUTH_CLIENT_SECRET);
			requiredParams.add(TOKEN);
			optionalParams.put(TOKEN_TYPE_HINT, new String[]{ ACCESS_TOKEN, REFRESH_TOKEN });
			enforceClientAuthentication = true;
		}
	}

}