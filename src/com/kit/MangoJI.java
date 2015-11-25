package com.kit;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MongoClient.MongoInitialClient;
import com.kit.SeedlinkClient.SeedlinkStreamClient;
import com.kit.Service.MongoInitialClientService;
import com.kit.Util.PropertyManager;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class MangoJI {

    public static void main( String[] args ) {
    	// Run configuration.. vm argument: -Dlog4j.configuration="file:./home/config/log4j.xml"
    	Logger logger = LoggerFactory.getLogger(MangoJI.class);
    	logger.info("{}","MangoJI start..");

    	// queue
    	BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>();

    	// index map
    	Map<String, Object> indexMap = new ConcurrentHashMap<>();
    	
    	// get property
    	PropertyManager pm = new PropertyManager();

    	// therad array
    	ArrayList<Thread> threads = new ArrayList<Thread>();

    	// get argument
    	if ( args.length != 2) {
    		System.out.println("Usage: YYYY MM");
    		return;
    	}
    	
    	String shardYear = args[0];
    	String shardMonth = args[1];
    	
    	// getIndexes, shardCollection, shardRange
    	MongoClient client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		MongoDatabase database = client.getDatabase(pm.getStringProperty("mongo.database"));
    	MongoInitialClientService mics = new MongoInitialClientService(client, database, indexMap);
    	mics.getIndexes();
    	logger.debug("getIndexes from mongodb");
    	mics.getShardCollections();
    	logger.debug("getShardcollections from mongodb");
    	//mics.getShardRange();
    	//logger.debug("getShardRange from mongodb");
    	
    	// MongoShardClient Thread
    	int threadCnt = pm.getIntegerProperty("mi.thread");
    	for (int i = 0; i < threadCnt; i++) {
    		MongoInitialClient msc = new MongoInitialClient(queue, pm, shardYear, shardMonth, indexMap);
    		Thread thdMsc = new Thread(msc);
    		threads.add(thdMsc);
    		thdMsc.start();
    	}
    	
    	// SeedlinkShardClient Thread
    	int slinkThdCnt = pm.getIntegerProperty("sc.thread");
    	if ( pm.getBooleanProperty("mi.buildentirelist")) slinkThdCnt = 1;
    	
    	for(int i=1; i<slinkThdCnt+1; i++) {

    		String[] networks = pm.getStringListProperty("sc." + i + ".network");
    		SeedlinkStreamClient ssc = new SeedlinkStreamClient(queue, pm);
    		ssc.setNetworks(networks);

    		Thread thdSlink = new Thread(ssc);
        	threads.add(thdSlink);
        	thdSlink.start();
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

