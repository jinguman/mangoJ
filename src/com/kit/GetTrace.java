package com.kit;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

import org.bson.Document;
import org.bson.types.Binary;

import com.kit.Dao.TraceDao;
import com.kit.Service.GenerateMiniSeed;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GetTrace {

	public static void main(String[] args) throws ParseException, SeedFormatException, IOException, UnsupportedCompressionType, CodecException {
		// TODO Auto-generated method stub

		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		int totSample = 0;
		
		String network = "UW";
		String station = "FINN";
		String location = "";
		String channel = "ENN";
		//String st = "2015-12-02T09:53:10.0000";
		//String et = "2015-12-02T09:53:30.0000";
		String st = "2015-12-11T08:18:00.0000";
		String et = "2015-12-11T08:22:00.0000";

		//{ "_id" : "AK_ANM__BHE", "it" : "2015-12-03T08:37:18", "net" : "AK", "sta" : "ANM", "loc" : "", "cha" : "BHE", "st" : "2015-12-02T01:38:22.2684", "et" : "2015-12-03T08:36:49.5284" }
		
		TraceDao traceDao = new TraceDao(database);
		GenerateMiniSeed gm = new GenerateMiniSeed();
		
		long startTime = System.currentTimeMillis();
		
		MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, st, et);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		
		System.out.println("took " + estimatedTime + " ms");
		
		int n = 0;
		String filename = "d:/trace.mseed";
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			Document sub = (Document) doc.get(channel);
			//System.out.println(sub.toJson());
			
			Binary binary = (Binary) sub.get("d");
			ByteBuf b = Unpooled.wrappedBuffer(binary.getData());
			
			DataRecord dr = (DataRecord)SeedRecord.read(b.array());
			//System.out.println(dr.toString());
			
			DataRecord dr2 = gm.trimPacket(st, et, dr, false);
			//if ( dr2 != null ) dr2.write(dos);
			
			// Write to file
			//String filename = "d:/trace" + n + ".mseed";
			n++;
			//DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
			if ( dr2 != null ) {
				//System.out.println(">>>>>>>>" + dr2.getControlHeader().getSequenceNum());
				//System.out.println(dr2.toString());
				System.out.println(dr2.getHeader().getStartTime() + "-" + dr2.getHeader().getEndTime() + ", " + dr2.getHeader().getNumSamples() + ", sample: " + dr2.getHeader().getSampleRate());
				totSample += dr2.getHeader().getNumSamples();
				dr2.write(dos);
			}
		
		}
		
		System.out.println(">>>>>>>> total samples: " +totSample );
		
		dos.close();
	}

}
