package com.kit.MongoClient;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.ShardDao;
import com.kit.Service.MongoInitialClientService;
import com.kit.Service.SeedlinkClientService;
import com.kit.Util.PropertyManager;
import com.kit.Vo.SLState;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoSocketReadException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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
	int miRestartSec = 5;
	Document streamsInfoDoc = null;
	MongoInitialClientService mics = null; 
	int sleepSec = -1;

	final Logger logger = LoggerFactory.getLogger(MongoInitialClient.class);
	
	public MongoInitialClient(BlockingQueue<Document> queue, PropertyManager pm, String shardYear, String shardMonth, SLState state) {
		this.pm = pm;
		
		client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		database = client.getDatabase(pm.getStringProperty("mongo.database"));
		shardDao = new ShardDao(client, database);
		
		scRestartSec = pm.getIntegerProperty("sc.restartsec");
		miRestartSec = pm.getIntegerProperty("mi.restartsec");
		this.shardYear = shardYear;
		this.shardMonth = shardMonth;
		this.queue = queue;
		mics = new MongoInitialClientService(client, database, state);
		sleepSec = pm.getIntegerProperty("mi.sleepsec");
	}

	public void run() {

		try {
			addInitial();
		} catch (MongoSocketReadException | InterruptedException e) {
			logger.info("MongoInitialClient restart after {} seconds.", miRestartSec);
			try {
				Thread.sleep(miRestartSec*1000);
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
			
			if ( pm.getBooleanProperty("mi.index")) mics.doEtIndex(network, station, location, channel, shardYear, shardMonth, false);
			if ( pm.getBooleanProperty("mi.shard")) mics.doShard(network, station, location, channel, shardYear, shardMonth);
			logger.info("Execute initiate.({}).", queue.size());

			if ( sleepSec > 0 ) Thread.sleep(sleepSec*1000);
		}
	}

}
