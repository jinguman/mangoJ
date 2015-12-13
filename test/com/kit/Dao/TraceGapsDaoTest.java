package com.kit.Dao;

import static org.junit.Assert.*;

import org.bson.Document;
import org.junit.Test;

import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class TraceGapsDaoTest {

	@Test
	public void test() {
		
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		
		TraceGapsDao dao = new TraceGapsDao(database);
		
		String network = "NET";
		String station = "STA";
		String location = "";
		String channel = "CHA";
		String st = "2015-01-01";
		String hour = "00";
		String min = "00";
		int n = 20;
		
		Document key = new Document()
				.append("_id", network + "_" + station + "_" + location + "_" + channel + "_" + st);
		
		//Document doc = new Document() 
		//		.append("$addToSet", new Document(hour,
		//				new Document("$addToSet", new Document("$inc", new Document(min,n)))
		//
		//));
		
		Document doc = new Document("$inc", new Document("m."+hour + "." + min,n)
											.append("d", n)
											.append("h."+hour, n)
				);				
		
		dao.upsertTraceGaps(key, doc);
	}

}
