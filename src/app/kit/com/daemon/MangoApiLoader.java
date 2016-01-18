package app.kit.com.daemon;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import app.kit.com.conf.MangoConf;
import app.kit.controller.http.HttpServerController;
import lombok.extern.slf4j.Slf4j;

/**
 * Mango api starter Loader Class
 * @author jman
 *
 */
@Slf4j
public class MangoApiLoader implements IMangoLoader{

	private AbstractApplicationContext ctx;
	
	@Override
	public void startEngine() throws Exception {

		log.info("Start Mango api server.");

		ctx = new AnnotationConfigApplicationContext(MangoConf.class);
		ctx.registerShutdownHook();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					stopEngine();
				} catch (Exception e) {
					log.error("There is error in Mango collector server.",e);
				}
			}
		});

		log.info("MongoDB initiate ---------------------------------------");
		HttpServerController controller = ctx.getBean(HttpServerController.class);
		controller.run();
	}

	@Override
	public void stopEngine() throws Exception {
		log.info("Stop Mango api server.");
        if ( ctx != null ) ctx.close();
	}
}
