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

import static eu.eubrazilcc.lvl.core.util.NamingUtils.NO_NAME;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toAsciiSafeName;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests naming utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NamingUtilsTest {

	@Test
	public void test() {
		System.out.println("NamingUtilsTest.test()");
		try {
			// test safe name generation
			System.err.println(" >> Non-printable names");
			final String[] names = { "AquaMaps (beta version)", "  Artificial   Neural  Network ",
					"Bioclim)", "Climate_Space Model_ ", "\\ENFA (Ecological-Niche Factor Analysis)",
					"Réal", "éü" };
			final String[] safeNames = { "aquamaps_beta_version", "artificial_neural_network",
					"bioclim", "climate_space_model", "enfa_ecological_niche_factor_analysis",
					"r_al", NO_NAME };
			for (int i = 0; i < names.length; i++) {
				final String safeName = toAsciiSafeName(names[i]);
				System.out.println(" >> '" + names[i] + "' => "
						+ (isNotEmpty(safeName) ? "'" + safeName + "'" : "NULL"));
				assertThat("ASCII safe name is not null", safeName, notNullValue());
				assertThat("ASCII safe name is not empty", isNotBlank(safeName));
				assertThat("ASCII safe name coincides with original", safeName, equalTo(safeNames[i]));				
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NamingUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("NamingUtilsTest.test() has finished");
		}
	}

}