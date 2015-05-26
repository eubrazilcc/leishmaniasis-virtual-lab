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

package eu.eubrazilcc.lvl.service.actor;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import static akka.event.Logging.getLogger;
import static com.google.common.collect.Maps.newHashMap;
import static eu.eubrazilcc.lvl.service.actor.LeishVlActorSystem.CONFIG_BASE;
import static eu.eubrazilcc.lvl.service.actor.LeishVlActorSystem.DISPATCHER;
import static eu.eubrazilcc.lvl.service.avro.messages.ImportDataCommand.IMPORTER_RUN;
import static eu.eubrazilcc.lvl.service.avro.messages.ImportDataCommand.MANAGER_RUN;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import scala.concurrent.duration.Duration;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import akka.japi.Function;

import com.google.common.reflect.ClassPath;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;

import eu.eubrazilcc.lvl.service.avro.messages.ImportedData;

/**
 * Data import manager.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DataImportManager extends UntypedActor {

	protected final LoggingAdapter logger = getLogger(getContext().system(), this);

	private final SupervisorStrategy supervisionStrategy = new OneForOneStrategy(30, Duration.create(1, MINUTES), new Function<Throwable, Directive>() {
		@Override
		public Directive apply(final Throwable t) {
			if (t instanceof DataImportException) {
				return restart();
			} else if (t instanceof Exception) {
				return stop();
			} else {
				return escalate();
			}
		}
	});

	private final Map<String, Class<?>> dataImporters;

	public DataImportManager() {
		Map<String, Class<?>> map = null;
		try {
			map = discoverDataImportProviders();
		} catch (Exception e) {
			logger.warning("Some data importers can't be loaded", e);
		} finally {
			dataImporters = map != null ? unmodifiableMap(map) : Collections.<String, Class<?>>emptyMap();
		}
	}

	/**
	 * Inspects the package where this class is stored and discovers any class annotated with the
	 * {@link DataImporter} annotation, which is used to tag data importers.
	 * @return A map where the target data source is the key and the data importer class is the value.
	 * @throws IOException Thrown when an error occurs during the inspection of the package.
	 */
	private Map<String, Class<?>> discoverDataImportProviders() throws IOException {
		final Map<String, Class<?>> map = newHashMap();
		final ClassPath classpath = ClassPath.from(Thread.currentThread().getContextClassLoader());
		for (final ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive(this.getClass().getPackage().getName())) {
			final Class<?> clazz = classInfo.load();
			if (clazz.isAnnotationPresent(DataImporter.class) && UntypedActor.class.isAssignableFrom(clazz)) {
				map.put(clazz.getAnnotation(DataImporter.class).dataSource(), clazz);
			}
		}
		return map;
	}

	@Override
	public void onReceive(final Object message) throws Exception {
		if (message == MANAGER_RUN) {
			final ConfigList dataSources = getContext().system().settings().config().getList(CONFIG_BASE + ".service.datasources");
			if (dataSources != null) {
				for (final ConfigValue dataSource : dataSources) {
					if (ConfigValueType.OBJECT == dataSource.valueType()) {
						@SuppressWarnings("unchecked")
						final Map<String, Object> obj = (Map<String, Object>)dataSource.unwrapped();
						final Object objId = obj.get("id");
						Class<?> importer = null;
						if (objId != null && objId instanceof String && (importer = dataImporters.get((String)objId)) != null) {
							getContext().actorOf(Props.create(importer).withDispatcher(DISPATCHER), 
									"data-importer_" + randomAlphanumeric(6)).tell(IMPORTER_RUN, getSelf());
						}
					}
				}								
			}
		} else if (message instanceof ImportedData) {
			final ImportedData importedData = (ImportedData)message;
			logger.info(importedData.getCount() + " new records imported from " + importedData.getDatasource());
		} else unhandled(message);
	}

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return supervisionStrategy;
	}

}