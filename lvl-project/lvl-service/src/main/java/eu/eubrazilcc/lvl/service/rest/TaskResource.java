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

import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.service.Task;
import eu.eubrazilcc.lvl.service.io.SequenceManager;

/**
 * Tasks resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/tasks")
public class TaskResource {

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Task Resource";
	public static final String RESOURCE_SCOPE = resourceScope(TaskResource.class);
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTask(final Task task, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		
		// TODO
		final String taskId = "112";
		
		SequenceManager.INSTANCE.importSequences();
		
		// TODO
		
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(taskId);		
		return Response.created(uriBuilder.build()).build();		
	}
	
}