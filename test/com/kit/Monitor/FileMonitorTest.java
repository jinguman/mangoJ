package com.kit.Monitor;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class FileMonitorTest {

	@Test
	public void test() {

		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		MongoDatabase database = client.getDatabase("trace");
		
		BckupWorker worker = new BckupWorker(client, database);
		FileListener listener = new FileListener();
		listener.setBckupWorker(worker);
		
		FileMonitor monitor = new FileMonitor();
		monitor.setListener(listener);
		
		monitor.start();
		
		while(true);
	}

}
