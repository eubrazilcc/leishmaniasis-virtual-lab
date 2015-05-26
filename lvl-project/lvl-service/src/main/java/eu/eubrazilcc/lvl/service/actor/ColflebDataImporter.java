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

import static akka.event.Logging.getLogger;
import static eu.eubrazilcc.lvl.service.avro.messages.ImportDataCommand.IMPORTER_RUN;
import akka.actor.UntypedActor;
import akka.event.LoggingAdapter;
import eu.eubrazilcc.lvl.service.avro.messages.ImportedData;

/**
 * Imports data from Fiocruz COLFLEB database.
 * @author Erik Torres <ertorser@upv.es>
 */
@DataImporter(dataSource=ColflebDataImporter.COLFLEB_DATA_SOURCE, 
displayName="COLFLEB collection of phlebotomine", url="http://colfleb.fiocruz.br/")
public class ColflebDataImporter extends UntypedActor {

	public static final String COLFLEB_DATA_SOURCE = "colfleb";

	protected final LoggingAdapter logger = getLogger(getContext().system(), this);

	@Override
	public void onReceive(final Object message) throws Exception {
		if (message == IMPORTER_RUN) {
			logger.info("Importing new data from: " + COLFLEB_DATA_SOURCE);

			// TODO
			System.err.println("\n\nIMPORTING FROM: " + COLFLEB_DATA_SOURCE + "\n");
			// TODO

			// TODO : DataImportException

			final ImportedData result = ImportedData.newBuilder()
					.setDatasource(COLFLEB_DATA_SOURCE)
					.setCount(3) // TODO
					.build();
			getSender().tell(result, getSelf());
		} else unhandled(message);
	}

}