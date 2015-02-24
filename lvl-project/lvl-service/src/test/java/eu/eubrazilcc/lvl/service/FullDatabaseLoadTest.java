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
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.REST_SERVICE_CONFIG;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static eu.eubrazilcc.lvl.core.conf.LogManager.LOG_MANAGER;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.LEISHMANIA_QUERY;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.SANDFLY_QUERY;
import static eu.eubrazilcc.lvl.service.CloserService.CLOSER_SERVICE;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.LEISHMANIA_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.service.io.ImportPublicationsTask;
import eu.eubrazilcc.lvl.service.io.ImportSequencesTask;
import eu.eubrazilcc.lvl.service.io.filter.NewSequenceFilter;
import eu.eubrazilcc.lvl.service.io.filter.SequenceIdFilter;

/**
 * A full database load in made from this class. <strong>Be sure that you really need to execute this test!</strong>
 * Since a full database load means that you will need to fetch more than 25,000 sequences from external databases, 
 * many bad things can happen when you run this test from your personal laptop: NCBI can ban your IP from accessing 
 * PubMed and GenBank, and Google can forbid you to access some services like Geocoding. Check the number of sequences
 * and publication references being imported to your local file system:<br>
 * <br>
 * $ watch "ls -l sequences/genbank/xml/ | wc -l ; ls -l papers/pubmed/xml/ | wc -l"<br>
 * <br>
 * October 2014:<br>
 * <ul>
 * <li>33,921 sandfly sequences imported from GenBank.</li>
 * <li>60,031 leishmania sequences imported from GenBank.</li>
 * <li>65 publication references imported from PubMed.</li>
 * @author Erik Torres <ertorser@upv.es>
 */
public class FullDatabaseLoadTest {

	@Before
	public void setUp() throws Exception {
		// load test configuration
		final ImmutableList.Builder<URL> builder = new ImmutableList.Builder<URL>();
		final ImmutableList<URL> defaultUrls = getDefaultConfiguration();
		for (final URL url : defaultUrls) {
			if (!url.toString().endsWith(REST_SERVICE_CONFIG)) {
				builder.add(url);
			} else {
				builder.add(this.getClass().getResource("/config/lvl-service.xml"));
			}
		}
		// setup environment
		CONFIG_MANAGER.setup(builder.build());
		CONFIG_MANAGER.preload();
		LOG_MANAGER.preload();
		CLOSER_SERVICE.preload();
	}

	@After
	public void cleanUp() {
		CLOSER_SERVICE.close();
	}

	@Ignore
	@Test
	public void test() {
		System.out.println("FullDatabaseLoadTest.test()");
		try {
			// import Sandfly sequences
			List<String> seqIds = newArrayList("353470160", "219663935"); // null;
			final ImportSequencesTask<Sandfly> sandflyTask = ImportSequencesTask.sandflyBuilder()
					.query(SANDFLY_QUERY)
					.builder(Sandfly.builder())
					.dao(SANDFLY_DAO)
					.filter(seqIds == null || seqIds.isEmpty() ? NewSequenceFilter.sandflyBuilder().dao(SANDFLY_DAO).build() : SequenceIdFilter.builder().ids(seqIds).build())
					.build();
			TASK_RUNNER.execute(sandflyTask);
			TASK_STORAGE.add(sandflyTask);

			while (!sandflyTask.isDone()) {
				System.out.println(" >> Sandfly progress: " + sandflyTask.toString());
				Thread.sleep(2000l);
			}

			System.out.println(" >> Import Sandfly sequences final: " + sandflyTask.toString());

			// import publications
			ImportPublicationsTask pubTask = (ImportPublicationsTask) TASK_STORAGE.get(sandflyTask.getImportPublicationsTaskId());
			assertThat("import Sandfly publications task is not null", pubTask, notNullValue());

			while (!pubTask.isDone()) {
				System.out.println(" >> Sandfly publication progress: " + pubTask.toString());
				Thread.sleep(2000l);
			}

			System.out.println(" >> Import Sandfly publications final: " + pubTask.toString());

			// import Leishmania sequences
			seqIds = newArrayList("384562879", "77864608"); // null;
			final ImportSequencesTask<Leishmania> leishmaniaTask = ImportSequencesTask.leishmaniaBuilder()
					.query(LEISHMANIA_QUERY)
					.builder(Leishmania.builder())
					.dao(LEISHMANIA_DAO)
					.filter(seqIds == null || seqIds.isEmpty() ? NewSequenceFilter.leishmaniaBuilder().dao(LEISHMANIA_DAO).build() : SequenceIdFilter.builder().ids(seqIds).build())
					.build();
			TASK_RUNNER.execute(leishmaniaTask);
			TASK_STORAGE.add(leishmaniaTask);

			while (!leishmaniaTask.isDone()) {
				System.out.println(" >> Leishmania progress: " + leishmaniaTask.toString());
				Thread.sleep(2000l);
			}

			System.out.println(" >> Import Leishmania sequences final: " + leishmaniaTask.toString());

			// import publications
			pubTask = (ImportPublicationsTask) TASK_STORAGE.get(leishmaniaTask.getImportPublicationsTaskId());
			assertThat("import Leishmania publications task is not null", pubTask, notNullValue());

			while (!pubTask.isDone()) {
				System.out.println(" >> Leishmania publication progress: " + pubTask.toString());
				Thread.sleep(2000l);
			}

			System.out.println(" >> Import Leishmania publications final: " + pubTask.toString());

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("FullDatabaseLoadTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("FullDatabaseLoadTest.test() has finished");
		}
	}

}