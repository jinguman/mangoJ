package com.kit.Dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

/**
 * shard와 관련된 명령을 처리하는 DAO
 * @author jman
 *
 */
public class ShardDao {

	final Logger logger = LoggerFactory.getLogger(ShardDao.class);
	
	private MongoClient client;
	private MongoDatabase database;
	private MongoDatabase admin;
	private MongoDatabase config;
	
	/**
	 * 생성자
	 * @param client mongodbClient 객체
	 * @param database mongodbDatabase 객체
	 */
	public ShardDao(MongoClient client, MongoDatabase database) {
		this.client = client;
		this.database = database;
		admin = client.getDatabase("admin");
		config = client.getDatabase("config");
	}
	
	/**
	 * mongodb에서 shardCollection된 collection리스트를 가져온다.
	 * @return shardCollection된 collection의 리스트
	 */
	public List<String> getShardCollections() {
		
		List<String> list = new ArrayList<>();
		
		List<Document> docs = new ArrayList<>();
		config.getCollection("collections").find().into(docs);
		
		for(Document doc : docs) {
			
			String ns = doc.getString("_id");
			Document keyDoc = (Document) doc.get("key");
			
			for(String key : keyDoc.keySet()) {
				list.add(ns + key);
			}
		}
		return list;
	}
	
	/**
	 * mongodb에서 shardRange된 collection리스트를 가져온다.
	 * @return shardRange된 collection의 리스트
	 */
	public List<String> getShardRange() {
		
		List<String> list = new ArrayList<>();
		
		List<Document> docs = new ArrayList<>();
		config.getCollection("tags").find().into(docs);
		
		for(Document doc : docs) {
			
			String ns = doc.getString("ns");
			String tag = doc.getString("tag");
			list.add(ns + tag);
		}
		return list;
	}
	
	/**
	 * shardCollection명령을 수행한다.
	 * @param collectionName collection명
	 * @param key collection 키
	 */
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
	
	/**
	 * TagRange명령을 수행한다.
	 * @param ns mongodb namespace
	 * @param min range 중 최소값
	 * @param max range 중 최대값
	 * @param tag 태그명
	 */
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
