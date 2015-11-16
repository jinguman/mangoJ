package com.kit.Dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.kit.Util.MangoJCode;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

public class TraceStatsDao {

	private final MongoCollection<Document> traceStatsColl;
	private UpdateOptions options = new UpdateOptions();
	
    public TraceStatsDao(final MongoDatabase database) {
    	traceStatsColl = database.getCollection(MangoJCode.COLLECTION_TRACE_STATS);
    }
    
    public List<Document> findTraceStats(Document doc) {

        List<Document> stats = new ArrayList<>();
        traceStatsColl.find().into(stats);
        return stats;
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
