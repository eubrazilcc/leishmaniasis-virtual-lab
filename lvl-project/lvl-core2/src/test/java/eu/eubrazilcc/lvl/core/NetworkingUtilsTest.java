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

package eu.eubrazilcc.lvl.core;

import static eu.eubrazilcc.lvl.core.util.NetworkingUtils.getInet4Address;
import static eu.eubrazilcc.lvl.core.util.NetworkingUtils.isPortAvailable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.net.ServerSocket;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import org.junit.Test;

/**
 * Tests networking utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NetworkingUtilsTest {

	@Test
	public void test() {
		System.out.println("NetworkingUtilsTest.test()");
		try {
			// check IP address discover
			final String address = getInet4Address();
			assertThat("host IP address is not null", address, notNullValue());
			assertThat("Host IP address is not empty", isNotBlank(address));
			
			// check port availability
			final int port = 33435;
			assertThat("port " + port + " is available", isPortAvailable(port), equalTo(true));
			try (final ServerSocket socket = new ServerSocket(port)) {
				assertThat("port " + port + " is unavailable", isPortAvailable(port), equalTo(false));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NetworkingUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("NetworkingUtilsTest.test() has finished");
		}
	}

}