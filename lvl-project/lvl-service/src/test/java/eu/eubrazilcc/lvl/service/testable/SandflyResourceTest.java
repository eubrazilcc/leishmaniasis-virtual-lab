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

package eu.eubrazilcc.lvl.service.testable;

import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.core.xml.GbSeqXmlBinder.GBSEQ_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.AUTHORIZATION_QUERY_OAUTH2;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.net.URI;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import org.apache.http.client.fluent.Request;

import eu.eubrazilcc.lvl.core.DataSource;
import eu.eubrazilcc.lvl.core.Identifiers;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.rest.SandflySequenceResource;
import eu.eubrazilcc.lvl.service.rest.SandflySequenceResource.Sequences;
import eu.eubrazilcc.lvl.storage.SequenceKey;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to sand-flies collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SandflyResourceTest extends Testable {

	public SandflyResourceTest(final TestContext testCtxt) {
		super(testCtxt, SandflyResourceTest.class);
	}

	@Override
	public void test() throws Exception {
		// test create new sandfly
		final GBSeq sandflySeq = GBSEQ_XML_FACTORY.createGBSeq()
				.withGBSeqPrimaryAccession("ABC12345678")
				.withGBSeqAccessionVersion("3.0")
				.withGBSeqOtherSeqids(GBSEQ_XML_FACTORY.createGBSeqOtherSeqids().withGBSeqid(GBSEQ_XML_FACTORY.createGBSeqid().withvalue(Integer.toString(Integer.MAX_VALUE))))
				.withGBSeqOrganism("organism")					
				.withGBSeqLength("850");

		final Path path = SandflySequenceResource.class.getAnnotation(Path.class);
		final Sandfly sandfly = Sandfly.builder()
				.dataSource(DataSource.GENBANK)
				.definition("Example sandfly")
				.accession("LVL00000")
				.version("0.0")
				.organism("Example organism")
				.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(1.2d, 3.4d).build()).build())
				.sequence(sandflySeq)
				.build();
		SequenceKey sequenceKey = SequenceKey.builder()
				.dataSource(sandfly.getDataSource())
				.accession(sandfly.getAccession())
				.build();
		Response response = testCtxt.target().path(path.value()).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(sandfly, APPLICATION_JSON));			
		assertThat("Create new sandfly response is not null", response, notNullValue());
		assertThat("Create new sandfly response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new sandfly response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new sandfly response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));
		// uncomment for additional output			
		System.out.println(" >> Create new sandfly response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new sandfly response JAX-RS object: " + response);
		System.out.println(" >> Create new sandfly HTTP headers: " + response.getStringHeaders());

		// test get sandflies (JSON encoded)
		response = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Get sandflies response is not null", response, notNullValue());
		assertThat("Get sandflies response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get sandflies response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get sandflies response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		// uncomment for additional output			
		System.out.println(" >> Get sandflies response body (JSON): " + payload);
		System.out.println(" >> Get sandflies response JAX-RS object: " + response);
		System.out.println(" >> Get sandflies HTTP headers: " + response.getStringHeaders());

		// test get sandflies (Java object)
		Sequences sandflies = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Get sandflies result is not null", sandflies, notNullValue());
		assertThat("Get sandflies list coincides with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), hasSize(sandflies.getTotalCount())));
		// uncomment for additional output			
		System.out.println(" >> Get sandflies result: " + sandflies.toString());

		// test get identifiers
		final Identifiers identifiers = testCtxt.target().path(path.value()).path("project/identifiers")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Identifiers.class);
		assertThat("Get identifiers result is not null", identifiers, notNullValue());
		assertThat("Get identifiers hash is correct", trim(identifiers.getHash()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("Get identifiers list coincides with expected", identifiers.getIdentifiers(), allOf(notNullValue(), not(empty()), hasSize(sandflies.getTotalCount())));		
		// uncomment for additional output			
		System.out.println(" >> Get identifiers result: " + identifiers.toString());

		// test sandfly pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate sandflies first page response is not null", response, notNullValue());
		assertThat("Paginate sandflies first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate sandflies first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate sandflies first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		sandflies = testCtxt.jsonMapper().readValue(payload, Sequences.class);
		assertThat("Paginate sandflies first page result is not null", sandflies, notNullValue());
		assertThat("Paginate sandflies first page list coincides with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, sandflies.getTotalCount()))));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandflies first page response body (JSON): " + payload);

		assertThat("Paginate sandflies first page links coincides with expected", sandflies.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));		
		Link lastLink = null;
		for (int i = 0; i < sandflies.getLinks().size() && lastLink == null; i++) {
			final Link link = sandflies.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate sandflies first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate sandflies last page response is not null", response, notNullValue());
		assertThat("Paginate sandflies last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate sandflies last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate sandflies last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		sandflies = testCtxt.jsonMapper().readValue(payload, Sequences.class);
		assertThat("Paginate sandflies last page result is not null", sandflies, notNullValue());
		assertThat("Paginate sandflies last page list is not empty", sandflies.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandflies last page response body (JSON): " + payload);

		assertThat("Paginate sandflies last page links coincide with expected", sandflies.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get sandflies pagination (Java object)
		sandflies = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Paginate sandflies first page result is not null", sandflies, notNullValue());
		assertThat("Paginate sandflies first page list coincides with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, sandflies.getTotalCount()))));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandflies first page result: " + sandflies.toString());

		assertThat("Paginate sandflies first page links coincide with expected", sandflies.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < sandflies.getLinks().size() && lastLink == null; i++) {
			final Link link = sandflies.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate sandflies first page link to last page is not null", lastLink, notNullValue());

		sandflies = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Paginate sandflies last page result is not null", sandflies, notNullValue());
		assertThat("Paginate sandflies last page list is not empty", sandflies.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandflies last page result: " + sandflies.toString());

		assertThat("Paginate sandflies last page links coincide with expected", sandflies.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get sandflies applying a full-text search filter
		sandflies = testCtxt.target().path(path.value())
				.queryParam("q", "papatasi")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Search sandflies result is not null", sandflies, notNullValue());
		assertThat("Search sandflies list coincides with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(sandflies.getTotalCount()), hasSize(3)));
		// uncomment for additional output			
		System.out.println(" >> Search sandflies result: " + sandflies.toString());

		// test get sandflies applying a keyword matching filter
		sandflies = testCtxt.target().path(path.value())
				.queryParam("q", "accession:JP553239")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Search sandflies result is not null", sandflies, notNullValue());
		assertThat("Search sandflies list coincides with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(sandflies.getTotalCount()), hasSize(1)));	
		// uncomment for additional output			
		System.out.println(" >> Search sandflies result: " + sandflies.toString());

		// test get sandflies applying a full-text search combined with a keyword matching filter
		sandflies = testCtxt.target().path(path.value())
				.queryParam("q", "source:GenBank Phlebotomus")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Search sandflies result is not null", sandflies, notNullValue());
		assertThat("Search sandflies list coincides with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(sandflies.getTotalCount()), hasSize(4)));		
		// uncomment for additional output			
		System.out.println(" >> Search sandflies result: " + sandflies.toString());

		// test get sandflies applying a full-text search combined with a keyword matching filter (JSON encoded)
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.queryParam("q", "source:GenBank Phlebotomus")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Search sandflies (JSON encoded) response is not null", response, notNullValue());
		assertThat("Search sandflies (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Search sandflies (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Search sandflies (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		sandflies = testCtxt.jsonMapper().readValue(payload, Sequences.class);
		assertThat("Search sandflies (JSON encoded) result is not null", sandflies, notNullValue());
		assertThat("Search sandflies (JSON encoded) items coincide with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, sandflies.getTotalCount()))));
		// uncomment for additional output			
		System.out.println(" >> Search sandflies response body (JSON): " + payload);

		// test get sandflies sorted by accession number
		sandflies = testCtxt.target().path(path.value())
				.queryParam("sort", "accession")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sequences.class);
		assertThat("Sorted sandflies result is not null", sandflies, notNullValue());
		assertThat("Sorted sandflies items coincide with expected", sandflies.getElements(), allOf(notNullValue(), not(empty()), hasSize(sandflies.getTotalCount())));		
		String last = "-1";
		for (final Sandfly seq : sandflies.getElements()) {
			assertThat("Sandflies are properly sorted", seq.getAccession().compareTo(last) > 0);
			last = seq.getAccession();
		}
		// uncomment for additional output			
		System.out.println(" >> Sorted sandflies result: " + sandflies.toString());

		// test get sandfly by data source + accession number
		Sandfly sandfly2 = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sandfly.class);
		assertThat("Get sandfly by accession number result is not null", sandfly2, notNullValue());
		assertThat("Get sandfly by accession number coincides with expected", sandfly2.equalsIgnoringVolatile(sandfly));
		// uncomment for additional output
		System.out.println(" >> Get sandfly by accession number result: " + sandfly2.toString());

		// test export sandfly 
		final GBSeq gbSeq = testCtxt.target().path(path.value())
				.path(sequenceKey.toId())
				.path("export/gb/xml")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(GBSeq.class);
		assertThat("Export sandfly result is not null", gbSeq, notNullValue());
		// uncomment for additional output
		System.out.println(" >> Export sandfly result: " + gbSeq.toString());

		// test update sandfly
		sandfly.setDefinition("Modified example sandfly");
		response = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.put(entity(sandfly, APPLICATION_JSON));
		assertThat("Update sandfly response is not null", response, notNullValue());
		assertThat("Update sandfly response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update sandfly response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update sandfly response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));		
		// uncomment for additional output			
		System.out.println(" >> Update sandfly response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update sandfly response JAX-RS object: " + response);
		System.out.println(" >> Update sandfly HTTP headers: " + response.getStringHeaders());

		// test get sandfly by accession number after update
		sandfly2 = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Sandfly.class);
		assertThat("Get sandfly by accession number after update result is not null", sandfly2, notNullValue());
		assertThat("Get sandfly by accession number after update coincides with expected", sandfly2.equalsIgnoringVolatile(sandfly));
		// uncomment for additional output
		System.out.println(" >> Get sandfly by accession number after update result: " + sandfly2.toString());			

		// test find sandflies near to a location
		FeatureCollection featCol = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(FeatureCollection.class);
		assertThat("Get nearby sandflies result is not null", featCol, notNullValue());
		assertThat("Get nearby sandflies list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		System.out.println(" >> Get nearby sandflies result: " + featCol.toString());

		// test find sandflies near to a location (using plain REST, no Jersey client)
		URI uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d).getUri();
		String response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.addHeader(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby sandflies result (plain) is not empty", trim(response2), allOf(notNullValue(), not(equalTo(""))));
		// uncomment for additional output
		System.out.println(" >> Get nearby sandflies result (plain): " + response2);
		featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby sandflies result (plain) is not null", featCol, notNullValue());
		assertThat("Get nearby sandflies (plain) list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		System.out.println(" >> Get nearby sandflies result (plain): " + featCol.toString());

		// test find sandflies near to a location (using plain REST, no Jersey client, and query style authz token)
		uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d)
				.queryParam(AUTHORIZATION_QUERY_OAUTH2, testCtxt.token("root"))
				.getUri();
		response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby sandflies result (plain + query token) is not empty", trim(response2), allOf(notNullValue(), not(equalTo(""))));		
		// uncomment for additional output
		System.out.println(" >> Get nearby sandflies result (plain + query token): " + response2);
		featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby sandflies result (plain + query token) is not null", featCol, notNullValue());
		assertThat("Get nearby sandflies (plain + query token) list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		System.out.println(" >> Get nearby sandflies result (plain + query token): " + featCol.toString());

		// test delete sandfly
		response = testCtxt.target().path(path.value()).path(sequenceKey.toId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.delete();
		assertThat("Delete sandfly response is not null", response, notNullValue());
		assertThat("Delete sandfly response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete sandfly response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete sandfly response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));		
		// uncomment for additional output
		System.out.println(" >> Delete sandfly response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete sandfly response JAX-RS object: " + response);
		System.out.println(" >> Delete sandfly HTTP headers: " + response.getStringHeaders());
	}

}