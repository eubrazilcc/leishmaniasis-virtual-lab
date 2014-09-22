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

import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

import eu.eubrazilcc.lvl.storage.oauth2.User;

/**
 * Encapsulates the information for use with the EL expression evaluator.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ScopeElContext extends ELContext {

	private User user;

	public ScopeElContext() { }

	public User getUser() {
		return user;
	}

	public void setUser(final User user) {
		this.user = user;
	}

	@Override
	public ELResolver getELResolver() {
		final CompositeELResolver resolver = new CompositeELResolver();
		resolver.add(new ScopeElContextResolver(user));
		resolver.add(new BeanELResolver(true));
		return resolver;
	}

	@Override
	public FunctionMapper getFunctionMapper() {
		return null;
	}

	@Override
	public VariableMapper getVariableMapper() {
		return null;
	}

	/* Fluent API */

	public static Builder builder() {
		return new Builder();
	}	

	public static class Builder {

		private final ScopeElContext instance = new ScopeElContext();

		public Builder user(final User user) {
			instance.setUser(user);
			return this;
		}

		public ScopeElContext build() {
			return instance;
		}

	}

}