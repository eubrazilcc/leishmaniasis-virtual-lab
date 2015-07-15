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

import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gunzip;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.storage.base.SaveOptions.SAVE_OVERRIDING;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.hash2bucket;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions.JSON_PRETTY_PRINTER;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.base.Metadata;

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
			final String[] filenames = { "-pTE7eaWAl@linkedin", "QUy9vZwul7@linkedin", "cala@lvl", "root@lvl" };
			final int[] buckets = { 12, 11, 13, 15 };
			for (int i = 0; i < filenames.length; i++) {
				int bucket = hash2bucket(filenames[i], BUCKET);
				assertThat("bucket coincides with expected", bucket, equalTo(buckets[i]));
				// uncomment for additional output
				System.err.println(" >> Filename '" + filenames[i] + "', bucket: " + bucket);
			}

			// create test files
			File textFile0 = new File(TEST_OUTPUT_DIR, "file1.txt");
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
					.newOther("tags", newHashSet("tag1", "tag2", "tag3"))
					.newOther("description", "Optional description")
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
			write(textFile0, "The second version of the text file is larger than the previous one", UTF_8.name());
			assertThat("test Text file exists", textFile0.exists(), equalTo(true));
			assertThat("test Text file is not empty", textFile0.length() > 0l, equalTo(true));
			metadata = Metadata.builder()
					.namespace("namespace")
					.filename(textFile0.getName())
					.newOther("openaccess_link", "public_link")
					.newOther("description", "New version of text file 0")
					.build();
			ds0.setMetadata(metadata);
			ds0.save(textFile0, SAVE_OVERRIDING).get(TIMEOUT, SECONDS); // TODO
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
					.newOther("description", "New version of the new version of text file 0")
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



			/* TODO

			// test type-ahead with filename
			List<String> filenames = DATASET_DAO.typeahead("namespace", "ile1", 100);
			assertThat("filenames are not null", filenames, notNullValue());
			assertThat("filenames are not empty", filenames.isEmpty(), equalTo(false));
			assertThat("number of filenames coincides with expected", filenames.size(), equalTo(2));
			// Uncomment for additional output
			System.out.println(filenames.toString());

			filenames = DATASET_DAO.typeahead("namespace", "ile1", 1);
			assertThat("filenames are not null", filenames, notNullValue());
			assertThat("filenames are not empty", filenames.isEmpty(), equalTo(false));
			assertThat("number of filenames coincides with expected", filenames.size(), equalTo(1));
			// Uncomment for additional output
			System.out.println(filenames.toString());

			filenames = DATASET_DAO.typeahead("namespace", "GZ", 100);
			assertThat("filenames are not null", filenames, notNullValue());
			assertThat("filenames are not empty", filenames.isEmpty(), equalTo(false));
			assertThat("number of filenames coincides with expected", filenames.size(), equalTo(1));
			// Uncomment for additional output
			System.out.println(filenames.toString());

			// create open access link
			final String secret = DATASET_DAO.createOpenAccessLink("namespace", gzipFile1.getName());
			assertThat("secret is not null", secret, notNullValue());
			assertThat("secret is not empty", isNotBlank(secret), equalTo(true));
			// Uncomment for additional output
			System.out.println("Secret to access file anonymously: " + secret);

			// verify that the access link was created
			dataset = DATASET_DAO.find("namespace", gzipFile1.getName());			
			assertThat("binary dataset is not null", dataset, notNullValue());
			assertThat("binary file is not null", dataset.getOutfile(), notNullValue());
			assertThat("binary file exists", dataset.getOutfile().exists(), equalTo(true));
			assertThat("binary file is not empty", dataset.getOutfile().length() > 0l, equalTo(true));
			checkDataset(dataset, gzipFile1, "namespace", null, Metadata.builder()
					.isLastestVersion(gzipFile1.getName())
					.openAccessLink(secret)
					.openAccessDate(dataset.getMetadata().getOpenAccessDate())
					.build());
			// Uncomment for additional output
			System.out.println(dataset.toString());

			// read file from open access link
			dataset = DATASET_DAO.findOpenAccess(secret);
			assertThat("binary dataset is not null", dataset, notNullValue());
			assertThat("binary file is not null", dataset.getOutfile(), notNullValue());
			assertThat("binary file exists", dataset.getOutfile().exists(), equalTo(true));
			assertThat("binary file is not empty", dataset.getOutfile().length() > 0l, equalTo(true));
			checkDataset(dataset, gzipFile1, "namespace", null, Metadata.builder()
					.isLastestVersion(gzipFile1.getName())
					.openAccessLink(secret)
					.openAccessDate(dataset.getMetadata().getOpenAccessDate())
					.build());
			// Uncomment for additional output
			System.out.println(" >> File from open access link: " + dataset.toString());

			// remove open access link
			DATASET_DAO.removeOpenAccessLink("namespace", gzipFile1.getName());					

			// verify that the access link was removed
			dataset = DATASET_DAO.find("namespace", gzipFile1.getName());			
			assertThat("binary dataset is not null", dataset, notNullValue());
			assertThat("binary file is not null", dataset.getOutfile(), notNullValue());
			assertThat("binary file exists", dataset.getOutfile().exists(), equalTo(true));
			assertThat("binary file is not empty", dataset.getOutfile().length() > 0l, equalTo(true));
			checkDataset(dataset, gzipFile1, "namespace", null, Metadata.builder()
					.isLastestVersion(gzipFile1.getName())
					.build());
			// Uncomment for additional output
			System.out.println(dataset.toString());

			// list files with open access links
			List<Dataset> datasets = DATASET_DAO.listOpenAccess("namespace", 0, Integer.MAX_VALUE, null, null, null);
			assertThat("datasets is not null", datasets, notNullValue());
			assertThat("datasets is not empty", datasets.isEmpty(), equalTo(false));
			assertThat("datasets size coincides with expected", datasets.size(), equalTo(1));
			assertThat("datasets contentt coincides with expected", datasets.get(0).getFilename(), equalTo("file1.txt"));
			// Uncomment for additional output
			System.out.println(datasets.toString());

			// list all files ignoring previous versions
			datasets = DATASET_DAO.findAll("namespace");
			assertThat("datasets is not null", datasets, notNullValue());
			assertThat("datasets is not empty", datasets.isEmpty(), equalTo(false));
			assertThat("datasets size coincides with expected", datasets.size(), equalTo(2));
			boolean found1 = false, found2 = false;
			for (int i = 0; i < datasets.size() && !found1 && ! found2; i++) {
				dataset = datasets.get(i);
				metadata = dataset.getMetadata();				
				if (textFile1.getName().equals(dataset.getFilename()) && "New version of text file 1".equals(metadata.getDescription())) {
					found1 = true;
				} else if (gzipFile1.getName().equals(dataset.getFilename()) && metadata.getIsLastestVersion().equals(dataset.getFilename())) {
					found2 = true;
				} else {
					throw new Exception("Unexpected dataset found: " + dataset);
				}
			}

			// list all the available versions of a given file
			datasets = DATASET_DAO.listVersions("namespace", textFile1.getName(), 0, Integer.MAX_VALUE, null,
					Sorting.builder().field("uploadDate").order(Order.ASC).build(), null);			
			assertThat("datasets is not null", datasets, notNullValue());
			assertThat("datasets is not empty", datasets.isEmpty(), equalTo(false));
			assertThat("datasets size coincides with expected", datasets.size(), equalTo(2));
			found1 = false; found2 = false;
			for (int i = 0; i < datasets.size() && !found1 && ! found2; i++) {
				dataset = datasets.get(i);
				metadata = dataset.getMetadata();				
				if (textFile1.getName().equals(dataset.getFilename()) && "New version of text file 1".equals(metadata.getDescription()) 
						&& "public_link".equals(metadata.getOpenAccessLink()) && metadata.getOpenAccessDate() != null) {
					found1 = true;
				} else if (textFile1.getName().equals(dataset.getFilename()) && "Optional description".equals(metadata.getDescription())
						&& metadata.getOpenAccessLink() == null) {
					found2 = true;
				} else {
					throw new Exception("Unexpected dataset found: " + dataset);
				}
			}

			// remove latest version
			final File textFile2 = new File(TEST_OUTPUT_DIR, "file2.txt");
			write(textFile2, "Third version to be removed", UTF_8.name());
			assertThat("test Text file exists", textFile2.exists(), equalTo(true));
			assertThat("test Text file is not empty", textFile2.length() > 0l, equalTo(true));
			DATASET_DAO.insert("namespace", textFile1.getName(), textFile2, metadata);

			datasets = DATASET_DAO.listVersions("namespace", textFile1.getName(), 0, Integer.MAX_VALUE, null, null, null);
			assertThat("datasets is not null", datasets, notNullValue());
			assertThat("datasets is not empty", datasets.isEmpty(), equalTo(false));
			assertThat("datasets size coincides with expected", datasets.size(), equalTo(3));

			metadata.setIsLastestVersion(textFile1.getName());
			dataset = DATASET_DAO.find("namespace", textFile1.getName());			
			assertThat("text dataset is not null", dataset, notNullValue());
			assertThat("text file is not null", dataset.getOutfile(), notNullValue());
			assertThat("text file exists", dataset.getOutfile().exists(), equalTo(true));
			assertThat("text file is not empty", dataset.getOutfile().length() > 0l, equalTo(true));
			checkDataset(dataset, textFile2, "namespace", textFile1.getName(), metadata);
			checkFile(textFile2, dataset.getOutfile());

			DATASET_DAO.undoLatestVersion("namespace", textFile1.getName());

			datasets = DATASET_DAO.listVersions("namespace", textFile1.getName(), 0, Integer.MAX_VALUE, null, null, null);
			assertThat("datasets is not null", datasets, notNullValue());
			assertThat("datasets is not empty", datasets.isEmpty(), equalTo(false));
			assertThat("datasets size coincides with expected", datasets.size(), equalTo(2));

			metadata.setIsLastestVersion(textFile1.getName());
			metadata.setOpenAccessLink("public_link");
			metadata.setDescription("New version of the new version of text file 1");
			dataset = DATASET_DAO.find("namespace", textFile1.getName());			
			assertThat("text dataset is not null", dataset, notNullValue());
			assertThat("text file is not null", dataset.getOutfile(), notNullValue());
			assertThat("text file exists", dataset.getOutfile().exists(), equalTo(true));
			assertThat("text file is not empty", dataset.getOutfile().length() > 0l, equalTo(true));
			metadata.setOpenAccessDate(dataset.getMetadata().getOpenAccessDate());
			checkDataset(dataset, textFile1, "namespace", null, metadata);
			checkFile(textFile1, dataset.getOutfile());
			// Uncomment for additional output
			System.out.println(dataset.toString());

			// remove all versions
			DATASET_DAO.delete("namespace", textFile1.getName());
			final long numRecords = DATASET_DAO.count("namespace");
			assertThat("number of files stored in the database coincides with expected", numRecords, equalTo(1l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final File file2 = new File(TEST_OUTPUT_DIR, "files_" + Integer.toString(i) + ".txt");
				write(file2, "This is a test " + Integer.toString(i), UTF_8.name());				
				ids.add(file2.getCanonicalPath());
				DATASET_DAO.insert("namespace", null, file2, null);
				DATASET_DAO.insert("namespace", null, textFile1, null);
			}
			final int size = 3;
			int start = 0;
			datasets = null;
			final MutableLong count = new MutableLong(0l);
			do {
				datasets = DATASET_DAO.list("namespace", start, size, null, null, count);
				if (datasets.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + datasets.size() + " of " + count.getValue() + " items");
				}
				start += datasets.size();
			} while (!datasets.isEmpty());
			for (final String id2 : ids) {			
				DATASET_DAO.delete("namespace", id2);
			}
			DATASET_DAO.stats("namespace", System.out);

			// test default namespace
			DATASET_DAO.insert(null, null, textFile1, metadata);

			dataset = DATASET_DAO.find(null, textFile1.getName());			
			assertThat("text dataset is not null", dataset, notNullValue());
			assertThat("text file is not null", dataset.getOutfile(), notNullValue());
			assertThat("text file exists", dataset.getOutfile().exists(), equalTo(true));
			assertThat("text file is not empty", dataset.getOutfile().length() > 0l, equalTo(true));
			checkDataset(dataset, textFile1, "", null, metadata);
			checkFile(textFile1, dataset.getOutfile());
			// Uncomment for additional output
			System.out.println(dataset.toString());

			DATASET_DAO.stats(null, System.out);

			 */

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