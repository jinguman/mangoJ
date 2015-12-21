package com.kit.Dao;

import static org.junit.Assert.*;

import java.util.List;

import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TraceGapsDaoTest {

	private MongoClient client = null;
	private MongoDatabase database = null; 
	private TraceGapsDao dao = null;
	
	@Before
	public void setup() {
		
		//client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		database = client.getDatabase("trace");
		
		dao = new TraceGapsDao(database);
	}
	
	@Test
	public void findCursor() {
		
		String network = "UW";
		String station = "ALCT";
		String location = "";
		String channel = "ENE";
		String st = "2015-12-19";
		
		List<Document> docs = dao.getTraceGaps(network, station, location, channel, st);
		
		for(Document d : docs) {
			System.out.println(d);
		}
		
	}
	
	//@Test
	public void upsert() {
		
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
