package app.kit.service.mongo;

import java.util.Map;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import app.kit.com.conf.MangoConf;
import app.kit.com.util.Helpers;
import app.kit.vo.Gaps;
import app.kit.vo.GapsVo;
import app.kit.vo.SLState;
import app.kit.vo.Stats;
import app.kit.vo.StatsVo;
import app.kit.vo.Trace;
import lombok.extern.slf4j.Slf4j;

@Service
@Scope("prototype")
@Slf4j
public class MongoSimpleClientService {

	@Autowired private MangoConf conf;
	@Resource(name="mongoDatabaseBean") private MongoDatabase database;
	@Autowired private TraceStatsDao statsDao;
	@Autowired private TraceGapsDao gapsDao;
	@Resource(name="updateOptionsTrue") private UpdateOptions optionsUpsertTrue;
	@Resource(name="updateOptionsFalse") private UpdateOptions optionsUpsertFalse;
	
	IndexOptions indexOptions = new IndexOptions();
	private MongoCollection<Document> collection = null;
	@Autowired private MongoInitialClientService initialService;

	/**
	 * 똑같은 패킷이 수신되는 경우에만 중복성 체크가 보장됨
	 * @param trace
	 * @return
	 */
	public UpdateResult insertTrace(Trace trace)  {

		indexOptions.background(true);

		String year = trace.getStartYear();
		String month = trace.getStartMonth();
		String network = trace.getNetwork();
		String station = trace.getStation();
		String location = trace.getLocation();
		String channel = trace.getChannel();
		String collectionName = Helpers.getTraceCollectionName(network, station, location, channel, year, month);

		// get collection
		collection = database.getCollection(collectionName);
		if ( conf.isMcIndex() ) initialService.doEtIndex(network, station, location, channel, year, month, true);
		if ( conf.isMcShard() ) {
			if ( conf.isMcIndex() ) initialService.doEtIndex(network, station, location, channel, year, month, true);
			initialService.doShard(network, station, location, channel, year, month);
		}

		// add trace
		Document key = new Document("_id", station + "_" + location + "_" + trace.getStartYYYYMMDDHHMMSS());
		UpdateResult result = addTrace(key, new Document("$addToSet", new Document(channel, trace.toDocument())));		

		return result;
	}

	private UpdateResult addTrace(Document key, Document doc) {

		UpdateResult result = null;
		try {
			result = collection.updateOne(key, doc, optionsUpsertTrue);
		} catch (MongoWriteException e) {

			if (e.getCode() == 11000) {
				// Duplicate Key Exception
				result = collection.updateOne(key, doc, optionsUpsertFalse);
			} else {
				log.warn("Error during write. key: {}, doc: {}", key.toJson(), doc.toJson());
				log.warn("{}", e);
			}
		}
		return result;
	}

	public void insertStats(Stats stats) {

		for(String key : stats.getMap().keySet()) {
			
			StatsVo vo = stats.getMap().get(key);
			String network = vo.getNetwork();
			String station = vo.getStation();
			String location = vo.getLocation();
			String channel = vo.getChannel();
			String st = Trace.getBtimeToStringYMDHMS(vo.getStBtime());
			String et = Trace.getBtimeToStringYMDHMS(vo.getEtBtime());
			
			Document idDoc = new Document("_id", key);
			Document valueDoc = new Document()
					.append("$set", new Document("net", network))
					.append("$set", new Document("sta", station))
					.append("$set", new Document("loc", location))
					.append("$set", new Document("cha", channel))
					.append("$min", new Document("st",st))
					.append("$max", new Document("et",et))
					.append("$set", new Document("it",Helpers.getCurrentUTC()) 
					.append("net", network)
					.append("sta", station)
					.append("loc", location)
					.append("cha", channel)
					);
			
			statsDao.upsertTraceStats(idDoc, valueDoc);
		}
	}
	
	public void insertGaps(Gaps gaps) {
		
		for(String key : gaps.getMap().keySet()) {
			
			GapsVo vo = gaps.getMap().get(key);
			
			float sampleRate = vo.getSampleRate();
			Map<String, Integer> hour = vo.getHour();
			Map<String, Integer> minute = vo.getMinute();
			
			Document idDoc = new Document() 
					.append("_id", key);  

			
			Document setDoc = new Document("s", sampleRate);
			
			Document incDoc = new Document("d", vo.getDay());
			for(String k : hour.keySet()) {
				incDoc.append("h." + k, hour.get(k));
			}
			for(String k : minute.keySet()) {
				incDoc.append("m." + k, minute.get(k));
			}
						
			Document valueDoc = new Document("$set", setDoc).append("$inc", incDoc);
			
			gapsDao.upsertTraceGaps(idDoc, valueDoc);
		}
	}
}
