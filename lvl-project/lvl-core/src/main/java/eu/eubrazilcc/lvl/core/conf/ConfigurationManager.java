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

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.contains;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static eu.eubrazilcc.lvl.core.util.UrlUtils.parseURL;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.getTempDirectoryPath;
import static org.apache.commons.io.FileUtils.getUserDirectoryPath;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import eu.eubrazilcc.lvl.core.Closeable2;

/**
 * Manages configuration.
 * @author Erik Torres <ertorser@upv.es>
 */
public enum ConfigurationManager implements Closeable2 {

	CONFIG_MANAGER;

	private final static Logger LOGGER = getLogger(ConfigurationManager.class);

	public static final String MAIN_CONFIG         = "lvl.xml";
	public static final String REST_SERVICE_CONFIG = "lvl-service.xml";
	public static final String AUTHZ_SERVER_CONFIG = "lvl-auth.xml";

	public static final ImmutableList<String> IGNORE_LIST = of("logback.xml");

	public static final String LVL_NAME = "Leishmaniasis Virtual Laboratory (LVL)";

	private Configuration dont_use = null;
	private Collection<URL> urls = getDefaultConfiguration();	

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

	public boolean isBrokerEmbedded() {
		return configuration().isBrokerEmbedded();
	}

	public ImmutableList<String> getMessageBrokers() {
		return configuration().getMessageBrokers();
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

	public String getWfHostname() {
		return configuration().getWfHostname();
	}

	public boolean isWfSecure() {
		return configuration().isWfSecure();
	}

	public int getWfPort() {
		return configuration().getWfPort();
	}

	public String getWfUsername() {
		return configuration().getWfUsername().or("");
	}

	public String getWfPasswd() {
		return configuration().getWfPasswd().or("");
	}

	@Override
	public void setup(final @Nullable Collection<URL> urls) {		
		this.dont_use = null;
		this.urls = (urls != null && !urls.isEmpty() ? urls : getDefaultConfiguration());
	}

	@Override
	public void preload() {
		// an initial access is needed due to lazy load, we create the application environment		
	}

	@Override
	public void close() throws IOException { }

	// auxiliary methods

	private Configuration configuration() {
		if (dont_use == null) {
			synchronized (Configuration.class) {
				if (dont_use == null && urls != null) {
					try {						
						XMLConfiguration main = null;
						// sorting secondary configurations ensures that combination always result the same
						final SortedMap<String, XMLConfiguration> secondary = newTreeMap();
						// extract main configuration
						for (final URL url : urls) {
							final String filename = getName(url.getPath());							
							if (MAIN_CONFIG.equalsIgnoreCase(filename)) {
								main = new XMLConfiguration(url);
								LOGGER.info("Loading main configuration from: " + url.toString());
							} else if (!IGNORE_LIST.contains(getName(url.getPath()))) {
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
							final List<String> foundNameList = newArrayList();
							// get main property will fail if the requested property is missing
							configuration.setThrowExceptionOnMissing(true);
							final File rootDir = getFile("lvl-root", configuration, foundNameList, true, null);
							final File localCacheDir = getFile("storage.local-cache", configuration, foundNameList, true, null);
							final File htdocsDir = getFile("storage.htdocs", configuration, foundNameList, false, null);
							final String dbName = getString("database.name", configuration, foundNameList, "lvldb");
							final String dbUsername = getString("database.credentials.username", configuration, foundNameList, null);
							final String dbPassword = getString("database.credentials.password", configuration, foundNameList, null);
							final ImmutableList<String> dbHosts = getStringList("database.hosts.host", Pattern.compile("^[\\w]+:[\\d]+$"), 
									configuration, foundNameList, newArrayList("localhost:27017"));
							final boolean brokerEmbedded = getBoolean("broker.embedded", configuration, foundNameList, true);
							final ImmutableList<String> messageBrokers = getStringList("messaging.hosts.host", Pattern.compile("^[\\w]+:[\\d]+$"), 
									configuration, foundNameList, newArrayList("localhost:61616"));
							final String smtpHost = getString("smtp.host", configuration, foundNameList, "localhost");
							final int smtpPort = getInteger("smtp.port", configuration, foundNameList, 25);
							final String smtpSupportEmail = getString("smtp.support-email", configuration, foundNameList, "support@example.com");
							final String smtpNoreplyEmail = getString("smtp.noreply-email", configuration, foundNameList, "noreply@example.com");
							final String portalEndpoint = getString("portal.endpoint", configuration, foundNameList, null);
							final String wfHostname = getString("workflow.endpoint.hostname", configuration, foundNameList, "localhost");
							final boolean wfSecure = getBoolean("workflow.endpoint.secure", configuration, foundNameList, false);
							final int wfPort = getInteger("workflow.endpoint.port", configuration, foundNameList, wfSecure ? 443 : 80);							
							final String wfUsername = getString("workflow.credentials.username", configuration, foundNameList, null);
							final String wfPasswd = getString("workflow.credentials.password", configuration, foundNameList, null);
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
									brokerEmbedded, messageBrokers, smtpHost, smtpPort, smtpSupportEmail, smtpNoreplyEmail, portalEndpoint, 
									wfHostname, wfSecure, wfPort, wfUsername, wfPasswd, othersMap);
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
		return of(ConfigurationManager.class.getClassLoader().getResource(MAIN_CONFIG),
				ConfigurationManager.class.getClassLoader().getResource(REST_SERVICE_CONFIG),
				ConfigurationManager.class.getClassLoader().getResource(AUTHZ_SERVER_CONFIG));				
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

	private static @Nullable Boolean getBoolean(final String name, final CombinedConfiguration configuration, 
			final List<String> foundNameList, final @Nullable Boolean defaultValue) {
		foundNameList.add(name);
		return configuration.getBoolean(name, defaultValue);
	}

	private static @Nullable ImmutableList<String> getStringList(final String name, final Pattern pattern, 
			final CombinedConfiguration configuration, final List<String> foundNameList, 
			final @Nullable List<String> defaultValue) {
		foundNameList.add(name);		
		return from(configuration.getList(name, defaultValue)).transform(new Function<Object, String>() {
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
				final String userDir = getUserDirectoryPath();
				substituted = (ensureWriting && !new File(userDir).canWrite()
						? substituted.replaceFirst("\\$HOME", getTempDirectoryPath())
								: substituted.replaceFirst("\\$HOME", userDir));
			} else if (substituted.startsWith("$TMP")) {
				substituted = substituted.replaceFirst("\\$TMP", getTempDirectoryPath());				
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
		private final boolean brokerEmbedded;
		private final ImmutableList<String> messageBrokers;
		private final String smtpHost;
		private final int smtpPort;
		private final String smtpSupportEmail;
		private final String smtpNoreplyEmail;
		private final Optional<String> portalEndpoint;
		private final String wfHostname;
		private final boolean wfSecure;
		private final int wfPort;							
		private final Optional<String> wfUsername;
		private final Optional<String> wfPasswd;		
		// other configurations
		private final ImmutableMap<String, String> othersMap;
		public Configuration(final File rootDir, final File localCacheDir, final File htdocsDir, 
				final String dbName, final @Nullable String dbUsername, final @Nullable String dbPassword, final ImmutableList<String> dbHosts,
				final boolean brokerEmbedded, final ImmutableList<String> messageBrokers,
				final String smtpHost, final int smtpPort, final String smtpSupportEmail, final String smtpNoreplyEmail,
				final @Nullable String portalEndpoint,
				final @Nullable String wfHostname, final boolean wfSecure, final int wfPort, final @Nullable String wfUsername, final @Nullable String wfPasswd,
				final @Nullable Map<String, String> othersMap) {
			this.rootDir = checkNotNull(rootDir, "Uninitialized root directory");
			this.localCacheDir = checkNotNull(localCacheDir, "Uninitialized local cache directory");			
			this.htdocsDir = checkNotNull(htdocsDir, "Uninitialized hyper-text documents directory");
			this.dbName = dbName;
			this.dbUsername = fromNullable(trimToNull(dbUsername));
			this.dbPassword = fromNullable(trimToNull(dbPassword));
			this.dbHosts = dbHosts;
			this.brokerEmbedded = brokerEmbedded;
			this.messageBrokers = messageBrokers;
			this.smtpHost = smtpHost;
			this.smtpPort = smtpPort;
			this.smtpSupportEmail = smtpSupportEmail;
			this.smtpNoreplyEmail = smtpNoreplyEmail;
			this.portalEndpoint = fromNullable(trimToNull(portalEndpoint));
			this.wfHostname = wfHostname;
			this.wfSecure = wfSecure;
			this.wfPort = wfPort;
			this.wfUsername = fromNullable(trimToNull(wfUsername));
			this.wfPasswd = fromNullable(trimToNull(wfPasswd));
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
		public boolean isBrokerEmbedded() {
			return brokerEmbedded;
		}		
		public ImmutableList<String> getMessageBrokers() {
			return messageBrokers;
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
		public String getWfHostname() {
			return wfHostname;
		}
		public boolean isWfSecure() {
			return wfSecure;
		}
		public int getWfPort() {
			return wfPort;
		}
		public Optional<String> getWfUsername() {
			return wfUsername;
		}
		public Optional<String> getWfPasswd() {
			return wfPasswd;
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
			return toStringHelper(this)
					.add("rootDir", rootDir)
					.add("localCacheDir", localCacheDir)
					.add("htdocsDir", htdocsDir)
					.add("dbName", dbName)
					.add("dbUsername", dbUsername.orNull())
					.add("dbPassword", dbPassword.orNull())
					.add("dbHosts", dbHosts)
					.add("brokerEmbedded", brokerEmbedded)
					.add("messageBrokers", messageBrokers)
					.add("smtpHost", smtpHost)
					.add("smtpPort", smtpPort)
					.add("smtpSupportEmail", smtpSupportEmail)
					.add("smtpNoreplyEmail", smtpNoreplyEmail)
					.add("portalEndpoint", portalEndpoint.orNull())
					.add("wfHostname", wfHostname)
					.add("wfSecure", wfSecure)
					.add("wfPort", wfPort)
					.add("wfUsername", wfUsername.orNull())
					.add("wfPasswd", wfPasswd.orNull())
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
						str = trimToEmpty(Files.toString(file, UTF_8));
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