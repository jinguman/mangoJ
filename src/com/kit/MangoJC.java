package com.kit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MongoClient.MongoSimpleClient;
import com.kit.SeedlinkClient.SeedlinkClient;
import com.kit.Service.MongoInitialClientService;
import com.kit.Util.PropertyManager;
import com.kit.Vo.SLState;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MangoJC {

    public static void main( String[] args ) {
    	// Run configuration.. vm argument: -Dlog4j.configuration="file:./home/config/log4j.xml"
    	Logger logger = LoggerFactory.getLogger(MangoJC.class);
    	logger.info("{}","MangoJC start..");
	
    	// queue
    	BlockingQueue<List<Document>> queue = new LinkedBlockingQueue<List<Document>>();
    	
    	// index map
    	// 1. INDEX(ns.index.indexName : boolean)
    	//    trace.AZ_BASP_00_2015.index.HNE.et_1 : true
    	// 2. SHARD(ns.shardCollection.shardName : boolean)
    	//    trace.AZ_BASP_00_2015.shardCollection._id : true
    	// 3. SHARD RANGE(ns.shardRange.rangeName : boolean)
    	//    trace.AZ_BASP_00_2015.shardRange.ATAG : true
    	// 4. TIME(ns.collection.et : yyyy-mm-ddTHH:mm:SS.SSSS)  ... not yet implemented ....
    	//    trace.AZ_BASP_00_2015.et : 2015-01-01T01:01:00.0000
    	//Map<String, Object> indexMap = new ConcurrentHashMap<>();
    	SLState state = new SLState();
    	
    	// get property
    	PropertyManager pm = new PropertyManager();

    	// getIndexes, shardCollection, shardRange
    	MongoClient client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		MongoDatabase database = client.getDatabase(pm.getStringProperty("mongo.database"));
    	MongoInitialClientService mics = new MongoInitialClientService(client, database, state);
    	mics.getIndexes();
    	logger.debug("getIndexes from mongodb");
    	mics.getShardCollections();
    	logger.debug("getShardcollections from mongodb");
    	mics.getShardRange();
    	logger.debug("getShardRange from mongodb");
    	
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					stopEngine();
				} catch (Exception e) {
					logger.error("There is error in Server.",e);
				}
			}

			private void stopEngine() {
				logger.error("STOP ENGINE~~~");
				
			}
		});
    	
    	// therad array
    	ArrayList<Thread> threads = new ArrayList<Thread>();
    	
    	// MongoSimpleClient Thread
    	logger.info("MongoDB thread start...");
    	int threadCnt = pm.getIntegerProperty("mc.thread");
    	for (int i = 0; i < threadCnt; i++) {
    		MongoSimpleClient msc = new MongoSimpleClient(queue, pm, state);
    		Thread thdMsc = new Thread(msc);
    		threads.add(thdMsc);
    		thdMsc.start();
    	}

    	// SeedlinkClient Thread
    	logger.info("Seedlink thread start...");
    	int slinkThdCnt = pm.getIntegerProperty("sc.thread");
    	for(int i=1; i<slinkThdCnt+1; i++) {

    		String[] networks = pm.getStringListProperty("sc." + i + ".network");
        	for(String network:  networks) {
        		SeedlinkClient sc = new SeedlinkClient(queue, pm);
        		sc.setNetwork(network);
        		sc.setStation(pm.getStringProperty("sc." + i + ".station"));
        		sc.setChannel(pm.getStringProperty("sc." + i + ".channel"));
        		//sc.setVerbose(pm.getBooleanProperty("sc." + i + ".verbose"));
        		String ip = pm.getStringProperty("sc." + i + ".ip");
        		if ( ip != null ) sc.setHost(ip);
        		int port = pm.getIntegerProperty("sc." + i + ".port");
        		if ( port > 0 ) sc.setPort(port);
        		//sc.setStart(pm.getStringProperty("sc." + i + ".start"));	// yesr,month,day,hour,min,sec
        		
            	Thread thdSlink = new Thread(sc);
            	threads.add(thdSlink);
            	thdSlink.start();
        	}
    	}
    	
    	// Thread Join
		for (int i = 0; i < threads.size(); i++) {
			Thread t = threads.get(i);
			try {
				t.join();
			} catch (Exception e) {
			}
		}
		

    }
}
