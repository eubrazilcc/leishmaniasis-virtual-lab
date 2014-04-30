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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.contains;
import static com.google.common.base.Predicates.notNull;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.parseURL;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import eu.eubrazilcc.lvl.core.Closeable2;
import eu.eubrazilcc.lvl.core.entrez.EntrezHelper.Format;

/**
 * Manages configuration.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum ConfigurationManager implements Closeable2 {

	INSTANCE;

	private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

	public static final String MAIN_CONFIG         = "lvl.xml";
	public static final String REST_SERVICE_CONFIG = "lvl-service.xml";
	public static final String AUTHZ_SERVER_CONFIG = "lvl-auth.xml";

	public static final ImmutableList<String> IGNORE_LIST = new ImmutableList.Builder<String>()
			.add("logback.xml").build();

	public static final String LVL_NAME = "Leishmaniasis Virtual Laboratory (LVL)";

	private ConfigurationManager.Configuration dont_use = null;
	private Collection<URL> urls = ConfigurationManager.getDefaultConfiguration();	

	// public methods

	public File getRootDir() {
		return configuration().getRootDir();
	}

	public File getLocalCacheDir() throws IOException {
		final File localCacheDir = configuration().getLocalCacheDir();
		if (!localCacheDir.exists()) {
			checkState(localCacheDir.mkdirs(), "Cannot create local cache directory");
		} else {
			checkState(localCacheDir.isDirectory() && localCacheDir.canWrite(), 
					"Invalid local cache directory: " + localCacheDir.getCanonicalPath());
		}
		return localCacheDir;
	}

	public File getHtdocsDir() {
		return configuration().getHtdocsDir();
	}

	public String getDbName() {
		return configuration().getDbName();
	}

	public String getDbUsername() {
		return configuration().getDbUsername().or("");
	}

	public String getDbPassword() {
		return configuration().getDbPassword().or("");
	}

	public boolean isAnonymousDbAccess() {
		return !configuration().getDbUsername().isPresent() 
				|| !configuration().getDbPassword().isPresent();
	}

	public ImmutableList<String> getDbHosts() {
		return configuration().getDbHosts();
	}

	public String getSmtpHost() {
		return configuration().getSmtpHost();
	}

	public int getSmtpPort() {
		return configuration().getSmtpPort();
	}

	public String getSmtpSupportEmail() {
		return configuration().getSmtpSupportEmail();
	}

	public String getSmtpNoreplyEmail() {
		return configuration().getSmtpNoreplyEmail();
	}	

	public String getPortalEndpoint() {
		return configuration().getPortalEndpoint().or("");
	}
	
	public File getGenBankDir(final Format format) {
		switch (format) {
		case GB_SEQ_XML:
			return new File(getRootDir(), "sequences/genbank/xml");			
		case FLAT_FILE:
			return new File(getRootDir(), "sequences/genbank/text");			
		default:
			throw new IllegalArgumentException("Unsupported GenBank format: " + format);
		}
	}

	@Override
	public void setup(final @Nullable Collection<URL> urls) {		
		this.dont_use = null;
		this.urls = (urls != null && !urls.isEmpty() ? urls : getDefaultConfiguration());
	}

	@Override
	public void preload() {
		// an initial access is needed due to lazy load, we create the application environment
		for (final File file : ImmutableList.of(getGenBankDir(Format.GB_SEQ_XML))) {
			try {
				file.mkdirs();
			} catch (Exception e) { }
		}		
	}

	@Override
	public void close() throws IOException { }

	// auxiliary methods

	private ConfigurationManager.Configuration configuration() {
		if (dont_use == null) {
			synchronized (ConfigurationManager.Configuration.class) {
				if (dont_use == null && urls != null) {
					try {						
						XMLConfiguration main = null;
						// sorting secondary configurations ensures that combination always result the same
						final SortedMap<String, XMLConfiguration> secondary = new TreeMap<String, XMLConfiguration>();
						// extract main configuration
						for (final URL url : urls) {
							final String filename = FilenameUtils.getName(url.getPath());							
							if (MAIN_CONFIG.equalsIgnoreCase(filename)) {
								main = new XMLConfiguration(url);
								LOGGER.info("Loading main configuration from: " + url.toString());
							} else if (!IGNORE_LIST.contains(FilenameUtils.getName(url.getPath()))) {
								secondary.put(filename, new XMLConfiguration(url));
								LOGGER.info("Loading secondary configuration from: " + url.toString());
							} else {
								LOGGER.info("Ignoring: " + url.toString());
							}
						}
						if (main != null) {							
							final CombinedConfiguration configuration = new CombinedConfiguration(new OverrideCombiner());							
							configuration.addConfiguration(main, MAIN_CONFIG);
							for (final Map.Entry<String, XMLConfiguration> entry : secondary.entrySet()) {
								configuration.addConfiguration(entry.getValue(), entry.getKey());
							}
							if (LOGGER.isDebugEnabled()) {
								String names = "";
								for (final String name : configuration.getConfigurationNameList()) {
									names += name + " ";
								}
								LOGGER.trace("Loading configuration from: " + names);
							}
							final List<String> foundNameList = new ArrayList<String>();
							// get main property will fail if the requested property is missing
							configuration.setThrowExceptionOnMissing(true);
							final File rootDir = getFile("lvl-root", configuration, foundNameList, true, null);
							final File localCacheDir = getFile("storage.local-cache", configuration, foundNameList, true, null);
							final File htdocsDir = getFile("storage.htdocs", configuration, foundNameList, false, null);
							final String dbName = getString("database.name", configuration, foundNameList, "lvldb");
							final String dbUsername = getString("database.credentials.username", configuration, foundNameList, null);
							final String dbPassword = getString("database.credentials.password", configuration, foundNameList, null);
							final ImmutableList<String> dbHosts = getStringList("database.hosts.host", Pattern.compile("^[\\w]+:[\\d]+$"), 
									configuration, foundNameList, Lists.newArrayList("localhost:27017"));
							final String smtpHost = getString("smtp.host", configuration, foundNameList, "localhost");
							final int smtpPort = getInteger("smtp.port", configuration, foundNameList, 25);
							final String smtpSupportEmail = getString("smtp.support-email", configuration, foundNameList, "support@example.com");
							final String smtpNoreplyEmail = getString("smtp.noreply-email", configuration, foundNameList, "noreply@example.com");
							final String portalEndpoint = getString("portal.endpoint", configuration, foundNameList, null);
							// get secondary property will return null if the requested property is missing
							configuration.setThrowExceptionOnMissing(false);
							// nothing yet
							// get other (free-format) properties
							final Iterator<String> keyIterator = configuration.getKeys();
							final Map<String, String> othersMap = new Hashtable<String, String>();
							while (keyIterator.hasNext()) {
								final String key = keyIterator.next();
								if (key != null && !foundNameList.contains(key)) {
									final String value = configuration.getString(key);
									if (value != null) {
										othersMap.put(key, value);
									}								
								}
							}
							dont_use = new Configuration(rootDir, localCacheDir, htdocsDir, dbName, dbUsername, dbPassword, dbHosts, 
									smtpHost, smtpPort, smtpSupportEmail, smtpNoreplyEmail, portalEndpoint, othersMap);
							LOGGER.info(dont_use.toString());
						} else {
							throw new IllegalStateException("Main configuration not found");
						}
					} catch (IllegalStateException e1) {
						throw e1;
					} catch (ConfigurationException e2) {
						throw new IllegalStateException(e2);
					} catch (Exception e) {						
						LOGGER.error("Failed to load configuration", e);
					}
				}
			}
		}
		return dont_use;
	}

	public static ImmutableList<URL> getDefaultConfiguration() {
		return new ImmutableList.Builder<URL>()
				.add(ConfigurationManager.class.getClassLoader().getResource(MAIN_CONFIG))
				.add(ConfigurationManager.class.getClassLoader().getResource(REST_SERVICE_CONFIG))
				.add(ConfigurationManager.class.getClassLoader().getResource(AUTHZ_SERVER_CONFIG))
				.build();
	}

	private static @Nullable File getFile(final String name, final CombinedConfiguration configuration, 
			final List<String> foundNameList, final boolean ensureWriting, final @Nullable File defaultValue) {
		foundNameList.add(name);
		final String value = subsEnvVars(configuration.getString(name), ensureWriting);
		return value != null ? new File(value) : defaultValue;		
	}

	private static @Nullable URL getUrl(final String name, final CombinedConfiguration configuration, 
			final List<String> foundNameList, final @Nullable URL defaultValue) {
		foundNameList.add(name);
		String value = subsEnvVars(configuration.getString(name), false);		
		URL url = null;
		try {
			url = parseURL(value);
		} catch (MalformedURLException e) {
			url = defaultValue;
			LOGGER.warn("The property contains an invalid URL: " + name);
		}
		return url;
	}

	private static @Nullable String getString(final String name, final CombinedConfiguration configuration, 
			final List<String> foundNameList, final @Nullable String defaultValue) {
		foundNameList.add(name);
		return configuration.getString(name, defaultValue);
	}

	private static @Nullable Integer getInteger(final String name, final CombinedConfiguration configuration, 
			final List<String> foundNameList, final @Nullable Integer defaultValue) {
		foundNameList.add(name);
		return configuration.getInt(name, defaultValue);
	}

	private static @Nullable ImmutableList<String> getStringList(final String name, final Pattern pattern, 
			final CombinedConfiguration configuration, final List<String> foundNameList, 
			final @Nullable List<String> defaultValue) {
		foundNameList.add(name);		
		return FluentIterable.from(configuration.getList(name, defaultValue)).transform(new Function<Object, String>() {
			@Override
			public String apply(final Object obj) {
				return obj instanceof String ? ((String) obj).trim() : null;
			}			
		}).filter(and(notNull(), contains(pattern))).toList();		
	}

	private static String subsEnvVars(final String path, final boolean ensureWriting) {
		String substituted = path;
		if (path != null && path.trim().length() > 0) {
			substituted = path.trim();
			if (substituted.startsWith("$HOME")) {
				final String userDir = FileUtils.getUserDirectoryPath();
				substituted = (ensureWriting && !new File(userDir).canWrite()
						? substituted.replaceFirst("\\$HOME", FileUtils.getTempDirectoryPath())
								: substituted.replaceFirst("\\$HOME", userDir));
			} else if (substituted.startsWith("$TMP")) {
				substituted = substituted.replaceFirst("\\$TMP", FileUtils.getTempDirectoryPath());				
			}
		}
		return substituted;
	}

	/* Inner classes */

	public static class Configuration {
		// common configuration
		private final File rootDir;
		private final File localCacheDir;
		private final File htdocsDir;		
		private final String dbName;
		private final Optional<String> dbUsername;
		private final Optional<String> dbPassword;
		private final ImmutableList<String> dbHosts;
		private final String smtpHost;
		private final int smtpPort;
		private final String smtpSupportEmail;
		private final String smtpNoreplyEmail;
		private final Optional<String> portalEndpoint;
		// other configurations
		private final ImmutableMap<String, String> othersMap;
		public Configuration(final File rootDir, final File localCacheDir, final File htdocsDir, 
				final String dbName, final @Nullable String dbUsername, final @Nullable String dbPassword, final ImmutableList<String> dbHosts,
				final String smtpHost, final int smtpPort, final String smtpSupportEmail, final String smtpNoreplyEmail,
				final @Nullable String portalEndpoint,
				final @Nullable Map<String, String> othersMap) {
			this.rootDir = checkNotNull(rootDir, "Uninitialized root directory");
			this.localCacheDir = checkNotNull(localCacheDir, "Uninitialized local cache directory");			
			this.htdocsDir = checkNotNull(htdocsDir, "Uninitialized hyper-text documents directory");			
			this.dbName = dbName;
			this.dbUsername = Optional.fromNullable(StringUtils.trimToNull(dbUsername));
			this.dbPassword = Optional.fromNullable(StringUtils.trimToNull(dbPassword));
			this.dbHosts = dbHosts;
			this.smtpHost = smtpHost;
			this.smtpPort = smtpPort;
			this.smtpSupportEmail = smtpSupportEmail;
			this.smtpNoreplyEmail = smtpNoreplyEmail;
			this.portalEndpoint = Optional.fromNullable(StringUtils.trimToNull(portalEndpoint));
			this.othersMap = new ImmutableMap.Builder<String, String>().putAll(othersMap).build();			
		}		
		public File getRootDir() {
			return rootDir;
		}
		public File getLocalCacheDir() {
			return localCacheDir;
		}
		public File getHtdocsDir() {
			return htdocsDir;
		}		
		public String getDbName() {
			return dbName;
		}
		public Optional<String> getDbUsername() {
			return dbUsername;
		}
		public Optional<String> getDbPassword() {
			return dbPassword;
		}		
		public ImmutableList<String> getDbHosts() {
			return dbHosts;
		}
		public String getSmtpHost() {
			return smtpHost;
		}
		public int getSmtpPort() {
			return smtpPort;
		}
		public String getSmtpSupportEmail() {
			return smtpSupportEmail;
		}
		public String getSmtpNoreplyEmail() {
			return smtpNoreplyEmail;
		}		
		public Optional<String> getPortalEndpoint() {
			return portalEndpoint;
		}
		public ImmutableMap<String, String> getOthersMap() {
			return othersMap;
		}
		public @Nullable String getProperty(final String name, final @Nullable String _default) {
			String configuration = null;
			if (othersMap != null && name != null) {
				configuration = othersMap.get(name);
			}
			return configuration != null ? configuration : _default; 
		}
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
					.add("rootDir", rootDir)
					.add("localCacheDir", localCacheDir)
					.add("htdocsDir", htdocsDir)					
					.add("dbName", dbName)
					.add("dbUsername", dbUsername.orNull())
					.add("dbPassword", dbPassword.orNull())
					.add("dbHosts", dbHosts)					
					.add("smtpHost", smtpHost)
					.add("smtpPort", smtpPort)
					.add("smtpSupportEmail", smtpSupportEmail)
					.add("smtpNoreplyEmail", smtpNoreplyEmail)
					.add("portalEndpoint", portalEndpoint.orNull())
					.add("customProperties", customPropertiesToString())
					.toString();
		}
		private String customPropertiesToString() {
			String str = "[";
			if (othersMap != null) {
				for (final Map.Entry<String, String> entry : othersMap.entrySet()) {
					str += entry.getKey() + "=" + entry.getValue() + " ";
				}
			}
			return str.trim() + "]";
		}
		private static @Nullable String readFromFile(final File file) {
			String str = null;
			if (file != null) {
				if (file.canRead()) {
					try {
						str = StringUtils.trimToEmpty(Files.toString(file, Charsets.UTF_8));
					} catch (Exception e) {
						LOGGER.error("Failed to read file: " + file.getPath(), e);
					}	
				} else {
					LOGGER.warn("Ignoring unreadable file: " + file.getPath());
				}
			}
			return str; 
		}
	}

}