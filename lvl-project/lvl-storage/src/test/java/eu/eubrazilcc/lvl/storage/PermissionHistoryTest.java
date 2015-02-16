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

package eu.eubrazilcc.lvl.storage;

import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.latestModification;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModificationType.GRANTED;
import static eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModificationType.REMOVED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;

import eu.eubrazilcc.lvl.storage.security.PermissionHistory;
import eu.eubrazilcc.lvl.storage.security.PermissionHistory.PermissionModification;

/**
 * Tests {@link PermissionHistory}
 * @author Erik Torres <ertorser@upv.es>
 */
public class PermissionHistoryTest {

	@Test
	public void test() {
		System.out.println("PermissionHistoryTest.test()");
		try {
			// create test dataset
			final PermissionHistory history = new PermissionHistory();
			history.getHistory().add(PermissionModification.builder()
					.permission("p1")
					.modificationDate(new Date(1234567887l))
					.modificationType(GRANTED)
					.build());
			final PermissionModification m1 = PermissionModification.builder()
					.permission("p1")
					.modificationDate(new Date(1234567889l))
					.modificationType(GRANTED)
					.build();
			history.getHistory().add(m1);
			history.getHistory().add(PermissionModification.builder()
					.permission("p2")
					.modificationDate(new Date(1234567891l))
					.modificationType(GRANTED)
					.build());
			history.getHistory().add(PermissionModification.builder()
					.permission("p2")
					.modificationDate(new Date(1234567890l))
					.modificationType(GRANTED)
					.build());
			history.getHistory().add(PermissionModification.builder()
					.permission("p1")
					.modificationDate(new Date(1234567888l))
					.modificationType(REMOVED)
					.build());

			// test permission sorting
			final PermissionModification latestModification = latestModification(history.getHistory(), "p1");
			assertThat("permission modification is not null", latestModification, notNullValue());
			assertThat("permission modification coincides with expected", latestModification, equalTo(m1));
			// uncomment for additional output
			System.out.println(" >> Latest permission modification: " + latestModification);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("PermissionHistoryTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("PermissionHistoryTest.test() has finished");
		}
	}

}