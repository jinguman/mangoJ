package com.kit.Dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.kit.Util.MangoJCode;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

/**
 * Trace collection俊 包访等 DAO
 * @author jman
 *
 */
public class TraceGapsDao {

	private final MongoCollection<Document> traceGapsColl;
	private UpdateOptions options = new UpdateOptions();
	
	/**
	 * 积己磊
	 * @param database mongodbDatabase 按眉
	 */
    public TraceGapsDao(final MongoDatabase database) {
    	traceGapsColl = database.getCollection(MangoJCode.COLLECTION_TRACE_GAPS);
    }
    
    public List<Document> findTraceGaps(Document doc) {

        List<Document> gaps = new ArrayList<>();
        traceGapsColl.find(doc).into(gaps);
        return gaps;
    }

    public MongoCursor<Document> findTraceGapsCursor(Document doc) {

        return traceGapsColl.find(doc).iterator();
    }
    
    public long countTraceGaps(Document doc) {
    	return traceGapsColl.count(doc);
    }
    
    public void upsertTraceGaps(Document key, Document doc) {    	
    	
    	options.upsert(true);
    	try {
    		traceGapsColl.updateOne(key, doc, options);
		} catch (MongoWriteException e) {
			if ( e.getCode() == 11000) {
				options.upsert(false);
				traceGapsColl.updateOne(key, doc, options);
			}
		}
    }
}
