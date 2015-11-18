package com.kit.Service;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.ShardDao;
import com.kit.MongoClient.MongoSimpleClient;
import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoInitialClientService {

	private ShardDao shardDao = null;
	private MongoCollection<Document> collection = null;
	private MongoDatabase database = null;
	
	final Logger logger = LoggerFactory.getLogger(MongoInitialClientService.class);
	
	public MongoInitialClientService(MongoClient client, MongoDatabase database) {
		
		shardDao = new ShardDao(client, database);
		this.database = database;
	}
	
	public void doIndex(String network, String station, String location, String channel, String year, String month) {
		IndexOptions indexOptions = new IndexOptions();
		indexOptions.background(true);
		
		String collectionName = Helpers.getTraceCollectionName(network, station, location, year, month);
		collection = database.getCollection(collectionName);
		
		Document doc = new Document(channel+".et",1);
		String str = collection.createIndex(doc, indexOptions);
		logger.debug("Create initial index. col: {}, idx: {}, rtn: {}", collectionName, doc.toJson(), str);
		
	}
	
	public void doShard(String network, String station, String location, String year, String month) {

		String collectionName = Helpers.getTraceCollectionName(network, station, location, year, month);
		collection = database.getCollection(collectionName);
		
		// add shardCollection
		shardDao.shardCollection(collectionName, new Document("_id",1));
		
		// add shardRange
		shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","0"), new Document("_id","L"), "ATAG");
		shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","M"), new Document("_id","Z"), "BTAG");
	
		
	}
}
