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

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * Executes external commands from within the JVM where this application is running.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class CommandExecutor {

	/**
	 * Executes an external command returning the command's output to the caller and optionally logging errors.
	 * @param cmd - external command to be executed
	 * @param timeout - timeout in seconds
	 * @return The output of the executed command.
	 * @throws IOException - when an I/O error occurs
	 * @throws InterruptedException - when the process execution is interrupted
	 */
	public static String execCommand(final String cmd, final int timeout) throws IOException, InterruptedException {
		return execCommand(cmd, 0, timeout, true, Level.ERROR);
	}

	/**
	 * Executes an external command returning the command's output to the caller and optionally logging errors.
	 * @param cmd - external command to be executed
	 * @param expectedExit - expected exit values, if the process exits with a value different than this the execution will be considered failed
	 * @param timeout - timeout in seconds
	 * @param logErrors - setting to {@code true} will send the errors to the logging system
	 * @param level - log level
	 * @return The output of the executed command.
	 * @throws IOException - when an I/O error occurs
	 * @throws InterruptedException - when the process execution is interrupted
	 */
	public static String execCommand(final String cmd, final int expectedExit, final int timeout, final boolean logErrors, final Level level) 
			throws IOException, InterruptedException {
		final CommandLine cmdLine = CommandLine.parse(cmd);
		final DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue(expectedExit);
		final ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout * 1000l);
		executor.setWatchdog(watchdog);
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		final OutputStream errorStream = logErrors ? new ExecLogHandler(level) : NULL_OUTPUT_STREAM;
		final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
		executor.setStreamHandler(streamHandler);
		executor.execute(cmdLine);
		return outputStream.toString();
	}

	/**
	 * Logging levels.
	 * @author Erik Torres <etserrano@gmail.com>
	 */
	public static enum Level {
		INFO(1),
		WARN(2),
		ERROR(3);

		private final int value;

		private Level(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

}