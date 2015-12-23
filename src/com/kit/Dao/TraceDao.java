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

import com.kit.Service.GenerateMiniSeed;
import com.kit.Util.Helpers;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import edu.sc.seis.seisFile.mseed.Btime;

public class TraceDao {

	private MongoDatabase database;
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private GenerateMiniSeed gm;
	
	final Logger logger = LoggerFactory.getLogger(TraceDao.class);
	
	public TraceDao(MongoDatabase database) {
		this.database = database;
		this.gm = new GenerateMiniSeed();
	}

	public MongoCursor<Document> getTraceCursor(String network, String station, String location, String channel, String startStr, String endStr) {
		
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

			String gteCond = station + "_" + location + "_" + Helpers.convertDateBefore1Min(startStr, sdfToSecond, sdfToMinute);
			String lteCond = station + "_" + location + "_" + Helpers.convertDate(endStr, sdfToSecond, sdfToMinute);
			
			Bson match = match( 
	    				and( gte("_id", gteCond),
	    	    				lte("_id", lteCond))
					);
			
			Bson unwind = unwind("$"+channel);
			Bson project = project(new Document("_id",0).append(channel, 1));
			Bson sort = sort(new Document(channel + ".et", 1));
			
			List<Bson> aggregateParams = new ArrayList<>();
			aggregateParams.add(match);
			aggregateParams.add(unwind);
			aggregateParams.add(project);
			aggregateParams.add(sort);

			cursor = collection.aggregate(aggregateParams).iterator();
			
		} catch (ParseException e) {
			logger.error("start or end parameter format is 'yyyy-MM-dd'T'HH:mm:ss.SSSS'. {}", e);
			return null;
		}

		return cursor;
	}
	
	public List<Document> getTraceTime(String network, String station, String location, String channel, String startStr, String endStr) {

		String year, month;
		List<Document> documents = new ArrayList<>();
		
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
				
				// ��Ŷ���۽ð��� ��û����ð��� �ڿ� ���� ���
				if ( stPacketBtime.afterOrEquals(etReqBtime) ) continue;
					
				// ��û���۽ð��� ��Ŷ����ð��� �ڿ� ���� ���
				if ( stReqBtime.afterOrEquals(etPacketBtime)) continue;
				
				// ��Ŷ����ð��� ��û����ð����� �ڿ� �ִ� ���
				if ( etPacketBtime.after(etReqBtime)) {
					sub.put("et", endStr);
				}
				
				// ��Ŷ���۽ð��� ��û���۽ð����� �տ� �ִ� ���
				if ( stPacketBtime.before(stReqBtime)) {
					sub.put("st", startStr);
				}
				
				// �񱳴���� ���� ��쿡�� ���������� ����
				if ( before == null ) {
					before = sub;
					continue;
				}
					
				// ���������� �ð��� ����Ǵ� ��쿡�� ���������� �ð��� ����
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
			logger.error("start or end parameter format is 'yyyy-MM-dd'T'HH:mm:ss.SSSS'. {}", e);
			return null;
		}
	}
}
