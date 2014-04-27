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

package eu.eubrazilcc.lvl.core;

import static org.junit.Assert.fail;

import org.junit.Test;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Test logging manager.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LogManagerTest {

	@Test
	public void test() {
		System.out.println("LogManagerTest.test()");
		try {
			// j.u.l. logger
			final java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(
					LogManagerTest.class.getCanonicalName());
			julLogger.setLevel(java.util.logging.Level.INFO);
			julLogger.info("This message was generated with j.u.l., logged with Logback+SL4J");
			
			// SLF4J logger
			final org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(LogManagerTest.class);
			slf4jLogger.info("This message was generated with SL4J, logged with Logback+SL4J");
			
			// print logback internal state 
			final LoggerContext loggerContext = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
			StatusPrinter.print(loggerContext);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LogManagerTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("LogManagerTest.test() has finished");
		}
	}

}