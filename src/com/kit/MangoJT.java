package com.kit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MongoClient.MongoSimpleClient;
import com.kit.Service.GenerateTrace;
import com.kit.Util.PropertyManager;

/**
 * Hello world!
 *
 */
public class MangoJT {
    
	public static void main( String[] args ) {
    	// Run configuration.. vm argument: -Dlog4j.configuration="file:./home/config/log4j.xml"
    	Logger logger = LoggerFactory.getLogger(MangoJT.class);
    	logger.info("{}","MangoJT start..");
    	
    	// queue
    	BlockingQueue<Document> queue = new LinkedBlockingQueue<Document>();
    	
    	// index set
    	Map<String, Object> indexMap = new ConcurrentHashMap();
    	
    	// get property
    	PropertyManager pm = new PropertyManager();
    	
    	// therad array
    	ArrayList<Thread> threads = new ArrayList<Thread>();
    	
    	// MongoClient Thread
    	int mcThreadCnt = pm.getIntegerProperty("mc.thread"); 
    	for (int i = 0; i < mcThreadCnt; i++) {
    		MongoSimpleClient ip = new MongoSimpleClient(queue, pm, indexMap);
    		Thread thdIp = new Thread(ip);
    		threads.add(thdIp);
    		thdIp.start();
    	}
    	
    	// Generator Thread
    	GenerateTrace gt = new GenerateTrace(queue, pm);

    	gt.setNsamp(pm.getIntegerProperty("gt.nsamp"));
    	gt.setNetwork(pm.getStringProperty("gt.network"));
    	gt.setStations(pm.getStringListProperty("gt.station"));
    	gt.setChannels(pm.getStringListProperty("gt.channel"));
    	
    	Thread thdGt = new Thread(gt);
    	threads.add(thdGt);
    	thdGt.start();

    }

}
