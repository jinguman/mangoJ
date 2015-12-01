package com.kit.Service;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.ShardDao;
import com.kit.MongoClient.MongoSimpleClient;
import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoInitialClientService {

	private ShardDao shardDao = null;
	private MongoCollection<Document> collection = null;
	private MongoDatabase database = null;
	private IndexOptions indexOptions = null;
	private Map<String, Object> indexMap;
	
	final Logger logger = LoggerFactory.getLogger(MongoInitialClientService.class);
	
	public MongoInitialClientService(MongoClient client, MongoDatabase database, Map<String, Object> indexMap) {
		
		shardDao = new ShardDao(client, database);
		this.database = database;
		this.indexMap = indexMap;
		
		indexOptions = new IndexOptions();
		indexOptions.background(false);
	}
	
	public void getIndexes() {
		
		MongoCursor<String> cursor = database.listCollectionNames().iterator();
		while(cursor.hasNext()) {
			String s = cursor.next();

			MongoCollection<Document> col = database.getCollection(s);
			MongoCursor<Document> c =  col.listIndexes().iterator();
			
			while(c.hasNext()) {
				Document d = c.next();
				
				String key = d.getString("ns") +".index" + "." + d.getString("name");
				if ( !indexMap.containsKey(key)) {
					indexMap.put(key, true);
				}
			}
		}
	}
	
	public void getShardCollections() {
		
		List<String> keys = shardDao.getShardCollections();

		for(String key : keys)
			indexMap.put(key, true);
	}
	
	public void getShardRange() {
		
		List<String> keys = shardDao.getShardRange();

		for(String key : keys)
			indexMap.put(key, true);
	}
	
	public void doEtIndex(String network, String station, String location, String channel, String year, String month, boolean isBackground) {
		
		String collectionName = Helpers.getTraceCollectionName(network, station, location, channel, year, month);
		collection = database.getCollection(collectionName);
		
		// check map
		String key = collection.getNamespace().getFullName() +".index" + "." + channel + ".et_1";
		if ( indexMap.containsKey(key) ) return;
		indexMap.put(key, true);
		
		// make index
		Document doc = new Document(channel+".et",1);
		indexOptions.background(isBackground);
		
		try {
			String str = collection.createIndex(doc, indexOptions);
			logger.debug("Create index. col: {}, idx: {}, rtn: {}", collectionName, doc.toJson(), str);
		} catch (MongoException e) {
			if ( e.getCode() == 67 ) {
				// too many indexes for collection
				logger.warn("Too many index. col: {}, idx: {}", collectionName, doc.toJson());
			}
		}
		
		
	}
	
	public void doShard(String network, String station, String location, String channel, String year, String month) {

		String collectionName = Helpers.getTraceCollectionName(network, station, location, channel, year, month);
		collection = database.getCollection(collectionName);

		// shardCollection
		String key = collection.getNamespace().getFullName() + ".shardCollection._id";
		if ( !indexMap.containsKey(key) ) {
			indexMap.put(key, true);
			shardDao.shardCollection(collectionName, new Document("_id",1));
		}
		
		// shardRange1
		//key = collection.getNamespace().getFullName() + ".shardRange.ATAG";
		//if ( !indexMap.containsKey(key)) {
		//	indexMap.put(key, true);
		//	shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","0"), new Document("_id","L"), "ATAG");
		//}
		
		// shardRange2
		//key = collection.getNamespace().getFullName() + ".shardRange.BTAG";
		//if ( !indexMap.containsKey(key)) {
		//	indexMap.put(key, true);
		//	shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","M"), new Document("_id","Z"), "BTAG");
		//}
	}
}
