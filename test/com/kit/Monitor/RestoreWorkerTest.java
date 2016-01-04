package com.kit.Monitor;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.kit.Util.PropertyManager;
import com.kit.Vo.SLState;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class RestoreWorkerTest {

	// file contents
	/*
	#COMMENT ST ET NET STA LOC CHA DOC (enable char *)
	d:/temp/
	*/
	
	@Test
	public void test() {
		
		// -Dlog4j.configuration="file:./home/config/log4j.xml"
		
		//Map<String, Object> indexMap = new ConcurrentHashMap<>();
		SLState state = new SLState();
    	
    	// get property
    	PropertyManager pm = new PropertyManager();
		
    	MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		
		RestoreWorker worker = new RestoreWorker(client, database, pm, state);
		
		File file = new File("d:/test.restore");
		worker.service(file);
	}

}
