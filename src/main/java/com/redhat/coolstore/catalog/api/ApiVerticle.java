package com.redhat.coolstore.catalog.api;

import java.util.List;

import com.redhat.coolstore.catalog.model.Product;
import com.redhat.coolstore.catalog.verticle.service.CatalogService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ApiVerticle extends AbstractVerticle {

	private CatalogService catalogService;

	public ApiVerticle(CatalogService catalogService) {
		this.catalogService = catalogService;
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {

		Router router = Router.router(vertx);
		// ----
		// Add routes to the Router
		// * A route for HTTP GET requests that matches the "/products" path.
		// The handler for this route is implemented by the `getProducts()` method.
		// * A route for HTTP GET requests that matches the /product/:itemId path.
		// The handler for this route is implemented by the `getProduct()` method.
		// * A route for the path "/product" to which a `BodyHandler` is attached.
		// * A route for HTTP POST requests that matches the "/product" path.
		// The handler for this route is implemented by the `addProduct()` method.
		// ----
		router.get("/products").produces("application/json").handler(rc -> getProducts(rc));
		router.get("/product/:itemId").produces("application/json").handler(rc -> getProduct(rc));

		router.route("/product").handler(BodyHandler.create());
		router.post("/product").produces("application/json").handler(rc -> addProduct(rc));

		// Health Checks
		router.get("/health/readiness").handler(rc -> rc.response().end("OK"));
		HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx).register("health", f -> health(f));
		router.get("/health/liveness").handler(healthCheckHandler);

		// ----
		// Create a HTTP server.
		// * Use the `Router` as request handler
		// * Use the verticle configuration to obtain the port to listen to.
		// Get the configuration from the `config()` method of AbstractVerticle.
		// Look for the key "catalog.http.host", which returns an Integer.
		// The default value (if the key is not set in the configuration) is 8080.
		// * If the HTTP server is correctly instantiated, complete the `Future`. If
		// there is a failure, fail the `Future`.
		// ----
		int port = config().getInteger("catalog.http.port", 8080);
		vertx.createHttpServer().requestHandler(router::accept).listen(port, res -> {
			if (res.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(res.cause());
			}
		});

	}

	private void getProducts(RoutingContext rc) {
		// ----
		// Needs to be implemented
		// In the implementation:
		// * Call the `getProducts()` method of the CatalogService.
		// * In the handler, transform the `List<Product>` response to a `JsonArray`
		// object.
		// * Put a "Content-type: application/json" header on the `HttpServerResponse`
		// object.
		// * Write the `JsonArray` to the `HttpServerResponse`, and end the response.
		// * If the `getProducts()` method returns a failure, fail the `RoutingContext`.
		// ----
		catalogService.getProducts(h -> {
			if (h.succeeded()) {
				List<Product> products = h.result();
				JsonArray array = new JsonArray();
				products.forEach(p -> array.add(p.toJson()));
				rc.response().setStatusCode(200).putHeader("content-type", "application/json").end(array.encode());
			} else {
				rc.response().setStatusCode(500).end();
				rc.fail(h.cause());
			}
		});
	}

	// PATH /product/:itemId
	private void getProduct(RoutingContext rc) {
		// ----
		// Needs to be implemented
		// In the implementation:
		// * Call the `getProduct()` method of the CatalogService.
		// * In the handler, transform the `Product response to a `JsonObject` object.
		// * Put a "Content-type: application/json" header on the `HttpServerResponse`
		// object.
		// * Write the `JsonObject` to the `HttpServerResponse`, and end the response.
		// * If the `getProduct()` method of the CatalogService returns null, fail the
		// RoutingContext with a 404 HTTP status code
		// * If the `getProduct()` method returns a failure, fail the `RoutingContext`.
		// ----
		String itemId = rc.request().getParam("itemId");
		catalogService.getProduct(itemId, h -> {
			if (h.succeeded()) {
				Product product = h.result();
				if (product == null) {
					rc.response().setStatusCode(404).end();
				} else {
					rc.response().setStatusCode(200).putHeader("content-type", "application/json")
							.end(product.toJson().encode());
				}
			} else {
				rc.response().setStatusCode(500).end();
				rc.fail(h.cause());
			}
		});
	}

	private void addProduct(RoutingContext rc) {
		// ----
		// Needs to be implemented
		// In the implementation:
		// * Obtain the body contents from the `RoutingContext`. Expect the body to be
		// JSON.
		// * Transform the JSON payload to a `Product` object.
		// * Call the `addProduct()` method of the CatalogService.
		// * If the call succeeds, set a HTTP status code 201 on the
		// `HttpServerResponse`, and end the response.
		// * If the call fails, fail the `RoutingContext`.
		// ----
		JsonObject body = rc.getBodyAsJson();
		if (body == null) {
			rc.response().setStatusCode(500).end();
			rc.fail(new IllegalArgumentException("The payload is empty"));
			return;
		}

		Product product = new Product(body);
		catalogService.addProduct(product, h -> {
			if (h.succeeded()) {
				rc.response().setStatusCode(201).end();
			} else {
				rc.response().setStatusCode(500).end();
				rc.fail(h.cause());
			}
		});
	}

	private void health(Future<Status> future) {
		catalogService.ping(ar -> {
			if (ar.succeeded()) {
				// HealthCheckHandler has a timeout of 1000s. If timeout is exceeded, the future
				// will be failed
				if (!future.isComplete()) {
					future.complete(Status.OK());
				}
			} else {
				if (!future.isComplete()) {
					future.complete(Status.KO());
				}
			}
		});
	}
}
