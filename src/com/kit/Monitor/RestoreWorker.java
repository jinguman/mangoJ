package com.kit.Monitor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.TraceStatsDao;
import com.kit.Service.MongoSimpleClientService;
import com.kit.Service.ReadMiniSeed;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class RestoreWorker {

	private List<FileContentVo> contents;
	private FileParser parser;
	private ReadMiniSeed readMiniSeed;
	private MongoSimpleClientService mscs;
	
	final Logger logger = LoggerFactory.getLogger(RestoreWorker.class);
	
	public RestoreWorker(MongoClient client, MongoDatabase database) {
		parser = new FileParser();
		readMiniSeed = new ReadMiniSeed(client, database);
	}
	
	public void service(File file) {
		
		contents = parser.parse2(file);

		for(FileContentVo content: contents) {

			String dir = content.getDir();

			// Get all files in directory
			List<File> files = (List<File>) FileUtils.listFiles(new File(dir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

			for(File f : files) {
				List<Document> documents = readMiniSeed.read(f);

				for(Document d : documents) {
					System.out.println(d.toJson());
				}
			}
		}
	}
	
}
