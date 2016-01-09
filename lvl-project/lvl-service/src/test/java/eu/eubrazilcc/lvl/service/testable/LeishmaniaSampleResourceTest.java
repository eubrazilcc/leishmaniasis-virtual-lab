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

import static eu.eubrazilcc.lvl.core.DataSource.CLIOC;
import static eu.eubrazilcc.lvl.core.http.LinkRelation.LAST;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getPath;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.getQueryParams;
import static eu.eubrazilcc.lvl.core.xml.DwcXmlBinder.DWC_XML_FACTORY;
import static eu.eubrazilcc.lvl.core.xml.XmlHelper.yearAsXMLGregorianCalendar;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaSampleDAO.LEISHMANIA_SAMPLE_DAO;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaSampleDAO.PRIMARY_KEY_PART1;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaSampleDAO.PRIMARY_KEY_PART2;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.toJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoDBJsonMapper.JsonOptions.JSON_PRETTY_PRINTER;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.AUTHORIZATION_QUERY_OAUTH2;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static java.lang.Integer.parseInt;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
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

import java.net.URI;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import org.apache.http.client.fluent.Request;
import org.junit.BeforeClass;

import com.google.common.collect.ImmutableMap;

import eu.eubrazilcc.lvl.core.Identifiers;
import eu.eubrazilcc.lvl.core.LeishmaniaSample;
import eu.eubrazilcc.lvl.core.geojson.FeatureCollection;
import eu.eubrazilcc.lvl.core.geojson.LngLatAlt;
import eu.eubrazilcc.lvl.core.geojson.Point;
import eu.eubrazilcc.lvl.core.xml.tdwg.dwc.SimpleDarwinRecord;
import eu.eubrazilcc.lvl.service.rest.LeishmaniaSampleResource;
import eu.eubrazilcc.lvl.service.rest.LeishmaniaSampleResource.Samples;
import eu.eubrazilcc.lvl.storage.SampleKey;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to leishmania samples collection in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishmaniaSampleResourceTest extends Testable {

	public LeishmaniaSampleResourceTest(final TestContext testCtxt) {
		super(testCtxt, LeishmaniaSampleResourceTest.class);		
	}

	@BeforeClass
	public static void setup() {
		// delete any possible sample that could be imported into the database in previous tests
		final List<LeishmaniaSample> samples = LEISHMANIA_SAMPLE_DAO.list(0, Integer.MAX_VALUE, null, null, 
				ImmutableMap.of(PRIMARY_KEY_PART1, true, PRIMARY_KEY_PART2, true), null);
		
		// TODO
		System.err.println("\n\n >> HERE IS OK: " + samples + "\n");
		// TODO
		
		ofNullable(samples).orElse(emptyList()).forEach(s -> {
			
			// TODO
			System.err.println("\n\n >> DELETING: " + s + "\n");
			// TODO
			
			LEISHMANIA_SAMPLE_DAO.delete(SampleKey.builder()
					.collectionId(s.getCollectionId())
					.catalogNumber(s.getCatalogNumber())
					.build());
		});
	}

	@Override
	public void test() throws Exception {
		// test create new leishmania
		final SimpleDarwinRecord dwc = DWC_XML_FACTORY.createSimpleDarwinRecord()
				.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2010-11-18T13:50:08"))
				.withCollectionID("199")
				.withInstitutionCode("Fiocruz")
				.withCollectionCode("Fiocruz-CLIOC")
				.withBasisOfRecord("L")
				.withOccurrenceID("523692851")
				.withCatalogNumber("IOCL 1001")
				.withRecordedBy("001 - Mauro Célio de Almeida Marzochi")
				.withYear(yearAsXMLGregorianCalendar(1979))
				.withCountry("Brasil")
				.withStateProvince("Rio de Janeiro")
				.withCounty("Rio de Janeiro")
				.withLocality("Jacarepaguá")
				.withDecimalLatitude(3.4d)
				.withDecimalLongitude(1.2d)
				.withIdentifiedBy("Elisa Cupolillo")
				.withScientificName("Leishmania (Viannia) braziliensis")
				.withPhylum("Euglenozoa")
				.withClazz("Kinetoplastea")
				.withOrder("Trypanosomatida")
				.withFamily("Trypanosomatidae")
				.withGenus("Leishmania")
				.withSpecificEpithet("braziliensis");

		final Path path = LeishmaniaSampleResource.class.getAnnotation(Path.class);
		final LeishmaniaSample sample = LeishmaniaSample.builder()
				.collectionId(CLIOC)
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
		assertThat("Create new leishmania sample response is not null", response, notNullValue());
		assertThat("Create new leishmania sample response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create new leishmania sample response is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create new leishmania sample response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));
		// uncomment for additional output			
		printMsg(" >> Create new leishmania sample response body (JSON), empty is OK: " + payload);
		printMsg(" >> Create new leishmania sample response JAX-RS object: " + response);
		printMsg(" >> Create new leishmania sample HTTP headers: " + response.getStringHeaders());

		URI locationUri = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Created location is not null", locationUri, notNullValue());
		assertThat("Created location path is not empty", trim(locationUri.getPath()), allOf(notNullValue(), not(equalTo(""))));
		String sampleId = getName(locationUri.toURL().getPath());
		assertThat("Created Id is not empty", trim(sampleId), allOf(notNullValue(), not(equalTo(""))));

		// test get leishmania sample by Id
		LeishmaniaSample sample2 = testCtxt.target().path(path.value()).path(sampleId)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(LeishmaniaSample.class);
		assertThat("Get leishmania sample by catalog number result is not null", sample2, notNullValue());
		assertThat("Get leishmania sample by catalog number coincides with expected", sample2.equalsIgnoringVolatile(sample));
		// uncomment for additional output
		printMsg(" >> Get leishmania sample by catalog number result: " + sample2.toString());

		// create a larger dataset to test complex operations
		final int numItems = 3;
		for (int i = 0; i < numItems; i++) {
			final SimpleDarwinRecord dwc2 = DWC_XML_FACTORY.createSimpleDarwinRecord()
					.withModified(DWC_XML_FACTORY.createSimpleLiteral().withContent("2015-11-30T13:50:08"))
					.withCollectionID("199")
					.withInstitutionCode("Fiocruz")
					.withCollectionCode("Fiocruz-CLIOC")
					.withBasisOfRecord("L")
					.withOccurrenceID(Integer.toString(i))
					.withCatalogNumber("IOCL X00" + Integer.toString(i))
					.withYear(yearAsXMLGregorianCalendar(1975 + i))
					.withRecordNumber("IOCL 000" + Integer.toString(i))
					.withStateProvince("This is an example");
			final Point location = i%2 == 0 ? Point.builder().coordinates(LngLatAlt.builder().coordinates(-122.913837d, 38.081473d).build()).build() : null;
			final LeishmaniaSample sample3 = LeishmaniaSample.builder()
					.collectionId(CLIOC)
					.catalogNumber(dwc2.getCatalogNumber())													
					.locale(i%2 != 0 ? Locale.ENGLISH : Locale.FRANCE)
					.location(location)
					.sample(dwc2)
					.build();
			testCtxt.target().path(path.value()).request().header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
			.post(entity(sample3, APPLICATION_JSON));
		}

		// test get leishmania samples (JSON encoded)
		response = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Get leishmania samples response is not null", response, notNullValue());
		assertThat("Get leishmania samples response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Get leishmania samples response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Get leishmania samples response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		// uncomment for additional output			
		printMsg(" >> Get leishmania samples response body (JSON): " + payload);
		printMsg(" >> Get leishmania samples response JAX-RS object: " + response);
		printMsg(" >> Get leishmania samples HTTP headers: " + response.getStringHeaders());

		// test get leishmania samples (Java object)
		Samples samples = testCtxt.target().path(path.value()).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Get leishmania samples result is not null", samples, notNullValue());
		assertThat("Get leishmania samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), hasSize(samples.getTotalCount())));
		// uncomment for additional output
		printMsg(" >> Get leishmania samples result: " + toJson(samples, JSON_PRETTY_PRINTER));

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

		// test leishmania sample pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate leishmania samples first page response is not null", response, notNullValue());
		assertThat("Paginate leishmania samples first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate leishmania samples first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate leishmania samples first page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));			
		samples = testCtxt.jsonMapper().readValue(payload, Samples.class);
		assertThat("Paginate leishmania samples first page result is not null", samples, notNullValue());
		assertThat("Paginate leishmania samples first page list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, samples.getTotalCount()))));		
		// uncomment for additional output			
		printMsg(" >> Paginate leishmania samples first page response body (JSON): " + payload);

		assertThat("Paginate leishmania samples first page links coincides with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));		
		Link lastLink = null;
		for (int i = 0; i < samples.getLinks().size() && lastLink == null; i++) {
			final Link link = samples.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate leishmania samples first page link to last page is not null", lastLink, notNullValue());

		response = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate leishmania samples last page response is not null", response, notNullValue());
		assertThat("Paginate leishmania samples last page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate leishmania samples last page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate leishmania samples last page response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));
		samples = testCtxt.jsonMapper().readValue(payload, Samples.class);
		assertThat("Paginate leishmania samples last page result is not null", samples, notNullValue());
		assertThat("Paginate leishmania samples last page list is not empty", samples.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		printMsg(" >> Paginate leishmania samples last page response body (JSON): " + payload);

		assertThat("Paginate leishmania samples last page links coincide with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get leishmania samples pagination (Java object)
		samples = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Paginate leishmania samples first page result is not null", samples, notNullValue());
		assertThat("Paginate leishmania samples first page list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, samples.getTotalCount()))));		
		// uncomment for additional output
		printMsg(" >> Paginate leishmania samples first page result: " + toJson(samples, JSON_PRETTY_PRINTER));

		assertThat("Paginate leishmania samples first page links coincide with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));
		lastLink = null;
		for (int i = 0; i < samples.getLinks().size() && lastLink == null; i++) {
			final Link link = samples.getLinks().get(i);
			if (LAST.equalsIgnoreCase(link.getRel())) {
				lastLink = link;
			}
		}
		assertThat("Paginate leishmania samples first page link to last page is not null", lastLink, notNullValue());

		samples = testCtxt.target().path(getPath(lastLink).substring(testCtxt.service().length()))
				.queryParam("page", parseInt(getQueryParams(lastLink).get("page")))
				.queryParam("per_page", parseInt(getQueryParams(lastLink).get("per_page")))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Paginate leishmania samples last page result is not null", samples, notNullValue());
		assertThat("Paginate leishmania samples last page list is not empty", samples.getElements(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		printMsg(" >> Paginate leishmania samples last page result: " + toJson(samples, JSON_PRETTY_PRINTER));

		assertThat("Paginate leishmania samples last page links coincide with expected", samples.getLinks(), allOf(notNullValue(), not(empty()), hasSize(2)));

		// test get leishmania samples applying a full-text search filter
		samples = testCtxt.target().path(path.value())
				.queryParam("q", "example")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Search leishmania samples result is not null", samples, notNullValue());		

		assertThat("Search leishmania samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(samples.getTotalCount()), hasSize(3)));
		// uncomment for additional output
		printMsg(" >> Search leishmania samples result: " + toJson(samples, JSON_PRETTY_PRINTER));

		// test get leishmania samples applying a keyword matching filter
		samples = testCtxt.target().path(path.value())
				.queryParam("q", "catalogNumber:\"" + sampleKey.getCatalogNumber() + "\"")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Search leishmania samples result is not null", samples, notNullValue());
		assertThat("Search leishmania samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(samples.getTotalCount()), hasSize(1)));
		// uncomment for additional output
		printMsg(" >> Search leishmania samples result: " + toJson(samples, JSON_PRETTY_PRINTER));

		// test get leishmania samples applying a full-text search combined with a keyword matching filter
		samples = testCtxt.target().path(path.value())
				.queryParam("q", "collectionId:" + sampleKey.getCollectionId() + " Fiocruz")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Search leishmania samples result is not null", samples, notNullValue());
		assertThat("Search leishmania samples list coincides with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(samples.getTotalCount()), hasSize(4)));		
		// uncomment for additional output
		printMsg(" >> Search leishmania samples result: " + toJson(samples, JSON_PRETTY_PRINTER));

		// test get leishmania samples applying a full-text search combined with a keyword matching filter (JSON encoded)
		response = testCtxt.target().path(path.value())
				.queryParam("per_page", perPage)
				.queryParam("q", "collectionId:" + sampleKey.getCollectionId() + " Fiocruz")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Search leishmania samples (JSON encoded) response is not null", response, notNullValue());
		assertThat("Search leishmania samples (JSON encoded) response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Search leishmania samples (JSON encoded) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Search leishmania samples (JSON encoded) response entity is not empty", trim(payload), allOf(notNullValue(), not(equalTo(""))));		
		samples = testCtxt.jsonMapper().readValue(payload, Samples.class);
		assertThat("Search leishmania samples (JSON encoded) result is not null", samples, notNullValue());
		assertThat("Search leishmania samples (JSON encoded) items coincide with expected", samples.getElements(), allOf(notNullValue(), not(empty()), 
				hasSize(min(perPage, samples.getTotalCount()))));
		// uncomment for additional output			
		printMsg(" >> Search leishmania samples response body (JSON): " + payload);

		// test get leishmania samples sorted by catalog number
		samples = testCtxt.target().path(path.value())
				.queryParam("sort", "catalogNumber")
				.queryParam("order", "asc")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Samples.class);
		assertThat("Sorted leishmania samples result is not null", samples, notNullValue());
		assertThat("Sorted leishmania samples items coincide with expected", samples.getElements(), allOf(notNullValue(), not(empty()), hasSize(samples.getTotalCount())));		
		String last = "-1";
		for (final LeishmaniaSample s : samples.getElements()) {
			assertThat("Leishmania samples are properly sorted", s.getCatalogNumber().compareTo(last) > 0);
			last = s.getCatalogNumber();
		}
		// uncomment for additional output			
		printMsg(" >> Sorted leishmania samples result: " + samples.toString());

		// test get leishmania sample by collection Id + catalog number
		sample2 = testCtxt.target().path(path.value()).path(sampleKey.toId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(LeishmaniaSample.class);
		assertThat("Get leishmania sample by catalog number result is not null", sample2, notNullValue());
		assertThat("Get leishmania sample by catalog number coincides with expected", sample2.equalsIgnoringVolatile(sample));
		// uncomment for additional output
		printMsg(" >> Get leishmania sample by catalog number result: " + sample2.toString());

		// test export leishmania sample
		final SimpleDarwinRecord dwc2 = testCtxt.target().path(path.value())
				.path(sampleKey.toId())
				.path("export/dwc/xml")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(SimpleDarwinRecord.class);
		assertThat("Export leishmania sample result is not null", dwc2, notNullValue());
		// uncomment for additional output
		printMsg(" >> Export leishmania sample result: " + dwc2.toString());

		// test update leishmania sample
		sample.setLocale(new Locale("es", "ES"));
		response = testCtxt.target().path(path.value()).path(sampleKey.toId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.put(entity(sample, APPLICATION_JSON));
		assertThat("Update leishmania sample response is not null", response, notNullValue());
		assertThat("Update leishmania sample response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update leishmania sample response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update leishmania sample response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));		
		// uncomment for additional output			
		printMsg(" >> Update leishmania sample response body (JSON), empty is OK: " + payload);
		printMsg(" >> Update leishmania sample response JAX-RS object: " + response);
		printMsg(" >> Update leishmania sample HTTP headers: " + response.getStringHeaders());

		// test get leishmania sample by catalog number after update
		sample2 = testCtxt.target().path(path.value()).path(sampleKey.toId())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(LeishmaniaSample.class);
		assertThat("Get leishmania sample by catalog number after update result is not null", sample2, notNullValue());
		assertThat("Get leishmania sample by catalog number after update coincides with expected", sample2.equalsIgnoringVolatile(sample));
		// uncomment for additional output
		printMsg(" >> Get leishmania sample by catalog number after update result: " + sample2.toString());			

		// test find leishmania samples near to a location
		FeatureCollection featCol = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(FeatureCollection.class);
		assertThat("Get nearby leishmania samples result is not null", featCol, notNullValue());
		assertThat("Get nearby leishmania samples list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output			
		printMsg(" >> Get nearby leishmania samples result: " + featCol.toString());

		// test find leishmania samples near to a location (using plain REST, no Jersey client)
		URI uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d).getUri();
		String response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.addHeader(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby leishmania samples result (plain) is not empty", trim(response2), allOf(notNullValue(), not(equalTo(""))));
		// uncomment for additional output
		printMsg(" >> Get nearby leishmania samples result (plain): " + response2);
		featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby leishmania samples result (plain) is not null", featCol, notNullValue());
		assertThat("Get nearby leishmania samples (plain) list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		printMsg(" >> Get nearby leishmania samples result (plain): " + featCol.toString());

		// test find leishmania samples near to a location (using plain REST, no Jersey client, and query style authz token)
		uri = testCtxt.target().path(path.value()).path("nearby").path("1.216666667").path("3.416666667")
				.queryParam("maxDistance", 4000.0d)
				.queryParam(AUTHORIZATION_QUERY_OAUTH2, testCtxt.token("root"))
				.getUri();
		response2 = Request.Get(uri)
				.addHeader("Accept", "application/json")
				.execute()
				.returnContent()
				.asString();
		assertThat("Get nearby leishmania samples result (plain + query token) is not empty", trim(response2), allOf(notNullValue(), not(equalTo(""))));		
		// uncomment for additional output
		printMsg(" >> Get nearby leishmania samples result (plain + query token): " + response2);
		featCol = testCtxt.jsonMapper().readValue(response2, FeatureCollection.class);
		assertThat("Get nearby leishmania samples result (plain + query token) is not null", featCol, notNullValue());
		assertThat("Get nearby leishmania samples (plain + query token) list is not empty", featCol.getFeatures(), allOf(notNullValue(), not(empty())));		
		// uncomment for additional output
		printMsg(" >> Get nearby leishmania samples result (plain + query token): " + featCol.toString());

		// test delete leishmania sample
		response = testCtxt.target().path(path.value()).path(sampleKey.toId())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.delete();
		assertThat("Delete leishmania sample response is not null", response, notNullValue());
		assertThat("Delete leishmania sample response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete leishmania sample response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete leishmania sample response entity is empty", trim(payload), allOf(notNullValue(), equalTo("")));		
		// uncomment for additional output
		printMsg(" >> Delete leishmania sample response body (JSON), empty is OK: " + payload);
		printMsg(" >> Delete leishmania sample response JAX-RS object: " + response);
		printMsg(" >> Delete leishmania sample HTTP headers: " + response.getStringHeaders());
	}

}