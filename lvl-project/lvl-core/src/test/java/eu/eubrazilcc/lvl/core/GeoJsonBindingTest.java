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

import static eu.eubrazilcc.lvl.core.util.TestUtils.getGeoJsonFiles;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.eubrazilcc.lvl.core.geojson.Crs;
import eu.eubrazilcc.lvl.core.geojson.Feature;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LineString;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.geojson.Polygon;
import eu.eubrazilcc.lvl.test.LeishvlTestCase;

/**
 * Test XML/JSON to/from GeoJSON Java object binding.
 * @author Erik Torres <ertorser@upv.es>
 */
public class GeoJsonBindingTest extends LeishvlTestCase {

	public GeoJsonBindingTest() {
		super(false);
	}

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	@Test
	public void test() {
		printMsg("GeoJsonBindingTest.test()");
		try {
			// test GeoJSON point JSON binding
			final Point point = Point.builder()
					.coordinates(LngLatAlt.builder().coordinates(1.0d, 2.0d).build())
					.build();
			assertThat("point is not null", point, notNullValue());
			String payload = JSON_MAPPER.writeValueAsString(point);
			assertThat("point payload is not null", payload, notNullValue());
			assertThat("point payload is not empty", isNotBlank(payload));
			final Point point2 = JSON_MAPPER.readValue(payload, Point.class);
			assertThat("point back is not null", point2, notNullValue());
			assertThat("point back coincides with original", point2, equalTo(point));
			printMsg("point: " + point.toString());
			printMsg("point JSON payload: " + payload);

			// test GeoJSON line JSON binding
			final LineString line = LineString.builder()			
					.coordinates(LngLatAlt.builder().coordinates(1.0d, 2.0d).build(),
							LngLatAlt.builder().coordinates(3.0d, 4.0d).build()).build();
			payload = JSON_MAPPER.writeValueAsString(line);
			assertThat("line payload is not null", payload, notNullValue());
			assertThat("line payload is not empty", isNotBlank(payload));
			final LineString line2 = JSON_MAPPER.readValue(payload, LineString.class);
			assertThat("line back is not null", line2, notNullValue());
			assertThat("line back coincides with original", line2, equalTo(line));
			printMsg("line: " + line.toString());
			printMsg("line JSON payload: " + payload);

			// test GeoJSON polygon JSON binding
			final Polygon polygon = Polygon.builder()
					.exteriorRing(LngLatAlt.builder().coordinates(1.0d, 1.0d).build(),
							LngLatAlt.builder().coordinates(2.0d, 2.0d).build(),
							LngLatAlt.builder().coordinates(3.0d, 3.0d).build(),
							LngLatAlt.builder().coordinates(1.0d, 1.0d).build()).build();
			payload = JSON_MAPPER.writeValueAsString(polygon);
			assertThat("polygon payload is not null", payload, notNullValue());
			assertThat("polygon payload is not empty", isNotBlank(payload));
			final Polygon polygon2 = JSON_MAPPER.readValue(payload, Polygon.class);
			assertThat("polygon back is not null", polygon2, notNullValue());
			assertThat("polygon back coincides with original", polygon2, equalTo(polygon));
			printMsg("polygon: " + polygon.toString());
			printMsg("polygon JSON payload: " + payload);

			// test GeoJSON feature collection JSON binding
			final FeatureCollection collection = FeatureCollection.builder()
					.crs(Crs.builder().wgs84().build())
					.features(Feature.builder().geometry(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.0d, 2.0d).build()).build()).build(),
							Feature.builder().geometry(Point.builder().coordinates(LngLatAlt.builder().coordinates(3.0d, 4.0d).build()).build()).build())
							.build();
			payload = JSON_MAPPER.writeValueAsString(collection);
			assertThat("payload is not null", payload, notNullValue());
			assertThat("payload is not empty", isNotBlank(payload));
			final FeatureCollection collection2 = JSON_MAPPER.readValue(payload, FeatureCollection.class);
			assertThat("feature collection back is not null", collection2, notNullValue());
			assertThat("feature collection back coincides with original", collection2, equalTo(collection));
			printMsg("feature collection: " + collection.toString());
			printMsg("feature collection JSON payload: " + payload);			

			// test reading complex GeoJSON from files
			final Collection<File> files = getGeoJsonFiles();
			for (final File file : files) {
				printMsg(" >> GeoJSON file: " + file.getCanonicalPath());
				final JsonNode typeNode = JSON_MAPPER.readTree(file).get("type");
				assertThat("GeoJSON type is not null", typeNode, notNullValue());
				final String type = typeNode.textValue();
				assertThat("GeoJSON type is not empty", isNotBlank(type));
				Object geojsonObject = null;
				if ("Point".equals(type)) {
					geojsonObject = JSON_MAPPER.readValue(file, Point.class);
				} else if ("LineString".equals(type)) {
					geojsonObject = JSON_MAPPER.readValue(file, LineString.class);
				} else if ("Polygon".equals(type)) {
					geojsonObject = JSON_MAPPER.readValue(file, Polygon.class);
				} else if ("FeatureCollection".equals(type)) {
					geojsonObject = JSON_MAPPER.readValue(file, FeatureCollection.class);			
				} else {
					throw new IllegalStateException("Unsupported GeoJSON type: " + type);
				}
				assertThat("GeoJSON object is not null", geojsonObject, notNullValue());
				printMsg("GeoJSON object: " + geojsonObject.toString());
				printMsg("GeoJSON JSON: " + JSON_MAPPER.writeValueAsString(geojsonObject));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("GeoJsonBindingTest.test() failed: " + e.getMessage());
		} finally {			
			printMsg("GeoJsonBindingTest.test() has finished");
		}
	}

}