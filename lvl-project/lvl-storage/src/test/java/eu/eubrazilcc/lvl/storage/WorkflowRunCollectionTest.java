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
import static eu.eubrazilcc.lvl.storage.dao.WorkflowRunDAO.WORKFLOW_RUN_DAO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.workflow.WorkflowParameters;
import eu.eubrazilcc.lvl.core.workflow.WorkflowRun;

/**
 * Tests workflow runs collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class WorkflowRunCollectionTest {

	@Test
	public void test() {
		System.out.println("WorkflowRunCollectionTest.test()");
		try {
			// insert
			final WorkflowRun run = WorkflowRun.builder()
					.id("ABCD123")
					.workflowId("980TYHN1")
					.invocationId("1209")
					.parameters(WorkflowParameters.builder().parameter("var1", "1", null, null).parameter("var2", "2", null, null).build())
					.submitter("submitter")
					.submitted(new Date())
					.build();
			WORKFLOW_RUN_DAO.insert(run);

			// find
			WorkflowRun run2 = WORKFLOW_RUN_DAO.find(run.getId());
			assertThat("workflow run is not null", run2, notNullValue());
			assertThat("workflow run coincides with original", run2, equalTo(run));
			System.out.println(run2.toString());

			// update
			run.setSubmitter("submitter2");
			WORKFLOW_RUN_DAO.update(run);

			// find after update
			run2 = WORKFLOW_RUN_DAO.find(run.getId());
			assertThat("workflow run is not null", run2, notNullValue());
			assertThat("workflow run coincides with original", run2, equalTo(run));
			System.out.println(run2.toString());

			// list all by submitter
			List<WorkflowRun> runs = WORKFLOW_RUN_DAO.findAll(run.getSubmitter());
			assertThat("workflow runs are not null", runs, notNullValue());
			assertThat("workflow runs are not empty", !runs.isEmpty(), equalTo(true));
			assertThat("number of workflow runs coincides with expected", runs.size(), equalTo(1));
			assertThat("workflow runs coincide with original", runs.get(0), equalTo(run));

			// remove
			WORKFLOW_RUN_DAO.delete(run.getId());
			final long numRecords = WORKFLOW_RUN_DAO.count();
			assertThat("number of workflow runs stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {
				final WorkflowRun run3 = WorkflowRun.builder()
						.id(Integer.toString(i)).build();
				ids.add(run3.getId());
				WORKFLOW_RUN_DAO.insert(run3);
			}
			final int size = 3;
			int start = 0;
			runs = null;
			final MutableLong count = new MutableLong(0l);
			do {
				runs = WORKFLOW_RUN_DAO.list(start, size, null, null, null, count);
				if (runs.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + runs.size() + " of " + count.getValue() + " items");
				}
				start += runs.size();
			} while (!runs.isEmpty());
			for (final String id2 : ids) {			
				WORKFLOW_RUN_DAO.delete(id2);
			}
			WORKFLOW_RUN_DAO.stats(System.out);

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("WorkflowRunCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("WorkflowRunCollectionTest.test() has finished");
		}
	}

}