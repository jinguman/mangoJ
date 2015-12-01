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

import com.kit.Util.Helpers;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class TraceDao {

	private MongoDatabase database;
	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	
	final Logger logger = LoggerFactory.getLogger(TraceDao.class);
	
	public TraceDao(MongoDatabase database) {
		this.database = database;
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
			
			//db.AK_2015.aggregate([{ $match: { $and:[{"_id" : {$gte:"ANM_2015-11-25T06:00"}},{_id:{$lte:"ANM_2015-11-25T06:02"}}]}},{ $unwind : "$BHE" },{ $project: {_id:1,"BHE":1}}]).pretty()

			Bson match = match( 
	    				and( gte("_id", station + "_" + Helpers.convertDate(startStr, sdfToSecond, sdfToMinute)),
	    	    				lte(channel + ".et", endStr))
					);
			Bson unwind = unwind("$"+channel);
			Bson project = project(new Document("_id",0).append(channel, 1));
			Bson sort = sort(new Document(channel + ",et", 1));
			
			List<Bson> aggregateParams = new ArrayList<>();
			aggregateParams.add(match);
			aggregateParams.add(unwind);
			aggregateParams.add(project);
			aggregateParams.add(sort);
			
			cursor = collection.aggregate(aggregateParams).iterator();
			
		} catch (ParseException e) {
			logger.error("start or end parameter format is 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
			return null;
		}

		return cursor;
	}
}
