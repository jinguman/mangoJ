package com.kit.Handler;

import java.util.Map;

import com.kit.Exception.ApiNettyServiceException;
import com.kit.Exception.RequestParamException;

import io.netty.channel.ChannelHandlerContext;
import spark.utils.StringUtils;

public class ApiRequestTrace extends ApiRequestTemplate {

	public ApiRequestTrace(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws com.kit.Exception.RequestParamException {
		if ( StringUtils.isEmpty(this.reqData.get("sta"))) {
			throw new RequestParamException("There is no sta parameter.");
		}
	};
	
	@Override
	public void service() throws ApiNettyServiceException {

	}

}
