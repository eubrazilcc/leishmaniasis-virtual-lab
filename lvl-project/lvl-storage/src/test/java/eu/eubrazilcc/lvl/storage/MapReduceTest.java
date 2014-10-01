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

import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.core.analysis.LocalizableAnalyzer.toFeatureCollection;
import static eu.eubrazilcc.lvl.storage.dao.SequenceDAO.SEQUENCE_DAO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.Localizable;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.Feature;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Tests map-reduce operations that include several collections at the same time, such as sequences and references.
 * @author Erik Torres <ertorser@upv.es>
 */
public class MapReduceTest {

	@Test
	public void test() {
		System.out.println("MapReduceTest.test()");
		try {
			// insert sequences
			final Sequence[] sequences = {
					Sequence.builder().dataSource(GENBANK).accession("ABC123").gi(123)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build())
					.pmids(newHashSet("PAPER1")).build(),
					Sequence.builder().dataSource(GENBANK).accession("DEF456").gi(456)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-140.00d, 30.00d).build()).build())
					.pmids(newHashSet("PAPER1")).build(),
					Sequence.builder().dataSource(GENBANK).accession("GHI789").gi(789)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-140.00d, 30.00d).build()).build())
					.build(),
					Sequence.builder().dataSource(GENBANK).accession("JKL012").gi(12)
					.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-110.0d, 50.0d).build()).build())
					.pmids(newHashSet("PAPER2")).build()
			};
			for (final Sequence sequence : sequences) {
				SEQUENCE_DAO.insert(sequence);
			}

			// test map sequence locations to references using all sequences with not null location
			List<Localizable<Point>> localizables = SEQUENCE_DAO.getReferenceLocations();
			assertThat("localizables is not null", localizables, notNullValue());
			assertThat("localizables is not empty", !localizables.isEmpty(), equalTo(true));
			assertThat("number of localizables coincides with expected", localizables.size(), equalTo(3));
			/* uncomment for additional output */
			for (final Localizable<Point> localizable : localizables) {
				System.out.println(" >> Localizable : " + localizable.toString());
			}
			
			// test converting to feature collection
			FeatureCollection features = toFeatureCollection(localizables, Crs.builder().wgs84().build(), true, true);
			assertThat("features is not null", features, notNullValue());
			assertThat("features list is not null", features.getFeatures(), notNullValue());
			assertThat("features list is not empty", !features.getFeatures().isEmpty(), equalTo(true));
			assertThat("number of features coincides with expected", features.getFeatures().size(), equalTo(3));
			/* uncomment for additional output */
			for (final Feature feature : features.getFeatures()) {
				System.out.println(" >> Feature : " + feature.toString());
			}
			
			// test map sequence locations to references using the sequences near to a point
			localizables = SEQUENCE_DAO.getReferenceLocations(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build(), 
					1000.0d);
			assertThat("localizables (geo near) is not null", localizables, notNullValue());
			assertThat("localizables (geo near) is not empty", !localizables.isEmpty(), equalTo(true));
			assertThat("number of localizables (geo near) coincides with expected", localizables.size(), equalTo(1));
			/* uncomment for additional output */
			for (final Localizable<Point> localizable : localizables) {
				System.out.println(" >> Localizable (geo near) : " + localizable.toString());
			}
			
			// test map sequence locations to references that returns an empty set
			localizables = SEQUENCE_DAO.getReferenceLocations(Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(0.0d, 0.0d).build()).build(), 
					10.0d);
			assertThat("localizables (empty set) is not null", localizables, notNullValue());
			assertThat("localizables (empty set) is empty", localizables.isEmpty(), equalTo(true));
			
			features = toFeatureCollection(localizables, Crs.builder().wgs84().build(), true, true);
			assertThat("features (empty set) is not null", features, notNullValue());
			assertThat("features (empty set) list is not null", features.getFeatures(), notNullValue());
			assertThat("features (empty set) list is empty", features.getFeatures().isEmpty(), equalTo(true));
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("MapReduceTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("MapReduceTest.test() has finished");
		}
	}

}