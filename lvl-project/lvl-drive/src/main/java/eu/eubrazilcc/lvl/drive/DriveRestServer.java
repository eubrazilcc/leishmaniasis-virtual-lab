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
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Drive REST server.
 * @author Erik Torres <ertorser@upv.es>
 */
public class DriveRestServer extends AbstractVerticle {

	private final static Logger LOGGER = getLogger(DriveRestServer.class);

	private final static long MAX_BODY_SIZE = 32;

	@Override
	public void start() throws Exception {
		final Router router = Router.router(vertx);
		// set body limit
		final long maxBodySize = context.config().getLong("http.max-body-size", MAX_BODY_SIZE) * 1000000l;		
		router.route().handler(BodyHandler.create().setBodyLimit(maxBodySize));
		// enable CORS
		router.route().handler(CorsHandler.create("*")
				.allowedMethod(HttpMethod.GET)
				.allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.PUT)
				.allowedMethod(HttpMethod.DELETE)
				.allowedMethod(HttpMethod.OPTIONS)
				.allowedHeader("Content-Type")
				.allowedHeader("Authorization"));		
		// configure index page
		router.route("/").handler(StaticHandler.create());
		// serve resources
		router.get("/rest/v1/datasets/:ns").produces("application/json").handler(this::handleGetDatasets);
		router.get("/rest/v1/datasets/:ns/:id").produces("application/json").handler(this::handleGetDataset);		
		router.post("/rest/v1/datasets/:ns").consumes("application/json").handler(this::handlePostDataset);
		router.put("/rest/v1/datasets/:ns/:id").consumes("application/json").handler(this::handlePutDataset);
		router.delete("/rest/v1/datasets/:ns/:id").handler(this::handleDeleteDataset);
		// start HTTP server
		final int port = context.config().getInteger("http.port", 8080);		
		vertx.createHttpServer().requestHandler(router::accept).listen(port);
		LOGGER.trace("New instance created: [id=" + context.deploymentID() + "].");
	}

	private void handleGetDatasets(final RoutingContext routingContext) {
		// TODO
		
		final HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json").end();
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

	private void handlePostDataset(final RoutingContext routingContext) {
		// TODO
		
		final HttpServerResponse response = routingContext.response();
		response.putHeader("Location", "/rest/v1/datasets/ns/id").setStatusCode(201).end(); // TODO
	}

	private void handlePutDataset(final RoutingContext routingContext) {
		// TODO
		
		final HttpServerResponse response = routingContext.response();
		response.setStatusCode(204).end();
	}

	private void handleDeleteDataset(final RoutingContext routingContext) {
		// TODO
		
		final HttpServerResponse response = routingContext.response();
		response.setStatusCode(204).end();
	}

	private void sendError(final int statusCode, final HttpServerResponse response) {
		response.setStatusCode(statusCode).end();
	}
	
	/* TODO
	 
	  private void handleAddProduct(RoutingContext routingContext) {
    String productID = routingContext.request().getParam("productID");
    HttpServerResponse response = routingContext.response();
    if (productID == null) {
      sendError(400, response);
    } else {
      JsonObject product = routingContext.getBodyAsJson();
      if (product == null) {
        sendError(400, response);
      } else {
        products.put(productID, product);
        response.end();
      }
    }
  }

  private void handleListProducts(RoutingContext routingContext) {
    JsonArray arr = new JsonArray();
    products.forEach((k, v) -> arr.add(v));
    routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
  }

  private void setUpInitialData() {
    addProduct(new JsonObject().put("id", "prod3568").put("name", "Egg Whisk").put("price", 3.99).put("weight", 150));
    addProduct(new JsonObject().put("id", "prod7340").put("name", "Tea Cosy").put("price", 5.99).put("weight", 100));
    addProduct(new JsonObject().put("id", "prod8643").put("name", "Spatula").put("price", 1.00).put("weight", 80));
  }

  private void addProduct(JsonObject product) {
    products.put(product.getString("id"), product);
  }
	  
	  import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.Runner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
	  
	  https://github.com/vert-x3/vertx-examples/blob/master/web-examples/src/main/java/io/vertx/example/web/rest/SimpleREST.java
	  
	 */

}