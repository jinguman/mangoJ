package app.kit.service.http;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import app.kit.exception.HttpServiceException;
import app.kit.handler.http.HttpServerTemplate;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("notFound")
@Scope("prototype")
public class NotfoundIssue extends HttpServerTemplate {

	public NotfoundIssue(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public boolean service() throws HttpServiceException {
		this.apiResult.append("resultCode", HttpResponseStatus.NOT_FOUND.code())
						.append("message", "Not found");
		return true;
	}

}
