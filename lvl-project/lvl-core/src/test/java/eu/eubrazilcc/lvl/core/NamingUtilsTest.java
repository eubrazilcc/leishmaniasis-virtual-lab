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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.NO_NAME;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.URI_ID_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.mergeIds;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.splitIds;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toAsciiSafeName;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toId;
import static org.apache.commons.lang.StringUtils.countMatches;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

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

			// create sequence dataset
			final List<Sequence> sequences = newArrayList();
			for (int i = 0; i < 5; i++) {
				sequences.add(Sequence.builder()
						.dataSource(GENBANK)
						.accession("SEQ_" + i)
						.build());
			}

			// test sequence identifier creation (using long notation)
			String dataSource = sequences.get(0).getDataSource();
			String accession = sequences.get(0).getAccession();
			String seqId = toId(dataSource, accession, NOTATION_LONG);
			assertThat("sequence Id is not null", seqId, notNullValue());
			assertThat("sequence Id is not empty", isNotBlank(seqId), equalTo(true));
			assertThat("sequence Id coincides with expected", seqId, 
					equalTo(GENBANK + ID_FRAGMENT_SEPARATOR + accession));

			// test sequence identifier creation (using short notation)
			dataSource = sequences.get(1).getDataSource();
			accession = sequences.get(1).getAccession();
			seqId = toId(dataSource, accession, NOTATION_SHORT);
			assertThat("sequence Id is not null", seqId, notNullValue());
			assertThat("sequence Id is not empty", isNotBlank(seqId), equalTo(true));
			assertThat("sequence Id coincides with expected", seqId, 
					equalTo(GENBANK_SHORT + ID_FRAGMENT_SEPARATOR + accession));

			// test sequence identifier creation (using long notation and sequence class)
			Sequence sequence = sequences.get(2);
			seqId = toId(sequence, NOTATION_LONG);
			assertThat("sequence Id is not null", seqId, notNullValue());
			assertThat("sequence Id is not empty", isNotBlank(seqId), equalTo(true));
			assertThat("sequence Id coincides with expected", seqId, 
					equalTo(GENBANK + ID_FRAGMENT_SEPARATOR + sequence.getAccession()));

			// test sequence identifier creation (using short notation and sequence class)
			sequence = sequences.get(3);
			seqId = toId(sequence, NOTATION_SHORT);
			assertThat("sequence Id is not null", seqId, notNullValue());
			assertThat("sequence Id is not empty", isNotBlank(seqId), equalTo(true));
			assertThat("sequence Id coincides with expected", seqId, 
					equalTo(GENBANK_SHORT + ID_FRAGMENT_SEPARATOR + sequence.getAccession()));

			// test sequence Ids merging using long notation
			String mergedIds = mergeIds(sequences, NOTATION_LONG);
			assertThat("merged Ids is not null", mergedIds, notNullValue());
			assertThat("merged Ids is not empty", isNotBlank(mergedIds), equalTo(true));
			assertThat("merged Ids number of fragment separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(ID_FRAGMENT_SEPARATOR)), 
					equalTo(sequences.size()));
			assertThat("merged Ids number of ids separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(URI_ID_SEPARATOR)), 
					equalTo(sequences.size() - 1));
			assertThat("merged Ids number of data sources coincides with expected", 
					countMatches(mergedIds, String.valueOf(GENBANK)), 
					equalTo(sequences.size()));
			/* uncomment for additional output */
			System.out.println(" >> merged Ids (using long notation): " + mergedIds);

			// test sequence Ids splitting using long notation
			List<String> idsList = splitIds(mergedIds);
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(sequences.size()));
			for (final String id : idsList) {
				validate(id, GENBANK);
			}
			/* uncomment for additional output */
			System.out.println(" >> split Ids (using long notation): " + idsList);

			// test sequence Ids merging using short notation			
			mergedIds = mergeIds(sequences, NOTATION_SHORT);
			assertThat("merged Ids is not null", mergedIds, notNullValue());
			assertThat("merged Ids is not empty", isNotBlank(mergedIds), equalTo(true));
			assertThat("merged Ids number of fragment separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(ID_FRAGMENT_SEPARATOR)), 
					equalTo(sequences.size()));
			assertThat("merged Ids number of ids separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(URI_ID_SEPARATOR)), 
					equalTo(sequences.size() - 1));
			assertThat("merged Ids number of data sources coincides with expected", 
					countMatches(mergedIds, String.valueOf(GENBANK_SHORT)), 
					equalTo(sequences.size()));
			/* uncomment for additional output */
			System.out.println(" >> merged Ids (using short notation): " + mergedIds);

			// test sequence Ids splitting using short notation
			idsList = splitIds(mergedIds);
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(sequences.size()));
			for (final String id : idsList) {
				validate(id, GENBANK_SHORT);
			}
			/* uncomment for additional output */
			System.out.println(" >> split Ids (using long notation): " + idsList);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("NamingUtilsTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("NamingUtilsTest.test() has finished");
		}
	}

	private static void validate(final String id, final String dataSource) {
		assertThat("split Id is not null", id, notNullValue());
		assertThat("split Id is not empty", isNotBlank(id), equalTo(true));
		assertThat("split Id coincides with expected", id.startsWith(dataSource + ID_FRAGMENT_SEPARATOR), 
				equalTo(true));
	}

}