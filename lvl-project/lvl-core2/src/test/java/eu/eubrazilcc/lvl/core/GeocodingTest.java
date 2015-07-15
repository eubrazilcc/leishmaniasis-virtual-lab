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

import static eu.eubrazilcc.lvl.core.geocoding.GeocodingHelper.geocode;
import static eu.eubrazilcc.lvl.core.geocoding.GeocodingHelper.getIfPresent;
import static eu.eubrazilcc.lvl.core.util.LocaleUtils.getLocale;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Locale;

import org.junit.Test;

import com.google.common.base.Optional;

import eu.eubrazilcc.lvl.core.geojson.Point;

/**
 * Tests Geocoding utilities.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GeocodingTest {

	@Test
	public void test() {
		System.out.println("GeocodingTest.test()");
		try {
			// test converting address into geographic coordinates
			final String address1 = "Spain:Valencia";
			Point point = geocode(address1).orNull();
			assertThat("location is not null", point, notNullValue());
			/* uncomment to display additional output */
			System.out.println(" >> Address: " + address1 + ", Location: " + point.toString());

			// test converting address with valid country, invalid region into geographic coordinates
			final String address2 = "Thailand: Ratchabun province, Muang district, Huay-Phai sub-district";
			point = geocode(address2).orNull();
			assertThat("location is not null", point, notNullValue());
			/* uncomment to display additional output */
			System.out.println(" >> Address: " + address2 + ", Location: " + point.toString());
			
			// test converting invalid address
			final String address3 = "1234abcd:1234abcd";
			point = geocode(address3).orNull();
			assertThat("location is null", point, nullValue());
			System.out.println(" >> Address: " + address3 + ", Location: NULL");
			
			// test get location from cache
			Optional<Point> cached = getIfPresent(address1);
			assertThat("cached location is not null", cached, notNullValue());
			assertThat("cached location content is not null", cached.orNull(), notNullValue());
			cached = getIfPresent(address2);
			assertThat("cached location content is not null", cached.orNull(), notNullValue());
			cached = getIfPresent(address3);
			assertThat("cached location content is null", cached.orNull(), nullValue());

			// test converting locale into geographic coordinates
			final Locale locale = getLocale("Spain");
			point = geocode(locale).orNull();
			assertThat("location is not null", point, notNullValue());
			/* uncomment to display additional output */
			System.out.println(" >> Locale: " + locale.getDisplayCountry() + ", Location: " + point.toString());
			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("GeocodingTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("GeocodingTest.test() has finished");
		}
	}

}