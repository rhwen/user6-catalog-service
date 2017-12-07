package com.redhat.coolstore.catalog.verticle.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class CatalogServiceImpl implements CatalogService {

	private MongoClient client;

	public CatalogServiceImpl(Vertx vertx, JsonObject config, MongoClient client) {
		this.client = client;
	}

	@Override
	public void getProducts(Handler<AsyncResult<List<Product>>> resulthandler) {
		// ----
		// To be implemented
		//
		// Use the `MongoClient.find()` method.
		// Use an empty JSONObject for the query
		// The collection to search is "products"
		// In the handler implementation, transform the `List<JSONObject>` to
		// `List<Person>` - use Java8 Streams!
		// Use a Future to set the result on the handle() method of the result handler
		// Don't forget to handle failures!
		// ----
		client.find("products", new JsonObject(), res -> {
			if (res.succeeded()) {
				resulthandler.handle(Future
						.succeededFuture(res.result().stream().map(m -> new Product(m)).collect(Collectors.toList())));
			} else {
				resulthandler.handle(Future.failedFuture(res.cause()));
				res.cause().printStackTrace();
			}
		});
	}

	@Override
	public void getProduct(String itemId, Handler<AsyncResult<Product>> resulthandler) {
		// ----
		// To be implemented
		//
		// Use the `MongoClient.find()` method.
		// Use a JSONObject for the query with the field 'itemId' set to the product
		// itemId
		// The collection to search is "products"
		// In the handler implementation, transform the `List<JSONObject>` to `Person` -
		// use Java8 Streams!
		// If the product is not found, the result should be set to null
		// Use a Future to set the result on the handle() method of the result handler
		// Don't forget to handle failures!
		// ----

		client.find("products", new JsonObject().put("_id", itemId), res -> {
			if (res.succeeded()) {
				resulthandler.handle(Future.succeededFuture(res.result().size() == 0 ? null
						: res.result().stream().map(m -> new Product(m)).collect(Collectors.toList()).get(0)));
			} else {
				resulthandler.handle(Future.failedFuture(res.cause()));
				res.cause().printStackTrace();
			}
		});
	}

	@Override
	public void addProduct(Product product, Handler<AsyncResult<String>> resulthandler) {
		client.save("products", toDocument(product), resulthandler);
	}

	@Override
	public void ping(Handler<AsyncResult<String>> resultHandler) {
		resultHandler.handle(Future.succeededFuture("OK"));
	}

	private JsonObject toDocument(Product product) {
		JsonObject document = product.toJson();
		document.put("_id", product.getItemId());
		return document;
	}
}
