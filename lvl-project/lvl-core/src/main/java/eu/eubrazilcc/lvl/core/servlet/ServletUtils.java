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

package eu.eubrazilcc.lvl.core.servlet;

import static com.google.common.base.Preconditions.checkArgument;
import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.CONFIG_MANAGER;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

/**
 * Utility class to handle Servlet common tasks.
 * @author Erik Torres <ertorser@upv.es>
 */
public final class ServletUtils {

	private final static Logger LOGGER = getLogger(ServletUtils.class);

	/**
	 * Gets the client's IP address that's accessing the application, inspecting the request for 
	 * HTTP header "X-Forwarded-For (XFF)" and, when no header is found, it uses the IP reported
	 * in the request.
	 * @param request - input request
	 * @return the client's IP address that's accessing the application.
	 */
	public static final String getClientAddress(final HttpServletRequest request) {
		checkArgument(request != null, "Uninitialized request");
		String address = request.getHeader("X-Forwarded-For");  
		if (isBlank(address)) {  
			address = request.getRemoteAddr();  
		}
		return address;
	}

	/**
	 * Gets the portal endpoint by inspecting the application configuration. In case that the endpoint
	 * cannot be discovered from the configuration, 
	 * @param baseUri - 
	 * @return
	 */
	public static final URI getPortalEndpoint(final URI baseUri) {
		URI portalUri = null;
		try {
			final String portalEndpoint = CONFIG_MANAGER.getPortalEndpoint();
			portalUri = isNotBlank(portalEndpoint) ? new URI(portalEndpoint.replaceAll("/$", "")) 
			: new URI(baseUri.getScheme(), baseUri.getAuthority(), null, null, null);
		} catch (URISyntaxException e) {
			LOGGER.error("Failed to create LVL portal endpoint", e);
		}
		return portalUri;
	}

}