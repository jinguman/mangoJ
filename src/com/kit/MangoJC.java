package com.kit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MongoClient.MongoSimpleClient;
import com.kit.SeedlinkClient.SeedlinkClient;
import com.kit.Util.PropertyManager;

public class MangoJC {

    public static void main( String[] args ) {
    	// Run configuration.. vm argument: -Dlog4j.configuration="file:./home/config/log4j.xml"
    	Logger logger = LoggerFactory.getLogger(MangoJC.class);
    	logger.info("{}","MangoJC start..");
	
    	// queue
    	BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>();
    	
    	// index set
    	Set<String> indexSet = new HashSet<>();
    	
    	// get property
    	PropertyManager pm = new PropertyManager();
    	
    	// therad array
    	ArrayList<Thread> threads = new ArrayList<Thread>();
    	
    	// MongoSimpleClient Thread
    	int threadCnt = pm.getIntegerProperty("mc.thread");
    	for (int i = 0; i < threadCnt; i++) {
    		MongoSimpleClient msc = new MongoSimpleClient(queue, pm, indexSet);
    		Thread thdMsc = new Thread(msc);
    		threads.add(thdMsc);
    		thdMsc.start();
    	}

    	// SeedlinkClient Thread
    	int slinkThdCnt = pm.getIntegerProperty("sc.thread");
    	for(int i=1; i<slinkThdCnt+1; i++) {

    		String[] networks = pm.getStringListProperty("sc." + i + ".network");
        	for(String network:  networks) {
        		SeedlinkClient sc = new SeedlinkClient(queue, pm);
        		sc.setNetwork(network);
        		sc.setStation(pm.getStringProperty("sc." + i + ".station"));
        		sc.setChannel(pm.getStringProperty("sc." + i + ".channel"));
        		sc.setVerbose(pm.getBooleanProperty("sc." + i + ".verbose"));
        		
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
