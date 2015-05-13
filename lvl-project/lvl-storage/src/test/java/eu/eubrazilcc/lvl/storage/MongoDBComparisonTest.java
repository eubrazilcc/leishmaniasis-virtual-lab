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

import static eu.eubrazilcc.lvl.storage.mongodb.MongoDBComparison.mongoNumeriComparison;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.notification.StoppedByUserException;

import com.mongodb.BasicDBObject;

/**
 * Tests transformation comparison query operators from logical to mongoDB format (and reverse).
 * @author Erik Torres <ertorser@upv.es>
 */
public class MongoDBComparisonTest {

	@Test
	public void test() {
		System.out.println("MongoDBComparisonTest.test()");
		try {
			// create test dataset
			final String[] expr_valid = { "20", ">100", "<=276", " >=  4013", "< 098", "<>56" };
			final String[] expr_invalid = { "ab12", "ab", "<a" };

			// test conversion of valid, supported logical expressions to mongoDB format
			for (final String expression : expr_valid) {
				final BasicDBObject obj = mongoNumeriComparison("field", expression);
				assertThat("comparison query is not null", obj, notNullValue());
				/* uncomment for additional output */
				System.out.println(" >> expression: " + expression + ", query: " + obj.toString());
			}

			// test conversion of invalid, unsupported logical expressions to mongoDB format
			for (final String expression : expr_invalid) {
				try {
					mongoNumeriComparison("field", expression);
					throw new StoppedByUserException();
				} catch (IllegalStateException | NumberFormatException e2) {
					/* uncomment for additional output */
					System.out.println(" >> caught expected exception: " + e2.getLocalizedMessage());
				}
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("MongoDBComparisonTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("MongoDBComparisonTest.test() has finished");
		}
	}

}