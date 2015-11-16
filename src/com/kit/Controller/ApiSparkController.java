package com.kit.Controller;

import static spark.Spark.get;
import static spark.Spark.port;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Util.PropertyManager;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import spark.Request;
import spark.Response;
import spark.Route;

@Deprecated
public class ApiSparkController {

	private PropertyManager pm;
	final Logger logger = LoggerFactory.getLogger(ApiSparkController.class);

	public ApiSparkController() throws IOException {

		final MongoClient mongoClient = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
        final MongoDatabase traceDatabase = mongoClient.getDatabase(pm.getStringProperty("mongo.database"));
        
		port(pm.getIntegerProperty("ac.port"));
		initializeRoutes();
	}

	private void initializeRoutes() throws IOException {

        get("/", new Route() {
			
			public Object handle(Request req, Response res) throws Exception {
				System.out.println("hello");
				return "Test";
			}
		});
        
        get("/hello", (request, response) -> "Hello World!");
		
	}
}
