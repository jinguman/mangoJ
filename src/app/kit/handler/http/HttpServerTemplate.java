package app.kit.handler.http;

import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;

import java.util.Map;

import org.bson.Document;

import app.kit.exception.HttpServiceException;
import app.kit.exception.RequestParamException;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class HttpServerTemplate implements HttpServerRequest {

	protected Map<String, String> reqData;
	protected ChannelHandlerContext ctx;
	protected Document apiResult;
	
	public Document getApiResult() {
		return apiResult;
	}
	
	public HttpServerTemplate(ChannelHandlerContext ctx, Map<String, String> reqData) {
		this.reqData = reqData;
		this.ctx = ctx;
		apiResult = new Document();
	}

	@Override
	public boolean executeService() {
		
		boolean bo = true;
		try {
			this.requestParamValidation();
			bo = this.service();
		} catch (RequestParamException e) {
			log.warn("{}", e.toString());
			apiResult.append("resultCode", METHOD_NOT_ALLOWED.code())
					.append("message", e.getMessage());
		} catch (HttpServiceException e) {
			log.error("{}", e);
			apiResult.append("resultCode", NOT_IMPLEMENTED)
			.append("message", NOT_IMPLEMENTED.toString());
		}
		return bo;
	}

	@Override
	public void requestParamValidation() throws RequestParamException {
		
		if ( getClass().getClasses().length == 0 ) {
			return;
		}
	}
}
