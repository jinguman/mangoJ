package com.kit.Service;

import org.bson.Document;

import com.kit.Dao.ShardDao;
import com.kit.Util.Helpers;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoInitialClientService {

	private ShardDao shardDao = null;
	private MongoCollection<Document> collection = null;
	private MongoDatabase database = null;
	
	public MongoInitialClientService() {
		
		//shardDao = new ShardDao(client, database);
	}
	
	public void doIndex(String network, String station, String location, String channel) {
		
	}
	
	public void doShard(String network, String station, String location, String year, String month) {

		String collectionName = Helpers.getTraceCollectionName(network, station, location, year, month);
		collection = database.getCollection(collectionName);
		
		// add shardCollection
		//String indexKey = collection.getNamespace().getFullName() + ".shardCollection";
		shardDao.shardCollection(collectionName, new Document("_id",1));

		// add shardRange
		//indexKey = collection.getNamespace().getFullName() + ".rangeATAG";
		shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","0"), new Document("_id","L"), "ATAG");

		//indexKey = collection.getNamespace().getFullName() + ".rangeBTAG";
		shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","M"), new Document("_id","Z"), "BTAG");
	
		
	}
}
