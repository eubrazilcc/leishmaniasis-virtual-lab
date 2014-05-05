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

import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskScheduler.TASK_SCHEDULER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.storage.oauth2.security.ScopeManager.resourceScope;
import static java.util.UUID.fromString;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;

import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.service.Task;
import eu.eubrazilcc.lvl.service.io.DbNotFoundSequenceFilter;
import eu.eubrazilcc.lvl.service.io.ImportSequencesTask;
import eu.eubrazilcc.lvl.service.io.MatchSequenceFilter;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2Gatekeeper;

/**
 * Tasks resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/tasks")
public class TaskResource {

	private final static Logger LOGGER = getLogger(TaskResource.class);

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Task Resource";
	public static final String RESOURCE_SCOPE = resourceScope(TaskResource.class);

	public static final String IS_DONE_EVENT  = "is_done";
	public static final String PROGRESS_EVENT = "progress";
	public static final String STATUS_EVENT   = "status";

	public static final int REFRESH_SECONDS = 10;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTask(final Task task, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, true, RESOURCE_NAME);
		if (task == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		switch (task.getType()) {
		case IMPORT_SEQUENCES:
			final List<String> ids = task.getIds();
			final ImportSequencesTask importSequencesTask = ImportSequencesTask.builder()
					.filter(ids == null || ids.isEmpty() ? new DbNotFoundSequenceFilter() : new MatchSequenceFilter(ids))
					.build();
			TASK_RUNNER.execute(importSequencesTask);
			TASK_STORAGE.add(importSequencesTask);
			task.setUuid(importSequencesTask.getUuid());
			break;
		default:
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(task.getUuid().toString());		
		return Response.created(uriBuilder.build()).build();		
	}

	@Path("progress/{id}")
	@GET
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput getServerSentEvents(final @PathParam("id") String id, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		OAuth2Gatekeeper.authorize(request, null, headers, RESOURCE_SCOPE, false, RESOURCE_NAME);
		if (isBlank(id)) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		// get from task storage
		final CancellableTask<?> task = TASK_STORAGE.get(fromString(id));
		if (task == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		final AtomicBoolean isInitial = new AtomicBoolean(true);
		final EventOutput eventOutput = new EventOutput();
		try {
			do {
				TASK_SCHEDULER.schedule(checkTaskProgress(eventOutput, task), 
						isInitial.getAndSet(false) ? 0 : REFRESH_SECONDS, TimeUnit.SECONDS).get();
			} while (!task.isDone());
		} catch (Exception e) {
			LOGGER.error("Failed to get task status", e);
		} finally {
			try {
				eventOutput.close();
			} catch (Exception ignored) { }
		}
		return eventOutput;
	}

	private static Runnable checkTaskProgress(final EventOutput eventOutput, final CancellableTask<?> task) {
		return new Runnable() {			
			@Override
			public void run() {
				try {
					final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
					eventOutput.write(eventBuilder.name(IS_DONE_EVENT)
							.data(Boolean.class, task.isDone())
							.build());
					eventOutput.write(eventBuilder.name(PROGRESS_EVENT)
							.data(Integer.class, task.getProgress())
							.build());
					eventOutput.write(eventBuilder.name(STATUS_EVENT)
							.data(String.class, task.getStatus())
							.build());
				} catch (IOException e) {
					throw new RuntimeException("Error when writing the event", e);
				}
			}			
		};
	}

}