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

package eu.eubrazilcc.lvl.storage.security.el;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import eu.eubrazilcc.lvl.storage.security.User;

/**
 * Builds and parses permission expressions from EL (expression language) templates.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://java.net/projects/el-spec/">Expression Language Specification</a>
 */
public final class PermissionElBuilder {

	public static final String EL_PARAMETER_PATTERN = ".*\\$\\{[a-zA-Z_0-9.]+\\}.*";
	
	private static final ExpressionFactory EXPR_FACTORY = ExpressionFactory.newInstance();

	/**
	 * Builds a permission expression from a template compatible with EL (expression language). The following are valid examples of
	 * permission templates which includes at least one property from {@link User}, considering the userid <tt>username</tt> and 
	 * the provider <tt>lvl</tt>:
	 * <ul>
	 * <li><tt>level1:${user.userid}:level3</tt> - builds to: <tt>level1:username:level3</tt></li>
	 * <li><tt>level1:${user.userid}@${user.provider}</tt> - builds to: <tt>level1:username@lvl</tt></li>
	 * <li><tt>${user.userid}:${user.userid}@${user.provider}</tt> - <tt>username:username@lvl</tt></li>
	 * </ul>
	 * @param template - permission template to be parsed and completed with user details
	 * @param user - contains the details of the user
	 * @return A permission expression where the EL parameters are replaced by the corresponding properties from the specified user.
	 */
	public static String buildPermission(final String template, final User user) {
		checkNotNull(template, "Uninitialized or invalid permission template");
		checkArgument(user != null && isNotBlank(user.getUserid()) && isNotBlank(user.getProvider()), "Uninitialized or invalid user");
		final PermissionElContext ctxt = PermissionElContext.builder().user(user).build();
		final String template2 = template.trim();
		final ValueExpression expr = EXPR_FACTORY.createValueExpression(ctxt, template2, String.class);
		return expr.getValue(ctxt).toString();
	}

}