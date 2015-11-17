package com.kit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MongoClient.MongoShardClient;
import com.kit.MongoClient.MongoSimpleClient;
import com.kit.SeedlinkClient.SeedlinkClient;
import com.kit.SeedlinkClient.SeedlinkStreamClient;
import com.kit.Util.PropertyManager;

public class MangoJI {

    public static void main( String[] args ) {
    	// Run configuration.. vm argument: -Dlog4j.configuration="file:./home/config/log4j.xml"
    	Logger logger = LoggerFactory.getLogger(MangoJI.class);
    	logger.info("{}","MangoJI start..");
	
    	// queue
    	BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>();
    	
    	// get property
    	PropertyManager pm = new PropertyManager();
    	
    	// therad array
    	ArrayList<Thread> threads = new ArrayList<Thread>();
    	
    	// MongoShardClient Thread
    	/*
    	int threadCnt = pm.getIntegerProperty("ms.thread");
    	for (int i = 0; i < threadCnt; i++) {
    		MongoSimpleClient msc = new MongoSimpleClient(queue, pm, indexSet);
    		Thread thdMsc = new Thread(msc);
    		threads.add(thdMsc);
    		thdMsc.start();
    	}
    	*/

    	// SeedlinkShardClient Thread
    	int slinkThdCnt = pm.getIntegerProperty("sc.thread");
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
