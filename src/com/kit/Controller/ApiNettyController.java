package com.kit.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kit.Util.PropertyManager;
import com.kit.test.HttpStaticFileServerInitializer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class ApiNettyController {

	private PropertyManager pm;
	final Logger logger = LoggerFactory.getLogger(ApiNettyController.class);
	
	public ApiNettyController(PropertyManager pm) {
		this.pm = pm;
	}
	
	public void run() {
		
		final MongoClient mongoClient = new MongoClient(new MongoClientURI(pm.getStringProperty("mongo.uri")));
        final MongoDatabase mongoDatabase = mongoClient.getDatabase(pm.getStringProperty("mongo.database"));
		
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(pm.getIntegerProperty("ac.thread"));

		ChannelFuture channelFuture = null;
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.handler(new LoggingHandler(LogLevel.INFO))
				.channel(NioServerSocketChannel.class)
				.childHandler(new ApiNettyServerInitializer(null, pm, mongoDatabase));
			
			Channel ch = b.bind(pm.getIntegerProperty("ac.port")).sync().channel();
			
			logger.info("Navigate to http://serverIP {}/", pm.getIntegerProperty("ac.port"));
			
			channelFuture = ch.closeFuture();
			channelFuture.sync();
			
		} catch (InterruptedException e) {
			logger.error("{}", e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
