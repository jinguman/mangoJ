package app.kit.service.http.seismic;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCursor;
import com.mysql.fabric.xmlrpc.base.Array;

import app.kit.com.conf.MangoConf;
import app.kit.com.ipfilter.IpFilter;
import app.kit.com.util.Helpers;
import app.kit.com.util.MangoJCode;
import app.kit.exception.HttpServiceException;
import app.kit.exception.RequestParamException;
import app.kit.handler.http.HttpServerTemplate;
import app.kit.service.TrafficLogService;
import app.kit.service.mongo.TraceDao;
import app.kit.service.seedlink.GenerateMiniSeed;
import app.kit.vo.CursorState;
import app.kit.vo.StimeState;
import app.kit.vo.Trace;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("traceIssue")
@Scope("prototype")
public class TraceIssue extends HttpServerTemplate {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	@Resource(name="trustIpFilterBean") private IpFilter ipFilter;
	@Autowired private TraceDao traceDao;
	@Autowired private GenerateMiniSeed gm;
	@Autowired private MangoConf conf;
	
	public TraceIssue(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
	}

	@Override
	public void requestParamValidation() throws app.kit.exception.RequestParamException {
		
		if ( this.reqData.get("contents") == null || this.reqData.get("contents").isEmpty()) {
			throw new RequestParamException("There is no contents(raw,...) parameter.");
		} else if ( this.reqData.get("net") == null || this.reqData.get("net").isEmpty()) {
			throw new RequestParamException("There is no net parameter.");
		} else if (this.reqData.get("sta") == null ||  this.reqData.get("sta").isEmpty()) {
			throw new RequestParamException("There is no sta parameter.");
		} else if ( this.reqData.get("cha") == null || this.reqData.get("cha").isEmpty()) {
				throw new RequestParamException("There is no cha parameter.");
		} else if ( this.reqData.get("st") == null || this.reqData.get("st").isEmpty()) {
			throw new RequestParamException("There is no st parameter.");
		} else if ( this.reqData.get("et") == null || this.reqData.get("et").isEmpty()) {
			throw new RequestParamException("There is no et parameter.");
		} 
		
		// format
		try {
			String st = this.reqData.get("st");
			sdf.parse(st);
			//if ( st.length() == 19 ) {
			//	st += ".0000";
			//	this.reqData.put("st", st);
			//}
		} catch (ParseException e) {
			throw new RequestParamException("st parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}
		
		try {
			sdf.parse(this.reqData.get("et"));
		} catch (ParseException e) {
			throw new RequestParamException("et parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}

		String value = this.reqData.get("contents");
		if ( !(value.equals(MangoJCode.PARAM_CONTENTS_VALUE_RAW) 
				|| value.equals(MangoJCode.PARAM_CONTENTS_VALUE_RAWMERGE)) ) {
			throw new RequestParamException("contents parameter's value is " 
				+ MangoJCode.PARAM_CONTENTS_VALUE_RAW
				+ ", "
				+ MangoJCode.PARAM_CONTENTS_VALUE_RAWMERGE
				);
		}
	}
	
	@Override
	public boolean service() throws HttpServiceException {
		
		log.info("Seismic TraceIssue start.");
		
		String network = this.reqData.get("net");
		String station = this.reqData.get("sta");
		String location = this.reqData.get("loc");
		String channel = this.reqData.get("cha");
		String stStr = this.reqData.get("st");
		String etStr = this.reqData.get("et");

		Btime stBtime, etBtime;
		try {
			stBtime = Helpers.getBtime(stStr, null);
			etBtime = Helpers.getBtime(etStr, null);
		} catch(ParseException e) {
			log.warn("{}", e);
        	apiResult.append("resultCode", HttpResponseStatus.NOT_FOUND.code()).append("message", "Not found. Illegal restricted Time format in conf. Ask to administrator.");
            return true;
		}
		
		// trust id
		String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
		if ( !ipFilter.accept(host)) {
		
			// filtering network.station.starttime.endtime
			String[] filteringPhrases = conf.getAcRejectStringArray();
			for(String pharse : filteringPhrases) {
				
				String[] words = pharse.trim().split("\\.");
				switch(words.length) {
					case 1:
						// network
						if ( network.equals(words[0])) {
							apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "No data found. Restricted network. " + network );
							return true;
						}
						break;
						
					case 2:
						// network.station
						if ( network.equals(words[0])) {
							if ( words[1].length() == 0 ) {
								apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "Restricted network. " + network );
								return true;
							} else if ( words[1].length() > 0 && station.equals(words[1])) {
								apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "Restricted network, station. " + network + "." + station );
								return true;
							}
						}
						break;
						
					case 4:
						// network.station.st.et
						Btime restStBtime, restEtBtime;
						try {
							restStBtime = Helpers.getBtime(words[2], new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));
							restEtBtime = Helpers.getBtime(words[3], new SimpleDateFormat("yyyy-MM-dd'T'HH:mm"));
						} catch (ParseException e) {
							log.warn("Unknown filter format. {}", pharse);
							continue;
						}
						
						if ( (stBtime.afterOrEquals(restStBtime) && restEtBtime.afterOrEquals(stBtime)) 
								|| (etBtime.afterOrEquals(restStBtime) && restEtBtime.afterOrEquals(etBtime)) 
								) {
							if ( network.equals(words[0])) {
								if ( words[1].length() == 0) {
									apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "Restricted network, time. " + network + ", " + words[2] + " ~ " + words[3]);
									return true;
								} else if ( words[1].length() > 0 && station.equals(words[1])) {
									apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "Restricted network, station, time. " + network + "." + station + ", " + words[2] + " ~ " + words[3]);
									return true;
								}
							}
						}
						break;
						
					default:
						// error
						log.warn("Unknown filter format. {}", pharse);
						break;
				}
			}
			
