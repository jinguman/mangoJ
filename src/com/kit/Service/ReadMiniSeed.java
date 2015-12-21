package com.kit.Service;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.TraceDao;
import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ReadMiniSeed {

	private TraceDao traceDao;
	private GenerateMiniSeed gm;
	
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy,DDD,HH:mm:ss");	//2015,306,00:49:01.7750
	
	final Logger logger = LoggerFactory.getLogger(ReadMiniSeed.class);

	public ReadMiniSeed(MongoClient client, MongoDatabase database) {
		traceDao = new TraceDao(database);
		gm = new GenerateMiniSeed();
	}
	
	public List<Document> read(File file) {
		
		DataInput input;
		List<Document> documents = new ArrayList<Document>();
		try {
			input = new DataInputStream(new FileInputStream(file.toString()));
			
			while(true) {
				DataRecord dr = (DataRecord) SeedRecord.read(input);
				
				List<DataRecord> records = gm.splitPacketPerMinute(dr);
            	for(DataRecord record : records) {
            		
            		String startTime = record.getHeader().getStartTime();
                    String endTime = record.getHeader().getEndTime();
                    Document d = Helpers.dRecordToDoc(record, Helpers.convertDatePerfectly(startTime, sdf, sdfToSecond), Helpers.convertDatePerfectly(endTime, sdf, sdfToSecond));

                    documents.add(d);
            	}
				
				
			}
		} catch (EOFException e) {
			
		} catch (FileNotFoundException e) {
			logger.warn("{}", e);
			e.printStackTrace();
		} catch (SeedFormatException e) {
			logger.warn("{}", e);
		} catch (IOException e) {
			logger.warn("{}", e);
		} catch(ParseException e) {
			logger.warn("{}", e);
		}
		
		return documents;
	}
	
	
}
