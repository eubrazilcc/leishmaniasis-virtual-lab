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

import static eu.eubrazilcc.lvl.core.json.client.FormValidationHelper.getValidationField;
import static eu.eubrazilcc.lvl.core.json.client.FormValidationHelper.getValidationType;
import static eu.eubrazilcc.lvl.core.json.client.FormValidationHelper.readValid;
import static eu.eubrazilcc.lvl.core.json.client.FormValidationHelper.validationResponse;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

/**
 * Tests form validation utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class FormValidationTest {

	@Test
	public void test() {
		System.out.println("FormValidationTest.test()");
		try {
			// test input parsing
			final MultivaluedMap<String, String> form = new MultivaluedHashMap<String, String>();
			form.putSingle("type", "validation_field");
			form.putSingle("validation_field", "value");

			final String type = getValidationType(form);
			assertThat("Validation type is not null", type, notNullValue());
			assertThat("Validation type is not empty", isNotBlank(type));
			assertThat("Validation type concides with expected", type, equalTo("validation_field"));

			final String field = getValidationField(type, form);
			assertThat("Validation field is not null", field, notNullValue());
			assertThat("Validation field is not empty", isNotBlank(field));
			assertThat("Validation field concides with expected", field, equalTo("value"));

			// test response creation
			final String response = validationResponse(true);
			assertThat("Validation response is not null", response, notNullValue());
			assertThat("Validation response is not empty", isNotBlank(response));

			// test output parsing
			final boolean valid = readValid(response);
			assertThat("Validation response concides with expected", valid, equalTo(true));			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("FormValidationTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("FormValidationTest.test() has finished");
		}
	}

}