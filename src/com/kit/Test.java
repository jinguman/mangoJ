package com.kit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;

import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class Test {

	public static void main(String[] args) throws ParseException {
		
		
		String str = "true";
		boolean a = Boolean.valueOf("true");
		System.out.println(a);
		
		/*
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40:12800"));
		MongoDatabase database = client.getDatabase("test");
		
		MongoCollection<Document> collection = null;
		
		Document key = new Document();
		key.append("_id", "2015-01-01");

		// get collection
		collection = database.getCollection("test");
		
		String channel = "HHN";
		
		Document d = new Document().append("t", 12);

		UpdateOptions options = new UpdateOptions();
		options.upsert(true);

		UpdateResult result = collection.updateOne(key, new Document("$addToSet",new Document(channel,d)), options);
		System.out.println(result.isModifiedCountAvailable() + ", " + result.getMatchedCount() + ", " + result.getModifiedCount() + ", " + result.getUpsertedId());

		// first
		// true, 0, 0, BsonString{value='2015-01-01'}
		
		// second
		// true, 1, 1, null
		 * 
		 * */
		 
		
		// TEST2. shard command
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40:12800"));
		MongoDatabase admin = client.getDatabase("admin");
		MongoDatabase trace = client.getDatabase("jobs");
		MongoDatabase config = client.getDatabase("config");
		
		try {
			Document result = admin.runCommand(new Document("enableSharding", "jobs2"));
			System.out.println(result.toJson());
		} catch (MongoCommandException e) {}
		
		// { "ok" : 1.0 }
		// MongoCommandException 
		// Command failed with error -1: 'already enabled' on server 192.168.5.40:12800. The full response is { "ok" : 0.0, "errmsg" : "already enabled" }
		
		// Example
		String database = "jobs";
		String collection = "AA";
		
		// create index
		Document shardKey = new Document("sta", 1);
		MongoCollection<Document> coll = trace.getCollection(collection);
		coll.createIndex(shardKey);
		
		// sharding setting
		// sh.shardCollection('trace.collection',{sta:1});
		// sh.addTagRange('trace.collection',{sta:'0'},{sta:'L'},'ATAG')
		// sh.addTagRange('trace.collection',{sta:'M'},{sta:¡®Z'},'BTAG')
		
		try {
			Document doc2 = admin.runCommand(new Document("shardCollection", database + "." + collection)
					.append("key", new Document("sta",1))
				);
			System.out.println(doc2.toJson());
		} catch (MongoCommandException e) {}
		
		// result
		// { "ok" : 1.0 }
		// com.mongodb.MongoCommandException: Command failed with error -1: 'already sharded' on server 192.168.5.40:12800. The full response is { "ok" : 0.0, "errmsg" : "already sharded" }
		
		
		// add range tag
		MongoCollection<Document> tags = config.getCollection("tags");
		
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);
		
		String ns = "jobs." + collection;
		Document minA = new Document("sta", "0");
		Document maxA = new Document("sta", "L");
		String tagA = "ATAG";
		
		Document minB = new Document("sta", "M");
		Document maxB = new Document("sta", "Z");
		String tagB = "BTAG";
		
		
		Document keyA = new Document("_id", 
				new Document("ns", ns).append("min", minA)
				);
		Document docA = new Document()
				.append("ns", ns)
				.append("min", minA)
				.append("max", maxA)
				.append("tag", tagA);
		
		Document keyB = new Document("_id", 
				new Document("ns", ns).append("min", minB)
				);
		Document docB = new Document()
				.append("ns", ns)
				.append("min", minB)
				.append("max", maxB)
				.append("tag", tagB);
		
		
		
		//System.out.println(key.toJson());
		//System.out.println(doc.toJson());
		
		tags.updateOne(keyA, new Document("$set", docA), options);
		
		tags.updateOne(keyB, new Document("$set", docB), options);
		
		long l = tags.count(keyA);
		System.out.println("count: " + l);
		
		System.out.println(trace.getName());
		
		System.out.println(tags.getNamespace().getFullName());
	}
	
	
	

}
