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

package eu.eubrazilcc.lvl.core.conf;

import static org.slf4j.LoggerFactory.getLogger;
import static org.slf4j.bridge.SLF4JBridgeHandler.install;
import static org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Manages loggers, installing the necessary bridges to unify logging.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum LogManager implements Closeable2 {

	INSTANCE;

	private final static Logger LOGGER = getLogger(LogManager.class);

	private LogManager() {
		// remove existing handlers attached to j.u.l root logger
		removeHandlersForRootLogger();

		// add SLF4JBridgeHandler to j.u.l's root logger, should be done once during the 
		// initialization phase of the application
		install();
	}

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("Log manager was loaded");		
	}

	@Override
	public void close() throws IOException { }

}