package app.kit.service.mongo;

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

import javax.annotation.Resource;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import app.kit.com.util.Helpers;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.Btime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TraceDao {

	@Resource(name="mongoDatabaseBean") private MongoDatabase database;

	public MongoCursor<Document> getTraceCursor(String network, String station, String location, String channel, Btime stBtime, Btime etBtime) {
		
		String year, month;
		MongoCursor<Document> cursor = null;

		year = Trace.getBtimeToStringY(stBtime);
		month = Trace.getBtimeToStringH(stBtime);
		MongoCollection<Document> collection = database.getCollection(Helpers.getTraceCollectionName(network, station, location, channel, year, month));
		
		String gteCond = station + "_" + location + "_" + Trace.getBtimeToStringYMDHM(stBtime);
		String lteCond = station + "_" + location + "_" + Trace.getBtimeToStringYMDHM(etBtime);
		
		Bson find = and( gte("_id", gteCond), lte("_id", lteCond));
		Bson project = new Document("_id",0).append(channel + ".d", 1).append(channel+".st", 1);
		Bson sort = new Document("_id",1);
		
		cursor = collection.find(find).projection(project).sort(sort).iterator();

		return cursor;
	}
	
	public MongoCursor<Document> getTraceCursor(String network, String station, String location, String channel, String startStr, String endStr) {
		
		SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
		SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		
		String year, month;
		MongoCursor<Document> cursor = null;
		try {
			year = Helpers.getYearString(startStr, sdfToSecond); 
			month = Helpers.getMonthString(startStr, sdfToSecond);
			MongoCollection<Document> collection = database.getCollection(Helpers.getTraceCollectionName(network, station, location, channel, year, month));
			
			String gteCond = station + "_" + location + "_" + Helpers.convertDate(startStr, sdfToSecond, sdfToMinute);
			String lteCond = station + "_" + location + "_" + Helpers.convertDate(endStr, sdfToSecond, sdfToMinute);
			
			
			Bson find = and( gte("_id", gteCond), lte("_id", lteCond));
			Bson project = new Document("_id",0).append(channel + ".d", 1).append(channel+".st", 1);
			Bson sort = new Document("_id",1);
			
			cursor = collection.find(find)
					.projection(project).sort(sort).iterator();
			
		} catch (ParseException e) {
			log.error("start or end parameter format is 'yyyy-MM-dd'T'HH:mm:ss.SSSS'. {}", e);
			return null;
		}

		return cursor;
	}
	
	public UpdateResult unsetTrace(String network, String station, String location, String channel, Btime stBtime, Btime etBtime) {
		
		String year, month;
		year = Trace.getBtimeToStringY(stBtime);
		month = Trace.getBtimeToStringH(stBtime);
		MongoCollection<Document> collection = database.getCollection(Helpers.getTraceCollectionName(network, station, location, channel, year, month));
		
		String gteCond = station + "_" + location + "_" + Trace.getBtimeToStringYMDHM(stBtime);
		String lteCond = station + "_" + location + "_" + Trace.getBtimeToStringYMDHM(etBtime);
		Bson find = and( gte("_id", gteCond), lte("_id", lteCond));
		Bson unset = new Document("$unset", new Document(channel,""));
		
		return collection.updateOne(find, unset);
	}
	
	@Deprecated
	public MongoCursor<Document> getTraceCursorByAggregate(String network, String station, String location, String channel, String startStr, String endStr) {
		
		SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
		SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		
		String year, month;
		MongoCursor<Document> cursor = null;
		try {
			year = Helpers.getYearString(startStr, sdfToSecond); 
			month = Helpers.getMonthString(startStr, sdfToSecond);

			MongoCollection<Document> collection = database.getCollection(Helpers.getTraceCollectionName(network, station, location, channel, year, month));
			
			// db.AK__2015.aggregate([
			//{ $match: { $and:[ {"_id" : {"$gte" : "ANM_2015-11-23T00:31"}}, {"_id" : {"$lte" : "ANM_2015-11-23T00:32"}}]}},
			//{ $unwind : "$BHZ" },
			//{ $project: {_id:0, "BHZ":1}},
			//{ $sort: {"BHZ.et":1}}
			//]).pretty()
			
			//db.AK_B_2015.aggregate([{ $match: { $and:[{"_id" : {$gte:"ANM__2015-12-02T09:53"}},{_id:{$lte:"ANM_2015-12-02T09:58"}}]}},{ $unwind : "$BHZ" },{ $project: {_id:1,"BHZ":1}}]).pretty()

			//String gteCond = station + "_" + location + "_" + Helpers.convertDateBefore1Min(startStr, sdfToSecond, sdfToMinute);
			String gteCond = station + "_" + location + "_" + Helpers.convertDate(startStr, sdfToSecond, sdfToMinute);
			String lteCond = station + "_" + location + "_" + Helpers.convertDate(endStr, sdfToSecond, sdfToMinute);
			
			Bson match = match( 
	    				and( gte("_id", gteCond),
	    	    				lte("_id", lteCond))
					);
			Bson unwind = unwind("$"+channel);
			Bson project = project(new Document("_id",0).append(channel + ".d", 1));
			Bson sort = sort(new Document(channel + ".et", 1));
			
			List<Bson> aggregateParams = new ArrayList<>();
			aggregateParams.add(match);
			aggregateParams.add(unwind);
			aggregateParams.add(project);
			aggregateParams.add(sort);
			
			cursor = collection.aggregate(aggregateParams).iterator();
			
		} catch (ParseException e) {
			log.error("start or end parameter format is 'yyyy-MM-dd'T'HH:mm:ss.SSSS'. {}", e);
			return null;
		}

		return cursor;
	}
	
	public List<Document> getTraceTime(String network, String station, String location, String channel, String startStr, String endStr) {

		String year, month;
		List<Document> documents = new ArrayList<>();
		
		SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
		SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

		
		try {
			System.out.println(">> " + network + "." + station + "." + location + "." + channel + " " + startStr + " - " + endStr);
			year = Helpers.getYearString(startStr, sdfToSecond); 
			month = Helpers.getMonthString(startStr, sdfToSecond);
			System.out.println(">>> " + network + "." + station + "." + location + "." + channel + " " + startStr + " - " + endStr);

			MongoCollection<Document> collection = database.getCollection(Helpers.getTraceCollectionName(network, station, location, channel, year, month));
			
			// db.AK__2015.aggregate([
			//{ $match: { $and:[ {"_id" : {"$gte" : "ANM_2015-11-23T00:31"}}, {"_id" : {"$lte" : "ANM_2015-11-23T00:32"}}]}},
			//{ $unwind : "$BHZ" },
			//{ $project: {_id:0, "BHZ":1}},
			//{ $sort: {"BHZ.et":1}}
			//]).pretty()
			
			//db.AK_B_2015.aggregate([{ $match: { $and:[{"_id" : {$gte:"ANM__2015-12-02T09:53"}},{_id:{$lte:"ANM_2015-12-02T09:58"}}]}},{ $unwind : "$BHZ" },{ $project: {_id:1,"BHZ":1}}]).pretty()

			String gteCond = station + "_" + location + "_" + Helpers.convertDate(startStr, sdfToSecond, sdfToMinute);
			String lteCond = station + "_" + location + "_" + Helpers.convertDate(endStr, sdfToSecond, sdfToMinute);

			Bson match = match( 
	    				and( gte("_id", gteCond),
	    	    				lte("_id", lteCond))
					);

			Bson unwind = unwind("$"+channel);
			Bson project = project(new Document("_id",0).append(channel + ".st", 1).append(channel + ".et", 1));
			Bson sort = sort(new Document(channel + ".et", 1));

			List<Bson> aggregateParams = new ArrayList<>();
			aggregateParams.add(match);
			aggregateParams.add(unwind);
			aggregateParams.add(project);
			aggregateParams.add(sort);

			
			MongoCursor<Document> cursor = collection.aggregate(aggregateParams).iterator();
			
			Btime stReqBtime = Helpers.getBtime(startStr, sdfToSecond);
			Btime etReqBtime = Helpers.getBtime(endStr, sdfToSecond);
			Document before = null;
			while(cursor.hasNext()) {
				Document d = cursor.next();
				Document sub = (Document) d.get(channel);
				Btime stPacketBtime = Helpers.getBtime(sub.getString("st"), sdfToSecond);
				Btime etPacketBtime = Helpers.getBtime(sub.getString("et"), sdfToSecond);
				
				// 패킷시작시간이 요청종료시간의 뒤에 있을 경우
				if ( stPacketBtime.afterOrEquals(etReqBtime) ) continue;
					
				// 요청시작시간이 패킷종료시간의 뒤에 있을 경우
				if ( stReqBtime.afterOrEquals(etPacketBtime)) continue;
				
				// 패킷종료시간이 요청종료시간보다 뒤에 있는 경우
				if ( etPacketBtime.after(etReqBtime)) {
					sub.put("et", endStr);
				}
				
				// 패킷시작시간이 요청시작시간보다 앞에 있는 경우
				if ( stPacketBtime.before(stReqBtime)) {
					sub.put("st", startStr);
				}
				
				// 비교대상이 없는 경우에는 이전문서로 지정
				if ( before == null ) {
					before = sub;
					continue;
				}
					
				// 이전문서와 시간이 연결되는 경우에는 이전문서의 시간을 연장
				if ( before.get("et").equals(sub.get("st"))) {
					before.put("et", sub.getString("et"));
				} else {
					documents.add(before);
					before = sub;
				}
			}
			if ( before != null ) documents.add(before);
			
			return documents;
			
		} catch (ParseException e) {
			log.error("start or end parameter format is 'yyyy-MM-dd'T'HH:mm:ss.SSSS'. {}", e);
			return null;
		}
	}
}
