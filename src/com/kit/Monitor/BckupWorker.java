package com.kit.Monitor;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.kit.Dao.TraceStatsDao;
import com.kit.Service.WriteMiniSeed;
import com.kit.Util.Helpers;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.seisFile.mseed.Btime;

public class BckupWorker {

	private List<FileContentVo> contents;
	private FileParser parser;
	private WriteMiniSeed writeMiniSeed;
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private TraceStatsDao traceStatsDao;
	
	final Logger logger = LoggerFactory.getLogger(BckupWorker.class);
	
	public BckupWorker(MongoClient client, MongoDatabase database) {
		parser = new FileParser();
		writeMiniSeed = new WriteMiniSeed(client, database);
		traceStatsDao = new TraceStatsDao(database);
	}
	
	public void service(File file) {
		
		contents = parser.parse(file);
		
		for(FileContentVo content: contents) {
		
			try {
				
				String network = content.getNetwork();
				String station = content.getStation();
				String location = content.getLocation();
				String channel = content.getChannel();
				String st = content.getSt();
				String et = content.getEt();
				
				String dir = content.getDir();
				
				List<Document> docs = traceStatsDao.findTraceStats(network, station, location, channel, st, et);
				
				for(Document doc : docs) {
					logger.debug("Selected doc: {}", doc.toJson());
					
					network = doc.getString("net");
					station = doc.getString("sta");
					location = doc.getString("loc");
					channel = doc.getString("cha");
					
					Btime bt = Helpers.getBtime(st, sdfToSecond);
					String filename = dir + Helpers.getFileName(network, station, location, channel, bt);
					logger.debug("Request: {}.{}.{}.{} {} - {}", network, station, location, channel, st, et);
					writeMiniSeed.write(network, station, location, channel, st, et, filename);
					
				}
				
				
			} catch (ParseException e) {
				logger.warn("ParseException. {}", e);
			}
		}
	}
}
