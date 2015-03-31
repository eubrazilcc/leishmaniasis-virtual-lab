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

package eu.eubrazilcc.lvl.oauth2;

import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.oauth2.rest.jackson.LinkedInMapper.createLinkedInMapper;
import static eu.eubrazilcc.lvl.oauth2.util.TestUtils.getLinkedInJsonFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.hamcrest.number.OrderingComparison.lessThan;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import eu.eubrazilcc.lvl.oauth2.rest.jackson.LinkedInMapper;

/**
 * Tests {@link LinkedInMapper}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LinkedInJsonTest {

	private final String INDUSTRY_DEFAULT = "DEFAULT_INDUSTRY";
	private final Set<String> POSITION_DEFAULT = newHashSet("DEFAULT_POSITION");

	@Test
	public void test() {
		System.out.println("LinkedInJsonTest.test()");
		try {

			// test read from JSON message
			int total = 0, withNoIndustry = 0, withNoPositions = 0;
			final Collection<File> files = getLinkedInJsonFiles();
			for (final File file : files) {
				System.out.println(" >> JSON file: " + file.getCanonicalPath());
				final String payload = readFileToString(file);
				assertThat("payload is not null", payload, notNullValue());
				assertThat("payload is not empty", isNotBlank(payload), equalTo(true));

				final LinkedInMapper linkedInMapper = createLinkedInMapper()
						.readObject(payload);
				final String userId = linkedInMapper.getUserId();
				assertThat("user Id is not null", userId, notNullValue());
				assertThat("user Id is not empty", isNotBlank(userId), equalTo(true));
				final String emailAddress = linkedInMapper.getEmailAddress();
				assertThat("email address is not null", emailAddress, notNullValue());
				assertThat("email address is not empty", isNotBlank(emailAddress), equalTo(true));
				final String firstName = linkedInMapper.getFirstName();
				assertThat("firstname is not null", firstName, notNullValue());
				assertThat("firstname is not empty", isNotBlank(firstName), equalTo(true));
				final String lastName = linkedInMapper.getLastName();
				assertThat("lastname is not null", lastName, notNullValue());
				assertThat("lastname is not empty", isNotBlank(lastName), equalTo(true));
				final String industry = linkedInMapper.getIndustry().or(INDUSTRY_DEFAULT);
				assertThat("industry is not null", industry, notNullValue());
				assertThat("industry is not empty", isNotBlank(industry), equalTo(true));
				if (INDUSTRY_DEFAULT.equals(industry)) withNoIndustry++;
				final Set<String> positions = linkedInMapper.getPositions().or(POSITION_DEFAULT);
				assertThat("positions is not null", positions, notNullValue());
				assertThat("positions is not empty", positions.isEmpty(), equalTo(false));
				if (POSITION_DEFAULT.equals(positions)) withNoPositions++;
				total++;
				/* uncomment for additional output */
				System.out.println(" >> LinkedIn profile: usedid='" + userId + "', email='" + emailAddress + "', firstname='" + firstName
						+ "', lastname='" + lastName + "', industry='" + industry + "', possitions='" + join(positions, ',') + "'");
			}
			assertThat("total number of read JSON messages coincides with expected", total, allOf(greaterThan(0), equalTo(files.size())));
			assertThat("records with no industry coincides with expected", withNoIndustry, allOf(greaterThanOrEqualTo(1), lessThan(total)));
			assertThat("records with no positions coincides with expected", withNoPositions, allOf(greaterThanOrEqualTo(1), lessThan(total)));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("LinkedInJsonTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("LinkedInJsonTest.test() has finished");
		}
	}

}