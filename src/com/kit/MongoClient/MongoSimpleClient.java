package com.kit.MongoClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.ShardDao;
import com.kit.Dao.TraceStatsDao;
import com.kit.Util.Helpers;
import com.kit.Util.PropertyManager;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

public class MongoSimpleClient implements Runnable {

	private BlockingQueue<Document> queue;
	private PropertyManager pm;

	private MongoClient client = null;
	private MongoDatabase database = null;
	private MongoDatabase config = null;
	private MongoDatabase admin = null;
	private MongoCollection<Document> collection = null;
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private TraceStatsDao traceStatsDao;
	private ShardDao shardDao;
	private Set<String> indexSet;
	int logThreshold = 0;
	boolean isShard = true;
	int cnt = 0; // log print count
	int restartSec = 5;

	final Logger logger = LoggerFactory.getLogger(MongoSimpleClient.class);
	
	public MongoSimpleClient(BlockingQueue<Document> queue, PropertyManager pm, Set<String> indexSet) {
		this.queue = queue;
		this.pm = pm;
		this.indexSet = indexSet;
		
		client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		database = client.getDatabase(pm.getStringProperty("mongo.database"));
		config = client.getDatabase("config");
		admin = client.getDatabase("admin");
		
		traceStatsDao = new TraceStatsDao(database);
		shardDao = new ShardDao(client, database);
		logThreshold = pm.getIntegerProperty("mc.logthreshold");
		
		isShard = pm.getBooleanProperty("mongo.shard");
		restartSec = pm.getIntegerProperty("mc.restartsec");
	}

	public void run() {
		
		try {
			InsertDocumentPerMinute();
		} catch (ParseException e) {
			logger.error("{}", e);
		} catch (MongoSocketReadException e) {
			logger.info("MongoClient restart after {} seconds.", restartSec);
			try {
				Thread.sleep(restartSec*1000);
			} catch (InterruptedException e1) {
				logger.error("{}",e1);
				//e1.printStackTrace();
			}
		}
	}

	private void InsertDocumentPerMinute() throws ParseException {

		// 에러처리를 정교하게 넣어야 함..
		// ex. 몽고DB에 접속이 끊겼을시 들고있는 큐데이터 처리 등...
		
		
		IndexOptions indexOptions = new IndexOptions();
		indexOptions.background(true);

		UpdateOptions options = new UpdateOptions();

		while(true) {

			Document d = null;
			try {
				d = queue.take();

				String network = d.getString("network");
				String station = d.getString("station");
				String channel = d.getString("channel");
				String location = d.getString("location");
				String st = d.getString("st");
				String et = d.getString("et");

				d.remove("network");
				d.remove("station");
				d.remove("channel");
				d.remove("location");

				String collectionName = network + "_" + station + "_" + location;
				collectionName += "_" + Helpers.getYearString(st, sdfToSecond) + Helpers.getMonthString(st, sdfToSecond); 

				Document key = new Document();
				//key.append("_id", Helpers.convertDate(d.getString("st"), sdfToSecond, sdfToMinute))
				//	.append("sta", station);
				key.append("_id", station + "_" + Helpers.convertDate(d.getString("st"), sdfToSecond, sdfToMinute));

				// get collection
				collection = database.getCollection(collectionName);

				// add index
				String indexKey = collection.getNamespace().getFullName() + "." + channel + ".et";
				addIndex(indexKey, new Document(channel+".et",1), indexOptions);

				if ( isShard ) {

					// add shardCollection
					indexKey = collection.getNamespace().getFullName() + ".shardCollection";
					if ( !indexSet.contains(indexKey)) {
						indexSet.add(indexKey);
						//shardCollection(collectionName, new Document("sta",1));
						shardDao.shardCollection(collectionName, new Document("_id",1));
					}

					// add shardRange
					indexKey = collection.getNamespace().getFullName() + ".rangeATAG";
					if ( !indexSet.contains(indexKey) ) {
						indexSet.add(indexKey);
						//addTagRange(collection.getNamespace().getFullName(), new Document("sta","0"), new Document("sta","L"), "ATAG");
						shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","0"), new Document("_id","L"), "ATAG");
					}
					indexKey = collection.getNamespace().getFullName() + ".rangeBTAG";
					if ( !indexSet.contains(indexKey) ) {
						indexSet.add(indexKey);
						//addTagRange(collection.getNamespace().getFullName(), new Document("sta","M"), new Document("sta","Z"), "BTAG");
						shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","M"), new Document("_id","Z"), "BTAG");
					}
				}
				
				// add trace
				addTrace(key, new Document("$addToSet",new Document(channel,d))
						, options, channel + ".st:" + st);				
				
				//indexKey = collection.getNamespace().getFullName() + ".sta";
				addIndex(indexKey, new Document("sta",1), indexOptions);

				// make stats
				Document keyTraceStatsDoc = new Document()
						.append("_id", network + "_" + station + "_" + location + "_" + channel);
				Document traceStatsDoc = new Document()
						.append("$set", new Document("net", network))
						.append("$set", new Document("sta", station))
						.append("$set", new Document("loc", location))
						.append("$set", new Document("cha", channel))
						.append("$min", new Document("st",st))
						.append("$max", new Document("et",et))
						.append("$set", new Document("it",Helpers.getCurrentUTC(sdfToSecond)) 
												.append("net", network)
												.append("sta", station)
												.append("loc", location)
												.append("cha", channel)
								);

				traceStatsDao.upsertTraceStats(keyTraceStatsDoc, traceStatsDoc);

				d.clear();
			} catch (InterruptedException e) {
				logger.error("{}", e);
				return;
			//} catch ( MongoSocketReadException e) {
				
				// 나중에 처리하자..
				//d.append("network", network);
				//d.remove("station");
				//d.remove("channel");
				//d.remove("location");
				//queue.add(d);
			}
		}	
	}

	private void addIndex(String key, Document doc, IndexOptions options) {
		
		if ( !indexSet.contains(key)) {
			indexSet.add(key);
			collection.createIndex(doc, options);
		} 
	}
	

	private void addTrace(Document key, Document doc, UpdateOptions options, String logStr) {

		UpdateResult result = null;
		try {
			options.upsert(true);
			result = collection.updateOne(key, doc, options);
			if ( cnt > logThreshold) {

				String collectionName = collection.getNamespace().getCollectionName();
				
				if ( result.getModifiedCount() > 0 ) {
					logger.debug("Update trace({}). col: {}, key: {}, {}", queue.size(), collectionName, key.toJson(), logStr); 
				} else if ( result.getUpsertedId() != null ) {
					logger.debug("Insert trace({}). col: {}, key: {}, {}", queue.size(), collectionName, key.toJson(), logStr);
				}
				cnt = 1;
			} else 
				cnt++;
		} catch (MongoWriteException e) {

			if (e.getCode() == 11000) {
				// Duplicate Key Exception
				options.upsert(false);
				collection.updateOne(key, doc, options);
				if ( cnt > logThreshold) {
					
					String collectionName = collection.getNamespace().getCollectionName();
					logger.debug("Update trace({}). col: {}, key: {}, {}", queue.size(), collectionName, key.toJson(), logStr);
					cnt = 1;
				} else {
					cnt++;
				}
			} else {
				logger.warn("Error during write. key: {}, doc: {}", key.toJson(), doc.toJson());
				logger.warn("{}", e);
			}
		}
	}


}
