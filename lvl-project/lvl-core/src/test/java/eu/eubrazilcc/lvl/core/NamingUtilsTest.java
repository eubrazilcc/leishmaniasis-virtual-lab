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

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.DataSource.CLIOC_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK_SHORT;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_LONG;
import static eu.eubrazilcc.lvl.core.DataSource.Notation.NOTATION_SHORT;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.ID_FRAGMENT_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.NO_NAME;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.URI_ID_SEPARATOR;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactRandomUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.compactUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.escapeUrlPathSegment;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.expandUUID;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.mergeSampleIds;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.mergeSeqIds;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.splitIds;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toAsciiSafeName;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.toId;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.unescapeUrlPathSegment;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Tests naming utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class NamingUtilsTest extends LeishvlTestCase {

	public NamingUtilsTest() {
		super(false);
	}

	@Test
	public void test() {
		System.out.println("NamingUtilsTest.test()");
		try {
			// test compact UUID generation (22 characters long instead of 36)
			final Map<String, String> uuids = ImmutableMap.<String, String>builder()
					.put("50a9ee1f-17f4-4055-99fd-d9139b7198b2", "UKnuHxf0QFWZ_dkTm3GYsg")
					.put("001544c2-ae4d-49da-8fc0-c874a6249d0d", "ABVEwq5NSdqPwMh0piSdDQ")
					.put("dfcab578-45e8-4fbd-97fe-80f12da129d6", "38q1eEXoT72X_oDxLaEp1g")
					.build();
			for (final Map.Entry<String, String> e : uuids.entrySet()) {
				final String cuuid = compactUUID(UUID.fromString(e.getKey()));
				assertThat("Compact UUID coincides with expected", cuuid, allOf(notNullValue(), equalTo(e.getValue())));
				final UUID uuid = expandUUID(cuuid);
				assertThat("Expanded UUID coincides with expected", uuid, allOf(notNullValue(), equalTo(UUID.fromString(e.getKey()))));
				printMsg(" >> UUID: " + uuid.toString() + ", cUUID: " + cuuid);
			}
			final String cuuid = compactRandomUUID();
			assertThat("Compact random UUID is not empty", trim(cuuid), allOf(notNullValue(), not(equalTo(""))));
			final UUID uuid = expandUUID(cuuid);
			assertThat("Expanded random UUID is not null", uuid, notNullValue());

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
				sequences.add(Sandfly.builder()
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

			// test sequence Ids splitting
			List<String> idsList = splitIds("gb:DQ887647,gb:DQ887648,gb:DQ887649,gb:DQ887650,gb:DQ887651,gb:DQ887652,"
					+ "gb:HM992926,gb:HM992927,gb:HM992928,gb:HM992929,gb:HM992930,gb:JF766954,gb:JF766955,gb:JF766956,"
					+ "gb:JF766957,gb:JF766958,gb:JF766959,gb:JF766960,gb:JF766961,gb:JF766962,gb:JF766963,gb:JF766964,"
					+ "gb:JF766965,gb:JF766966,gb:JF766967,gb:JF766968,gb:JF766969,gb:JF766970,gb:JF766971,gb:JF766972,"
					+ "gb:JF766973,gb:JF766974,gb:JF766975,gb:JF729345,gb:JF729346,gb:JF729347,gb:JF729348,gb:JF729349,"
					+ "gb:JF729350,gb:JF729351,gb:KF680810,gb:KF680811,gb:KF680812,gb:KF680813,gb:KF680814,gb:KF680815,"
					+ "gb:KF680816,gb:KF680817,gb:KF680818,gb:KF680842,gb:KF680843,gb:KF680844,gb:KF680845,gb:KF680846,"
					+ "gb:KF680847,gb:KF680848,gb:KF680849,gb:KF680850,gb:AF091533");
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(59));
			for (int i = 0; i < idsList.size(); i++) {
				String current = idsList.get(i);
				final List<String> copy = new ArrayList<String>(idsList);
				assertThat("current element is eliminated from the copy list of split Ids", copy.remove(current));
				assertThat("the list of split Ids contains no duplicated elements", copy, not(hasItem(current)));
			}

			// test sequence Ids merging using long notation
			String mergedIds = mergeSeqIds(sequences, NOTATION_LONG);
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
			idsList = splitIds(mergedIds);
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(sequences.size()));
			for (final String id : idsList) {
				validate(id, GENBANK);
			}
			/* uncomment for additional output */
			System.out.println(" >> split Ids (using long notation): " + idsList);

			// test sequence Ids merging using short notation			
			mergedIds = mergeSeqIds(sequences, NOTATION_SHORT);
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

	@Test
	public void test2() {
		System.out.println("NamingUtilsTest.test2()");
		try {
			// create sequence dataset
			final List<Sample> samples = newArrayList();
			for (int i = 0; i < 5; i++) {
				if (i%2 == 0) {
					samples.add(SandflySample.builder()
							.collectionId(COLFLEB)
							.catalogNumber("0" + i + "/75")
							.build());
				} else {
					samples.add(LeishmaniaSample.builder()
							.collectionId(CLIOC)
							.catalogNumber("IOCL 000" + i)
							.build());
				}				
			}

			// test sample identifier creation (using long notation)
			String collectionId = samples.get(0).getCollectionId();
			String catalogNumber = samples.get(0).getCatalogNumber();
			String sampleId = toId(collectionId, catalogNumber, NOTATION_LONG);
			assertThat("sample Id is not null", sampleId, notNullValue());
			assertThat("sample Id is not empty", isNotBlank(sampleId), equalTo(true));
			assertThat("sample Id coincides with expected", sampleId, 
					equalTo(COLFLEB + ID_FRAGMENT_SEPARATOR + catalogNumber));

			// test sample identifier creation (using short notation)
			collectionId = samples.get(1).getCollectionId();
			catalogNumber = samples.get(1).getCatalogNumber();
			sampleId = toId(collectionId, catalogNumber, NOTATION_SHORT);
			assertThat("sample Id is not null", sampleId, notNullValue());
			assertThat("sample Id is not empty", isNotBlank(sampleId), equalTo(true));
			assertThat("sample Id coincides with expected", sampleId, 
					equalTo(CLIOC_SHORT + ID_FRAGMENT_SEPARATOR + catalogNumber));

			// test sample identifier creation (using long notation and sample class)
			Sample sample = samples.get(2);
			sampleId = toId(sample, NOTATION_LONG);
			assertThat("sample Id is not null", sampleId, notNullValue());
			assertThat("sample Id is not empty", isNotBlank(sampleId), equalTo(true));
			assertThat("sample Id coincides with expected", sampleId, 
					equalTo(COLFLEB + ID_FRAGMENT_SEPARATOR + sample.getCatalogNumber()));

			// test sample identifier creation (using short notation and sample class)
			sample = samples.get(3);
			sampleId = toId(sample, NOTATION_SHORT);
			assertThat("sample Id is not null", sampleId, notNullValue());
			assertThat("sample Id is not empty", isNotBlank(sampleId), equalTo(true));
			assertThat("sample Id coincides with expected", sampleId, 
					equalTo(CLIOC_SHORT + ID_FRAGMENT_SEPARATOR + sample.getCatalogNumber()));

			// test sample Ids splitting
			List<String> idsList = splitIds("fcf:051/75,fcf:072/78,fcf:051/75,fcl:IOCL 0001,fcl:IOCL 0001,fcl:IOCL 0002");
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(4));
			for (int i = 0; i < idsList.size(); i++) {
				String current = idsList.get(i);
				final List<String> copy = new ArrayList<String>(idsList);
				assertThat("current element is eliminated from the copy list of split Ids", copy.remove(current));
				assertThat("the list of split Ids contains no duplicated elements", copy, not(hasItem(current)));
			}

			// test sample Ids merging using long notation
			String mergedIds = mergeSampleIds(samples, NOTATION_LONG);
			assertThat("merged Ids is not null", mergedIds, notNullValue());
			assertThat("merged Ids is not empty", isNotBlank(mergedIds), equalTo(true));
			assertThat("merged Ids number of fragment separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(ID_FRAGMENT_SEPARATOR)), 
					equalTo(samples.size()));
			assertThat("merged Ids number of ids separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(URI_ID_SEPARATOR)), 
					equalTo(samples.size() - 1));
			assertThat("merged Ids number of data sources coincides with expected",
					countMatches(mergedIds, String.valueOf(COLFLEB)) + countMatches(mergedIds, String.valueOf(CLIOC)), 
					equalTo(samples.size()));
			/* uncomment for additional output */
			System.out.println(" >> merged Ids (using long notation): " + mergedIds);

			// test sample Ids splitting using long notation
			idsList = splitIds(mergedIds);
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(samples.size()));
			for (final String id : idsList) {
				validate(id, COLFLEB, CLIOC);
			}
			/* uncomment for additional output */
			System.out.println(" >> split Ids (using long notation): " + idsList);

			// test sample Ids merging using short notation
			mergedIds = mergeSampleIds(samples, NOTATION_SHORT);
			assertThat("merged Ids is not null", mergedIds, notNullValue());
			assertThat("merged Ids is not empty", isNotBlank(mergedIds), equalTo(true));
			assertThat("merged Ids number of fragment separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(ID_FRAGMENT_SEPARATOR)), 
					equalTo(samples.size()));
			assertThat("merged Ids number of ids separators coincides with expected", 
					countMatches(mergedIds, String.valueOf(URI_ID_SEPARATOR)), 
					equalTo(samples.size() - 1));
			assertThat("merged Ids number of data sources coincides with expected", 
					countMatches(mergedIds, String.valueOf(COLFLEB_SHORT)) + countMatches(mergedIds, String.valueOf(CLIOC_SHORT)),
					equalTo(samples.size()));
			/* uncomment for additional output */
			System.out.println(" >> merged Ids (using short notation): " + mergedIds);

			// test sample Ids splitting using short notation
			idsList = splitIds(mergedIds);
			assertThat("split Ids is not null", idsList, notNullValue());
			assertThat("split Ids is not empty", idsList.isEmpty(), equalTo(false));
			assertThat("number of split Ids coincides with expected", idsList.size(), equalTo(samples.size()));
			for (final String id : idsList) {
				validate(id, COLFLEB_SHORT, CLIOC_SHORT);
			}
			/* uncomment for additional output */
			System.out.println(" >> split Ids (using long notation): " + idsList);
		} finally {
			System.out.println("NamingUtilsTest.test2() has finished");
		}
	}

	@Test
	public void test3() {
		// create test dataset
		final Map<String, String> segments = new ImmutableMap.Builder<String, String>()
				.put("IOCL 0001", "IOCL 0001")
				.put("051/75", "051$g-forward-slash!75")
				.put("x?y", "x$g-question-mark!y")
				.put("/", "$g-forward-slash!")
				.put("?", "$g-question-mark!")
				.put("hello?", "hello$g-question-mark!")
				.put("?hello", "$g-question-mark!hello")
				.put("123?hello/456", "123$g-question-mark!hello$g-forward-slash!456")
				.put("056/75", "056$g-forward-slash!75")
				.put("356", "356")
				.put("g-forward-slash!", "g-forward-slash!")
				.put("$g-question-mark", "$g-question-mark")
				.build();

		segments.entrySet().stream().forEach(e -> {
			// escape URL path segment
			final String escaped = escapeUrlPathSegment(e.getKey());			
			assertThat("escaped segment coincides with expected", trim(escaped), allOf(notNullValue(), equalTo(e.getValue())));
			printMsg("URL path segment: '" + e.getKey() + "' was successfully escaped: '" + escaped + "'");

			// unescape URL path segment
			final String unescaped = unescapeUrlPathSegment(e.getValue());
			assertThat("unescaped segment coincides with expected", trim(unescaped), allOf(notNullValue(), equalTo(e.getKey())));			
			printMsg("URL path segment: '" + e.getValue() + "' was successfully unescaped: '" + unescaped + "'");
		});		
	}

	private static void validate(final String id, final String dataSource) {
		assertThat("split Id is not null", id, notNullValue());
		assertThat("split Id is not empty", isNotBlank(id), equalTo(true));
		assertThat("split Id coincides with expected", id.startsWith(dataSource + ID_FRAGMENT_SEPARATOR), 
				equalTo(true));
	}

	private static void validate(final String id, final String collection1, final String collection2) {
		assertThat("split Id is not null", id, notNullValue());
		assertThat("split Id is not empty", isNotBlank(id), equalTo(true));
		assertThat("split Id coincides with expected", id, anyOf(startsWith(collection1 + ID_FRAGMENT_SEPARATOR), 
				startsWith(collection2 + ID_FRAGMENT_SEPARATOR)));
	}

}