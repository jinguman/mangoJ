package com.kit.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.TraceDao;
import com.kit.Dao.TraceGapsDao;
import com.kit.Dao.TraceStatsDao;
import com.kit.Util.Helpers;
import com.kit.Util.PropertyManager;
import com.kit.Vo.SLState;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import edu.sc.seis.seisFile.mseed.Btime;

public class MongoSimpleClientService {

	final Logger logger = LoggerFactory.getLogger(MongoSimpleClientService.class);
	IndexOptions indexOptions = new IndexOptions();
	UpdateOptions options = new UpdateOptions();
	
	private MongoClient client = null;
	private MongoDatabase database = null;
	private MongoCollection<Document> collection = null;
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private SimpleDateFormat sdfToDay = new SimpleDateFormat("yyyy-MM-dd");
	private TraceStatsDao traceStatsDao;
	private TraceGapsDao traceGapsDao;
	private TraceDao traceDao;
	
	boolean isShard = true;
	boolean isIndex = true;
	boolean isWriteGapStats = true;
	int restartSec = 5;
	private MongoInitialClientService mics;
	private GenerateMiniSeed gm;

	public MongoSimpleClientService(PropertyManager pm, SLState state) {

		client = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
		database = client.getDatabase(pm.getStringProperty("mongo.database"));

		traceStatsDao = new TraceStatsDao(database);
		traceGapsDao = new TraceGapsDao(database);
		traceDao = new TraceDao(database);

		isShard = pm.getBooleanProperty("mc.shard");
		isIndex= pm.getBooleanProperty("mc.index");
		restartSec = pm.getIntegerProperty("mc.restartsec");
		isWriteGapStats = pm.getBooleanProperty("mc.writeGapStats");

		mics = new MongoInitialClientService(client, database, state);
		gm = new GenerateMiniSeed();
	}
	
	public UpdateResult insertTraceRaw(Document d) throws ParseException {

		indexOptions.background(true);

		String network = d.getString("network");
		String station = d.getString("station");
		String channel = d.getString("channel");
		String location = d.getString("location");
		String st = d.getString("st");
		String et = d.getString("et");

		d.remove("network");
		d.remove("station");
		d.remove("channel");
		d.remove("location");
		d.remove("seqnum");
		d.remove("sbtime");

		String year = Helpers.getYearString(st, sdfToSecond);
		String month = Helpers.getMonthString(st, sdfToSecond);

		Btime stBtime = Helpers.getBtime(st, sdfToSecond);
		String hour = Helpers.getStrHourBtime(stBtime);
		String min = Helpers.getStrMinBtime(stBtime);

		String collectionName = Helpers.getTraceCollectionName(network, station, location, channel, year, month);

		// get collection
		collection = database.getCollection(collectionName);

		if ( isIndex ) mics.doEtIndex(network, station, location, channel, year, month, true);

		if ( isShard ) {
			if ( isIndex ) mics.doEtIndex(network, station, location, channel, year, month, true);
			mics.doShard(network, station, location, channel, year, month);
		}

		// add trace
		Document key = new Document("_id", station + "_" + location + "_" + Helpers.convertDate(d.getString("st"), sdfToSecond, sdfToMinute));
		//UpdateResult result = addTrace(key, new Document("$addToSet",new Document(channel,d)));		
		UpdateResult result = addTrace(key, new Document("$set",new Document(channel+"."+Helpers.getEpochTimeLong(stBtime),d)));

		// Update or Insert condition
		if ( result.getModifiedCount() > 0 || result.getUpsertedId() != null ) {

			// write trace statistics
			Document keyTraceStatsDoc = new Document()
					.append("_id", network + "_" + station + "_" + location + "_" + channel);
			Document traceStatsDoc = new Document()
					.append("$set", new Document("net", network))
					.append("$set", new Document("sta", station))
					.append("$set", new Document("loc", location))
					.append("$set", new Document("cha", channel))
					.append("$min", new Document("st",st))
					.append("$max", new Document("et",et))
					.append("$set", new Document("it",Helpers.getCurrentUTC(sdfToSecond)) 
											.append("net", network)
											.append("sta", station)
											.append("loc", location)
											.append("cha", channel)
							);

			traceStatsDao.upsertTraceStats(keyTraceStatsDoc, traceStatsDoc);

			// write trace gap statistics
			if (isWriteGapStats) {
				Document keyTraceGapsDoc = new Document()
						.append("_id", Helpers.getTraceGapsKey(network, station, location, channel, Helpers.convertDate(d.getString("st"), sdfToSecond, sdfToDay))); 
				
				Document traceGapsDoc = new Document()
						.append("$set", new Document("s", d.get("s")))
						.append("$inc", new Document("m." + hour + "." + min, d.get("n"))
									.append("h." + hour, d.get("n"))
									.append("d", d.get("n"))
								);

				traceGapsDao.upsertTraceGaps(keyTraceGapsDoc, traceGapsDoc);
			}
		}

		d.clear();
		return result;
	}

	private UpdateResult addTrace(Document key, Document doc) {

		UpdateResult result = null;
		try {
			options.upsert(true);
			result = collection.updateOne(key, doc, options);
		} catch (MongoWriteException e) {

			if (e.getCode() == 11000) {
				// Duplicate Key Exception
				options.upsert(false);
				result = collection.updateOne(key, doc, options);
			} else {
				logger.warn("Error during write. key: {}, doc: {}", key.toJson(), doc.toJson());
				logger.warn("{}", e);
			}
		}
		return result;
	}
}
