package com.kit;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;

import com.kit.Dao.ShardDao;
import com.kit.Service.GenerateMiniSeed;
import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.MiniSeedRead;
import edu.sc.seis.seisFile.mseed.MissingBlockette1000;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;

public class Test {

	public static void main(String[] args) throws ParseException, SeedFormatException, IOException {
		
		

		// read miniseed from file
		//String filename = "d:/KWADG.HGE.KW.00.20150601";
		String filename = "d:/ANM.HGZ.2015.335.00.00.00";
		
		
		DataInputStream ls = null;
        PrintWriter out = new PrintWriter(System.out, true);
        int maxPackets = -1;
        try {
            out.println("open socket");
            
            ls = new DataInputStream(new BufferedInputStream(new FileInputStream(filename), 4096));
            
            MiniSeedRead rf = new MiniSeedRead(ls);
            for(int i = 0; maxPackets == -1 || i < maxPackets; i++) {
                SeedRecord sr;
                try {
                    sr = rf.getNextRecord();
                } catch(MissingBlockette1000 e) {
                    out.println("Missing Blockette1000, trying with record size of 4096");
                    // try with 4096 as default
                    sr = rf.getNextRecord(4096);
                }
                sr.writeASCII(out, "    ");
                if(sr instanceof DataRecord) {
                    DataRecord dr = (DataRecord)sr;
                    byte[] data = dr.getData();
                    // should use seedCodec to do something with the data...
                }
            }
        } catch(EOFException e) {
            System.out.println("EOF, so done.");
        } catch(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            try {
                if(ls != null)
                    ls.close();
            } catch(Exception ee) {}
        }

		
	}
	
	
	

}
