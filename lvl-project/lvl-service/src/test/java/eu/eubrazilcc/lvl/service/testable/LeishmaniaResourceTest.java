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

import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static java.lang.Math.min;
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

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.http.client.fluent.Request;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.rest.LeishmaniaSequenceResource;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to leishmania collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishmaniaResourceTest extends Testable {

	public LeishmaniaResourceTest(final TestContext testCtxt) {
		super(testCtxt, LeishmaniaResourceTest.class);
	}

	@Override
	public void test() throws Exception {
		// test create new leishmania
		final Path path = LeishmaniaSequenceResource.class.getAnnotation(Path.class);
		final Leishmania leishmania = Leishmania.builder()
				.dataSource(DataSource.GENBANK)
				.definition("Example leishmania")
				.accession("LVL00000")
				.version("0.0")
				.organism("Example organism")
				.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.2d, 3.4d).build()).build())
				.build();
		SequenceKey sequenceKey = SequenceKey.builder()
				.dataSource(leishmania.getDataSource())
				.accession(leishmania.getAccession())
				.build();			
		Response response = testCtxt.target().path(path.value()).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(leishmania, APPLICATION_JSON));			
		assertThat("Create new leishmania response is not null", response, notNullValue());
		assertThat("Create new leishmania response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new leishmania response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new leishmania response entity is not null", payload, notNullValue());
		assertThat("Create new leishmania response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Create new leishmania response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new leishmania response JAX-RS object: " + response);
		System.out.println(" >> Create new leishmania HTTP headers: " + response.getStringHeaders());

		// test get leishmania (JSON encoded)
		response = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Get leishmania response is not null", response, notNullValue());
		assertThat("Get leishmania response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get leishmania response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get leishmania response entity is not null", payload, notNullValue());
		assertThat("Get leishmania response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Get leishmania response body (JSON): " + payload);
		System.out.println(" >> Get leishmania response JAX-RS object: " + response);
		System.out.println(" >> Get leishmania HTTP headers: " + response.getStringHeaders());

		// test leishmania pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate leishmania first page response is not null", response, notNullValue());
		assertThat("Paginate leishmania first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate leishmania first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate leishmania first page response entity is not null", payload, notNullValue());
		assertThat("Paginate leishmania first page response entity is not empty", isNotBlank(payload));			
		final LeishmaniaSequenceResource.Sequences leishmanias = testCtxt.jsonMapper().readValue(payload, LeishmaniaSequenceResource.Sequences.class);			
		assertThat("Paginate leishmania first page result is not null", leishmanias, notNullValue());
		assertThat("Paginate leishmania first page list is not null", leishmanias.getElements(), notNullValue());
		assertThat("Paginate leishmania first page list is not empty", !leishmanias.getElements().isEmpty());
		assertThat("Paginate leishmania first page items count coincide with page size", leishmanias.getElements().size(), 
				equalTo(min(perPage, leishmanias.getTotalCount())));
		// uncomment for additional output			
		System.out.println(" >> Paginate leishmanias first page response body (JSON): " + payload);

		// test update leishmania
		leishmania.setDefinition("Modified example leishmania");
		response = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.put(entity(leishmania, APPLICATION_JSON));
		assertThat("Update leishmania response is not null", response, notNullValue());
		assertThat("Update leishmania response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update leishmania response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update leishmania response entity is not null", payload, notNullValue());
		assertThat("Update leishmania response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update leishmania response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update leishmania response JAX-RS object: " + response);
		System.out.println(" >> Update leishmania HTTP headers: " + response.getStringHeaders());

		// test get leishmania by accession number after update
		final Leishmania leishmania2 = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Leishmania.class);
		assertThat("Get leishmania by accession number after update result is not null", leishmania2, notNullValue());
		assertThat("Get leishmania by accession number after update coincides with expected", leishmania2.equalsIgnoringVolatile(leishmania));
		// uncomment for additional output
		System.out.println(" >> Get leishmania by accession number after update result: " + leishmania2.toString());			

		// test find leishmania near to a location (using plain REST, no Jersey client)
		URI uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d).getUri();
		String response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.addHeader(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby leishmania result (plain) is not null", response2, notNullValue());
		assertThat("Get nearby leishmania result (plain) is not empty", isNotBlank(response2));
		// uncomment for additional output
		System.out.println(" >> Get nearby leishmania result (plain): " + response2);
		FeatureCollection featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby leishmania result (plain) is not null", featCol, notNullValue());
		assertThat("Get nearby leishmania (plain) list is not null", featCol.getFeatures(), notNullValue());
		assertThat("Get nearby leishmania (plain) list is not empty", featCol.getFeatures().size() > 0);
		// uncomment for additional output
		System.out.println(" >> Get nearby leishmania result (plain): " + featCol.toString());

		// test delete leishmania
		response = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.delete();
		assertThat("Delete leishmania response is not null", response, notNullValue());
		assertThat("Delete leishmania response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete leishmania response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete leishmania response entity is not null", payload, notNullValue());
		assertThat("Delete leishmania response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete leishmania response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete leishmania response JAX-RS object: " + response);
		System.out.println(" >> Delete leishmania HTTP headers: " + response.getStringHeaders());
	}

}