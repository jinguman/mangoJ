package com.kit.Dao;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import org.bson.Document;
import org.bson.types.Binary;
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

	@Test
	public void test() {
		
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		
		String network = "CI";
		String station = "MLAC";
		String location = "";
		String channel = "HHN";
		//String st = "2015-12-02T09:53:10.0000";
		//String et = "2015-12-02T09:53:30.0000";
		String st = "2015-12-14T04:20:10.0000";
		String et = "2015-12-14T04:20:10.0000";

		
		TraceDao traceDao = new TraceDao(database);
		
		long startTime = System.currentTimeMillis();
		
		MongoCursor<Document> cursor = traceDao.getTraceCursorLight(network, station, location, channel, st, et);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		
		System.out.println("took " + estimatedTime + " ms");
		
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			System.out.println(doc.toJson());
		}
	}	

}
