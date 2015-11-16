package com.kit.Handler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

public class ServiceDispatcher {

	public static ApiRequest dispatch(ChannelHandlerContext ctx, Map<String, String> requestMap) {

		final Logger logger = LoggerFactory.getLogger(ApiRequest.class);
		
		String serviceUri = requestMap.get("REQUEST_URI");
		logger.info("Request Uri: {}", serviceUri);
		
		if ( serviceUri.startsWith("/stations")) {
			
			return new ApiRequestStations(ctx, requestMap);
			
		} else if ( serviceUri.startsWith("/trace")) {
			
			return new ApiRequestTrace(ctx, requestMap);
			
		}
		
		return null;
	}

}
