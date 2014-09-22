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

import static com.google.common.collect.Maps.newHashMap;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

import eu.eubrazilcc.lvl.storage.oauth2.User;

/**
 * The context resolver that resolves the instance objects that can be used in a scope EL expression.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ScopeElContextResolver extends ELResolver {

	private Map<String, Object> scopeObjects;
	public final static String USER_OBJECT = "user";

	public ScopeElContextResolver(final User user) {
		scopeObjects = newHashMap();
		scopeObjects.put(USER_OBJECT, user);
	}

	@Override
	public Class<?> getType(final ELContext ctxt, final Object base, final Object property) {
		if (isHandled(ctxt, base, property)) {
			return getValue(ctxt, base, property).getClass();
		}
		return null;
	}

	@Override
	public Class<?> getCommonPropertyType(final ELContext ctxt, final Object base) {
		return Object.class;
	}	

	@Override
	public Object getValue(final ELContext ctxt, final Object base, final Object property) {
		if (isHandled(ctxt, base, property)) {
			return scopeObjects.get(property.toString());
		}
		return null;
	}

	@Override
	public boolean isReadOnly(final ELContext ctxt, final Object base, final Object property) {
		if (isHandled(ctxt, base, property)) {
			return true;
		}
		return false;
	}

	@Override
	public void setValue(final ELContext ctxt, final Object base, final Object property, final Object value) {
		throw new PropertyNotWritableException(value.toString());
	}	

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(final ELContext ctxt, final Object base) {		
		return null;
	}	

	private boolean isHandled(final ELContext ctxt, final Object base, final Object property) {
		if (base != null) {
			return false;
		}
		if (scopeObjects.containsKey(property.toString())) {
			ctxt.setPropertyResolved(true);
			return true;
		}
		return false;
	}

}