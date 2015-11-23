package com.kit.Handler;

import com.kit.Exception.ApiNettyServiceException;
import com.kit.Exception.RequestParamException;
import com.mongodb.client.MongoDatabase;

public interface ApiRequest {

	public void service() throws ApiNettyServiceException, RequestParamException;
	
	public void requestParamValidation() throws RequestParamException;
	
	public void executeService(MongoDatabase mongoDatabase);

}
