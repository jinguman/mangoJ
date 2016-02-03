package app.kit.service.http.seismic;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import app.kit.com.util.MangoJCode;
import app.kit.exception.HttpServiceException;
import app.kit.exception.RequestParamException;
import app.kit.handler.http.HttpServerTemplate;
import app.kit.service.mongo.TraceStatsDao;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("stationIssue")
@Scope("prototype")
public class StationIssue extends HttpServerTemplate {


	@Autowired private TraceStatsDao dao;
	
	public StationIssue(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws RequestParamException {
		
		if ( this.reqData.get("REQUEST_USERNAME").isEmpty() ) {
			apiResult.append("resultCode", HttpResponseStatus.NOT_ACCEPTABLE)
				.append("message", "Username, password is empty");
			return;
		} 
		
		if ( this.reqData.get("contents").isEmpty()) throw new RequestParamException("contents parameter is mandatory.");
		String value = this.reqData.get("contents");
		if ( !(value.equals(MangoJCode.PARAM_CONTENTS_VALUE_STATIONS) 
				|| value.equals(MangoJCode.PARAM_CONTENTS_VALUE_COUNT)) ) {
			throw new RequestParamException("contents parameter's value is " 
				+ MangoJCode.PARAM_CONTENTS_VALUE_STATIONS
				+ ", "
				+ MangoJCode.PARAM_CONTENTS_VALUE_COUNT
				);
		}
	};
	
	@Override
	public boolean service() throws HttpServiceException {

		log.info("Seismic StationIssue start.");
		
		if ( reqData.get(MangoJCode.PARAM_CONTENTS).equals(MangoJCode.PARAM_CONTENTS_VALUE_COUNT) ) {
			
			long cnt = dao.countTraceStats(new Document());
			log.debug("Seismic Station count. {}", cnt);
			
			apiResult.append("resultCode", HttpResponseStatus.OK.code());
			apiResult.append("message", HttpResponseStatus.OK.toString());
			apiResult.append("count", cnt);
			
		} else if ( reqData.get(MangoJCode.PARAM_CONTENTS).equals(MangoJCode.PARAM_CONTENTS_VALUE_STATIONS) ) {
			List<Document> documents = dao.findTraceStats(new Document());
			log.debug("Seismic Station contents. {}", documents.size());
			
			apiResult.append("resultCode", HttpResponseStatus.OK.code());
			apiResult.append("message", HttpResponseStatus.OK.toString());
			apiResult.append("stations", documents);
		}
		return true;
	}
}