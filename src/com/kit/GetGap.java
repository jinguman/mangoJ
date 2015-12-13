package com.kit;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;

import com.kit.Dao.TraceDao;
import com.kit.Dao.TraceGapsDao;
import com.kit.Service.GenerateMiniSeed;
import com.kit.Util.Helpers;
import com.kit.Util.MangoJCode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GetGap {

	public static void main(String[] args) throws ParseException, SeedFormatException, IOException, UnsupportedCompressionType, CodecException {
		// TODO Auto-generated method stub

		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://210.114.91.91:18832"));
		MongoDatabase database = client.getDatabase("trace");
		int totSample = 0;
		
		SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat sdfToDay = new SimpleDateFormat("yyyy-MM-dd");
		
		String network = "AK";
		String station = "GHO";
		String location = "";
		String channel = "BH1";
		//String st = "2015-12-02T09:53:10.0000";
		//String et = "2015-12-02T09:53:30.0000";
		String st = "2015-12-11T00:00:00.0000";
		String et = "2015-12-12T00:00:00.0000";

		TraceGapsDao dao = new TraceGapsDao(database);
		
		//Document doc = new Document("_id",Helpers.getTraceGapsKey(network, station, location, channel, Helpers.convertDate(st, sdfToSecond, sdfToDay)));
		Document doc = new Document("_id", java.util.regex.Pattern.compile(network+"_"+station));
		
		MongoCursor<Document> cursor = dao.findTraceGapsCursor(doc);
		while(cursor.hasNext()) {
			Document d = cursor.next();
			System.out.println(d);
		}
	}
	
}
