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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.util.concurrent.AbstractIdleService;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * Driver service.
 * @author Erik Torres <ertorser@upv.es>
 */
public class VertxService extends AbstractIdleService {

	private final static Logger LOGGER = getLogger(VertxService.class);

	private final VertxOptions vertxOptions;
	private final DeploymentOptions deploymentOptions;

	private Vertx vertx;

	private List<Class<?>> verticles = newArrayList(DriveRestServer.class);

	public VertxService(final @Nullable VertxOptions vertxOptions, final @Nullable DeploymentOptions deploymentOptions) {
		this.vertxOptions = ofNullable(vertxOptions).orElse(new VertxOptions());
		this.deploymentOptions = ofNullable(deploymentOptions).orElse(new DeploymentOptions());
	}

	@Override
	protected void startUp() throws Exception {
		final Consumer<Vertx> runner = vertx -> {
			for (final Class<?> verticle : verticles) {
				vertx.deployVerticle(verticle.getCanonicalName(), deploymentOptions, new Handler<AsyncResult<String>>() {
					@Override
					public void handle(final AsyncResult<String> result) {
						if (result != null && result.succeeded()) LOGGER.info("New verticle deployed: [type=" + verticle.getSimpleName() 
							+ ", id=" + result.result() + "].");
						else LOGGER.error("Failed to deploy verticle: " + result.result(), result.cause());
					}
				});
			}
		};
		if (vertxOptions.isClustered()) {
			Vertx.clusteredVertx(vertxOptions, res -> {
				if (res.succeeded()) {
					vertx = res.result();					
					runner.accept(vertx);
				} else {
					res.cause().printStackTrace();
				}
			});
		} else {
			vertx = Vertx.vertx(vertxOptions);
			runner.accept(vertx);
		}
	}

	@Override
	protected void shutDown() throws Exception {
		vertx.close(new Handler<AsyncResult<Void>>() {			
			@Override
			public void handle(final AsyncResult<Void> result) {
				if (result != null && result.succeeded()) LOGGER.info("Shutdown succeeded.");
				else LOGGER.info("Exited with error.", result.cause());
			}
		});
	}

}