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

package eu.eubrazilcc.lvl.storage;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gunzip;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.storage.dao.DatasetDAO.DATASET_DAO;
import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.Dataset;
import eu.eubrazilcc.lvl.core.Dataset.DatasetMetadata;
import eu.eubrazilcc.lvl.core.Target;

/**
 * Tests dataset collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DatasetCollectionTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			DatasetCollectionTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
		TASK_RUNNER.preload();
	}

	@After
	public void cleanUp() throws IOException {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("DatasetCollectionTest.test()");
		try {
			// create test files
			File textFile1 = new File(TEST_OUTPUT_DIR, "file1.txt");
			write(textFile1, "This is a test", UTF_8.name());
			assertThat("test Text file exists", textFile1.exists(), equalTo(true));
			assertThat("test Text file is not empty", textFile1.length() > 0l, equalTo(true));

			final String gzipFilename1 = gzip(textFile1.getCanonicalPath());
			assertThat("test GZIP filename is not null", gzipFilename1, notNullValue());
			assertThat("test GZIP filename is not empty", isNotEmpty(gzipFilename1), equalTo(true));
			final File gzipFile1 = new File(gzipFilename1);
			assertThat("test GZIP file exists", gzipFile1.exists(), equalTo(true));
			assertThat("test GZIP file is not empty", gzipFile1.length() > 0l, equalTo(true));

			// insert
			DatasetMetadata metadata = DatasetMetadata.builder()
					.tags(newHashSet("tag1", "tag2", "tag3"))					
					.description("Optional description")
					.target(Target.builder().type("sequence").id("JP540074").filter("export_fasta").build())
					.build();
			DATASET_DAO.insert("namespace", textFile1, metadata);
			DATASET_DAO.insert("namespace", gzipFile1, null);

			// find
			File file = new File(TEST_OUTPUT_DIR, "out_" + textFile1.getName());
			Dataset dataset = DATASET_DAO.find("namespace", textFile1.getName(), file);			
			assertThat("text dataset is not null", dataset, notNullValue());
			assertThat("text file is not null", file, notNullValue());
			assertThat("text file exists", file.exists(), equalTo(true));
			assertThat("text file is not empty", file.length() > 0l, equalTo(true));
			checkDataset(dataset, textFile1, metadata);
			checkFile(textFile1, file);
			/* Uncomment for additional output */
			System.out.println(dataset.toString());

			file = new File(TEST_OUTPUT_DIR, "out_" + gzipFile1.getName());
			dataset = DATASET_DAO.find("namespace", gzipFile1.getName(), file);			
			assertThat("binary dataset is not null", dataset, notNullValue());
			assertThat("binary file is not null", file, notNullValue());
			assertThat("binary file exists", file.exists(), equalTo(true));
			assertThat("binary file is not empty", file.length() > 0l, equalTo(true));
			checkDataset(dataset, gzipFile1, null);
			final File uncompressedFile = new File(TEST_OUTPUT_DIR, "uncompressed_" + textFile1.getName());
			gunzip(file.getCanonicalPath(), uncompressedFile.getCanonicalPath());
			checkFile(textFile1, uncompressedFile);
			/* Uncomment for additional output */
			System.out.println(dataset.toString());			
			
			// insert duplicates (versions)
			write(textFile1, "The second version of the text file is larger than the previous one", UTF_8.name());
			assertThat("test Text file exists", textFile1.exists(), equalTo(true));
			assertThat("test Text file is not empty", textFile1.length() > 0l, equalTo(true));
			metadata.setPublicLink("public_link");
			metadata.setDescription("New version of text file 1");
			DATASET_DAO.insert("namespace", textFile1, metadata);
			
			// find the latest version after inserting the new version
			file = new File(TEST_OUTPUT_DIR, "out2_" + textFile1.getName());
			dataset = DATASET_DAO.find("namespace", textFile1.getName(), file);			
			assertThat("text dataset is not null", dataset, notNullValue());
			assertThat("text file is not null", file, notNullValue());
			assertThat("text file exists", file.exists(), equalTo(true));
			assertThat("text file is not empty", file.length() > 0l, equalTo(true));
			checkDataset(dataset, textFile1, metadata);
			checkFile(textFile1, file);
			/* Uncomment for additional output */
			System.out.println(dataset.toString());
			
			// list all files ignoring previous versions
			List<Dataset> datasets = DATASET_DAO.findAll("namespace");
			assertThat("datasets is not null", datasets, notNullValue());
			assertThat("datasets is not empty", datasets.isEmpty(), equalTo(false));
			assertThat("datasets size coincides with expected", datasets.size(), equalTo(2));
			boolean found1 = false, found2 = false;
			for (int i = 0; i < datasets.size() && !found1 && ! found2; i++) {
				dataset = datasets.get(i);
				metadata = (DatasetMetadata)dataset.getMetadata();
				if (textFile1.getName().equals(dataset.getFilename()) && "New version of text file 1".equals(metadata.getDescription())) {
					found1 = true;
				} else if (gzipFile1.getName().equals(dataset.getFilename()) && "Optional description".equals(metadata.getDescription())) {
					found1 = true;
				} else {
					throw new Exception("Unexpected dataset found: " + dataset);
				}
			}
			
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
				DATASET_DAO.insert("namespace", file2, null);				
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

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("DatasetCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("DatasetCollectionTest.test() has finished");
		}
	}

	private static void checkDataset(final Dataset dataset, final File file, final DatasetMetadata metadata) {
		assertThat("lenght type coincides with expected", dataset.getLength(), equalTo(file.length()));
		assertThat("content type coincides with expected", dataset.getContentType(), equalTo(mimeType(file)));
		assertThat("filename coincides with expected", dataset.getFilename(), equalTo(file.getName()));
		assertThat("namespace coincides with expected", dataset.getNamespace(), equalTo("namespace"));
		assertThat("metadata coincides with expected", (DatasetMetadata)dataset.getMetadata(), equalTo(metadata));
	}

	private void checkFile(final File originalFile, final File retrievedFile) throws IOException {
		final String original = readFileToString(originalFile);
		assertThat("original file content is not null", original, notNullValue());
		assertThat("original file content is not empty", isNotEmpty(original), equalTo(true));
		final String retrieved = readFileToString(retrievedFile);
		assertThat("retrieved file content is not null", retrieved, notNullValue());
		assertThat("retrieved file content is not empty", isNotEmpty(retrieved), equalTo(true));
		assertThat("retrieved file content coincides with expected", retrieved, equalTo(original));
	}

}