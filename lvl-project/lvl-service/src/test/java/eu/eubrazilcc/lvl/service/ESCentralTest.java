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

package eu.eubrazilcc.lvl.service;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationFinder.DEFAULT_LOCATION;
import static eu.eubrazilcc.lvl.core.util.CollectionUtils.collectionToString;
import static eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector.ESCENTRAL_CONN;
import static eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.TEST_CONFIG_DIR;
import static java.io.File.separator;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.RandomStringUtils.random;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.eubrazilcc.lvl.core.workflow.WfOption;
import eu.eubrazilcc.lvl.core.workflow.WorkflowDefinition;
import eu.eubrazilcc.lvl.core.workflow.WorkflowParameters;
import eu.eubrazilcc.lvl.core.workflow.WorkflowStatus;
import eu.eubrazilcc.lvl.service.workflow.esc.ESCentralConnector;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.ConditionalIgnore;
import eu.eubrazilcc.lvl.test.ConditionalIgnoreRule.IgnoreCondition;

/**
 * Tests {@link ESCentralConnector}. <strong>Be sure that you really need to execute this test!</strong>
 * @author Erik Torres <ertorser@upv.es>
 */
public class ESCentralTest {

	private static final File TEST_OUTPUT_DIR = new File(concat(getProperty("java.io.tmpdir"),
			ESCentralTest.class.getSimpleName() + "_" + random(8, true, true)));

