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

package eu.eubrazilcc.lvl.drive;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * Drive REST server.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DriveRestServer extends AbstractVerticle {

	private final static Logger LOGGER = getLogger(DriveRestServer.class);
	
	@Override
	public void start() throws Exception {
		final Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		router.route().handler(CorsHandler.create("*")
				.allowedMethod(HttpMethod.GET)
				.allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.OPTIONS)
				.allowedHeader("Content-Type"));
		router.get("/datasets/:ns/:id").handler(this::handleGetDataset);
		final int port = context.config().getInteger("http.port", 8080);
		vertx.createHttpServer().requestHandler(router::accept).listen(port);
		LOGGER.trace("New instance created: [id=" + context.deploymentID() + "].");
	}

	private void handleGetDataset(final RoutingContext routingContext) {
		final String ns = routingContext.request().getParam("ns");
		final String id = routingContext.request().getParam("id");
		final HttpServerResponse response = routingContext.response();
		if (id == null) {
			sendError(400, response);
		} else {


			/* TODO JsonObject product = products.get(productID);
			if (product == null) {
				sendError(404, response);
			} else {
				response.putHeader("content-type", "application/json").end(product.encodePrettily());
			} */

			response.putHeader("content-type", "application/json").end("{ \"id\" : \"21\" }"); // TODO
		}
	}

	private void sendError(final int statusCode, final HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}

}