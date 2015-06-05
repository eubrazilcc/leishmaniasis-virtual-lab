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

package eu.eubrazilcc.lvl.storage;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openprovenance.prov.model.Document;

import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.storage.prov.SequenceProv;

/**
 * Test support for data provenance.
 * 
 * @author Erik Torres <ertorser@upv.es>
 */
public class ProvenanceTest {

	@Test
	public void test() {
		try {
			System.out.println("ProvenanceTest.test()");

			// test sequence provenance
			final Point point = Point.builder().coordinates(LngLatAlt.builder().longitude(2.0d).latitude(1.0d).build()).build();
			final SequenceProv sequenceProv = new SequenceProv();
			final Document document = sequenceProv.importGenbankSeq("ACCN:U49845", point, "LVL:GB:U49845");
			sequenceProv.exportToFile(document, "/tmp/provenance2.json");
			sequenceProv.exportToFile(document, "/tmp/provenance2.svg");

			// TODO
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ProvenanceTest.test() failed: " + e.getMessage());
		} finally {
			System.out.println("ProvenanceTest.test() has finished");
		}
	}

}