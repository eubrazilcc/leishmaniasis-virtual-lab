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

package eu.eubrazilcc.lvl.oauth2.rest;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.slf4j.Logger;

import com.google.common.base.Function;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.conf.LogManager;
import eu.eubrazilcc.lvl.oauth2.SingletonService;

/**
 * OAuth2 JAX-RS application.
 * @author Erik Torres <ertorser@upv.es>
 */
public class OAuth2Application extends Application {

	private final static Logger LOGGER = getLogger(OAuth2Application.class);

	public static final String SERVICE_NAME = ConfigurationManager.LVL_NAME + " OAuth2 RESTful Web service";

	final Set<Class<?>> classes = newHashSet();
	final Set<Object> instances = newHashSet();

	public OAuth2Application() {
		// load logging bridges
		LogManager.INSTANCE.preload();
		// start service
		SingletonService.INSTANCE.service();
		// create LVL resources
		instances.add(new OAuth2Registration());
		instances.add(new OAuth2AuthzServer());
		instances.add(new OAuth2Token());
		instances.add(new OAuth2TokenRevocation());
		instances.add(new IdentityProvider());
		instances.add(new UserRegistration());
		// add additional JAX-RS providers
		classes.add(MoxyJsonFeature.class);
		LOGGER.info(SERVICE_NAME + " initialized successfully, registered resources: " + objectsClassNames(instances));
	}

	@Override
	public Set<Class<?>> getClasses() {		
		return classes;		
	}

	@Override
	public Set<Object> getSingletons() {
		return instances;
	}

	private static final String objectsClassNames(final Collection<Object> objects) {
		return Arrays.toString(newArrayList(transform(objects, new Function<Object, String>() {
			@Override
			public String apply(final Object obj) {
				return obj.getClass().getCanonicalName();
			}			
		})).toArray(new String[objects.size()]));
	}

}