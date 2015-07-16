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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gunzip;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.storage.base.SaveOptions.SAVE_OVERRIDING;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.hash2bucket;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonMapper.objectToJson;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions.JSON_PRETTY_PRINTER;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import jersey.repackaged.com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.base.Metadata;
import eu.eubrazilcc.lvl.storage.base.Metadata.OpenAccess;
import eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionStats;

/**
 * Tests {@link Dataset} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetCollectionTest {

	private static final int BUCKET = 16;
	private static final long TIMEOUT = 5l;

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			DatasetCollectionTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	@After
	public void cleanUp() throws IOException {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("DatasetCollectionTest.test()");
		try {
			// test bucket assignment			
			final String[] usernames = { "-pTE7eaWAl@linkedin", "QUy9vZwul7@linkedin", "cala@lvl", "root@lvl" };
			final int[] buckets = { 12, 11, 13, 15 };
			for (int i = 0; i < usernames.length; i++) {
				int bucket = hash2bucket(usernames[i], BUCKET);
				assertThat("bucket coincides with expected", bucket, equalTo(buckets[i]));
				// uncomment for additional output
				System.err.println(" >> Filename '" + usernames[i] + "', bucket: " + bucket);
			}

			// create test files
			final File textFile0 = new File(TEST_OUTPUT_DIR, "file0.txt");
			write(textFile0, "This is a test", UTF_8.name());
			assertThat("test Text file exists", textFile0.exists(), equalTo(true));
			assertThat("test Text file is not empty", textFile0.length() > 0l, equalTo(true));

			final String gzipFilename0 = gzip(textFile0.getCanonicalPath());
			assertThat("test GZIP filename is not null", gzipFilename0, notNullValue());
			assertThat("test GZIP filename is not empty", isNotBlank(gzipFilename0), equalTo(true));
			final File gzipFile0 = new File(gzipFilename0);
			assertThat("test GZIP file exists", gzipFile0.exists(), equalTo(true));
			assertThat("test GZIP file is not empty", gzipFile0.length() > 0l, equalTo(true));

			// save new file
			Metadata metadata = Metadata.builder()
					.namespace("namespace")
					.filename(textFile0.getName())
					.description("Optional description")
					.tags(newHashSet("tag1", "tag2", "tag3"))
					.newOther("labels", newArrayList("label1", "label2"))
					.newOther("information", "Additional string field")
					.newOther("type", "sequence")
					.newOther("ids", newHashSet("JP540074"))
					.newOther("filter", "export_fasta")
					.build();
			final Dataset ds0 = Dataset.builder()
					.metadata(metadata)
					.build();
			ds0.save(textFile0).get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds0.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved length is not zero", ds0.getLength(), greaterThan(0l));
			assertThat("saved chuck size is not zero", ds0.getChunkSize(), greaterThan(0l));
			assertThat("saved upload date is not null", ds0.getUploadDate(), notNullValue());
			assertThat("saved checksum is not empty", trim(ds0.getMd5()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds0.getFilename()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds0.getContentType()), allOf(notNullValue(), not(equalTo(""))));
			// uncomment for additional output
			System.out.println(" >> Inserted dataset (text file 0):\n" + ds0.toJson(JSON_PRETTY_PRINTER));			

			metadata = Metadata.builder()
					.namespace("namespace")
					.filename(gzipFile0.getName())
					.build();
			final Dataset ds1 = Dataset.builder()
					.metadata(metadata)
					.build();
			ds1.save(gzipFile0).get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds1.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved length is not zero", ds1.getLength(), greaterThan(0l));
			assertThat("saved chuck size is not zero", ds1.getChunkSize(), greaterThan(0l));
			assertThat("saved upload date is not null", ds1.getUploadDate(), notNullValue());
			assertThat("saved checksum is not empty", trim(ds1.getMd5()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds1.getFilename()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds1.getContentType()), allOf(notNullValue(), not(equalTo(""))));
			// uncomment for additional output
			System.out.println(" >> Inserted dataset (binary file 0):\n" + ds1.toJson(JSON_PRETTY_PRINTER));

			// get file from database
			Dataset ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(textFile0.getName()).build())
					.build();
			ds2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());			
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds0));
			checkFile(textFile0, ds2.getOutfile());
			// uncomment for additional output
			System.out.println(" >> Fetched dataset (text file 0):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(gzipFile0.getName()).build())
					.build();
			ds2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds1));
			File uncompressedFile = new File(TEST_OUTPUT_DIR, "uncompressed_" + textFile0.getName());
			gunzip(ds2.getOutfile().getCanonicalPath(), uncompressedFile.getCanonicalPath());
			checkFile(textFile0, uncompressedFile);
			// uncomment for additional output
			System.out.println(" >> Fetched dataset (binary file 0):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			// fetching non-existing objects should fail
			try {
				Dataset.builder().metadata(Metadata.builder().filename(textFile0.getName()).build()).build().fetch().get(TIMEOUT, SECONDS);
				fail("Expected exception due to non-existing object");
			} catch (Exception expected) {
				assertThat("exception cause is not null", expected.getCause(), notNullValue());
				assertThat("exception cause coincides with expected", expected.getCause() instanceof LvlObjectNotFoundException, equalTo(true));				
				System.out.println("Expected exception caught: " + expected.getCause().getMessage());
			}

			// test file exists operation
			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(textFile0.getName()).build())
					.build();
			boolean fileExists = ds2.exists().get(TIMEOUT, SECONDS);
			assertThat("file exists result coincides with expected", fileExists, equalTo(true));

			ds2 = Dataset.builder()
					.metadata(Metadata.builder().filename(textFile0.getName()).build())
					.build();
			fileExists = ds2.exists().get(TIMEOUT, SECONDS);
			assertThat("file exists result coincides with expected", fileExists, equalTo(false));

			// override existing file
			String dbId = ds0.getId();
			Date uploadDate = ds0.getUploadDate();
			final File textFile0_Original = new File(TEST_OUTPUT_DIR, "file0_original.txt");
			copyFile(textFile0, textFile0_Original);
			write(textFile0, "The second version of the text file is larger than the previous one", UTF_8.name());
			assertThat("test Text file exists", textFile0.exists(), equalTo(true));
			assertThat("test Text file is not empty", textFile0.length() > 0l, equalTo(true));
			metadata = Metadata.builder()
					.namespace("namespace")
					.filename(textFile0.getName())
					.description("New version of text file 0")
					.newOther("reference", "file_id")
					.newOther("information", "New additional string field for file 0")
					.build();
			ds0.setMetadata(metadata);
			ds0.save(textFile0, SAVE_OVERRIDING).get(TIMEOUT, SECONDS);
			assertThat("Id is not empty", trim(ds0.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("Id is different in the new version", ds0.getId(), not(equalTo(dbId)));
			assertThat("upload date is not null", ds0.getUploadDate(), notNullValue());			
			assertThat("upload date is in the future", ds0.getUploadDate().after(uploadDate), equalTo(true));

			// get file after saving the new version			
			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(textFile0.getName()).build())
					.build();
			ds2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds0));
			checkFile(textFile0, ds2.getOutfile());
			// Uncomment for additional output
			System.out.println(" >> Fetched dataset (after saving the new version):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			// update metadata
			dbId = ds0.getId();
			uploadDate = ds0.getUploadDate();
			metadata = Metadata.builder()
					.namespace("namespace")
					.filename(textFile0.getName())
					.description("New version of the new version of text file 0")
					.newOther("information", "New version of the new version of the additional string field for file 0")
					.build();
			ds0.setMetadata(metadata);
			ds0.updateMetadata().get(TIMEOUT, SECONDS);
			assertThat("Id is not empty", trim(ds0.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("Id concides in the updated object", ds0.getId(), equalTo(dbId));
			assertThat("upload date is not null", ds0.getUploadDate(), notNullValue());			
			assertThat("upload date coincides in the updated object", ds0.getUploadDate(), equalTo(uploadDate));
			assertThat("metadata coincides in the updated object", ds0.getMetadata(), equalTo(metadata));

			// get file after updating the metadata
			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(textFile0.getName()).build())
					.build();
			ds2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds0));
			checkFile(textFile0, ds2.getOutfile());
			// Uncomment for additional output
			System.out.println(" >> Fetched dataset (after updating the metadata):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			// list all files within a specific name space
			Datasets dss = Datasets.builder().namespace("namespace").build();
			int count = dss.fetch(0, Integer.MAX_VALUE, null, null).get(TIMEOUT, SECONDS);
			assertThat("fetched files are not null", dss.getElements(), notNullValue());
			assertThat("fetched files are not empty", dss.getElements().isEmpty(), equalTo(false));
			assertThat("number of fetched files coincides with expected", dss.getElements().size(), equalTo(2));
			assertThat("number of fetched files coincides with expected", dss.size(), equalTo(count));
			assertThat("order of fetched files coincides with expected", dss.getElements().get(0).getMetadata().getFilename(), equalTo(textFile0.getName()));
			// Uncomment for additional output
			System.out.println(" >> Fetched datasets (with default order)\n" + dss.toJson(JSON_PRETTY_PRINTER));

			// list all files within a specific name space (sorting by filename in ascending order)
			count = dss.fetch(0, Integer.MAX_VALUE, null, ImmutableMap.of("metadata.filename", false)).get(TIMEOUT, SECONDS);
			assertThat("fetched files are not null", dss.getElements(), notNullValue());
			assertThat("fetched files are not empty", dss.getElements().isEmpty(), equalTo(false));
			assertThat("number of fetched files coincides with expected", dss.getElements().size(), equalTo(2));
			assertThat("number of fetched files coincides with expected", dss.size(), equalTo(count));
			assertThat("order of fetched files coincides with expected", dss.getElements().get(1).getMetadata().getFilename(), equalTo(gzipFile0.getName()));
			// Uncomment for additional output
			System.out.println(" >> Fetched datasets (sorting by filename, desc)\n" + dss.toJson(JSON_PRETTY_PRINTER));

			// test type-ahead with filename
			dss = Datasets.builder().namespace("namespace").build();
			List<String> filenames = dss.typeahead("ile0", 100).get(TIMEOUT, SECONDS);
			assertThat("filenames are not null", filenames, notNullValue());
			assertThat("filenames are not empty", filenames.isEmpty(), equalTo(false));
			assertThat("number of filenames coincides with expected", filenames.size(), equalTo(2));
			// Uncomment for additional output
			System.out.println(" >> Typeahead: " + filenames.toString());

			filenames = dss.typeahead("ile0", 1).get(TIMEOUT, SECONDS);
			assertThat("filenames are not null", filenames, notNullValue());
			assertThat("filenames are not empty", filenames.isEmpty(), equalTo(false));
			assertThat("number of filenames coincides with expected", filenames.size(), equalTo(1));
			// Uncomment for additional output
			System.out.println(" >> Typeahead: " + filenames.toString());

			filenames = dss.typeahead("GZ", 100).get(TIMEOUT, SECONDS);
			assertThat("filenames are not null", filenames, notNullValue());
			assertThat("filenames are not empty", filenames.isEmpty(), equalTo(false));
			assertThat("number of filenames coincides with expected", filenames.size(), equalTo(1));
			// Uncomment for additional output
			System.out.println(" >> Typeahead: " + filenames.toString());

			// create open access link
			ds1.createOpenAccessLink().get(TIMEOUT, SECONDS);
			assertThat("open access is not null", ds1.getMetadata().getOpenAccess(), notNullValue());
			String secret = ds1.getMetadata().getOpenAccess().getSecret();
			Integer bucket = ds1.getMetadata().getOpenAccess().getBucket();			
			assertThat("secret is not empty", trim(secret), allOf(notNullValue(), not(equalTo(""))));
			assertThat("bucket is not empty", bucket, allOf(notNullValue(), greaterThanOrEqualTo(0)));
			assertThat("open access date is not null", ds1.getMetadata().getOpenAccess().getDate(), notNullValue());
			// Uncomment for additional output
			System.out.println(" >> Secret to access file anonymously: {endpoint}/" + bucket + "/" + secret + "/" + ds1.getMetadata().getFilename());

			// verify that the access link was created
			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(gzipFile0.getName()).build())
					.build();
			ds2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds1));
			uncompressedFile = new File(TEST_OUTPUT_DIR, "uncompressed_" + textFile0.getName());
			gunzip(ds2.getOutfile().getCanonicalPath(), uncompressedFile.getCanonicalPath());
			checkFile(textFile0_Original, uncompressedFile);
			// Uncomment for additional output
			System.out.println(" >> Fetched dataset (after creating open-access link):\n" + ds2.toJson(JSON_PRETTY_PRINTER));			

			// read file from open access link
			final OpenAccess openAccess = OpenAccess.builder()
					.bucket(bucket)
					.secret(secret)
					.build();
			metadata = Metadata.builder()
					.filename(gzipFile0.getName())
					.openAccess(openAccess)
					.build();
			ds2 = Dataset.builder()
					.metadata(metadata)
					.build();
			ds2.fetchOpenAccess().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds1));
			uncompressedFile = new File(TEST_OUTPUT_DIR, "uncompressed_" + textFile0.getName());
			gunzip(ds2.getOutfile().getCanonicalPath(), uncompressedFile.getCanonicalPath());
			checkFile(textFile0_Original, uncompressedFile);
			// Uncomment for additional output
			System.out.println(" >> Fetched dataset (using open-access link):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			// list files with open access links
			dss = Datasets.builder().namespace("namespace").build();
			count = dss.fetchOpenAccess(0, Integer.MAX_VALUE, null).get(TIMEOUT, SECONDS);			
			assertThat("fetched files are not null", dss.getElements(), notNullValue());
			assertThat("fetched files are not empty", dss.getElements().isEmpty(), equalTo(false));
			assertThat("number of fetched files coincides with expected", dss.getElements().size(), equalTo(1));
			assertThat("number of fetched files coincides with expected", dss.size(), equalTo(count));
			assertThat("content of fetched files coincides with expected", dss.getElements().get(0).getMetadata().getFilename(), equalTo(gzipFile0.getName()));
			// Uncomment for additional output
			System.out.println(" >> Fetched datasets (with open access links)\n" + dss.toJson(JSON_PRETTY_PRINTER));

			// remove open access link
			ds1.removeOpenAccessLink().get(TIMEOUT, SECONDS);
			assertThat("open access is null", ds1.getMetadata().getOpenAccess(), nullValue());

			// verify that the access link was removed
			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(gzipFile0.getName()).build())
					.build();
			ds2.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds1));
			uncompressedFile = new File(TEST_OUTPUT_DIR, "uncompressed_" + textFile0.getName());
			gunzip(ds2.getOutfile().getCanonicalPath(), uncompressedFile.getCanonicalPath());
			checkFile(textFile0_Original, uncompressedFile);
			// Uncomment for additional output
			System.out.println(" >> Fetched dataset (after removing open-access link):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			// delete file
			dss = Datasets.builder().namespace("namespace").build();
			long totalCount = dss.totalCount().get(TIMEOUT, SECONDS);
			assertThat("number of files stored in the database coincides with expected", totalCount, equalTo(2l));
			ds1.delete().get(TIMEOUT, SECONDS);
			totalCount = dss.totalCount().get(TIMEOUT, SECONDS);
			assertThat("number of files stored in the database coincides with expected", totalCount, equalTo(1l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 10; i++) {
				final File file2 = new File(TEST_OUTPUT_DIR, "files_" + Integer.toString(i) + ".txt");
				write(file2, "This is a test " + Integer.toString(i), UTF_8.name());				
				ids.add(file2.getName());
				ds2 = Dataset.builder()
						.metadata(Metadata.builder().namespace("namespace").filename(file2.getName()).build())
						.build();
				ds2.save(file2).get(TIMEOUT, SECONDS);
			}
			final int size = 3;
			int start = 0;
			dss = Datasets.builder().namespace("namespace").build();
			do {
				count = dss.fetch(start, size, null, null).get(TIMEOUT, SECONDS);				
				if (dss.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + dss.size() + " of " + dss.getTotalCount() + " items\n"
							+ "Namespace: " + dss.getNamespace() + ", Items: " + join(dss.filenames(), ", "));
				}
				start += dss.size();
			} while (!dss.getElements().isEmpty());

			// delete all the files inserted for pagination test
			for (final String id2 : ids) {
				ds2 = Dataset.builder()
						.metadata(Metadata.builder().namespace("namespace").filename(id2).build())
						.build();
				ds2.delete().get(TIMEOUT, SECONDS);
			}
			dss = Datasets.builder().namespace("namespace").build();
			count = dss.fetch(0, Integer.MAX_VALUE, null, null).get(TIMEOUT, SECONDS);
			assertThat("number of fetched files coincides with expected", count, equalTo(1));

			// collect statistics about the collection
			dss = Datasets.builder().namespace("namespace").build();
			MongoCollectionStats stats = dss.stats().get(TIMEOUT, SECONDS);
			assertThat("files statistics are not null", stats, notNullValue());
			System.out.println(" >> Files statistics:\n" + objectToJson(stats, JSON_PRETTY_PRINTER));

			// test default namespace
			ds2 = Dataset.builder()
					.metadata(Metadata.builder().filename(textFile0.getName()).build())
					.build();
			ds2.save(textFile0).get(TIMEOUT, SECONDS);
			assertThat("saved Id is not empty", trim(ds2.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved length is not zero", ds2.getLength(), greaterThan(0l));
			assertThat("saved chuck size is not zero", ds2.getChunkSize(), greaterThan(0l));
			assertThat("saved upload date is not null", ds2.getUploadDate(), notNullValue());
			assertThat("saved checksum is not empty", trim(ds2.getMd5()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds2.getFilename()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds2.getContentType()), allOf(notNullValue(), not(equalTo(""))));
			// uncomment for additional output
			System.out.println(" >> Inserted dataset (text file 0, no namespace):\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			Dataset ds3 = Dataset.builder()
					.metadata(Metadata.builder().filename(textFile0.getName()).build())
					.build();
			ds3.fetch().get(TIMEOUT, SECONDS);
			assertThat("retrieved dataset is not null", ds3, notNullValue());			
			assertThat("retrieved dataset coincides with expected", ds3, equalTo(ds2));
			checkFile(textFile0, ds3.getOutfile());
			// uncomment for additional output
			System.out.println(" >> Fetched dataset (text file 0, no namespace):\n" + ds3.toJson(JSON_PRETTY_PRINTER));
			
			dss = Datasets.builder().build();
			stats = dss.stats().get(TIMEOUT, SECONDS);
			assertThat("files statistics are not null", stats, notNullValue());
			System.out.println(" >> Files statistics (no namespace):\n" + objectToJson(stats, JSON_PRETTY_PRINTER));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DatasetCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DatasetCollectionTest.test() has finished");
		}
	}	

	private void checkFile(final File originalFile, final File retrievedFile) throws IOException {
		final String original = readFileToString(originalFile);
		assertThat("original file content is not null", original, notNullValue());
		assertThat("original file content is not empty", isNotBlank(original), equalTo(true));
		final String retrieved = readFileToString(retrievedFile);
		assertThat("retrieved file content is not null", retrieved, notNullValue());
		assertThat("retrieved file content is not empty", isNotBlank(retrieved), equalTo(true));
		assertThat("retrieved file content coincides with expected", retrieved, equalTo(original));
	}

}