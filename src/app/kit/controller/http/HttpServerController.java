package app.kit.controller.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import app.kit.com.conf.MangoConf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HttpServerController {
	
	@Autowired private MangoConf conf;  
	
	public void run() {
		
		int port = conf.getAcPort();
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(conf.getAcThread());
		ChannelFuture channelFuture = null;
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.handler(new LoggingHandler(LogLevel.INFO))
				.channel(NioServerSocketChannel.class)
				.childHandler(new HttpServerInitializer(null));
			
			Channel ch = b.bind(port).sync().channel();
			log.info("Navigate to http://serverIP:{}/", port);
			
			channelFuture = ch.closeFuture();
			channelFuture.sync();
			
		} catch (InterruptedException e) {
			log.error("{}", e);
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
}
