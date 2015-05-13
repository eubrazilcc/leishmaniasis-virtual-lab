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

package eu.eubrazilcc.lvl.core.servlet;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayDeque;
import java.util.Deque;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import eu.eubrazilcc.lvl.core.ShutdownHook;

/**
 * Much inspired in Guava {@code com.google.common.io.Closer}. It stops services started
 * from a Servlet context.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ServiceStopper {

	private static final Logger LOGGER = getLogger(ServiceStopper.class);

	private final Deque<ServiceHook> stack = new ArrayDeque<ServiceHook>(1);

	private ServiceStopper() { }

	public static ServiceStopper create() {
		return new ServiceStopper();
	}

	public <T extends AbstractIdleService> T add(final T service) {
		checkNotNull(service, "Uninitialized service");
		stack.push(new ServiceHook(service));
		return service;
	}

	public void stopAndWait() {
		LOGGER.trace("Stopping services");
		while (!stack.isEmpty()) {
			final ServiceHook service = stack.pop();
			if (Optional.fromNullable(service).isPresent()) {
				try {				
					service.stop();
				} catch (Exception ignore) { }
			}
		}
		LOGGER.trace("All services stopped");
	}

	public static class ServiceHook {
		final AbstractIdleService service;
		final String name;
		final ShutdownHook shutdownHook;
		public ServiceHook(final AbstractIdleService service) {
			this.service = service;
			this.name = service.getClass().getCanonicalName();
			// place a shutdown hook to terminate the service when the JVM is terminated, as a 
			// latest resource in case that the stopper is never caller by the servlet container
			this.shutdownHook = new ShutdownHook();
			this.shutdownHook.register(this.service);
		}
		public AbstractIdleService getService() {
			return service;
		}
		public String getName() {
			return name;
		}
		public final void stop() {			
			if (Optional.fromNullable(service).isPresent() && service.isRunning()) {
				LOGGER.info("Stopping service: " + name);
				// stop the service
				service.stopAsync().awaitTerminated();
				// cancel the shutdown hook
				shutdownHook.cancel();
				LOGGER.info("Service stopped: " + name);
			}
		}
	}

}