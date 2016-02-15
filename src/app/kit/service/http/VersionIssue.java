package app.kit.service.http;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import app.kit.com.util.MangoJCode;
import app.kit.exception.HttpServiceException;
import app.kit.handler.http.HttpServerTemplate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("versionIssue")
@Scope("prototype")
public class VersionIssue extends HttpServerTemplate {

	public VersionIssue(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws app.kit.exception.RequestParamException {
		
	}
	
	@Override
	public boolean service() throws HttpServiceException {
		
		log.info("VersionIssue start.");
    	apiResult.append("resultCode", HttpResponseStatus.OK.code())
    		.append("message", HttpResponseStatus.OK.toString())
    		.append("version", MangoJCode.PROTOCOL_VERSION);
    	return true;
 
	}
	

}
