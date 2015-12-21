package com.kit.Monitor;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

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
		
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		
		RestoreWorker worker = new RestoreWorker(client, database);
		
		File file = new File("d:/test.restore");
		worker.service(file);
	}

}
