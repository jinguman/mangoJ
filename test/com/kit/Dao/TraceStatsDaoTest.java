package com.kit.Dao;

import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class TraceStatsDaoTest {

	private MongoClient client;
	private MongoDatabase database;
	private TraceStatsDao dao;
	
	@Before
	public void setup() {
		
		client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		database = client.getDatabase("trace");
		
		dao = new TraceStatsDao(database);
	}
	
	@Test
	public void find() {
	
		String network = "*";
		String station = "B943";
		String location = "*";
		String channel = "EHZ";
		String st = "2015-12-15T00:00:00.0000";
		String et = "2015-12-15T00:00:00.0000";
		
		List<Document> docs = dao.findTraceStats(network, station, location, channel, st, et);
		
		
		for (Document doc : docs) {
			System.out.println(doc.toJson());
		}
	}

}
