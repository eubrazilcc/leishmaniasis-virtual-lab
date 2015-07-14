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
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.storage.mongodb.MongoCollectionConfigurer.hash2bucket;
import static eu.eubrazilcc.lvl.storage.mongodb.jackson.MongoJsonOptions.JSON_PRETTY_PRINTER;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.storage.base.Metadata;

/**
 * Tests {@link Dataset} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetCollectionTest {

	private static int BUCKET = 16;

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
			ds0.save(textFile0);
			assertThat("saved Id is not empty", trim(ds0.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved length is not zero", ds0.getLength(), greaterThan(0l));
			assertThat("saved chuck size is not zero", ds0.getChunkSize(), greaterThan(0l));
			assertThat("saved upload date is not empty", ds0.getUploadDate(), notNullValue());
			assertThat("saved checksum is not empty", trim(ds0.getMd5()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds0.getFilename()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds0.getContentType()), allOf(notNullValue(), not(equalTo(""))));
			// uncomment for additional output
			System.out.println(" >> Inserted dataset:\n" + ds0.toJson(JSON_PRETTY_PRINTER));			

			metadata = Metadata.builder()
					.namespace("namespace")
					.filename(gzipFile0.getName())
					.build();
			final Dataset ds1 = Dataset.builder()
					.metadata(metadata)
					.build();
			ds1.save(gzipFile0);
			assertThat("saved Id is not empty", trim(ds1.getId()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved length is not zero", ds1.getLength(), greaterThan(0l));
			assertThat("saved chuck size is not zero", ds1.getChunkSize(), greaterThan(0l));
			assertThat("saved upload date is not empty", ds1.getUploadDate(), notNullValue());
			assertThat("saved checksum is not empty", trim(ds1.getMd5()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds1.getFilename()), allOf(notNullValue(), not(equalTo(""))));
			assertThat("saved filename is not empty", trim(ds1.getContentType()), allOf(notNullValue(), not(equalTo(""))));
			// uncomment for additional output
			System.out.println(" >> Inserted dataset:\n" + ds1.toJson(JSON_PRETTY_PRINTER));

			// get file from database
			Dataset ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(textFile0.getName()).build())
					.build();
			ds2.fetch();
			assertThat("retrieved dataset is not null", ds2, notNullValue());			
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds0));
			// uncomment for additional output
			System.out.println(" >> Fetched dataset:\n" + ds2.toJson(JSON_PRETTY_PRINTER));

			ds2 = Dataset.builder()
					.metadata(Metadata.builder().namespace("namespace").filename(gzipFile0.getName()).build())
					.build();
			ds2.fetch();
			assertThat("retrieved dataset is not null", ds2, notNullValue());
			assertThat("retrieved dataset coincides with expected", ds2, equalTo(ds1));
			// uncomment for additional output
			System.out.println(" >> Fetched dataset:\n" + ds2.toJson(JSON_PRETTY_PRINTER));


			// TODO

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DatasetCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DatasetCollectionTest.test() has finished");
		}
	}

}