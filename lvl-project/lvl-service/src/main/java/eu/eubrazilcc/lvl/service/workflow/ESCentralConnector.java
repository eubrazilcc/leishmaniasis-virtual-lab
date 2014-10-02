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

package eu.eubrazilcc.lvl.service.workflow;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;

import com.connexience.api.WorkflowClient;
import com.connexience.api.model.EscWorkflow;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Workflow connector based on e-Science Central.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="http://www.esciencecentral.co.uk/">e-Science Central</a>
 * @see <a href="http://sourceforge.net/projects/esciencecentral/">e-Science Central at SourceForge.org</a>
 */
public enum ESCentralConnector implements Closeable2 {

	ESCENTRAL_CONN;

	private static final Logger LOGGER = getLogger(ESCentralConnector.class);

	private Lock mutex = new ReentrantLock();
	private WorkflowClient __client = null;

	private WorkflowClient client() {
		mutex.lock();
		try {
			if (__client == null) {
				__client = new WorkflowClient("localhost", 8080, false, "myusername", "mypassword");
			}
			return __client;
		} finally {
			mutex.unlock();
		}
	}

	public void listWorkflows() {
		try {
			final EscWorkflow[] userWorkflows = client().listWorkflows();
			for (final EscWorkflow w : userWorkflows) {
				System.out.println(w.getName() + " -- " + w.getId());
			}
		} catch (Exception e) {
			LOGGER.error("Failed to list workflows", e);
		}
	}

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("e-Science Central connector initialized successfully");
	}

	@Override
	public void close() throws IOException {
		mutex.lock();
		try {
			if (__client != null) {
				__client = null;
			}
		} finally {
			mutex.unlock();
			LOGGER.info("e-Science Central connector shutdown successfully");
		}
	}

}