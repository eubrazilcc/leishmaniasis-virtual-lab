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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.SequenceDatabase;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;
import eu.eubrazilcc.lvl.storage.dao.SequenceDAO;

/**
 * Tests sequence collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SequenceCollectionTest {

	@Test
	public void test() {
		System.out.println("SequenceCollectionTest.test()");
		try {
			// insert
			final Sequence sequence = Sequence.builder()
					.source(SequenceDatabase.GENBANK)
					.accession("12345678")
					.version("3.0")
					.definition("definition")
					.organism("organism")
					.countryFeature("Spain: Murcia")
					.location(Point.builder().coordinate(-122.913837d, 38.081473d).build())
					.locale(new Locale("es", "ES"))					
					.build();
			SequenceDAO.INSTANCE.insert(sequence);
			
			// find
			Sequence sequence2 = SequenceDAO.INSTANCE.find(sequence.getAccession());
			assertThat("sequence is not null", sequence2, notNullValue());
			assertThat("sequence coincides with original", sequence2, equalTo(sequence));
			System.out.println(sequence2.toString());
			
			// update
			sequence.setVersion("4.0");
			SequenceDAO.INSTANCE.update(sequence);
			
			// find after update
			sequence2 = SequenceDAO.INSTANCE.find(sequence.getAccession());
			assertThat("sequence is not null", sequence2, notNullValue());
			assertThat("sequence coincides with original", sequence2, equalTo(sequence));
			System.out.println(sequence2.toString());
			
			// search sequences near a point and within a maximum distance
			List<Sequence> sequences = SequenceDAO.INSTANCE.getNear(Point.builder()
					.coordinate(-122.90d, 38.08d).build(), 10000.0d);
			assertThat("sequence is not null", sequences, notNullValue());
			assertThat("ids is not empty", !sequences.isEmpty());
			
			// search sequences within an area
			sequences = SequenceDAO.INSTANCE.geoWithin(Polygon.builder()
					.coordinate(-140.0d, 30.0d)
					.coordinate(-110.0d, 30.0d)
					.coordinate(-110.0d, 50.0d)
					.coordinate(-140.0d, 30.0d)
					.build());
			assertThat("sequence is not null", sequences, notNullValue());
			assertThat("ids is not empty", !sequences.isEmpty());
			
			// remove
			SequenceDAO.INSTANCE.delete(sequence.getAccession());
			
			// pagination
			final List<String> ids = Lists.newArrayList();
			for (int i = 0; i < 11; i++) {
				final Sequence sequence3 = Sequence.builder()
						.accession(Integer.toString(i)).build();
				ids.add(sequence3.getAccession());
				SequenceDAO.INSTANCE.insert(sequence3);
			}
			final int size = 3;
			int start = 0;
			sequences = null;
			final MutableLong count = new MutableLong(0l);
			do {
				sequences = SequenceDAO.INSTANCE.list(start, size, count);
				if (sequences.size() != 0) {
					System.out.println("Paging " + start + " - " + sequences.size() + " of " + count.getValue());
				}
				start += sequences.size();
			} while (!sequences.isEmpty());
			for (final String id2 : ids) {			
				SequenceDAO.INSTANCE.delete(id2);
			}
			SequenceDAO.INSTANCE.stats(System.out);			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("SequenceCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("SequenceCollectionTest.test() has finished");
		}
	}

}