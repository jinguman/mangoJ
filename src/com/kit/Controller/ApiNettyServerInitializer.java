package com.kit.Controller;

import com.kit.Handler.ApiNettyRequestHandler;
import com.kit.Util.PropertyManager;
import com.mongodb.client.MongoDatabase;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

public class ApiNettyServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	private PropertyManager pm;
	private MongoDatabase mongoDatabase;
	
	public ApiNettyServerInitializer(SslContext sslCtx, PropertyManager pm, MongoDatabase mongoDatabase) {
		this.sslCtx = sslCtx;
		this.pm = pm;
		this.mongoDatabase = mongoDatabase;
		
	}

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new ApiNettyRequestHandler(pm, mongoDatabase));
        pipeline.addLast(new HttpContentCompressor());
    }
}
