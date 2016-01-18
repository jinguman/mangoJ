package app.kit.handler.http;

import org.bson.Document;

import app.kit.exception.HttpServiceException;
import app.kit.exception.RequestParamException;

public interface HttpServerRequest {

	public boolean service() throws HttpServiceException;
	public void requestParamValidation() throws RequestParamException;
	public boolean executeService();
	public Document getApiResult();
}