	private static final List<String> ESC_DOCUMENTS = newArrayList();

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	@Rule
	public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

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
	@ConditionalIgnore(condition=EnableESCentralTestIsFound.class)
	public void test() {
		System.out.println("ESCentralTest.test()");
		try {
			// list panel sequence files
			final String panelFolderId = "4568";
			final Map<String, String> panels = ESCENTRAL_CONN.listFiles(panelFolderId);
			assertThat("panel sequences list is not null", panels, notNullValue());
			assertThat("panel sequences list is not empty", !panels.isEmpty(), equalTo(true));			
			System.out.println(" >> Available panel sequences: " + panels);			

			// test listing available workflows from e-SC
			final List<WorkflowDefinition> workflows = ESCENTRAL_CONN.listWorkflows();
			assertThat("workflows list is not null", workflows, notNullValue());
			assertThat("workflows list is not empty", !workflows.isEmpty(), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Available workflows: " + collectionToString(workflows));			

			String workflowId = "workflows-eubcc-nj_pipeline-1.0";
			String versionId = "3193"; // null for last version

			// test get workflow definition from e-SC
			final WorkflowDefinition workflow = ESCENTRAL_CONN.getWorkflow(workflowId);
			assertThat("workflow definition is not null", workflow, notNullValue());
			/* uncomment for additional output */
			System.out.println(" >> Workflow definition: " + workflow.toString());

			// test get workflow parameters from e-SC
			final WorkflowParameters parameters = ESCENTRAL_CONN.getParameters(workflowId, versionId, 
					asList(WfOption.builder().name("ReferenceData-FileId").folderId(panelFolderId).build()));
			assertThat("workflow parameters are not null", parameters, notNullValue());
			assertThat("workflow parameters list is not null", parameters.getParameters(), notNullValue());
			assertThat("workflow parameters list is not empty", !parameters.getParameters().isEmpty(), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Workflow parameters: " + parameters.toString());			

			final WorkflowParameters parameters2 = WorkflowParameters.builder()
					.parameter("SequenceURL", "http://lvl.i3m.upv.es/lvl-service/rest/v1/datasets/objects/root%40lvl/hsp70.fasta/download", null, null)
					.parameter("HTTPGet-RequestHeaders", "Authorization: Bearer " + new EnableESCentralTestIsFound().getAuthToken(), null, null)
					// this parameter must be a e-SC document
					// TODO .parameter("ReferenceData-FileId", "hsp70_LVL.fasta")
					.parameter("Align", "1", null, null)
					.parameter("No. of Bootstrap Replications", "20", null, null)
					.build();

			// test JSON serialization
			final String payload = JSON_MAPPER.writeValueAsString(parameters2);
			assertThat("serialized workflow parameters is not null", payload, notNullValue());
			assertThat("serialized workflow parameters is not empty", isNotBlank(payload), equalTo(true));
			/* uncomment for additional output */
			System.out.println(" >> Serialized workflow parameters (JSON): " + payload);

			// test JSON deserialization
			final WorkflowParameters parameters3 = JSON_MAPPER.readValue(payload, WorkflowParameters.class);
			assertThat("deserialized workflow parameters is not null", parameters3, notNullValue());
			assertThat("deserialized workflow parameters coincides with expected", parameters3, equalTo(parameters2));

			// test submitting a workflow to e-SC
			String invocationId = ESCENTRAL_CONN.executeWorkflow(workflowId, versionId, parameters2);
			assertThat("workflow invocation Id is not null", invocationId, notNullValue());
			assertThat("workflow invocation Id is not empty", isNotBlank(invocationId));
			/* uncomment for additional output */
			System.out.println(" >> Workflow invocation Id: " + invocationId);

			// test workflow completion
			boolean isCompleted = false;
			WorkflowStatus status = null;
			while (!isCompleted) {
				status = ESCENTRAL_CONN.getStatus(invocationId);
				assertThat("workflow status is not null", status, notNullValue());
				/* uncomment for additional output */
				System.out.println(" >> Workflow status: " + status.toString());
				if (status.isCompleted()) {
					isCompleted = true;
				} else {
					Thread.sleep(2000l);
				}
			}
			assertThat("workflow status is not null", status, notNullValue());
			assertThat("workflow execution is successful", !status.isFailed(), equalTo(true));

			// test retrieving products from e-SC
			TEST_OUTPUT_DIR.mkdirs(); // force directory creation (maybe deleted by previous tests?)
			ESCENTRAL_CONN.saveProducts(invocationId, TEST_OUTPUT_DIR);
			final Collection<File> outputFiles = listFiles(TEST_OUTPUT_DIR, null, true);
			assertThat("product files is not null", outputFiles, notNullValue());
			assertThat("product files is not empty", !outputFiles.isEmpty(), equalTo(true));
			/* uncomment for additional output */
			for (final File file : outputFiles) {
				System.out.println(" >> Product file: " + file.getCanonicalPath());
			}

			// test submitting a workflow to e-SC (with wrong parameters)
			final WorkflowParameters badParameters = WorkflowParameters.builder()
					.parameter("SequenceURL", "http://lvl.i3m.upv.es/lvl-service/rest/v1/datasets/objects/root%40lvl/hsp70.fastaBAD/download", null, null)
					.parameter("HTTPGet-RequestHeaders", "Authorization: Bearer " + new EnableESCentralTestIsFound().getAuthToken(), null, null)
					.parameter("Align", "1", null, null)
					.parameter("No. of Bootstrap Replications", "20", null, null)
					.build();

			invocationId = ESCENTRAL_CONN.executeWorkflow(workflowId, versionId, badParameters);
			assertThat("workflow (with wrong params) invocation Id is not null", invocationId, notNullValue());
			assertThat("workflow (with wrong params) invocation Id is not empty", isNotBlank(invocationId));
			/* uncomment for additional output */
			System.out.println(" >> Workflow invocation Id: " + invocationId);

			// test workflow completion
			isCompleted = false;
			status = null;
			while (!isCompleted) {
				status = ESCENTRAL_CONN.getStatus(invocationId);
				assertThat("workflow (with wrong params) status is not null", status, notNullValue());
				/* uncomment for additional output */
				System.out.println(" >> Workflow (with wrong params) status: " + status.toString());
				if (status.isCompleted()) {
					isCompleted = true;
				} else {
					Thread.sleep(2000l);
				}
			}
			assertThat("workflow (with wrong params) status is not null", status, notNullValue());
			assertThat("workflow (with wrong params) execution is failed", !status.isFailed(), equalTo(false));
			assertThat("workflow (with wrong params) status message", isNotBlank(status.getDescription()), equalTo(true));

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ESCentralTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ESCentralTest.test() has finished");
		}
	}

	/**
	 * Checks whether a flag file is available in the local filesystem.
	 * @author Erik Torres <ertorser@upv.es>
	 */
	public class EnableESCentralTestIsFound implements IgnoreCondition {
		private final File file = new File(concat(DEFAULT_LOCATION, TEST_CONFIG_DIR + separator + "e-sc.test"));
		@Override
		public boolean isSatisfied() {			
			return !file.canRead();
		}
		public String getAuthToken() throws IOException {
			return trimToEmpty(readFileToString(file)).replaceAll("(?m)^\\s", "");
		}
	}

}