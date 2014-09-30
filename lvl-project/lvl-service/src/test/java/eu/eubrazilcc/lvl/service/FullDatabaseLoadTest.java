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
import static eu.eubrazilcc.lvl.service.CloserService.CLOSER_SERVICE;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

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
 * September 2014: 25,828 sequences imported from GenBank and 65 publication references imported from PubMed.
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

	@Test
	public void test() {
		System.out.println("FullDatabaseLoadTest.test()");
		try {
			// import sequences
			final List<String> seqIds = null; newArrayList("353470160");
			final ImportSequencesTask seqTask = ImportSequencesTask.builder()
					.filter(seqIds == null || seqIds.isEmpty() ? NewSequenceFilter.builder().build() : SequenceIdFilter.builder().ids(seqIds).build())
					.build();
			TASK_RUNNER.execute(seqTask);
			TASK_STORAGE.add(seqTask);

			while (!seqTask.isDone()) {
				System.out.println(" >> Progress: " + seqTask.toString());
				Thread.sleep(2000l);
			}

			System.out.println(" >> Import sequences final: " + seqTask.toString());

			// import publications
			final ImportPublicationsTask pubTask = (ImportPublicationsTask) TASK_STORAGE.get(seqTask.getImportPublicationsTaskId());

			while (!pubTask.isDone()) {
				System.out.println(" >> Progress: " + pubTask.toString());
				Thread.sleep(2000l);
			}

			System.out.println(" >> Import publications final: " + pubTask.toString());

		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("FullDatabaseLoadTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("FullDatabaseLoadTest.test() has finished");
		}
	}

}