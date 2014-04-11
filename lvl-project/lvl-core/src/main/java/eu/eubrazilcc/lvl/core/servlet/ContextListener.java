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

package eu.eubrazilcc.lvl.core.servlet;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Implements a Servlet Context Listener that catches the context name that is assigned 
 * to the Servlet when is first created at runtime. Also, it can manage the release of
 * resources when the Servlet is stopped, as well as pre-loading of objects in cache.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ContextListener implements ServletContextListener {

	private static ServletContext SERVLET_CONTEXT = null;

	private static final ServiceStopper STOPPER = ServiceStopper.create();

	private static final Lock mutex = new ReentrantLock();

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		mutex.lock();
		try {
			SERVLET_CONTEXT = event.getServletContext();			
		} finally {
			mutex.unlock();
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		mutex.lock();
		try {
			STOPPER.stopAndWait();
			SERVLET_CONTEXT = null;
		} finally {
			mutex.unlock();
		}
	}

	public static ServletContext getServletContext() {
		mutex.lock();
		try {
			return SERVLET_CONTEXT;
		} finally {
			mutex.unlock();
		}
	}

	public static ServiceStopper getServiceStopper() {
		mutex.lock();
		try {
			return STOPPER;
		} finally {
			mutex.unlock();
		}
	}
	
}