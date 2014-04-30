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

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.eubrazilcc.lvl.core.geospatial.Line;
import eu.eubrazilcc.lvl.core.geospatial.Point;
import eu.eubrazilcc.lvl.core.geospatial.Polygon;
import eu.eubrazilcc.lvl.core.xml.GeoJSONXmlBindingHelper;

/**
 * Test XML to/from GeoJSON Java object binding.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GeoJSONXmlBindingTest {

	@Test
	public void test() {
		System.out.println("XmlBindingTest.test()");
		try {
			// test GeoJSON point binding
			final Point point = Point.builder()
					.coordinate(1.0d, 1.0d)
					.build();
			assertThat("point is not null", point, notNullValue());
			String payload = GeoJSONXmlBindingHelper.typeToXml(point);
			assertThat("point payload is not null", payload, notNullValue());
			assertThat("point payload is not empty", isNotBlank(payload));
			final Point point2 = GeoJSONXmlBindingHelper.typeFromXml(payload);
			assertThat("point back is not null", point2, notNullValue());
			assertThat("point back coincides with original", point2, equalTo(point));
			System.out.println("point: " + point.toString());
			System.out.println("point payload: " + payload);

			// test GeoJSON line binding
			final Line line = Line.builder()			
					.coordinate(1.0d, 1.0d)
					.coordinate(2.0d, 2.0d)
					.build();
			payload = GeoJSONXmlBindingHelper.typeToXml(line);
			assertThat("line payload is not null", payload, notNullValue());
			assertThat("line payload is not empty", isNotBlank(payload));
			final Line line2 = GeoJSONXmlBindingHelper.typeFromXml(payload);
			assertThat("line back is not null", line2, notNullValue());
			assertThat("line back coincides with original", line2, equalTo(line));
			System.out.println("line: " + line.toString());
			System.out.println("line payload: " + payload);

			// test GeoJSON polygon binding
			final Polygon polygon = Polygon.builder()
					.coordinate(1.0d, 1.0d)
					.coordinate(2.0d, 2.0d)
					.coordinate(3.0d, 3.0d)
					.coordinate(1.0d, 1.0d)
					.build();
			payload = GeoJSONXmlBindingHelper.typeToXml(polygon);
			assertThat("polygon payload is not null", payload, notNullValue());
			assertThat("polygon payload is not empty", isNotBlank(payload));
			final Polygon polygon2 = GeoJSONXmlBindingHelper.typeFromXml(payload);
			assertThat("polygon back is not null", polygon2, notNullValue());
			assertThat("polygon back coincides with original", polygon2, equalTo(polygon));
			System.out.println("polygon: " + polygon.toString());
			System.out.println("polygon payload: " + payload);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("XmlBindingTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("XmlBindingTest.test() has finished");
		}
	}

}