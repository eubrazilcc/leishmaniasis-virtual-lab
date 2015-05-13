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

package eu.eubrazilcc.lvl.service.rest;

import static com.google.common.collect.Range.closed;
import static eu.eubrazilcc.lvl.core.concurrent.TaskRunner.TASK_RUNNER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskScheduler.TASK_SCHEDULER;
import static eu.eubrazilcc.lvl.core.concurrent.TaskStorage.TASK_STORAGE;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.LEISHMANIA_QUERY;
import static eu.eubrazilcc.lvl.core.entrez.EntrezHelper.SANDFLY_QUERY;
import static eu.eubrazilcc.lvl.core.servlet.ServletUtils.getClientAddress;
import static eu.eubrazilcc.lvl.service.Task.TaskType.IMPORT_SANDFLY_SEQ;
import static eu.eubrazilcc.lvl.storage.dao.LeishmaniaDAO.LEISHMANIA_DAO;
import static eu.eubrazilcc.lvl.storage.dao.SandflyDAO.SANDFLY_DAO;
import static eu.eubrazilcc.lvl.storage.oauth2.security.SseSubscriptionHttpHeaders.ssehHttpHeaders;
import static java.util.UUID.fromString;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ListenableScheduledFuture;

import eu.eubrazilcc.lvl.core.Leishmania;
import eu.eubrazilcc.lvl.core.Sandfly;
import eu.eubrazilcc.lvl.core.Sequence;
import eu.eubrazilcc.lvl.core.concurrent.CancellableTask;
import eu.eubrazilcc.lvl.core.conf.ConfigurationManager;
import eu.eubrazilcc.lvl.service.Progress;
import eu.eubrazilcc.lvl.service.Task;
import eu.eubrazilcc.lvl.service.io.ImportSequencesTask;
import eu.eubrazilcc.lvl.service.io.filter.NewSequenceFilter;
import eu.eubrazilcc.lvl.service.io.filter.SequenceIdFilter;
import eu.eubrazilcc.lvl.storage.oauth2.security.OAuth2SecurityManager;

/**
 * Tasks resource.
 * @author Erik Torres <ertorser@upv.es>
 */
@Path("/tasks")
public class TaskResource {

	private final static Logger LOGGER = getLogger(TaskResource.class);

	public static final String RESOURCE_NAME = ConfigurationManager.LVL_NAME + " Task Resource";

	public static final String PROGRESS_EVENT = "progress";

	public static final Range<Integer> REFRESH_RANGE = closed(1, 86400); // from 1 second to 1 day

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createTask(final Task task, final @Context UriInfo uriInfo,
			final @Context HttpServletRequest request, final @Context HttpHeaders headers) {		
		if (task == null) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		switch (task.getType()) {
		case IMPORT_SANDFLY_SEQ:
		case IMPORT_LEISMANIA_SEQ:
			OAuth2SecurityManager.login(request, null, headers, RESOURCE_NAME).requiresPermissions("tasks:data:maintenance:*:create");
			final List<String> ids = task.getIds();
			ImportSequencesTask<? extends Sequence> importSequencesTask = null;
			if (IMPORT_SANDFLY_SEQ.equals(task.getType())) {
				importSequencesTask = ImportSequencesTask.sandflyBuilder()
						.query(SANDFLY_QUERY)
						.builder(Sandfly.builder())						
						.dao(SANDFLY_DAO)
						.filter(ids == null || ids.isEmpty() ? NewSequenceFilter.sandflyBuilder().dao(SANDFLY_DAO).build() : SequenceIdFilter.builder().ids(ids).build())
						.build();
			} else {
				importSequencesTask = ImportSequencesTask.leishmaniaBuilder()
						.query(LEISHMANIA_QUERY)
						.builder(Leishmania.builder())
						.dao(LEISHMANIA_DAO)
						.filter(ids == null || ids.isEmpty() ? NewSequenceFilter.leishmaniaBuilder().dao(LEISHMANIA_DAO).build() : SequenceIdFilter.builder().ids(ids).build())
						.build();
			}			
			TASK_RUNNER.execute(importSequencesTask);
			TASK_STORAGE.add(importSequencesTask);
			task.setUuid(importSequencesTask.getUuid());
			break;		
		default:
			throw new WebApplicationException("Parameters do not match", Response.Status.BAD_REQUEST);
		}
		final UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(task.getUuid().toString());		
		return Response.created(uriBuilder.build()).build();		
	}

	@Path("progress/{id}")
	@GET
	@Produces(SseFeature.SERVER_SENT_EVENTS)
	public EventOutput getServerSentEvents(final @PathParam("id") String id, final @QueryParam("refresh") @DefaultValue("30") int refresh,
			final @QueryParam("token") @DefaultValue("") String token, final @Context HttpServletRequest request, 
			final @Context HttpHeaders headers) {
		if (isBlank(id) || !REFRESH_RANGE.contains(refresh)) {
			throw new WebApplicationException("Missing required parameters", Response.Status.BAD_REQUEST);
		}
		OAuth2SecurityManager.login(request, null, isBlank(token) ? headers : ssehHttpHeaders(token), RESOURCE_NAME).requiresPermissions("tasks:*:*:" + id.trim() + ":view");
		// get from task storage
		final CancellableTask<?> task = TASK_STORAGE.get(fromString(id));
		if (task == null) {
			throw new WebApplicationException("Element not found", Response.Status.NOT_FOUND);
		}
		final String client = getClientAddress(request);
		LOGGER.info("Subscribed to progress events: " + client);
		final AtomicLong eventId = new AtomicLong(0l);
		final EventOutput eventOutput = new EventOutput();
		TASK_RUNNER.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				try {
					do {
						final ListenableScheduledFuture<?> future = TASK_SCHEDULER.schedule(checkTaskProgress(eventOutput, eventId, task), 
								eventId.getAndIncrement() == 0 ? 0 : refresh, SECONDS);
						future.get();
					} while (!task.isDone());
				} catch (Exception e) {
					LOGGER.error("Failed to get task status", e);
				} finally {
					try {
						eventOutput.close();
					} catch (Exception ignored) { }
					LOGGER.info("Closing progress events where subscriber is: " + client);
				}
				return null;
			}			
		});
		return eventOutput;
	}

	private static Runnable checkTaskProgress(final EventOutput eventOutput, final AtomicLong eventId, final CancellableTask<?> task) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					eventOutput.write(new OutboundEvent.Builder().name(PROGRESS_EVENT)
							.id(Long.toString(eventId.get()))
							.data(String.class, JSON_MAPPER.writeValueAsString(Progress.builder()
									.done(task.isDone())
									.progress(task.getProgress())
									.status(task.getStatus())
									.hasErrors(task.hasErrors())
									.build()))
									.build());
				} catch (IOException e) {
					throw new RuntimeException("Error when writing the event", e);
				}
			}			
		};
	}

}