package app.kit.service.mongo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import app.kit.com.util.Helpers;
import app.kit.com.util.MangoJCode;
import lombok.extern.slf4j.Slf4j;

/**
 * Trace collection에 관련된 DAO
 * @author jman
 *
 */
@Component
@Slf4j
public class TraceGapsDao {


	@Resource(name="mongoCollectionGaps") private MongoCollection<Document> traceGapsColl;
	@Resource(name="updateOptionsTrue") private UpdateOptions optionsUpsertTrue;
	@Resource(name="updateOptionsFalse") private UpdateOptions optionsUpsertFalse;
	
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private SimpleDateFormat sdfToDay = new SimpleDateFormat("yyyy-MM-dd");

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

    	try {
    		traceGapsColl.updateOne(key, doc, optionsUpsertTrue);
		} catch (MongoWriteException e) {
			if ( e.getCode() == 11000) {
				traceGapsColl.updateOne(key, doc, optionsUpsertFalse);
			} else {
				log.warn("{}", e);
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
			log.error("{}", e);
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
