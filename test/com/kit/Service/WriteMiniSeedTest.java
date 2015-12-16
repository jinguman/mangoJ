package com.kit.Service;

import static org.junit.Assert.*;

import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class WriteMiniSeedTest {

	@Test
	public void write() {
		
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		MongoDatabase database = client.getDatabase("trace");
		
		String network = "PB";
		String station = "B013";
		String location = "";
		String channel = "EH2";
		String st = "2015-12-14T23:52:00.0000";
		String et = "2015-12-14T23:53:00.0000";
		String filename = "d:/test.mseed";
		
		WriteMiniSeed wms = new WriteMiniSeed(client, database);
		System.out.println(wms.write(network, station, location, channel, st, et, filename));
		
	}

}
