package app.kit.service.http;

import java.text.SimpleDateFormat;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import app.kit.exception.HttpServiceException;
import app.kit.handler.http.HttpServerTemplate;
import app.kit.service.mongo.TraceDao;
import app.kit.service.seedlink.GenerateMiniSeed;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("sessionIssue")
@Scope("prototype")
public class SessionIssue extends HttpServerTemplate {

	public SessionIssue(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws app.kit.exception.RequestParamException {
		
	}
	
	@Override
	public boolean service() throws HttpServiceException {
		
		log.info("SessionIssue start.");
    	apiResult.append("resultCode", HttpResponseStatus.SERVICE_UNAVAILABLE.code()).append("message", "Too many connections.");
 
    	return true;
 
	}
	

}
