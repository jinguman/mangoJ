package com.kit.MongoClient;

import java.text.ParseException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Service.MongoSimpleClientService;
import com.kit.Util.PropertyManager;
import com.kit.Vo.SLState;
import com.mongodb.MongoSocketReadException;
import com.mongodb.client.result.UpdateResult;

public class MongoSimpleClient implements Runnable {

	private BlockingQueue<Document> queue;
	int logThreshold = 0;
	int cnt = 0; // log print count
	int restartSec = 5;

	private MongoSimpleClientService mscs;
	
	final Logger logger = LoggerFactory.getLogger(MongoSimpleClient.class);
	
	public MongoSimpleClient(BlockingQueue<Document> queue, PropertyManager pm, SLState state) {
		this.queue = queue;

		logThreshold = pm.getIntegerProperty("mc.logthreshold");
		restartSec = pm.getIntegerProperty("mc.restartsec");
		mscs = new MongoSimpleClientService(pm, state);
		
		// get random start cnt
		Random random = new Random();
		if ( logThreshold > 0 ) cnt = random.nextInt(logThreshold);
		
	}

	public void run() {
		
		// 에러처리를 정교하게 넣어야 함..
		// ex. 몽고DB에 접속이 끊겼을시 들고있는 큐데이터 처리 등...
		
		// get station list

		while(true) {

			Document d = null;
			try {
				d = queue.take();
				
				String network = d.getString("network");
				String station = d.getString("station");
				String channel = d.getString("channel");
				String location = d.getString("location");
				String st = d.getString("st");
				
				UpdateResult result = mscs.insertTraceRaw(d);
				
				String logStr = network + "." + station + "." + location + "." + channel + " " + st;
				printTraceLog(result, logStr);
				d.clear();

			} catch (ParseException e) {
				
			} catch (MongoSocketReadException | InterruptedException e) {
				logger.error("{}", e);
				logger.info("MongoClient restart after {} seconds.", restartSec);
				try {
					Thread.sleep(restartSec*1000);
				} catch (InterruptedException e1) {
					logger.error("{}",e1);
					//e1.printStackTrace();
				}
			} 
			//} catch ( MongoSocketReadException e) {
				
				// 나중에 처리하자..
				//d.append("network", network);
				//d.remove("station");
				//d.remove("channel");
				//d.remove("location");
				//queue.add(d);

		}	
		
	}

	private void printTraceLog(UpdateResult result, String logStr) {

		if ( result == null || logStr.isEmpty() ) return;

		if ( cnt > logThreshold) {

			if ( result.getModifiedCount() > 0 ) {
				logger.debug("Update trace({}). {}", queue.size(), logStr); 
			} else if ( result.getUpsertedId() != null ) {
				logger.debug("Insert trace({}). {}", queue.size(), logStr);
			}
			cnt = 1;
		} else 
			cnt++;
	}

}
