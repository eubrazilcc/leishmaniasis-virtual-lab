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

package eu.eubrazilcc.lvl.test;

/**
 * Provides a common interface to inject a context in a test.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class Testable {

	protected final TestContext testCtxt;
	private final Class<?> implementingClass;

	public Testable(final TestContext testCtxt, final Class<?> implementingClass) {
		this.testCtxt = testCtxt;
		this.implementingClass = implementingClass;
	}

	protected void setUp() {
		System.out.println("  >> " + implementingClass.getSimpleName() + ".test() is starting...");
	}

	protected void cleanUp() {
		System.out.println("  >> " + implementingClass.getSimpleName() + ".test() ends.");
	}

	protected abstract void test() throws Exception;

	public void runTest() throws Exception {
		setUp();
		try {
			test();
		} catch (Exception e) {
			throw e;
		} finally {
			cleanUp();
		}
	}

}