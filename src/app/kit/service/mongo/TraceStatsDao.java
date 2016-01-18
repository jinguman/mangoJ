package app.kit.service.mongo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.stereotype.Component;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

/**
 * Trace collection에 관련된 DAO
 * @author jman
 *
 */
@Component
public class TraceStatsDao {

	@Resource(name="mongoCollectionStats") private MongoCollection<Document> traceStatsColl;
	@Resource(name="updateOptionsTrue") private UpdateOptions optionsUpsertTrue;
	@Resource(name="updateOptionsFalse") private UpdateOptions optionsUpsertFalse;
	
    public List<Document> findTraceStats(Document doc) {

        List<Document> stats = new ArrayList<>();
        traceStatsColl.find(doc).into(stats);
        return stats;
    }
    
    public List<Document> findTraceStats(String network, String station, String location, String channel, String st, String et) {
    	
    	Document doc = new Document();
    	
    	if ( !network.equals("*") ) doc.append("net", network);
    	if ( !station.equals("*") ) doc.append("sta", station);
    	if ( !location.equals("*") ) doc.append("loc", location);
    	if ( !channel.equals("*") ) doc.append("cha", channel);
    	
    	doc.append("st", new Document("$gte",st));
    	doc.append("et", new Document("$lte",et));
    	
    	return findTraceStats(doc);
    }
    
    public long countTraceStats(Document doc) {
    	return traceStatsColl.count();
    }
    
    public void upsertTraceStats(Document key, Document doc) {    	
    	
    	try {
    		traceStatsColl.updateOne(key, doc, optionsUpsertTrue);
		} catch (MongoWriteException e) {
			if ( e.getCode() == 11000) {
				traceStatsColl.updateOne(key, doc, optionsUpsertFalse);
			}
		}
    }
}
