package com.kit.Dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.kit.Util.MangoJCode;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * Trace collection俊 包访等 DAO
 * @author jman
 *
 */
public class TraceStatsDao {

	private final MongoCollection<Document> traceStatsColl;
	private UpdateOptions options = new UpdateOptions();
	
	/**
	 * 积己磊
	 * @param database mongodbDatabase 按眉
	 */
    public TraceStatsDao(final MongoDatabase database) {
    	traceStatsColl = database.getCollection(MangoJCode.COLLECTION_TRACE_STATS);
    }
    
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
    	
    	doc.append("st", new Document("$lte",st));
    	doc.append("et", new Document("$gte",et));
    	
    	return findTraceStats(doc);
    }
    
    public long countTraceStats(Document doc) {
    	return traceStatsColl.count();
    }
    
    public void upsertTraceStats(Document key, Document doc) {    	
    	
    	options.upsert(true);
    	try {
    		traceStatsColl.updateOne(key, doc, options);
		} catch (MongoWriteException e) {
			if ( e.getCode() == 11000) {
				options.upsert(false);
				traceStatsColl.updateOne(key, doc, options);
			}
		}
    }
}
