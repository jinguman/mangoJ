package com.kit.Service;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.TraceDao;
import com.kit.Util.PropertyManager;import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class WriteMiniSeed {

	private TraceDao traceDao;
	private GenerateMiniSeed gm;
	
	final Logger logger = LoggerFactory.getLogger(WriteMiniSeed.class);

	public WriteMiniSeed(MongoClient client, MongoDatabase database) {
		traceDao = new TraceDao(database);
		gm = new GenerateMiniSeed();
	}
	
	public boolean write(String network, String station, String location, String channel, String st, String et, String filename) {
		
		MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, st, et);
		
		int totSample = 0;
		DataOutputStream dos = null;
		
		try {
			dos = new DataOutputStream(new FileOutputStream(filename));
			while(cursor.hasNext()) {
				Document doc = cursor.next();
				Document sub = (Document) doc.get(channel);
				//System.out.println(sub.toJson());
				
				Binary binary = (Binary) sub.get("d");
				ByteBuf b = Unpooled.wrappedBuffer(binary.getData());
				
				DataRecord dr = (DataRecord)SeedRecord.read(b.array());
				DataRecord dr2 = gm.trimPacket(st, et, dr, false);

				if ( dr2 != null ) {
					totSample += dr2.getHeader().getNumSamples();
					dr2.write(dos);
				}
			
			}
			logger.debug("Write to file. {}_{}_{}_{}, {}-{}, name: {}, nsamp: {}", network, station, location, channel, st, et, filename, totSample);
			
		} catch(IOException e) {
			logger.warn("{}", e);
			return false;
		} catch (SeedFormatException e) {
			logger.warn("{}", e);
			return false;
		}
		
		if ( dos != null) {
			try {
				dos.close();
			} catch (IOException e) {
				logger.warn("{}", e);
				return false;
			}
		}
		return true;
	}
}
