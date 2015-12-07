package com.kit;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.Binary;

import com.kit.Dao.TraceDao;
import com.kit.Handler.ApiRequestTrace;
import com.kit.Service.GenerateMiniSeed;
import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim1;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.iris.dmc.seedcodec.UnsupportedCompressionType;
import edu.sc.seis.seisFile.mseed.Blockette100;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GetTrace {

	public static void main(String[] args) throws ParseException, SeedFormatException, IOException, UnsupportedCompressionType, CodecException {
		// TODO Auto-generated method stub

		//MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost"));
		MongoClient client = new MongoClient(new MongoClientURI("mongodb://192.168.5.40"));
		MongoDatabase database = client.getDatabase("trace");
		
		String network = "WY";
		String station = "YDC";
		String location = "01";
		String channel = "EHZ";
		//String st = "2015-12-02T09:53:10.0000";
		//String et = "2015-12-02T09:53:30.0000";
		String st = "2015-12-06T08:00:00.0000";
		String et = "2015-12-06T09:00:00.0000";

		//{ "_id" : "AK_ANM__BHE", "it" : "2015-12-03T08:37:18", "net" : "AK", "sta" : "ANM", "loc" : "", "cha" : "BHE", "st" : "2015-12-02T01:38:22.2684", "et" : "2015-12-03T08:36:49.5284" }
		
		TraceDao traceDao = new TraceDao(database);
		//ApiRequestTrace art = new ApiRequestTrace(null, null);
		GenerateMiniSeed gm = new GenerateMiniSeed();
		
		MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, st, et);
		
		int n = 0;
		String filename = "d:/trace.mseed";
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
		while(cursor.hasNext()) {
			Document doc = cursor.next();
			Document sub = (Document) doc.get(channel);
			System.out.println(sub.toJson());
			
			Binary binary = (Binary) sub.get("d");
			ByteBuf b = Unpooled.wrappedBuffer(binary.getData());
			
			DataRecord dr = (DataRecord)SeedRecord.read(b.array());
			//System.out.println(dr.toString());
			
			//DataRecord dr2 = art.splitSlPacket(st, et, dr);
			DataRecord dr2 = gm.trimPacket(st, et, dr);
			//if ( dr2 != null ) dr2.write(dos);
			
			// Write to file
			//String filename = "d:/trace" + n + ".mseed";
			n++;
			//DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));
			if ( dr2 != null ) {
				//System.out.println(">>>>>>>>" + dr2.getControlHeader().getSequenceNum());
				dr2.write(dos);
			}
			
			//dos.close();
/*			
			byte[] originData = dr.getData();

			DecompressedData dd = dr.decompress();
			//System.out.println("Type: " + dd.getTypeString());
			int[] data = dd.getAsInt();
			System.out.println("Data: " + data[0] + ", " + data[1] + " ...(" + data.length + ")");
			System.out.println();System.out.println();
			
			// remake
			
			Blockette1000 blockette1000A = (Blockette1000) dr.getUniqueBlockette(1000);
			//byte seed512 = blockette1000A.getDataRecordLengthByte(); 
			byte seed4096 = (byte)10;
			
			DataHeader header = new DataHeader(n, 'D', false);
	        header.setStationIdentifier(dr.getHeader().getStationIdentifier());
	        header.setChannelIdentifier(dr.getHeader().getChannelIdentifier());
	        header.setNetworkCode(dr.getHeader().getNetworkCode());
	        header.setLocationIdentifier(dr.getHeader().getLocationIdentifier());
	        header.setNumSamples((short) data.length);
	        header.setSampleRate(dr.getHeader().getSampleRate());
	        Btime btime = dr.getHeader().getStartBtime();
	        header.setStartBtime(btime);

	        DataRecord record = new DataRecord(header);
	        Blockette1000 blockette1000 = new Blockette1000();
	        blockette1000.setEncodingFormat((byte)B1000Types.STEIM2);
	        blockette1000.setWordOrder((byte)1);
	        blockette1000.setDataRecordLength(seed4096);
	        record.addBlockette(blockette1000);
	        
	        Blockette100 blockette100 = new Blockette100();
	        blockette100.setActualSampleRate(50.0f);
	        record.addBlockette(blockette100);
	        
	        SteimFrameBlock steimData = null;
	
	        steimData = Steim2.encode(data, 7);
	        record.setData(steimData.getEncodedData());
	        
	        byte[] modifyData = steimData.getEncodedData();

	        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename + ".ccc")));
	        record.write(out);
	        out.close();
	        System.out.println("Wrote miniseed to "+filename+".ccc"+", "+(data.length*4)+" compressed to "+steimData.numNonEmptyFrames()*64
	                           +" record size="+record.getRecordSize());
			
	        // 비교
	        System.out.println(originData.length + "/ " + modifyData.length);
	        for(int i = 0; i< originData.length; i++) {
	        	System.out.println(originData[i] + "|" + modifyData[i]);
	        }
	        
	        // 데이터비교
	        DecompressedData dd2 = record.decompress();
			int[] data2 = dd2.getAsInt();
			System.out.println("Data2: " + datj2[0] + ", " + data2[1] + " ...(" + data.length + ")");
			
*/			
		}
		
		dos.close();
	}

}