			// filtering time length
			if ( Helpers.getDiffByMinute(stBtime, etBtime) > conf.getAcRejectTimeLength() ) {
				apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "No data found. Restricted timelength. Not allowed more than " + conf.getAcRejectTimeLength() + " minutes." );
				return true;
			}
			
			// filtering time 
			if ( Helpers.getDiffByMinute(etBtime, Helpers.getCurrentUTCBtime()) < conf.getAcRejectNow() ) {
				apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "No data found. Restricted time. Not allowed " + conf.getAcRejectNow() + " minutes from now" );
				return true;
			}

		} else {
			log.info("Request from trust IP. No filtering. {}", host);
		}
		
    	// process across year
		List<StimeState> stimeStateList = Helpers.splitSttimePerYear(stBtime, etBtime);
		List<CursorState> cursorStateList = new ArrayList<>();
		
		for (StimeState stimeState : stimeStateList) {
			MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, stBtime, etBtime);
			if ( cursor == null || cursor.hasNext() == false ) {}
			else {
				cursorStateList.add(new CursorState(stimeState.getStBtime(), stimeState.getEtBtime(), cursor));
			}
		}
		if ( cursorStateList.size() == 0 ) {
			log.debug("Cursor is null.");
    		apiResult.append("resultCode", HttpResponseStatus.NO_CONTENT.code()).append("message", "No data found.");
    		return true;
		}
		
		log.info("Cursor found.");
		HttpResponse response;
        ChannelFuture sendFileFuture = null;
        try {
        	String fileName = Helpers.getFileName(network, station, location, channel, stBtime);
    		
    		response = new DefaultHttpResponse(HTTP_1_1, OK);
    		response.headers().add("Content-Disposition", "inline; filename=myfile.txt");
    		response.headers().set(CONTENT_TYPE, "application/octet-stream");
            response.headers().set(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);            
            response.headers().set("content-disposition", "attachment; filename=\"" + fileName +"\"");
            
            if (reqData.get("Connection").equals("keep-alive")) {
            	response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            ctx.write(response);
            sendFileFuture = writeTrace(cursorStateList, network, station, location, channel);
    		
        } catch (Exception ignore) {
        	log.warn("{}", ignore);
        	apiResult.append("resultCode", HttpResponseStatus.NOT_FOUND.code()).append("message", "Not found.");
            return true;
        }

        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        lastContentFuture.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				log.debug("Send lastContent complete.");
			}
		});

        if (!HttpHeaders.isKeepAlive(response)) {
            sendFileFuture.addListener(ChannelFutureListener.CLOSE);
        }
        return false;
	}
	
	private ChannelFuture writeTrace(List<CursorState> cursorStateList, 
			String network, String station, String location, String channel 
			) throws SeedFormatException, IOException {

		ChannelFuture sendFileFuture = null;
		int totSize = 0;
		
		for(CursorState cursorState : cursorStateList) {
			MongoCursor<Document> cursor = cursorState.getCursor();
			Btime stBtime = cursorState.getStBtime();
			Btime etBtime = cursorState.getEtBtime();
			
			while(cursor.hasNext()) {

				Document d = cursor.next();
				
				Object o = d.get(channel);
				if (o instanceof Document ) {
					Document sub = (Document) o;
					
					Binary bytes = (Binary) ((Document)sub.get(channel)).get("d");
					ByteBuf b = Unpooled.wrappedBuffer(bytes.getData());
					
					DataRecord dr = (DataRecord)SeedRecord.read(b.array());
					DataRecord dr2 = gm.trimPacket(stBtime, etBtime, dr, false);
					
					if ( dr2 != null ) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream dos = new DataOutputStream(baos);
						dr2.write(dos);
						ByteBuf b2 = Unpooled.wrappedBuffer(baos.toByteArray());
						
						totSize += bytes.length();
						
				        // Write the content.
						DefaultHttpContent httpContent = new DefaultHttpContent(b2);
						sendFileFuture = ctx.write(httpContent);
					}
					
				} else if ( o instanceof ArrayList<?>) {
					List<Document> subs = (List<Document>) o;
					Collections.sort(subs, Helpers.traceCompare);
					for(Document sub : subs) {
						Binary bytes = (Binary) sub.get("d");
						ByteBuf b = Unpooled.wrappedBuffer(bytes.getData());
						
						DataRecord dr = (DataRecord)SeedRecord.read(b.array());
						DataRecord dr2 = gm.trimPacket(stBtime, etBtime, dr, false);
						
						if ( dr2 != null ) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							DataOutputStream dos = new DataOutputStream(baos);
							dr2.write(dos);
							ByteBuf b2 = Unpooled.wrappedBuffer(baos.toByteArray());
							
							totSize += bytes.length();
							
					        // Write the content.
							DefaultHttpContent httpContent = new DefaultHttpContent(b2);
							sendFileFuture = ctx.write(httpContent);
						}
					}
				}
			}
			
			String id = reqData.get("id");
			String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
		    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
			String context = id + "," + network + "," + station + "," + location + "," + channel + "," + Trace.getBtimeToStringYMDHMS(stBtime) + "," + Trace.getBtimeToStringYMDHMS(etBtime) + "," + totSize; 
					
			log.info("Seismic TraceIssue send data(byte/kb/mb). {}:{} {}/{}/{}", host, port, totSize, totSize/1024, totSize/1024/1024);
			TrafficLogService.write("seismic", host+":"+port, context);
			
		}
		
		return sendFileFuture;
	}
}
