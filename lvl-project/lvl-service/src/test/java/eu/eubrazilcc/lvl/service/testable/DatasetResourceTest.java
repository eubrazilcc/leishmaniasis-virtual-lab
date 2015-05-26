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

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.net.MediaType.parse;
import static eu.eubrazilcc.lvl.core.SequenceCollection.SANDFLY_COLLECTION;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_DEFAULT_NS;
import static eu.eubrazilcc.lvl.core.entrez.GbSeqXmlHelper.getSequence;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.core.util.NamingUtils.urlEncodeUtf8;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Common.HEADER_AUTHORIZATION;
import static eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager.bearerHeader;
import static eu.eubrazilcc.lvl.test.testset.FastaTestHelper.createTestFasta;
import static java.lang.Math.min;
import static java.lang.System.getProperty;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Request;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Target;
import eu.eubrazilcc.lvl.core.xml.ncbi.gb.GBSeq;
import eu.eubrazilcc.lvl.service.Datasets;
import eu.eubrazilcc.lvl.service.rest.DatasetResource;
import eu.eubrazilcc.lvl.test.TestContext;
import eu.eubrazilcc.lvl.test.Testable;

/**
 * Tests access to datasets in the REST API.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetResourceTest extends Testable {

	public DatasetResourceTest(final TestContext testCtxt) {
		super(testCtxt, DatasetResourceTest.class);
	}

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			DatasetResourceTest.class.getSimpleName() + "_" + random(8, true, true)));

	protected void setUp() {
		super.setUp();
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	protected void cleanUp() {
		super.cleanUp();
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	@Override
	public void test() throws Exception {
		// test create dataset (GZIP compressed FASTA sandfly)
		final Path path = DatasetResource.class.getAnnotation(Path.class);
		Target datasetTarget = Target.builder()
				.type("sequence")
				.collection(SANDFLY_COLLECTION)
				.id("gb:JP540074")
				.filter("export_fasta")
				.compression("gzip").build();
		Metadata datasetMetadata = Metadata.builder()
				.target(datasetTarget)
				.description("Optional description")
				.build();
		Dataset dataset = Dataset.builder()
				.filename("my_fasta_sequences.zip")
				.metadata(datasetMetadata)
				.build();			
		Response response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(dataset, APPLICATION_JSON));
		assertThat("Create dataset (FASTA.GZIP sandfly) response is not null", response, notNullValue());
		assertThat("Create dataset (FASTA.GZIP sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset (FASTA.GZIP sandfly) is not empty", response.getEntity(), notNullValue());
		String payload = response.readEntity(String.class);
		assertThat("Create dataset (FASTA.GZIP sandfly) response entity is not null", payload, notNullValue());
		assertThat("Create dataset (FASTA.GZIP sandfly) response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset (FASTA.GZIP sandfly) response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset (FASTA.GZIP sandfly) response JAX-RS object: " + response);
		System.out.println(" >> Create dataset (FASTA.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
		URI location = new URI((String)response.getHeaders().get("Location").get(0));
		assertThat("Create dataset (FASTA.GZIP sandfly) location is not null", location, notNullValue());
		assertThat("Create dataset (FASTA.GZIP sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

		// test list datasets (from super-user account)	
		Datasets datasets = testCtxt.target().path(path.value()).path(urlEncodeUtf8(testCtxt.ownerid("user1"))).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get(Datasets.class);
		assertThat("Get datasets (root account) result is not null", datasets, notNullValue());
		assertThat("Get datasets (root account) list is not null", datasets.getElements(), notNullValue());
		assertThat("Get datasets (root account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
		assertThat("Get datasets (root account) items count coincide with list size", datasets.getElements().size(), 
				equalTo(datasets.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get datasets (root account) result: " + datasets.toString());		

		// test list datasets (from user unauthorized user account)
		try {
			datasets = testCtxt.target().path(path.value()).path(urlEncodeUtf8(testCtxt.ownerid("user1"))).request(APPLICATION_JSON)
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user2")))
					.get(Datasets.class);
			fail("list datasets from user unauthorized user account must produce 401 error");
		} catch (NotAuthorizedException e) {
			// uncomment for additional output			
			System.out.println(" >> Get datasets (unauthorized user account) produced the expected 401 error");
		}			

		// test list datasets (from user account)
		datasets = testCtxt.target().path(path.value()).path(urlEncodeUtf8(testCtxt.ownerid("user1"))).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(Datasets.class);
		assertThat("Get datasets (user account) result is not null", datasets, notNullValue());
		assertThat("Get datasets (user account) list is not null", datasets.getElements(), notNullValue());
		assertThat("Get datasets (user account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
		assertThat("Get datasets (user account) items count coincide with list size", datasets.getElements().size(), 
				equalTo(datasets.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get datasets (user account) result: " + datasets.toString());

		// test list datasets (from user account using default namespace)
		datasets = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(Datasets.class);
		assertThat("Get datasets (user account) result is not null", datasets, notNullValue());
		assertThat("Get datasets (user account) list is not null", datasets.getElements(), notNullValue());
		assertThat("Get datasets (user account) list is not empty", datasets.getElements().isEmpty(), equalTo(false));
		assertThat("Get datasets (user account) items count coincide with list size", datasets.getElements().size(), 
				equalTo(datasets.getTotalCount()));
		// uncomment for additional output			
		System.out.println(" >> Get datasets (user account) result: " + datasets.toString());

		// test get dataset
		datasetMetadata = dataset.getMetadata();
		datasetMetadata.setEditor(testCtxt.ownerid("user1"));
		datasetMetadata.setIsLastestVersion(dataset.getFilename());
		datasetMetadata.getTags().add("fasta");
		Dataset dataset2 = testCtxt.target().path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
				.path(datasets.getElements().get(0).getUrlSafeFilename())
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(Dataset.class);
		assertThat("Get dataset result is not null", dataset2, notNullValue());
		assertThat("Get dataset namespace is not empty", isNotBlank(dataset2.getNamespace()), equalTo(true));
		assertThat("Get dataset namespace coincides with expected", dataset2.getNamespace(), equalTo(testCtxt.ownerid("user1")));			
		assertThat("Get dataset id is not empty", isNotBlank(dataset2.getId()), equalTo(true));
		assertThat("Get dataset length coincides with expected", dataset2.getLength(), greaterThan(0L));
		assertThat("Get dataset chunk size coincides with expected", dataset2.getChunkSize(), greaterThan(0L));
		assertThat("Get dataset creation date is not null", dataset2.getUploadDate(), notNullValue());
		assertThat("Get dataset md5 is not empty", isNotBlank(dataset2.getMd5()), equalTo(true));
		assertThat("Get dataset filename is not empty", isNotBlank(dataset2.getFilename()), equalTo(true));
		assertThat("Get dataset filename coincides with expected", dataset2.getFilename(), equalTo(dataset.getFilename()));
		assertThat("Get dataset content type is not empty", isNotBlank(dataset2.getContentType()), equalTo(true));
		assertThat("Get dataset content type coincides with expected", dataset2.getContentType(), equalTo("application/gzip"));
		assertThat("Get dataset aliases coincides with expected", dataset2.getAliases(), equalTo(dataset.getAliases()));
		assertThat("Get dataset metadata coincides with expected", dataset2.getMetadata(), equalTo(dataset.getMetadata()));
		// uncomment for additional output
		System.out.println(" >> Get dataset result: " + dataset2.toString());

		// test get dataset manually encoding the namespace and filename
		String datasetId = dataset2.getId();
		dataset2 = testCtxt.target().path(path.value()).path(urlEncodeUtf8(defaultIfBlank(datasets.getElements().get(0).getNamespace(), LVL_DEFAULT_NS).trim()))
				.path(urlEncodeUtf8(defaultIfBlank(datasets.getElements().get(0).getFilename(), LVL_DEFAULT_NS).trim()))
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(Dataset.class);			
		assertThat("Get dataset (url encoded) result is not null", dataset2, notNullValue());
		assertThat("Get dataset (url encoded) Id coincides with expected", dataset2.getId(), equalTo(datasetId));
		// uncomment for additional output
		System.out.println(" >> Get dataset (url encoded) result: " + dataset2.toString());

		// test update dataset
		dataset.getMetadata().setDescription("Different description");
		response = testCtxt.target().path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
				.path(datasets.getElements().get(0).getUrlSafeFilename())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.put(entity(dataset, APPLICATION_JSON));
		assertThat("Update dataset response is not null", response, notNullValue());
		assertThat("Update dataset response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Update dataset response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Update dataset response entity is not null", payload, notNullValue());
		assertThat("Update dataset response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Update dataset response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Update dataset response JAX-RS object: " + response);
		System.out.println(" >> Update dataset HTTP headers: " + response.getStringHeaders());

		// test delete dataset
		response = testCtxt.target().path(path.value()).path(datasets.getElements().get(0).getUrlSafeNamespace())
				.path(datasets.getElements().get(0).getUrlSafeFilename())
				.request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.delete();
		assertThat("Delete dataset response is not null", response, notNullValue());
		assertThat("Delete dataset response is NO_CONTENT", response.getStatus(), equalTo(NO_CONTENT.getStatusCode()));
		assertThat("Delete dataset response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Delete dataset response entity is not null", payload, notNullValue());
		assertThat("Delete dataset response entity is empty", isBlank(payload));
		// uncomment for additional output			
		System.out.println(" >> Delete dataset response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Delete dataset response JAX-RS object: " + response);
		System.out.println(" >> Delete dataset HTTP headers: " + response.getStringHeaders());

		// test create dataset (GZIP compressed NCBI sandfly)
		datasetTarget = Target.builder()
				.type("sequence")
				.collection(SANDFLY_COLLECTION)
				.id("gb:JP540074")
				.filter("export")
				.compression("gzip")
				.build();
		datasetMetadata = Metadata.builder()
				.target(datasetTarget)
				.description("Optional description")
				.build();
		dataset = Dataset.builder()
				.filename("my_ncbi_sequences.zip")
				.metadata(datasetMetadata)
				.build();
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(dataset, APPLICATION_JSON));
		assertThat("Create dataset (NCBI.GZIP sandfly) response is not null", response, notNullValue());
		assertThat("Create dataset (NCBI.GZIP sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset (NCBI.GZIP sandfly) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Create dataset (NCBI.GZIP sandfly) response entity is not null", payload, notNullValue());
		assertThat("Create dataset (NCBI.GZIP sandfly) response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset (NCBI.GZIP sandfly) response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset (NCBI.GZIP sandfly) response JAX-RS object: " + response);
		System.out.println(" >> Create dataset (NCBI.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
		location = new URI((String)response.getHeaders().get("Location").get(0));			
		assertThat("Create dataset (NCBI.GZIP sandfly) location is not null", location, notNullValue());
		assertThat("Create dataset (NCBI.GZIP sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

		// test create dataset (uncompressed FASTA sandfly)
		datasetTarget = Target.builder()
				.type("sequence")
				.collection(SANDFLY_COLLECTION)
				.id("gb:JP540074")
				.filter("export_fasta")
				.build();
		datasetMetadata = Metadata.builder()
				.target(datasetTarget)
				.description("Optional description")
				.build();
		dataset = Dataset.builder()
				.filename("my_sequence.fasta")
				.metadata(datasetMetadata)
				.build();
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(dataset, APPLICATION_JSON));
		assertThat("Create dataset (FASTA sandfly) response is not null", response, notNullValue());
		assertThat("Create dataset (FASTA sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset (FASTA sandfly) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Create dataset (FASTA sandfly) response entity is not null", payload, notNullValue());
		assertThat("Create dataset (FASTA sandfly) response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset (FASTA sandfly) response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset (FASTA sandfly) response JAX-RS object: " + response);
		System.out.println(" >> Create dataset (FASTA sandfly) HTTP headers: " + response.getStringHeaders());
		location = new URI((String)response.getHeaders().get("Location").get(0));			
		assertThat("Create dataset (FASTA sandfly) location is not null", location, notNullValue());
		assertThat("Create dataset (FASTA sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

		// test create dataset (uncompressed NCBI sandfly)
		datasetTarget = Target.builder()
				.type("sequence")
				.collection(SANDFLY_COLLECTION)
				.id("gb:JP540074")
				.filter("export")
				.compression("none")
				.build();
		datasetMetadata = Metadata.builder()
				.target(datasetTarget)
				.description("Optional description")
				.build();
		dataset = Dataset.builder()
				.filename("my_ncbi_sequence.xml")
				.metadata(datasetMetadata)
				.build();
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(dataset, APPLICATION_JSON));
		assertThat("Create dataset (NCBI sandfly) response is not null", response, notNullValue());
		assertThat("Create dataset (NCBI sandfly) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset (NCBI sandfly) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Create dataset (NCBI sandfly) response entity is not null", payload, notNullValue());
		assertThat("Create dataset (NCBI sandfly) response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset (NCBI sandfly) response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset (NCBI sandfly) response JAX-RS object: " + response);
		System.out.println(" >> Create dataset (NCBI sandfly) HTTP headers: " + response.getStringHeaders());
		location = new URI((String)response.getHeaders().get("Location").get(0));			
		assertThat("Create dataset (NCBI sandfly) location is not null", location, notNullValue());
		assertThat("Create dataset (NCBI sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

		// test create dataset (GZIP compressed NCBI bulk of sandflies)
		datasetTarget = Target.builder()
				.type("sequence")
				.collection(SANDFLY_COLLECTION)
				.ids(newHashSet("gb:JP540074", "gb:JP553239"))
				.filter("export")
				.compression("gzip")
				.build();
		datasetMetadata = Metadata.builder()
				.target(datasetTarget)
				.description("Optional description")
				.build();
		dataset = Dataset.builder()
				.filename("my_ncbi_sequences.zip")
				.metadata(datasetMetadata)
				.build();
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).request()
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.post(entity(dataset, APPLICATION_JSON));			
		assertThat("Create dataset (NCBI.GZIP sandflies bulk) response is not null", response, notNullValue());
		assertThat("Create dataset (NCBI.GZIP sandflies bulk) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
		assertThat("Create dataset (NCBI.GZIP sandflies bulk) response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Create dataset (NCBI.GZIP sandflies bulk) response entity is not null", payload, notNullValue());
		assertThat("Create dataset (NCBI.GZIP sandflies bulk) response entity is empty", isBlank(payload));
		// uncomment for additional output
		System.out.println(" >> Create dataset (NCBI.GZIP sandflies bulk) response body (JSON), empty is OK: " + payload);
		System.out.println(" >> Create dataset (NCBI.GZIP sandflies bulk) response JAX-RS object: " + response);
		System.out.println(" >> Create dataset (NCBI.GZIP sandfly) HTTP headers: " + response.getStringHeaders());
		location = new URI((String)response.getHeaders().get("Location").get(0));			
		assertThat("Create dataset (NCBI.GZIP sandfly) location is not null", location, notNullValue());
		assertThat("Create dataset (NCBI.GZIP sandfly) path is not empty", isNotBlank(location.getPath()), equalTo(true));

		// test pagination (JSON encoded)
		final int perPage = 2;
		response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(testCtxt.ownerid("user1")))
				.queryParam("per_page", perPage)
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("root")))
				.get();
		assertThat("Paginate datasets first page response is not null", response, notNullValue());
		assertThat("Paginate datasets first page response is OK", response.getStatus(), equalTo(OK.getStatusCode()));
		assertThat("Paginate datasets first page response is not empty", response.getEntity(), notNullValue());
		payload = response.readEntity(String.class);
		assertThat("Paginate datasets first page response entity is not null", payload, notNullValue());
		assertThat("Paginate datasets first page response entity is not empty", isNotBlank(payload));			
		datasets = testCtxt.jsonMapper().readValue(payload, Datasets.class);			
		assertThat("Paginate datasets first page result is not null", datasets, notNullValue());
		assertThat("Paginate datasets first page list is not null", datasets.getElements(), notNullValue());
		assertThat("Paginate datasets first page list is not empty", !datasets.getElements().isEmpty());
		assertThat("Paginate datasets first page items count coincide with page size", datasets.getElements().size(), 
				equalTo(min(perPage, datasets.getTotalCount())));
		// uncomment for additional output			
		System.out.println(" >> Paginate datasets first page response body (JSON): " + payload);

		// test type-ahead
		List<?> filenames = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path("my_")
				.path("typeahead")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(List.class);			
		assertThat("Get filenames (typeahead) result is not null", filenames, notNullValue());
		assertThat("Get filenames (typeahead) is not empty", filenames.isEmpty(), equalTo(false));
		assertThat("Number of filenames (typeahead) coincides with expected", filenames.size(), equalTo(3));
		// uncomment for additional output
		System.out.println(" >> Get filenames (typeahead) result: " + filenames.toString());

		filenames = testCtxt.target().path(path.value())
				.path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path("ZIP")
				.path("typeahead")
				.request(APPLICATION_JSON)
				.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.get(List.class);			
		assertThat("Get filenames (typeahead) result is not null", filenames, notNullValue());
		assertThat("Get filenames (typeahead) is not empty", filenames.isEmpty(), equalTo(false));
		assertThat("Number of filenames (typeahead) coincides with expected", filenames.size(), equalTo(1));
		// uncomment for additional output
		System.out.println(" >> Get filenames (typeahead) result: " + filenames.toString());		

		// test file download
		URI downloadUri = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS))
				.path(urlEncodeUtf8("my_ncbi_sequence.xml")).path("download").getUri();
		org.apache.http.client.fluent.Response response3 = Request.Get(downloadUri)
				.version(HttpVersion.HTTP_1_1) // use HTTP/1.1
				.addHeader("Accept", APPLICATION_OCTET_STREAM)
				.addHeader(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
				.execute();
		assertThat("Download dataset response is not null", response3, notNullValue());
		HttpResponse response4 = response3.returnResponse();
		assertThat("Download dataset HTTP response is not null", response4, notNullValue());
		assertThat("Download dataset status line is not null", response4.getStatusLine(), notNullValue());
		assertThat("Download dataset status coincides with expected", response4.getStatusLine().getStatusCode(),
				equalTo(OK.getStatusCode()));
		Header[] headers = response4.getAllHeaders();
		assertThat("Download dataset headers is not null", headers, notNullValue());
		assertThat("Download dataset headers is not empty", headers.length, greaterThan(0));
		String filename = null;
		for (int i = 0; i < headers.length && filename == null; i++) {
			if ("content-disposition".equalsIgnoreCase(headers[i].getName())) {
				final HeaderElement[] elements = headers[i].getElements();
				if (elements != null) {
					for (int j = 0; j < elements.length && filename == null; j++) {							
						if ("attachment".equalsIgnoreCase(elements[j].getName())) {
							final NameValuePair pair = elements[j].getParameterByName("filename");
							if (pair != null) {
								filename = pair.getValue();
							}
						}
					}
				}
			}
		}
		assertThat("Download dataset filename is not empty", isNotBlank(filename), equalTo(true));
		HttpEntity entity = response4.getEntity();
		assertThat("Download dataset entity is not null", entity, notNullValue());
		assertThat("Download dataset content length coincides with expected", entity.getContentLength(), greaterThan(0l));
		File outfile = new File(testCtxt.testOutputDir(), filename);
		outfile.createNewFile();
		try (final InputStream inputStream = entity.getContent();
				final FileOutputStream outputStream = new FileOutputStream(outfile)) {
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		}
		assertThat("Downloaded file exists", outfile.exists(), equalTo(true));
		assertThat("Downloaded file is not empty", outfile.length(), greaterThan(0L));			
		GBSeq sequence = getSequence(outfile);
		assertThat("XML parsed from downloaded file is not null", sequence, notNullValue());
		// uncomment for additional output
		System.out.println(" >> Saved file: " + filename);

		// test upload custom file (FASTA)
		dataset = Dataset.builder()
				.filename("custom_sequences.fasta")
				.metadata(Metadata.builder().target(Target.builder().build()).build())
				.build();
		final File fastaFile = createTestFasta(TEST_OUTPUT_DIR, "sequence.fasta", 3, "NA");
		final com.google.common.net.MediaType gooFileType = parse(mimeType(fastaFile));
		final MediaType fileType = new MediaType(gooFileType.type(), gooFileType.subtype());
		try (final FormDataMultiPart multipart = new FormDataMultiPart()) {
			multipart.field("dataset", dataset, APPLICATION_JSON_TYPE).bodyPart(new StreamDataBodyPart("file", new FileInputStream(fastaFile), fastaFile.getName(), fileType));
			response = testCtxt.target().path(path.value()).path(urlEncodeUtf8(LVL_DEFAULT_NS)).path("upload").request()
					.header(HEADER_AUTHORIZATION, bearerHeader(testCtxt.token("user1")))
					.post(entity(multipart, multipart.getMediaType()));
			assertThat("Upload custom dataset (fasta file) response is not null", response, notNullValue());
			assertThat("Upload custom dataset (fasta file) response is CREATED", response.getStatus(), equalTo(CREATED.getStatusCode()));
			assertThat("Upload custom dataset (fasta file) response is not empty", response.getEntity(), notNullValue());
			payload = response.readEntity(String.class);
			assertThat("Upload custom dataset (fasta file) response entity is not null", payload, notNullValue());
			assertThat("Upload custom dataset (fasta file) response entity is empty", isBlank(payload));
			// uncomment for additional output			
			System.out.println(" >> Upload custom dataset (fasta file) response body (JSON), empty is OK: " + payload);
			System.out.println(" >> Upload custom dataset (fasta file) response JAX-RS object: " + response);
			System.out.println(" >> Upload custom dataset (fasta file) HTTP headers: " + response.getStringHeaders());
			location = new URI((String)response.getHeaders().get("Location").get(0));
			assertThat("Upload custom dataset (fasta file) location is not null", location, notNullValue());
			assertThat("Upload custom dataset (fasta file) path is not empty", isNotBlank(location.getPath()), equalTo(true));

			datasetId = getName(location.toURL().getPath());
			assertThat("Uploaded custom dataset (fasta file) Id is not null", datasetId, notNullValue());
			assertThat("Uploaded custom dataset (fasta file) Id is not empty", isNotBlank(datasetId), equalTo(true));			
		}
	}

}