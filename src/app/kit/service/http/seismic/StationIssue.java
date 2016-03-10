package app.kit.service.http.seismic;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import app.kit.com.conf.MangoConf;
import app.kit.com.ipfilter.IpFilter;
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

	@Resource(name="trustIpFilterBean") private IpFilter ipFilter;
	@Autowired private TraceStatsDao dao;
	@Autowired private MangoConf conf;
	private String[] filteringWord;
	
	public StationIssue(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws RequestParamException {
		
		if ( !this.reqData.containsKey("contents")) throw new RequestParamException("contents parameter is mandatory.");
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
			List<Document> documents = null;
			
			if ( reqData.containsKey("net") || reqData.containsKey("sta") || reqData.containsKey("loc") || reqData.containsKey("cha") ) {
				String net = "*";
				String sta = "*";
				String loc = "*";
				String cha = "*";
				if ( reqData.containsKey("net") ) net = reqData.get("net");
				if ( reqData.containsKey("sta") ) sta = reqData.get("sta");
				if ( reqData.containsKey("loc") ) loc = reqData.get("loc");
				if ( reqData.containsKey("cha") ) cha = reqData.get("cha");
				documents = dao.findTraceStats(net, sta, loc, cha, null, null);
			} else {
				documents = dao.findTraceStats(new Document());
			}
			
			// trust id
			String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
			if ( !ipFilter.accept(host)) {
			
				// filtering
				filteringWord = conf.getAcRejectStringArray();
				int docLen = documents.size()-1;
				for(int i = docLen; i>=0; i--) {
					for(String word : filteringWord) {
						
						// net.sta -> net_sta_
						word = word.replaceAll("\\.", "_");
						if ( !word.endsWith("_")) word += "_";
						//System.out.println(">>>> " + word);
						
						if ( documents.get(i).getString("_id").startsWith(word) ) {
							documents.remove(i);
							break;
						}
					}
				}
			} else {
				log.info("Request from trust IP. No filtering. {}", host);
			}

			log.debug("Get seismic Station contents. size: {}", documents.size());
			
			apiResult.append("resultCode", HttpResponseStatus.OK.code());
			apiResult.append("message", HttpResponseStatus.OK.toString());
			apiResult.append("stations", documents);
		}
		return true;
	}
}
