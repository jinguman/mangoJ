package com.kit.Handler;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Aggregates.sort;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import com.kit.Exception.ApiNettyServiceException;
import com.kit.Exception.RequestParamException;
import com.kit.Util.Helpers;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import spark.utils.StringUtils;

public class ApiRequestTrace extends ApiRequestTemplate {

	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private SimpleDateFormat sdfToMinute = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	
	public ApiRequestTrace(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws com.kit.Exception.RequestParamException {
		
		if ( StringUtils.isEmpty(this.reqData.get("net"))) {
			throw new RequestParamException("There is no net parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("sta"))) {
			throw new RequestParamException("There is no sta parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("cha"))) {
				throw new RequestParamException("There is no cha parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("st"))) {
			throw new RequestParamException("There is no st parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("et"))) {
			throw new RequestParamException("There is no et parameter.");
		}
		
		// format
		try {
			sdfToSecond.parse(this.reqData.get("st"));
		} catch (ParseException e) {
			throw new RequestParamException("st parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}
		
		
		
		try {
			sdfToSecond.parse(this.reqData.get("et"));
		} catch (ParseException e) {
			throw new RequestParamException("et parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}
	};
	
	@Override
	public void service() throws ApiNettyServiceException, RequestParamException {
		
		// get request string
		String network = this.reqData.get("net");
		String station = this.reqData.get("sta");
		String location = this.reqData.get("loc");
		String channel = this.reqData.get("cha");
		String stStr = this.reqData.get("st");
		String etStr = this.reqData.get("et");
		
		String year, month;
		try {
			year = Helpers.getYearString(stStr, sdfToSecond); 
			month = Helpers.getMonthString(stStr, sdfToSecond);
		} catch (ParseException e) {
			throw new RequestParamException("et parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}

		

        // Write Contents
		HttpResponse response;
        ChannelFuture sendFileFuture = null;
        try {

    		MongoCollection<Document> collection = mongoDatabase.getCollection(Helpers.getTraceCollectionName(network, station, location, year, month));
    		
    		// db.AK_ANM__2015.aggregate([
    		//{ $match: { "_id" : {"$gte" : "ANM_2015-11-23T00:31"}, "BHZ.et" : {"$lte" : "2015-11-23T00:32:00.0000"}}},
    		//{ $unwind : "$BHZ" },
    		//{ $project: {_id:0, "BHZ":1}},
    		//{ $sort: {"BHZ.et":1}}
    		//]).pretty()
    		
    		Bson match = match( 
	    				and( gte("_id", station + "_" + Helpers.convertDate(stStr, sdfToSecond, sdfToMinute)),
	    	    				lte(channel + ".et", etStr))
    				);
    		Bson unwind = unwind("$"+channel);
    		Bson project = project(new Document("_id",0).append(channel, 1));
    		Bson sort = sort(new Document(channel + ",et", 1));
    		
    		List<Bson> aggregateParams = new ArrayList<>();
    		aggregateParams.add(match);
    		aggregateParams.add(unwind);
    		aggregateParams.add(project);
    		aggregateParams.add(sort);
    		
    		logger.debug("Mongo query. match: " + Helpers.toJson(match) + ", unwind: " + Helpers.toJson(unwind) + ", project: " + Helpers.toJson(project) + ", sort: " + Helpers.toJson(sort) );
    		MongoCursor<Document> cursor = collection.aggregate(aggregateParams).iterator();
    		
    		// check document
    		if ( false == cursor.hasNext() ) {
    			sendError(ctx, NO_CONTENT, "No Data found.");
    			return;
    		}
    		
    		response = new DefaultHttpResponse(HTTP_1_1, OK);
    		response.headers().add("Content-Disposition", "inline; filename=myfile.txt");
    		response.headers().set(CONTENT_TYPE, "application/octet-stream");
            response.headers().set(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            
            if (reqData.get("Connection").equals("keep-alive")) {
            	response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            // Write the initial line and the header.
            ctx.write(response);
    		
    		int totLen = 0;
    		while(cursor.hasNext()) {

    			Document d = cursor.next();
    			
    			Binary bytes = (Binary) ((Document)d.get(channel)).get("d");
    			totLen += bytes.length();
    			
    			ByteBuf b = Unpooled.wrappedBuffer(bytes.getData());
				
		        // Write the content.
				DefaultHttpContent httpContent = new DefaultHttpContent(b);
				sendFileFuture = ctx.write(httpContent);
    		}
    		
    		
    		logger.debug("Response data length: " + totLen);
    		
    		
        } catch (Exception ignore) {
        	logger.warn("{}", ignore);
            sendError(ctx, NOT_FOUND, ignore.getMessage());
            return;
        }


        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        lastContentFuture.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				System.out.println(">>>>>>> complete./.");
			}
		});

        // Decide whether to close the connection or not.
       // if (!HttpHeaders.isKeepAlive(response)) {
       //     sendFileFuture.addListener(ChannelFutureListener.CLOSE);
       // }
		
	}


}
