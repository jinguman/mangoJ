package app.kit.com.daemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import app.kit.com.conf.MangoConf;
import app.kit.monitor.FileMonitor;
import app.kit.service.mongo.MongoInitialClientService;
import app.kit.vo.SLState;
import lombok.extern.slf4j.Slf4j;

/**
 * Mango file starter Loader Class
 * @author jman
 *
 */
@Slf4j
public class MangoFileLoader implements IMangoLoader{

	private AbstractApplicationContext ctx;
	private FileMonitor monitor;
	ExecutorService exec = Executors.newCachedThreadPool();
	int cnt = 0;

	@Override
	public void startEngine() throws Exception {

		log.info("Start Mango file server.");

		ctx = new AnnotationConfigApplicationContext(MangoConf.class);
		ctx.registerShutdownHook();
		monitor = ctx.getBean(FileMonitor.class);
		monitor.start();
	}

	@Override
	public void stopEngine() throws Exception {
		log.info("Stop Mango file server.");
		
        if ( ctx != null ) ctx.close();
	}

}
