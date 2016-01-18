package app.kit.handler.http;

import java.net.InetSocketAddress;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.netty.channel.ChannelHandlerContext;

@Component
public class ServiceDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(ServiceDispatcher.class);	
	private static ApplicationContext context;
	private static HttpServerSession session;

    @Autowired
    public void init(ApplicationContext context) {
        ServiceDispatcher.context = context;
    }
	
	public static HttpServerRequest dispatch(ChannelHandlerContext ctx, Map<String, String> requestMap) {

		String serviceUri = requestMap.get("REQUEST_URI");
		String beanName = null;
		
	    // session in
		session = (HttpServerSession) context.getBean(HttpServerSession.class);
		String host = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
	    if ( !session.addSession(host) ) {
	    	beanName = "sessionIssue";
	    } else {
	    	// url check
	    	if ( serviceUri.startsWith("/seismic/stations")) {
				beanName = "stationIssue";
			} else if ( serviceUri.startsWith("/seismic/trace")) {
				beanName = "traceIssue";
			}
	    }

		HttpServerRequest service = null;
		try {
			service = (HttpServerRequest) context.getBean(beanName, ctx, requestMap);
		} catch (Exception e) {
			logger.error("{}", e);
			service = (HttpServerRequest) context.getBean("notFound", ctx, requestMap);
		}
		
		// session out
		session.eraseSession(host);
		return service;
	}
}
