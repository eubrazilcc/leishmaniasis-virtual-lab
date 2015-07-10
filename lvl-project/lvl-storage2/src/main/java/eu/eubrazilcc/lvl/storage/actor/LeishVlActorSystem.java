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

package eu.eubrazilcc.lvl.storage.actor;

import static akka.actor.ActorRef.noSender;
import static com.typesafe.config.ConfigRenderOptions.concise;
import static eu.eubrazilcc.lvl.storage.avro.messages.ImportDataCommand.MANAGER_RUN;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;
import static scala.concurrent.duration.Duration.Zero;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Manages the actor system within the LeishVL application.
 * @author Erik Torres <ertorser@upv.es>
 */
public class LeishVlActorSystem implements Closeable2 {

	private final static Logger LOGGER = getLogger(LeishVlActorSystem.class);

	public static final String ACTOR_SYSTEM_ID = "LeishVL";
	public static final String ACTOR_SYSTEM_NAME = ACTOR_SYSTEM_ID + " actor system";

	public static final String CONFIG_BASE = "leishvl";
	public static final String DISPATCHER = CONFIG_BASE + ".dispatcher";

	private final ActorSystem actorSystem;

	public LeishVlActorSystem(final @Nullable String confname) {
		// load configuration
		final Config config = loadConfig(confname);
		// create actor system
		actorSystem = ActorSystem.create(ACTOR_SYSTEM_ID, config);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(actorSystem.settings().config().root().render());
		} else {
			LOGGER.info("Configuration: " + actorSystem.settings().config().getObject(CONFIG_BASE).render(concise()));
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

	@Override
	public void setup(final Collection<URL> urls) {
		// nothing to do (so far)
	}

	@Override
	public void preload() {
		// periodically check external datasources for new data
		final ActorRef dataImportManager = actorSystem.actorOf(Props.create(DataImportManager.class)
				.withDispatcher(DISPATCHER), "data-import-mngr_" + randomAlphanumeric(6));
		actorSystem.scheduler().schedule(Zero(), Duration.create(24, HOURS), dataImportManager, MANAGER_RUN, actorSystem.dispatcher(), noSender());		
		LOGGER.info(ACTOR_SYSTEM_NAME + " started");
	}

	@Override
	public void close() throws IOException {
		LOGGER.info(ACTOR_SYSTEM_NAME + " terminating...");
		actorSystem.shutdown();
		LOGGER.info(ACTOR_SYSTEM_NAME + " terminated");
	}

}