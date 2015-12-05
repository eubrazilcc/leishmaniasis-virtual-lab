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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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

import eu.eubrazilcc.lvl.core.PendingSequence;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.service.rest.PendingSequenceResource;
import eu.eubrazilcc.lvl.service.rest.PendingSequenceResource.PendingSequences;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to pending sequences collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class PendingSequenceResourceTest extends Testable {

	public PendingSequenceResourceTest(final TestContext testCtxt) {
		super(testCtxt, PendingSequenceResourceTest.class);		
	}

	@Override
	public void test() throws Exception {
		// test create new pending sequence
		final SimpleDarwinRecord dwc = DWC_XML_FACTORY.createSimpleDarwinRecord()
				.withInstitutionCode("ISCIII-WHO-CCL")
				.withCollectionCode("ISCIII-Leishmaniasis-collection")
				.withContinent("Europe")
				.withCountry("Spain")
				.withStateProvince("Madrid")
				.withCounty("Madrid")
				.withLocality("Fuenlabrada")
				.withDecimalLatitude(38.081473d)
				.withDecimalLongitude(-122.913837d)
				.withScientificName("Leishmania infantum")
				.withPhylum("Euglenozoa")
				.withClazz("Kinetoplastea")
				.withOrder("Trypanosomatida")
				.withFamily("Trypanosomatidae")
				.withGenus("Leishmania")
				.withSpecificEpithet("infantum");

		final Path path = PendingSequenceResource.class.getAnnotation(Path.class);
		final PendingSequence pendingSeq = PendingSequence.builder()
				.sample(dwc)
				.sequence("GCGAAGAGGCTGGGCCAGAGAAAGCAAGACACGAGATGAAGCGGAGGGACACACACACACACACACACATACACACACACACACACACCTCCTCTACCAGAAGGAAAACG")
				.build();		
		Response response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(pendingSeq, APPLICATION_JSON));			
		assertThat("Create new pending sequence response is not null", response, notNullValue());
		assertThat("Create new pending sequence response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new pending sequence response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new pending sequence response entity is not null", payload, notNullValue());
		assertThat("Create new pending sequence response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Create new pending sequence response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create new pending sequence response JAX-RS object: " + response);
		printMsg(" >> Create new pending sequence HTTP headers: " + response.getStringHeaders());

		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create pending response location is not null", location, notNullValue());
		assertThat("Create pending response path is not empty", isNotBlank(location.getPath()), equalTo(true));

		final String pendingSeqId = getName(location.toURL().getPath());
		assertThat("Created pending response Id is not null", pendingSeqId, notNullValue());
		assertThat("Created pending response Id is not empty", isNotBlank(pendingSeqId), equalTo(true));
		pendingSeq.setId(pendingSeqId);
		pendingSeq.setNamespace(testCtxt.ownerid("user1"));		

		// test get pending sequence by Id (Java object)
		PendingSequence pendingSeq2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingSeq.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequence.class);
		assertThat("Get pending sequence by Id result is not null", pendingSeq2, notNullValue());
		assertThat("Get pending sequence by Id date is not null", pendingSeq2.getSample(), notNullValue());
		pendingSeq.setSample(pendingSeq2.getSample());
		assertThat("Get pending sequence by Id coincides with expected", pendingSeq2.equalsIgnoringVolatile(pendingSeq));
		// uncomment for additional output
		printMsg(" >> Get pending sequence by Id result: " + toJson(pendingSeq2, JSON_PRETTY_PRINTER));

		// create a larger dataset to test complex operations
		final int numItems = 3;
		for (int i = 0; i < numItems; i++) {
			final SimpleDarwinRecord dwc2 = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withInstitutionCode("ISCIII-WHO-CCL")
					.withCollectionCode("ISCIII-Leishmaniasis-collection")
					.withCountry("Spain")
					.withLocality("This is an example")
					.withScientificName("Leishmania infantum");					
			final PendingSequence pendingSeq3 = PendingSequence.builder()
					.sample(dwc2)
					.sequence("GCGAAGA")
					.build();
			response = testCtxt.target().path(path.value())
					.path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
					.post(entity(pendingSeq3, APPLICATION_JSON));
			assertThat("Create new pending sequence response is not null", response, notNullValue());
			assertThat("Create new pending sequence response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		}

		// test list all pending sequences (JSON encoded)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Get pending sequences response is not null", response, notNullValue());
		assertThat("Get pending sequences response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get pending sequences response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get pending sequences response entity is not null", payload, notNullValue());
		assertThat("Get pending sequences response entity is not empty", isNotBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Get pending sequences response body (JSON): " + payload);
		printMsg(" >> Get pending sequences response JAX-RS object: " + response);
		printMsg(" >> Get pending sequences HTTP headers: " + response.getStringHeaders());

		// test list all pending sequences (Java object)
		PendingSequences pendingSeqs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Get pending sequences result is not null", pendingSeqs, notNullValue());
		assertThat("Get pending sequences list coincides with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), hasSize(pendingSeqs.getTotalCount())));
		// uncomment for additional output
		printMsg(" >> Get pending sequences result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));
		
		// access from an unauthorized user must fail
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
				.get();
		assertThat("Unauthorized get pending sequence response is not null", response, notNullValue());
		assertThat("Unauthorized get pending sequence response is UNAUTHORIZED", response.getStatus(), equalTo(UNAUTHORIZED.getStatusCode()));		

		// test pending sequence pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Paginate pending sequences first page response is not null", response, notNullValue());
		assertThat("Paginate pending sequences first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate pending sequences first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate pending sequences first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		pendingSeqs = testCtxt.jsonMapper().readValue(payload, PendingSequences.class);
		assertThat("Paginate pending sequences first page result is not null", pendingSeqs, notNullValue());
		assertThat("Paginate pending sequences first page list coincides with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, pendingSeqs.getTotalCount()))));		
		// uncomment for additional output			
		printMsg(" >> Paginate pending sequences first page response body (JSON): " + payload);

		assertThat("Paginate pending sequences first page links coincides with expected", pendingSeqs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));		
		Link lastLink = null;
		for (int i = 0; i < pendingSeqs.getLinks().size() && lastLink == null; i++) {
			final Link link = pendingSeqs.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate pending sequences first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Paginate pending sequences last page response is not null", response, notNullValue());
		assertThat("Paginate pending sequences last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate pending sequences last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate pending sequences last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		pendingSeqs = testCtxt.jsonMapper().readValue(payload, PendingSequences.class);
		assertThat("Paginate pending sequences last page result is not null", pendingSeqs, notNullValue());
		assertThat("Paginate pending sequences last page list is not empty", pendingSeqs.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		printMsg(" >> Paginate pending sequences last page response body (JSON): " + payload);

		assertThat("Paginate pending sequences last page links coincide with expected", pendingSeqs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get pending sequences pagination (Java object)
		pendingSeqs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Paginate pending sequences first page result is not null", pendingSeqs, notNullValue());
		assertThat("Paginate pending sequences first page list coincides with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, pendingSeqs.getTotalCount()))));		
		// uncomment for additional output
		printMsg(" >> Paginate pending sequences first page result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));

		assertThat("Paginate pending sequences first page links coincide with expected", pendingSeqs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < pendingSeqs.getLinks().size() && lastLink == null; i++) {
			final Link link = pendingSeqs.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate pending sequences first page link to last page is not null", lastLink, notNullValue());

		pendingSeqs = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Paginate pending sequences last page result is not null", pendingSeqs, notNullValue());
		assertThat("Paginate pending sequences last page list is not empty", pendingSeqs.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		printMsg(" >> Paginate pending sequences last page result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));

		assertThat("Paginate pending sequences last page links coincide with expected", pendingSeqs.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get pending sequences applying a full-text search filter
		pendingSeqs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("q", "example")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Search pending sequences result is not null", pendingSeqs, notNullValue());		

		assertThat("Search pending sequences list coincides with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(pendingSeqs.getTotalCount()), hasSize(3)));
		// uncomment for additional output
		printMsg(" >> Search pending sequences result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));

		// test get pending sequences applying a keyword matching filter
		pendingSeqs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("q", "catalogNumber:\"" + pendingSeq.getSample().getCatalogNumber() + "\"")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Search pending sequences result is not null", pendingSeqs, notNullValue());
		assertThat("Search pending sequences list coincides with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(pendingSeqs.getTotalCount()), hasSize(1)));
		// uncomment for additional output
		printMsg(" >> Search pending sequences result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));

		// test get pending sequences applying a full-text search combined with a keyword matching filter
		pendingSeqs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("q", "collection:" + pendingSeq.getSample().getCollectionCode())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Search pending sequences result is not null", pendingSeqs, notNullValue());
		assertThat("Search pending sequences list coincides with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(pendingSeqs.getTotalCount()), hasSize(4)));		
		// uncomment for additional output
		printMsg(" >> Search pending sequences result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));

		// test get pending sequences applying a full-text search combined with a keyword matching filter (JSON encoded)
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("per_page", perPage)
				.queryParam("q", "collection:" + pendingSeq.getSample().getCollectionCode())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get();
		assertThat("Search pending sequences (JSON encoded) response is not null", response, notNullValue());
		assertThat("Search pending sequences (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Search pending sequences (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Search pending sequences (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		pendingSeqs = testCtxt.jsonMapper().readValue(payload, PendingSequences.class);
		assertThat("Search pending sequences (JSON encoded) result is not null", pendingSeqs, notNullValue());
		assertThat("Search pending sequences (JSON encoded) items coincide with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, pendingSeqs.getTotalCount()))));
		// uncomment for additional output			
		printMsg(" >> Search pending sequences response body (JSON): " + payload);

		// test get pending sequences sorted by catalog number
		pendingSeqs = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.queryParam("sort", "catalogNumber")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequences.class);
		assertThat("Sorted pending sequences result is not null", pendingSeqs, notNullValue());
		assertThat("Sorted pending sequences items coincide with expected", pendingSeqs.getElements(), allOf(notNullValue(), not(empty()), hasSize(pendingSeqs.getTotalCount())));		
		String last = "-1";
		for (final PendingSequence s : pendingSeqs.getElements()) {
			assertThat("PendingSeqs are properly sorted", s.getSample().getCatalogNumber().compareTo(last) > 0);
			last = s.getSample().getCatalogNumber();
		}
		// uncomment for additional output			
		printMsg(" >> Sorted pending sequences result: " + toJson(pendingSeqs, JSON_PRETTY_PRINTER));

		// test get pending sequence by Id
	 	pendingSeq2 = testCtxt.target().path(path.value())
	 			.path(urlEncodeUtf8(LVL_DEFAULT_NS)).path(pendingSeq.getId())	 			
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequence.class);
		assertThat("Get pending sequence by catalog number result is not null", pendingSeq2, notNullValue());
		assertThat("Get pending sequence by catalog number coincides with expected", pendingSeq2.equalsIgnoringVolatile(pendingSeq));
		// uncomment for additional output
		printMsg(" >> Get pending sequence by catalog number result: " + toJson(pendingSeq2, JSON_PRETTY_PRINTER));

		// test update pending sequence
		pendingSeq.getSample().setCollectionID("123");
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingSeq.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(pendingSeq, APPLICATION_JSON));
		assertThat("Update pending sequence response is not null", response, notNullValue());
		assertThat("Update pending sequence response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update pending sequence response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update pending sequence response entity is not null", payload, notNullValue());
		assertThat("Update pending sequence response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Update pending sequence response body (JSON), empty is OK: " + payload);
		printMsg(" >> Update pending sequence response JAX-RS object: " + response);
		printMsg(" >> Update pending sequence HTTP headers: " + response.getStringHeaders());

		// test get pending sequence by Id after update
		pendingSeq2 = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingSeq.getId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(PendingSequence.class);
		assertThat("Get pending sequence by Id after update result is not null", pendingSeq2, notNullValue());
		assertThat("Get pending sequence by Id after update coincides with expected", pendingSeq2.equalsIgnoringVolatile(pendingSeq));
		// uncomment for additional output
		printMsg(" >> Get pending sequence by Id after update result: " + toJson(pendingSeq2, JSON_PRETTY_PRINTER));

		// test delete pending sequence
		response = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.path(pendingSeq.getId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete pending sequence response is not null", response, notNullValue());
		assertThat("Delete pending sequence response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete pending sequence response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete pending sequence response entity is not null", payload, notNullValue());
		assertThat("Delete pending sequence response entity is empty", isBlank(payload));
		// uncomment for additional output			
		printMsg(" >> Delete pending sequence response body (JSON), empty is OK: " + payload);
		printMsg(" >> Delete pending sequence response JAX-RS object: " + response);
		printMsg(" >> Delete pending sequence HTTP headers: " + response.getStringHeaders());
	}

}