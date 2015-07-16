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

package eu.eubrazilcc.lvl.storage.mongodb;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.storage.mongodb.client.MongoFileClient;
import eu.eubrazilcc.lvl.storage.mongodb.client.MongoTraceableObjectClient;

/**
 * Data connector based on mongoDB. Access to file collections is provided through the GridFS specification.
 * @author Erik Torres <ertorser@upv.es>
 * @see <a href="https://www.mongodb.org/">mongoDB</a>
 * @see <a href="http://docs.mongodb.org/manual/core/gridfs/">GridFS</a>
 */
public enum MongoConnector implements Closeable2 {

	MONGODB_CONN;

	private static final Logger LOGGER = getLogger(MongoConnector.class);

	private final MongoTraceableObjectClient traceableObjectClient = new MongoTraceableObjectClient();
	private final MongoFileClient fileClient = new MongoFileClient();

	public MongoTraceableObjectClient client() {
		return traceableObjectClient;
	}

	public MongoFileClient fsClient() {
		return fileClient;
	}

	@Override
	public void setup(final Collection<URL> urls) { }

	@Override
	public void preload() {
		LOGGER.info("mongoDB connector initialized successfully");
	}

	@Override
	public void close() throws IOException {
		try {
			traceableObjectClient.close();
		} catch (Exception ignore) { }
		try {
			fileClient.close();
		} catch (Exception ignore) { }
		LOGGER.info("mongoDB connector shutdown successfully");
	}

}