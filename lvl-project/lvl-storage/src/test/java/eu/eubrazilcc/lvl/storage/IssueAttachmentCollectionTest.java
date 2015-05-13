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
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.util.MimeUtils.mimeType;
import static eu.eubrazilcc.lvl.storage.support.dao.IssueAttachmentDAO.ISSUE_ATTACHMENT_DAO;
import static eu.eubrazilcc.lvl.test.testset.ImageTestHelper.createTestJpeg;
import static eu.eubrazilcc.lvl.test.testset.ImageTestHelper.createTestPng;
import static java.lang.System.getProperty;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
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

import eu.eubrazilcc.lvl.core.Metadata;
import eu.eubrazilcc.lvl.core.Sorting;
import eu.eubrazilcc.lvl.core.Sorting.Order;
import eu.eubrazilcc.lvl.core.support.IssueAttachment;

/**
 * Tests issue attachments collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class IssueAttachmentCollectionTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			IssueAttachmentCollectionTest.class.getSimpleName() + "_" + random(8, true, true)));

	@Before
	public void setUp() {
		deleteQuietly(TEST_OUTPUT_DIR);
		TASK_RUNNER.preload();
	}

	@After
	public void cleanUp() throws IOException {
		ISSUE_ATTACHMENT_DAO.cleanCache();
		deleteQuietly(TEST_OUTPUT_DIR);		
	}

	@Test
	public void test() {
		System.out.println("IssueAttachmentCollectionTest.test()");
		try {
			// create test files
			final String fname1 = "11111111-1111-1111-1111-111111111111";
			final String originalFname1 = "screenshot.png";
			File imgFile1 = createTestPng(TEST_OUTPUT_DIR, originalFname1);
			Metadata metadata1 = Metadata.builder()
					.description("Optional description")
					.originalFilename(originalFname1)
					.build();

			final String fname2 = "22222222-2222-2222-2222-222222222222";
			final String originalFname2 = "image.jpg";
			File imgFile2 = createTestJpeg(TEST_OUTPUT_DIR, originalFname2);
			Metadata metadata2 = Metadata.builder()
					.originalFilename(originalFname2)
					.build();

			// insert
			ISSUE_ATTACHMENT_DAO.insert(null, fname1, imgFile1, metadata1);
			ISSUE_ATTACHMENT_DAO.insert(null, fname2, imgFile2, metadata2);

			// find
			metadata1.setIsLastestVersion(fname1);
			IssueAttachment attachment = ISSUE_ATTACHMENT_DAO.find(null, fname1);			
			assertThat("file1 is not null", attachment, notNullValue());
			assertThat("file1 is not null", attachment.getOutfile(), notNullValue());
			assertThat("file1 exists", attachment.getOutfile().exists(), equalTo(true));
			assertThat("file1 is not empty", attachment.getOutfile().length() > 0l, equalTo(true));
			checkIssueAttachment(attachment, imgFile1, fname1, metadata1);
			checkFile(imgFile1, attachment.getOutfile());
			// Uncomment for additional output 
			System.out.println(attachment.toString());

			metadata2.setIsLastestVersion(fname2);
			attachment = ISSUE_ATTACHMENT_DAO.find(null, fname2);			
			assertThat("file2 is not null", attachment, notNullValue());
			assertThat("file2 is not null", attachment.getOutfile(), notNullValue());
			assertThat("file2 exists", attachment.getOutfile().exists(), equalTo(true));
			assertThat("file2 is not empty", attachment.getOutfile().length() > 0l, equalTo(true));
			checkIssueAttachment(attachment, imgFile2, fname2, metadata2);
			checkFile(imgFile2, attachment.getOutfile());
			// Uncomment for additional output 
			System.out.println(attachment.toString());

			boolean fileExists = ISSUE_ATTACHMENT_DAO.fileExists(null, fname1);
			assertThat("file1 exists result coincides with expected", fileExists, equalTo(true));

			fileExists = ISSUE_ATTACHMENT_DAO.fileExists(null, fname2);
			assertThat("file2 exists result coincides with expected", fileExists, equalTo(true));

			// insert duplicates (versions)
			imgFile1 = createTestPng(TEST_OUTPUT_DIR, originalFname1);
			metadata1.setDescription("New version of image file 1");
			ISSUE_ATTACHMENT_DAO.insert(null, fname1, imgFile1, metadata1);

			// find the latest version after inserting the new version
			attachment = ISSUE_ATTACHMENT_DAO.find(null, fname1);			
			assertThat("file1 is not null", attachment, notNullValue());
			assertThat("file1 is not null", attachment.getOutfile(), notNullValue());
			assertThat("file1 exists", attachment.getOutfile().exists(), equalTo(true));
			assertThat("file1 is not empty", attachment.getOutfile().length() > 0l, equalTo(true));
			checkIssueAttachment(attachment, imgFile1, fname1, metadata1);
			checkFile(imgFile1, attachment.getOutfile());
			// Uncomment for additional output 
			System.out.println(attachment.toString());

			// update metadata
			metadata1.setDescription("New version of the new version of image file 1");
			ISSUE_ATTACHMENT_DAO.updateMetadata(null, fname1, metadata1);

			// find the latest version after updating the metadata
			attachment = ISSUE_ATTACHMENT_DAO.find(null, fname1);			
			assertThat("file1 is not null", attachment, notNullValue());
			assertThat("file1 is not null", attachment.getOutfile(), notNullValue());
			assertThat("file1 exists", attachment.getOutfile().exists(), equalTo(true));
			assertThat("file1 is not empty", attachment.getOutfile().length() > 0l, equalTo(true));
			checkIssueAttachment(attachment, imgFile1, fname1, metadata1);
			checkFile(imgFile1, attachment.getOutfile());
			// Uncomment for additional output 
			System.out.println(attachment.toString());

			// list all files ignoring previous versions
			List<IssueAttachment> attachments = ISSUE_ATTACHMENT_DAO.findAll(null);
			assertThat("files is not null", attachments, notNullValue());
			assertThat("files is not empty", attachments.isEmpty(), equalTo(false));
			assertThat("files size coincides with expected", attachments.size(), equalTo(2));
			boolean found1 = false, found2 = false;
			for (int i = 0; i < attachments.size() && !found1 && ! found2; i++) {
				attachment = attachments.get(i);
				metadata1 = attachment.getMetadata();				
				if (fname1.equals(attachment.getFilename()) && metadata1.getIsLastestVersion().equals(attachment.getFilename())
						&& imgFile1.getName().equals(metadata1.getOriginalFilename())
						&& "New version of image file 1".equals(metadata1.getDescription())) {
					found1 = true;
				} else if (fname2.equals(attachment.getFilename()) && metadata2.getIsLastestVersion().equals(attachment.getFilename())
						&& imgFile2.getName().equals(metadata2.getOriginalFilename())
						&& metadata2.getDescription() == null) {
					found2 = true;
				} else {
					throw new Exception("Unexpected file found when listing all files ignoring previous versions: " + attachment);
				}
			}

			// list all the available versions of a given file
			attachments = ISSUE_ATTACHMENT_DAO.listVersions(null, fname1, 0, Integer.MAX_VALUE, null,
					Sorting.builder().field("uploadDate").order(Order.ASC).build(), null);			
			assertThat("files is not null", attachments, notNullValue());
			assertThat("files is not empty", attachments.isEmpty(), equalTo(false));
			assertThat("files size coincides with expected", attachments.size(), equalTo(2));
			found1 = false; found2 = false;
			for (int i = 0; i < attachments.size() && !found1 && ! found2; i++) {
				attachment = attachments.get(i);
				metadata1 = attachment.getMetadata();
				if (fname1.equals(attachment.getFilename()) && imgFile1.getName().equals(metadata1.getOriginalFilename())
						&& "New version of image file 1".equals(metadata1.getDescription())
						&& metadata1.getIsLastestVersion().equals(attachment.getFilename())) {
					found1 = true;
				} else if (fname1.equals(attachment.getFilename()) && imgFile1.getName().equals(metadata1.getOriginalFilename())
						&& "Optional description".equals(metadata1.getDescription())
						&& metadata1.getIsLastestVersion() == null) {
					found2 = true;
				} else {
					throw new Exception("Unexpected file found when listing all the available versions of a given file: " + attachment);
				}
			}

			// remove latest version
			final String originalFname1b = "screenshot_b.png";
			final File imgFile1b = createTestPng(TEST_OUTPUT_DIR, originalFname1b);
			metadata1 = Metadata.builder()
					.description("Third version to be removed")
					.originalFilename(originalFname1)
					.build();			
			ISSUE_ATTACHMENT_DAO.insert(null, fname1, imgFile1b, metadata1);

			attachments = ISSUE_ATTACHMENT_DAO.listVersions(null, fname1, 0, Integer.MAX_VALUE, null, null, null);
			assertThat("files is not null", attachments, notNullValue());
			assertThat("files is not empty", attachments.isEmpty(), equalTo(false));
			assertThat("files size coincides with expected", attachments.size(), equalTo(3));

			metadata1.setIsLastestVersion(fname1);
			attachment = ISSUE_ATTACHMENT_DAO.find(null, fname1);			
			assertThat("file is not null", attachment, notNullValue());
			assertThat("file is not null", attachment.getOutfile(), notNullValue());
			assertThat("file exists", attachment.getOutfile().exists(), equalTo(true));
			assertThat("file is not empty", attachment.getOutfile().length() > 0l, equalTo(true));
			checkIssueAttachment(attachment, imgFile1b, fname1, metadata1);
			checkFile(imgFile1b, attachment.getOutfile());

			ISSUE_ATTACHMENT_DAO.undoLatestVersion(null, fname1);

			attachments = ISSUE_ATTACHMENT_DAO.listVersions(null, fname1, 0, Integer.MAX_VALUE, null, null, null);
			assertThat("files is not null", attachments, notNullValue());
			assertThat("files is not empty", attachments.isEmpty(), equalTo(false));
			assertThat("files size coincides with expected", attachments.size(), equalTo(2));

			metadata1.setIsLastestVersion(fname1);
			metadata1.setDescription("New version of the new version of image file 1");
			attachment = ISSUE_ATTACHMENT_DAO.find(null, fname1);			
			assertThat("file is not null", attachment, notNullValue());
			assertThat("file is not null", attachment.getOutfile(), notNullValue());
			assertThat("file exists", attachment.getOutfile().exists(), equalTo(true));
			assertThat("file is not empty", attachment.getOutfile().length() > 0l, equalTo(true));
			metadata1.setOpenAccessDate(attachment.getMetadata().getOpenAccessDate());
			checkIssueAttachment(attachment, imgFile1, fname1, metadata1);
			checkFile(imgFile1, attachment.getOutfile());
			// Uncomment for additional output 
			System.out.println(attachment.toString());

			// remove all versions
			ISSUE_ATTACHMENT_DAO.delete(null, fname1);
			final long numRecords = ISSUE_ATTACHMENT_DAO.count(null);
			assertThat("number of files stored in the database coincides with expected", numRecords, equalTo(1l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {				
				final String fnameX = randomUUID().toString();
				final String originalFnameX = "image_" + Integer.toString(i) + ".png";
				final File imgFileX = createTestPng(TEST_OUTPUT_DIR, originalFnameX);
				ids.add(fnameX);
				ISSUE_ATTACHMENT_DAO.insert(null, fnameX, imgFileX, Metadata.builder()
						.originalFilename(originalFnameX)
						.build());
				ISSUE_ATTACHMENT_DAO.insert(null, fname1, imgFile1, Metadata.builder()
						.originalFilename(originalFname1)
						.build());
			}
			final int size = 3;
			int start = 0;
			attachments = null;
			final MutableLong count = new MutableLong(0l);
			do {
				attachments = ISSUE_ATTACHMENT_DAO.list(null, start, size, null, null, count);
				if (attachments.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + attachments.size() + " of " + count.getValue() + " items");
				}
				start += attachments.size();
			} while (!attachments.isEmpty());
			for (final String id2 : ids) {			
				ISSUE_ATTACHMENT_DAO.delete(null, id2);
			}
			ISSUE_ATTACHMENT_DAO.stats(null, System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("IssueAttachmentCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("IssueAttachmentCollectionTest.test() has finished");
		}
	}

	private static void checkIssueAttachment(final IssueAttachment attachment, final File file, final String filename, final Metadata metadata) {
		assertThat("lenght type coincides with expected", attachment.getLength(), equalTo(file.length()));
		assertThat("content type coincides with expected", attachment.getContentType(), equalTo(mimeType(file)));
		assertThat("filename coincides with expected", attachment.getFilename(), equalTo(isNotBlank(filename) ? filename : file.getName()));
		assertThat("metadata coincides with expected", attachment.getMetadata(), equalTo(metadata));
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