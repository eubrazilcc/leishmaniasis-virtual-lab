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

package eu.eubrazilcc.lvl.storage.oauth2.security.el;

import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.inheritElUsername;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import eu.eubrazilcc.lvl.storage.oauth2.User;

/**
 * Builds and parses scope expression from EL (expression language) templates.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://java.net/projects/el-spec/">Expression Language Specification</a>
 */
public final class ScopeElBuilder {

	private static final ExpressionFactory EXPR_FACTORY = ExpressionFactory.newInstance();

	public static String buildScope(final String scope, final User user) {
		final ScopeElContext ctxt = ScopeElContext.builder().user(user).build();
		final String template = inheritElUsername(scope);
		final ValueExpression expr = EXPR_FACTORY.createValueExpression(ctxt, template, String.class);
		return expr.getValue(ctxt).toString();
	}

}