package com.kit.Handler;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Util.PropertyManager;
import com.mongodb.client.MongoDatabase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

public class ApiNettyRequestHandler extends SimpleChannelInboundHandler<FullHttpMessage> {

	final Logger logger = LoggerFactory.getLogger(ApiNettyRequestHandler.class);

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

	private HttpRequest request;
	private HttpPostRequestDecoder decoder;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk
	private PropertyManager pm;
	private MongoDatabase mongoDatabase;
	
	private Map<String, String> reqData = new HashMap<String, String>();
	
	public ApiNettyRequestHandler(PropertyManager pm, MongoDatabase mongoDatabase) {
		this.pm = pm;
		this.mongoDatabase = mongoDatabase;
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		
		String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
	    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
		
		logger.info("<<<<<<< Request process complete. {}:{}", host, port);
		ctx.flush();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception {
		
		String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
	    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
	    logger.info(">>>>>>> Request process start. {}:{}", host, port );
		
		// Request Decode test
        if (!msg.getDecoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST, "");
            return;
        }
		
		// Request Header
		if ( msg instanceof HttpRequest ) {
			this.request = (HttpRequest) msg;
			
			if ( HttpHeaders.is100ContinueExpected(request)) send100Continue(ctx);
			
			HttpHeaders headers = request.headers();
			if ( !headers.isEmpty() ) {
				for (Map.Entry<String, String> h : headers ) {
					reqData.put(h.getKey(), h.getValue());
					
					if ( h.getKey().equals(HttpHeaders.Names.AUTHORIZATION) ) decodeAuthorization(h.getValue());
					
					System.out.println(">>> " + h.getKey() + ", " + h.getValue());
				}
			}

			// parsing authorization
			reqData.put("REQUEST_URI", request.getUri());
			reqData.put("REQUEST_METHOD", request.getMethod().name());
		}
		
		// Request Content
		if ( msg instanceof HttpContent) {
			//logger.debug("LastHttpContent message received. {}", request.getUri() );
			
			readPostData();

			// dispatch
			ApiRequest service = ServiceDispatcher.dispatch(ctx, reqData);
			
			if ( service == null ) {
				sendError(ctx, BAD_REQUEST, "Request Url is no exist.");
			} else {
				service.executeService(mongoDatabase);
			}
			
			reqData.clear();
            reset();
		}
	}

	private void reset() {
        request = null;
    }
	
	
	private void decodeAuthorization(String str) {

		// Basic dXNlcjpwd2Q=
		if ( str.startsWith("Basic")) {
			str = str.substring(6, str.length());
			ByteBuf stringByteBuf = Unpooled.copiedBuffer(str.toCharArray(), Charset.defaultCharset());
			ByteBuf decodeByteBuf = Base64.decode(stringByteBuf);
			
			String[] strings = decodeByteBuf.toString(Charset.defaultCharset()).split(":");
			
			if ( strings.length == 2) {
				reqData.put("REQUEST_USERNAME", strings[0]);
				reqData.put("REQUEST_PASSWORD", strings[1]);
			}
		}
	}
	
	private void readPostData() {

		try {
			decoder = new HttpPostRequestDecoder(factory, request);
			for( InterfaceHttpData data : decoder.getBodyHttpDatas() ) {
				if ( HttpDataType.Attribute == data.getHttpDataType() ) {
					try {
						Attribute attribute = (Attribute) data;
						reqData.put(attribute.getName(), attribute.getValue());
						
						System.out.println(">>> " + attribute.getName() + ", " + attribute.getValue());
						
					} catch (IOException e) {
						logger.error("BODY Attribute: {}. {}", data.getHttpDataType().name(), e);
						return;
					}
				} else {
					logger.info("BODY data. {}: {}", data.getHttpDataType().name(), data);
				}
			}
		} catch (ErrorDataDecoderException e) {
			logger.error("{}", e);
		}
		finally {
			if ( decoder != null ) decoder.destroy();
		}
	}


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR, "");
        }
    }
    
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String error) {
    	
    	Document doc = new Document();
    	if ( !error.isEmpty() ) {
    		doc.append("message", error);
    	} else {
    		doc.append("message", "Failure: " + status.toString());
    	}
    	doc.append("resultCode", status.code());
    	
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer(doc.toJson(), CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

	private void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, CONTINUE);
		ctx.write(response);
	}
}
