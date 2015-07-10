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

import static akka.dispatch.Futures.sequence;
import static akka.event.Logging.getLogger;
import static akka.pattern.Patterns.ask;
import static akka.pattern.Patterns.pipe;
import static com.google.common.collect.Lists.newArrayList;
import static eu.eubrazilcc.lvl.storage.actor.LeishVlActorSystem.DISPATCHER;
import static eu.eubrazilcc.lvl.storage.avro.messages.ImportDataCommand.IMPORTER_RUN;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.util.Iterator;
import java.util.List;

import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.dispatch.Mapper;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import akka.util.Timeout;
import eu.eubrazilcc.lvl.storage.avro.messages.FetchConfig;
import eu.eubrazilcc.lvl.storage.avro.messages.ImportedData;

/**
 * Imports DNA sequences from NCBI GenBank database.
 * @author Erik Torres <ertorser@upv.es>
 */
@DataImporter(dataSource=GenbankDataImporter.GENBANK_DATA_SOURCE, 
displayName="GenBank sequence database", url="http://www.ncbi.nlm.nih.gov/genbank/")
public class GenbankDataImporter extends UntypedActor {

	public static final String GENBANK_DATA_SOURCE = "genbank";

	protected final LoggingAdapter logger = getLogger(getContext().system(), this);

	private final Timeout timeout = new Timeout(Duration.create(10, MINUTES));
	private Router router = new Router(new RoundRobinRoutingLogic());

	@Override
	public void onReceive(final Object message) throws Exception {
		if (message == IMPORTER_RUN) {

			// TODO
			System.err.println("\n\nIMPORTING FROM: " + GENBANK_DATA_SOURCE + "\n");
			// TODO

			// TODO : DataImportException

			logger.info("Importing new data from: " + GENBANK_DATA_SOURCE);
			final List<Future<Object>> futures = newArrayList();
			final List<Routee> routes = newArrayList();
			for (int i = 0; i < 3; i++) {
				final ActorRef worker = getContext().actorOf(Props.create(GenbankWorker.class).withDispatcher(DISPATCHER), 
						"genbank-worker_" + randomAlphanumeric(6));
				getContext().watch(worker);
				routes.add(new ActorRefRoutee(worker));
				futures.add(ask(worker, FetchConfig.newBuilder().setOutputDir("outputDir").build(), timeout));
			}
			final ExecutionContext execCtxt = getContext().system().dispatchers().lookup(DISPATCHER);
			final Future<Iterable<Object>> aggregate = sequence(futures, execCtxt);
			final Future<ImportedData> result = aggregate.map(new Mapper<Iterable<Object>, ImportedData>() {
				@Override
				public ImportedData apply(final Iterable<Object> iterable) {
					int count = 0;
					final Iterator<Object> it = iterable.iterator();
					while (it.hasNext()) {
						count += (Integer)it.next();
					}					
					return ImportedData.newBuilder()
							.setDatasource(GENBANK_DATA_SOURCE)
							.setCount(count)
							.build();
				}
			}, execCtxt);
			pipe(result, execCtxt).to(getSender());			
		} else if (message instanceof Terminated) {
			router = router.removeRoutee(((Terminated) message).actor());
		} else unhandled(message);
	}

}