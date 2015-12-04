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

import static eu.eubrazilcc.lvl.core.DataSource.COLFLEB;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.XmlHelper.yearAsXMLGregorianCalendar;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
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
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.math.BigInteger;
import java.net.URI;
import java.util.Locale;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import org.apache.http.client.fluent.Request;

import eu.eubrazilcc.lvl.core.Identifiers;
import eu.eubrazilcc.lvl.core.SandflySample;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.service.rest.SandflySampleResource;
import eu.eubrazilcc.lvl.service.rest.SandflySampleResource.Samples;
import eu.eubrazilcc.lvl.storage.SampleKey;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to sandfly samples collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class SandflySampleResourceTest extends Testable {

	public SandflySampleResourceTest(final TestContext testCtxt) {
		super(testCtxt, SandflySampleResourceTest.class, true);
	}

	@Override
	public void test() throws Exception {
		// test create new sandfly
		final SimpleDarwinRecord dwc = DWC_XML_FACTORY.createSimpleDarwinRecord()
				.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-09-28T11:42:11"))
				.withCollectionID("221")
				.withInstitutionCode("Fiocruz")
				.withCollectionCode("Fiocruz-COLFLEB")
				.withBasisOfRecord("S")
				.withOccurrenceID("524447531")
				.withCatalogNumber("051/75")
				.withRecordNumber("051/75")
				.withIndividualCount(BigInteger.valueOf(19))
				.withOccurrenceRemarks("COLETA: Hora Ini: 18:00:00 Hora Fim: 08:20:00 ;")
				.withYear(yearAsXMLGregorianCalendar(1975))
				.withContinent("América do Sul")
				.withCountry("Brasil")
				.withStateProvince("Minas Gerais")
				.withCounty("Caratinga")
				.withLocality("Córrego Barracão")
				.withDecimalLatitude(3.4d)
				.withDecimalLongitude(1.2d)
				.withScientificName("Nyssomyia whitmani")
				.withKingdom("Animalia")
				.withPhylum("Arthropoda")
				.withClazz("Insecta")
				.withOrder("Diptera")
				.withFamily("Psychodidae")
				.withGenus("Nyssomyia")
				.withSpecificEpithet("whitmani");

		final Path path = SandflySampleResource.class.getAnnotation(Path.class);
		final SandflySample sample = SandflySample.builder()
				.collectionId(COLFLEB)
				.catalogNumber(dwc.getCatalogNumber())											
				.location(Point.builder().coordinates(LngLatAlt.builder().coordinates(dwc.getDecimalLongitude(), dwc.getDecimalLatitude()).build()).build())
				.locale(new Locale("pt", "BR"))
				.sample(dwc)
				.build();
		SampleKey sampleKey = SampleKey.builder()
				.collectionId(sample.getCollectionId())
				.catalogNumber(sample.getCatalogNumber())
				.build();
		Response response = testCtxt.target().path(path.value()).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.post(entity(sample, APPLICATION_JSON));			
		assertThat("Create new sandfly sample response is not null", response, notNullValue());
		assertThat("Create new sandfly sample response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new sandfly sample response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new sandfly sample response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));
		// uncomment for additional output			
		System.out.println(" >> Create new sandfly sample response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create new sandfly sample response JAX-RS object: " + response);
		System.out.println(" >> Create new sandfly sample HTTP headers: " + response.getStringHeaders());

		URI locationUri = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Created location is not null", locationUri, notNullValue());
		assertThat("Created location path is not empty", trim(locationUri.getPath()), allOf(notNullValue(), not(equalTo(""))));
		String sampleId = getName(locationUri.toURL().getPath());
		assertThat("Created Id is not empty", trim(sampleId), allOf(notNullValue(), not(equalTo(""))));

		// test get sandfly sample by Id
		SandflySample sample2 = testCtxt.target().path(path.value()).path(sampleId)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(SandflySample.class);
		assertThat("Get sandfly sample by catalog number result is not null", sample2, notNullValue());
		assertThat("Get sandfly sample by catalog number coincides with expected", sample2.equalsIgnoringVolatile(sample));
		// uncomment for additional output
		System.out.println(" >> Get sandfly sample by catalog number result: " + sample2.toString());

		// create a larger dataset to test complex operations
		final int numItems = 3;
		for (int i = 0; i < numItems; i++) {
			final SimpleDarwinRecord dwc2 = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-09-28T11:42:11"))
					.withCollectionID("221")
					.withInstitutionCode("Fiocruz")
					.withCollectionCode("Fiocruz-COLFLEB")
					.withBasisOfRecord("S")
					.withOccurrenceID(Integer.toString(i))
					.withCatalogNumber(Integer.toString(i) + "/15")
					.withYear(yearAsXMLGregorianCalendar(1975 + i))
					.withRecordNumber(Integer.toString(i) + "/15")
					.withStateProvince("This is an example");
			final Point location = i%2 == 0 ? Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build() : null;
			final SandflySample sample3 = SandflySample.builder()
					.collectionId(COLFLEB)
					.catalogNumber(dwc2.getCatalogNumber())													
					.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
					.location(location)
					.sample(dwc2)
					.build();
			testCtxt.target().path(path.value()).request().header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
			.post(entity(sample3, APPLICATION_JSON));
		}

		// test get sandfly samples (JSON encoded)
		response = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Get sandfly samples response is not null", response, notNullValue());
		assertThat("Get sandfly samples response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get sandfly samples response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get sandfly samples response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		// uncomment for additional output			
		System.out.println(" >> Get sandfly samples response body (JSON): " + payload);
		System.out.println(" >> Get sandfly samples response JAX-RS object: " + response);
		System.out.println(" >> Get sandfly samples HTTP headers: " + response.getStringHeaders());

		// test get sandfly samples (Java object)
		Samples samples = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Get sandfly samples result is not null", samples, notNullValue());
		assertThat("Get sandfly samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), hasSize(samples.getTotalCount())));
		// uncomment for additional output
		printMsg(" >> Get sandfly samples result: " + toJson(samples, JSON_PRETTY_PRINTER));

		// test get identifiers
		final Identifiers identifiers = testCtxt.target().path(path.value()).path("project/identifiers")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Identifiers.class);
		assertThat("Get identifiers result is not null", identifiers, notNullValue());
		assertThat("Get identifiers hash is correct", trim(identifiers.getHash()), allOf(notNullValue(), not(equalTo(""))));
		assertThat("Get identifiers list coincides with expected", identifiers.getIdentifiers(), allOf(notNullValue(), not(empty()), hasSize(samples.getTotalCount())));		
		// uncomment for additional output
		printMsg(" >> Get identifiers result: " + toJson(identifiers, JSON_PRETTY_PRINTER));

		// test sandfly sample pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate sandfly samples first page response is not null", response, notNullValue());
		assertThat("Paginate sandfly samples first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate sandfly samples first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate sandfly samples first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		samples = testCtxt.jsonMapper().readValue(payload, Samples.class);
		assertThat("Paginate sandfly samples first page result is not null", samples, notNullValue());
		assertThat("Paginate sandfly samples first page list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, samples.getTotalCount()))));		
		// uncomment for additional output
		System.out.println(" >> Paginate sandfly samples first page response body (JSON): " + payload);

		assertThat("Paginate sandfly samples first page links coincides with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));		
		Link lastLink = null;
		for (int i = 0; i < samples.getLinks().size() && lastLink == null; i++) {
			final Link link = samples.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate sandfly samples first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate sandfly samples last page response is not null", response, notNullValue());
		assertThat("Paginate sandfly samples last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate sandfly samples last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate sandfly samples last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		samples = testCtxt.jsonMapper().readValue(payload, Samples.class);
		assertThat("Paginate sandfly samples last page result is not null", samples, notNullValue());
		assertThat("Paginate sandfly samples last page list is not empty", samples.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandfly samples last page response body (JSON): " + payload);

		assertThat("Paginate sandfly samples last page links coincide with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get sandfly samples pagination (Java object)
		samples = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Paginate sandfly samples first page result is not null", samples, notNullValue());
		assertThat("Paginate sandfly samples first page list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, samples.getTotalCount()))));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandfly samples first page result: " + samples.toString());

		assertThat("Paginate sandfly samples first page links coincide with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < samples.getLinks().size() && lastLink == null; i++) {
			final Link link = samples.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate sandfly samples first page link to last page is not null", lastLink, notNullValue());

		samples = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Paginate sandfly samples last page result is not null", samples, notNullValue());
		assertThat("Paginate sandfly samples last page list is not empty", samples.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		System.out.println(" >> Paginate sandfly samples last page result: " + samples.toString());

		assertThat("Paginate sandfly samples last page links coincide with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get sandfly samples applying a full-text search filter
		samples = testCtxt.target().path(path.value())
				.queryParam("q", "example")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Search sandfly samples result is not null", samples, notNullValue());
		assertThat("Search sandfly samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(samples.getTotalCount()), hasSize(3)));
		// uncomment for additional output			
		System.out.println(" >> Search sandfly samples result: " + samples.toString());

		// test get sandfly samples applying a keyword matching filter
		samples = testCtxt.target().path(path.value())
				.queryParam("q", "catalogNumber:" + sampleKey.getCatalogNumber())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Search sandfly samples result is not null", samples, notNullValue());
		assertThat("Search sandfly samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(samples.getTotalCount()), hasSize(1)));	
		// uncomment for additional output			
		System.out.println(" >> Search sandfly samples result: " + samples.toString());

		// test get sandfly samples applying a full-text search combined with a keyword matching filter
		samples = testCtxt.target().path(path.value())
				.queryParam("q", "collection:" + sampleKey.getCollectionId() + " Fiocruz")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Search sandfly samples result is not null", samples, notNullValue());
		assertThat("Search sandfly samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(samples.getTotalCount()), hasSize(4)));
		// uncomment for additional output			
		System.out.println(" >> Search sandfly samples result: " + samples.toString());

		// test get sandfly samples applying a full-text search combined with a keyword matching filter (JSON encoded)
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.queryParam("q", "collection:" + sampleKey.getCollectionId() + " Fiocruz")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Search sandfly samples (JSON encoded) response is not null", response, notNullValue());
		assertThat("Search sandfly samples (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Search sandfly samples (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Search sandfly samples (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		samples = testCtxt.jsonMapper().readValue(payload, Samples.class);
		assertThat("Search sandfly samples (JSON encoded) result is not null", samples, notNullValue());
		assertThat("Search sandfly samples (JSON encoded) items coincide with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, samples.getTotalCount()))));
		// uncomment for additional output
		System.out.println(" >> Search sandfly samples response body (JSON): " + payload);

		// test get sandfly samples sorted by catalog number
		samples = testCtxt.target().path(path.value())
				.queryParam("sort", "catalogNumber")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Sorted sandfly samples result is not null", samples, notNullValue());
		assertThat("Sorted sandfly samples items coincide with expected", samples.getElements(), allOf(notNullValue(), not(empty()), hasSize(samples.getTotalCount())));		
		String last = "-1";
		for (final SandflySample s : samples.getElements()) {
			assertThat("Sandfly samples are properly sorted", s.getCatalogNumber().compareTo(last) > 0);
			last = s.getCatalogNumber();
		}
		// uncomment for additional output			
		System.out.println(" >> Sorted sandfly samples result: " + samples.toString());

		// test get sandfly sample by collection Id + catalog number
		sample2 = testCtxt.target().path(path.value()).path(sampleKey.toId(true))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(SandflySample.class);
		assertThat("Get sandfly sample by catalog number result is not null", sample2, notNullValue());
		assertThat("Get sandfly sample by catalog number coincides with expected", sample2.equalsIgnoringVolatile(sample));
		// uncomment for additional output
		System.out.println(" >> Get sandfly sample by catalog number result: " + sample2.toString());

		// test export sandfly sample
		final SimpleDarwinRecord dwc2 = testCtxt.target().path(path.value())
				.path(sampleKey.toId(true))
				.path("export/dwc/xml")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(SimpleDarwinRecord.class);
		assertThat("Export sandfly sample result is not null", dwc2, notNullValue());
		// uncomment for additional output
		printMsg(" >> Export sandfly sample result: " + toJson(dwc2, JSON_PRETTY_PRINTER));

		// test update sandfly sample
		sample.setLocale(new Locale("es", "ES"));
		response = testCtxt.target().path(path.value()).path(sampleKey.toId(true))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.put(entity(sample, APPLICATION_JSON));
		assertThat("Update sandfly sample response is not null", response, notNullValue());
		assertThat("Update sandfly sample response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update sandfly sample response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update sandfly sample response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));		
		// uncomment for additional output			
		System.out.println(" >> Update sandfly sample response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update sandfly sample response JAX-RS object: " + response);
		System.out.println(" >> Update sandfly sample HTTP headers: " + response.getStringHeaders());

		// test get sandfly sample by Id after update
		sample2 = testCtxt.target().path(path.value()).path(sampleKey.toId(true))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(SandflySample.class);
		assertThat("Get sandfly sample by catalog number after update result is not null", sample2, notNullValue());
		assertThat("Get sandfly sample by catalog number after update coincides with expected", sample2.equalsIgnoringVolatile(sample));
		// uncomment for additional output
		System.out.println(" >> Get sandfly sample by catalog number after update result: " + sample2.toString());			

		// test find sandfly samples near to a location
		FeatureCollection featCol = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(FeatureCollection.class);
		assertThat("Get nearby sandfly samples result is not null", featCol, notNullValue());
		assertThat("Get nearby sandfly samples list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		System.out.println(" >> Get nearby sandfly samples result: " + featCol.toString());

		// test find sandfly samples near to a location (using plain REST, no Jersey client)
		URI uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d).getUri();
		String response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.addHeader(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby sandfly samples result (plain) is not empty", trim(response2), allOf(notNullValue(), not(equalTo(""))));
		// uncomment for additional output
		System.out.println(" >> Get nearby sandfly samples result (plain): " + response2);
		featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby sandfly samples result (plain) is not null", featCol, notNullValue());
		assertThat("Get nearby sandfly samples (plain) list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		System.out.println(" >> Get nearby sandfly samples result (plain): " + featCol.toString());

		// test find sandfly samples near to a location (using plain REST, no Jersey client, and query style authz token)
		uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d)
				.queryParam(AUTHORIZATION_QUERY_OAUTH2, testCtxt.token("root"))
				.getUri();
		response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby sandfly samples result (plain + query token) is not empty", trim(response2), allOf(notNullValue(), not(equalTo(""))));		
		// uncomment for additional output
		System.out.println(" >> Get nearby sandfly samples result (plain + query token): " + response2);
		featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby sandfly samples result (plain + query token) is not null", featCol, notNullValue());
		assertThat("Get nearby sandfly samples (plain + query token) list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		System.out.println(" >> Get nearby sandfly samples result (plain + query token): " + featCol.toString());

		// test delete sandfly sample
		response = testCtxt.target().path(path.value()).path(sampleKey.toId(true))
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.delete();
		assertThat("Delete sandfly sample response is not null", response, notNullValue());
		assertThat("Delete sandfly sample response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete sandfly sample response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete sandfly sample response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));		
		// uncomment for additional output
		System.out.println(" >> Delete sandfly sample response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete sandfly sample response JAX-RS object: " + response);
		System.out.println(" >> Delete sandfly sample HTTP headers: " + response.getStringHeaders());
	}

}