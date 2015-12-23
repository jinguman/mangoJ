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
		String station = "B001";
		String location = "";
		String channel = "EH1";
		String st = "2015-12-15T00:00:00.0000";
		String et = "2015-12-15T00:01:00.0000";
		String filename = "d:/test.mseed";
		
		//db.PB_E_2015.update({_id:'B001__2015-12-15T00:00'},{$pull:{'EH1':{'st':'2015-12-15T00:00:06.5184'}}})
		
		WriteMiniSeed wms = new WriteMiniSeed(client, database);
		System.out.println(wms.write(network, station, location, channel, st, et, filename));
		
	}

}
