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
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.analysis.DisjointSet;

/**
 * Tests {@link DisjointSet} data analysis utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DisjointSetTest {

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		System.out.println("DisjointSetTest.test()");
		try {
			// test create disjoint set
			final List<String> dataset = newArrayList("Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine");

			final DisjointSet<String> disjointSet = DisjointSet.of(dataset);
			assertThat("disjoint-set is not null", disjointSet, notNullValue());
			for (int i = 0; i < dataset.size(); i++) {
				assertThat("all elements are initially in different sets", disjointSet.find(dataset.get(i)), equalTo(i));				
			}

			// test merging
			disjointSet.merge("Two", "Five");			
			disjointSet.merge("Eight", "One");
			disjointSet.merge("Six", "Eight");
			disjointSet.merge("Two", "Five");
			disjointSet.merge("Eight", "Five");
			disjointSet.merge("Four", "Three");
			disjointSet.merge("Seven", "Seven");

			// test find after merge
			assertThat("find result coincides with expected", disjointSet.find("One"), equalTo(disjointSet.find("Two")));
			assertThat("find result coincides with expected", disjointSet.find("Two"), equalTo(disjointSet.find("Five")));
			assertThat("find result coincides with expected", disjointSet.find("Five"), equalTo(disjointSet.find("Six")));
			assertThat("find result coincides with expected", disjointSet.find("Six"), equalTo(disjointSet.find("Eight")));
			assertThat("find result coincides with expected", disjointSet.find("Three"), equalTo(disjointSet.find("Four")));
			assertThat("find result coincides with expected", disjointSet.find("Zero"), equalTo(0));
			assertThat("find result coincides with expected", disjointSet.find("Seven"), equalTo(7));
			assertThat("find result coincides with expected", disjointSet.find("Nine"), equalTo(9));
			/* uncomment for additional output */
			System.out.print(" >> disjoint set\n" + disjointSet.toString());

			// test get root elements
			final List<String> rootList = disjointSet.rootElements();
			assertThat("list of root elements is not null", rootList, notNullValue());
			assertThat("list of root elements is not empty", rootList.size(), not(0));
			assertThat("number of root elements coincides with expected", rootList.size(), equalTo(5));
			/* uncomment for additional output */
			for (final String item : rootList) {
				System.out.println(" >> root element: " + item);
			}

			// test get elements in a set
			String element = "Six";
			List<String> elements = disjointSet.getElementsInSet(element);
			assertThat("list of elements is not null", elements, notNullValue());
			assertThat("list of elements is not empty", elements.size(), not(0));
			assertThat("number of elements coincides with expected", elements.size(), equalTo(5));
			for (final String item : elements) {
				assertThat("elements coincides with expected", item, anyOf(equalTo("One"), equalTo("Two"), equalTo("Five"), 
						equalTo("Six"), equalTo("Eight")));
				/* uncomment for additional output */
				System.out.println(" >> element in the same set that " + element + ": " + item);
			}

			element = "One";
			elements = disjointSet.getElementsInSet(element);
			assertThat("list of elements is not null", elements, notNullValue());
			assertThat("list of elements is not empty", elements.size(), not(0));
			assertThat("number of elements coincides with expected", elements.size(), equalTo(5));
			for (final String item : elements) {
				assertThat("elements coincides with expected", item, anyOf(equalTo("One"), equalTo("Two"), equalTo("Five"), 
						equalTo("Six"), equalTo("Eight")));
				/* uncomment for additional output */
				System.out.println(" >> element in the same set that " + element + ": " + item);
			}

			element = "Three";
			elements = disjointSet.getElementsInSet(element);
			assertThat("list of elements is not null", elements, notNullValue());
			assertThat("list of elements is not empty", elements.size(), not(0));
			assertThat("number of elements coincides with expected", elements.size(), equalTo(2));
			for (final String item : elements) {
				assertThat("elements coincides with expected", item, anyOf(equalTo("Three"), equalTo("Four")));
				/* uncomment for additional output */
				System.out.println(" >> element in the same set that " + element + ": " + item);
			}

			element = "Seven";
			elements = disjointSet.getElementsInSet(element);
			assertThat("list of elements is not null", elements, notNullValue());
			assertThat("list of elements is not empty", elements.size(), not(0));
			assertThat("number of elements coincides with expected", elements.size(), equalTo(1));
			for (final String item : elements) {
				assertThat("elements coincides with expected", item, anyOf(equalTo("Seven")));
				/* uncomment for additional output */
				System.out.println(" >> element in the same set that " + element + ": " + item);
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DisjointSetTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DisjointSetTest.test() has finished");
		}
	}

}