package com.kit.Dao;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.MseedClient.MseedSimpleClient;
import com.kit.Util.Helpers;
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
	final Logger logger = LoggerFactory.getLogger(TraceGapsDao.class);
	
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private SimpleDateFormat sdfToDay = new SimpleDateFormat("yyyy-MM-dd");

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

    public List<Document> findTraceGaps(Document doc, Document proj) {

        List<Document> gaps = new ArrayList<>();
        traceGapsColl.find(doc).projection(proj).into(gaps);
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
			} else {
				logger.warn("{}", e);
			}
		}
    }
    
	public List<Document> getTraceGaps(String network, String station, String location, String channel, String st) {
		
		try {
			st = convertDate(st);
			if ( st == null ) return null;
			String key = Helpers.getTraceGapsKey(network, station, location, channel, st);
			
			Document doc = new Document("_id", key);
			
			return findTraceGaps(doc);
		} catch(ParseException e) {
			logger.error("{}", e);
			return null;
		}
	}

	private String convertDate(String st) throws ParseException {
		String key = null;
		
		if ( st.length() == 10 ) {
			// yyyy-MM-dd
			key = st;
		} else if ( st.length() == 16) {
			// yyyy-MM-ddThh:mm
			key = Helpers.convertDate(st, sdfToMinute, sdfToDay);
		} else if ( st.length() == 19) {
			// yyyy-MMTddThh:mm:ss
			key = Helpers.convertDate(st, sdfToSecond, sdfToDay);
		} 
		
		return key;
	}
	
}
