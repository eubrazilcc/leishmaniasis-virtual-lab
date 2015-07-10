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

package eu.eubrazilcc.lvl.storage;

import static eu.eubrazilcc.lvl.core.conf.ConfigurationFinder.DEFAULT_LOCATION;
import static eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.TEST_CONFIG_DIR;
import static java.io.File.separator;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.actor.DataImportManager;
import eu.eubrazilcc.lvl.storage.actor.LeishVlActorSystem;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.ConditionalIgnore;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Tests {@link DataImportManager}. <strong>Be sure that you really need to execute this test!</strong>
 * @author Erik Torres <ertorser@upv.es>
 */
public class DataImportManagerTest {

	/* TODO	
	
	1) If one actor manages the work another actor is doing, e.g. by passing on sub-tasks, 
	then the manager should supervise the child. The reason is that the manager knows which 
	kind of failures are expected and how to handle them.
	
	2) If one actor depends on another actor for carrying out its duty, it should watch that 
	other actor’s liveness and act upon receiving a termination notice. This is different from 
	supervision, as the watching party has no influence on the supervisor strategy, and it should 
	be noted that a functional dependency alone is not a criterion for deciding where to place a 
	certain child actor in the hierarchy.
	
	3) Actors should process events and generate responses (or more requests) in an event-driven 
	manner. Actors should not block (i.e. passively wait while occupying a Thread) on some external 
	entity —which might be a lock, a network socket, etc.— unless it is unavoidable. The following
	solutions are available when blocking is unavoidable:
	
	3a) Do the blocking call within a set of actors managed by a router, making sure to configure 
	a thread pool which is either dedicated for this purpose or sufficiently sized.
	
	A common patter is to create a router for N actors, each of which wraps a single data source 
	connection and handles queries as sent to the router. The number N must then be tuned for 
	maximum throughput, which will vary depending on which data source is targeted.
	
	Configuring thread pools is a task best delegated to Akka, simply configure in the application.conf 
	and instantiate through an ActorSystem.
	
	4) Do not pass mutable objects between actors. In order to ensure that, prefer immutable messages.
	
	5) Do not routinely send behavior within messages.
	
	 */
	
	
	@Rule
	public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

	@Test
	@ConditionalIgnore(condition=DbImportManagerTestIsFound.class)
	public void test() {
		System.out.println("DataImportManagerTest.test()");
		try {
			
			// TODO
			
			try (final LeishVlActorSystem actorSystem = new LeishVlActorSystem(null)) {
				actorSystem.preload();
				Thread.sleep(2000l);
			}

			// TODO
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DataImportManagerTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DataImportManagerTest.test() has finished");
		}
	}

	/**
	 * Checks whether a flag file is available in the local filesystem.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public class DbImportManagerTestIsFound implements IgnoreCondition {
		private final File file = new File(concat(DEFAULT_LOCATION, TEST_CONFIG_DIR + separator + "data-import-manager.test"));
		@Override
		public boolean isSatisfied() {			
			return !file.canRead();
		}		
	}

}