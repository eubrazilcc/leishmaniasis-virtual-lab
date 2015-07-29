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

package eu.eubrazilcc.lvl.microservices;

import static com.google.common.base.Preconditions.checkArgument;
import static com.typesafe.config.ConfigRenderOptions.concise;
import static eu.eubrazilcc.lvl.core.LogManager.LOG_MANAGER;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;

import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ServiceManager.Listener;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

/**
 * Tools for starting applications.
 * @author Erik Torres <ertorser@upv.es>
 */
public abstract class LvlDaemon implements Daemon {

	public static final String[] ARGS_CONFIG_OPT  = { "c", "configuration" };
	public static final String ARGS_DIR_PROP = "directory";

	protected final Logger logger;	

	protected Thread daemonThread;
	protected boolean stopped = false;

	protected Config config;
	protected ServiceManager serviceManager;

	public LvlDaemon(final Class<?> clazz) {
		LOG_MANAGER.init();
		logger = getLogger(clazz);
	}

	@Override
	public void init(final DaemonContext daemonContext) throws DaemonInitException, Exception {
		// start daemon thread
		daemonThread = new Thread() {	
			@Override
			public synchronized void start() {
				LvlDaemon.this.stopped = false;
				super.start();
			}
			@Override
			public void run() {             
				if (!stopped) {					
					super.run();					
				}
			}			
		};
	}

	@Override
	public void start() throws Exception {
		daemonThread.start();
		serviceManager.addListener(new Listener() {
			@Override
			public void healthy() {
				final double startupTime = serviceManager.startupTimes().entrySet().stream().mapToDouble(Map.Entry::getValue).sum();
				logger.info("Services started in: " + ((long)startupTime/1000l) + " seconds.");
			}
		});
		serviceManager.startAsync();
	}

	@Override
	public void stop() throws Exception {
		stopped = true;
		try {
			serviceManager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
			logger.info("Service manager was stopped.");
		} catch (TimeoutException timeout) {
			logger.info("Stopping timed out.");
		}
		try {
			daemonThread.join(1000l);
		} catch (InterruptedException e) {
			logger.warn("Stopping error.", e);
			throw e;
		}		
	}

	@Override
	public void destroy() {		
		daemonThread = null;
		serviceManager = null;
		try {
			logger.info("Closing log manager: all messages will be lost beyond this point.");
			LOG_MANAGER.close();
		} catch (IOException ignore) { }
	}

	public void awaitHealthy(final long timeout, final TimeUnit unit) throws TimeoutException {
		serviceManager.awaitHealthy(timeout, unit);
	}

	protected CommandLine parseParameters(final String[] args, final Options options) throws ParseException {
		final Option configOption = Option.builder(ARGS_CONFIG_OPT[0])
				.longOpt(ARGS_CONFIG_OPT[1])
				.hasArg()
				.argName(ARGS_DIR_PROP)
				.desc("load configuration from the specified directory")
				.build();
		options.addOption(configOption);

		final CommandLineParser parser = new DefaultParser();
		return parser.parse(options, args);
	}

	protected void loadConfig(final CommandLine cmd) {
		String confname = null;
		if (cmd.hasOption(ARGS_CONFIG_OPT[0])) {
			try {
				confname = cmd.getOptionValue(ARGS_CONFIG_OPT[0]);
				checkArgument(isNotBlank(confname), "Parameter " + ARGS_DIR_PROP + " is expected.");				
			} catch (Exception e) {
				logger.error("Configuration load failed.", e);
				System.exit(1);
			}
		}

		// load and merge application configuration with default properties
		config = loadConfig(confname);
		if (logger.isTraceEnabled()) {
			logger.trace(config.root().render());
		} else {
			logger.info("Configuration: " + config.getObject("leishvl").render(concise()));
		}
	}

	private Config loadConfig(final @Nullable String confname) {
		Config config = null;
		final String confname2 = trimToNull(confname);
		if (confname2 != null) {
			final ConfigParseOptions options = ConfigParseOptions.defaults().setAllowMissing(false);
			final Config customConfig = ConfigFactory.parseFileAnySyntax(new File(confname2), options);
			final Config regularConfig = ConfigFactory.load();
			final Config combined = customConfig.withFallback(regularConfig);
			config = ConfigFactory.load(combined);
		} else {
			config = ConfigFactory.load();
		}
		return config;
	}

}