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

package eu.eubrazilcc.lvl.service;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.service.util.TestUtils.getFastaFiles;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static java.lang.System.getProperty;
import static java.util.Collections.shuffle;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eubrazilcc.lvl.service.workflow.WorkflowDefinition;
import eu.eubrazilcc.lvl.service.workflow.WorkflowStatus;
import eu.eubrazilcc.lvl.service.workflow.WorkspaceParameters;
import eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector;

/**
 * Tests {@link ESCentralConnector}.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ESCentralTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			ESCentralTest.class.getSimpleName() + "_" + random(8, true, true)));

	private static final List<String> ESC_DOCUMENTS = newArrayList();

	@Before
	public void setUp() {
		// setup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);
	}

	@After
	public void cleanUp() throws IOException {
		// cleanup test file-system environment
		deleteQuietly(TEST_OUTPUT_DIR);
		for (final String document : ESC_DOCUMENTS) {
			try {
				ESCENTRAL_CONN.deleteFile(document);
			} catch (Exception ignore) { }
		}
	}

	@Test
	public void test() {
		System.out.println("ESCentralTest.test()");
		try {			
			// test file uploading to e-SC
			final Collection<File> files = getFastaFiles();
			assertThat("FASTA files is not null", files, notNullValue());
			assertThat("FASTA files is not empty", !files.isEmpty(), equalTo(true));

			final List<File> fileList = newArrayList(files);
			shuffle(fileList);
			final File fastaFile = fileList.get(0);
			System.out.println(" >> FASTA file: " + fastaFile.getCanonicalPath());

			final String inputFileId = ESCENTRAL_CONN.uploadFile(fastaFile);
			assertThat("input file Id is not null", inputFileId, notNullValue());
			assertThat("input file Id is not empty", isNotBlank(inputFileId), equalTo(true));
			ESC_DOCUMENTS.add(inputFileId);
			/* uncomment for additional output */
			System.out.println(" >> e-SC document Id: " + inputFileId);

			// test listing available workflows from e-SC
			final List<WorkflowDefinition> workflows = ESCENTRAL_CONN.listWorkflows();
			assertThat("workflow list is not null", workflows, notNullValue());
			// TODO : after the API is updated, check that the list is not empty

			// test get workflow definition from e-SC
			final WorkflowDefinition workflow = ESCENTRAL_CONN.getWorkflow("677");
			assertThat("workflow definition is not null", workflow, notNullValue());
			/* uncomment for additional output */
			System.out.println(" >> Workflow definition: " + workflow.toString());

			// test get workflow parameters from e-SC
			// TODO : after the API is updated, get and check the parameters

			// modify the default parameters for execution
			final WorkspaceParameters parameters = WorkspaceParameters.builder()
					.parameter("input_file", "Source", inputFileId)
					.parameter("seqboot", "Number of replicates", "5")
					.parameter("seqboot", "Random number seed", "13457")
					.parameter("dnapars", "Number of data sets", "5")
					.parameter("dnapars", "Outgroup root", "10")	
					.build();

			// test submitting a workflow to e-SC
			final String invocationId = ESCENTRAL_CONN.executeWorkflow(workflow.getId(), parameters);
			assertThat("workflow invocation Id is not null", invocationId, notNullValue());
			assertThat("workflow invocation Id is not empty", isNotBlank(invocationId));
			/* uncomment for additional output */
			System.out.println(" >> Workflow invocation Id: " + invocationId);

			// test workflow completion
			boolean isCompleted = false;
			while (!isCompleted) {
				final WorkflowStatus status = ESCENTRAL_CONN.getStatus(invocationId);
				assertThat("workflow status is not null", status, notNullValue());
				/* uncomment for additional output */
				System.out.println(" >> Workflow status: " + status.toString());
				if (status.isCompleted()) {
					isCompleted = true;
				} else {
					Thread.sleep(2000l);
				}
			}

			// test retrieving products from e-SC
			ESCENTRAL_CONN.saveProducts(invocationId, TEST_OUTPUT_DIR);
			final Collection<File> outputFiles = listFiles(TEST_OUTPUT_DIR, null, true);
			assertThat("product files is not null", outputFiles, notNullValue());
			assertThat("product files is not empty", !outputFiles.isEmpty());
			/* uncomment for additional output */
			for (final File file : outputFiles) {
				System.out.println(" >> Product file: " + file.getCanonicalPath());
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ESCentralTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ESCentralTest.test() has finished");
		}
	}

}