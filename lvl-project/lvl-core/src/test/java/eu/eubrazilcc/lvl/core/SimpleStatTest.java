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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static eu.eubrazilcc.lvl.core.SimpleStat.normalizeStats;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests {@link SimpleStat} class.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SimpleStatTest {

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	
	@Test
	public void test() {
		System.out.println("SimpleStatTest.test()");
		try {
			// test with less than 10 elements
			final List<SimpleStat> stats = newArrayList();
			for (int i = 0; i < 7; i++) {
				stats.add(SimpleStat.builder()
						.label("item" + i)
						.value(i)
						.build());
			}
			shuffle(stats);
			System.err.println(" >> Before (<10): " + Arrays.toString(stats.toArray(new SimpleStat[stats.size()])));
			List<SimpleStat> normalized = normalizeStats(stats);
			assertThat("normalized list is not null", normalized, notNullValue());
			assertThat("normalized size coincides with expected", normalized.size(), equalTo(stats.size()));			
			System.err.println(" >> After (<10): " + Arrays.toString(normalized.toArray(new SimpleStat[normalized.size()])));

			// test with exactly 10 elements
			for (int i = 7; i < 10; i++) {
				stats.add(SimpleStat.builder()
						.label("item" + i)
						.value(i)
						.build());
			}
			shuffle(stats);
			System.err.println(" >> Before (=10): " + Arrays.toString(stats.toArray(new SimpleStat[stats.size()])));
			normalized = normalizeStats(stats);
			assertThat("normalized list is not null", normalized, notNullValue());
			assertThat("normalized size coincides with expected", normalized.size(), equalTo(stats.size()));			
			System.err.println(" >> After (=10): " + Arrays.toString(normalized.toArray(new SimpleStat[normalized.size()])));

			// test with exactly 11 elements
			for (int i = 10; i < 11; i++) {
				stats.add(SimpleStat.builder()
						.label("item" + i)
						.value(i)
						.build());
			}
			shuffle(stats);
			System.err.println(" >> Before (=11): " + Arrays.toString(stats.toArray(new SimpleStat[stats.size()])));
			normalized = normalizeStats(stats);
			assertThat("normalized list is not null", normalized, notNullValue());
			assertThat("normalized size coincides with expected", normalized.size(), equalTo(11));			
			System.err.println(" >> After (=11): " + Arrays.toString(normalized.toArray(new SimpleStat[normalized.size()])));

			// test with more than 11 elements
			for (int i = 11; i < 13; i++) {
				stats.add(SimpleStat.builder()
						.label("item" + i)
						.value(i)
						.build());
			}
			shuffle(stats);
			System.err.println(" >> Before (>11): " + Arrays.toString(stats.toArray(new SimpleStat[stats.size()])));
			normalized = normalizeStats(stats);
			assertThat("normalized list is not null", normalized, notNullValue());
			assertThat("normalized size coincides with expected", normalized.size(), equalTo(11));			
			System.err.println(" >> After (>11): " + Arrays.toString(normalized.toArray(new SimpleStat[normalized.size()])));

			// test JSON serialization
			final String payload = JSON_MAPPER.writeValueAsString(normalized);
			assertThat("serialized list is not null", payload, notNullValue());
			assertThat("serialized list is not empty", isNotBlank(payload), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Serialized list (JSON): " + payload);

			// test JSON deserialization			
			final JavaType type = JSON_MAPPER.getTypeFactory().constructParametricType(List.class, SimpleStat.class);
			final List<SimpleStat> normalized2 = JSON_MAPPER.readValue(payload, type);
			assertThat("deserialized list is not null", normalized2, notNullValue());
			assertThat("deserialized list coincides with expected", normalized2, equalTo(normalized));
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SimpleStatTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SimpleStatTest.test() has finished");
		}
	}

}