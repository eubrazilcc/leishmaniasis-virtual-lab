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

package eu.eubrazilcc.lvl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.core.servlet.ContextListener;

/**
 * LVL service with an operational state, with methods to start and stop.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishVirtLabService extends AbstractIdleService {

	private final static Logger LOGGER = LoggerFactory.getLogger(LeishVirtLabService.class);

	public static final String SERVICE_NAME = ConfigurationManager.LVL_NAME + " service";

	public LeishVirtLabService() {
		LOGGER.info(SERVICE_NAME + " initialized successfully");
	}

	@Override
	protected void startUp() throws Exception {
		// load configuration and core services
		CloserService.INSTANCE.preload();
		// register for termination
		ContextListener.getServiceStopper().add(this);
		LOGGER.info(SERVICE_NAME + " started");
	}

	@Override
	protected void shutDown() throws Exception {		
		// close core services
		CloserService.INSTANCE.close();
		LOGGER.info(SERVICE_NAME + " terminated");
	}

}