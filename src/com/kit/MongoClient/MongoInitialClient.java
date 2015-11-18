package com.kit.MongoClient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.ShardDao;
import com.kit.Dao.TraceStatsDao;
import com.kit.Service.SeedlinkClientService;
import com.kit.Util.Helpers;
import com.kit.Util.MangoJCode;
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

import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;

public class MongoInitialClient implements Runnable {

	private PropertyManager pm;
	private BlockingQueue<Document> queue;

	private MongoClient client = null;
	private MongoDatabase database = null;
	private MongoCollection<Document> collection = null;
	private SeedlinkClientService scs = null;
	private String shardYear, shardMonth;
	private ShardDao shardDao;
	int logThreshold = 0;
	int scRestartSec = 5;
	int mcRestartSec = 5;
	Document streamsInfoDoc = null;

	final Logger logger = LoggerFactory.getLogger(MongoInitialClient.class);
	
	public MongoInitialClient(BlockingQueue<Document> queue, PropertyManager pm, String shardYear, String shardMonth) {
		this.pm = pm;
		
		client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		database = client.getDatabase(pm.getStringProperty("mongo.database"));
		shardDao = new ShardDao(client, database);
		
		scRestartSec = pm.getIntegerProperty("sc.restartsec");
		mcRestartSec = pm.getIntegerProperty("mc.restartsec");
		this.shardYear = shardYear;
		this.shardMonth = shardMonth;
		this.queue = queue;
	}

	public void run() {

		try {
			addInitial();
		} catch (MongoSocketReadException | InterruptedException e) {
			logger.info("MongoInitialClient restart after {} seconds.", mcRestartSec);
			try {
				Thread.sleep(mcRestartSec*1000);
			} catch (InterruptedException e1) {
				logger.error("{}",e1);
			}
		}
	}

	private void addInitial() throws InterruptedException {
		while(true) {
			Document doc = queue.take();
			
			String network = doc.getString("network");
			String station = doc.getString("station");
			String location = doc.getString("location");
			String channel = doc.getString("channel");
			
			doIndex(network, station, location, channel);
			doShard(network, station, location, shardYear, shardMonth);
			logger.debug("execute shard({}).", queue.size());
		}
	}
	
	private void doIndex(String network, String station, String location, String channel) {
		
	}
	
	private void doShard(String network, String station, String location, String year, String month) {

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
