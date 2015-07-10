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

import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.hash2bucket;

/**
 * Tests {@link Dataset} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetCollectionTest {

	@Test
	public void test() {
		System.out.println("DatasetCollectionTest.test()");
		try {

			final String[] filenames = { "filename1.txt", "a.jpg", "0" };

			for (final String filename : filenames) {
				long bucket = hash2bucket(filename, 16);
				// uncomment for additional output
				System.err.println(" >> Filename '" + filename + "', bucket: " + bucket);
			}

			// TODO

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DatasetCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DatasetCollectionTest.test() has finished");
		}
	}

}