package com.kit.Handler;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.TRANSFER_ENCODING;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Dao.TraceDao;
import com.kit.Exception.ApiNettyServiceException;
import com.kit.Exception.RequestParamException;
import com.kit.Service.MongoInitialClientService;
import com.kit.Util.Helpers;
import com.kit.Util.MangoJCode;
import com.mongodb.client.MongoCursor;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.CodecException;
import edu.iris.dmc.seedcodec.DecompressedData;
import edu.iris.dmc.seedcodec.Steim2;
import edu.iris.dmc.seedcodec.SteimException;
import edu.iris.dmc.seedcodec.SteimFrameBlock;
import edu.sc.seis.seisFile.mseed.Blockette1000;
import edu.sc.seis.seisFile.mseed.Btime;
import edu.sc.seis.seisFile.mseed.DataHeader;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import edu.sc.seis.seisFile.seedlink.SeedlinkException;
import edu.sc.seis.seisFile.seedlink.SeedlinkPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import spark.utils.StringUtils;

public class ApiRequestTrace extends ApiRequestTemplate {

	private SimpleDateFormat sdfToSecond = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	private TraceDao traceDao = null;
	
	final Logger logger = LoggerFactory.getLogger(ApiRequestTrace.class);
	
	public ApiRequestTrace(ChannelHandlerContext ctx, Map<String, String> reqData) {
		super(ctx, reqData);
		
	}

