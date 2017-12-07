package com.redhat.coolstore.catalog.verticle.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.coolstore.catalog.model.Product;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class CatalogServiceTest extends MongoTestBase {

	private Vertx vertx;

	@Before
	public void setup(TestContext context) throws Exception {
		vertx = Vertx.vertx();
		vertx.exceptionHandler(context.exceptionHandler());
		JsonObject config = getConfig();
		mongoClient = MongoClient.createNonShared(vertx, config);
		Async async = context.async();
		dropCollection(mongoClient, "products", async, context);
		async.await(10000);
	}

	@After
	public void tearDown() throws Exception {
		mongoClient.close();
		vertx.close();
	}

	@Test
	public void testAddProduct(TestContext context) throws Exception {
		String itemId = "999999";
		String name = "productName";
		Product product = new Product();
		product.setItemId(itemId);
		product.setName(name);
		product.setDesc("productDescription");
		product.setPrice(100.0);

		CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

		Async async = context.async();

		service.addProduct(product, ar -> {
			if (ar.failed()) {
				context.fail(ar.cause().getMessage());
			} else {
				JsonObject query = new JsonObject().put("_id", itemId);
				mongoClient.findOne("products", query, null, ar1 -> {
					if (ar1.failed()) {
						context.fail(ar1.cause().getMessage());
					} else {
						assertThat(ar1.result().getString("name"), equalTo(name));
						async.complete();
					}
				});
			}
		});
	}

	// @Test
	public void testGetProducts(TestContext context) throws Exception {
		// ----
		// To be implemented
		//
		// In your test:
		// -Insert two or more products in MongoDB. Use the `MongoClient.save` method to
		// do so.
		// - Retrieve the products from Mongo using the `testGetProducts` method.
		// - Verify that no failures happened,
		// that the number of products retrieved corresponds to the number inserted,
		// and that the product values match what was inserted.
		//
		// ----
		Async async = context.async(2);

		Product product1 = new Product();
		product1.setItemId("00001");
		product1.setName("Name-1");
		product1.setDesc("productDescription-1");
		product1.setPrice(100.0);
		JsonObject pjson1 = product1.toJson().put("_id", "00001");
		mongoClient.save("products", pjson1, res -> {
			if (res.failed()) {
				context.fail();
			}
			async.countDown();
		});

		Product product2 = new Product();
		product2.setItemId("00002");
		product2.setName("Name-2");
		product2.setDesc("productDescription-2");
		product2.setPrice(120.0);
		JsonObject pjson2 = product2.toJson().put("_id", "00002");
		mongoClient.save("products", pjson2, res -> {
			if (res.failed()) {
				context.fail();
			}
			async.countDown();
		});

		async.await();

		CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

		service.getProducts(handler -> {
			if (handler.succeeded()) {
				List<Product> products = handler.result();
				assertEquals(2, products.size());
				Product p1 = products.stream().filter(x -> x.getItemId().equals("00001")).findFirst().get();
				assertEquals("00001", p1.getItemId());
				assertEquals("Name-1", p1.getName());
				assertEquals("productDescription-1", p1.getDesc());
				assertEquals(100.0d, p1.getPrice(), 0);

				Product p2 = products.stream().filter(x -> x.getItemId().equals("00002")).findFirst().get();
				assertEquals("00002", p2.getItemId());
				assertEquals("Name-2", p2.getName());
				assertEquals("productDescription-2", p2.getDesc());
				assertEquals(120.0d, p2.getPrice(), 0);
			} else {
				fail("Error retrieve products.");
			}
		});
	}

	@Test
	public void testGetProduct(TestContext context) throws Exception {
		// ----
		// To be implemented
		//
		// ----
		Async async = context.async();

		Product product1 = new Product();
		product1.setItemId("00001");
		product1.setName("Name-1");
		product1.setDesc("productDescription-1");
		product1.setPrice(100.0);
		JsonObject pjson1 = product1.toJson().put("_id", "00001");
		mongoClient.save("products", pjson1, res -> {
			if (res.failed()) {
				context.fail();
			}
			async.complete();
			;
		});
		async.await();

		CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

		service.getProduct("00001", handler -> {
			if (handler.succeeded()) {
				Product p1 = handler.result();
				assertEquals("00001", p1.getItemId());
				assertEquals("Name-1", p1.getName());
				assertEquals("productDescription-1", p1.getDesc());
				assertEquals(100.0d, p1.getPrice(), 0);

			} else {
				fail("Error retrieve products.");
			}
		});
	}

	@Test
	public void testGetNonExistingProduct(TestContext context) throws Exception {
		// ----
		// To be implemented
		//
		// ----
		CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

		service.getProduct("00001", handler -> {
			if (handler.succeeded()) {
				assertNull(handler.result());
			} else {
				fail("Error retrieve products.");
			}
		});
	}

	@Test
	public void testPing(TestContext context) throws Exception {
		CatalogService service = new CatalogServiceImpl(vertx, getConfig(), mongoClient);

		Async async = context.async();
		service.ping(ar -> {
			assertThat(ar.succeeded(), equalTo(true));
			async.complete();
		});
	}

}
