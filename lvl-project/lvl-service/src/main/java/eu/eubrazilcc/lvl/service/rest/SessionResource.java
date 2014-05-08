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

package eu.eubrazilcc.lvl.service.rest;

import static eu.eubrazilcc.lvl.storage.oauth2.security.SecretProvider.generateFastUrlSafeSecret;
import static java.util.Calendar.getInstance;
import static javax.ws.rs.core.NewCookie.valueOf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.filter.CsrfProtectionFilter;

/**
 * Resource that can be used to create a new session in the application. This resource assumes that incoming requests
 * that modify the status of the resource (POST, PUT, DELETE) are handled by a CSRF protection filter that checks for 
 * the presence of a valid session in the request. Therefore, this resource is limited to HTTP GET requests.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/sessions")
public class SessionResource {

	public static final int SESSION_VALIDITY_HOURS = 168;
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z");
	
	@GET
	public Response openSession() {
		final Calendar expires = getInstance();
		expires.setTime(new Date());
		expires.add(Calendar.HOUR, SESSION_VALIDITY_HOURS);
		return Response.ok()
				.cookie(valueOf(CsrfProtectionFilter.HEADER_NAME + "=" + generateFastUrlSafeSecret() 
						+ "; Expires=" + DATE_FORMAT.format(expires.getTime()) 
						+ "; Secure"
						+ "; HttpOnly"))
				.build();
		
		// TODO : make XSRF verifiable
		
	}
	
}