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
import static eu.eubrazilcc.lvl.core.DataSource.GENBANK;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
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

import eu.eubrazilcc.lvl.core.Reference;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.service.rest.CitationResource;
import eu.eubrazilcc.lvl.service.rest.CitationResource.References;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to paper citations collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class CitationResourceTest extends Testable {

	public CitationResourceTest(final TestContext testCtxt) {
		super(testCtxt);
	}

	@Override
	public void test() throws Exception {
		printTestStart(CitationResourceTest.class.getSimpleName(), "test");

		// test create new reference
		final String pmid = "00000000";

		final Sandfly sandfly3 = Sandfly.builder()
				.dataSource(GENBANK)
				.accession("ABC12345678")
				.gi(123)
				.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.90d, 38.08d).build()).build())
				.pmids(newHashSet(pmid)).build();
		SANDFLY_DAO.insert(sandfly3);

		final Path path = CitationResource.class.getAnnotation(Path.class);
		final Reference reference = Reference.builder()
				.title("The best paper in the world")
				.pubmedId(pmid)
				.publicationYear(1984)
				.seqids(newHashSet(sandfly3.getId()))
				.build();					
		Response response = testCtxt.target().path(path.value()).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(reference, APPLICATION_JSON));			
		assertThat("Create new reference response is not null", response, notNullValue());
		assertThat("Create new reference response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new reference response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new reference response entity is not null", payload, notNullValue());
		assertThat("Create new reference response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Create new reference response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new reference response JAX-RS object: " + response);
		System.out.println(" >> Create new reference HTTP headers: " + response.getStringHeaders());			

		// test get reference by PMID (Java object)
		Reference reference2 = testCtxt.target().path(path.value()).path(reference.getPubmedId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Reference.class);
		assertThat("Get reference by PMID result is not null", reference2, notNullValue());
		assertThat("Get reference by PMID coincides with expected", reference2.equalsIgnoringVolatile(reference));
		// uncomment for additional output
		System.out.println(" >> Get reference by PMID result: " + reference2.toString());

		// test list all references (JSON encoded)
		response = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Get references response is not null", response, notNullValue());
		assertThat("Get references response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get references response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get references response entity is not null", payload, notNullValue());
		assertThat("Get references response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Get references response body (JSON): " + payload);
		System.out.println(" >> Get references response JAX-RS object: " + response);
		System.out.println(" >> Get references HTTP headers: " + response.getStringHeaders());			

		// test list all references (Java object)
		References references = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(References.class);
		assertThat("Get references result is not null", references, notNullValue());
		assertThat("Get references list is not null", references.getElements(), notNullValue());
		assertThat("Get references list is not empty", !references.getElements().isEmpty());
		assertThat("Get references items count coincide with list size", references.getElements().size(), equalTo(references.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get references result: " + references.toString());			

		// test find references near to a location (JSON encoded)
		URI uri = testCtxt.target().path(path.value()).path("nearby").path("-122.90").path("38.08")
				.queryParam("maxDistance", 1000.0d).getUri();
		String response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.addHeader(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby references result (plain) is not null", response2, notNullValue());
		assertThat("Get nearby references result (plain) is not empty", isNotBlank(response2));
		// uncomment for additional output
		System.out.println(" >> Get nearby references result (plain): " + response2);
		FeatureCollection featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby references result (plain) is not null", featCol, notNullValue());
		assertThat("Get nearby references (plain) list is not null", featCol.getFeatures(), notNullValue());
		assertThat("Get nearby references (plain) list is not empty", featCol.getFeatures().size() > 0);
		// uncomment for additional output
		System.out.println(" >> Get nearby references result (plain): " + featCol.toString());			

		// test find references near to a location (Java object)
		featCol = testCtxt.target().path(path.value()).path("nearby").path("-122.90").path("38.08")
				.queryParam("maxDistance", 1000.0d)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(FeatureCollection.class);
		assertThat("Get nearby references result is not null", featCol, notNullValue());
		assertThat("Get nearby references list is not null", featCol.getFeatures(), notNullValue());
		assertThat("Get nearby references list is not empty", featCol.getFeatures().size() > 0);
		// uncomment for additional output			
		System.out.println(" >> Get nearby references result: " + featCol.toString());

		// test update reference
		reference.setTitle("A very good paper");
		response = testCtxt.target().path(path.value()).path(reference.getPubmedId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.put(entity(reference, APPLICATION_JSON));
		assertThat("Update reference response is not null", response, notNullValue());
		assertThat("Update reference response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update reference response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update reference response entity is not null", payload, notNullValue());
		assertThat("Update reference response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update reference response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update reference response JAX-RS object: " + response);
		System.out.println(" >> Update reference HTTP headers: " + response.getStringHeaders());

		// test get reference by PMID after update
		reference2 = testCtxt.target().path(path.value()).path(reference.getPubmedId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Reference.class);
		assertThat("Get reference by PMID after update result is not null", reference2, notNullValue());
		assertThat("Get reference by PMID after update coincides with expected", reference2.equalsIgnoringVolatile(reference));
		// uncomment for additional output
		System.out.println(" >> Get reference by PMID after update result: " + reference2.toString());

		// test delete reference
		response = testCtxt.target().path(path.value()).path(reference.getPubmedId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.delete();
		assertThat("Delete reference response is not null", response, notNullValue());
		assertThat("Delete reference response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete reference response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete reference response entity is not null", payload, notNullValue());
		assertThat("Delete reference response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete reference response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete reference response JAX-RS object: " + response);
		System.out.println(" >> Delete reference HTTP headers: " + response.getStringHeaders());

		printTestEnd(CitationResourceTest.class.getSimpleName(), "test");
	}

}