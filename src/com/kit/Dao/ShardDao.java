package com.kit.Dao;

import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.unwind;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class ShardDao {

	final Logger logger = LoggerFactory.getLogger(ShardDao.class);
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoDatabase admin;
	private MongoDatabase config;
	
	public ShardDao(MongoClient client, MongoDatabase database) {
		this.client = client;
		this.database = database;
		admin = client.getDatabase("admin");
		config = client.getDatabase("config");
	}
	
	public List<String> getShardCollections() {
		
		List<String> list = new ArrayList<>();
		
		List<Document> docs = new ArrayList<>();
		config.getCollection("collections").find().into(docs);
		
		for(Document doc : docs) {
			
			String ns = doc.getString("_id");
			Document keyDoc = (Document) doc.get("key");
			
			for(String key : keyDoc.keySet()) {
				list.add(ns + ".shardCollection." + key);
			}
		}
		return list;
	}
	
	public List<String> getShardRange() {
		
		List<String> list = new ArrayList<>();
		
		List<Document> docs = new ArrayList<>();
		config.getCollection("tags").find().into(docs);
		
		for(Document doc : docs) {
			
			String ns = doc.getString("ns");
			String tag = doc.getString("tag");
			list.add(ns + ".shardRange." + tag);
		}
		return list;
	}
	
	public void shardCollection(String collectionName, Document key) {
		
		String databaseName = database.getName();

		// make shardCollection
		try {
			Document d = admin.runCommand(new Document("shardCollection", databaseName + "." + collectionName)
					.append("key", key).append("unique", "true")
				);
			logger.debug("Update shardCollection. {}", d.toJson());
		} catch (MongoCommandException e) {
			logger.debug("Update shardCollection. {}", e.getMessage());
		}
	}
	
	public void addTagRange(String ns, Document min, Document max, String tag) {

		MongoCollection<Document> tags = config.getCollection("tags");

		UpdateOptions options = new UpdateOptions();
		options.upsert(true);
	
		Document key = new Document("_id", 
				new Document("ns", ns).append("min", min)
				);
		Document doc = new Document()
				.append("ns", ns)
				.append("min", min)
				.append("max", max)
				.append("tag", tag);

		long l = tags.count(key);
		if ( l > 0 ) {
			// already exist
			logger.debug("Update tagRange. Already exists.");
			return;
		}

		try {
			UpdateResult result = tags.updateOne(key, new Document("$set", doc), options);
			logger.debug("Update tagRange. {}", result);
		} catch (MongoWriteException e) {
			logger.debug("Update tagRange. {}", e.getMessage());
		}
		
	}
}
