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
import static eu.eubrazilcc.lvl.storage.support.dao.IssueDAO.ISSUE_DAO;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.mutable.MutableLong;
import org.junit.Test;

import eu.eubrazilcc.lvl.core.support.Issue;
import eu.eubrazilcc.lvl.storage.dao.WriteResult;

/**
 * Tests {@link Issue} collection in the database.
 * @author Erik Torres <ertorser@upv.es>
 */
public class IssueCollectionTest {

	@Test
	public void test() {
		System.out.println("IssueCollectionTest.test()");
		try {
			// insert
			final Issue issue = Issue.builder()
					.newId()
					.email("username@example.com")
					.browser("Google Chrome 42")
					.system("Ubuntu 14.04")
					.description("Problem description")
					.opened(new SimpleDateFormat("yyyy-mm-dd").parse("2014-01-01"))
					.build();
			WriteResult<Issue> writeResult = ISSUE_DAO.insert(issue);
			assertThat("insert write result is not null", writeResult, notNullValue());
			assertThat("insert write result Id is not null", writeResult.getId(), notNullValue());
			assertThat("insert write result Id is not empty", isNotBlank(writeResult.getId()), equalTo(true));

			// insert ignoring duplicates			
			try {
				ISSUE_DAO.insert(issue, true);
				fail("Expected Exception due to duplicate insertion");
			} catch (RuntimeException expected) {
				System.out.println("Expected exception caught: " + expected.getClass());
			}

			// find
			Issue issue2 = ISSUE_DAO.find(issue.getId());
			assertThat("issue is not null", issue2, notNullValue());
			assertThat("issue coincides with original", issue2, equalTo(issue));
			System.out.println(issue2.toString());

			// update
			issue.setClosed(new SimpleDateFormat("yyyy-mm-dd").parse("2015-02-02"));
			issue.getFollowUp().put(new SimpleDateFormat("yyyy-mm-dd").parse("2014-01-07").getTime(), "Something new");
			ISSUE_DAO.update(issue);

			// find after update
			issue2 = ISSUE_DAO.find(issue.getId());
			assertThat("issue is not null", issue2, notNullValue());
			assertThat("issue coincides with original", issue2, equalTo(issue));
			System.out.println(issue2.toString());

			// remove
			ISSUE_DAO.delete(issue.getId());
			final long numRecords = ISSUE_DAO.count();
			assertThat("number of issues stored in the database coincides with expected", numRecords, equalTo(0l));

			// pagination
			final List<String> ids = newArrayList();
			for (int i = 0; i < 11; i++) {				
				final Issue issue3 = Issue.builder()
						.id("issue-" + i)
						.email("username" + i + "@example.com")
						.browser("Browswer")
						.system("System")
						.description("Description")
						.opened(new Date())						
						.build();
				ids.add(issue3.getId());
				ISSUE_DAO.insert(issue3);
			}
			final int size = 3;
			int start = 0;
			List<Issue> issues = null;
			final MutableLong count = new MutableLong(0l);
			do {
				issues = ISSUE_DAO.list(start, size, null, null, null, count);
				if (issues.size() != 0) {
					System.out.println("Paging: first item " + start + ", showing " + issues.size() + " of " + count.getValue() + " items");
				}
				start += issues.size();
			} while (!issues.isEmpty());
			for (final String id2 : ids) {			
				ISSUE_DAO.delete(id2);
			}
			ISSUE_DAO.stats(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("IssueCollectionTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("IssueCollectionTest.test() has finished");
		}
	}

}