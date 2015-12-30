package org.mermaid.vertxmvc;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DispatcherVerticle extends AbstractVerticle {
	private Logger logger = LogManager.getLogger(getClass());

	public void start() {
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route().handler(routingContext -> {
			HttpServerRequest request = routingContext.request();
			HttpServerResponse response = routingContext.response();
			logger.info(request.getParam("a"));
			response.putHeader("content-type", "text/plain");
			response.end("Hello World from Vert.x-Web!");
		});
		server.requestHandler(router::accept).listen(Container.config.getServer().getPort());
	}
}
