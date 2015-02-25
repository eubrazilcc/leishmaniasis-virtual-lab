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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Integration tests. <code>FullDatabaseLoadTest.class</code> performs a full-database load and is disabled by default because it consumes a 
 * lot of system resources and processing time. Activate this test only when you're completely sure that you need to check this feature.
 * @author Erik Torres <ertorser@upv.es>
 */
@RunWith(Suite.class)
/* TODO @SuiteClasses({ ServiceTest.class, FullDatabaseLoadTest.class, WorkflowTest.class }) */
@SuiteClasses({ WorkflowTest.class })
public class AllIntegrationTests {

	@BeforeClass
	public static void setup() {
		System.out.println("AllIntegrationTests.setup()");		
	}

	@AfterClass
	public static void release() {
		System.out.println("AllIntegrationTests.release()");
	}

}