package com.kit.Handler;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE;

import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Exception.ApiNettyServiceException;
import com.kit.Exception.RequestParamException;
import com.mongodb.client.MongoDatabase;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.Setter;

public abstract class ApiRequestTemplate implements ApiRequest {

	final Logger logger = LoggerFactory.getLogger(ApiRequestTemplate.class);
	
	protected Map<String, String> reqData;
	protected ChannelHandlerContext ctx;
	protected MongoDatabase mongoDatabase;
	
	public ApiRequestTemplate(ChannelHandlerContext ctx, Map<String, String> reqData) {
		this.reqData = reqData;
		this.ctx = ctx;
	}

	@Override
	public void executeService(MongoDatabase mongoDatabase) {

		this.mongoDatabase = mongoDatabase;
		
		try {
			this.requestParamValidation();
			this.service();
		} catch (RequestParamException e) {
			//logger.warn("{}", e);
			sendError(ctx, REQUESTED_RANGE_NOT_SATISFIABLE, e.getMessage());
		} catch (ApiNettyServiceException e) {
			logger.warn("{}", e);
			//this.apiResult.append("resultCode", "501");
		}
		
	}

	@Override
	public void requestParamValidation() throws RequestParamException {
		
		if ( getClass().getClasses().length == 0 ) {
			return;
		}
	}

	public final <T extends Enum<T>> T fromValue(Class<T> paramClass, String paramValue) {
        if (paramValue == null || paramClass == null) {
            throw new IllegalArgumentException("There is no value with name '" + paramValue + " in Enum "
                    + paramClass.getClass().getName());
        }

        for (T param : paramClass.getEnumConstants()) {
            if (paramValue.equals(param.toString())) {
                return param;
            }
        }

        throw new IllegalArgumentException("There is no value with name '" + paramValue + " in Enum "
                + paramClass.getClass().getName());
    }
	
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String error) {
    	
    	Document doc = new Document();
    	if ( !error.isEmpty() ) {
    		doc.append("message", error);
    	} else {
    		doc.append("message", "Failure: " + status.toString());
    	}
    	doc.append("resultCode", status.code());
    	
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer(doc.toJson(), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        
        logger.debug("Response: {}",doc.toJson());

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
