package com.kit.Handler;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.kit.Dao.TraceStatsDao;
import com.kit.Exception.ApiNettyServiceException;
import com.kit.Exception.RequestParamException;
import com.kit.Util.MangoJCode;
import com.mongodb.util.JSON;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import spark.utils.StringUtils;

public class ApiRequestStations extends ApiRequestTemplate {

	public ApiRequestStations(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws com.kit.Exception.RequestParamException {
		
		if ( StringUtils.isEmpty(this.reqData.get("username"))) {
			throw new RequestParamException("username parameter is mandatory.");
		}
		
		if ( StringUtils.isEmpty(this.reqData.get("password"))) {
			throw new RequestParamException("password parameter is mandatory.");
		}
		
		if ( StringUtils.isEmpty(this.reqData.get("contents"))) {
			throw new RequestParamException("contents parameter is mandatory.");
		}
		
		String value = this.reqData.get("contents");
		if ( !(value.equals(MangoJCode.PARAM_CONTENTS_VALUE_STATIONS) 
				|| value.equals(MangoJCode.PARAM_CONTENTS_VALUE_COUNT)) ) {
			throw new RequestParamException("contents parameter's value is " 
				+ MangoJCode.PARAM_CONTENTS_VALUE_STATIONS
				+ " or "
				+ MangoJCode.PARAM_CONTENTS_VALUE_COUNT
				);
		}
	};
	
	@Override
	public void service() throws ApiNettyServiceException {

		TraceStatsDao dao = new TraceStatsDao(mongoDatabase); 
		
		Document res = new Document();
		
		if ( reqData.get(MangoJCode.PARAM_CONTENTS).equals(MangoJCode.PARAM_CONTENTS_VALUE_COUNT) ) {
			
			long cnt = dao.countTraceStats(new Document());
			
			res.append("resultCode", OK.code());
			res.append("message", OK.toString());
			res.append("count", cnt);
			
		} else if ( reqData.get(MangoJCode.PARAM_CONTENTS).equals(MangoJCode.PARAM_CONTENTS_VALUE_STATIONS) ) {
			List<Document> documents = dao.findTraceStats(new Document());
			
			res.append("resultCode", OK.code());
			res.append("message", OK.toString());
			res.append("stations", documents);
			
			for(Document d : documents) {
				System.out.println(d.get("sta"));
			}
		}
		
		
		
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        
        
        ByteBuf buffer = Unpooled.copiedBuffer(JSON.serialize(res), CharsetUtil.UTF_8);
        response.content().writeBytes(buffer);
        buffer.release();

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}
}
