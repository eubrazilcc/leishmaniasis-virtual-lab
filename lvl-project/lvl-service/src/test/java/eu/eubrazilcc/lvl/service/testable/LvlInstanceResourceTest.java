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

package eu.eubrazilcc.lvl.service.testable;

import static com.google.common.collect.Sets.newHashSet;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.http.client.fluent.Request;

import eu.eubrazilcc.lvl.core.LvlInstance;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.LvlInstances;
import eu.eubrazilcc.lvl.service.rest.LvlInstanceResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to instances collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LvlInstanceResourceTest extends Testable {

	public LvlInstanceResourceTest(final TestContext testCtxt) {
		super(testCtxt, LvlInstanceResourceTest.class);
	}

	@Override
	public void test() throws Exception {
		// test create new instance
		final String instanceId = "00000001";
		final Date heartbeat = new Date();			

		final Path path = LvlInstanceResource.class.getAnnotation(Path.class);
		final LvlInstance instance = LvlInstance.builder()
				.instanceId(instanceId)
				.roles(newHashSet("shard"))
				.heartbeat(heartbeat)
				.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build())					
				.build();
		Response response = testCtxt.target().path(path.value()).request()
				.post(entity(instance, APPLICATION_JSON));			
		assertThat("Create new instance response is not null", response, notNullValue());
		assertThat("Create new instance response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new instance response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new instance response entity is not null", payload, notNullValue());
		assertThat("Create new instance response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Create new instance response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new instance response JAX-RS object: " + response);
		System.out.println(" >> Create new instance HTTP headers: " + response.getStringHeaders());			

		// test get instance by Id (Java object)
		LvlInstance instance2 = testCtxt.target().path(path.value()).path(instance.getInstanceId())
				.request(APPLICATION_JSON)
				.get(LvlInstance.class);
		assertThat("Get instance by Id result is not null", instance2, notNullValue());
		assertThat("Get instance by Id coincides with expected", instance2.equalsIgnoringVolatile(instance));
		// uncomment for additional output
		System.out.println(" >> Get instance by Id result: " + instance2.toString());

		// test list all instances (JSON encoded)
		response = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.get();
		assertThat("Get instances response is not null", response, notNullValue());
		assertThat("Get instances response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get instances response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get instances response entity is not null", payload, notNullValue());
		assertThat("Get instances response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Get instances response body (JSON): " + payload);
		System.out.println(" >> Get instances response JAX-RS object: " + response);
		System.out.println(" >> Get instances HTTP headers: " + response.getStringHeaders());			

		// test list all instances (Java object)
		LvlInstances instances = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.get(LvlInstances.class);
		assertThat("Get instances result is not null", instances, notNullValue());
		assertThat("Get instances list is not null", instances.getElements(), notNullValue());
		assertThat("Get instances list is not empty", !instances.getElements().isEmpty());
		assertThat("Get instances items count coincide with list size", instances.getElements().size(), equalTo(instances.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get instances result: " + instances.toString());			

		// test find instances near to a location (JSON encoded)
		URI uri = testCtxt.target().path(path.value()).path("nearby").path("-122.90").path("38.08")
				.queryParam("maxDistance", 1000.0d).getUri();
		String response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby instances result (plain) is not null", response2, notNullValue());
		assertThat("Get nearby instances result (plain) is not empty", isNotBlank(response2));
		// uncomment for additional output
		System.out.println(" >> Get nearby instances result (plain): " + response2);
		FeatureCollection featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby instances result (plain) is not null", featCol, notNullValue());
		assertThat("Get nearby instances (plain) list is not null", featCol.getFeatures(), notNullValue());
		assertThat("Get nearby instances (plain) list is not empty", featCol.getFeatures().size() > 0);
		// uncomment for additional output
		System.out.println(" >> Get nearby instances result (plain): " + featCol.toString());			

		// test find instances near to a location (Java object)
		featCol = testCtxt.target().path(path.value()).path("nearby").path("-122.90").path("38.08")
				.queryParam("maxDistance", 1000.0d)
				.request(APPLICATION_JSON)
				.get(FeatureCollection.class);
		assertThat("Get nearby instances result is not null", featCol, notNullValue());
		assertThat("Get nearby instances list is not null", featCol.getFeatures(), notNullValue());
		assertThat("Get nearby instances list is not empty", featCol.getFeatures().size() > 0);
		// uncomment for additional output			
		System.out.println(" >> Get nearby instances result: " + featCol.toString());

		// test update instance
		instance.setRoles(newHashSet("working_node"));
		response = testCtxt.target().path(path.value()).path(instance.getInstanceId())
				.request()
				.put(entity(instance, APPLICATION_JSON));
		assertThat("Update instance response is not null", response, notNullValue());
		assertThat("Update instance response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update instance response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update instance response entity is not null", payload, notNullValue());
		assertThat("Update instance response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update instance response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update instance response JAX-RS object: " + response);
		System.out.println(" >> Update instance HTTP headers: " + response.getStringHeaders());

		// test get instance by Id after update
		instance2 = testCtxt.target().path(path.value()).path(instance.getInstanceId())
				.request(APPLICATION_JSON)
				.get(LvlInstance.class);
		assertThat("Get instance by Id after update result is not null", instance2, notNullValue());
		assertThat("Get instance by Id after update coincides with expected", instance2.equalsIgnoringVolatile(instance));
		// uncomment for additional output
		System.out.println(" >> Get instance by Id after update result: " + instance2.toString());

		// test delete instance
		response = testCtxt.target().path(path.value()).path(instance.getInstanceId())
				.request()
				.delete();
		assertThat("Delete instance response is not null", response, notNullValue());
		assertThat("Delete instance response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete instance response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete instance response entity is not null", payload, notNullValue());
		assertThat("Delete instance response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete instance response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete instance response JAX-RS object: " + response);
		System.out.println(" >> Delete instance HTTP headers: " + response.getStringHeaders());
	}

}