	@Override
	public void requestParamValidation() throws com.kit.Exception.RequestParamException {
		
		if ( StringUtils.isEmpty(this.reqData.get("REQUEST_USERNAME")) ) {
			throw new RequestParamException("There is no username, password parameter.");
		}
		
		if ( StringUtils.isEmpty(this.reqData.get("net"))) {
			throw new RequestParamException("There is no net parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("sta"))) {
			throw new RequestParamException("There is no sta parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("cha"))) {
				throw new RequestParamException("There is no cha parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("st"))) {
			throw new RequestParamException("There is no st parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("et"))) {
			throw new RequestParamException("There is no et parameter.");
		} else if ( StringUtils.isEmpty(this.reqData.get("content"))) {
			throw new RequestParamException("There is no content(raw,...) parameter.");
		}

		// format
		try {
			sdfToSecond.parse(this.reqData.get("st"));
		} catch (ParseException e) {
			throw new RequestParamException("st parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}
		
		try {
			sdfToSecond.parse(this.reqData.get("et"));
		} catch (ParseException e) {
			throw new RequestParamException("et parameter's format 'yyyy-MM-dd'T'HH:mm:ss.SSSS'");
		}
		/*
		String value = this.reqData.get("contents");
		if ( !(value.equals(MangoJCode.PARAM_CONTENTS_VALUE_RAW) 
				|| value.equals(MangoJCode.PARAM_CONTENTS_VALUE_RAWMERGE)) ) {
			throw new RequestParamException("contents parameter's value is " 
				+ MangoJCode.PARAM_CONTENTS_VALUE_RAW
				+ ", "
				+ MangoJCode.PARAM_CONTENTS_VALUE_RAWMERGE
				);
		}*/
	}
	
	@Override
	public void service() throws ApiNettyServiceException, RequestParamException {
		
		this.traceDao = new TraceDao(mongoDatabase); 
		
		// get request string
		String network = this.reqData.get("net");
		String station = this.reqData.get("sta");
		String location = this.reqData.get("loc");
		String channel = this.reqData.get("cha");
		String stStr = this.reqData.get("st");
		String etStr = this.reqData.get("et");

		// Get cursor
    	MongoCursor<Document> cursor = traceDao.getTraceCursor(network, station, location, channel, stStr, etStr);
    	if ( cursor == null ) {
    		logger.warn("Cursor is null.");
    		sendError(ctx, NO_CONTENT, "No Data found.");
    		return;
    	}

		// check document
		if ( false == cursor.hasNext() ) {
			sendError(ctx, NO_CONTENT, "No Data found.");
			return;
		}
		
        // Write Contents
		HttpResponse response;
        ChannelFuture sendFileFuture = null;
        try {
    		
    		response = new DefaultHttpResponse(HTTP_1_1, OK);
    		response.headers().add("Content-Disposition", "inline; filename=myfile.txt");
    		response.headers().set(CONTENT_TYPE, "application/octet-stream");
            response.headers().set(TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            
            if (reqData.get("Connection").equals("keep-alive")) {
            	response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            // Write the initial line and the header.
            ctx.write(response);
    		
    		// send binary
            sendFileFuture = writeRaw(cursor, network, station, location, channel, stStr, etStr);
    		
        } catch (Exception ignore) {
        	logger.warn("{}", ignore);
            sendError(ctx, NOT_FOUND, ignore.getMessage());
            return;
        }


        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        lastContentFuture.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				logger.debug("Send lastContent complete.");
				//System.out.println(">>>>>>> complete./.");
			}
		});

        // Decide whether to close the connection or not.
        if (!HttpHeaders.isKeepAlive(response)) {
            sendFileFuture.addListener(ChannelFutureListener.CLOSE);
        }
	}
	
	private ChannelFuture writeRaw(MongoCursor<Document> cursor, String network, String station, String location, String channel, String stStr, String etStr) throws SeedFormatException, IOException {

		ChannelFuture sendFileFuture = null;
		int totLen = 0;
		while(cursor.hasNext()) {

			Document d = cursor.next();
			//System.out.println(d);
			
			Binary bytes = (Binary) ((Document)d.get(channel)).get("d");
			ByteBuf b = Unpooled.wrappedBuffer(bytes.getData());
			
			DataRecord dr = (DataRecord)SeedRecord.read(b.array());
			
			System.out.println("PACKET: " + dr.toString());
			
			Blockette1000 b1000 = (Blockette1000)dr.getUniqueBlockette(1000);
			System.out.println("PACKET_B1000: " + (int)b1000.getEncodingFormat() + ", " + b1000.getDataRecordLength());
			
			splitSlPacket(stStr, etStr, dr);
			
			if ( dr != null ) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				dr.write(dos);
				ByteBuf b2 = Unpooled.wrappedBuffer(baos.toByteArray());
				
				totLen += bytes.length();
				
		        // Write the content.
				DefaultHttpContent httpContent = new DefaultHttpContent(b2);
				sendFileFuture = ctx.write(httpContent);
			}
			
		}
		
		
		logger.debug("Response data length: " + totLen);
		return sendFileFuture;
	}
	
	private void writeRawMerge() {
		
	}
	
	public DataRecord splitSlPacket(String stStr, String etStr, DataRecord dr) {
		
		// seedlinkpacket parsing
		try {
			//SeedlinkPacket sp = new SeedlinkPacket(bytes.getData());
			DataHeader header = dr.getHeader();
			
			// seedpacket time
			Btime stPacketBtime = header.getStartBtime();
			Btime etPacketBtime = header.getLastSampleBtime();
			int sampleRate = Math.round(header.getSampleRate());
			
			// request time
			Btime stReqBtime = Helpers.getBtime(stStr, sdfToSecond);
			Btime etReqBtime = Helpers.getBtime(etStr, sdfToSecond);
			
			//System.out.println("Req: " + stReqBtime.toString() + " ~ " + etReqBtime.toString());
			
			// check range.
			// 요청시작시간이 요청종료시간의 뒤에 있을 경우
			if ( stReqBtime.afterOrEquals(etReqBtime) ) {
				logger.warn("Range invalid. start time must be before endtime. stReq: " + stReqBtime.toString() + ", etReq: " + etReqBtime.toString());
				return null;
			}
			
			// check range. 
			//                                       |stPacketBtime         |etPacketBtime
			//       |stReqBtime            |etReqBtime
			// 패킷시작시간이 요청종료시간의 뒤에 있을 경우
			if ( stPacketBtime.afterOrEquals(etReqBtime) ) {
				logger.warn("Range invalid. request time within miniseed packet time. Req: " + stReqBtime.toString() + " ~ " + etReqBtime.toString() 
								+ ", packet: " + stPacketBtime.toString() + " ~ " + etPacketBtime.toString()
						);
				return null;
			}
			// 요청시작시간이 패킷종료시간의 뒤에 있을 경우
			if ( stReqBtime.afterOrEquals(etPacketBtime)) {
				logger.warn("Range invalid. request time within miniseed packet time. Req: " + stReqBtime.toString() + " ~ " + etReqBtime.toString() 
				+ ", packet: " + stPacketBtime.toString() + " ~ " + etPacketBtime.toString()
						);
				return null;
			}
			
			// check range. 시작패킷시간이 요청시작시간보다 뒤에 있고, 요청종료시간이 종료패킷시간보다 뒤에 있을 경우
			//              |stPacketBtime         |etPacketBtime
			//       |stReqBtime                           |etReqBtime
			if ( stPacketBtime.afterOrEquals(stReqBtime) && etReqBtime.afterOrEquals(etPacketBtime)) {
				return dr;
			}
			
			// get Data
			DecompressedData decomData = dr.decompress();
            int[] temp = decomData.getAsInt();
			
            System.out.println(">>>>>>>>LENGTH : " + temp.length);
            
            int lTrimDelta = 0;
			int rTrimDelta = 0;
            
			// case1.       |stPacketBtime         |etPacketBtime
			//                    |stReqBtime
			// 요청시작시간이 시작패킷시간보다 뒤에 있고
			if ( stReqBtime.after(stPacketBtime) ) {
				lTrimDelta = (int) (Math.floor((Helpers.getEpochTime(stPacketBtime) - Helpers.getEpochTime(stReqBtime))* sampleRate)) * -1;
			}
			
			// case1.       |stPacketBtime         |etPacketBtime
			//                                 |etReqBtime      
			// 패킷종료시간이 요청종료시간보다 뒤에 있고
			if ( etPacketBtime.after(etReqBtime) ) {
				rTrimDelta = (int) Math.floor(( Helpers.getEpochTime(etPacketBtime) - Helpers.getEpochTime(etReqBtime) ) * sampleRate); 
			}
			
			System.out.println(">>>>>>>>> Req: " + stReqBtime.toString() + " ~ " + etReqBtime.toString() 
			+ ", packet: " + stPacketBtime.toString() + " ~ " + etPacketBtime.toString()
					);
			System.out.println(">>>>>>>> SPLIT: " + lTrimDelta + ", " + rTrimDelta);

			// cut 
			int[] temp2 = new int[temp.length - lTrimDelta - rTrimDelta]; 
			System.arraycopy(temp, lTrimDelta, temp2, 0, temp2.length);
			
			// remake seedlink packet
			SteimFrameBlock steimData = null;
	        steimData = Steim2.encode(temp2, 63);
	        
	        // modify header
	        header.setNumSamples((short)temp2.length);
	        header.setStartBtime(Helpers.getBtimeAddSamples(stPacketBtime, sampleRate, lTrimDelta));

	        dr.setData(steimData.getEncodedData());
			
	        return dr;
	        
		} catch (ParseException e) {
			logger.error("{}", e);
			return null;
		} catch (CodecException e) {
			logger.error("{}", e);
			return null;
		} catch (SeedFormatException e) {
			logger.error("{}", e);
			return null;
		} catch (IOException e) {
			logger.error("{}", e);
			return null;
		}
    }

}
