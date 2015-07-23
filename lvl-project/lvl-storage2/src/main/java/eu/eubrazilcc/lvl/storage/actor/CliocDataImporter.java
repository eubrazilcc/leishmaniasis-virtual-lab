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

import static akka.event.Logging.getLogger;
import static eu.eubrazilcc.lvl.storage.avro.messages.ImportDataCommand.IMPORTER_RUN;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import eu.eubrazilcc.lvl.storage.avro.messages.ImportedData;

/**
 * Imports data from Fiocruz CLIOC database.
 * @author Erik Torres <ertorser@upv.es>
 */
@DataImporter(dataSource=CliocDataImporter.CLIOC_DATA_SOURCE, 
displayName="CLIOC collection of leishmania", url="http://clioc.fiocruz.br/")
public class CliocDataImporter extends UntypedActor {

	public static final String CLIOC_DATA_SOURCE = "clioc";

	protected final LoggingAdapter logger = getLogger(getContext().system(), this);

	@Override
	public void onReceive(final Object message) throws Exception {
		if (message == IMPORTER_RUN) {
			logger.info("Importing new data from: " + CLIOC_DATA_SOURCE);

			// TODO
			System.err.println("\n\nIMPORTING FROM: " + CLIOC_DATA_SOURCE + "\n");
			// TODO

			// TODO : DataImportException

			final ImportedData result = ImportedData.newBuilder()
					.setDatasource(CLIOC_DATA_SOURCE)
					.setCount(3) // TODO
					.build();
			getSender().tell(result, getSelf());
		} else unhandled(message);
	}

}