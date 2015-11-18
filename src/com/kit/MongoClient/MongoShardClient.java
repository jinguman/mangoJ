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

public class MongoShardClient implements Runnable {

	private PropertyManager pm;

	private MongoClient client = null;
	private MongoDatabase database = null;
	private MongoCollection<Document> collection = null;
	private SeedlinkClientService scs = null;
	private ShardDao shardDao;
	int logThreshold = 0;
	int scRestartSec = 5;
	int mcRestartSec = 5;
	Document streamsInfoDoc = null;

	final Logger logger = LoggerFactory.getLogger(MongoShardClient.class);
	
	public MongoShardClient(BlockingQueue<Document> queue, PropertyManager pm) {
		this.pm = pm;
		
		client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		database = client.getDatabase(pm.getStringProperty("mongo.database"));

		shardDao = new ShardDao(client, database);
		
		scRestartSec = pm.getIntegerProperty("sc.restartsec");
		mcRestartSec = pm.getIntegerProperty("mc.restartsec");
		
		// Don't need queue
		scs = new SeedlinkClientService(null, pm);
	}

	public void run() {
		
		// get stream info
		while(true) {
			try {
				streamsInfoDoc = scs.getStreamsInfo();
				break;
			} catch (SeedlinkException | SeedFormatException | IOException e) {
				logger.info("SeedlinkClient restart after {} seconds.", scRestartSec);
				logger.error("{}",e);

				try {
					Thread.sleep(scRestartSec*1000);
				} catch (InterruptedException e1) {
					logger.error("{}",e1);
				}
			}
		}
		
		try {
			addShard();
		} catch (MongoSocketReadException e) {
			logger.info("MongoClient restart after {} seconds.", mcRestartSec);
			try {
				Thread.sleep(mcRestartSec*1000);
			} catch (InterruptedException e1) {
				logger.error("{}",e1);
			}
		}
	}

	private void addShard() {
		
	}
	
	private void doShard() {
/*
		String collectionName = network + "_" + station + "_" + location;
		collectionName += "_" + Helpers.getYearString(st, sdfToSecond) + Helpers.getMonthString(st, sdfToSecond);
		
		// add shardCollection
		String indexKey = collection.getNamespace().getFullName() + ".shardCollection";
		shardDao.shardCollection(collectionName, new Document("_id",1));

		// add shardRange
		indexKey = collection.getNamespace().getFullName() + ".rangeATAG";
		shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","0"), new Document("_id","L"), "ATAG");

		indexKey = collection.getNamespace().getFullName() + ".rangeBTAG";
		shardDao.addTagRange(collection.getNamespace().getFullName(), new Document("_id","M"), new Document("_id","Z"), "BTAG");
*/		
		
	}

}
