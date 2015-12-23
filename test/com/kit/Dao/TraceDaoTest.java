package com.kit.Dao;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;
import org.junit.Before;
import org.junit.Test;

import com.kit.Service.GenerateMiniSeed;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TraceDaoTest {

	private MongoClient client;
	private MongoDatabase database;
	private TraceDao traceDao;
	
	@Before
	public void setup() {
		client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		//client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		database = client.getDatabase("trace");
		traceDao = new TraceDao(database);		
	}
	
	//@Test
	public void trace() {
		
		
		String network = "CI";
		String station = "MLAC";
		String location = "";
		String channel = "HHN";
		//String st = "2015-12-02T09:53:10.0000";
		//String et = "2015-12-02T09:53:30.0000";
		String st = "2015-12-14T04:20:10.0000";
		String et = "2015-12-14T04:20:10.0000";
		
		
		long startTime = System.currentTimeMillis();
		
		MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, st, et);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		
		System.out.println("took " + estimatedTime + " ms");
		
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			System.out.println(doc.toJson());
		}
	}	

	@Test
	public void traceTime() {
		
		String network = "PB";
		String station = "B001";
		String location = "";
		String channel = "EH1";
		//String st = "2015-12-02T09:53:10.0000";
		//String et = "2015-12-02T09:53:30.0000";
		String st = "2015-12-15T00:00:00.2000";
		String et = "2015-12-15T00:00:14.4000";

		long startTime = System.currentTimeMillis();		
		List<Document> documents = traceDao.getTraceTime(network, station, location, channel, st, et);
		long estimatedTime = System.currentTimeMillis() - startTime;

		System.out.println("took " + estimatedTime + " ms");
		
		System.out.println("size: " + documents.size());
		for(Document doc : documents) {
			System.out.println(doc.toJson());
		}
	}	
}
