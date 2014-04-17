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

package eu.eubrazilcc.lvl.core.conf;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.getDefaultConfiguration;
import static java.lang.System.getenv;
import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/**
 * Utility class that searches for configuration files in the different locations where 
 * they can be provided to the application or service.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ConfigurationFinder {

	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationFinder.class);

	public static final String ENV_HOME_VAR = "LVL_HOME";
	public static final String DEFAULT_LOCATION = "/opt/lvl";
	public static final String[] CONFIGURATION_FILENAMES = { ConfigurationManager.MAIN_CONFIG,
		ConfigurationManager.AUTHZ_SERVER_CONFIG, ConfigurationManager.REST_SERVICE_CONFIG };

	/**
	 * Finds available configuration files, searching the different sources in the following order:
	 * <ol>
	 *   <li>Application environment;</li>
	 *   <li>Default location in the file-system locally available to the application; and finally</li>
	 *   <li>If none of the above works, configuration files are returned from the resources available 
	 *       in the application class path.</li>
	 * </ol>
	 * @return configuration files available to the application
	 */
	public final static ImmutableList<URL> findConfigurationFiles() {
		ImmutableList<File> configFiles = null;
		// try to read environment variable and retrieve configuration files from the file system
		try {
			final String location = getenv(ENV_HOME_VAR) + "/etc";
			if (isNotBlank(location) && testConfigurationFiles(location, CONFIGURATION_FILENAMES)) {
				configFiles = convertPathListToFiles(location, CONFIGURATION_FILENAMES);				
				LOGGER.trace("Environment variable '" + ENV_HOME_VAR + "' found. Configuration files: " + filesToString(configFiles));
			} else {
				LOGGER.trace("Environment variable '" + ENV_HOME_VAR + "' was not found");
			}
		} catch (Exception ignore) { }
		// try to retrieve configuration files from the default location
		if (configFiles == null) {
			try {
				final String location = DEFAULT_LOCATION + "/etc";
				if (isNotBlank(location) && testConfigurationFiles(location, CONFIGURATION_FILENAMES)) {
					configFiles = convertPathListToFiles(location, CONFIGURATION_FILENAMES);
					LOGGER.trace("Default location '" + DEFAULT_LOCATION + "' found. Configuration files: " + filesToString(configFiles));
				} else {
					LOGGER.trace("Default location '" + DEFAULT_LOCATION + "' was not found");
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to search for configuration files in default location: " + DEFAULT_LOCATION, e);
			}
		}
		// return default configuration files
		if (configFiles == null) {
			return getDefaultConfiguration();
		} else {
			return from(configFiles).transform(new Function<File, URL>() {
				@Override
				public URL apply(final File file) {
					URL url = null;
					if (file != null) {
						try {
							url = file.toURI().toURL();
						} catch (MalformedURLException e) {
							LOGGER.warn("Ignoring file: " + file.getName(), e);
						}
					}					
					return url;
				}
			}).filter(notNull()).toList();
		}
	}

	public final static boolean testConfigurationFiles(final String baseDir, final String[] filenames) {
		boolean passed = (isNotEmpty(baseDir) && filenames != null && filenames.length > 0 ? true : false);
		for (int i = 0; i < filenames.length && passed; i++) {
			try {
				final File file = new File(concat(baseDir, filenames[i]));
				if (!file.isFile() || !file.canRead()) {
					passed = false;
				}
			} catch (Exception ignore) {
				passed = false;
			}
		}
		return passed;
	}

	private static ImmutableList<File> convertPathListToFiles(final String baseDir, final String[] filenames) {
		final ImmutableList.Builder<File> builder = new ImmutableList.Builder<File>();
		for (final String filename : filenames) {
			builder.add(new File(concat(baseDir, filename)));
		}
		return builder.build();
	}

	private static String filesToString(final ImmutableList<File> files) {
		String str = "";
		if (files != null && files.size() > 0) {
			int i = 0;
			for (; i < (files.size() - 1); i++) {
				try {
					str += files.get(i).getCanonicalPath() + ", ";
				} catch (Exception ignore) { }
			}
			try {
				str += files.get(i).getCanonicalPath();
			} catch (Exception ignore) { }
		}
		return str;
	}

}