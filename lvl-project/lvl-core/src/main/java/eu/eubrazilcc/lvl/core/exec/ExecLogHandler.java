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

package eu.eubrazilcc.lvl.core.exec;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.exec.CommandExecutor.Level;

/**
 * Writes command execution errors to the logging system.
 * @author Erik Torres <ertorser@upv.es>
 */
public class ExecLogHandler extends LogOutputStream {

	private final static Logger LOGGER = getLogger(ExecLogHandler.class);
	
	private final Level level;
	
	public ExecLogHandler(final Level level) {
		this.level = level;
	}
	
	@Override
	protected void processLine(final String line, final int level) {
		switch (this.level) {
		case INFO:
			LOGGER.info(line);
			break;
		case WARN:
			LOGGER.warn(line);
			break;
		case ERROR:
			LOGGER.error(line);
			break;
		default:
			break;
		}		
	}

}