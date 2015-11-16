package com.kit.Handler;

import java.util.Map;

import com.kit.Exception.ApiNettyServiceException;

import io.netty.channel.ChannelHandlerContext;

public class ApiRequestDefault extends ApiRequestTemplate {

	public ApiRequestDefault(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void service() throws ApiNettyServiceException {
	}

}
