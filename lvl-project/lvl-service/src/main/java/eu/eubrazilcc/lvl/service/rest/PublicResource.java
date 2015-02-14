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

import static eu.eubrazilcc.lvl.core.conf.ConfigurationManager.LVL_NAME;
import static eu.eubrazilcc.lvl.service.rest.ResourceIdentifierPattern.URL_FRAGMENT_PATTERN;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

/**
 * Public resources.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/public")
public class PublicResource {

	private final static Logger LOGGER = getLogger(PublicResource.class);

	public static final String RESOURCE_NAME = LVL_NAME + " Public Resource";
	
	@GET
	@Path("datasets/{id: " + URL_FRAGMENT_PATTERN + "}")
	@Produces(APPLICATION_OCTET_STREAM)
	public Response downloadDataset(final @PathParam("id") String id, final @Context UriInfo uriInfo, 
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		// TODO
		return null;
	}
	
}