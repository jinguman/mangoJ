package com.kit.Monitor;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class BckupWorkerTest {

	// file contents
	/*
	#COMMENT ST ET NET STA LOC CHA DOC (enable char *)
	2015-12-15T00:00:00.0000 2015-12-15T00:20:00.0000 PB B013 * * d:/temp/
	*/
	
	@Test
	public void test() {
		
		// -Dlog4j.configuration="file:./home/config/log4j.xml"
		
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		
		BckupWorker worker = new BckupWorker(client, database);
		
		File file = new File("d:/test.bckup");
		worker.service(file);
	}

}
