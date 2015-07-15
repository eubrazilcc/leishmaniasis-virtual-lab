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

package eu.eubrazilcc.lvl.core;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.AllTests.TEST_RESOURCES_PATH;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gunzip;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.gzip;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.tarGzipDir;
import static eu.eubrazilcc.lvl.core.io.FileCompressor.unGzipUnTar;
import static eu.eubrazilcc.lvl.core.util.TestUtils.GB_SEQUENCES_FOLDER;
import static eu.eubrazilcc.lvl.core.util.TestUtils.getGBSeqXMLFiles;
import static java.lang.System.getProperty;
import static java.util.Collections.shuffle;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.io.FileCompressor;

/**
 * Tests file archiving and compression with {@link FileCompressor} utility.
 * @author Erik Torres <ertorser@upv.es>
 */
public class FileCompressorTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			FileCompressorTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("FileCompressorTest.test()");
		try {
			// test create tarball from directory
			final String filenameNoPath = "bundle.tar.gz";
			final File tarGzipFile = new File(TEST_OUTPUT_DIR, filenameNoPath);
			final String tarGzipFilename = tarGzipFile.getCanonicalPath();
			final String sourceDir = concat(TEST_RESOURCES_PATH, GB_SEQUENCES_FOLDER);
			System.out.println(" >> Will compress directory " + sourceDir + " to TARBALL " + tarGzipFilename);
			tarGzipDir(sourceDir, tarGzipFilename);
			assertThat("tar.gz file exists", tarGzipFile.exists(), equalTo(true));
			assertThat("tar.gz file is not empty", tarGzipFile.length() > 0l, equalTo(true));

			// test tarball uncompress
			final String uncompressedDirname = tarGzipFilename + "_uncompressed";
			final File uncompressedDir = new File(uncompressedDirname);
			System.out.println(" >> Will uncompress TARBALL " + tarGzipFilename + " to directory " + uncompressedDirname);
			final List<String> uncompressedFiles = unGzipUnTar(tarGzipFilename, uncompressedDirname);
			assertThat("tar.gz uncompressed directory exists", uncompressedDir.exists(), equalTo(true));
			final int numberOfFiles = listFiles(new File(sourceDir), null, true).size();
			assertThat("number of uncompressed files coincides with expected", uncompressedFiles.size(), equalTo(numberOfFiles));
			for (final String uncompressedFile : uncompressedFiles) {
				assertThat("uncompressed file exists: " + uncompressedFile, new File(uncompressedFile).exists(), equalTo(true));
				assertThat("uncompressed file is not empty: " + uncompressedFile, new File(uncompressedFile).length() > 0l, equalTo(true));
				checkFile(new File(sourceDir, getName(uncompressedFile)), new File(uncompressedFile));				
			}

			// test compress file with GZIP
			final List<File> files = newArrayList(getGBSeqXMLFiles());
			shuffle(files);
			final File srcFile = files.get(0);
			final String srcFilename = srcFile.getCanonicalPath();
			System.out.println(" >> Will compress file " + srcFilename + " with GZIP");
			final String outFilename = gzip(srcFilename);
			assertThat("output GZIP filename is not null", outFilename, notNullValue());
			assertThat("output GZIP filename is not empty", isNotEmpty(outFilename), equalTo(true));
			final File outFile = new File(outFilename);
			assertThat("output GZIP file exists", outFile.exists(), equalTo(true));
			assertThat("output GZIP file is not empty", outFile.length() > 0l, equalTo(true));

			// test uncompress GZIP compressed file
			final File uncompressedFile = new File(TEST_OUTPUT_DIR, srcFile.getName());
			final String uncompressedFilename = uncompressedFile.getCanonicalPath();
			gunzip(outFilename, uncompressedFilename);
			assertThat("file uncompressed from GZIP exists: " + uncompressedFilename, uncompressedFile.exists(), equalTo(true));
			assertThat("file uncompressed from GZIP is not empty: " + uncompressedFilename, uncompressedFile.length() > 0l, equalTo(true));
			checkFile(srcFile, uncompressedFile);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("FileCompressorTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("FileCompressorTest.test() has finished");
		}
	}

	@After
	public void cleanUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	private void checkFile(final File originalFile, final File uncompressedFile) throws IOException {
		final String original = readFileToString(originalFile);
		assertThat("original file content is not null", original, notNullValue());
		assertThat("original file content is not empty", isNotEmpty(original), equalTo(true));
		final String decompressed = readFileToString(uncompressedFile);
		assertThat("decompressed file content is not null", decompressed, notNullValue());
		assertThat("decompressed file content is not empty", isNotEmpty(decompressed), equalTo(true));
		assertThat("uncompressed file content coincides with expected", decompressed, equalTo(original));
	}

}