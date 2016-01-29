package app.kit.handler.http;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.util.JSON;

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
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpServerRequestHandler extends SimpleChannelInboundHandler<FullHttpMessage> {

	private HttpRequest request;
	private HttpPostRequestDecoder decoder;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk
	private Map<String, String> reqData = new HashMap<String, String>();
	private Document apiResult;
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		
		String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
	    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
        log.info("Request received complete. {}:{}", host, port);
        
        ctx.flush();

	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception {
		
		String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
	    int port = ((InetSocketAddress)ctx.channel().remoteAddress()).getPort();
	    log.info("Request received. {}:{}", host, port );
	    
		// Request Header
		if ( msg instanceof HttpRequest ) {
			this.request = (HttpRequest) msg;
			
			if ( HttpHeaders.is100ContinueExpected(request)) send100Continue(ctx);
			
			HttpHeaders headers = request.headers();
			if ( !headers.isEmpty() ) {
				for (Map.Entry<String, String> h : headers ) {
					reqData.put(h.getKey(), h.getValue());
					
					if ( h.getKey().equals(HttpHeaders.Names.AUTHORIZATION) ) decodeAuthorization(h.getValue());
					log.debug("Request get param. {}: {}", h.getKey(), h.getValue());
				}
			}

			// parsing authorization
			reqData.put("REQUEST_URI", request.getUri());
			reqData.put("REQUEST_METHOD", request.getMethod().name());
		}
		
		// Request Content
		if ( msg instanceof HttpContent) {

			LastHttpContent trailer = msg;
			readPostData();

			HttpServerRequest service = ServiceDispatcher.dispatch(ctx, reqData);

			try {
				boolean isResponse = service.executeService();
				apiResult = service.getApiResult();
				
				if ( isResponse ) {
					if ( !writeResponse(trailer, ctx)) ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
				}
			} finally {
				reqData.clear();
			}
			
			reset();
		}
	}
	
    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        ByteBuf buffer = Unpooled.copiedBuffer(JSON.serialize(apiResult), CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, currentObj.getDecoderResult().isSuccess() ? HttpResponseStatus.OK : BAD_REQUEST);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.content().writeBytes(buffer);
        buffer.release();
        
        if (keepAlive) {
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response);

        return keepAlive;
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
						log.debug("Request post param. {}: {}", attribute.getName(), attribute.getValue());
						
					} catch (IOException e) {
						log.error("BODY Attribute: {}. {}", data.getHttpDataType().name(), e);
						return;
					}
				} else {
					log.info("BODY data. {}: {}", data.getHttpDataType().name(), data);
				}
			}
		} catch (ErrorDataDecoderException e) {
			log.error("{}", e);
		}
		finally {
			if ( decoder != null ) decoder.destroy();
		}
	}

	private void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, CONTINUE);
		ctx.write(response);
	}
}
