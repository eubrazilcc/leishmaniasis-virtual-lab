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

package eu.eubrazilcc.lvl.core;

import static com.google.common.util.concurrent.MoreExecutors.sameThreadExecutor;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskScheduler.TASK_SCHEDULER;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableScheduledFuture;

public class ConcurrencyTest {

	private boolean isCompleted;
	
	@Before
	public void setUp() {
		isCompleted = false;
	}
	
	@After
	public void cleanUp() throws IOException {
		// don't close the task runner here since it will be used in other tests
		TASK_SCHEDULER.close();
	}
	
	@Test
	public void test() {
		System.out.println("ConcurrencyTest.test()");
		try {
			final String success = "OK";

			// test uninitialized task runner			
			try {
				TASK_RUNNER.submit(new Callable<String>() {
					@Override
					public String call() throws Exception {
						return success;
					}				
				});
				fail("Should have thrown an IllegalStateException because task runner is uninitialized");
			} catch (IllegalStateException e) {
				assertThat(e.getMessage(), containsString("Task runner uninitialized"));
			}

			// test run task
			TASK_RUNNER.preload();
			final ListenableFuture<String> future = TASK_RUNNER.submit(new Callable<String>() {
				@Override
				public String call() throws Exception {
					return success;
				}				
			});
			assertThat("task future is not null", future, notNullValue());
			final String result = future.get(20, TimeUnit.SECONDS);
			assertThat("task result is not null", result, notNullValue());			
			assertThat("task result is not empty", isNotBlank(result));
			assertThat("task result coincides with expected", result, equalTo(success));

			// test uninitialized task scheduler
			try {
				TASK_SCHEDULER.scheduleAtFixedRate(new Runnable() {					
					@Override
					public void run() {
						System.out.println(" >> Scheluded job runs");
					}
				}, 0, 20, TimeUnit.SECONDS);
				fail("Should have thrown an IllegalStateException because task runner is uninitialized");
			} catch (IllegalStateException e) {
				assertThat(e.getMessage(), containsString("Task scheduler uninitialized"));
			}

			// test schedule task
			TASK_SCHEDULER.preload();
			final ListenableScheduledFuture<?> future2 = TASK_SCHEDULER.scheduleAtFixedRate(new Runnable() {					
				@Override
				public void run() {
					System.out.println(" >> Scheluded job runs");
				}
			}, 0, 20, TimeUnit.SECONDS);
			assertThat("scheduled task future is not null", future2, notNullValue());			
			future2.addListener(new Runnable() {				
				@Override
				public void run() {
					isCompleted = true;
				}
			}, sameThreadExecutor());
			Thread.sleep(2000);
			assertThat("scheduled task result coincides with expected", isCompleted, equalTo(isCompleted));			
		} catch (Exception e) {
			e.printStackTrace(System.err);
			fail("ConcurrencyTest.test() failed: " + e.getMessage());
		} finally {			
			System.out.println("ConcurrencyTest.test() has finished");
		}
	}

}